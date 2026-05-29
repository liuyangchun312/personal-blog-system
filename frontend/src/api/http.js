import axios from 'axios'
import { ElMessage } from 'element-plus'

const baseURL = import.meta.env.VITE_API_BASE || '/api'
const http = axios.create({ baseURL, timeout: 8000 })
const raw = axios.create({ baseURL, timeout: 8000 })

const authRequest = config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
}

http.interceptors.request.use(authRequest)
raw.interceptors.request.use(authRequest)

http.interceptors.response.use(res => {
  if (res.data.code !== 0) {
    ElMessage.error(res.data.message || '请求失败')
    return Promise.reject(new Error(res.data.message))
  }
  return res.data.data
}, error => {
  const status = error.response?.status
  if (status === 401 || status === 403) {
    localStorage.removeItem('token')
    ElMessage.error('登录已失效，请重新登录')
    if (window.location.pathname !== '/login') window.location.href = '/login'
  } else {
    ElMessage.error(error.response?.data?.message || '网络异常')
  }
  return Promise.reject(error)
})

http.raw = raw
export default http
