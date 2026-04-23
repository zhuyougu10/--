<template>
  <div class="user-list">
    <div class="page-header">
      <h2>用户管理</h2>
      <a-button type="primary" @click="openCreateModal">新增预置用户</a-button>
    </div>
    
    <a-card class="search-card">
      <a-form layout="inline">
        <a-form-item label="关键词">
          <a-input
            v-model:value="searchParams.keyword"
            placeholder="姓名/手机号"
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchParams.status"
            style="width: 120px"
            placeholder="选择状态"
            allowClear
          >
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
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
      :data-source="users"
      :loading="loading"
      :pagination="pagination"
      rowKey="id"
      style="margin-top: 16px"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '正常' : '禁用' }}
          </a-tag>
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="handleDetail(record)">
              详情
            </a-button>
            <a-popconfirm
              :title="record.status === 1 ? '确定禁用该用户？' : '确定启用该用户？'"
              @confirm="handleToggleStatus(record)"
            >
              <a-button type="link" size="small">
                {{ record.status === 1 ? '禁用' : '启用' }}
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal
      v-model:open="createVisible"
      title="新增预置用户"
      :confirm-loading="createLoading"
      @ok="handleCreateUser"
      @cancel="resetCreateForm"
    >
      <a-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        layout="vertical"
      >
        <a-form-item label="姓名" name="name">
          <a-input v-model:value="createForm.name" placeholder="请输入姓名" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="createForm.phone" placeholder="请输入手机号（选填）" />
        </a-form-item>
        <a-form-item label="工号/学号" name="studentNo">
          <a-input v-model:value="createForm.studentNo" placeholder="请输入工号或学号" />
        </a-form-item>
        <a-form-item label="用户类型" name="userType">
          <a-select v-model:value="createForm.userType" placeholder="请选择用户类型">
            <a-select-option :value="1">学生</a-select-option>
            <a-select-option :value="2">教师</a-select-option>
            <a-select-option :value="3">外部人员</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
    
    <a-modal
      v-model:open="detailVisible"
      title="用户详情"
      :footer="null"
      width="700px"
    >
      <a-descriptions :column="2" bordered v-if="currentUser">
        <a-descriptions-item label="工号/学号">{{ currentUser.studentNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="currentUser.status === 1 ? 'green' : 'red'">
            {{ currentUser.status === 1 ? '正常' : '禁用' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="姓名">{{ currentUser.name || '-' }}</a-descriptions-item>
        <a-descriptions-item label="手机号">{{ currentUser.phone || '-' }}</a-descriptions-item>
        <a-descriptions-item label="用户类型">{{ currentUser.userTypeText || '-' }}</a-descriptions-item>
        <a-descriptions-item label="绑定状态">{{ currentUser.isBound === 1 ? '已绑定' : '未绑定' }}</a-descriptions-item>
        <a-descriptions-item label="注册时间">{{ currentUser.createdAt }}</a-descriptions-item>
        <a-descriptions-item label="爽约次数">{{ currentUser.noShowCount || 0 }}</a-descriptions-item>
      </a-descriptions>
      
      <a-divider />
      
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="bookings" tab="预约记录">
          <a-table
            :columns="bookingColumns"
            :data-source="userBookings"
            :loading="bookingsLoading"
            size="small"
            rowKey="id"
            :pagination="{ pageSize: 5 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="getStatusColor(record.status)">
                  {{ record.statusText }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
        <a-tab-pane key="violations" tab="违约记录">
          <a-table
            :columns="violationColumns"
            :data-source="userViolations"
            :loading="violationsLoading"
            size="small"
            rowKey="id"
            :pagination="{ pageSize: 5 }"
          />
        </a-tab-pane>
      </a-tabs>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { createPresetUser, getUserList, getUserBookings, getUserViolations, updateUserStatus } from '@/api/user'
import { ApiError } from '@/utils/request'

const loading = ref(false)
const createLoading = ref(false)
const users = ref([])
const detailVisible = ref(false)
const createVisible = ref(false)
const currentUser = ref(null)
const userBookings = ref([])
const userViolations = ref([])
const bookingsLoading = ref(false)
const violationsLoading = ref(false)
const activeTab = ref('bookings')
const createFormRef = ref(null)

const searchParams = reactive({
  keyword: '',
  status: null
})

const createForm = reactive({
  name: '',
  phone: '',
  studentNo: '',
  userType: undefined
})

const createRules = {
  name: [{ required: true, message: '请输入姓名' }],
  studentNo: [{ required: true, message: '请输入工号/学号' }],
  userType: [{ required: true, message: '请选择用户类型' }]
}

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const columns = [
  { title: '工号/学号', dataIndex: 'studentNo', key: 'studentNo' },
  { title: '姓名', dataIndex: 'name', key: 'name' },
  { title: '手机号', dataIndex: 'phone', key: 'phone' },
  { title: '爽约次数', dataIndex: 'noShowCount', key: 'noShowCount' },
  { title: '状态', key: 'status' },
  { title: '操作', key: 'action', width: 120 }
]

const bookingColumns = [
  { title: '预约单号', dataIndex: 'bookingNo', key: 'bookingNo' },
  { title: '球馆', dataIndex: 'venueName', key: 'venueName' },
  { title: '日期', dataIndex: 'bookingDate', key: 'bookingDate' },
  { title: '状态', key: 'status' }
]

const violationColumns = [
  { title: '类型', dataIndex: 'typeText', key: 'type' },
  { title: '预约单号', dataIndex: 'bookingNo', key: 'bookingNo' },
  { title: '时间', dataIndex: 'createdAt', key: 'createdAt' },
  { title: '禁用天数', dataIndex: 'banDays', key: 'banDays' }
]

const statusMap = {
  0: { text: '已取消', color: 'default' },
  1: { text: '待使用', color: 'blue' },
  2: { text: '已核销', color: 'green' },
  3: { text: '已爽约', color: 'red' }
}

const getStatusColor = (status) => statusMap[status]?.color || 'default'

const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.pageSize,
      keyword: searchParams.keyword || undefined,
      status: searchParams.status
    }
    const result = await getUserList(params)
    users.value = result.data.records
    pagination.total = result.data.total
  } catch (e) {
    if (e instanceof ApiError) {
      message.error(e.message)
    } else {
      message.error('加载用户列表失败')
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadUsers()
}

const handleReset = () => {
  searchParams.keyword = ''
  searchParams.status = null
  handleSearch()
}

const handleTableChange = (pag) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadUsers()
}

const resetCreateForm = () => {
  createForm.name = ''
  createForm.phone = ''
  createForm.studentNo = ''
  createForm.userType = undefined
  createFormRef.value?.clearValidate()
}

const openCreateModal = () => {
  resetCreateForm()
  createVisible.value = true
}

const handleCreateUser = async () => {
  try {
    await createFormRef.value.validate()
    createLoading.value = true
    const result = await createPresetUser({
      ...createForm,
      phone: createForm.phone || undefined
    })
    message.success(result.message || '预置用户创建成功')
    createVisible.value = false
    resetCreateForm()
    pagination.current = 1
    await loadUsers()
  } catch (e) {
    if (e instanceof ApiError) {
      message.error(e.message)
    } else if (e?.errorFields) {
      return
    } else {
      message.error('创建预置用户失败')
    }
  } finally {
    createLoading.value = false
  }
}

const handleDetail = async (record) => {
  currentUser.value = record
  detailVisible.value = true
  activeTab.value = 'bookings'
  loadUserBookings(record.id)
}

const loadUserBookings = async (userId) => {
  bookingsLoading.value = true
  try {
    const result = await getUserBookings(userId, { current: 1, size: 10 })
    userBookings.value = Array.isArray(result.data) ? result.data : (result.data?.records || [])
  } catch (e) {
    if (e instanceof ApiError) {
      message.error(e.message)
    }
  } finally {
    bookingsLoading.value = false
  }
}

const loadUserViolations = async (userId) => {
  violationsLoading.value = true
  try {
    const result = await getUserViolations(userId, { current: 1, size: 10 })
    userViolations.value = Array.isArray(result.data) ? result.data : (result.data?.records || [])
  } catch (e) {
    if (e instanceof ApiError) {
      message.error(e.message)
    }
  } finally {
    violationsLoading.value = false
  }
}

const handleToggleStatus = async (record) => {
  try {
    const newStatus = record.status === 1 ? 0 : 1
    const result = await updateUserStatus(record.id, newStatus)
    message.success(result.message || '状态更新成功')
    loadUsers()
  } catch (e) {
    if (e instanceof ApiError) {
      message.error(e.message)
    } else {
      message.error('状态更新失败')
    }
  }
}

onMounted(() => {
  loadUsers()
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
