<template><section class="content"><h1>关于我</h1><p>{{ about }}</p></section></template>
<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'
const about = ref('')
onMounted(async () => about.value = (await http.get('/public/site-config')).about)
</script>
