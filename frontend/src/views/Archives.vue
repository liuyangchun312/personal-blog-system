<template>
  <section class="content">
    <h1>文章归档</h1>
    <div v-for="group in archives" :key="group.month" class="archive-group">
      <h2>{{ group.month }}</h2>
      <router-link v-for="article in group.articles" :key="article.id" :to="`/article/${article.slug}`" class="archive-link">
        <span>{{ article.title }}</span>
        <time>{{ article.publishedAt?.slice(0, 10) }}</time>
      </router-link>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'
const archives = ref([])
onMounted(async () => archives.value = await http.get('/public/archives'))
</script>
