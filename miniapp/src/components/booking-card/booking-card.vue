<template>
  <view class="booking-card" @click="handleClick">
    <view class="booking-header">
      <text class="venue-name">{{ booking.venueName }}</text>
      <text class="status-tag" :class="statusClass">
        {{ statusText }}
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
    
    <view class="booking-footer" v-if="showFooter">
      <text class="booking-no">{{ booking.bookingNo }}</text>
      <text class="arrow">></text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'BookingCard',
  props: {
    booking: {
      type: Object,
      required: true
    },
    showFooter: {
      type: Boolean,
      default: true
    }
  },
  computed: {
    statusClass() {
      const classes = {
        1: 'pending',
        2: 'cancelled',
        3: 'completed',
        4: 'no-show'
      }
      return classes[this.booking.status] || ''
    },
    statusText() {
      const texts = {
        1: '待使用',
        2: '已取消',
        3: '已完成',
        4: '爽约'
      }
      return texts[this.booking.status] || '未知'
    }
  },
  methods: {
    handleClick() {
      this.$emit('click', this.booking)
    }
  }
}
</script>

<style scoped>
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
</style>
