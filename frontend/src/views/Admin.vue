<template>
  <section class="admin-shell">
    <aside class="admin-menu">
      <h2>管理后台</h2>
      <button v-for="item in menu" :key="item.key" :class="{ active: tab === item.key }" @click="tab = item.key">{{ item.label }}</button>
    </aside>

    <main class="admin-panel">
      <header class="admin-header">
        <h1>{{ currentTitle }}</h1>
        <el-button @click="load">刷新</el-button>
      </header>

      <section v-if="tab === 'dashboard'" class="stats">
        <el-statistic title="文章数" :value="stats.articleCount || 0" />
        <el-statistic title="已发布" :value="stats.publishedArticleCount || 0" />
        <el-statistic title="待审评论" :value="stats.pendingCommentCount || 0" />
        <el-statistic title="总阅读量" :value="stats.totalViews || 0" />
      </section>

      <section v-if="tab === 'articles'">
        <div class="admin-actions">
          <el-button type="primary" @click="newArticle">新建文章</el-button>
        </div>
        <el-table :data="articles.records">
          <el-table-column prop="title" label="标题" min-width="220" />
          <el-table-column prop="status" label="状态" width="110" />
          <el-table-column prop="viewCount" label="阅读" width="90" />
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button size="small" @click="editArticle(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="removeArticle(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'editor'" class="editor-grid">
        <el-form :model="articleForm" label-position="top">
          <el-form-item label="标题"><el-input v-model="articleForm.title" /></el-form-item>
          <el-form-item label="Slug"><el-input v-model="articleForm.slug" /></el-form-item>
          <el-form-item label="摘要"><el-input v-model="articleForm.summary" type="textarea" /></el-form-item>
          <el-form-item label="封面图">
            <el-upload :http-request="uploadCover" :show-file-list="false">
              <el-button>上传封面</el-button>
            </el-upload>
            <img v-if="articleForm.coverUrl" class="cover-preview" :src="articleForm.coverUrl" alt="" />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="articleForm.categoryId">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="标签">
            <el-select v-model="articleForm.tagIds" multiple>
              <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="正文 HTML / Markdown">
            <el-input v-model="articleForm.content" type="textarea" :rows="14" />
          </el-form-item>
          <el-form-item label="SEO 标题"><el-input v-model="articleForm.seoTitle" /></el-form-item>
          <el-form-item label="SEO 关键词"><el-input v-model="articleForm.seoKeywords" /></el-form-item>
          <el-form-item label="SEO 描述"><el-input v-model="articleForm.seoDescription" type="textarea" /></el-form-item>
          <div class="admin-actions">
            <el-button @click="articleForm.status = 'DRAFT'; saveArticle()">保存草稿</el-button>
            <el-button type="primary" @click="articleForm.status = 'PUBLISHED'; saveArticle()">发布</el-button>
          </div>
        </el-form>
        <article class="preview-pane">
          <h2>{{ articleForm.title || '文章预览' }}</h2>
          <p>{{ articleForm.summary }}</p>
          <div class="article-body" v-html="renderedPreview"></div>
        </article>
      </section>

      <section v-if="tab === 'categories'">
        <inline-editor title="分类" :items="categories" :fields="categoryFields" @save="saveCategory" @remove="removeCategory" />
      </section>

      <section v-if="tab === 'tags'">
        <inline-editor title="标签" :items="tags" :fields="tagFields" @save="saveTag" @remove="removeTag" />
      </section>

      <section v-if="tab === 'comments'">
        <el-table :data="comments">
          <el-table-column prop="authorName" label="用户" width="140" />
          <el-table-column prop="content" label="内容" />
          <el-table-column prop="status" label="状态" width="110" />
          <el-table-column label="操作" width="220">
            <template #default="{ row }">
              <el-button size="small" @click="setComment(row.id, 'APPROVED')">通过</el-button>
              <el-button size="small" type="warning" @click="setComment(row.id, 'REJECTED')">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'messages'">
        <el-table :data="messages">
          <el-table-column prop="authorName" label="用户" width="140" />
          <el-table-column prop="content" label="留言" />
          <el-table-column prop="status" label="状态" width="110" />
          <el-table-column label="操作" width="220">
            <template #default="{ row }">
              <el-button size="small" @click="setMessage(row.id, 'APPROVED')">通过</el-button>
              <el-button size="small" type="danger" @click="deleteMessage(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section v-if="tab === 'links'">
        <inline-editor title="友链" :items="links" :fields="linkFields" @save="saveLink" @remove="removeLink" />
      </section>

      <section v-if="tab === 'config'">
        <el-form :model="configForm" label-position="top">
          <el-form-item label="网站标题"><el-input v-model="configForm.siteTitle" /></el-form-item>
          <el-form-item label="Logo"><el-input v-model="configForm.logo" /></el-form-item>
          <el-form-item label="ICP备案"><el-input v-model="configForm.icp" /></el-form-item>
          <el-form-item label="关于我"><el-input v-model="configForm.about" type="textarea" :rows="6" /></el-form-item>
          <el-button type="primary" @click="saveConfig">保存配置</el-button>
        </el-form>
      </section>

      <section v-if="tab === 'ops'">
        <el-alert title="本地备份会调用容器内 mysqldump，并输出到 backups 目录。" type="info" :closable="false" />
        <el-button class="mt" type="primary" @click="backup">立即备份数据库</el-button>
      </section>
    </main>
  </section>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api/http'

const InlineEditor = defineComponent({
  props: { title: String, items: Array, fields: Array },
  emits: ['save', 'remove'],
  setup(props, { emit }) {
    const draft = reactive({})
    return () => h('div', [
      h('div', { class: 'inline-form' }, [
        ...props.fields.map(f => h('input', { placeholder: f.label, value: draft[f.key] || '', onInput: e => draft[f.key] = e.target.value })),
        h('button', { onClick: () => { emit('save', { ...draft }); props.fields.forEach(f => draft[f.key] = '') } }, `新增${props.title}`)
      ]),
      h('div', { class: 'simple-list' }, props.items.map(item => h('div', { class: 'simple-row' }, [
        ...props.fields.map(f => h('span', `${f.label}: ${item[f.key] || ''}`)),
        h('button', { onClick: () => emit('save', item) }, '保存'),
        h('button', { onClick: () => emit('remove', item.id) }, '删除')
      ])))
    ])
  }
})

const menu = [
  { key: 'dashboard', label: '数据概览' },
  { key: 'articles', label: '文章管理' },
  { key: 'editor', label: '文章编辑' },
  { key: 'categories', label: '分类管理' },
  { key: 'tags', label: '标签管理' },
  { key: 'comments', label: '评论审核' },
  { key: 'messages', label: '留言管理' },
  { key: 'links', label: '友链管理' },
  { key: 'config', label: '站点配置' },
  { key: 'ops', label: '运维备份' }
]
const tab = ref('dashboard')
const currentTitle = computed(() => menu.find(m => m.key === tab.value)?.label || '管理后台')
const stats = ref({})
const articles = reactive({ records: [] })
const categories = ref([])
const tags = ref([])
const comments = ref([])
const messages = ref([])
const links = ref([])
const configForm = reactive({})
const articleForm = reactive({ id: null, title: '', slug: '', summary: '', content: '', coverUrl: '', categoryId: null, tagIds: [], status: 'DRAFT', featured: 0, seoTitle: '', seoKeywords: '', seoDescription: '' })
const categoryFields = [{ key: 'name', label: '名称' }, { key: 'slug', label: 'Slug' }, { key: 'description', label: '描述' }]
const tagFields = [{ key: 'name', label: '名称' }, { key: 'slug', label: 'Slug' }]
const linkFields = [{ key: 'name', label: '名称' }, { key: 'url', label: '链接' }, { key: 'description', label: '描述' }, { key: 'status', label: '状态' }]
const renderedPreview = computed(() => articleForm.content)

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
const newArticle = () => { Object.assign(articleForm, { id: null, title: '', slug: '', summary: '', content: '', coverUrl: '', categoryId: categories.value[0]?.id, tagIds: [], status: 'DRAFT', featured: 0 }); tab.value = 'editor' }
const editArticle = row => { Object.assign(articleForm, { ...row, tagIds: [] }); tab.value = 'editor' }
const saveArticle = async () => { articleForm.id ? await http.put(`/admin/articles/${articleForm.id}`, articleForm) : await http.post('/admin/articles', articleForm); ElMessage.success('文章已保存'); await load(); tab.value = 'articles' }
const removeArticle = async id => { await ElMessageBox.confirm('确认删除这篇文章？'); await http.delete(`/admin/articles/${id}`); await load() }
const uploadCover = async ({ file }) => { const data = new FormData(); data.append('file', file); articleForm.coverUrl = await http.post('/admin/upload', data) }
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
const backup = async () => ElMessage.success(await http.post('/admin/backup'))
onMounted(load)
</script>
