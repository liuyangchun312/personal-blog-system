<template>
  <section class="admin">
    <el-tabs v-model="tab">
      <el-tab-pane label="数据统计" name="stats">
        <div class="stats">
          <el-statistic title="文章数" :value="stats.articleCount" />
          <el-statistic title="已发布" :value="stats.publishedArticleCount" />
          <el-statistic title="待审评论" :value="stats.pendingCommentCount" />
          <el-statistic title="总阅读量" :value="stats.totalViews" />
        </div>
      </el-tab-pane>
      <el-tab-pane label="文章管理" name="articles">
        <el-button type="primary" @click="saveArticle">发布示例文章</el-button>
        <el-table :data="articles.records"><el-table-column prop="title" label="标题" /><el-table-column prop="status" label="状态" /><el-table-column prop="viewCount" label="阅读" /></el-table>
      </el-tab-pane>
      <el-tab-pane label="评论审核" name="comments"><el-table :data="comments"><el-table-column prop="authorName" label="用户" /><el-table-column prop="content" label="内容" /><el-table-column prop="status" label="状态" /></el-table></el-tab-pane>
      <el-tab-pane label="系统配置" name="config"><el-descriptions border :column="1"><el-descriptions-item v-for="(v,k) in config" :key="k" :label="k">{{ v }}</el-descriptions-item></el-descriptions><el-button @click="backup">数据库备份</el-button></el-tab-pane>
    </el-tabs>
  </section>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'
const tab = ref('stats')
const articles = reactive({ records: [] })
const comments = ref([])
const config = ref({})
const stats = ref({})
const load = async () => { Object.assign(articles, await http.get('/admin/articles')); comments.value = await http.get('/admin/comments'); config.value = await http.get('/admin/site-config'); stats.value = await http.get('/admin/statistics') }
const saveArticle = async () => { await http.post('/admin/articles', { title: `新文章 ${Date.now()}`, slug: `post-${Date.now()}`, summary: '后台发布的文章', content: '<p>正文内容</p>', categoryId: 1, tagIds: [1], status: 'PUBLISHED', featured: 0 }); await load() }
const backup = async () => ElMessage.success(await http.post('/admin/backup'))
onMounted(load)
</script>
