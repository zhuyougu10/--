<template>
  <div class="dashboard-page">
    <h2>仪表盘</h2>
    <a-row :gutter="16">
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="今日预约"
            :value="stats.todayBookings"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix><CalendarOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="今日核销"
            :value="stats.todayCheckins"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix><CheckCircleOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="活跃球馆"
            :value="stats.activeVenues"
            :value-style="{ color: '#722ed1' }"
          >
            <template #prefix><HomeOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="活跃场地"
            :value="stats.activeCourts"
            :value-style="{ color: '#fa8c16' }"
          >
            <template #prefix><AppstoreOutlined /></template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>
    
    <a-card title="快捷操作" style="margin-top: 24px">
      <a-space>
        <a-button type="primary" @click="$router.push('/booking/today')">
          今日排场
        </a-button>
        <a-button @click="$router.push('/checkin')">
          扫码核销
        </a-button>
        <a-button @click="$router.push('/venue')">
          球馆管理
        </a-button>
      </a-space>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  CalendarOutlined,
  CheckCircleOutlined,
  HomeOutlined,
  AppstoreOutlined
} from '@ant-design/icons-vue'

const stats = ref({
  todayBookings: 0,
  todayCheckins: 0,
  activeVenues: 0,
  activeCourts: 0
})

onMounted(() => {
})
</script>

<style scoped>
.dashboard-page h2 {
  margin-bottom: 24px;
}
</style>
