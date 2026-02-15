<template>
  <view class="container">
    <view class="search-bar">
      <input 
        class="search-input" 
        placeholder="搜索球馆" 
        v-model="keyword"
        @confirm="search"
      />
    </view>
    
    <view class="filter-bar">
      <view 
        class="filter-item" 
        :class="{ active: activeType === '' }"
        @click="filterByType('')"
      >
        全部
      </view>
      <view 
        class="filter-item" 
        :class="{ active: activeType === 'badminton' }"
        @click="filterByType('badminton')"
      >
        羽毛球
      </view>
      <view 
        class="filter-item" 
        :class="{ active: activeType === 'basketball' }"
        @click="filterByType('basketball')"
      >
        篮球
      </view>
      <view 
        class="filter-item" 
        :class="{ active: activeType === 'table_tennis' }"
        @click="filterByType('table_tennis')"
      >
        乒乓球
      </view>
    </view>
    
    <view class="venue-list">
      <view 
        class="venue-card" 
        v-for="venue in filteredVenues" 
        :key="venue.id"
        @click="goToVenue(venue.id)"
      >
        <image class="venue-image" :src="venue.imageUrl || '/static/default-venue.svg'" mode="aspectFill" />
        <view class="venue-info">
          <text class="venue-name">{{ venue.name }}</text>
          <text class="venue-location">{{ venue.location }}</text>
          <view class="venue-meta">
            <view class="sport-types">
              <text v-for="type in getSportTypes(venue.sportType)" :key="type" class="sport-type">{{ getSportTypeName(type) }}</text>
            </view>
            <text class="court-count">{{ venue.courtCount || 0 }}个场地</text>
          </view>
          <view class="venue-status">
            <text class="status-dot" :class="{ open: isOpen(venue) }"></text>
            <text class="status-text">{{ isOpen(venue) ? '营业中' : '已闭馆' }}</text>
          </view>
        </view>
      </view>
      
      <view class="empty" v-if="filteredVenues.length === 0 && !loading">
        <text>暂无球馆数据</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useVenue, useSportType } from '@/composables/useVenue'
import { isVenueOpen } from '@/utils/date'
import type { Venue } from '@/types'

const { venues, loading, loadVenues } = useVenue()
const { getSportTypeName } = useSportType()

const getSportTypes = (sportType: string) => {
  if (!sportType) return []
  return sportType.split(',').filter(t => t.trim())
}

const keyword = ref('')
const activeType = ref('')

const filteredVenues = computed(() => {
  let result = venues.value
  if (activeType.value) {
    result = result.filter(v => v.sportType && v.sportType.split(',').includes(activeType.value))
  }
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    result = result.filter(v => 
      v.name.toLowerCase().includes(kw) || 
      v.location.toLowerCase().includes(kw)
    )
  }
  return result
})

onShow(() => {
  loadVenues()
})

const isOpen = (venue: Venue) => isVenueOpen(venue)

const filterByType = (type: string) => {
  activeType.value = type
}

const search = () => {
  // keyword filter is computed
}

const goToVenue = (id: number) => {
  uni.navigateTo({
    url: `/pages/venue-detail/venue-detail?id=${id}`
  })
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
}

.search-bar {
  padding: 20rpx;
  background: #fff;
}

.search-input {
  background: #f5f5f5;
  border-radius: 40rpx;
  padding: 16rpx 30rpx;
  font-size: 28rpx;
}

.filter-bar {
  display: flex;
  padding: 20rpx;
  background: #fff;
  border-top: 1rpx solid #eee;
}

.filter-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  font-size: 26rpx;
  color: #666;
  border-radius: 8rpx;
}

.filter-item.active {
  background: #e6f7ff;
  color: #1890ff;
}

.venue-list {
  padding: 20rpx;
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
  height: 180rpx;
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
  margin-top: 12rpx;
}

.sport-types {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.sport-type {
  font-size: 24rpx;
  color: #1890ff;
  background: rgba(24, 144, 255, 0.1);
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.court-count {
  font-size: 24rpx;
  color: #666;
}

.venue-status {
  display: flex;
  align-items: center;
  margin-top: 12rpx;
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #ccc;
  margin-right: 8rpx;
}

.status-dot.open {
  background: #52c41a;
}

.status-text {
  font-size: 24rpx;
  color: #999;
}

.empty {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
}
</style>
