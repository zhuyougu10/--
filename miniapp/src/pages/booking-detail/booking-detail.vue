<template>
  <view class="container">
    <view class="status-card" :class="statusClass">
      <text class="status-text">{{ statusText }}</text>
      <text class="booking-no">预约单号: {{ currentBooking?.bookingNo }}</text>
    </view>
    
    <view class="info-card">
      <view class="info-row">
        <text class="label">球馆</text>
        <text class="value">{{ currentBooking?.venueName }}</text>
      </view>
      <view class="info-row">
        <text class="label">场地</text>
        <text class="value">{{ currentBooking?.courtName }}</text>
      </view>
      <view class="info-row">
        <text class="label">日期</text>
        <text class="value">{{ currentBooking?.bookingDate }}</text>
      </view>
      <view class="info-row">
        <text class="label">时段</text>
        <text class="value">{{ currentBooking?.startTime }} - {{ currentBooking?.endTime }}</text>
      </view>
      <view class="info-row">
        <text class="label">创建时间</text>
        <text class="value">{{ formatDateTime(currentBooking?.createdAt) }}</text>
      </view>
    </view>
    
    <view class="qr-section" v-if="currentBooking?.status === 1">
      <view class="qr-container" @click="refreshQrCodeData">
        <image class="qr-code" :src="qrCodeUrl" mode="aspectFit" v-if="qrCodeUrl" />
        <view class="qr-placeholder" v-else>
          <text>点击获取核销码</text>
        </view>
      </view>
      <text class="qr-tip">请在核销窗口内向场馆员出示此码</text>
      <text class="qr-expire" v-if="qrExpireAt">
        有效期至: {{ formatDateTime(qrExpireAt) }}
      </text>
      <button class="refresh-btn" @click="refreshQrCodeData">刷新核销码</button>
    </view>
    
    <view class="actions" v-if="currentBooking?.status === 1">
      <button class="cancel-btn" @click="handleCancelBooking">取消预约</button>
    </view>
    
    <view class="cancel-info" v-if="currentBooking?.status === 2">
      <text class="cancel-reason">取消原因: {{ currentBooking?.cancelReason || '用户取消' }}</text>
      <text class="cancel-time">取消时间: {{ formatDateTime(currentBooking?.cancelledAt) }}</text>
    </view>
    
    <view class="checkin-info" v-if="currentBooking?.status === 3">
      <text class="checkin-time">签到时间: {{ formatDateTime(currentBooking?.checkinTime) }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useBooking, useQrCode, useBookingStatus } from '@/composables/useBooking'
import { formatDateTime } from '@/utils/date'

const bookingNo = ref('')
const { currentBooking, loadBookingDetail, cancelBooking } = useBooking()
const { qrCodeUrl, qrExpireAt, refreshQrCode } = useQrCode()
const { getStatusText, getStatusClass } = useBookingStatus()

const statusClass = computed(() => {
  const classes: Record<number, string> = {
    1: 'confirmed',
    2: 'cancelled',
    3: 'checked-in',
    4: 'no-show'
  }
  return classes[currentBooking.value?.status || 0] || ''
})

const statusText = computed(() => 
  getStatusText(currentBooking.value?.status || 0)
)

onLoad((options) => {
  if (options?.bookingNo) {
    bookingNo.value = options.bookingNo
    loadBookingDetail(bookingNo.value)
  }
})

onShow(() => {
  if (currentBooking.value?.status === 1) {
    refreshQrCodeData()
  }
})

const refreshQrCodeData = () => {
  refreshQrCode(bookingNo.value)
}

const handleCancelBooking = () => {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消此预约吗？',
    success: async (res) => {
      if (res.confirm) {
        const success = await cancelBooking(bookingNo.value, '用户取消')
        if (success) {
          uni.showToast({ title: '取消成功', icon: 'success' })
          loadBookingDetail(bookingNo.value)
        }
      }
    }
  })
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx;
  padding-bottom: 60rpx;
}

.status-card {
  padding: 40rpx;
  border-radius: 16rpx;
  text-align: center;
  margin-bottom: 20rpx;
}

.status-card.confirmed {
  background: linear-gradient(135deg, #52c41a, #73d13d);
}

.status-card.cancelled {
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
}

.status-card.checked-in {
  background: linear-gradient(135deg, #1890ff, #40a9ff);
}

.status-card.no-show {
  background: linear-gradient(135deg, #faad14, #ffc53d);
}

.status-text {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  display: block;
}

.booking-no {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 16rpx;
  display: block;
}

.info-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.info-row:last-child {
  border-bottom: none;
}

.label {
  color: #999;
  font-size: 28rpx;
}

.value {
  color: #333;
  font-size: 28rpx;
}

.qr-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  text-align: center;
  margin-bottom: 20rpx;
}

.qr-container {
  width: 300rpx;
  height: 300rpx;
  margin: 0 auto;
  border: 2rpx solid #e8e8e8;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.qr-code {
  width: 280rpx;
  height: 280rpx;
}

.qr-placeholder {
  color: #999;
  font-size: 28rpx;
}

.qr-tip {
  font-size: 26rpx;
  color: #666;
  margin-top: 20rpx;
  display: block;
}

.qr-expire {
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
  display: block;
}

.refresh-btn {
  margin-top: 30rpx;
  background: #f5f5f5;
  color: #666;
  font-size: 26rpx;
  padding: 16rpx 40rpx;
  border-radius: 40rpx;
  border: none;
}

.actions {
  margin-top: 40rpx;
}

.cancel-btn {
  background: #fff;
  color: #ff4d4f;
  font-size: 30rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  border: 2rpx solid #ff4d4f;
}

.cancel-info, .checkin-info {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-top: 20rpx;
}

.cancel-reason, .cancel-time, .checkin-time {
  font-size: 28rpx;
  color: #666;
  display: block;
  margin-bottom: 10rpx;
}
</style>
