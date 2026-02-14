import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getProfile, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const userInfo = ref(null)
  
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || localStorage.getItem('admin_username') || '管理员')
  const role = computed(() => userInfo.value?.role || 'admin')

  const login = async (credentials) => {
    const result = await loginApi(credentials)
    token.value = result.token
    localStorage.setItem('admin_token', result.token)
    localStorage.setItem('admin_username', credentials.username)
    return result
  }

  const fetchProfile = async () => {
    if (!token.value) return null
    try {
      const result = await getProfile()
      userInfo.value = result
      return result
    } catch (e) {
      token.value = ''
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_username')
      throw e
    }
  }

  const logout = async () => {
    try {
      await logoutApi()
    } catch (e) {
      console.error('Logout API failed:', e)
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_username')
    }
  }

  const clearAuth = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_username')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    role,
    login,
    fetchProfile,
    logout,
    clearAuth
  }
})
