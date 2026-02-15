<template>
  <div class="booking-list">
    <div class="page-header">
      <h2>预约记录</h2>
    </div>
    
    <a-card class="search-card">
      <a-form layout="inline">
        <a-form-item label="球馆">
          <a-select
            v-model:value="searchParams.venueId"
            style="width: 150px"
            placeholder="选择球馆"
            allowClear
          >
            <a-select-option v-for="v in venues" :key="v.id" :value="v.id">
              {{ v.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="预约日期">
          <a-range-picker v-model:value="searchParams.dateRange" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchParams.status"
            style="width: 120px"
            placeholder="选择状态"
            allowClear
          >
            <a-select-option :value="1">待使用</a-select-option>
            <a-select-option :value="2">已核销</a-select-option>
            <a-select-option :value="3">已爽约</a-select-option>
            <a-select-option :value="0">已取消</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="预约人">
          <a-input
            v-model:value="searchParams.userName"
            placeholder="预约人姓名"
            style="width: 120px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
    
    <a-table
      :columns="columns"
      :data-source="bookings"
      :loading="loading"
      :pagination="pagination"
      rowKey="id"
      style="margin-top: 16px"
      @change="handleTableChange"
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
          <a-button type="link" size="small" @click="handleDetail(record)">
            详情
          </a-button>
        </template>
      </template>
    </a-table>
    
    <a-modal
      v-model:open="detailVisible"
      title="预约详情"
      :footer="null"
      width="600px"
    >
      <a-descriptions :column="2" bordered v-if="currentBooking">
        <a-descriptions-item label="预约单号">{{ currentBooking.bookingNo }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(currentBooking.status)">
            {{ currentBooking.statusText }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="球馆">{{ currentBooking.venueName }}</a-descriptions-item>
        <a-descriptions-item label="场地">{{ currentBooking.courtName }}</a-descriptions-item>
        <a-descriptions-item label="预约人">{{ currentBooking.userName }}</a-descriptions-item>
        <a-descriptions-item label="联系电话">{{ currentBooking.userPhone }}</a-descriptions-item>
        <a-descriptions-item label="预约日期">{{ currentBooking.bookingDate }}</a-descriptions-item>
        <a-descriptions-item label="时段">{{ currentBooking.startTime }} - {{ currentBooking.endTime }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ currentBooking.createdAt }}</a-descriptions-item>
        <a-descriptions-item label="核销时间">{{ currentBooking.checkinTime || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getBookingList } from '@/api/booking'
import { getVenueList } from '@/api/venue'
import { ApiError } from '@/utils/request'

const loading = ref(false)
const bookings = ref([])
const venues = ref([])
const detailVisible = ref(false)
const currentBooking = ref(null)

const searchParams = reactive({
  venueId: null,
  dateRange: null,
  status: null,
  userName: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: '预约单号', dataIndex: 'bookingNo', key: 'bookingNo' },
  { title: '球馆', dataIndex: 'venueName', key: 'venueName' },
  { title: '场地', dataIndex: 'courtName', key: 'courtName' },
  { title: '预约人', dataIndex: 'userName', key: 'userName' },
  { title: '日期', dataIndex: 'bookingDate', key: 'bookingDate' },
  { title: '时段', key: 'time' },
  { title: '状态', key: 'status' },
  { title: '操作', key: 'action', width: 80 }
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
    venues.value = result.data.records
  } catch (e) {
    if (e instanceof ApiError) {
      message.error(e.message)
    } else {
      message.error('加载球馆列表失败')
    }
  }
}

const loadBookings = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.pageSize,
      venueId: searchParams.venueId,
      status: searchParams.status,
      userName: searchParams.userName || undefined
    }
    if (searchParams.dateRange && searchParams.dateRange.length === 2) {
      params.startDate = searchParams.dateRange[0].format('YYYY-MM-DD')
      params.endDate = searchParams.dateRange[1].format('YYYY-MM-DD')
    }
    const result = await getBookingList(params)
    bookings.value = result.data.records
    pagination.total = result.data.total
  } catch (e) {
    if (e instanceof ApiError) {
      message.error(e.message)
    } else {
      message.error('加载预约列表失败')
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadBookings()
}

const handleReset = () => {
  searchParams.venueId = null
  searchParams.dateRange = null
  searchParams.status = null
  searchParams.userName = ''
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadBookings()
}

const handleDetail = (record) => {
  currentBooking.value = record
  detailVisible.value = true
}

onMounted(() => {
  loadVenues()
  loadBookings()
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

.search-card {
  margin-bottom: 16px;
}
</style>
