<template>
  <el-container>
    <el-header class="topbar">
      <router-link class="brand" to="/">{{ config.siteTitle || '企业级个人博客' }}</router-link>
      <nav>
        <router-link to="/">首页</router-link>
        <router-link to="/archives">归档</router-link>
        <router-link to="/about">关于我</router-link>
        <router-link to="/links">友链</router-link>
        <router-link to="/messages">留言板</router-link>
        <router-link to="/admin">后台</router-link>
        <el-switch v-model="dark" active-text="暗黑" @change="toggleDark" />
      </nav>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
    <el-footer class="footer">{{ config.icp }}</el-footer>
  </el-container>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from './api/http'
const config = reactive({})
const dark = ref(localStorage.getItem('dark') === '1')
const toggleDark = () => {
  document.documentElement.classList.toggle('dark', dark.value)
  localStorage.setItem('dark', dark.value ? '1' : '0')
}
onMounted(async () => {
  Object.assign(config, await http.get('/public/site-config'))
  toggleDark()
})
</script>
