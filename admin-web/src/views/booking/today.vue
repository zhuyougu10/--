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
        <template v-if="column.key === 'time'">
          {{ record.startTime }} - {{ record.endTime }}
        </template>
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

const statusMap = {
  0: { text: '已取消', color: 'default' },
  1: { text: '待使用', color: 'blue' },
  2: { text: '已核销', color: 'green' },
  3: { text: '已爽约', color: 'red' }
}

const getStatusColor = (status) => statusMap[status]?.color || 'default'

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
