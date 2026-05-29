<template>
  <section class="article-layout">
    <article class="content">
      <h1>{{ article.title }}</h1>
      <p class="meta">阅读 {{ article.viewCount || 0 }} · {{ formatTime(article.publishedAt) }}</p>
      <div class="article-tags" v-if="article.category || article.tags?.length">
        <el-tag v-if="article.category" type="info">{{ article.category.name }}</el-tag>
        <el-tag v-for="tag in article.tags || []" :key="tag.id" type="success">{{ tag.name }}</el-tag>
      </div>
      <img v-if="article.coverUrl" class="detail-cover" :src="article.coverUrl" alt="" />
      <div ref="bodyRef" class="article-body" v-html="article.content"></div>
      <nav class="prev-next">
        <router-link v-if="navigation.previous" :to="`/article/${navigation.previous.slug}`">上一篇：{{ navigation.previous.title }}</router-link>
        <router-link v-if="navigation.next" :to="`/article/${navigation.next.slug}`">下一篇：{{ navigation.next.title }}</router-link>
      </nav>
      <section class="comments">
        <h2>评论</h2>
        <div v-for="c in comments" :key="c.id" class="comment"><strong>{{ c.authorName }}</strong>：{{ c.content }}</div>
        <el-form :model="form" label-position="top">
          <el-form-item label="昵称"><el-input v-model="form.authorName" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="form.authorEmail" /></el-form-item>
          <el-form-item label="评论"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
          <el-button type="primary" @click="submit">提交审核</el-button>
        </el-form>
      </section>
    </article>
    <aside class="toc" v-if="toc.length">
      <h3>文章目录</h3>
      <a v-for="item in toc" :key="item.id" :href="`#${item.id}`" :class="`toc-${item.level}`">{{ item.text }}</a>
    </aside>
  </section>
</template>
<script setup>
import { nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'
const route = useRoute()
const article = ref({})
const navigation = ref({})
const comments = ref([])
const toc = ref([])
const bodyRef = ref(null)
const form = reactive({ authorName: '', authorEmail: '', content: '' })
const loadComments = async () => comments.value = await http.get('/public/comments', { params: { articleId: article.value.id } })
const slugifyHeading = value => (value || '').trim().toLowerCase().replace(/[^a-z0-9\u4e00-\u9fa5]+/g, '-').replace(/^-+|-+$/g, '')
const buildToc = () => {
  const headings = bodyRef.value?.querySelectorAll('h1,h2,h3,h4') || []
  const used = new Map()
  toc.value = Array.from(headings).map((el, index) => {
    const base = slugifyHeading(el.textContent) || `heading-${index + 1}`
    const count = used.get(base) || 0
    used.set(base, count + 1)
    const id = count ? `${base}-${count + 1}` : base
    el.id = id
    return { id, text: el.textContent, level: el.tagName.toLowerCase() }
  })
}
const formatTime = value => value ? value.replace('T', ' ').slice(0, 16) : ''
const submit = async () => {
  if (!form.authorName || !form.content) {
    ElMessage.warning('请填写昵称和评论内容')
    return
  }
  await http.post('/public/comments', { ...form, articleId: article.value.id })
  ElMessage.success('评论已提交，审核后展示')
  form.content = ''
}
const load = async () => {
  article.value = await http.get(`/public/articles/${route.params.slug}`)
  navigation.value = await http.get(`/public/articles/${route.params.slug}/prev-next`)
  await loadComments()
  await nextTick()
  buildToc()
}
watch(() => route.params.slug, load)
onMounted(load)
</script>
