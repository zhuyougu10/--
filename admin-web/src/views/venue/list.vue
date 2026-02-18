<template>
  <div class="venue-list">
    <div class="page-header">
      <h2>球馆管理</h2>
      <a-button type="primary" @click="$router.push('/venue/create')">
        <template #icon><PlusOutlined /></template>
        新建球馆
      </a-button>
    </div>
    
    <a-table
      :columns="columns"
      :data-source="venues"
      :loading="loading"
      :pagination="pagination"
      rowKey="id"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="isVenueOpen(record) ? 'green' : 'default'">
            {{ isVenueOpen(record) ? '营业中' : '已闭馆' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'sportType'">
          <a-tag v-for="type in getSportTypes(record.sportType)" :key="type" color="blue" style="margin: 2px;">
            {{ getSportTypeLabel(type) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'openHours'">
          {{ record.openTime }} - {{ record.closeTime }}
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleEdit(record)">
              编辑
            </a-button>
            <a-popconfirm
              :title="record.status === 1 ? '确定关闭该球馆？' : '确定开启该球馆？'"
              @confirm="handleToggleStatus(record)"
            >
              <a-button type="link" size="small">
                {{ record.status === 1 ? '关闭' : '开启' }}
              </a-button>
            </a-popconfirm>
            <a-popconfirm title="确定删除该球馆？" @confirm="handleDelete(record)">
              <a-button type="link" size="small" danger>
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import { getVenueList, updateVenueStatus, deleteVenue } from '@/api/venue'
import { ApiError } from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const venues = ref([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: '球馆名称', dataIndex: 'name', key: 'name' },
  { title: '位置', dataIndex: 'location', key: 'location' },
  { title: '运动类型', dataIndex: 'sportType', key: 'sportType' },
  { title: '营业时间', key: 'openHours' },
  { title: '场地数', dataIndex: 'courtCount', key: 'courtCount' },
  { title: '状态', key: 'status' },
  { title: '操作', key: 'action', width: 200 }
]

const sportTypeMap = {
  badminton: '羽毛球',
  basketball: '篮球',
  table_tennis: '乒乓球',
  tennis: '网球',
  volleyball: '排球'
}

const getSportTypeLabel = (type) => sportTypeMap[type] || type

const getSportTypes = (sportType) => {
  if (!sportType) return []
  return sportType.split(',').filter(t => t.trim())
}

const isVenueOpen = (venue) => {
  if (venue.status !== 1) return false
  const now = new Date()
  const currentTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
  return currentTime >= venue.openTime && currentTime <= venue.closeTime
}

const loadVenues = async () => {
  loading.value = true
  try {
    const result = await getVenueList({
      current: pagination.current,
      size: pagination.pageSize
    })
    venues.value = result.data.records
    pagination.total = result.data.total
  } catch (e) {
    if (e instanceof ApiError) {
      message.error(e.message)
    } else {
      message.error('加载球馆列表失败')
    }
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadVenues()
}

const handleEdit = (record) => {
  router.push(`/venue/${record.id}/edit`)
}

const handleToggleStatus = async (record) => {
  try {
    const newStatus = record.status === 1 ? 0 : 1
    const result = await updateVenueStatus(record.id, newStatus)
    message.success(result.message || '状态更新成功')
    loadVenues()
  } catch (e) {
    if (e instanceof ApiError) {
      message.error(e.message)
    } else {
      message.error('状态更新失败')
    }
  }
}

const handleDelete = async (record) => {
  try {
    const result = await deleteVenue(record.id)
    message.success(result.message || '删除成功')
    loadVenues()
  } catch (e) {
    if (e instanceof ApiError) {
      message.error(e.message)
    } else {
      message.error('删除失败')
    }
  }
}

onMounted(() => {
  loadVenues()
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
