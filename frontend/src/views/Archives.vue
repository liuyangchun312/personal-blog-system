<template>
  <section class="archive-layout">
    <div class="content">
      <h1>文章归档</h1>
      <p class="meta">按发布时间沉淀内容，方便回看每个月写了什么。</p>
      <div v-for="group in archives" :key="group.month" class="archive-group">
        <h2>{{ group.month }}</h2>
        <router-link v-for="article in group.articles" :key="article.id" :to="`/article/${article.slug}`" class="archive-link">
          <span>{{ article.title }}</span>
          <time>{{ article.publishedAt?.slice(0, 10) }}</time>
        </router-link>
      </div>
    </div>
    <aside>
      <h3>标签云</h3>
      <router-link v-for="tag in tags" :key="tag.id" class="cloud-link" :style="{ fontSize: `${tagSize(tag)}px` }" :to="{ path: '/', query: { tagId: tag.id } }">
        {{ tag.name }}
      </router-link>
      <h3>分类</h3>
      <router-link v-for="category in categories" :key="category.id" class="side-link" :to="{ path: '/', query: { categoryId: category.id } }">
        {{ category.name }}
      </router-link>
    </aside>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'
const archives = ref([])
const tags = ref([])
const categories = ref([])
const tagSize = tag => 14 + Math.min(8, Number(tag.id || 0) % 8)
onMounted(async () => {
  archives.value = await http.get('/public/archives')
  tags.value = await http.get('/public/tags')
  categories.value = await http.get('/public/categories')
})
</script>
