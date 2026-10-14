import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 统一的 axios 实例。
 * baseURL 使用相对路径 /api：
 *  - 开发环境由 Vite 代理到 http://localhost:8080
 *  - 生产环境与后端同源
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动附加 JWT
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理业务码与 HTTP 错误
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端统一响应：{ code, message, data }
    if (res && typeof res.code !== 'undefined' && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    const data = error.response?.data
    let message = data?.message

    if (status === 401) {
      message = message || '登录已过期，请重新登录'
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.replace('/login')
      }
    } else if (status === 403) {
      message = message || '没有权限执行该操作'
    } else if (status === 404) {
      message = message || '请求的资源不存在'
    } else if (status >= 500) {
      message = message || '服务器内部错误'
    } else if (!message) {
      message = error.message || '网络异常，请稍后重试'
    }

    // 参数校验失败时后端会返回字段级错误
    if (data?.data && typeof data.data === 'object') {
      const first = Object.values(data.data)[0]
      if (first) message = `${message}：${first}`
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
