<template>
  <view class="container">
    <view class="venue-header">
      <image class="venue-image" :src="venue.imageUrl || '/static/default-venue.svg'" mode="aspectFill" />
      <view class="venue-overlay">
        <text class="venue-name">{{ venue.name }}</text>
        <text class="venue-location">{{ venue.location }}</text>
      </view>
    </view>
    
    <view class="venue-info card">
      <view class="info-row">
        <text class="label">运动类型</text>
        <text class="value">{{ getSportTypeName(venue.sportType) }}</text>
      </view>
      <view class="info-row">
        <text class="label">营业时间</text>
        <text class="value">{{ venue.openTime }} - {{ venue.closeTime }}</text>
      </view>
      <view class="info-row">
        <text class="label">联系电话</text>
        <text class="value">{{ venue.phone || '暂无' }}</text>
      </view>
      <view class="info-row">
        <text class="label">场馆状态</text>
        <text class="value" :class="{ 'text-success': venue.status === 1 }">
          {{ venue.status === 1 ? '营业中' : '已闭馆' }}
        </text>
      </view>
    </view>
    
    <view class="court-section">
      <view class="section-title">场地列表</view>
      <view class="court-list">
        <view 
          class="court-card" 
          v-for="court in venue.courts" 
          :key="court.id"
          @click="goToBooking(court)"
        >
          <view class="court-info">
            <text class="court-name">{{ court.name }}</text>
            <text class="court-type">{{ court.indoor ? '室内' : '室外' }}</text>
          </view>
          <view class="court-status">
            <text class="status-tag" :class="{ available: court.status === 1 }">
              {{ court.status === 1 ? '可预约' : '维护中' }}
            </text>
          </view>
          <view class="court-arrow">
            <text class="arrow">></text>
          </view>
        </view>
      </view>
      
      <view class="empty" v-if="!venue.courts || venue.courts.length === 0">
        <text>暂无场地</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getVenueDetail } from '@/api/venue'

export default {
  data() {
    return {
      venueId: null,
      venue: {}
    }
  },
  onLoad(options) {
    this.venueId = options.id
    this.loadVenue()
  },
  methods: {
    async loadVenue() {
      try {
        this.venue = await getVenueDetail(this.venueId)
      } catch (e) {
        console.error(e)
      }
    },
    getSportTypeName(type) {
      const types = {
        badminton: '羽毛球',
        basketball: '篮球',
        table_tennis: '乒乓球',
        tennis: '网球'
      }
      return types[type] || type
    },
    goToBooking(court) {
      if (court.status !== 1) {
        uni.showToast({
          title: '该场地维护中',
          icon: 'none'
        })
        return
      }
      uni.navigateTo({
        url: `/pages/booking/booking?venueId=${this.venueId}&courtId=${court.id}`
      })
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 40rpx;
}

.venue-header {
  position: relative;
  height: 360rpx;
}

.venue-image {
  width: 100%;
  height: 100%;
}

.venue-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30rpx;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
}

.venue-name {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  display: block;
}

.venue-location {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 10rpx;
  display: block;
}

.card {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 30rpx;
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

.text-success {
  color: #52c41a;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  padding: 20rpx;
}

.court-section {
  margin-top: 20rpx;
}

.court-list {
  background: #fff;
  margin: 0 20rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.court-card {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.court-card:last-child {
  border-bottom: none;
}

.court-info {
  flex: 1;
}

.court-name {
  font-size: 30rpx;
  font-weight: bold;
  display: block;
}

.court-type {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}

.court-status {
  margin-right: 20rpx;
}

.status-tag {
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  background: #f5f5f5;
  color: #999;
}

.status-tag.available {
  background: #e6f7ff;
  color: #1890ff;
}

.court-arrow {
  color: #ccc;
}

.arrow {
  font-size: 28rpx;
}

.empty {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
}
</style>
