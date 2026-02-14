<template>
  <view class="container">
    <view class="tabs">
      <view 
        class="tab-item" 
        :class="{ active: activeStatus === 'all' }"
        @click="changeStatus('all')"
      >
        全部
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeStatus === 1 }"
        @click="changeStatus(1)"
      >
        待使用
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeStatus === 3 }"
        @click="changeStatus(3)"
      >
        已完成
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeStatus === 2 }"
        @click="changeStatus(2)"
      >
        已取消
      </view>
    </view>
    
    <view class="booking-list">
      <view 
        class="booking-card" 
        v-for="booking in bookings" 
        :key="booking.bookingNo"
        @click="goToDetail(booking.bookingNo)"
      >
        <view class="booking-header">
          <text class="venue-name">{{ booking.venueName }}</text>
          <text class="status-tag" :class="getStatusClass(booking.status)">
            {{ getStatusText(booking.status) }}
          </text>
        </view>
        
        <view class="booking-info">
          <view class="info-row">
            <text class="label">场地:</text>
            <text class="value">{{ booking.courtName }}</text>
          </view>
          <view class="info-row">
            <text class="label">日期:</text>
            <text class="value">{{ booking.bookingDate }}</text>
          </view>
          <view class="info-row">
            <text class="label">时段:</text>
            <text class="value">{{ booking.startTime }} - {{ booking.endTime }}</text>
          </view>
        </view>
        
        <view class="booking-footer">
          <text class="booking-no">{{ booking.bookingNo }}</text>
          <text class="arrow">></text>
        </view>
      </view>
      
      <view class="empty" v-if="bookings.length === 0 && !loading">
        <text>暂无预约记录</text>
      </view>
      
      <view class="loading" v-if="loading">
        <text>加载中...</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getMyBookings } from '@/api/booking'

export default {
  data() {
    return {
      activeStatus: 'all',
      bookings: [],
      loading: false
    }
  },
  onLoad(options) {
    if (options.status) {
      this.activeStatus = options.status === 'all' ? 'all' : parseInt(options.status)
    }
    this.loadBookings()
  },
  onShow() {
    this.loadBookings()
  },
  methods: {
    async loadBookings() {
      this.loading = true
      try {
        const status = this.activeStatus === 'all' ? null : this.activeStatus
        this.bookings = await getMyBookings(status)
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    
    changeStatus(status) {
      this.activeStatus = status
      this.loadBookings()
    },
    
    getStatusClass(status) {
      const classes = {
        1: 'pending',
        2: 'cancelled',
        3: 'completed',
        4: 'no-show'
      }
      return classes[status] || ''
    },
    
    getStatusText(status) {
      const texts = {
        1: '待使用',
        2: '已取消',
        3: '已完成',
        4: '爽约'
      }
      return texts[status] || '未知'
    },
    
    goToDetail(bookingNo) {
      uni.navigateTo({
        url: `/pages/booking-detail/booking-detail?bookingNo=${bookingNo}`
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

.tabs {
  display: flex;
  background: #fff;
  padding: 20rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  font-size: 28rpx;
  color: #666;
  border-radius: 8rpx;
}

.tab-item.active {
  background: #e6f7ff;
  color: #1890ff;
  font-weight: bold;
}

.booking-list {
  padding: 20rpx;
}

.booking-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.booking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.venue-name {
  font-size: 32rpx;
  font-weight: bold;
}

.status-tag {
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
}

.status-tag.pending {
  background: #e6f7ff;
  color: #1890ff;
}

.status-tag.cancelled {
  background: #f5f5f5;
  color: #999;
}

.status-tag.completed {
  background: #f6ffed;
  color: #52c41a;
}

.status-tag.no-show {
  background: #fff7e6;
  color: #faad14;
}

.booking-info {
  border-top: 1rpx solid #f5f5f5;
  padding-top: 20rpx;
}

.info-row {
  display: flex;
  margin-bottom: 12rpx;
}

.label {
  color: #999;
  font-size: 26rpx;
  width: 100rpx;
}

.value {
  color: #333;
  font-size: 26rpx;
}

.booking-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f5f5f5;
}

.booking-no {
  font-size: 24rpx;
  color: #999;
}

.arrow {
  color: #ccc;
  font-size: 28rpx;
}

.empty, .loading {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
}
</style>
