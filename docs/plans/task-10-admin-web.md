# Task 10: 后台管理端开发

> **依赖:** task-03-auth-module.md
> **预计时间:** 10-12 小时

## 目标
使用 Vue 3 开发 Web 后台管理端，实现管理员登录、球馆管理、预约管理、核销签到、数据统计等功能。

---

## 项目结构

```
admin-web/
├── public/
├── src/
│   ├── api/
│   │   ├── auth.js
│   │   ├── venue.js
│   │   ├── court.js
│   │   ├── booking.js
│   │   └── checkin.js
│   ├── components/
│   │   ├── layout/
│   │   │   ├── Sidebar.vue
│   │   │   └── Header.vue
│   │   └── common/
│   ├── views/
│   │   ├── login/
│   │   │   └── index.vue
│   │   ├── dashboard/
│   │   │   └── index.vue
│   │   ├── venue/
│   │   │   ├── list.vue
│   │   │   └── form.vue
│   │   ├── court/
│   │   │   ├── list.vue
│   │   │   └── form.vue
│   │   ├── booking/
│   │   │   ├── today.vue
│   │   │   └── list.vue
│   │   ├── checkin/
│   │   │   └── index.vue
│   │   └── user/
│   │       └── list.vue
│   ├── router/
│   │   └── index.js
│   ├── store/
│   │   ├── index.js
│   │   └── modules/
│   │       └── user.js
│   ├── utils/
│   │   └── request.js
│   ├── App.vue
│   └── main.js
├── package.json
├── vite.config.js
└── index.html
```

---

## Step 1: 初始化项目

**命令:**
```bash
npm create vite@latest admin-web -- --template vue
cd admin-web
npm install vue-router@4 pinia axios ant-design-vue @ant-design/icons-vue
```

**文件:** `admin-web/vite.config.js`

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

---

## Step 2: 创建请求封装

**文件:** `admin-web/src/utils/request.js`

```javascript
import axios from 'axios'
import { message } from 'ant-design-vue'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const { data } = response
    if (data.code === 200) {
      return data.data
    } else if (data.code === 401) {
      localStorage.removeItem('admin_token')
      router.push('/login')
      return Promise.reject(new Error('未登录'))
    } else {
      message.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
  },
  error => {
    message.error('网络错误')
    return Promise.reject(error)
  }
)

export default request
```

---

## Step 3: 创建 API 模块

**文件:** `admin-web/src/api/auth.js`

```javascript
import request from '@/utils/request'

export const login = (data) => {
  return request({
    url: '/admin/auth/login',
    method: 'POST',
    data
  })
}

export const getProfile = () => {
  return request({
    url: '/admin/auth/profile'
  })
}
```

**文件:** `admin-web/src/api/venue.js`

```javascript
import request from '@/utils/request'

export const getVenueList = (params) => {
  return request({
    url: '/admin/venues',
    params
  })
}

export const getVenueDetail = (id) => {
  return request({
    url: `/admin/venues/${id}`
  })
}

export const createVenue = (data) => {
  return request({
    url: '/admin/venues',
    method: 'POST',
    data
  })
}

export const updateVenue = (id, data) => {
  return request({
    url: `/admin/venues/${id}`,
    method: 'PUT',
    data
  })
}

export const updateVenueStatus = (id, status) => {
  return request({
    url: `/admin/venues/${id}/status`,
    method: 'PATCH',
    params: { status }
  })
}

export const deleteVenue = (id) => {
  return request({
    url: `/admin/venues/${id}`,
    method: 'DELETE'
  })
}
```

**文件:** `admin-web/src/api/booking.js`

```javascript
import request from '@/utils/request'

export const getTodaySchedule = (venueId) => {
  return request({
    url: '/admin/bookings/today',
    params: { venueId }
  })
}

export const getBookingDetail = (bookingNo) => {
  return request({
    url: `/admin/bookings/${bookingNo}`
  })
}
```

**文件:** `admin-web/src/api/checkin.js`

```javascript
import request from '@/utils/request'

export const scanCheckin = (token) => {
  return request({
    url: '/admin/checkin/scan',
    method: 'POST',
    data: { token, checkinMethod: 1 }
  })
}

export const manualCheckin = (bookingNo) => {
  return request({
    url: '/admin/checkin/manual',
    method: 'POST',
    data: { bookingNo }
  })
}

export const markNoShow = (bookingNo) => {
  return request({
    url: '/admin/violations/no-show',
    method: 'POST',
    data: { bookingNo }
  })
}
```

---

## Step 4: 创建路由配置

**文件:** `admin-web/src/router/index.js`

```javascript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'dashboard' }
      },
      {
        path: 'venue',
        name: 'VenueList',
        component: () => import('@/views/venue/list.vue'),
        meta: { title: '球馆管理', icon: 'home' }
      },
      {
        path: 'venue/create',
        name: 'VenueCreate',
        component: () => import('@/views/venue/form.vue'),
        meta: { title: '新建球馆', hidden: true }
      },
      {
        path: 'venue/:id/edit',
        name: 'VenueEdit',
        component: () => import('@/views/venue/form.vue'),
        meta: { title: '编辑球馆', hidden: true }
      },
      {
        path: 'court',
        name: 'CourtList',
        component: () => import('@/views/court/list.vue'),
        meta: { title: '场地管理', icon: 'appstore' }
      },
      {
        path: 'booking/today',
        name: 'TodaySchedule',
        component: () => import('@/views/booking/today.vue'),
        meta: { title: '今日排场', icon: 'calendar' }
      },
      {
        path: 'checkin',
        name: 'Checkin',
        component: () => import('@/views/checkin/index.vue'),
        meta: { title: '扫码核销', icon: 'scan' }
      },
      {
        path: 'user',
        name: 'UserList',
        component: () => import('@/views/user/list.vue'),
        meta: { title: '用户管理', icon: 'user' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
```

---

## Step 5: 创建布局组件

**文件:** `admin-web/src/components/layout/MainLayout.vue`

```vue
<template>
  <a-layout class="main-layout">
    <a-layout-sider v-model:collapsed="collapsed" collapsible>
      <div class="logo">
        <span v-if="!collapsed">球馆管理系统</span>
        <span v-else>球馆</span>
      </div>
      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="inline"
      >
        <a-menu-item key="dashboard" @click="$router.push('/dashboard')">
          <template #icon><DashboardOutlined /></template>
          <span>仪表盘</span>
        </a-menu-item>
        <a-menu-item key="venue" @click="$router.push('/venue')">
          <template #icon><HomeOutlined /></template>
          <span>球馆管理</span>
        </a-menu-item>
        <a-menu-item key="court" @click="$router.push('/court')">
          <template #icon><AppstoreOutlined /></template>
          <span>场地管理</span>
        </a-menu-item>
        <a-menu-item key="today" @click="$router.push('/booking/today')">
          <template #icon><CalendarOutlined /></template>
          <span>今日排场</span>
        </a-menu-item>
        <a-menu-item key="checkin" @click="$router.push('/checkin')">
          <template #icon><ScanOutlined /></template>
          <span>扫码核销</span>
        </a-menu-item>
        <a-menu-item key="user" @click="$router.push('/user')">
          <template #icon><UserOutlined /></template>
          <span>用户管理</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="header">
        <div class="header-right">
          <a-dropdown>
            <a class="user-info" @click.prevent>
              <UserOutlined />
              <span style="margin-left: 8px">{{ userName }}</span>
            </a>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="logout">退出登录</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  DashboardOutlined,
  HomeOutlined,
  AppstoreOutlined,
  CalendarOutlined,
  ScanOutlined,
  UserOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)
const selectedKeys = computed(() => {
  const path = route.path
  if (path.includes('/venue')) return ['venue']
  if (path.includes('/court')) return ['court']
  if (path.includes('/booking/today')) return ['today']
  if (path.includes('/checkin')) return ['checkin']
  if (path.includes('/user')) return ['user']
  return ['dashboard']
})

const userName = computed(() => localStorage.getItem('admin_username') || '管理员')

const logout = () => {
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_username')
  router.push('/login')
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background: rgba(255, 255, 255, 0.1);
}

.header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.user-info {
  color: #333;
}

.content {
  margin: 24px;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  min-height: calc(100vh - 112px);
}
</style>
```

---

## Step 6: 创建登录页面

**文件:** `admin-web/src/views/login/index.vue`

```vue
<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="title">球馆管理系统</h1>
      <a-form
        :model="formState"
        @finish="handleLogin"
      >
        <a-form-item
          name="username"
          :rules="[{ required: true, message: '请输入用户名' }]"
        >
          <a-input
            v-model:value="formState.username"
            placeholder="用户名"
            size="large"
          >
            <template #prefix><UserOutlined /></template>
          </a-input>
        </a-form-item>
        <a-form-item
          name="password"
          :rules="[{ required: true, message: '请输入密码' }]"
        >
          <a-input-password
            v-model:value="formState.password"
            placeholder="密码"
            size="large"
          >
            <template #prefix><LockOutlined /></template>
          </a-input-password>
        </a-form-item>
        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            block
            :loading="loading"
          >
            登录
          </a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { login } from '@/api/auth'

const router = useRouter()
const loading = ref(false)
const formState = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  loading.value = true
  try {
    const result = await login(formState)
    localStorage.setItem('admin_token', result.token)
    localStorage.setItem('admin_username', formState.username)
    message.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1890ff, #36cfc9);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  margin-bottom: 40px;
  color: #1890ff;
}
</style>
```

---

## Step 7: 创建今日排场页面

**文件:** `admin-web/src/views/booking/today.vue`

```vue
<template>
  <div class="today-schedule">
    <div class="page-header">
      <h2>今日排场</h2>
      <a-select
        v-model:value="selectedVenue"
        style="width: 200px"
        placeholder="选择球馆"
        allowClear
        @change="loadSchedule"
      >
        <a-select-option v-for="v in venues" :key="v.id" :value="v.id">
          {{ v.name }}
        </a-select-option>
      </a-select>
    </div>
    
    <a-table
      :columns="columns"
      :data-source="bookings"
      :loading="loading"
      rowKey="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="getStatusColor(record.status)">
            {{ record.statusText }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button
              type="link"
              size="small"
              @click="handleCheckin(record)"
              :disabled="record.status !== 1"
            >
              核销
            </a-button>
            <a-button
              type="link"
              size="small"
              danger
              @click="handleNoShow(record)"
              :disabled="record.status !== 1"
            >
              爽约
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { getTodaySchedule } from '@/api/booking'
import { getVenueList } from '@/api/venue'
import { manualCheckin, markNoShow } from '@/api/checkin'

const loading = ref(false)
const bookings = ref([])
const venues = ref([])
const selectedVenue = ref(null)

const columns = [
  { title: '预约单号', dataIndex: 'bookingNo', key: 'bookingNo' },
  { title: '球馆', dataIndex: 'venueName', key: 'venueName' },
  { title: '场地', dataIndex: 'courtName', key: 'courtName' },
  { title: '预约人', dataIndex: 'userName', key: 'userName' },
  { title: '日期', dataIndex: 'bookingDate', key: 'bookingDate' },
  { title: '时段', key: 'time' },
  { title: '状态', key: 'status' },
  { title: '操作', key: 'action', width: 150 }
]

const getStatusColor = (status) => {
  const colors = { 1: 'blue', 2: 'default', 3: 'green', 4: 'red' }
  return colors[status] || 'default'
}

const loadVenues = async () => {
  try {
    const result = await getVenueList({ current: 1, size: 100 })
    venues.value = result.records
  } catch (e) {
    console.error(e)
  }
}

const loadSchedule = async () => {
  loading.value = true
  try {
    bookings.value = await getTodaySchedule(selectedVenue.value)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleCheckin = (record) => {
  Modal.confirm({
    title: '确认核销',
    content: `确认核销 ${record.userName} 的预约？`,
    async onOk() {
      try {
        await manualCheckin(record.bookingNo)
        message.success('核销成功')
        loadSchedule()
      } catch (e) {
        console.error(e)
      }
    }
  })
}

const handleNoShow = (record) => {
  Modal.confirm({
    title: '标记爽约',
    content: `确认将 ${record.userName} 标记为爽约？`,
    async onOk() {
      try {
        await markNoShow(record.bookingNo)
        message.success('已标记爽约')
        loadSchedule()
      } catch (e) {
        console.error(e)
      }
    }
  })
}

onMounted(() => {
  loadVenues()
  loadSchedule()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
}
</style>
```

---

## Step 8: 创建扫码核销页面

**文件:** `admin-web/src/views/checkin/index.vue`

```vue
<template>
  <div class="checkin-page">
    <div class="checkin-container">
      <div class="scanner-section">
        <h3>扫码核销</h3>
        <div class="scanner-box" @click="startScan">
          <ScanOutlined class="scan-icon" />
          <p>点击扫描用户核销码</p>
        </div>
      </div>
      
      <a-divider>或</a-divider>
      
      <div class="manual-section">
        <h3>手动核销</h3>
        <a-input-search
          v-model:value="bookingNo"
          placeholder="输入预约单号"
          enter-button="核销"
          @search="handleManualCheckin"
        />
      </div>
    </div>
    
    <a-modal
      v-model:open="resultVisible"
      :title="checkinResult.success ? '核销成功' : '核销失败'"
      :footer="null"
    >
      <a-result
        :status="checkinResult.success ? 'success' : 'error'"
        :title="checkinResult.message"
      >
        <template #extra>
          <div class="checkin-info" v-if="checkinResult.success">
            <p><strong>预约人：</strong>{{ checkinResult.userName }}</p>
            <p><strong>球馆：</strong>{{ checkinResult.venueName }}</p>
            <p><strong>场地：</strong>{{ checkinResult.courtName }}</p>
            <p><strong>时段：</strong>{{ checkinResult.startTime }} - {{ checkinResult.endTime }}</p>
          </div>
          <a-button type="primary" @click="resultVisible = false">确定</a-button>
        </template>
      </a-result>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { ScanOutlined } from '@ant-design/icons-vue'
import { scanCheckin, manualCheckin } from '@/api/checkin'

const bookingNo = ref('')
const resultVisible = ref(false)
const checkinResult = reactive({
  success: false,
  message: '',
  userName: '',
  venueName: '',
  courtName: '',
  startTime: '',
  endTime: ''
})

const startScan = () => {
  message.info('请在微信小程序中实现扫码功能')
}

const handleManualCheckin = async () => {
  if (!bookingNo.value) {
    message.warning('请输入预约单号')
    return
  }
  
  try {
    const result = await manualCheckin(bookingNo.value)
    checkinResult.success = result.success
    checkinResult.message = result.message
    checkinResult.userName = result.userName
    checkinResult.venueName = result.venueName
    checkinResult.courtName = result.courtName
    checkinResult.startTime = result.startTime
    checkinResult.endTime = result.endTime
    resultVisible.value = true
    bookingNo.value = ''
  } catch (e) {
    console.error(e)
  }
}
</script>

<style scoped>
.checkin-page {
  max-width: 600px;
  margin: 0 auto;
}

.checkin-container {
  background: #f5f5f5;
  padding: 40px;
  border-radius: 16px;
}

.scanner-section {
  text-align: center;
}

.scanner-box {
  width: 200px;
  height: 200px;
  margin: 20px auto;
  background: #1890ff;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  transition: transform 0.2s;
}

.scanner-box:hover {
  transform: scale(1.05);
}

.scan-icon {
  font-size: 60px;
}

.manual-section {
  text-align: center;
}

.checkin-info {
  text-align: left;
  margin-bottom: 20px;
}

.checkin-info p {
  margin: 8px 0;
}
</style>
```

---

## Step 9: 验证项目

**运行项目:**
```bash
cd admin-web
npm run dev
```

**访问:** http://localhost:3000

---

## 提交

```bash
git add admin-web/
git commit -m "feat(admin-web): init vue3 admin dashboard with venue and booking management"
```

---

## 注意事项

1. **权限控制**: 根据用户角色显示/隐藏菜单
2. **扫码功能**: 需要集成微信 JS-SDK 或使用摄像头 API
3. **数据导出**: 预约列表支持导出 Excel
