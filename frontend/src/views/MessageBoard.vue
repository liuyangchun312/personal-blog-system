<template>
  <section class="content"><h1>留言板</h1><el-form :model="form" label-position="top"><el-form-item label="昵称"><el-input v-model="form.authorName" /></el-form-item><el-form-item label="邮箱"><el-input v-model="form.authorEmail" /></el-form-item><el-form-item label="留言"><el-input v-model="form.content" type="textarea" /></el-form-item><el-button type="primary" @click="submit">提交留言</el-button></el-form></section>
</template>
<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'
const form = reactive({ authorName: '', authorEmail: '', content: '' })
const submit = async () => { await http.post('/public/messages', form); ElMessage.success('留言已提交审核'); form.content = '' }
</script>
