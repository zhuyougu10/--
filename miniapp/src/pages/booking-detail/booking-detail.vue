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
    
    <view class="qr-section" v-if="currentBooking?.status === 1 && !isExpired">
      <view class="qr-container" @click="refreshQrCodeData">
        <canvas
          canvas-id="qrCanvas"
          id="qrCanvas"
          class="qr-canvas"
          style="position: fixed; left: -9999px; top: -9999px; width: 320px; height: 320px;"
        ></canvas>
        <image class="qr-code" :src="qrCodeUrl" mode="aspectFit" v-if="qrCodeUrl" />
        <view class="qr-placeholder" v-else>
          <text>正在生成核销码...</text>
        </view>
      </view>
      <text class="qr-tip">请在核销窗口内向场馆员出示此码</text>
      <text class="qr-expire" v-if="qrExpireAt">
        有效期至: {{ formatDateTime(qrExpireAt) }}
      </text>
      <button class="refresh-btn" @click="refreshQrCodeData">刷新核销码</button>
    </view>

    <view class="expire-info" v-if="currentBooking?.status === 1 && isExpired">
      <text class="expire-text">预约已过期，请重新预约</text>
    </view>
    
    <view class="actions" v-if="currentBooking?.status === 1 && !isExpired">
      <button class="cancel-btn" @click="handleCancelBooking">取消预约</button>
    </view>
    
    <view class="cancel-info" v-if="currentBooking?.status === 2">
      <text class="cancel-reason">取消原因: {{ currentBooking?.cancelReason || '用户取消' }}</text>
      <text class="cancel-time">取消时间: {{ formatDateTime(currentBooking?.cancelledAt) }}</text>
    </view>
    
    <view class="checkin-info" v-if="currentBooking?.status === 3">
      <text class="checkin-time">签到时间: {{ formatDateTime(currentBooking?.checkedInAt) }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad, onShow, onHide, onUnload } from '@dcloudio/uni-app'
import { useBooking, useQrCode, useBookingStatus } from '@/composables/useBooking'
import { formatDateTime } from '@/utils/date'
import qrcode from 'qrcode-generator'

const bookingNo = ref('')
const { currentBooking, loadBookingDetail, cancelBooking } = useBooking()
const { qrCodeUrl, qrToken, qrExpireAt, refreshQrCode } = useQrCode()
const { getStatusText } = useBookingStatus()
let bookingRefreshTimer: ReturnType<typeof setInterval> | null = null
let qrRefreshTimer: ReturnType<typeof setTimeout> | null = null

const drawQrCodeByToken = (token: string): Promise<string> => {
  return new Promise((resolve, reject) => {
    if (!token) {
      reject(new Error('二维码内容为空'))
      return
    }
    const qr = qrcode(0, 'H')
    qr.addData(token)
    qr.make()

    const cells = qr.getModuleCount()
    const cellSize = 8
    const margin = 16
    const size = cells * cellSize + margin * 2

    const ctx = uni.createCanvasContext('qrCanvas')
    ctx.setFillStyle('#FFFFFF')
    ctx.fillRect(0, 0, size, size)
    ctx.setFillStyle('#111111')

    for (let row = 0; row < cells; row++) {
      for (let col = 0; col < cells; col++) {
        if (qr.isDark(row, col)) {
          ctx.fillRect(margin + col * cellSize, margin + row * cellSize, cellSize, cellSize)
        }
      }
    }

    ctx.draw(false, () => {
      setTimeout(() => {
        uni.canvasToTempFilePath({
          canvasId: 'qrCanvas',
          x: 0,
          y: 0,
          width: size,
          height: size,
          destWidth: size * 2,
          destHeight: size * 2,
          fileType: 'png',
          quality: 1,
          success: (res) => resolve(res.tempFilePath),
          fail: (err) => reject(err)
        })
      }, 60)
    })
  })
}

const isExpired = computed(() => {
  const booking = currentBooking.value
  if (!booking || booking.status !== 1) return false
  if (!booking.bookingDate || !booking.endTime) return false
  const endAt = new Date(`${booking.bookingDate} ${booking.endTime}`)
  if (Number.isNaN(endAt.getTime())) return false
  return Date.now() > endAt.getTime()
})

const statusClass = computed(() => {
  if (isExpired.value) return 'expired'
  const classes: Record<number, string> = {
    1: 'confirmed',
    2: 'cancelled',
    3: 'checked-in',
    4: 'no-show'
  }
  return classes[currentBooking.value?.status || 0] || ''
})

const statusText = computed(() => {
  if (isExpired.value) return '已过期'
  return getStatusText(currentBooking.value?.status || 0)
})

const clearQrRefreshTimer = () => {
  if (qrRefreshTimer) {
    clearTimeout(qrRefreshTimer)
    qrRefreshTimer = null
  }
}

const scheduleQrRefresh = () => {
  clearQrRefreshTimer()
  if (!qrExpireAt.value || currentBooking.value?.status !== 1 || isExpired.value) return
  const expireTime = new Date(qrExpireAt.value).getTime()
  if (Number.isNaN(expireTime)) return
  const delay = expireTime - Date.now()
  if (delay <= 0) {
    refreshQrCodeData(true)
    return
  }
  qrRefreshTimer = setTimeout(() => {
    refreshQrCodeData(true)
  }, delay + 500)
}

const clearBookingRefreshTimer = () => {
  if (bookingRefreshTimer) {
    clearInterval(bookingRefreshTimer)
    bookingRefreshTimer = null
  }
}

const startBookingStatusPolling = () => {
  clearBookingRefreshTimer()
  bookingRefreshTimer = setInterval(async () => {
    if (!bookingNo.value) return
    await loadBookingDetail(bookingNo.value)
    if (currentBooking.value?.status !== 1 || isExpired.value) {
      clearBookingRefreshTimer()
      clearQrRefreshTimer()
      return
    }
  }, 5000)
}

const initBookingDetailPage = async () => {
  if (!bookingNo.value) return
  await loadBookingDetail(bookingNo.value)
  if (currentBooking.value?.status === 1 && !isExpired.value) {
    await refreshQrCodeData(true)
    startBookingStatusPolling()
  } else {
    clearQrRefreshTimer()
    clearBookingRefreshTimer()
  }
}

onLoad((options) => {
  if (options?.bookingNo) {
    bookingNo.value = options.bookingNo
    initBookingDetailPage()
  }
})

onShow(() => {
  if (!bookingNo.value) return
  initBookingDetailPage()
})

onHide(() => {
  clearBookingRefreshTimer()
  clearQrRefreshTimer()
})

onUnload(() => {
  clearBookingRefreshTimer()
  clearQrRefreshTimer()
})

const refreshQrCodeData = async (force = false) => {
  if (!bookingNo.value || currentBooking.value?.status !== 1 || isExpired.value) return
  if (!force && qrExpireAt.value) {
    const expireTime = new Date(qrExpireAt.value).getTime()
    if (!Number.isNaN(expireTime) && expireTime > Date.now()) {
      return
    }
  }
  await refreshQrCode(bookingNo.value)
  if (qrToken.value) {
    try {
      qrCodeUrl.value = await drawQrCodeByToken(qrToken.value)
    } catch (e) {
      console.error('生成二维码失败', e)
      uni.showToast({ title: '二维码生成失败', icon: 'none' })
    }
  }
  scheduleQrRefresh()
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
          await loadBookingDetail(bookingNo.value)
          clearBookingRefreshTimer()
          clearQrRefreshTimer()
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

.status-card.expired {
  background: linear-gradient(135deg, #f5222d, #ff7875);
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

.qr-canvas {
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

.expire-info {
  background: #fff1f0;
  border: 2rpx solid #ffa39e;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-top: 20rpx;
}

.expire-text {
  font-size: 30rpx;
  color: #cf1322;
  font-weight: bold;
}

.cancel-reason, .cancel-time, .checkin-time {
  font-size: 28rpx;
  color: #666;
  display: block;
  margin-bottom: 10rpx;
}
</style>
