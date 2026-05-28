<template>
  <section class="layout">
    <div>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索文章标题、摘要或正文" clearable @keyup.enter="load" />
        <el-button type="primary" @click="load">搜索</el-button>
      </div>
      <article v-for="item in page.records" :key="item.id" class="article-card">
        <img v-if="item.coverUrl" :src="item.coverUrl" alt="" />
        <div>
          <router-link class="article-title" :to="`/article/${item.slug}`">{{ item.title }}</router-link>
          <p>{{ item.summary }}</p>
          <span>{{ item.publishedAt?.replace('T', ' ') }} · 阅读 {{ item.viewCount }}</span>
        </div>
      </article>
      <el-empty v-if="!page.records.length" description="暂无文章" />
      <el-pagination layout="prev, pager, next" :total="page.total" :page-size="page.size" @current-change="p => { page.page = p; load() }" />
    </div>
    <aside>
      <h3>分类导航</h3>
      <el-tag class="tag" @click="clearFilter">全部</el-tag>
      <el-tag v-for="c in categories" :key="c.id" class="tag" @click="filterCategory(c.id)">{{ c.name }}</el-tag>
      <h3>标签云</h3>
      <el-tag v-for="t in tags" :key="t.id" class="tag tag-cloud" type="success" @click="filterTag(t.id)">{{ t.name }}</el-tag>
      <h3>热门文章</h3>
      <router-link v-for="a in hot" :key="a.id" :to="`/article/${a.slug}`" class="side-link">{{ a.title }}</router-link>
      <h3>最新文章</h3>
      <router-link v-for="a in latest" :key="a.id" :to="`/article/${a.slug}`" class="side-link">{{ a.title }}</router-link>
    </aside>
  </section>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '../api/http'
const keyword = ref('')
const categoryId = ref(null)
const tagId = ref(null)
const page = reactive({ total: 0, page: 1, size: 10, records: [] })
const categories = ref([])
const tags = ref([])
const hot = ref([])
const latest = ref([])
const load = async () => Object.assign(page, await http.get('/public/articles', { params: { page: page.page, size: page.size, keyword: keyword.value, categoryId: categoryId.value, tagId: tagId.value } }))
const filterCategory = id => { categoryId.value = id; tagId.value = null; page.page = 1; load() }
const filterTag = id => { tagId.value = id; categoryId.value = null; page.page = 1; load() }
const clearFilter = () => { categoryId.value = null; tagId.value = null; keyword.value = ''; page.page = 1; load() }
onMounted(async () => {
  await load()
  categories.value = await http.get('/public/categories')
  tags.value = await http.get('/public/tags')
  hot.value = await http.get('/public/articles/hot')
  latest.value = await http.get('/public/articles/latest')
})
</script>
