<template>
  <view class="container">
    <view class="venue-info">
      <text class="venue-name">{{ currentVenue?.name }}</text>
      <text class="court-name">{{ court?.name }}</text>
    </view>
    
    <view class="date-picker">
      <scroll-view scroll-x class="date-scroll">
        <view 
          class="date-item" 
          v-for="(date, index) in dateList" 
          :key="index"
          :class="{ active: selectedDate === date.value }"
          @click="selectDate(date.value)"
        >
          <text class="date-week">{{ date.week }}</text>
          <text class="date-day">{{ date.day }}</text>
        </view>
      </scroll-view>
    </view>
    
    <view class="slots-container">
      <view class="slots-header">
        <text>选择时段</text>
        <view class="legend">
          <text class="legend-item free">可预约</text>
          <text class="legend-item occupied">已占用</text>
        </view>
      </view>
      
      <view class="slots-grid">
        <view 
          class="slot-item"
          v-for="slot in slots" 
          :key="slot.startTime"
          :class="{ 
            free: slot.status === 'free',
            occupied: slot.status === 'occupied',
            selected: isSelected(slot)
          }"
          @click="toggleSlot(slot)"
        >
          <text class="slot-time">{{ formatTime(slot.startTime) }}</text>
        </view>
      </view>
      
      <view class="empty" v-if="slots.length === 0 && !loading">
        <text>暂无时段数据</text>
      </view>
    </view>
    
    <view class="footer">
      <view class="selected-info">
        <text v-if="selectedSlots.length > 0">
          已选: {{ formatTime(selectedSlots[0].startTime) }} - {{ formatTime(selectedSlots[selectedSlots.length - 1].endTime) }}
        </text>
        <text v-else>请选择时段</text>
      </view>
      <button class="submit-btn" @click="submitBooking" :disabled="selectedSlots.length === 0 || submitting">
        {{ submitting ? '提交中...' : '立即预约' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useVenue, useTimeSlots } from '@/composables/useVenue'
import { useBooking } from '@/composables/useBooking'
import { generateDateList, formatTime } from '@/utils/date'
import type { TimeSlot, Court } from '@/types'

const venueId = ref(0)
const courtId = ref(0)

const { currentVenue, loadVenueDetail } = useVenue()
const { slots, loading, loadSlots } = useTimeSlots()
const { submitting, submitBooking: doSubmitBooking } = useBooking()

const dateList = computed(() => generateDateList(7))
const selectedDate = ref('')
const selectedSlots = ref<TimeSlot[]>([])

const court = computed<Court | undefined>(() => 
  currentVenue.value?.courts?.find(c => c.id === courtId.value)
)

onLoad((options) => {
  venueId.value = Number(options?.venueId)
  courtId.value = Number(options?.courtId)
  initDateList()
  loadVenueDetail(venueId.value)
})

const initDateList = () => {
  selectedDate.value = dateList.value[0]?.value || ''
  loadSlotsData()
}

const loadSlotsData = async () => {
  await loadSlots(venueId.value, courtId.value, selectedDate.value)
  selectedSlots.value = []
}

const selectDate = (date: string) => {
  selectedDate.value = date
  loadSlotsData()
}

const toggleSlot = (slot: TimeSlot) => {
  if (slot.status !== 'free') return
  
  const index = selectedSlots.value.findIndex(s => s.startTime === slot.startTime)
  
  if (index > -1) {
    selectedSlots.value.splice(index, 1)
  } else {
    selectedSlots.value.push(slot)
    selectedSlots.value.sort((a, b) => a.startTime.localeCompare(b.startTime))
  }
}

const isSelected = (slot: TimeSlot): boolean => {
  return selectedSlots.value.some(s => s.startTime === slot.startTime)
}

const submitBooking = async () => {
  if (selectedSlots.value.length === 0 || submitting.value) return
  
  const bookingNo = await doSubmitBooking({
    venueId: venueId.value,
    courtId: courtId.value,
    bookingDate: selectedDate.value,
    startTime: selectedSlots.value[0].startTime,
    endTime: selectedSlots.value[selectedSlots.value.length - 1].endTime
  })
  
  if (bookingNo) {
    uni.showToast({ title: '预约成功', icon: 'success' })
    
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/booking-detail/booking-detail?bookingNo=${bookingNo}`
      })
    }, 1500)
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 120rpx;
}

.venue-info {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.venue-name {
  font-size: 36rpx;
  font-weight: bold;
  display: block;
}

.court-name {
  font-size: 28rpx;
  color: #666;
  margin-top: 10rpx;
  display: block;
}

.date-picker {
  background: #fff;
  padding: 20rpx 0;
  margin-bottom: 20rpx;
}

.date-scroll {
  white-space: nowrap;
  padding: 0 10rpx;
}

.date-item {
  display: inline-block;
  text-align: center;
  padding: 16rpx 24rpx;
  border-radius: 12rpx;
  margin: 0 6rpx;
}

.date-item.active {
  background: #1890ff;
  color: #fff;
}

.date-week {
  font-size: 24rpx;
  display: block;
}

.date-day {
  font-size: 32rpx;
  font-weight: bold;
  display: block;
  margin-top: 8rpx;
}

.slots-container {
  background: #fff;
  padding: 20rpx;
}

.slots-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.legend {
  font-size: 24rpx;
}

.legend-item {
  margin-left: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.legend-item.free {
  background: #e6f7ff;
  color: #1890ff;
}

.legend-item.occupied {
  background: #f5f5f5;
  color: #999;
}

.slots-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.slot-item {
  width: calc(25% - 12rpx);
  text-align: center;
  padding: 20rpx 0;
  border-radius: 12rpx;
  background: #f5f5f5;
}

.slot-item.free {
  background: #e6f7ff;
}

.slot-item.occupied {
  background: #f5f5f5;
  color: #999;
}

.slot-item.selected {
  background: #1890ff;
  color: #fff;
}

.slot-time {
  font-size: 26rpx;
}

.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.selected-info {
  flex: 1;
  font-size: 28rpx;
}

.submit-btn {
  background: #1890ff;
  color: #fff;
  font-size: 28rpx;
  padding: 20rpx 60rpx;
  border-radius: 40rpx;
  border: none;
}

.submit-btn[disabled] {
  background: #ccc;
}

.empty {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
}
</style>
