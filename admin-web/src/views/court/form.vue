<template>
  <div class="court-form">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑场地' : '新建场地' }}</h2>
      <a-button @click="$router.back()">返回</a-button>
    </div>
    
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      :label-col="{ span: 4 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item label="所属球馆" name="venueId">
        <a-select
          v-model:value="formState.venueId"
          placeholder="请选择球馆"
          @change="handleVenueChange"
        >
          <a-select-option v-for="v in venues" :key="v.id" :value="v.id">
            {{ v.name }}
          </a-select-option>
        </a-select>
      </a-form-item>
      
      <a-form-item label="场地名称" name="name">
        <a-input v-model:value="formState.name" placeholder="请输入场地名称" />
      </a-form-item>
      
      <a-form-item label="场地编号" name="courtCode">
        <a-input v-model:value="formState.courtCode" placeholder="请输入场地编号" />
      </a-form-item>
      
      <a-form-item label="运动类型" name="sportType">
        <a-select v-model:value="formState.sportType" placeholder="请选择运动类型">
          <a-select-option :value="1">羽毛球</a-select-option>
          <a-select-option :value="2">篮球</a-select-option>
          <a-select-option :value="3">乒乓球</a-select-option>
          <a-select-option :value="4">网球</a-select-option>
          <a-select-option :value="5">排球</a-select-option>
        </a-select>
      </a-form-item>
      
      <a-form-item label="是否室内" name="isIndoor">
        <a-switch v-model:checked="formState.isIndoor" />
      </a-form-item>
      
      <a-form-item label="备注" name="remark">
        <a-textarea
          v-model:value="formState.remark"
          placeholder="请输入备注"
          :rows="3"
        />
      </a-form-item>
      
      <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
        <a-space>
          <a-button type="primary" @click="handleSubmit" :loading="loading">
            提交
          </a-button>
          <a-button @click="$router.back()">取消</a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getCourtDetail, createCourt, updateCourt } from '@/api/court'
import { getVenueList } from '@/api/venue'
import { ApiError } from '@/utils/request'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const venues = ref([])

const isEdit = computed(() => !!route.params.id)
const courtId = computed(() => route.params.id)

const formState = reactive({
  venueId: null,
  name: '',
  courtCode: '',
  sportType: null,
  isIndoor: true,
  remark: ''
})

const rules = {
  venueId: [{ required: true, message: '请选择球馆' }],
  name: [{ required: true, message: '请输入场地名称' }],
  courtCode: [{ required: true, message: '请输入场地编号' }],
  sportType: [{ required: true, message: '请选择运动类型' }]
}

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

const loadCourt = async () => {
  if (!courtId.value) return
  try {
    const result = await getCourtDetail(courtId.value)
    Object.assign(formState, result.data)
  } catch (e) {
    if (e instanceof ApiError) {
      message.error(e.message)
    } else {
      message.error('加载场地信息失败')
    }
  }
}

const handleVenueChange = (venueId) => {
  const venue = venues.value.find(v => v.id === venueId)
  if (venue && venue.sportTypes && venue.sportTypes.length > 0) {
    formState.sportType = venue.sportTypes[0]
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    let result
    if (isEdit.value) {
      result = await updateCourt(courtId.value, formState)
    } else {
      result = await createCourt(formState)
    }
    message.success(result.message || (isEdit.value ? '更新成功' : '创建成功'))
    router.push('/court')
  } catch (e) {
    if (e instanceof ApiError) {
      message.error(e.message)
    } else {
      message.error('提交失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadVenues()
  if (isEdit.value) {
    loadCourt()
  }
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
