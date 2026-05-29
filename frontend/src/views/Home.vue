<template>
  <section class="home-shell">
    <section class="hero-panel">
      <div class="hero-copy">
        <span class="hero-kicker">Personal Blog / Digital Garden</span>
        <h1>把灵感、代码和生活切片，收进一个会发光的博客。</h1>
        <p>这里是文章、分类、标签与留言的入口。功能保持原样，只让阅读体验更像一个有态度的个人空间。</p>
      </div>
      <div class="hero-orbit" aria-hidden="true">
        <span class="orbit-card orbit-card-one">BLOG</span>
        <span class="orbit-card orbit-card-two">IDEAS</span>
        <span class="orbit-card orbit-card-three">NOTES</span>
      </div>
    </section>

    <section class="layout">
    <div class="feed-panel">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索文章标题、摘要或正文" clearable @keyup.enter="load" />
        <el-button type="primary" @click="load">搜索</el-button>
      </div>
      <article v-for="(item, index) in page.records" :key="item.id" class="article-card">
        <img v-if="item.coverUrl" :src="item.coverUrl" alt="" />
        <div>
          <span class="article-index">{{ String(index + 1).padStart(2, '0') }}</span>
          <router-link class="article-title" :to="`/article/${item.slug}`">{{ item.title }}</router-link>
          <p>{{ item.summary }}</p>
          <div class="article-card-tags">
            <el-tag v-if="item.category" size="small" type="info">{{ item.category.name }}</el-tag>
            <el-tag v-for="tag in item.tags || []" :key="tag.id" size="small" type="success">{{ tag.name }}</el-tag>
          </div>
          <span>{{ item.publishedAt?.replace('T', ' ').slice(0, 16) }} · 阅读 {{ item.viewCount || 0 }}</span>
        </div>
      </article>
      <el-empty v-if="!page.records.length" description="暂无文章" />
      <el-pagination layout="prev, pager, next" :total="page.total" :page-size="page.size" @current-change="p => { page.page = p; load() }" />
    </div>
    <aside>
      <h3>分类导航</h3>
      <el-tag class="tag" @click="clearFilter">全部</el-tag>
      <el-tag v-for="c in categories" :key="c.id" class="tag" :type="categoryId === c.id ? 'primary' : 'info'" @click="filterCategory(c.id)">{{ c.name }}</el-tag>
      <h3>标签云</h3>
      <el-tag v-for="t in tags" :key="t.id" class="tag tag-cloud" :type="tagId === t.id ? 'success' : 'info'" @click="filterTag(t.id)">{{ t.name }}</el-tag>
      <h3>热门文章</h3>
      <router-link v-for="a in hot" :key="a.id" :to="`/article/${a.slug}`" class="side-link">{{ a.title }}</router-link>
      <h3>最新文章</h3>
      <router-link v-for="a in latest" :key="a.id" :to="`/article/${a.slug}`" class="side-link">{{ a.title }}</router-link>
    </aside>
    </section>
  </section>
</template>
<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '../api/http'
const route = useRoute()
const router = useRouter()
const keyword = ref('')
const categoryId = ref(null)
const tagId = ref(null)
const page = reactive({ total: 0, page: 1, size: 10, records: [] })
const categories = ref([])
const tags = ref([])
const hot = ref([])
const latest = ref([])
const numberOrNull = value => value ? Number(value) : null
const syncFromQuery = () => {
  categoryId.value = numberOrNull(route.query.categoryId)
  tagId.value = numberOrNull(route.query.tagId)
}
const load = async () => Object.assign(page, await http.get('/public/articles', { params: { page: page.page, size: page.size, keyword: keyword.value, categoryId: categoryId.value, tagId: tagId.value } }))
const pushFilter = query => router.push({ path: '/', query })
const filterCategory = id => { page.page = 1; pushFilter({ categoryId: id }) }
const filterTag = id => { page.page = 1; pushFilter({ tagId: id }) }
const clearFilter = () => { categoryId.value = null; tagId.value = null; keyword.value = ''; page.page = 1; pushFilter({}) }
watch(() => route.query, async () => {
  syncFromQuery()
  await load()
})
onMounted(async () => {
  syncFromQuery()
  await load()
  categories.value = await http.get('/public/categories')
  tags.value = await http.get('/public/tags')
  hot.value = await http.get('/public/articles/hot')
  latest.value = await http.get('/public/articles/latest')
})
</script>
