<template>
  <div class="admin-user-list">
    <div class="page-header">
      <h2>后台账号管理</h2>
      <a-button type="primary" @click="openCreateModal">新增场馆管理员</a-button>
    </div>

    <a-alert
      type="info"
      show-icon
      message="仅支持为非管理员账号分配球馆；保存后重新登录即可刷新该账号的菜单和数据范围。"
      style="margin-bottom: 16px"
    />

    <a-table
      :columns="columns"
      :data-source="adminUsers"
      :loading="loading"
      row-key="id"
      :pagination="false"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'roles'">
          <a-space wrap>
            <a-tag v-for="role in record.roleTexts" :key="role" color="blue">{{ role }}</a-tag>
          </a-space>
        </template>

        <template v-if="column.key === 'venues'">
          <a-space wrap>
            <a-tag v-for="venue in record.venueNames" :key="venue" color="green">{{ venue }}</a-tag>
            <span v-if="!record.venueNames?.length" class="text-muted">未分配球馆</span>
          </a-space>
        </template>

        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ record.status === 1 ? '正常' : '禁用' }}
          </a-tag>
        </template>

        <template v-if="column.key === 'action'">
          <a-button type="link" size="small" :disabled="record.roleCodes?.includes('ADMIN')" @click="openVenueModal(record)">
            管理球馆
          </a-button>
        </template>
      </template>
    </a-table>

    <a-modal
      v-model:open="createModalVisible"
      title="新增场馆管理员"
      :confirm-loading="createSaving"
      @ok="handleCreateAdminUser"
      @cancel="resetCreateForm"
    >
      <a-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        layout="vertical"
      >
        <a-form-item label="用户名" name="username">
          <a-input v-model:value="createForm.username" placeholder="请输入登录用户名" />
        </a-form-item>
        <a-form-item label="姓名" name="name">
          <a-input v-model:value="createForm.name" placeholder="请输入姓名" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="createForm.phone" placeholder="请输入手机号（选填）" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="createForm.email" placeholder="请输入邮箱（选填）" />
        </a-form-item>
        <a-form-item label="初始密码" name="password">
          <a-input-password v-model:value="createForm.password" placeholder="请输入初始密码" />
        </a-form-item>
        <a-form-item label="可管理球馆" name="venueIds">
          <a-select
            v-model:value="createForm.venueIds"
            mode="multiple"
            style="width: 100%"
            placeholder="请选择至少一个球馆"
            :options="venueOptions"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="modalVisible"
      title="分配可管理球馆"
      :confirm-loading="saving"
      @ok="handleSaveVenues"
    >
      <div v-if="currentAdminUser">
        <p class="modal-desc">账号：{{ currentAdminUser.name || currentAdminUser.username }}</p>
        <a-select
          v-model:value="selectedVenueIds"
          mode="multiple"
          style="width: 100%"
          placeholder="选择该账号可管理的球馆"
          :options="venueOptions"
        />
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { createAdminUser, getAdminUserList, updateAdminUserVenues } from '@/api/admin-user'
import { getVenueList } from '@/api/venue'
import { ApiError } from '@/utils/request'

const loading = ref(false)
const saving = ref(false)
const createSaving = ref(false)
const adminUsers = ref([])
const venueOptions = ref([])
const modalVisible = ref(false)
const createModalVisible = ref(false)
const currentAdminUser = ref(null)
const selectedVenueIds = ref([])
const createFormRef = ref(null)

const createForm = reactive({
  username: '',
  name: '',
  phone: '',
  email: '',
  password: '',
  venueIds: []
})

const createRules = {
  username: [{ required: true, message: '请输入用户名' }],
  name: [{ required: true, message: '请输入姓名' }],
  password: [
    { required: true, message: '请输入初始密码' },
    { min: 8, message: '初始密码至少 8 位' }
  ],
  venueIds: [{ required: true, type: 'array', min: 1, message: '请至少选择一个球馆' }]
}

const columns = [
  { title: '用户名', dataIndex: 'username', key: 'username' },
  { title: '姓名', dataIndex: 'name', key: 'name' },
  { title: '角色', key: 'roles' },
  { title: '状态', key: 'status', width: 100 },
  { title: '可管理球馆', key: 'venues' },
  { title: '操作', key: 'action', width: 120 }
]

const loadAdminUsers = async () => {
  loading.value = true
  try {
    const result = await getAdminUserList()
    adminUsers.value = result.data || []
  } catch (e) {
    message.error(e instanceof ApiError ? e.message : '加载后台账号失败')
  } finally {
    loading.value = false
  }
}

const loadVenueOptions = async () => {
  try {
    const result = await getVenueList({ current: 1, size: 1000 })
    venueOptions.value = (result.data.records || []).map(item => ({
      label: item.name,
      value: item.id
    }))
  } catch (e) {
    message.error(e instanceof ApiError ? e.message : '加载球馆选项失败')
  }
}

const openVenueModal = (record) => {
  currentAdminUser.value = record
  selectedVenueIds.value = [...(record.venueIds || [])]
  modalVisible.value = true
}

const resetCreateForm = () => {
  createForm.username = ''
  createForm.name = ''
  createForm.phone = ''
  createForm.email = ''
  createForm.password = ''
  createForm.venueIds = []
  createFormRef.value?.clearValidate()
}

const openCreateModal = () => {
  resetCreateForm()
  createModalVisible.value = true
}

const handleCreateAdminUser = async () => {
  try {
    await createFormRef.value.validate()
    createSaving.value = true
    const result = await createAdminUser({
      ...createForm,
      phone: createForm.phone || undefined,
      email: createForm.email || undefined
    })
    message.success(result.message || '场馆管理员创建成功')
    createModalVisible.value = false
    resetCreateForm()
    await loadAdminUsers()
  } catch (e) {
    if (e instanceof ApiError) {
      message.error(e.message)
    } else if (e?.errorFields) {
      return
    } else {
      message.error('创建场馆管理员失败')
    }
  } finally {
    createSaving.value = false
  }
}

const handleSaveVenues = async () => {
  if (!currentAdminUser.value) return

  saving.value = true
  try {
    const result = await updateAdminUserVenues(currentAdminUser.value.id, selectedVenueIds.value)
    message.success(result.message || '球馆分配已更新')
    modalVisible.value = false
    await loadAdminUsers()
  } catch (e) {
    message.error(e instanceof ApiError ? e.message : '保存球馆分配失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadAdminUsers(), loadVenueOptions()])
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

.modal-desc {
  margin-bottom: 12px;
  color: #666;
}

.text-muted {
  color: #999;
}
</style>
