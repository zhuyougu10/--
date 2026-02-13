# Task 09: 小程序端开发

> **依赖:** task-03-auth-module.md, task-04-venue-management.md, task-05-booking-core.md
> **预计时间:** 8-10 小时

## 目标
使用 uni-app 开发微信小程序端，实现用户登录、球馆浏览、预约管理、核销码展示等功能。

---

## 项目结构

```
miniapp/
├── pages/
│   ├── index/              # 首页
│   ├── venue/              # 球馆列表
│   ├── venue-detail/       # 球馆详情
│   ├── booking/            # 预约页面
│   ├── booking-detail/     # 预约详情
│   ├── my/                 # 我的
│   └── my-bookings/        # 我的预约
├── components/
│   ├── time-slot-picker/   # 时段选择器
│   ├── venue-card/         # 球馆卡片
│   └── booking-card/       # 预约卡片
├── api/
│   ├── index.js            # API 统一出口
│   ├── auth.js             # 认证接口
│   ├── venue.js            # 球馆接口
│   └── booking.js          # 预约接口
├── utils/
│   ├── request.js          # 请求封装
│   └── auth.js             # 认证工具
├── static/
├── App.vue
├── main.js
├── pages.json
├── manifest.json
└── uni.scss
```

---

## Step 1: 初始化项目

**命令:**
```bash
npx degit dcloudio/uni-preset-vue#vite miniapp
cd miniapp
npm install
```

**文件:** `miniapp/manifest.json`

```json
{
  "name": "球馆预约",
  "appid": "__UNI__XXXXXXX",
  "description": "校园球馆智能预约系统",
  "versionName": "1.0.0",
  "versionCode": "100",
  "transformPx": false,
  "mp-weixin": {
    "appid": "your_wechat_appid",
    "setting": {
      "urlCheck": false,
      "es6": true,
      "minified": true
    },
    "usingComponents": true,
    "permission": {
      "scope.userLocation": {
        "desc": "获取位置信息用于显示附近球馆"
      }
    }
  }
}
```

**文件:** `miniapp/pages.json`

```json
{
  "pages": [
    {
      "path": "pages/index/index",
      "style": {
        "navigationBarTitleText": "首页"
      }
    },
    {
      "path": "pages/venue/venue",
      "style": {
        "navigationBarTitleText": "球馆列表"
      }
    },
    {
      "path": "pages/venue-detail/venue-detail",
      "style": {
        "navigationBarTitleText": "球馆详情"
      }
    },
    {
      "path": "pages/booking/booking",
      "style": {
        "navigationBarTitleText": "预约"
      }
    },
    {
      "path": "pages/booking-detail/booking-detail",
      "style": {
        "navigationBarTitleText": "预约详情"
      }
    },
    {
      "path": "pages/my/my",
      "style": {
        "navigationBarTitleText": "我的"
      }
    },
    {
      "path": "pages/my-bookings/my-bookings",
      "style": {
        "navigationBarTitleText": "我的预约"
      }
    }
  ],
  "globalStyle": {
    "navigationBarTextStyle": "black",
    "navigationBarTitleText": "球馆预约",
    "navigationBarBackgroundColor": "#ffffff",
    "backgroundColor": "#f5f5f5"
  },
  "tabBar": {
    "color": "#999999",
    "selectedColor": "#1890ff",
    "backgroundColor": "#ffffff",
    "list": [
      {
        "pagePath": "pages/index/index",
        "text": "首页",
        "iconPath": "static/tab/home.png",
        "selectedIconPath": "static/tab/home-active.png"
      },
      {
        "pagePath": "pages/venue/venue",
        "text": "球馆",
        "iconPath": "static/tab/venue.png",
        "selectedIconPath": "static/tab/venue-active.png"
      },
      {
        "pagePath": "pages/my/my",
        "text": "我的",
        "iconPath": "static/tab/my.png",
        "selectedIconPath": "static/tab/my-active.png"
      }
    ]
  }
}
```

---

## Step 2: 创建请求封装

**文件:** `miniapp/utils/request.js`

```javascript
const BASE_URL = 'http://localhost:8080/api'

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data.data)
          } else if (res.data.code === 401) {
            uni.removeStorageSync('token')
            uni.reLaunch({ url: '/pages/my/my' })
            reject(new Error('未登录'))
          } else {
            uni.showToast({
              title: res.data.message || '请求失败',
              icon: 'none'
            })
            reject(new Error(res.data.message))
          }
        } else {
          uni.showToast({
            title: '网络错误',
            icon: 'none'
          })
          reject(new Error('网络错误'))
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

export default request
```

---

## Step 3: 创建 API 模块

**文件:** `miniapp/api/auth.js`

```javascript
import request from '@/utils/request'

export const wechatLogin = (code) => {
  return request({
    url: '/api/auth/wechat/login',
    method: 'POST',
    data: { code }
  })
}

export const updateUserProfile = (data) => {
  return request({
    url: '/api/user/profile',
    method: 'PUT',
    data
  })
}

export const getUserProfile = () => {
  return request({
    url: '/api/user/profile'
  })
}
```

**文件:** `miniapp/api/venue.js`

```javascript
import request from '@/utils/request'

export const getVenueList = () => {
  return request({
    url: '/api/venues'
  })
}

export const getVenueDetail = (id) => {
  return request({
    url: `/api/venues/${id}`
  })
}

export const getTimeSlots = (venueId, courtId, date) => {
  return request({
    url: `/api/venues/${venueId}/courts/${courtId}/slots`,
    data: { date }
  })
}
```

**文件:** `miniapp/api/booking.js`

```javascript
import request from '@/utils/request'

export const createBooking = (data) => {
  return request({
    url: '/api/bookings',
    method: 'POST',
    data
  })
}

export const cancelBooking = (bookingNo, reason) => {
  return request({
    url: `/api/bookings/${bookingNo}/cancel`,
    method: 'POST',
    data: { reason }
  })
}

export const getBookingDetail = (bookingNo) => {
  return request({
    url: `/api/bookings/${bookingNo}`
  })
}

export const getMyBookings = () => {
  return request({
    url: '/api/bookings/my'
  })
}

export const getQrCode = (bookingNo) => {
  return request({
    url: `/api/qrcode/booking/${bookingNo}`
  })
}

export const getRecommendations = (data) => {
  return request({
    url: '/api/recommendations',
    method: 'POST',
    data
  })
}
```

---

## Step 4: 创建首页

**文件:** `miniapp/pages/index/index.vue`

```vue
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
        <image class="venue-image" :src="venue.imageUrl || '/static/default-venue.png'" mode="aspectFill" />
        <view class="venue-info">
          <text class="venue-name">{{ venue.name }}</text>
          <text class="venue-location">{{ venue.location }}</text>
          <view class="venue-meta">
            <text class="sport-type">{{ getSportTypeName(venue.sportType) }}</text>
            <text class="open-time">{{ venue.openTime }} - {{ venue.closeTime }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getVenueList } from '@/api/venue'

export default {
  data() {
    return {
      venues: []
    }
  },
  onShow() {
    this.loadVenues()
  },
  methods: {
    async loadVenues() {
      try {
        this.venues = await getVenueList()
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
</style>
```

---

## Step 5: 创建预约页面

**文件:** `miniapp/pages/booking/booking.vue`

```vue
<template>
  <view class="container">
    <view class="venue-info">
      <text class="venue-name">{{ venue.name }}</text>
      <text class="court-name">{{ court.name }}</text>
    </view>
    
    <view class="date-picker">
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
    </view>
    
    <view class="slots-container">
      <view class="slots-header">
        <text>选择时段</text>
        <text class="legend">
          <text class="legend-item free">可预约</text>
          <text class="legend-item occupied">已占用</text>
        </text>
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
    
    <view class="footer">
      <view class="selected-info">
        <text v-if="selectedSlots.length > 0">
          已选: {{ formatTime(selectedSlots[0].startTime) }} - {{ formatTime(selectedSlots[selectedSlots.length - 1].endTime) }}
        </text>
        <text v-else>请选择时段</text>
      </view>
      <button class="submit-btn" @click="submitBooking" :disabled="selectedSlots.length === 0">
        立即预约
      </button>
    </view>
  </view>
</template>

<script>
import { getVenueDetail, getTimeSlots } from '@/api/venue'
import { createBooking } from '@/api/booking'

export default {
  data() {
    return {
      venueId: null,
      courtId: null,
      venue: {},
      court: {},
      dateList: [],
      selectedDate: '',
      slots: [],
      selectedSlots: []
    }
  },
  onLoad(options) {
    this.venueId = options.venueId
    this.courtId = options.courtId
    this.initDateList()
    this.loadVenue()
  },
  methods: {
    async loadVenue() {
      this.venue = await getVenueDetail(this.venueId)
      this.court = this.venue.courts?.find(c => c.id == this.courtId) || {}
    },
    
    initDateList() {
      const dates = []
      const weekDays = ['日', '一', '二', '三', '四', '五', '六']
      const today = new Date()
      
      for (let i = 0; i < 7; i++) {
        const date = new Date(today)
        date.setDate(today.getDate() + i)
        dates.push({
          value: this.formatDate(date),
          week: i === 0 ? '今天' : '周' + weekDays[date.getDay()],
          day: date.getDate()
        })
      }
      
      this.dateList = dates
      this.selectedDate = dates[0].value
      this.loadSlots()
    },
    
    async loadSlots() {
      try {
        this.slots = await getTimeSlots(this.venueId, this.courtId, this.selectedDate)
        this.selectedSlots = []
      } catch (e) {
        console.error(e)
      }
    },
    
    selectDate(date) {
      this.selectedDate = date
      this.loadSlots()
    },
    
    toggleSlot(slot) {
      if (slot.status !== 'free') return
      
      const index = this.selectedSlots.findIndex(s => s.startTime === slot.startTime)
      
      if (index > -1) {
        this.selectedSlots.splice(index, 1)
      } else {
        this.selectedSlots.push(slot)
        this.selectedSlots.sort((a, b) => a.startTime.localeCompare(b.startTime))
      }
    },
    
    isSelected(slot) {
      return this.selectedSlots.some(s => s.startTime === slot.startTime)
    },
    
    async submitBooking() {
      if (this.selectedSlots.length === 0) return
      
      try {
        const result = await createBooking({
          venueId: this.venueId,
          courtId: this.courtId,
          bookingDate: this.selectedDate,
          startTime: this.selectedSlots[0].startTime,
          endTime: this.selectedSlots[this.selectedSlots.length - 1].endTime
        })
        
        uni.showToast({ title: '预约成功', icon: 'success' })
        
        setTimeout(() => {
          uni.redirectTo({
            url: `/pages/booking-detail/booking-detail?bookingNo=${result.bookingNo}`
          })
        }, 1500)
      } catch (e) {
        console.error(e)
      }
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
  display: flex;
  background: #fff;
  padding: 20rpx 10rpx;
  margin-bottom: 20rpx;
}

.date-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  border-radius: 12rpx;
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
</style>
```

---

## Step 6: 创建预约详情页（含二维码）

**文件:** `miniapp/pages/booking-detail/booking-detail.vue`

```vue
<template>
  <view class="container">
    <view class="status-card" :class="statusClass">
      <text class="status-text">{{ statusText }}</text>
      <text class="booking-no">预约单号: {{ booking.bookingNo }}</text>
    </view>
    
    <view class="info-card">
      <view class="info-row">
        <text class="label">球馆</text>
        <text class="value">{{ booking.venueName }}</text>
      </view>
      <view class="info-row">
        <text class="label">场地</text>
        <text class="value">{{ booking.courtName }}</text>
      </view>
      <view class="info-row">
        <text class="label">日期</text>
        <text class="value">{{ booking.bookingDate }}</text>
      </view>
      <view class="info-row">
        <text class="label">时段</text>
        <text class="value">{{ booking.startTime }} - {{ booking.endTime }}</text>
      </view>
    </view>
    
    <view class="qr-section" v-if="booking.status === 1">
      <view class="qr-container">
        <image class="qr-code" :src="qrCodeUrl" mode="aspectFit" v-if="qrCodeUrl" />
        <view class="qr-placeholder" v-else>
          <text>点击获取核销码</text>
        </view>
      </view>
      <text class="qr-tip">请在核销窗口内向场馆员出示此码</text>
      <text class="qr-expire" v-if="qrExpireAt">
        有效期至: {{ formatDateTime(qrExpireAt) }}
      </text>
      <button class="refresh-btn" @click="refreshQrCode">刷新核销码</button>
    </view>
    
    <view class="actions" v-if="booking.status === 1">
      <button class="cancel-btn" @click="cancelBooking">取消预约</button>
    </view>
  </view>
</template>

<script>
import { getBookingDetail, getQrCode, cancelBooking as cancelBookingApi } from '@/api/booking'

export default {
  data() {
    return {
      bookingNo: '',
      booking: {},
      qrCodeUrl: '',
      qrExpireAt: null
    }
  },
  computed: {
    statusClass() {
      const classes = {
        1: 'confirmed',
        2: 'cancelled',
        3: 'checked-in',
        4: 'no-show'
      }
      return classes[this.booking.status] || ''
    },
    statusText() {
      const texts = {
        1: '已确认',
        2: '已取消',
        3: '已签到',
        4: '爽约'
      }
      return texts[this.booking.status] || '未知'
    }
  },
  onLoad(options) {
    this.bookingNo = options.bookingNo
    this.loadBooking()
  },
  onShow() {
    if (this.booking.status === 1) {
      this.refreshQrCode()
    }
  },
  methods: {
    async loadBooking() {
      try {
        this.booking = await getBookingDetail(this.bookingNo)
      } catch (e) {
        console.error(e)
      }
    },
    
    async refreshQrCode() {
      try {
        const result = await getQrCode(this.bookingNo)
        this.qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${result.token}`
        this.qrExpireAt = result.expiresAt
      } catch (e) {
        console.error(e)
      }
    },
    
    async cancelBooking() {
      uni.showModal({
        title: '确认取消',
        content: '确定要取消此预约吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              await cancelBookingApi(this.bookingNo, '用户取消')
              uni.showToast({ title: '取消成功', icon: 'success' })
              this.loadBooking()
            } catch (e) {
              console.error(e)
            }
          }
        }
      })
    },
    
    formatDateTime(datetime) {
      return datetime.replace('T', ' ').substring(0, 16)
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx;
}

.status-card {
  background: #1890ff;
  color: #fff;
  padding: 40rpx;
  border-radius: 16rpx;
  text-align: center;
  margin-bottom: 20rpx;
}

.status-card.cancelled {
  background: #999;
}

.status-card.checked-in {
  background: #52c41a;
}

.status-card.no-show {
  background: #ff4d4f;
}

.status-text {
  font-size: 40rpx;
  font-weight: bold;
  display: block;
}

.booking-no {
  font-size: 26rpx;
  opacity: 0.8;
  margin-top: 10rpx;
  display: block;
}

.info-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx 30rpx;
  margin-bottom: 20rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.info-row:last-child {
  border-bottom: none;
}

.label {
  color: #999;
  font-size: 28rpx;
}

.value {
  font-size: 28rpx;
  font-weight: 500;
}

.qr-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  text-align: center;
  margin-bottom: 20rpx;
}

.qr-container {
  width: 300rpx;
  height: 300rpx;
  margin: 0 auto 20rpx;
  border: 2rpx solid #f0f0f0;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qr-code {
  width: 260rpx;
  height: 260rpx;
}

.qr-placeholder {
  color: #999;
  font-size: 28rpx;
}

.qr-tip {
  font-size: 28rpx;
  color: #666;
  display: block;
}

.qr-expire {
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
  display: block;
}

.refresh-btn {
  margin-top: 30rpx;
  background: #f0f0f0;
  font-size: 26rpx;
  padding: 16rpx 40rpx;
  border-radius: 30rpx;
  border: none;
}

.actions {
  margin-top: 40rpx;
}

.cancel-btn {
  background: #fff;
  color: #ff4d4f;
  font-size: 28rpx;
  padding: 24rpx;
  border-radius: 40rpx;
  border: 1rpx solid #ff4d4f;
}
</style>
```

---

## Step 7: 验证项目

**运行小程序:**
```bash
cd miniapp
npm run dev:mp-weixin
```

**在微信开发者工具中打开:** `miniapp/dist/dev/mp-weixin`

---

## 提交

```bash
git add miniapp/
git commit -m "feat(miniapp): init uni-app mini program with booking features"
```

---

## 注意事项

1. **登录态管理**: 使用 uni.checkSession 检查登录态
2. **二维码生成**: 可使用微信小程序原生二维码 API
3. **性能优化**: 长列表使用虚拟列表
