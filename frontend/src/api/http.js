import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({ baseURL: import.meta.env.VITE_API_BASE || '/api', timeout: 8000 })

http.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(res => {
  if (res.data.code !== 0) {
    ElMessage.error(res.data.message || '请求失败')
    return Promise.reject(new Error(res.data.message))
  }
  return res.data.data
}, error => {
  ElMessage.error(error.response?.data?.message || '网络异常')
  return Promise.reject(error)
})

export default http
