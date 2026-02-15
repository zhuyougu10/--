# Vue 2 Options API to Vue 3 Composition API 迁移计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将 miniapp 端所有组件从 Vue 2 Options API 语法全面迁移至 Vue 3 Composition API (setup 语法糖)，提升代码可维护性、类型推断支持和开发体验。

**Architecture:** 采用渐进式迁移策略，按组件复杂度从低到高依次迁移。使用 `<script setup>` 语法糖配合 TypeScript，利用 Vue 3 响应式 API (ref、reactive、computed) 重构状态管理，使用生命周期钩子函数替代 Options API 钩子。

**Tech Stack:** Vue 3.5.28 + uni-app 3.x + Pinia 2.1.7 + TypeScript 5.4.5 + Vite 5.2.8

---

## 一、项目现状评估

### 1.1 技术栈版本

| 依赖 | 当前版本 | 状态 |
|------|----------|------|
| Vue | 3.5.28 | 已升级至 Vue 3 |
| uni-app | 3.0.0-4080720251210001 | Vue 3 版本 |
| Pinia | 2.1.7 | 已支持 Composition API |
| vue-i18n | 9.14.5 | 已支持 Vue 3 |
| TypeScript | 5.4.5 | 已配置 |
| Vite | 5.2.8 | 已配置 |

### 1.2 迁移范围统计

| 类型 | 文件数 | 说明 |
|------|--------|------|
| 页面组件 | 7 | index, venue, venue-detail, booking, booking-detail, my, my-bookings |
| 自定义组件 | 3 | venue-card, time-slot-picker, booking-card |
| 应用入口 | 1 | App.vue |
| API 模块 | 3 | auth.js, venue.js, booking.js (已是函数式，无需迁移) |
| 工具函数 | 1 | request.js (已是函数式，无需迁移) |

### 1.3 当前代码特征分析

**Options API 特征（需迁移）：**
- `data()` 返回响应式状态
- `computed` 计算属性对象
- `methods` 方法对象
- `onLoad`, `onShow` 等生命周期钩子
- `props` 属性定义
- `this.$emit` 事件触发

**已具备的 Vue 3 特性：**
- `createSSRApp` 入口函数
- Vite 构建工具
- Pinia 状态管理（未使用）

---

## 二、迁移范围界定

### 2.1 需要迁移的文件

#### 页面组件 (7个)

| 文件路径 | 复杂度 | 优先级 |
|----------|--------|--------|
| `src/pages/index/index.vue` | 低 | P1 |
| `src/pages/venue/venue.vue` | 中 | P2 |
| `src/pages/venue-detail/venue-detail.vue` | 低 | P1 |
| `src/pages/booking/booking.vue` | 高 | P3 |
| `src/pages/booking-detail/booking-detail.vue` | 高 | P3 |
| `src/pages/my/my.vue` | 中 | P2 |
| `src/pages/my-bookings/my-bookings.vue` | 中 | P2 |

#### 自定义组件 (3个)

| 文件路径 | 复杂度 | 优先级 |
|----------|--------|--------|
| `src/components/venue-card/venue-card.vue` | 低 | P1 |
| `src/components/time-slot-picker/time-slot-picker.vue` | 中 | P2 |
| `src/components/booking-card/booking-card.vue` | 低 | P1 |

#### 应用入口 (1个)

| 文件路径 | 复杂度 | 优先级 |
|----------|--------|--------|
| `src/App.vue` | 低 | P1 |

### 2.2 不需要迁移的文件

- `src/api/*.js` - 已是函数式模块导出
- `src/utils/request.js` - 已是函数式工具
- `src/main.js` - 已使用 Vue 3 API
- `src/pages.json` - 配置文件
- `src/manifest.json` - 配置文件
- `src/uni.scss` - 样式文件

---

## 三、技术栈适配分析

### 3.1 uni-app 生命周期钩子映射

| Options API | Composition API |
|-------------|-----------------|
| `onLoad(options)` | `onLoad((options) => {})` |
| `onShow()` | `onShow(() => {})` |
| `onHide()` | `onHide(() => {})` |
| `onReady()` | `onReady(() => {})` |
| `onPullDownRefresh()` | `onPullDownRefresh(() => {})` |
| `onReachBottom()` | `onReachBottom(() => {})` |

### 3.2 Vue 3 响应式 API 选择策略

| 场景 | 推荐API | 说明 |
|------|---------|------|
| 简单类型状态 | `ref()` | 自动解包，适合基础类型 |
| 对象状态 | `reactive()` | 适合复杂对象 |
| 派生状态 | `computed()` | 缓存计算结果 |
| 异步操作 | `async/await` + `ref()` | 配合 try-catch |
| Props | `defineProps<T>()` | TypeScript 类型推断 |
| Emits | `defineEmits<T>()` | TypeScript 类型推断 |

### 3.3 第三方库兼容性

| 库 | 当前版本 | Composition API 支持 | 迁移方案 |
|----|----------|---------------------|----------|
| Pinia | 2.1.7 | 完全支持 | 使用 `useStore()` 组合式函数 |
| vue-i18n | 9.14.5 | 完全支持 | 使用 `useI18n()` 组合式函数 |
| uni-app | 3.x | 完全支持 | 使用 `@dcloudio/uni-app` 导入钩子 |

---

## 四、分阶段实施步骤

### 阶段一：基础设施准备 (预计 0.5 天)

#### Task 1: 创建类型定义文件

**Files:**
- Create: `src/types/index.ts`
- Create: `src/types/venue.ts`
- Create: `src/types/booking.ts`
- Create: `src/types/user.ts`

**Step 1: 创建类型定义目录**

Run: `mkdir -p src/types`

**Step 2: 编写 Venue 类型定义**

```typescript
// src/types/venue.ts
export interface Venue {
  id: number
  name: string
  location: string
  sportType: 'badminton' | 'basketball' | 'table_tennis' | 'tennis'
  openTime: string
  closeTime: string
  imageUrl?: string
  phone?: string
  status: number
  courts?: Court[]
  courtCount?: number
}

export interface Court {
  id: number
  name: string
  indoor: boolean
  status: number
}

export interface TimeSlot {
  startTime: string
  endTime: string
  status: 'free' | 'occupied'
}
```

**Step 3: 编写 Booking 类型定义**

```typescript
// src/types/booking.ts
export interface Booking {
  bookingNo: string
  venueId: number
  venueName: string
  courtId: number
  courtName: string
  bookingDate: string
  startTime: string
  endTime: string
  status: BookingStatus
  createdAt: string
  cancelledAt?: string
  cancelReason?: string
  checkinTime?: string
}

export type BookingStatus = 1 | 2 | 3 | 4

export interface CreateBookingParams {
  venueId: number
  courtId: number
  bookingDate: string
  startTime: string
  endTime: string
}
```

**Step 4: 编写 User 类型定义**

```typescript
// src/types/user.ts
export interface User {
  id: number
  nickname?: string
  avatar?: string
  userType: 'student' | 'teacher' | 'staff'
}

export interface LoginResult {
  token: string
  user: User
}
```

**Step 5: 编写类型索引文件**

```typescript
// src/types/index.ts
export * from './venue'
export * from './booking'
export * from './user'
```

**Step 6: Commit**

```bash
git add src/types/
git commit -m "feat: add TypeScript type definitions for venue, booking and user"
```

---

#### Task 2: 创建组合式函数 (Composables)

**Files:**
- Create: `src/composables/useVenue.ts`
- Create: `src/composables/useBooking.ts`
- Create: `src/composables/useAuth.ts`

**Step 1: 创建 composables 目录**

Run: `mkdir -p src/composables`

**Step 2: 编写 useVenue 组合式函数**

```typescript
// src/composables/useVenue.ts
import { ref } from 'vue'
import { getVenueList, getVenueDetail, getTimeSlots } from '@/api/venue'
import type { Venue, TimeSlot } from '@/types'

export function useVenue() {
  const venues = ref<Venue[]>([])
  const currentVenue = ref<Venue | null>(null)
  const loading = ref(false)

  const loadVenues = async () => {
    loading.value = true
    try {
      venues.value = await getVenueList()
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  const loadVenueDetail = async (id: number) => {
    loading.value = true
    try {
      currentVenue.value = await getVenueDetail(id)
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  return {
    venues,
    currentVenue,
    loading,
    loadVenues,
    loadVenueDetail
  }
}

export function useTimeSlots() {
  const slots = ref<TimeSlot[]>([])
  const loading = ref(false)

  const loadSlots = async (venueId: number, courtId: number, date: string) => {
    loading.value = true
    try {
      slots.value = await getTimeSlots(venueId, courtId, date)
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  return {
    slots,
    loading,
    loadSlots
  }
}

export function useSportType() {
  const sportTypeMap: Record<string, string> = {
    badminton: '羽毛球',
    basketball: '篮球',
    table_tennis: '乒乓球',
    tennis: '网球'
  }

  const getSportTypeName = (type: string): string => {
    return sportTypeMap[type] || type
  }

  return {
    getSportTypeName
  }
}
```

**Step 3: 编写 useBooking 组合式函数**

```typescript
// src/composables/useBooking.ts
import { ref, computed } from 'vue'
import { 
  createBooking, 
  getBookingDetail, 
  getMyBookings, 
  cancelBooking as cancelBookingApi,
  getQrCode 
} from '@/api/booking'
import type { Booking, CreateBookingParams } from '@/types'

export function useBooking() {
  const bookings = ref<Booking[]>([])
  const currentBooking = ref<Booking | null>(null)
  const loading = ref(false)
  const submitting = ref(false)

  const loadBookings = async (status?: number) => {
    loading.value = true
    try {
      bookings.value = await getMyBookings(status ?? null)
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  const loadBookingDetail = async (bookingNo: string) => {
    loading.value = true
    try {
      currentBooking.value = await getBookingDetail(bookingNo)
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  const submitBooking = async (params: CreateBookingParams): Promise<string | null> => {
    submitting.value = true
    try {
      const result = await createBooking(params)
      return result.bookingNo
    } catch (e) {
      console.error(e)
      return null
    } finally {
      submitting.value = false
    }
  }

  const cancelBooking = async (bookingNo: string, reason: string): Promise<boolean> => {
    try {
      await cancelBookingApi(bookingNo, reason)
      return true
    } catch (e) {
      console.error(e)
      return false
    }
  }

  return {
    bookings,
    currentBooking,
    loading,
    submitting,
    loadBookings,
    loadBookingDetail,
    submitBooking,
    cancelBooking
  }
}

export function useQrCode() {
  const qrCodeUrl = ref('')
  const qrExpireAt = ref<string | null>(null)

  const refreshQrCode = async (bookingNo: string) => {
    try {
      const result = await getQrCode(bookingNo)
      qrCodeUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(result.token)}`
      qrExpireAt.value = result.expiresAt
    } catch (e) {
      console.error(e)
    }
  }

  return {
    qrCodeUrl,
    qrExpireAt,
    refreshQrCode
  }
}

export function useBookingStatus() {
  const statusTextMap: Record<number, string> = {
    1: '待使用',
    2: '已取消',
    3: '已完成',
    4: '爽约'
  }

  const statusClassMap: Record<number, string> = {
    1: 'pending',
    2: 'cancelled',
    3: 'completed',
    4: 'no-show'
  }

  const getStatusText = (status: number): string => {
    return statusTextMap[status] || '未知'
  }

  const getStatusClass = (status: number): string => {
    return statusClassMap[status] || ''
  }

  return {
    getStatusText,
    getStatusClass
  }
}
```

**Step 4: 编写 useAuth 组合式函数**

```typescript
// src/composables/useAuth.ts
import { ref, computed } from 'vue'
import { wechatLogin, getUserProfile } from '@/api/auth'
import type { User, LoginResult } from '@/types'

export function useAuth() {
  const token = ref(uni.getStorageSync('token') || '')
  const userInfo = ref<User | null>(uni.getStorageSync('userInfo') || null)

  const isLoggedIn = computed(() => !!token.value)

  const checkLogin = () => {
    token.value = uni.getStorageSync('token') || ''
    userInfo.value = uni.getStorageSync('userInfo') || null
  }

  const login = async (): Promise<boolean> => {
    try {
      const loginRes = await new Promise<UniApp.LoginRes>((resolve, reject) => {
        uni.login({
          provider: 'weixin',
          success: resolve,
          fail: reject
        })
      })

      const result: LoginResult = await wechatLogin(loginRes.code)
      
      token.value = result.token
      userInfo.value = result.user
      
      uni.setStorageSync('token', result.token)
      uni.setStorageSync('userInfo', result.user)
      
      uni.showToast({ title: '登录成功', icon: 'success' })
      return true
    } catch (e) {
      console.error(e)
      uni.showToast({ title: '登录失败', icon: 'none' })
      return false
    }
  }

  const logout = () => {
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    token.value = ''
    userInfo.value = null
    uni.showToast({ title: '已退出', icon: 'success' })
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    checkLogin,
    login,
    logout
  }
}

export function useUserType() {
  const userTypeMap: Record<string, string> = {
    student: '学生',
    teacher: '教师',
    staff: '教职工'
  }

  const getUserTypeName = (type: string): string => {
    return userTypeMap[type] || '用户'
  }

  return {
    getUserTypeName
  }
}
```

**Step 5: Commit**

```bash
git add src/composables/
git commit -m "feat: add composables for venue, booking and auth"
```

---

#### Task 3: 创建工具函数

**Files:**
- Create: `src/utils/date.ts`
- Create: `src/utils/format.ts`

**Step 1: 编写日期工具函数**

```typescript
// src/utils/date.ts
export const formatDate = (date: Date): string => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export const formatTime = (time: string): string => {
  return time.substring(0, 5)
}

export const formatDateTime = (datetime: string | null | undefined): string => {
  if (!datetime) return ''
  return datetime.replace('T', ' ').substring(0, 19)
}

export const generateDateList = (daysAhead: number = 7) => {
  const dates: Array<{ value: string; week: string; day: number }> = []
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  const today = new Date()

  for (let i = 0; i < daysAhead; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    dates.push({
      value: formatDate(date),
      week: i === 0 ? '今天' : '周' + weekDays[date.getDay()],
      day: date.getDate()
    })
  }

  return dates
}

export const isVenueOpen = (venue: { status: number; openTime: string; closeTime: string }): boolean => {
  if (venue.status !== 1) return false
  const now = new Date()
  const currentTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
  return currentTime >= venue.openTime && currentTime <= venue.closeTime
}
```

**Step 2: Commit**

```bash
git add src/utils/date.ts
git commit -m "feat: add date utility functions"
```

---

### 阶段二：简单组件迁移 (预计 1 天)

#### Task 4: 迁移 App.vue

**Files:**
- Modify: `src/App.vue`

**Step 1: 迁移 App.vue 到 Composition API**

```vue
<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('App Launch')
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style>
page {
  background-color: #f5f5f5;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.container {
  padding: 20rpx;
  min-height: 100vh;
}

.btn-primary {
  background: #1890ff;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  padding: 20rpx 40rpx;
}

.btn-primary:active {
  background: #096dd9;
}

.text-primary {
  color: #1890ff;
}

.text-muted {
  color: #999;
}

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}
</style>
```

**Step 2: Commit**

```bash
git add src/App.vue
git commit -m "refactor: migrate App.vue to Composition API"
```

---

#### Task 5: 迁移 venue-card 组件

**Files:**
- Modify: `src/components/venue-card/venue-card.vue`

**Step 1: 迁移 venue-card 组件**

```vue
<template>
  <view class="venue-card" @click="handleClick">
    <image class="venue-image" :src="venue.imageUrl || '/static/default-venue.svg'" mode="aspectFill" />
    <view class="venue-info">
      <text class="venue-name">{{ venue.name }}</text>
      <text class="venue-location">{{ venue.location }}</text>
      <view class="venue-meta">
        <text class="sport-type">{{ getSportTypeName(venue.sportType) }}</text>
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

const isOpen = computed(() => isVenueOpen(props.venue))

const handleClick = () => {
  emit('click', props.venue)
}
</script>

<style scoped>
/* 样式保持不变 */
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
  margin-top: 12rpx;
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
```

**Step 2: Commit**

```bash
git add src/components/venue-card/venue-card.vue
git commit -m "refactor: migrate venue-card to Composition API with TypeScript"
```

---

#### Task 6: 迁移 booking-card 组件

**Files:**
- Modify: `src/components/booking-card/booking-card.vue`

**Step 1: 先读取现有文件确认内容**

Run: `cat src/components/booking-card/booking-card.vue`

**Step 2: 迁移 booking-card 组件**

```vue
<template>
  <view class="booking-card" @click="handleClick">
    <view class="booking-header">
      <text class="venue-name">{{ booking.venueName }}</text>
      <text class="status-tag" :class="getStatusClass(booking.status)">
        {{ getStatusText(booking.status) }}
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
    
    <view class="booking-footer">
      <text class="booking-no">{{ booking.bookingNo }}</text>
      <text class="arrow">></text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useBookingStatus } from '@/composables/useBooking'
import type { Booking } from '@/types'

const props = defineProps<{
  booking: Booking
}>()

const emit = defineEmits<{
  click: [bookingNo: string]
}>()

const { getStatusText, getStatusClass } = useBookingStatus()

const handleClick = () => {
  emit('click', props.booking.bookingNo)
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
```

**Step 3: Commit**

```bash
git add src/components/booking-card/booking-card.vue
git commit -m "refactor: migrate booking-card to Composition API with TypeScript"
```

---

#### Task 7: 迁移 time-slot-picker 组件

**Files:**
- Modify: `src/components/time-slot-picker/time-slot-picker.vue`

**Step 1: 迁移 time-slot-picker 组件**

```vue
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

<script setup lang="ts">
import { computed } from 'vue'
import { generateDateList, formatTime } from '@/utils/date'
import type { TimeSlot } from '@/types'

const props = withDefaults(defineProps<{
  slots: TimeSlot[]
  selectedDate: string
  selectedSlots: TimeSlot[]
  daysAhead?: number
}>(), {
  slots: () => [],
  selectedDate: '',
  selectedSlots: () => [],
  daysAhead: 7
})

const emit = defineEmits<{
  'date-change': [date: string]
  'slot-toggle': [slot: TimeSlot]
}>()

const dateList = computed(() => generateDateList(props.daysAhead))

const selectDate = (date: string) => {
  emit('date-change', date)
}

const toggleSlot = (slot: TimeSlot) => {
  if (slot.status !== 'free') return
  emit('slot-toggle', slot)
}

const isSelected = (slot: TimeSlot): boolean => {
  return props.selectedSlots.some(s => s.startTime === slot.startTime)
}
</script>

<style scoped>
/* 样式保持不变 */
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
```

**Step 2: Commit**

```bash
git add src/components/time-slot-picker/time-slot-picker.vue
git commit -m "refactor: migrate time-slot-picker to Composition API with TypeScript"
```

---

### 阶段三：页面组件迁移 (预计 2 天)

#### Task 8: 迁移 index 页面

**Files:**
- Modify: `src/pages/index/index.vue`

**Step 1: 迁移 index 页面到 Composition API**

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

<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app'
import { useVenue, useSportType } from '@/composables/useVenue'

const { venues, loading, loadVenues } = useVenue()
const { getSportTypeName } = useSportType()

onShow(() => {
  loadVenues()
})

const goToVenue = (id: number) => {
  uni.navigateTo({
    url: `/pages/venue-detail/venue-detail?id=${id}`
  })
}
</script>

<style scoped>
/* 样式保持不变 */
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
```

**Step 2: Commit**

```bash
git add src/pages/index/index.vue
git commit -m "refactor: migrate index page to Composition API"
```

---

#### Task 9: 迁移 venue-detail 页面

**Files:**
- Modify: `src/pages/venue-detail/venue-detail.vue`

**Step 1: 迁移 venue-detail 页面**

```vue
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
        <text class="value">{{ getSportTypeName(currentVenue?.sportType || '') }}</text>
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
        <text class="value" :class="{ 'text-success': currentVenue?.status === 1 }">
          {{ currentVenue?.status === 1 ? '营业中' : '已闭馆' }}
        </text>
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
      
      <view class="empty" v-if="!currentVenue?.courts || currentVenue.courts.length === 0">
        <text>暂无场地</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useVenue, useSportType } from '@/composables/useVenue'
import type { Court } from '@/types'

const venueId = ref<number>(0)
const { currentVenue, loadVenueDetail } = useVenue()
const { getSportTypeName } = useSportType()

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
/* 样式保持不变 */
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
```

**Step 2: Commit**

```bash
git add src/pages/venue-detail/venue-detail.vue
git commit -m "refactor: migrate venue-detail page to Composition API"
```

---

#### Task 10: 迁移 venue 页面

**Files:**
- Modify: `src/pages/venue/venue.vue`

**Step 1: 迁移 venue 页面**

```vue
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
            <text class="sport-type">{{ getSportTypeName(venue.sportType) }}</text>
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

const keyword = ref('')
const activeType = ref('')

const filteredVenues = computed(() => {
  let result = venues.value
  if (activeType.value) {
    result = result.filter(v => v.sportType === activeType.value)
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
/* 样式保持不变 */
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
```

**Step 2: Commit**

```bash
git add src/pages/venue/venue.vue
git commit -m "refactor: migrate venue page to Composition API"
```

---

#### Task 11: 迁移 my 页面

**Files:**
- Modify: `src/pages/my/my.vue`

**Step 1: 迁移 my 页面**

```vue
<template>
  <view class="container">
    <view class="user-card" v-if="isLoggedIn">
      <view class="user-info">
        <image class="avatar" :src="userInfo?.avatar || '/static/default-avatar.svg'" mode="aspectFill" />
        <view class="user-detail">
          <text class="nickname">{{ userInfo?.nickname || '用户' }}</text>
          <text class="user-type">{{ getUserTypeName(userInfo?.userType || '') }}</text>
        </view>
      </view>
    </view>
    
    <view class="login-card" v-else>
      <button class="login-btn" @click="handleLogin">
        <text class="login-text">微信登录</text>
      </button>
      <text class="login-tip">登录后可使用预约功能</text>
    </view>
    
    <view class="menu-section">
      <view class="menu-item" @click="goToBookings('all')">
        <view class="menu-icon">
          <text class="iconfont">📋</text>
        </view>
        <text class="menu-text">我的预约</text>
        <text class="menu-arrow">></text>
      </view>
      
      <view class="menu-item" @click="goToBookings(1)">
        <view class="menu-icon">
          <text class="iconfont">✅</text>
        </view>
        <text class="menu-text">待使用</text>
        <text class="menu-badge" v-if="pendingCount > 0">{{ pendingCount }}</text>
        <text class="menu-arrow">></text>
      </view>
      
      <view class="menu-item" @click="goToBookings(3)">
        <view class="menu-icon">
          <text class="iconfont">✓</text>
        </view>
        <text class="menu-text">已完成</text>
        <text class="menu-arrow">></text>
      </view>
    </view>
    
    <view class="menu-section">
      <view class="menu-item" @click="showAbout">
        <view class="menu-icon">
          <text class="iconfont">ℹ️</text>
        </view>
        <text class="menu-text">关于我们</text>
        <text class="menu-arrow">></text>
      </view>
      
      <view class="menu-item" @click="showRules">
        <view class="menu-icon">
          <text class="iconfont">📖</text>
        </view>
        <text class="menu-text">预约须知</text>
        <text class="menu-arrow">></text>
      </view>
    </view>
    
    <view class="logout-section" v-if="isLoggedIn">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuth, useUserType } from '@/composables/useAuth'
import { useBooking } from '@/composables/useBooking'

const { isLoggedIn, userInfo, checkLogin, login, logout } = useAuth()
const { getUserTypeName } = useUserType()
const { loadBookings, bookings } = useBooking()

const pendingCount = ref(0)

onShow(() => {
  checkLogin()
  if (isLoggedIn.value) {
    loadPendingCount()
  }
})

const loadPendingCount = async () => {
  await loadBookings(1)
  pendingCount.value = bookings.value.length || 0
}

const handleLogin = async () => {
  const success = await login()
  if (success) {
    loadPendingCount()
  }
}

const goToBookings = (status: 'all' | number) => {
  if (!isLoggedIn.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: `/pages/my-bookings/my-bookings?status=${status}`
  })
}

const showAbout = () => {
  uni.showModal({
    title: '关于我们',
    content: '校园球馆智能预约系统\n版本: 1.0.0',
    showCancel: false
  })
}

const showRules = () => {
  uni.showModal({
    title: '预约须知',
    content: '1. 每人每天最多预约2个时段\n2. 预约开始前30分钟不可取消\n3. 爽约3次将被限制预约7天\n4. 请在预约时段开始前15分钟内核销签到',
    showCancel: false
  })
}

const handleLogout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        logout()
        pendingCount.value = 0
      }
    }
  })
}
</script>

<style scoped>
/* 样式保持不变 */
.container {
  min-height: 100vh;
  background: #f5f5f5;
}

.user-card {
  background: linear-gradient(135deg, #1890ff, #36cfc9);
  padding: 60rpx 30rpx;
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
}

.user-detail {
  margin-left: 30rpx;
}

.nickname {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  display: block;
}

.user-type {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 8rpx;
  display: block;
}

.login-card {
  background: linear-gradient(135deg, #1890ff, #36cfc9);
  padding: 80rpx 30rpx;
  text-align: center;
}

.login-btn {
  background: #fff;
  color: #1890ff;
  font-size: 32rpx;
  padding: 24rpx 80rpx;
  border-radius: 50rpx;
  border: none;
}

.login-text {
  font-weight: bold;
}

.login-tip {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 20rpx;
  display: block;
}

.menu-section {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  width: 48rpx;
  text-align: center;
}

.iconfont {
  font-size: 36rpx;
}

.menu-text {
  flex: 1;
  font-size: 30rpx;
  margin-left: 20rpx;
}

.menu-badge {
  background: #ff4d4f;
  color: #fff;
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  margin-right: 16rpx;
}

.menu-arrow {
  color: #ccc;
  font-size: 28rpx;
}

.logout-section {
  padding: 60rpx 30rpx;
}

.logout-btn {
  background: #fff;
  color: #ff4d4f;
  font-size: 30rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  border: 2rpx solid #ff4d4f;
}
</style>
```

**Step 2: Commit**

```bash
git add src/pages/my/my.vue
git commit -m "refactor: migrate my page to Composition API"
```

---

#### Task 12: 迁移 my-bookings 页面

**Files:**
- Modify: `src/pages/my-bookings/my-bookings.vue`

**Step 1: 迁移 my-bookings 页面**

```vue
<template>
  <view class="container">
    <view class="tabs">
      <view 
        class="tab-item" 
        :class="{ active: activeStatus === 'all' }"
        @click="changeStatus('all')"
      >
        全部
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeStatus === 1 }"
        @click="changeStatus(1)"
      >
        待使用
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeStatus === 3 }"
        @click="changeStatus(3)"
      >
        已完成
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeStatus === 2 }"
        @click="changeStatus(2)"
      >
        已取消
      </view>
    </view>
    
    <view class="booking-list">
      <view 
        class="booking-card" 
        v-for="booking in bookings" 
        :key="booking.bookingNo"
        @click="goToDetail(booking.bookingNo)"
      >
        <view class="booking-header">
          <text class="venue-name">{{ booking.venueName }}</text>
          <text class="status-tag" :class="getStatusClass(booking.status)">
            {{ getStatusText(booking.status) }}
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
        
        <view class="booking-footer">
          <text class="booking-no">{{ booking.bookingNo }}</text>
          <text class="arrow">></text>
        </view>
      </view>
      
      <view class="empty" v-if="bookings.length === 0 && !loading">
        <text>暂无预约记录</text>
      </view>
      
      <view class="loading" v-if="loading">
        <text>加载中...</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useBooking, useBookingStatus } from '@/composables/useBooking'

const { bookings, loading, loadBookings } = useBooking()
const { getStatusText, getStatusClass } = useBookingStatus()

const activeStatus = ref<'all' | number>('all')

onLoad((options) => {
  if (options?.status) {
    activeStatus.value = options.status === 'all' ? 'all' : parseInt(options.status)
  }
  loadBookingsData()
})

onShow(() => {
  loadBookingsData()
})

const loadBookingsData = async () => {
  const status = activeStatus.value === 'all' ? undefined : activeStatus.value as number
  await loadBookings(status)
}

const changeStatus = (status: 'all' | number) => {
  activeStatus.value = status
  loadBookingsData()
}

const goToDetail = (bookingNo: string) => {
  uni.navigateTo({
    url: `/pages/booking-detail/booking-detail?bookingNo=${bookingNo}`
  })
}
</script>

<style scoped>
/* 样式保持不变 */
.container {
  min-height: 100vh;
  background: #f5f5f5;
}

.tabs {
  display: flex;
  background: #fff;
  padding: 20rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  font-size: 28rpx;
  color: #666;
  border-radius: 8rpx;
}

.tab-item.active {
  background: #e6f7ff;
  color: #1890ff;
  font-weight: bold;
}

.booking-list {
  padding: 20rpx;
}

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

.empty, .loading {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
}
</style>
```

**Step 2: Commit**

```bash
git add src/pages/my-bookings/my-bookings.vue
git commit -m "refactor: migrate my-bookings page to Composition API"
```

---

#### Task 13: 迁移 booking 页面

**Files:**
- Modify: `src/pages/booking/booking.vue`

**Step 1: 迁移 booking 页面**

```vue
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
/* 样式保持不变 */
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
```

**Step 2: Commit**

```bash
git add src/pages/booking/booking.vue
git commit -m "refactor: migrate booking page to Composition API"
```

---

#### Task 14: 迁移 booking-detail 页面

**Files:**
- Modify: `src/pages/booking-detail/booking-detail.vue`

**Step 1: 迁移 booking-detail 页面**

```vue
<template>
  <view class="container">
    <view class="status-card" :class="statusClass">
      <text class="status-text">{{ statusText }}</text>
      <text class="booking-no">预约单号: {{ currentBooking?.bookingNo }}</text>
    </view>
    
    <view class="info-card">
      <view class="info-row">
        <text class="label">球馆</text>
        <text class="value">{{ currentBooking?.venueName }}</text>
      </view>
      <view class="info-row">
        <text class="label">场地</text>
        <text class="value">{{ currentBooking?.courtName }}</text>
      </view>
      <view class="info-row">
        <text class="label">日期</text>
        <text class="value">{{ currentBooking?.bookingDate }}</text>
      </view>
      <view class="info-row">
        <text class="label">时段</text>
        <text class="value">{{ currentBooking?.startTime }} - {{ currentBooking?.endTime }}</text>
      </view>
      <view class="info-row">
        <text class="label">创建时间</text>
        <text class="value">{{ formatDateTime(currentBooking?.createdAt) }}</text>
      </view>
    </view>
    
    <view class="qr-section" v-if="currentBooking?.status === 1">
      <view class="qr-container" @click="refreshQrCodeData">
        <image class="qr-code" :src="qrCodeUrl" mode="aspectFit" v-if="qrCodeUrl" />
        <view class="qr-placeholder" v-else>
          <text>点击获取核销码</text>
        </view>
      </view>
      <text class="qr-tip">请在核销窗口内向场馆员出示此码</text>
      <text class="qr-expire" v-if="qrExpireAt">
        有效期至: {{ formatDateTime(qrExpireAt) }}
      </text>
      <button class="refresh-btn" @click="refreshQrCodeData">刷新核销码</button>
    </view>
    
    <view class="actions" v-if="currentBooking?.status === 1">
      <button class="cancel-btn" @click="handleCancelBooking">取消预约</button>
    </view>
    
    <view class="cancel-info" v-if="currentBooking?.status === 2">
      <text class="cancel-reason">取消原因: {{ currentBooking?.cancelReason || '用户取消' }}</text>
      <text class="cancel-time">取消时间: {{ formatDateTime(currentBooking?.cancelledAt) }}</text>
    </view>
    
    <view class="checkin-info" v-if="currentBooking?.status === 3">
      <text class="checkin-time">签到时间: {{ formatDateTime(currentBooking?.checkinTime) }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useBooking, useQrCode, useBookingStatus } from '@/composables/useBooking'
import { formatDateTime } from '@/utils/date'

const bookingNo = ref('')
const { currentBooking, loadBookingDetail, cancelBooking } = useBooking()
const { qrCodeUrl, qrExpireAt, refreshQrCode } = useQrCode()
const { getStatusText, getStatusClass } = useBookingStatus()

const statusClass = computed(() => {
  const classes: Record<number, string> = {
    1: 'confirmed',
    2: 'cancelled',
    3: 'checked-in',
    4: 'no-show'
  }
  return classes[currentBooking.value?.status || 0] || ''
})

const statusText = computed(() => 
  getStatusText(currentBooking.value?.status || 0)
)

onLoad((options) => {
  if (options?.bookingNo) {
    bookingNo.value = options.bookingNo
    loadBookingDetail(bookingNo.value)
  }
})

onShow(() => {
  if (currentBooking.value?.status === 1) {
    refreshQrCodeData()
  }
})

const refreshQrCodeData = () => {
  refreshQrCode(bookingNo.value)
}

const handleCancelBooking = () => {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消此预约吗？',
    success: async (res) => {
      if (res.confirm) {
        const success = await cancelBooking(bookingNo.value, '用户取消')
        if (success) {
          uni.showToast({ title: '取消成功', icon: 'success' })
          loadBookingDetail(bookingNo.value)
        }
      }
    }
  })
}
</script>

<style scoped>
/* 样式保持不变 */
.container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20rpx;
  padding-bottom: 60rpx;
}

.status-card {
  padding: 40rpx;
  border-radius: 16rpx;
  text-align: center;
  margin-bottom: 20rpx;
}

.status-card.confirmed {
  background: linear-gradient(135deg, #52c41a, #73d13d);
}

.status-card.cancelled {
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
}

.status-card.checked-in {
  background: linear-gradient(135deg, #1890ff, #40a9ff);
}

.status-card.no-show {
  background: linear-gradient(135deg, #faad14, #ffc53d);
}

.status-text {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  display: block;
}

.booking-no {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 16rpx;
  display: block;
}

.info-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
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
  margin: 0 auto;
  border: 2rpx solid #e8e8e8;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.qr-code {
  width: 280rpx;
  height: 280rpx;
}

.qr-placeholder {
  color: #999;
  font-size: 28rpx;
}

.qr-tip {
  font-size: 26rpx;
  color: #666;
  margin-top: 20rpx;
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
  background: #f5f5f5;
  color: #666;
  font-size: 26rpx;
  padding: 16rpx 40rpx;
  border-radius: 40rpx;
  border: none;
}

.actions {
  margin-top: 40rpx;
}

.cancel-btn {
  background: #fff;
  color: #ff4d4f;
  font-size: 30rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  border: 2rpx solid #ff4d4f;
}

.cancel-info, .checkin-info {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-top: 20rpx;
}

.cancel-reason, .cancel-time, .checkin-time {
  font-size: 28rpx;
  color: #666;
  display: block;
  margin-bottom: 10rpx;
}
</style>
```

**Step 2: Commit**

```bash
git add src/pages/booking-detail/booking-detail.vue
git commit -m "refactor: migrate booking-detail page to Composition API"
```

---

### 阶段四：测试验证与优化 (预计 1 天)

#### Task 15: 运行构建验证

**Step 1: 运行开发构建**

Run: `cd miniapp && npm run dev:mp-weixin`

Expected: 构建成功，无 TypeScript 错误

**Step 2: 运行生产构建**

Run: `cd miniapp && npm run build:mp-weixin`

Expected: 构建成功，输出到 dist/build/mp-weixin

**Step 3: Commit**

```bash
git add -A
git commit -m "chore: verify build after migration"
```

---

#### Task 16: 功能验证测试

**Step 1: 验证首页功能**

- 打开小程序首页
- 验证球馆列表正常显示
- 验证点击跳转正常

**Step 2: 验证预约流程**

- 选择球馆 -> 选择场地 -> 选择时段 -> 提交预约
- 验证预约成功后跳转

**Step 3: 验证个人中心**

- 验证登录功能
- 验证预约列表
- 验证预约详情和核销码

**Step 4: 记录测试结果**

---

### 阶段五：文档更新与收尾 (预计 0.5 天)

#### Task 17: 更新项目文档

**Files:**
- Modify: `README.md` (如存在)

**Step 1: 更新技术栈说明**

添加 Vue 3 Composition API 相关说明

**Step 2: Commit**

```bash
git add README.md
git commit -m "docs: update README with Vue 3 Composition API"
```

---

## 五、风险评估与应对策略

### 5.1 风险矩阵

| 风险项 | 影响程度 | 发生概率 | 应对策略 |
|--------|----------|----------|----------|
| uni-app 生命周期钩子兼容性 | 高 | 低 | 使用 `@dcloudio/uni-app` 导入钩子 |
| TypeScript 类型推断问题 | 中 | 中 | 使用 `defineProps<T>()` 和 `defineEmits<T>()` |
| 响应式数据丢失 | 高 | 低 | 使用 `ref()` 和 `reactive()` 正确声明 |
| 组件通信问题 | 中 | 中 | 使用 `defineProps` 和 `defineEmits` |
| 第三方库兼容性 | 低 | 低 | 已验证 Pinia、vue-i18n 兼容 |

### 5.2 回滚机制

1. **Git 分支策略**: 在 `feature/vue3-composition-api` 分支开发，合并前可随时回滚
2. **渐进式迁移**: 按组件独立迁移，单个组件回滚不影响其他组件
3. **版本标签**: 每个阶段完成后打 tag，便于快速回滚

---

## 六、测试验证方案

### 6.1 单元测试

- 组件渲染测试
- Props 传递测试
- 事件触发测试
- 计算属性测试

### 6.2 集成测试

- 页面导航测试
- API 调用测试
- 状态管理测试

### 6.3 端到端测试

- 完整预约流程测试
- 用户登录流程测试
- 预约管理流程测试

---

## 七、交付物清单

| 交付物 | 说明 |
|--------|------|
| TypeScript 类型定义 | `src/types/*.ts` |
| 组合式函数 | `src/composables/*.ts` |
| 工具函数 | `src/utils/date.ts` |
| 迁移后的组件 | 所有 `.vue` 文件 |
| 构建产物 | `dist/build/mp-weixin` |
| 测试报告 | 功能验证结果 |

---

## 八、验收标准

### 8.1 功能验收

- [ ] 所有页面正常渲染
- [ ] 所有交互功能正常
- [ ] API 调用正常
- [ ] 路由跳转正常

### 8.2 代码质量验收

- [ ] TypeScript 编译无错误
- [ ] 无 ESLint 警告
- [ ] 代码符合 Composition API 最佳实践
- [ ] 组件职责单一，逻辑清晰

### 8.3 性能验收

- [ ] 首屏加载时间无明显增加
- [ ] 页面切换流畅
- [ ] 内存占用正常

---

## 九、时间节点与里程碑

| 阶段 | 预计时间 | 里程碑 |
|------|----------|--------|
| 阶段一：基础设施准备 | 0.5 天 | 类型定义和组合式函数完成 |
| 阶段二：简单组件迁移 | 1 天 | 3 个组件 + App.vue 迁移完成 |
| 阶段三：页面组件迁移 | 2 天 | 7 个页面迁移完成 |
| 阶段四：测试验证 | 1 天 | 所有功能验证通过 |
| 阶段五：文档收尾 | 0.5 天 | 文档更新，合并主分支 |

**总计预计时间：5 天**

---

**Plan complete and saved to `docs/plans/2026-02-15-vue2-to-vue3-composition-api-migration.md`. Two execution options:**

**1. Subagent-Driven (this session)** - I dispatch fresh subagent per task, review between tasks, fast iteration

**2. Parallel Session (separate)** - Open new session with executing-plans, batch execution with checkpoints

**Which approach?**
