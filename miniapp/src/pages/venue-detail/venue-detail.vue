<template>
  <view class="container">
    <view class="venue-header">
      <image class="venue-image" :src="currentVenue?.imageUrl || '/static/default-venue.svg'" mode="aspectFill" />
      <view class="venue-overlay">
        <text class="venue-name">{{ currentVenue?.name }}</text>
        <text class="venue-location">{{ currentVenue?.location }}</text>
      </view>
    </view>
    
    <view class="venue-info card">
      <view class="info-row">
        <text class="label">运动类型</text>
        <view class="sport-types">
          <text v-for="type in getSportTypes(currentVenue?.sportType || '')" :key="type" class="sport-tag">{{ getSportTypeName(type) }}</text>
        </view>
      </view>
      <view class="info-row">
        <text class="label">营业时间</text>
        <text class="value">{{ currentVenue?.openTime }} - {{ currentVenue?.closeTime }}</text>
      </view>
      <view class="info-row">
        <text class="label">联系电话</text>
        <text class="value">{{ currentVenue?.phone || '暂无' }}</text>
      </view>
      <view class="info-row">
        <text class="label">场馆状态</text>
        <text class="value" :class="{ 'text-success': isVenueOpen }">
          {{ isVenueOpen ? '营业中' : '已闭馆' }}
        </text>
      </view>
    </view>

    <view class="rules-section card">
      <view class="section-header">
        <text class="section-title">📖 预约须知</text>
      </view>
      <view class="rules-content" :class="{ expanded: rulesExpanded }">
        <view class="rules-inner">
          <view class="rule-item" v-if="currentVenue?.dailySlotLimit">
            <text class="rule-dot">•</text>
            <text class="rule-text">每人每天最多预约 {{ currentVenue.dailySlotLimit }} 个时段</text>
          </view>
          <view class="rule-item" v-if="currentVenue?.weeklySlotLimit">
            <text class="rule-dot">•</text>
            <text class="rule-text">每人每周最多预约 {{ currentVenue.weeklySlotLimit }} 个时段</text>
          </view>
          <view class="rule-item" v-if="currentVenue?.bookAheadDays">
            <text class="rule-dot">•</text>
            <text class="rule-text">可提前 {{ currentVenue.bookAheadDays }} 天预约</text>
          </view>
          <view class="rule-item expandable" v-if="currentVenue?.cancelCutoffMinutes">
            <text class="rule-dot">•</text>
            <text class="rule-text">预约开始前 {{ currentVenue.cancelCutoffMinutes }} 分钟不可取消</text>
          </view>
          <view class="rule-item expandable" v-if="currentVenue?.checkinWindowBefore">
            <text class="rule-dot">•</text>
            <text class="rule-text">请在预约时段开始前 {{ currentVenue.checkinWindowBefore }} 分钟内核销签到</text>
          </view>
          <view class="rule-item expandable" v-if="currentVenue?.noShowGraceMinutes">
            <text class="rule-dot">•</text>
            <text class="rule-text">迟到 {{ currentVenue.noShowGraceMinutes }} 分钟将视为爽约</text>
          </view>
          <view class="rule-item expandable" v-if="currentVenue?.slotMinutes">
            <text class="rule-dot">•</text>
            <text class="rule-text">每个时段时长 {{ currentVenue.slotMinutes }} 分钟</text>
          </view>
          <view class="rule-item expandable">
            <text class="rule-dot">•</text>
            <text class="rule-text">爽约3次将被限制预约7天</text>
          </view>
        </view>
      </view>
      <view class="expand-btn" @click="rulesExpanded = !rulesExpanded">
        <text class="expand-text">{{ rulesExpanded ? '收起' : '展开全部' }}</text>
        <text class="expand-icon" :class="{ rotated: rulesExpanded }">▼</text>
      </view>
    </view>
    
    <view class="court-section">
      <view class="section-title">场地列表</view>
      <view class="court-list">
        <view 
          class="court-card" 
          v-for="court in currentVenue?.courts" 
          :key="court.id"
          @click="goToBooking(court)"
        >
          <view class="court-info">
            <text class="court-name">{{ court.name }}</text>
            <text class="court-type">{{ court.courtNo ? court.courtNo + '号场' : '' }}</text>
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
      
      <view class="empty" v-if="!currentVenue?.courts || currentVenue.courts.length === 0">
        <text>暂无场地</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useVenue, useSportType } from '@/composables/useVenue'
import type { Court } from '@/types'

const venueId = ref<number>(0)
const rulesExpanded = ref(false)
const { currentVenue, loadVenueDetail } = useVenue()
const { getSportTypeName } = useSportType()

const getSportTypes = (sportType: string) => {
  if (!sportType) return []
  return sportType.split(',').filter(t => t.trim())
}

const isVenueOpen = computed(() => {
  if (!currentVenue.value || currentVenue.value.status !== 1) return false
  const now = new Date()
  const currentTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
  return currentTime >= currentVenue.value.openTime && currentTime <= currentVenue.value.closeTime
})

onLoad((options) => {
  if (options?.id) {
    venueId.value = Number(options.id)
    loadVenueDetail(venueId.value)
  }
})

const goToBooking = (court: Court) => {
  if (court.status !== 1) {
    uni.showToast({
      title: '该场地维护中',
      icon: 'none'
    })
    return
  }
  uni.navigateTo({
    url: `/pages/booking/booking?venueId=${venueId.value}&courtId=${court.id}`
  })
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

.sport-types {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  justify-content: flex-end;
}

.sport-tag {
  font-size: 24rpx;
  color: #1890ff;
  background: rgba(24, 144, 255, 0.1);
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
}

.rules-section {
  padding: 0;
  overflow: hidden;
}

.section-header {
  padding: 30rpx 30rpx 20rpx 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
}

.rules-content {
  max-height: 240rpx;
  overflow: hidden;
  transition: max-height 0.3s ease-out;
}

.rules-content.expanded {
  max-height: 800rpx;
}

.rules-inner {
  padding: 0 30rpx 10rpx 30rpx;
}

.rule-item {
  display: flex;
  align-items: flex-start;
  padding: 12rpx 0;
}

.rule-item.expandable {
  opacity: 0;
  transform: translateY(-10rpx);
  transition: opacity 0.3s ease-out, transform 0.3s ease-out;
  transition-delay: 0s;
}

.rules-content.expanded .rule-item.expandable {
  opacity: 1;
  transform: translateY(0);
}

.rules-content.expanded .rule-item.expandable:nth-child(4) {
  transition-delay: 0.05s;
}

.rules-content.expanded .rule-item.expandable:nth-child(5) {
  transition-delay: 0.1s;
}

.rules-content.expanded .rule-item.expandable:nth-child(6) {
  transition-delay: 0.15s;
}

.rules-content.expanded .rule-item.expandable:nth-child(7) {
  transition-delay: 0.2s;
}

.rules-content.expanded .rule-item.expandable:nth-child(8) {
  transition-delay: 0.25s;
}

.rule-dot {
  color: #1890ff;
  font-size: 28rpx;
  margin-right: 12rpx;
}

.rule-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.5;
}

.expand-btn {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20rpx 30rpx 30rpx 30rpx;
  background: #fff;
}

.expand-text {
  font-size: 28rpx;
  color: #1890ff;
  margin-right: 8rpx;
}

.expand-icon {
  font-size: 20rpx;
  color: #1890ff;
  transition: transform 0.3s ease;
}

.expand-icon.rotated {
  transform: rotate(180deg);
}

.court-section {
  margin-top: 20rpx;
}

.court-section .section-title {
  font-size: 32rpx;
  font-weight: bold;
  padding: 20rpx;
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
