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
        <a-menu-item key="booking" @click="$router.push('/booking')">
          <template #icon><FileTextOutlined /></template>
          <span>预约记录</span>
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
  FileTextOutlined,
  ScanOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const collapsed = ref(false)

const selectedKeys = computed(() => {
  const path = route.path
  if (path.includes('/venue')) return ['venue']
  if (path.includes('/court')) return ['court']
  if (path.includes('/booking/today')) return ['today']
  if (path.includes('/booking')) return ['booking']
  if (path.includes('/checkin')) return ['checkin']
  if (path.includes('/user')) return ['user']
  return ['dashboard']
})

const userName = computed(() => userStore.username)

const logout = () => {
  userStore.clearAuth()
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
