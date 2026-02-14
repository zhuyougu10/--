<template>
  <div class="court-list">
    <div class="page-header">
      <h2>场地管理</h2>
      <a-space>
        <a-select
          v-model:value="selectedVenue"
          style="width: 200px"
          placeholder="选择球馆筛选"
          allowClear
          @change="loadCourts"
        >
          <a-select-option v-for="v in venues" :key="v.id" :value="v.id">
            {{ v.name }}
          </a-select-option>
        </a-select>
        <a-button type="primary" @click="$router.push('/court/create')">
          <template #icon><PlusOutlined /></template>
          新建场地
        </a-button>
      </a-space>
    </div>
    
    <a-table
      :columns="columns"
      :data-source="courts"
      :loading="loading"
      :pagination="pagination"
      rowKey="id"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '可用' : '已关闭' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'sportType'">
          <a-tag color="blue">{{ getSportTypeLabel(record.sportType) }}</a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleEdit(record)">
              编辑
            </a-button>
            <a-popconfirm
              :title="record.status === 1 ? '确定关闭该场地？' : '确定开启该场地？'"
              @confirm="handleToggleStatus(record)"
            >
              <a-button type="link" size="small">
                {{ record.status === 1 ? '关闭' : '开启' }}
              </a-button>
            </a-popconfirm>
            <a-popconfirm title="确定删除该场地？" @confirm="handleDelete(record)">
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
import { getCourtList, updateCourtStatus, deleteCourt } from '@/api/court'
import { getVenueList } from '@/api/venue'

const router = useRouter()
const loading = ref(false)
const courts = ref([])
const venues = ref([])
const selectedVenue = ref(null)
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: '场地名称', dataIndex: 'name', key: 'name' },
  { title: '所属球馆', dataIndex: 'venueName', key: 'venueName' },
  { title: '运动类型', key: 'sportType' },
  { title: '场地编号', dataIndex: 'courtCode', key: 'courtCode' },
  { title: '状态', key: 'status' },
  { title: '操作', key: 'action', width: 200 }
]

const sportTypeMap = {
  1: '羽毛球',
  2: '篮球',
  3: '乒乓球',
  4: '网球',
  5: '排球'
}

const getSportTypeLabel = (type) => sportTypeMap[type] || type

const loadVenues = async () => {
  try {
    const result = await getVenueList({ current: 1, size: 100 })
    venues.value = result.records
  } catch (e) {
    console.error(e)
  }
}

const loadCourts = async () => {
  loading.value = true
  try {
    const result = await getCourtList({
      current: pagination.current,
      size: pagination.pageSize,
      venueId: selectedVenue.value
    })
    courts.value = result.records
    pagination.total = result.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadCourts()
}

const handleEdit = (record) => {
  router.push(`/court/${record.id}/edit`)
}

const handleToggleStatus = async (record) => {
  try {
    const newStatus = record.status === 1 ? 0 : 1
    await updateCourtStatus(record.id, newStatus)
    message.success('状态更新成功')
    loadCourts()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = async (record) => {
  try {
    await deleteCourt(record.id)
    message.success('删除成功')
    loadCourts()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadVenues()
  loadCourts()
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
