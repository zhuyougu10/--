<template>
  <view class="venue-card" @click="handleClick">
    <image class="venue-image" :src="venue.imageUrl || '/static/default-venue.svg'" mode="aspectFill" />
    <view class="venue-info">
      <text class="venue-name">{{ venue.name }}</text>
      <text class="venue-location">{{ venue.location }}</text>
      <view class="venue-meta">
        <view class="sport-types">
          <text v-for="type in getSportTypes(venue.sportType)" :key="type" class="sport-type">{{ getSportTypeName(type) }}</text>
        </view>
        <text class="open-time" v-if="showTime">{{ venue.openTime }} - {{ venue.closeTime }}</text>
        <text class="court-count" v-if="showCourtCount">{{ venue.courtCount || 0 }}个场地</text>
      </view>
      <view class="venue-status" v-if="showStatus">
        <text class="status-dot" :class="{ open: isOpen }"></text>
        <text class="status-text">{{ isOpen ? '营业中' : '已闭馆' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useSportType } from '@/composables/useVenue'
import { isVenueOpen } from '@/utils/date'
import type { Venue } from '@/types'

const props = withDefaults(defineProps<{
  venue: Venue
  showTime?: boolean
  showCourtCount?: boolean
  showStatus?: boolean
}>(), {
  showTime: true,
  showCourtCount: false,
  showStatus: false
})

const emit = defineEmits<{
  click: [venue: Venue]
}>()

const { getSportTypeName } = useSportType()

const getSportTypes = (sportType: string) => {
  if (!sportType) return []
  return sportType.split(',').filter(t => t.trim())
}

const isOpen = computed(() => isVenueOpen(props.venue))

const handleClick = () => {
  emit('click', props.venue)
}
</script>

<style scoped>
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
  align-items: center;
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

.open-time, .court-count {
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
</style>
