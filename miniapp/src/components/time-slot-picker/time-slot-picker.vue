<template>
  <view class="time-slot-picker">
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
    </view>
  </view>
</template>

<script>
export default {
  name: 'TimeSlotPicker',
  props: {
    slots: {
      type: Array,
      default: () => []
    },
    selectedDate: {
      type: String,
      default: ''
    },
    selectedSlots: {
      type: Array,
      default: () => []
    },
    daysAhead: {
      type: Number,
      default: 7
    }
  },
  computed: {
    dateList() {
      const dates = []
      const weekDays = ['日', '一', '二', '三', '四', '五', '六']
      const today = new Date()
      
      for (let i = 0; i < this.daysAhead; i++) {
        const date = new Date(today)
        date.setDate(today.getDate() + i)
        dates.push({
          value: this.formatDate(date),
          week: i === 0 ? '今天' : '周' + weekDays[date.getDay()],
          day: date.getDate()
        })
      }
      
      return dates
    }
  },
  methods: {
    selectDate(date) {
      this.$emit('date-change', date)
    },
    
    toggleSlot(slot) {
      if (slot.status !== 'free') return
      this.$emit('slot-toggle', slot)
    },
    
    isSelected(slot) {
      return this.selectedSlots.some(s => s.startTime === slot.startTime)
    },
    
    formatDate(date) {
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    },
    
    formatTime(time) {
      return time.substring(0, 5)
    }
  }
}
</script>

<style scoped>
.time-slot-picker {
  background: #fff;
}

.date-picker {
  padding: 20rpx 0;
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
</style>
