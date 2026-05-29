<template>
  <section class="admin-shell">
    <aside class="admin-menu">
      <h2>管理后台</h2>
      <button v-for="item in menu" :key="item.key" :class="{ active: tab === item.key }" @click="switchTab(item.key)">
        {{ item.label }}
      </button>
    </aside>

    <main class="admin-panel">
      <header class="admin-header">
        <div>
          <h1>{{ currentTitle }}</h1>
          <p>{{ currentHint }}</p>
        </div>
        <el-button @click="load">刷新数据</el-button>
      </header>

      <section v-if="tab === 'dashboard'" class="dashboard-space">
        <div class="stats">
          <el-statistic title="文章总数" :value="stats.articleCount || 0" />
          <el-statistic title="已发布" :value="stats.publishedArticleCount || 0" />
          <el-statistic title="待审评论" :value="stats.pendingCommentCount || 0" />
          <el-statistic title="留言数量" :value="stats.messageCount || 0" />
          <el-statistic title="总阅读量" :value="stats.totalViews || 0" />
        </div>
        <div class="admin-grid two">
          <el-card shadow="never">
            <template #header>最新文章</template>
            <div v-for="item in articles.records.slice(0, 6)" :key="item.id" class="mini-row">
              <span>{{ item.title }}</span>
              <el-tag size="small" :type="item.status === 'PUBLISHED' ? 'success' : 'info'">{{ statusText(item.status) }}</el-tag>
            </div>
          </el-card>
          <el-card shadow="never">
            <template #header>待处理</template>
            <div class="mini-row"><span>待审核评论</span><strong>{{ pendingComments.length }}</strong></div>
            <div class="mini-row"><span>待审核留言</span><strong>{{ pendingMessages.length }}</strong></div>
            <div class="mini-row"><span>待审核友链</span><strong>{{ pendingLinks.length }}</strong></div>
          </el-card>
        </div>
      </section>

      <section v-if="tab === 'articles'">
        <div class="admin-actions">
          <el-button type="primary" @click="newArticle">新建文章</el-button>
        </div>
        <el-table :data="articles.records" row-key="id">
          <el-table-column prop="title" label="标题" min-width="220" />
          <el-table-column label="分类" width="130">
            <template #default="{ row }">{{ row.category?.name || '-' }}</template>
          </el-table-column>
          <el-table-column label="标签" min-width="160">
            <template #default="{ row }">
              <el-tag v-for="tag in row.tags || []" :key="tag.id" size="small" class="gap-tag">{{ tag.name }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" label="阅读" width="90" />
          <el-table-column label="操作" width="230" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="editArticle(row)">编辑</el-button>
              <el-button size="small" @click="previewArticle(row)">预览</el-button>
              <el-button size="small" type="danger" @click="removeArticle(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'editor'" class="editor-grid">
        <el-form :model="articleForm" label-position="top" class="article-form">
          <div class="admin-grid two">
            <el-form-item label="标题"><el-input v-model="articleForm.title" @blur="ensureSlug" /></el-form-item>
            <el-form-item label="Slug"><el-input v-model="articleForm.slug" placeholder="article-url-slug" /></el-form-item>
          </div>
          <el-form-item label="摘要"><el-input v-model="articleForm.summary" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item>
          <div class="admin-grid two">
            <el-form-item label="分类">
              <el-select v-model="articleForm.categoryId" placeholder="选择分类" filterable>
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="标签">
              <el-select v-model="articleForm.tagIds" multiple placeholder="选择标签" filterable>
                <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
              </el-select>
            </el-form-item>
          </div>
          <el-form-item label="封面">
            <div class="cover-tools">
              <el-upload :http-request="uploadCover" :show-file-list="false" accept="image/*">
                <el-button>上传封面</el-button>
              </el-upload>
              <el-input v-model="articleForm.coverUrl" placeholder="也可以粘贴图片地址" />
            </div>
            <img v-if="articleForm.coverUrl" class="cover-preview" :src="articleForm.coverUrl" alt="" />
          </el-form-item>

          <el-form-item label="正文">
            <el-segmented v-model="editorMode" :options="editorOptions" class="mode-tabs" />
            <div v-if="editorMode === 'rich'" class="rich-editor" contenteditable="true" @input="syncRichContent" @blur="syncRichContent" ref="richRef"></div>
            <el-input v-else v-model="articleForm.content" type="textarea" :rows="18" placeholder="支持 HTML，也可以保留 Markdown 与代码块文本" />
          </el-form-item>

          <el-divider>SEO</el-divider>
          <el-form-item label="SEO 标题"><el-input v-model="articleForm.seoTitle" /></el-form-item>
          <el-form-item label="SEO 关键词"><el-input v-model="articleForm.seoKeywords" placeholder="多个关键词用英文逗号分隔" /></el-form-item>
          <el-form-item label="SEO 描述"><el-input v-model="articleForm.seoDescription" type="textarea" :rows="3" /></el-form-item>
          <div class="admin-actions sticky-actions">
            <el-switch v-model="articleForm.featured" :active-value="1" :inactive-value="0" active-text="推荐" />
            <div>
              <el-button @click="setDraftAndSave">保存草稿</el-button>
              <el-button type="primary" @click="publishArticle">发布</el-button>
            </div>
          </div>
        </el-form>
        <article class="preview-pane">
          <div class="preview-head">
            <h2>{{ articleForm.title || '文章预览' }}</h2>
            <el-tag :type="articleForm.status === 'PUBLISHED' ? 'success' : 'info'">{{ statusText(articleForm.status) }}</el-tag>
          </div>
          <img v-if="articleForm.coverUrl" class="detail-cover" :src="articleForm.coverUrl" alt="" />
          <p class="meta">{{ articleForm.summary }}</p>
          <div class="article-body" v-html="renderedPreview"></div>
        </article>
      </section>

      <section v-if="tab === 'categories'">
        <entity-manager title="分类" :items="categories" :fields="categoryFields" @save="saveCategory" @remove="removeCategory" />
      </section>

      <section v-if="tab === 'tags'">
        <entity-manager title="标签" :items="tags" :fields="tagFields" @save="saveTag" @remove="removeTag" />
      </section>

      <section v-if="tab === 'comments'">
        <el-table :data="comments" row-key="id">
          <el-table-column prop="authorName" label="用户" width="140" />
          <el-table-column prop="authorEmail" label="邮箱" width="180" />
          <el-table-column prop="content" label="内容" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }"><el-tag>{{ statusText(row.status) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="210">
            <template #default="{ row }">
              <el-button size="small" type="success" @click="setComment(row.id, 'APPROVED')">通过</el-button>
              <el-button size="small" type="warning" @click="setComment(row.id, 'REJECTED')">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'messages'">
        <el-table :data="messages" row-key="id">
          <el-table-column prop="authorName" label="用户" width="140" />
          <el-table-column prop="authorEmail" label="邮箱" width="180" />
          <el-table-column prop="content" label="留言" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }"><el-tag>{{ statusText(row.status) }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="230">
            <template #default="{ row }">
              <el-button size="small" type="success" @click="setMessage(row.id, 'APPROVED')">通过</el-button>
              <el-button size="small" type="warning" @click="setMessage(row.id, 'REJECTED')">拒绝</el-button>
              <el-button size="small" type="danger" @click="deleteMessage(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'links'">
        <entity-manager title="友链" :items="links" :fields="linkFields" @save="saveLink" @remove="removeLink" />
      </section>

      <section v-if="tab === 'config'">
        <el-form :model="configForm" label-position="top" class="config-form">
          <el-form-item label="网站标题"><el-input v-model="configForm.siteTitle" /></el-form-item>
          <el-form-item label="Logo"><el-input v-model="configForm.logo" /></el-form-item>
          <el-form-item label="ICP备案"><el-input v-model="configForm.icp" /></el-form-item>
          <el-form-item label="关于我"><el-input v-model="configForm.about" type="textarea" :rows="8" /></el-form-item>
          <el-form-item label="存储模式"><el-input v-model="configForm.ossMode" /></el-form-item>
          <el-button type="primary" @click="saveConfig">保存配置</el-button>
        </el-form>
      </section>

      <section v-if="tab === 'ops'" class="ops-panel">
        <el-alert title="健康检查地址：/api/actuator/health。Docker 会使用该地址判断后端是否可用。" type="info" :closable="false" />
        <div class="ops-actions">
          <el-button type="primary" @click="backup">立即备份数据库</el-button>
          <el-button @click="checkHealth">检查后端健康</el-button>
        </div>
        <el-descriptions v-if="health" title="健康状态" :column="1" border>
          <el-descriptions-item label="status">{{ health.status }}</el-descriptions-item>
        </el-descriptions>
      </section>
    </main>
  </section>
</template>

<script setup>
import { computed, defineComponent, h, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'

const EntityManager = defineComponent({
  props: { title: String, items: Array, fields: Array },
  emits: ['save', 'remove'],
  setup(props, { emit }) {
    const draft = reactive({})
    const editing = ref(null)
    const editValue = reactive({})
    const startEdit = item => {
      editing.value = item.id
      Object.assign(editValue, item)
    }
    const saveDraft = () => {
      emit('save', { ...draft })
      props.fields.forEach(f => draft[f.key] = '')
    }
    return () => h('div', [
      h('div', { class: 'inline-form' }, [
        ...props.fields.map(f => h(f.type === 'select' ? 'select' : 'input', {
          placeholder: f.label,
          value: draft[f.key] ?? f.default ?? '',
          onInput: e => draft[f.key] = e.target.value
        }, f.type === 'select' ? f.options.map(option => h('option', { value: option.value }, option.label)) : undefined)),
        h('button', { onClick: saveDraft }, `新增${props.title}`)
      ]),
      h('div', { class: 'simple-list' }, props.items.map(item => h('div', { class: 'simple-row', key: item.id }, [
        editing.value === item.id
          ? h('div', { class: 'row-editor' }, props.fields.map(f => h(f.type === 'select' ? 'select' : 'input', {
            value: editValue[f.key] ?? '',
            onInput: e => editValue[f.key] = e.target.value
          }, f.type === 'select' ? f.options.map(option => h('option', { value: option.value }, option.label)) : undefined)))
          : h('div', { class: 'row-view' }, props.fields.map(f => h('span', `${f.label}: ${displayValue(f, item[f.key]) || '-'}`))),
        h('div', { class: 'row-actions' }, editing.value === item.id
          ? [
            h('button', { onClick: () => { emit('save', { ...editValue }); editing.value = null } }, '保存'),
            h('button', { onClick: () => editing.value = null }, '取消')
          ]
          : [
            h('button', { onClick: () => startEdit(item) }, '编辑'),
            h('button', { onClick: () => emit('remove', item.id) }, '删除')
          ])
      ])))
    ])
  }
})

const displayValue = (field, value) => field.options?.find(option => option.value === value)?.label || value

const menu = [
  { key: 'dashboard', label: '数据概览', hint: '查看内容、互动与阅读概况。' },
  { key: 'articles', label: '文章管理', hint: '管理文章列表、发布状态与预览。' },
  { key: 'categories', label: '分类管理', hint: '维护文章分类和排序。' },
  { key: 'tags', label: '标签管理', hint: '维护标签云与筛选入口。' },
  { key: 'comments', label: '评论审核', hint: '通过或拒绝文章评论。' },
  { key: 'messages', label: '留言管理', hint: '审核和清理留言板内容。' },
  { key: 'links', label: '友链管理', hint: '新增、编辑、审核友链。' },
  { key: 'config', label: '站点配置', hint: '维护站点标题、备案、关于页等配置。' },
  { key: 'ops', label: '运维备份', hint: '执行备份并检查服务健康状态。' }
]
const tab = ref('dashboard')
const editorMode = ref('rich')
const editorOptions = [{ label: '富文本 HTML', value: 'rich' }, { label: '源码模式', value: 'source' }]
const richRef = ref(null)
const health = ref(null)
const current = computed(() => menu.find(m => m.key === tab.value) || menu[0])
const currentTitle = computed(() => current.value.label)
const currentHint = computed(() => current.value.hint)
const stats = ref({})
const articles = reactive({ total: 0, page: 1, size: 100, records: [] })
const categories = ref([])
const tags = ref([])
const comments = ref([])
const messages = ref([])
const links = ref([])
const configForm = reactive({})
const articleForm = reactive(emptyArticle())
const categoryFields = [{ key: 'name', label: '名称' }, { key: 'slug', label: 'Slug' }, { key: 'description', label: '描述' }, { key: 'sortOrder', label: '排序' }]
const tagFields = [{ key: 'name', label: '名称' }, { key: 'slug', label: 'Slug' }]
const linkFields = [
  { key: 'name', label: '名称' },
  { key: 'url', label: '链接' },
  { key: 'logo', label: 'Logo' },
  { key: 'description', label: '描述' },
  { key: 'status', label: '状态', type: 'select', default: 'APPROVED', options: statusOptions() }
]
const renderedPreview = computed(() => normalizeContent(articleForm.content))
const pendingComments = computed(() => comments.value.filter(item => item.status === 'PENDING'))
const pendingMessages = computed(() => messages.value.filter(item => item.status === 'PENDING'))
const pendingLinks = computed(() => links.value.filter(item => item.status === 'PENDING'))

watch(editorMode, async value => {
  await nextTick()
  if (value === 'rich' && richRef.value) richRef.value.innerHTML = articleForm.content || ''
})

function emptyArticle() {
  return { id: null, title: '', slug: '', summary: '', content: '', coverUrl: '', categoryId: null, tagIds: [], status: 'DRAFT', featured: 0, seoTitle: '', seoKeywords: '', seoDescription: '' }
}

function statusOptions() {
  return [
    { label: '待审核', value: 'PENDING' },
    { label: '已通过', value: 'APPROVED' },
    { label: '已拒绝', value: 'REJECTED' }
  ]
}

const statusText = status => ({ PUBLISHED: '已发布', DRAFT: '草稿', PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }[status] || status || '-')
const slugify = value => (value || '').trim().toLowerCase().replace(/['"]/g, '').replace(/[^a-z0-9\u4e00-\u9fa5]+/g, '-').replace(/^-+|-+$/g, '')
const normalizeContent = value => (value || '').replace(/```([\s\S]*?)```/g, (_, code) => `<pre><code>${escapeHtml(code.trim())}</code></pre>`)
const escapeHtml = value => value.replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]))
const switchTab = key => { tab.value = key }
const ensureSlug = () => { if (!articleForm.slug) articleForm.slug = slugify(articleForm.title) }

const load = async () => {
  stats.value = await http.get('/admin/statistics')
  Object.assign(articles, await http.get('/admin/articles', { params: { size: 100 } }))
  categories.value = await http.get('/admin/categories')
  tags.value = await http.get('/admin/tags')
  comments.value = await http.get('/admin/comments')
  messages.value = await http.get('/admin/messages')
  links.value = await http.get('/admin/friend-links')
  Object.assign(configForm, await http.get('/admin/site-config'))
}
const newArticle = async () => {
  Object.assign(articleForm, emptyArticle(), { categoryId: categories.value[0]?.id || null })
  tab.value = 'editor'
  editorMode.value = 'rich'
  await nextTick()
  if (richRef.value) richRef.value.innerHTML = ''
}
const editArticle = async row => {
  Object.assign(articleForm, emptyArticle(), row, { tagIds: (row.tags || []).map(tag => tag.id) })
  tab.value = 'editor'
  editorMode.value = 'rich'
  await nextTick()
  if (richRef.value) richRef.value.innerHTML = articleForm.content || ''
}
const previewArticle = row => window.open(`/article/${row.slug}`, '_blank')
const syncRichContent = () => { articleForm.content = richRef.value?.innerHTML || '' }
const validateArticle = () => {
  ensureSlug()
  if (!articleForm.title || !articleForm.slug || !articleForm.content || !articleForm.categoryId) {
    ElMessage.warning('请填写标题、Slug、正文和分类')
    return false
  }
  return true
}
const saveArticle = async () => {
  if (editorMode.value === 'rich') syncRichContent()
  if (!validateArticle()) return
  articleForm.id ? await http.put(`/admin/articles/${articleForm.id}`, articleForm) : await http.post('/admin/articles', articleForm)
  ElMessage.success('文章已保存')
  await load()
  tab.value = 'articles'
}
const setDraftAndSave = () => { articleForm.status = 'DRAFT'; saveArticle() }
const publishArticle = () => { articleForm.status = 'PUBLISHED'; saveArticle() }
const removeArticle = async id => {
  await ElMessageBox.confirm('确认删除这篇文章？')
  await http.delete(`/admin/articles/${id}`)
  await load()
}
const uploadCover = async ({ file }) => {
  const data = new FormData()
  data.append('file', file)
  articleForm.coverUrl = await http.post('/admin/upload', data)
}
const saveCategory = async item => { item.id ? await http.put(`/admin/categories/${item.id}`, item) : await http.post('/admin/categories', item); await load() }
const removeCategory = async id => { await http.delete(`/admin/categories/${id}`); await load() }
const saveTag = async item => { item.id ? await http.put(`/admin/tags/${item.id}`, item) : await http.post('/admin/tags', item); await load() }
const removeTag = async id => { await http.delete(`/admin/tags/${id}`); await load() }
const setComment = async (id, status) => { await http.put(`/admin/comments/${id}/status`, null, { params: { status } }); await load() }
const setMessage = async (id, status) => { await http.put(`/admin/messages/${id}/status`, null, { params: { status } }); await load() }
const deleteMessage = async id => { await http.delete(`/admin/messages/${id}`); await load() }
const saveLink = async item => { item.status ||= 'APPROVED'; item.id ? await http.put(`/admin/friend-links/${item.id}`, item) : await http.post('/admin/friend-links', item); await load() }
const removeLink = async id => { await http.delete(`/admin/friend-links/${id}`); await load() }
const saveConfig = async () => { await http.put('/admin/site-config', configForm); ElMessage.success('配置已保存') }
const backup = async () => ElMessage.success(`备份完成：${await http.post('/admin/backup')}`)
const checkHealth = async () => {
  health.value = await http.raw.get('/actuator/health').then(res => res.data)
  ElMessage.success('后端健康检查通过')
}
onMounted(load)
</script>
