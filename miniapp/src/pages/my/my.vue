<template>
  <view class="container">
    <view class="user-card" v-if="isLoggedIn">
      <view class="user-info">
        <image class="avatar" :src="userInfo?.avatar || '/static/default-avatar.svg'" mode="aspectFill" />
        <view class="user-detail">
          <text class="nickname">{{ userInfo?.name || '用户' }}</text>
          <text class="user-type" v-if="userInfo?.studentNo">{{ userInfo.studentNo }}</text>
          <text class="user-type">{{ getUserTypeName(userInfo?.userType) }}</text>
        </view>
      </view>
    </view>
    
    <view class="login-card" v-else>
      <button class="login-btn" @click="handleLogin">
        <text class="login-text">微信登录</text>
      </button>
      <text class="login-tip">登录后可使用预约功能</text>
    </view>
    
    <view class="menu-section">
      <view class="menu-item" @click="goToBookings('all')">
        <view class="menu-icon">
          <text class="iconfont">📋</text>
        </view>
        <text class="menu-text">我的预约</text>
        <text class="menu-arrow">></text>
      </view>
      
      <view class="menu-item" @click="goToBookings(1)">
        <view class="menu-icon">
          <text class="iconfont">✅</text>
        </view>
        <text class="menu-text">待使用</text>
        <text class="menu-badge" v-if="pendingCount > 0">{{ pendingCount }}</text>
        <text class="menu-arrow">></text>
      </view>
      
      <view class="menu-item" @click="goToBookings(3)">
        <view class="menu-icon">
          <text class="iconfont">✓</text>
        </view>
        <text class="menu-text">已完成</text>
        <text class="menu-arrow">></text>
      </view>
    </view>
    
    <view class="menu-section">
      <view class="menu-item" @click="showAbout">
        <view class="menu-icon">
          <text class="iconfont">ℹ️</text>
        </view>
        <text class="menu-text">关于我们</text>
        <text class="menu-arrow">></text>
      </view>
    </view>
    
    <view class="logout-section" v-if="isLoggedIn">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuth, useUserType } from '@/composables/useAuth'
import { useBooking } from '@/composables/useBooking'
import { getUserProfile } from '@/api/auth'

const { isLoggedIn, userInfo, checkLogin, login, logout } = useAuth()
const { getUserTypeName } = useUserType()
const { loadBookings, bookings } = useBooking()

const pendingCount = ref(0)

onShow(() => {
  checkLogin()
  if (isLoggedIn.value) {
    loadUserProfile()
    loadPendingCount()
  }
})

const loadUserProfile = async () => {
  try {
    const result = await getUserProfile()
    if (result.data) {
      userInfo.value = result.data
      uni.setStorageSync('userInfo', result.data)
    }
  } catch (e) {
    console.error('加载用户信息失败', e)
  }
}

const loadPendingCount = async () => {
  await loadBookings()
  const list = bookings.value || []
  pendingCount.value = list.filter(item => item.status === 1 && !item.isExpired).length
}

const handleLogin = async () => {
  const success = await login()
  if (success) {
    loadUserProfile()
    loadPendingCount()
  }
}

const goToBookings = (status: 'all' | number) => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: `/pages/my-bookings/my-bookings?status=${status}`
  })
}

const showAbout = () => {
  uni.showModal({
    title: '关于我们',
    content: '校园球馆智能预约系统\n版本: 1.0.0',
    showCancel: false
  })
}

const handleLogout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        logout()
        pendingCount.value = 0
      }
    }
  })
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
}

.user-card {
  background: linear-gradient(135deg, #1890ff, #36cfc9);
  padding: 60rpx 30rpx;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
}

.user-detail {
  margin-left: 30rpx;
}

.nickname {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  display: block;
}

.user-type {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 8rpx;
  display: block;
}

.login-card {
  background: linear-gradient(135deg, #1890ff, #36cfc9);
  padding: 80rpx 30rpx;
  text-align: center;
}

.login-btn {
  background: #fff;
  color: #1890ff;
  font-size: 32rpx;
  padding: 24rpx 80rpx;
  border-radius: 50rpx;
  border: none;
}

.login-text {
  font-weight: bold;
}

.login-tip {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 20rpx;
  display: block;
}

.menu-section {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  width: 48rpx;
  text-align: center;
}

.iconfont {
  font-size: 36rpx;
}

.menu-text {
  flex: 1;
  font-size: 30rpx;
  margin-left: 20rpx;
}

.menu-badge {
  background: #ff4d4f;
  color: #fff;
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  margin-right: 16rpx;
}

.menu-arrow {
  color: #ccc;
  font-size: 28rpx;
}

.logout-section {
  padding: 60rpx 30rpx;
}

.logout-btn {
  background: #fff;
  color: #ff4d4f;
  font-size: 30rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  border: 2rpx solid #ff4d4f;
}
</style>
