<template>
  <view class="container">
    <view class="status-card" :class="statusClass">
      <text class="status-text">{{ statusText }}</text>
      <text class="booking-no">预约单号: {{ booking.bookingNo }}</text>
    </view>
    
    <view class="info-card">
      <view class="info-row">
        <text class="label">球馆</text>
        <text class="value">{{ booking.venueName }}</text>
      </view>
      <view class="info-row">
        <text class="label">场地</text>
        <text class="value">{{ booking.courtName }}</text>
      </view>
      <view class="info-row">
        <text class="label">日期</text>
        <text class="value">{{ booking.bookingDate }}</text>
      </view>
      <view class="info-row">
        <text class="label">时段</text>
        <text class="value">{{ booking.startTime }} - {{ booking.endTime }}</text>
      </view>
      <view class="info-row">
        <text class="label">创建时间</text>
        <text class="value">{{ formatDateTime(booking.createdAt) }}</text>
      </view>
    </view>
    
    <view class="qr-section" v-if="booking.status === 1">
      <view class="qr-container" @click="refreshQrCode">
        <image class="qr-code" :src="qrCodeUrl" mode="aspectFit" v-if="qrCodeUrl" />
        <view class="qr-placeholder" v-else>
          <text>点击获取核销码</text>
        </view>
      </view>
      <text class="qr-tip">请在核销窗口内向场馆员出示此码</text>
      <text class="qr-expire" v-if="qrExpireAt">
        有效期至: {{ formatDateTime(qrExpireAt) }}
      </text>
      <button class="refresh-btn" @click="refreshQrCode">刷新核销码</button>
    </view>
    
    <view class="actions" v-if="booking.status === 1">
      <button class="cancel-btn" @click="cancelBooking">取消预约</button>
    </view>
    
    <view class="cancel-info" v-if="booking.status === 2">
      <text class="cancel-reason">取消原因: {{ booking.cancelReason || '用户取消' }}</text>
      <text class="cancel-time">取消时间: {{ formatDateTime(booking.cancelledAt) }}</text>
    </view>
    
    <view class="checkin-info" v-if="booking.status === 3">
      <text class="checkin-time">签到时间: {{ formatDateTime(booking.checkinTime) }}</text>
    </view>
  </view>
</template>

<script>
import { getBookingDetail, getQrCode, cancelBooking as cancelBookingApi } from '@/api/booking'

export default {
  data() {
    return {
      bookingNo: '',
      booking: {},
      qrCodeUrl: '',
      qrExpireAt: null
    }
  },
  computed: {
    statusClass() {
      const classes = {
        1: 'confirmed',
        2: 'cancelled',
        3: 'checked-in',
        4: 'no-show'
      }
      return classes[this.booking.status] || ''
    },
    statusText() {
      const texts = {
        1: '已确认',
        2: '已取消',
        3: '已签到',
        4: '爽约'
      }
      return texts[this.booking.status] || '未知'
    }
  },
  onLoad(options) {
    this.bookingNo = options.bookingNo
    this.loadBooking()
  },
  onShow() {
    if (this.booking.status === 1) {
      this.refreshQrCode()
    }
  },
  methods: {
    async loadBooking() {
      try {
        this.booking = await getBookingDetail(this.bookingNo)
        if (this.booking.status === 1) {
          this.refreshQrCode()
        }
      } catch (e) {
        console.error(e)
      }
    },
    
    async refreshQrCode() {
      try {
        const result = await getQrCode(this.bookingNo)
        this.qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(result.token)}`
        this.qrExpireAt = result.expiresAt
      } catch (e) {
        console.error(e)
      }
    },
    
    async cancelBooking() {
      uni.showModal({
        title: '确认取消',
        content: '确定要取消此预约吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await cancelBookingApi(this.bookingNo, '用户取消')
              uni.showToast({ title: '取消成功', icon: 'success' })
              this.loadBooking()
            } catch (e) {
              console.error(e)
            }
          }
        }
      })
    },
    
    formatDateTime(datetime) {
      if (!datetime) return ''
      return datetime.replace('T', ' ').substring(0, 19)
    }
  }
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
