<template>
  <view class="container">
    <view class="header">
      <text class="title">绑定工号/学号</text>
      <text class="subtitle">首次登录需要绑定工号/学号</text>
    </view>

    <view class="form-section">
      <view class="form-item">
        <text class="label">工号/学号</text>
        <input 
          class="input" 
          v-model="form.studentNo" 
          placeholder="请输入工号/学号"
          maxlength="50"
        />
      </view>

      <view class="form-item">
        <text class="label">姓名</text>
        <input 
          class="input" 
          v-model="form.name" 
          placeholder="请输入姓名用于验证"
          maxlength="50"
        />
      </view>

      <button class="submit-btn" :disabled="loading" @click="handleSubmit">
        {{ loading ? '绑定中...' : '确认绑定' }}
      </button>
    </view>

    <view class="tips">
      <text class="tips-title">温馨提示</text>
      <text class="tips-item">1. 工号/学号需要与系统预置信息匹配</text>
      <text class="tips-item">2. 姓名需要与工号/学号对应</text>
      <text class="tips-item">3. 如有问题请联系管理员</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { bindStudentNo } from '@/api/auth'

const form = ref({
  studentNo: '',
  name: ''
})

const loading = ref(false)

const handleSubmit = async () => {
  if (!form.value.studentNo.trim()) {
    uni.showToast({ title: '请输入工号/学号', icon: 'none' })
    return
  }
  if (!form.value.name.trim()) {
    uni.showToast({ title: '请输入姓名', icon: 'none' })
    return
  }

  loading.value = true
  try {
    await bindStudentNo({
      studentNo: form.value.studentNo.trim(),
      name: form.value.name.trim()
    })
    uni.showToast({ title: '绑定成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 1500)
  } catch (error: any) {
    uni.showToast({ 
      title: error.message || '绑定失败，请检查信息', 
      icon: 'none' 
    })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 30rpx;
}

.header {
  text-align: center;
  padding: 60rpx 0;
}

.title {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  display: block;
}

.subtitle {
  font-size: 28rpx;
  color: #999;
  margin-top: 16rpx;
  display: block;
}

.form-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.label {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-bottom: 16rpx;
}

.input {
  width: 100%;
  height: 88rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 30rpx;
  box-sizing: border-box;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  background: #1890ff;
  color: #fff;
  font-size: 32rpx;
  border-radius: 12rpx;
  border: none;
  margin-top: 40rpx;
}

.submit-btn[disabled] {
  background: #ccc;
}

.tips {
  margin-top: 40rpx;
  padding: 30rpx;
  background: #fffbe6;
  border-radius: 12rpx;
}

.tips-title {
  font-size: 28rpx;
  color: #d48806;
  font-weight: bold;
  display: block;
  margin-bottom: 16rpx;
}

.tips-item {
  font-size: 26rpx;
  color: #d48806;
  display: block;
  margin-bottom: 8rpx;
}
</style>
