<template>
  <view class="container">
    <view class="user-card" v-if="isLoggedIn">
      <view class="user-info">
        <image class="avatar" :src="userInfo.avatar || '/static/default-avatar.svg'" mode="aspectFill" />
        <view class="user-detail">
          <text class="nickname">{{ userInfo.nickname || '用户' }}</text>
          <text class="user-type">{{ getUserTypeName(userInfo.userType) }}</text>
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
      
      <view class="menu-item" @click="showRules">
        <view class="menu-icon">
          <text class="iconfont">📖</text>
        </view>
        <text class="menu-text">预约须知</text>
        <text class="menu-arrow">></text>
      </view>
    </view>
    
    <view class="logout-section" v-if="isLoggedIn">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script>
import { wechatLogin, getUserProfile } from '@/api/auth'
import { getMyBookings } from '@/api/booking'

export default {
  data() {
    return {
      isLoggedIn: false,
      userInfo: {},
      pendingCount: 0
    }
  },
  onShow() {
    this.checkLogin()
    if (this.isLoggedIn) {
      this.loadPendingCount()
    }
  },
  methods: {
    checkLogin() {
      const token = uni.getStorageSync('token')
      const userInfo = uni.getStorageSync('userInfo')
      this.isLoggedIn = !!token
      this.userInfo = userInfo || {}
    },
    
    async handleLogin() {
      try {
        const loginRes = await new Promise((resolve, reject) => {
          uni.login({
            provider: 'weixin',
            success: resolve,
            fail: reject
          })
        })
        
        const result = await wechatLogin(loginRes.code)
        
        uni.setStorageSync('token', result.token)
        uni.setStorageSync('userInfo', result.user)
        
        this.isLoggedIn = true
        this.userInfo = result.user
        
        uni.showToast({ title: '登录成功', icon: 'success' })
        this.loadPendingCount()
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '登录失败', icon: 'none' })
      }
    },
    
    async loadPendingCount() {
      try {
        const bookings = await getMyBookings(1)
        this.pendingCount = bookings.length || 0
      } catch (e) {
        console.error(e)
      }
    },
    
    getUserTypeName(type) {
      const types = {
        student: '学生',
        teacher: '教师',
        staff: '教职工'
      }
      return types[type] || '用户'
    },
    
    goToBookings(status) {
      if (!this.isLoggedIn) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      uni.navigateTo({
        url: `/pages/my-bookings/my-bookings?status=${status}`
      })
    },
    
    showAbout() {
      uni.showModal({
        title: '关于我们',
        content: '校园球馆智能预约系统\n版本: 1.0.0',
        showCancel: false
      })
    },
    
    showRules() {
      uni.showModal({
        title: '预约须知',
        content: '1. 每人每天最多预约2个时段\n2. 预约开始前30分钟不可取消\n3. 爽约3次将被限制预约7天\n4. 请在预约时段开始前15分钟内核销签到',
        showCancel: false
      })
    },
    
    handleLogout() {
      uni.showModal({
        title: '确认退出',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            this.isLoggedIn = false
            this.userInfo = {}
            this.pendingCount = 0
            uni.showToast({ title: '已退出', icon: 'success' })
          }
        }
      })
    }
  }
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
