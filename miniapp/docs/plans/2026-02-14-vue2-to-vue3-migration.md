# Vue 2 to Vue 3 Composition API 迁移计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 将UniApp项目从Vue 2 Options API语法迁移到Vue 3 Composition API规范

**Architecture:** 采用渐进式迁移策略，逐文件将Options API重构为Composition API，使用ref/reactive进行响应式数据管理，使用组合式函数(composables)抽取复用逻辑

**Tech Stack:** Vue 3.5.28 + UniApp 3.0 + Pinia 2.1.7 + TypeScript 5.4.5

---

## 项目分析

### 当前状态
- 依赖已升级至Vue 3.5.28，但代码仍使用Vue 2 Options API
- 共10个.vue文件需要迁移
- 3个组件：venue-card, booking-card, time-slot-picker
- 7个页面：index, venue, venue-detail, booking, booking-detail, my, my-bookings
- 1个应用入口：App.vue

### 迁移范围
| 文件 | 类型 | 复杂度 | 主要变更 |
|------|------|--------|----------|
| App.vue | 应用入口 | 低 | 生命周期钩子迁移 |
| main.js | 入口配置 | 低 | 已是Vue 3语法 |
| index.vue | 首页 | 中 | data/methods迁移 |
| venue.vue | 球馆列表 | 中 | data/computed/methods迁移 |
| venue-detail.vue | 球馆详情 | 中 | data/methods迁移 |
| booking.vue | 预约页面 | 高 | 复杂状态管理 |
| booking-detail.vue | 预约详情 | 中 | computed/methods迁移 |
| my.vue | 个人中心 | 高 | 复杂登录逻辑 |
| my-bookings.vue | 我的预约 | 中 | data/methods迁移 |
| venue-card.vue | 组件 | 中 | props/computed迁移 |
| booking-card.vue | 组件 | 低 | props/computed迁移 |
| time-slot-picker.vue | 组件 | 中 | props/computed迁移 |

---

## Task 1: 创建组合式函数(Composables)

**Files:**
- Create: `src/composables/useVenue.js`
- Create: `src/composables/useBooking.js`
- Create: `src/composables/useAuth.js`

**Step 1: 创建useVenue组合式函数**

```javascript
import { ref, computed } from 'vue'
import { getVenueList, getVenueDetail, getTimeSlots } from '@/api/venue'

export function useVenue() {
  const venues = ref([])
  const currentVenue = ref({})
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

  const loadVenueDetail = async (id) => {
    loading.value = true
    try {
      currentVenue.value = await getVenueDetail(id)
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  const getSportTypeName = (type) => {
    const types = {
      badminton: '羽毛球',
      basketball: '篮球',
      table_tennis: '乒乓球',
      tennis: '网球'
    }
    return types[type] || type
  }

  const isOpen = (venue) => {
    if (venue.status !== 1) return false
    const now = new Date()
    const currentTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
    return currentTime >= venue.openTime && currentTime <= venue.closeTime
  }

  return {
    venues,
    currentVenue,
    loading,
    loadVenues,
    loadVenueDetail,
    getSportTypeName,
    isOpen
  }
}

export function useTimeSlots() {
  const slots = ref([])
  const selectedSlots = ref([])
  const loading = ref(false)

  const loadSlots = async (venueId, courtId, date) => {
    loading.value = true
    try {
      slots.value = await getTimeSlots(venueId, courtId, date)
      selectedSlots.value = []
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  const toggleSlot = (slot) => {
    if (slot.status !== 'free') return
    const index = selectedSlots.value.findIndex(s => s.startTime === slot.startTime)
    if (index > -1) {
      selectedSlots.value.splice(index, 1)
    } else {
      selectedSlots.value.push(slot)
      selectedSlots.value.sort((a, b) => a.startTime.localeCompare(b.startTime))
    }
  }

  const isSelected = (slot) => {
    return selectedSlots.value.some(s => s.startTime === slot.startTime)
  }

  return {
    slots,
    selectedSlots,
    loading,
    loadSlots,
    toggleSlot,
    isSelected
  }
}
```

**Step 2: 创建useBooking组合式函数**

```javascript
import { ref, computed } from 'vue'
import { createBooking, getBookingDetail, getMyBookings, cancelBooking as cancelBookingApi, getQrCode } from '@/api/booking'

export function useBooking() {
  const bookings = ref([])
  const currentBooking = ref({})
  const loading = ref(false)
  const submitting = ref(false)

  const loadBookings = async (status) => {
    loading.value = true
    try {
      bookings.value = await getMyBookings(status)
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  const loadBookingDetail = async (bookingNo) => {
    loading.value = true
    try {
      currentBooking.value = await getBookingDetail(bookingNo)
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  const submitBooking = async (data) => {
    submitting.value = true
    try {
      const result = await createBooking(data)
      uni.showToast({ title: '预约成功', icon: 'success' })
      return result
    } catch (e) {
      console.error(e)
      throw e
    } finally {
      submitting.value = false
    }
  }

  const cancelBooking = async (bookingNo, reason) => {
    try {
      await cancelBookingApi(bookingNo, reason)
      uni.showToast({ title: '取消成功', icon: 'success' })
      return true
    } catch (e) {
      console.error(e)
      return false
    }
  }

  const getStatusClass = (status) => {
    const classes = {
      1: 'pending',
      2: 'cancelled',
      3: 'completed',
      4: 'no-show'
    }
    return classes[status] || ''
  }

  const getStatusText = (status) => {
    const texts = {
      1: '待使用',
      2: '已取消',
      3: '已完成',
      4: '爽约'
    }
    return texts[status] || '未知'
  }

  return {
    bookings,
    currentBooking,
    loading,
    submitting,
    loadBookings,
    loadBookingDetail,
    submitBooking,
    cancelBooking,
    getStatusClass,
    getStatusText
  }
}

export function useQrCode() {
  const qrCodeUrl = ref('')
  const qrExpireAt = ref(null)

  const refreshQrCode = async (bookingNo) => {
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
```

**Step 3: 创建useAuth组合式函数**

```javascript
import { ref, computed } from 'vue'
import { wechatLogin, getUserProfile } from '@/api/auth'

export function useAuth() {
  const isLoggedIn = ref(false)
  const userInfo = ref({})

  const checkLogin = () => {
    const token = uni.getStorageSync('token')
    const savedUserInfo = uni.getStorageSync('userInfo')
    isLoggedIn.value = !!token
    userInfo.value = savedUserInfo || {}
  }

  const handleLogin = async () => {
    try {
      const loginRes = await new Promise((resolve, reject) => {
        uni.login({
          provider: 'weixin',
          success: resolve,
          fail: reject
        })
      })

      const result = await wechatLogin(loginRes.code)

      uni.setStorageSync('token', result.token)
      uni.setStorageSync('userInfo', result.user)

      isLoggedIn.value = true
      userInfo.value = result.user

      uni.showToast({ title: '登录成功', icon: 'success' })
      return true
    } catch (e) {
      console.error(e)
      uni.showToast({ title: '登录失败', icon: 'none' })
      return false
    }
  }

  const handleLogout = () => {
    uni.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          isLoggedIn.value = false
          userInfo.value = {}
          uni.showToast({ title: '已退出', icon: 'success' })
        }
      }
    })
  }

  const getUserTypeName = (type) => {
    const types = {
      student: '学生',
      teacher: '教师',
      staff: '教职工'
    }
    return types[type] || '用户'
  }

  return {
    isLoggedIn,
    userInfo,
    checkLogin,
    handleLogin,
    handleLogout,
    getUserTypeName
  }
}
```

**Step 4: 验证文件创建**

Run: `ls src/composables/`
Expected: 三个文件已创建

**Step 5: Commit**

```bash
git add src/composables/
git commit -m "feat: add composables for venue, booking and auth"
```

---

## Task 2: 迁移App.vue

**Files:**
- Modify: `src/App.vue`

**Step 1: 重构为Composition API**

```vue
<script setup>
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

**Step 2: 验证编译**

Run: `npm run dev:mp-weixin`
Expected: 无编译错误

**Step 3: Commit**

```bash
git add src/App.vue
git commit -m "refactor: migrate App.vue to Composition API"
```

---

## Task 3: 迁移venue-card组件

**Files:**
- Modify: `src/components/venue-card/venue-card.vue`

**Step 1: 重构为Composition API**

将Options API的props、computed、methods重构为setup语法

**Step 2: 验证编译**

Run: `npm run dev:mp-weixin`
Expected: 无编译错误

**Step 3: Commit**

```bash
git add src/components/venue-card/venue-card.vue
git commit -m "refactor: migrate venue-card to Composition API"
```

---

## Task 4: 迁移booking-card组件

**Files:**
- Modify: `src/components/booking-card/booking-card.vue`

**Step 1: 重构为Composition API**

将Options API的props、computed、methods重构为setup语法

**Step 2: 验证编译**

Run: `npm run dev:mp-weixin`
Expected: 无编译错误

**Step 3: Commit**

```bash
git add src/components/booking-card/booking-card.vue
git commit -m "refactor: migrate booking-card to Composition API"
```

---

## Task 5: 迁移time-slot-picker组件

**Files:**
- Modify: `src/components/time-slot-picker/time-slot-picker.vue`

**Step 1: 重构为Composition API**

将Options API的props、computed、methods重构为setup语法

**Step 2: 验证编译**

Run: `npm run dev:mp-weixin`
Expected: 无编译错误

**Step 3: Commit**

```bash
git add src/components/time-slot-picker/time-slot-picker.vue
git commit -m "refactor: migrate time-slot-picker to Composition API"
```

---

## Task 6: 迁移index.vue首页

**Files:**
- Modify: `src/pages/index/index.vue`

**Step 1: 重构为Composition API**

使用useVenue组合式函数，将onShow生命周期迁移

**Step 2: 验证编译**

Run: `npm run dev:mp-weixin`
Expected: 无编译错误

**Step 3: Commit**

```bash
git add src/pages/index/index.vue
git commit -m "refactor: migrate index.vue to Composition API"
```

---

## Task 7: 迁移venue.vue球馆列表

**Files:**
- Modify: `src/pages/venue/venue.vue`

**Step 1: 重构为Composition API**

使用useVenue组合式函数，处理computed过滤逻辑

**Step 2: 验证编译**

Run: `npm run dev:mp-weixin`
Expected: 无编译错误

**Step 3: Commit**

```bash
git add src/pages/venue/venue.vue
git commit -m "refactor: migrate venue.vue to Composition API"
```

---

## Task 8: 迁移venue-detail.vue球馆详情

**Files:**
- Modify: `src/pages/venue-detail/venue-detail.vue`

**Step 1: 重构为Composition API**

使用useVenue组合式函数，处理onLoad参数获取

**Step 2: 验证编译**

Run: `npm run dev:mp-weixin`
Expected: 无编译错误

**Step 3: Commit**

```bash
git add src/pages/venue-detail/venue-detail.vue
git commit -m "refactor: migrate venue-detail.vue to Composition API"
```

---

## Task 9: 迁移booking.vue预约页面

**Files:**
- Modify: `src/pages/booking/booking.vue`

**Step 1: 重构为Composition API**

使用useVenue和useTimeSlots组合式函数，处理复杂状态

**Step 2: 验证编译**

Run: `npm run dev:mp-weixin`
Expected: 无编译错误

**Step 3: Commit**

```bash
git add src/pages/booking/booking.vue
git commit -m "refactor: migrate booking.vue to Composition API"
```

---

## Task 10: 迁移my.vue个人中心

**Files:**
- Modify: `src/pages/my/my.vue`

**Step 1: 重构为Composition API**

使用useAuth组合式函数，处理登录状态管理

**Step 2: 验证编译**

Run: `npm run dev:mp-weixin`
Expected: 无编译错误

**Step 3: Commit**

```bash
git add src/pages/my/my.vue
git commit -m "refactor: migrate my.vue to Composition API"
```

---

## Task 11: 迁移my-bookings.vue我的预约

**Files:**
- Modify: `src/pages/my-bookings/my-bookings.vue`

**Step 1: 重构为Composition API**

使用useBooking组合式函数

**Step 2: 验证编译**

Run: `npm run dev:mp-weixin`
Expected: 无编译错误

**Step 3: Commit**

```bash
git add src/pages/my-bookings/my-bookings.vue
git commit -m "refactor: migrate my-bookings.vue to Composition API"
```

---

## Task 12: 迁移booking-detail.vue预约详情

**Files:**
- Modify: `src/pages/booking-detail/booking-detail.vue`

**Step 1: 重构为Composition API**

使用useBooking和useQrCode组合式函数

**Step 2: 验证编译**

Run: `npm run dev:mp-weixin`
Expected: 无编译错误

**Step 3: Commit**

```bash
git add src/pages/booking-detail/booking-detail.vue
git commit -m "refactor: migrate booking-detail.vue to Composition API"
```

---

## Task 13: 验证与测试

**Step 1: 完整编译测试**

Run: `npm run build:mp-weixin`
Expected: 编译成功无错误

**Step 2: 功能验证**

- 首页球馆列表加载
- 球馆详情查看
- 预约流程完整测试
- 个人中心登录/退出
- 我的预约列表查看

**Step 3: 最终提交**

```bash
git add .
git commit -m "refactor: complete Vue 2 to Vue 3 Composition API migration"
```

---

## 迁移要点总结

### Options API → Composition API 映射

| Options API | Composition API |
|-------------|-----------------|
| data() | ref() / reactive() |
| methods | 普通函数 |
| computed | computed() |
| watch | watch() / watchEffect() |
| props | defineProps() |
| emit | defineEmits() |
| onLoad | onLoad() from @dcloudio/uni-app |
| onShow | onShow() from @dcloudio/uni-app |
| this.xxx | xxx.value (for ref) |

### 响应式数据访问

- `ref`: 访问时需要 `.value`
- `reactive`: 直接访问属性
- `computed`: 访问时需要 `.value`

### 生命周期钩子

UniApp页面生命周期需要从 `@dcloudio/uni-app` 导入：
- onLoad
- onShow
- onHide
- onPullDownRefresh
- onReachBottom
等

### 组件通信

- Props: `defineProps()`
- Emits: `defineEmits()`
- 无需 `this.$emit`，直接调用emit函数
