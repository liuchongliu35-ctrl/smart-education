import { message } from 'antd'
import axios from 'axios'

const http = axios.create({
  baseURL: 'http://localhost:8080/',
})
//10.33.125.86
// 139.199.181.14
// 添加请求拦截器
http.interceptors.request.use((config) => {
  // 从 localStorage 获取 token
  const token = localStorage.getItem('authToken')

  // 如果 token 存在，添加到请求头
  if (token) {
    config.headers.token = token
  }

  return config
}, (error) => {
  return Promise.reject(error)
})

// 添加响应拦截器
http.interceptors.response.use((response) => {
  // 2xx 范围内的状态码都会触发该函数
  return response.data
}, (error) => {
  // 超出 2xx 范围的状态码
  if (error.response) {
    const { status } = error.response

    // 处理 401 未授权错误
    if (status === 401) {
      // 清除无效的 token
      localStorage.removeItem('authToken')

      // 显示错误消息
      message.error('登录已过期，请重新登录')

      // 重定向到登录页（保留当前路径以便登录后跳回）
      const currentPath = window.location.pathname + window.location.search
      setTimeout(() => {
        window.location.href = `/?redirect=${encodeURIComponent(currentPath)}`
      }, 1500)

      // 返回一个特殊对象表示已处理
      return { success: false, code: 401 }
    }

    // 处理 500 服务器错误
    if (status === 500) {
      message.error('服务器内部错误，请稍后重试！')
      setTimeout(() => {
        window.location.reload()
      }, 2000)
      return { success: false }
    }
  }

  // 对于其他错误（如网络错误），仍然抛出错误
  return Promise.reject(error)
})

export { http }