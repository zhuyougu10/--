import { ref, computed } from 'vue'
import { wechatLogin } from '@/api/auth'
import type { User, LoginResult } from '@/types'

export function useAuth() {
  const token = ref(uni.getStorageSync('token') || '')
  const userInfo = ref<User | null>(uni.getStorageSync('userInfo') || null)

  const isLoggedIn = computed(() => !!token.value)

  const checkLogin = () => {
    token.value = uni.getStorageSync('token') || ''
    userInfo.value = uni.getStorageSync('userInfo') || null
  }

  const login = async (): Promise<boolean> => {
    try {
      const loginRes = await new Promise<UniApp.LoginRes>((resolve, reject) => {
        uni.login({
          provider: 'weixin',
          success: resolve,
          fail: reject
        })
      })

      const result: LoginResult = await wechatLogin(loginRes.code)
      
      token.value = result.token
      userInfo.value = result.user
      
      uni.setStorageSync('token', result.token)
      uni.setStorageSync('userInfo', result.user)
      
      uni.showToast({ title: '登录成功', icon: 'success' })
      return true
    } catch (e) {
      console.error(e)
      uni.showToast({ title: '登录失败', icon: 'none' })
      return false
    }
  }

  const logout = () => {
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    token.value = ''
    userInfo.value = null
    uni.showToast({ title: '已退出', icon: 'success' })
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    checkLogin,
    login,
    logout
  }
}

export function useUserType() {
  const userTypeMap: Record<string, string> = {
    student: '学生',
    teacher: '教师',
    staff: '教职工'
  }

  const getUserTypeName = (type: string): string => {
    return userTypeMap[type] || '用户'
  }

  return {
    getUserTypeName
  }
}
