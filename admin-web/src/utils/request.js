import axios from 'axios'
import { message } from 'ant-design-vue'
import router from '@/router'

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
      return data.data
    } else if (data.code === 401) {
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_username')
      router.push('/login')
      return Promise.reject(new Error('未登录'))
    } else {
      message.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
  },
  error => {
    message.error('网络错误')
    return Promise.reject(error)
  }
)

export default request
