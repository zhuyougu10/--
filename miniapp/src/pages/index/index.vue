<template>
  <view class="container">
    <view class="header">
      <text class="title">校园球馆预约</text>
      <text class="subtitle">智能预约，轻松运动</text>
    </view>
    
    <view class="venue-list">
      <view class="section-title">热门球馆</view>
      <view 
        class="venue-card" 
        v-for="venue in venues" 
        :key="venue.id"
        @click="goToVenue(venue.id)"
      >
        <image class="venue-image" :src="venue.imageUrl || '/static/default-venue.svg'" mode="aspectFill" />
        <view class="venue-info">
          <text class="venue-name">{{ venue.name }}</text>
          <text class="venue-location">{{ venue.location }}</text>
          <view class="venue-meta">
            <text class="sport-type">{{ getSportTypeName(venue.sportType) }}</text>
            <text class="open-time">{{ venue.openTime }} - {{ venue.closeTime }}</text>
          </view>
        </view>
      </view>
      
      <view class="empty" v-if="venues.length === 0 && !loading">
        <text>暂无球馆数据</text>
      </view>
      
      <view class="loading" v-if="loading">
        <text>加载中...</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getVenueList } from '@/api/venue'

export default {
  data() {
    return {
      venues: [],
      loading: false
    }
  },
  onShow() {
    this.loadVenues()
  },
  methods: {
    async loadVenues() {
      this.loading = true
      try {
        this.venues = await getVenueList()
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
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
    goToVenue(id) {
      uni.navigateTo({
        url: `/pages/venue-detail/venue-detail?id=${id}`
      })
    }
  }
}
</script>

<style scoped>
.container {
  padding: 20rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.header {
  padding: 40rpx 20rpx;
  background: linear-gradient(135deg, #1890ff, #36cfc9);
  border-radius: 16rpx;
  margin-bottom: 30rpx;
}

.title {
  font-size: 44rpx;
  font-weight: bold;
  color: #fff;
  display: block;
}

.subtitle {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 10rpx;
  display: block;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
}

.venue-card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
  display: flex;
}

.venue-image {
  width: 200rpx;
  height: 160rpx;
}

.venue-info {
  flex: 1;
  padding: 20rpx;
}

.venue-name {
  font-size: 32rpx;
  font-weight: bold;
  display: block;
}

.venue-location {
  font-size: 26rpx;
  color: #999;
  margin-top: 8rpx;
  display: block;
}

.venue-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 16rpx;
}

.sport-type {
  font-size: 24rpx;
  color: #1890ff;
  background: rgba(24, 144, 255, 0.1);
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.open-time {
  font-size: 24rpx;
  color: #666;
}

.empty, .loading {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
}
</style>
