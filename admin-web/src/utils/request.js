import axios from 'axios'
import router from '@/router'

export class ApiError extends Error {
  constructor(code, message, data = null) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.data = data
  }
}

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const { data } = response
    if (data.code === 200) {
      return {
        data: data.data,
        code: data.code,
        message: data.message
      }
    } else if (data.code === 401) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_username')
      router.push('/login')
      return Promise.reject(new ApiError(data.code, data.message || '未登录'))
    } else {
      return Promise.reject(new ApiError(data.code, data.message || '请求失败', data.data))
    }
  },
  error => {
    const code = error.response?.status || 500
    const message = error.response?.data?.message || '网络错误'
    return Promise.reject(new ApiError(code, message))
  }
)

export default request
