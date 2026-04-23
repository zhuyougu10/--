import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getProfile, logout as logoutApi } from '@/api/auth'
import { ApiError } from '@/utils/request'
import { isMobileDevice } from '@/utils/device'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const userInfo = ref(null)
  
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.name || userInfo.value?.username || localStorage.getItem('admin_username') || '管理员')
  const role = computed(() => userInfo.value?.role || localStorage.getItem('admin_role') || '')
  const roleText = computed(() => userInfo.value?.roleText || (role.value === 'VENUE_STAFF' ? '场馆管理员' : '管理员'))
  const isAdmin = computed(() => role.value === 'ADMIN')
  const isVenueStaff = computed(() => role.value === 'VENUE_STAFF')

  const login = async (credentials) => {
    const result = await loginApi(credentials)
    token.value = result.data.token
    userInfo.value = result.data
    localStorage.setItem('admin_token', result.data.token)
    localStorage.setItem('admin_username', result.data.name || result.data.username || credentials.username)
    localStorage.setItem('admin_role', result.data.role || '')
    localStorage.setItem('admin_is_mobile', isMobileDevice() ? '1' : '0')
    return result
  }

  const fetchProfile = async () => {
    if (!token.value) return null
    try {
      const result = await getProfile()
      userInfo.value = result.data
      localStorage.setItem('admin_username', result.data.name || result.data.username || '')
      localStorage.setItem('admin_role', result.data.role || '')
      return result.data
    } catch (e) {
      token.value = ''
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_username')
      localStorage.removeItem('admin_role')
      localStorage.removeItem('admin_is_mobile')
      throw e
    }
  }

  const logout = async () => {
    try {
      await logoutApi()
    } catch (e) {
      if (e instanceof ApiError) {
        console.error('Logout API failed:', e.message)
      } else {
        console.error('Logout API failed:', e)
      }
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_username')
      localStorage.removeItem('admin_role')
      localStorage.removeItem('admin_is_mobile')
    }
  }

  const clearAuth = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_username')
    localStorage.removeItem('admin_role')
    localStorage.removeItem('admin_is_mobile')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    username,
    role,
    roleText,
    isAdmin,
    isVenueStaff,
    login,
    fetchProfile,
    logout,
    clearAuth
  }
})
