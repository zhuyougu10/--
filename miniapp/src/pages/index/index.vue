<template>
  <view class="container">
    <view class="header">
      <text class="title">校园球馆预约</text>
      <text class="subtitle">智能预约，轻松运动</text>
    </view>
    
    <swiper
      class="venue-swiper"
      :circular="carouselVenues.length > 1"
      :autoplay="carouselVenues.length > 1"
      :interval="3000"
      v-if="carouselVenues.length > 0"
    >
      <swiper-item v-for="venue in carouselVenues" :key="venue.id" @click="goToVenue(venue.id)">
        <view class="swiper-item">
          <image class="swiper-image" :src="getVenueImage(venue.imageUrl)" mode="aspectFill" />
          <view class="swiper-info">
            <text class="swiper-name">{{ venue.name }}</text>
            <view class="swiper-meta">
              <text class="swiper-location">{{ venue.location }}</text>
              <view class="sport-types">
                <text v-for="type in getSportTypes(venue.sportType)" :key="type" class="sport-type">{{ getSportTypeName(type) }}</text>
              </view>
            </view>
          </view>
        </view>
      </swiper-item>
    </swiper>

    <view class="quick-nav">
      <view class="nav-item" @click="navTo('/pages/venue/venue', true)">
        <view class="nav-icon" style="background-color: #1890ff;">馆</view>
        <text class="nav-text">全部球馆</text>
      </view>
      <view class="nav-item" @click="navTo('/pages/my-bookings/my-bookings?status=all')">
        <view class="nav-icon" style="background-color: #52c41a;">预</view>
        <text class="nav-text">我的预约</text>
      </view>
      <view class="nav-item" @click="navTo('/pages/my-bookings/my-bookings?status=1')">
        <view class="nav-icon" style="background-color: #faad14;">待</view>
        <text class="nav-text">待使用</text>
      </view>
      <view class="nav-item" @click="navTo('/pages/my/my', true)">
        <view class="nav-icon" style="background-color: #722ed1;">我</view>
        <text class="nav-text">个人中心</text>
      </view>
    </view>
    
    <view class="venue-list">
      <view class="section-title">热门球馆</view>
      
      <venue-card 
        v-for="venue in venues" 
        :key="venue.id"
        :venue="venue"
        @click="handleVenueClick"
      />
      
      <view class="empty" v-if="venues.length === 0 && !loading">
        <text>暂无球馆数据</text>
      </view>
      
      <view class="loading" v-if="loading">
        <text>加载中...</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useVenue, useSportType } from '@/composables/useVenue'
import { resolveAssetUrl } from '@/utils/asset'
import VenueCard from '@/components/venue-card/venue-card.vue'
import type { Venue } from '@/types'

const { venues, loading, loadVenues } = useVenue()
const { getSportTypeName } = useSportType()

const carouselVenues = computed(() => {
  return venues.value.slice(0, 3)
})

const getSportTypes = (sportType: string) => {
  if (!sportType) return []
  return sportType.split(',').map(t => t.trim()).filter(Boolean)
}

onShow(() => {
  loadVenues()
})

const goToVenue = (id: number) => {
  uni.navigateTo({
    url: `/pages/venue-detail/venue-detail?id=${id}`
  })
}

const handleVenueClick = (venue: Venue) => {
  goToVenue(venue.id)
}

const navTo = (url: string, isTab = false) => {
  if (isTab) {
    uni.switchTab({ url })
  } else {
    uni.navigateTo({ url })
  }
}

const getVenueImage = (imageUrl?: string) => resolveAssetUrl(imageUrl)
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
  margin-bottom: 20rpx;
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

.venue-swiper {
  height: 300rpx;
  margin-bottom: 30rpx;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.swiper-item {
  width: 100%;
  height: 100%;
  position: relative;
}

.swiper-image {
  width: 100%;
  height: 100%;
}

.swiper-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  color: #fff;
}

.swiper-name {
  font-size: 32rpx;
  font-weight: bold;
  display: block;
}

.swiper-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
  margin-top: 10rpx;
}

.swiper-location {
  flex: 1;
  min-width: 0;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.quick-nav {
  display: flex;
  justify-content: space-between;
  padding: 30rpx 40rpx;
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.nav-icon {
  width: 90rpx;
  height: 90rpx;
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 36rpx;
  margin-bottom: 12rpx;
}

.nav-text {
  font-size: 24rpx;
  color: #333;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
}

.sport-types {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8rpx;
}

.sport-type {
  font-size: 22rpx;
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.empty, .loading {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
}
</style>
