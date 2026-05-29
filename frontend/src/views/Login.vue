<template>
  <section class="login">
    <el-card>
      <h1>管理员登录</h1>
      <el-input v-model="form.username" placeholder="用户名" />
      <el-input v-model="form.password" type="password" placeholder="密码" show-password @keyup.enter="login" />
      <el-button type="primary" :loading="loading" @click="login">登录</el-button>
    </el-card>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'Admin@123456' })

const login = async () => {
  loading.value = true
  try {
    const data = await http.post('/auth/login', form)
    localStorage.setItem('token', data.token)
    router.push('/admin')
  } finally {
    loading.value = false
  }
}
</script>
