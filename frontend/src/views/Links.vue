<template>
  <section class="content"><h1>友链</h1><el-card v-for="l in links" :key="l.id" class="link-card"><a :href="l.url" target="_blank">{{ l.name }}</a><p>{{ l.description }}</p></el-card></section>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'
const links = ref([])
onMounted(async () => links.value = await http.get('/public/friend-links'))
</script>
