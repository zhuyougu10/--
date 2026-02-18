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
      userInfo.value = {
        id: result.userId,
        name: result.name,
        userType: result.userType,
        userTypeText: result.userTypeText,
        isBound: result.isBound ? 1 : 0
      }
      
      uni.setStorageSync('token', result.token)
      uni.setStorageSync('userInfo', userInfo.value)
      
      if (result.needBind) {
        uni.navigateTo({ url: '/pages/bind/bind' })
        return true
      }
      
      if (result.isNewUser) {
        uni.showToast({ title: '欢迎新用户', icon: 'success' })
      } else {
        uni.showToast({ title: '登录成功', icon: 'success' })
      }
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
  const userTypeMap: Record<number, string> = {
    1: '学生',
    2: '教师',
    3: '外部人员'
  }

  const getUserTypeName = (type: number | undefined): string => {
    if (type === undefined || type === null) return '未绑定'
    return userTypeMap[type] || '用户'
  }

  const getUserTypeCode = (type: string): number => {
    const codeMap: Record<string, number> = {
      'STUDENT': 1,
      'TEACHER': 2,
      'EXTERNAL': 3
    }
    return codeMap[type] ?? 1
  }

  return {
    getUserTypeName,
    getUserTypeCode
  }
}
