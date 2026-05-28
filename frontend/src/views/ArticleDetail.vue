<template>
  <section class="article-layout">
    <article class="content">
      <h1>{{ article.title }}</h1>
      <p class="meta">阅读 {{ article.viewCount }} · {{ article.publishedAt?.replace('T', ' ') }}</p>
      <img v-if="article.coverUrl" class="detail-cover" :src="article.coverUrl" alt="" />
      <div ref="bodyRef" class="article-body" v-html="article.content"></div>
      <nav class="prev-next">
        <router-link v-if="navigation.previous" :to="`/article/${navigation.previous.slug}`">上一篇：{{ navigation.previous.title }}</router-link>
        <router-link v-if="navigation.next" :to="`/article/${navigation.next.slug}`">下一篇：{{ navigation.next.title }}</router-link>
      </nav>
      <section class="comments">
        <h2>评论</h2>
        <div v-for="c in comments" :key="c.id" class="comment">{{ c.authorName }}：{{ c.content }}</div>
        <el-form :model="form" label-position="top">
          <el-form-item label="昵称"><el-input v-model="form.authorName" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="form.authorEmail" /></el-form-item>
          <el-form-item label="评论"><el-input v-model="form.content" type="textarea" /></el-form-item>
          <el-button type="primary" @click="submit">提交审核</el-button>
        </el-form>
      </section>
    </article>
    <aside class="toc" v-if="toc.length">
      <h3>目录</h3>
      <a v-for="item in toc" :key="item.id" :href="`#${item.id}`" :class="`toc-${item.level}`">{{ item.text }}</a>
    </aside>
  </section>
</template>
<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
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
const buildToc = () => {
  const headings = bodyRef.value?.querySelectorAll('h2,h3') || []
  toc.value = Array.from(headings).map((el, index) => {
    const id = `heading-${index + 1}`
    el.id = id
    return { id, text: el.textContent, level: el.tagName.toLowerCase() }
  })
}
const submit = async () => {
  await http.post('/public/comments', { ...form, articleId: article.value.id })
  ElMessage.success('评论已提交，审核后展示')
  form.content = ''
}
onMounted(async () => {
  article.value = await http.get(`/public/articles/${route.params.slug}`)
  navigation.value = await http.get(`/public/articles/${route.params.slug}/prev-next`)
  await loadComments()
  await nextTick()
  buildToc()
})
</script>
