<template>
  <div class="admin-user-list">
    <div class="page-header">
      <h2>后台账号管理</h2>
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
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getAdminUserList, updateAdminUserVenues } from '@/api/admin-user'
import { getVenueList } from '@/api/venue'
import { ApiError } from '@/utils/request'

const loading = ref(false)
const saving = ref(false)
const adminUsers = ref([])
const venueOptions = ref([])
const modalVisible = ref(false)
const currentAdminUser = ref(null)
const selectedVenueIds = ref([])

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
