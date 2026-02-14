<template>
  <div class="venue-form">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑球馆' : '新建球馆' }}</h2>
      <a-button @click="$router.back()">返回</a-button>
    </div>
    
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      :label-col="{ span: 4 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item label="球馆名称" name="name">
        <a-input v-model:value="formState.name" placeholder="请输入球馆名称" />
      </a-form-item>
      
      <a-form-item label="地址" name="address">
        <a-input v-model:value="formState.address" placeholder="请输入地址" />
      </a-form-item>
      
      <a-form-item label="运动类型" name="sportTypes">
        <a-select
          v-model:value="formState.sportTypes"
          mode="multiple"
          placeholder="请选择运动类型"
        >
          <a-select-option :value="1">羽毛球</a-select-option>
          <a-select-option :value="2">篮球</a-select-option>
          <a-select-option :value="3">乒乓球</a-select-option>
          <a-select-option :value="4">网球</a-select-option>
          <a-select-option :value="5">排球</a-select-option>
        </a-select>
      </a-form-item>
      
      <a-form-item label="营业时间" name="openTime">
        <a-time-picker
          v-model:value="formState.openTime"
          format="HH:mm"
          placeholder="开馆时间"
          style="width: 45%"
        />
        <span style="margin: 0 8px">至</span>
        <a-time-picker
          v-model:value="formState.closeTime"
          format="HH:mm"
          placeholder="闭馆时间"
          style="width: 45%"
        />
      </a-form-item>
      
      <a-form-item label="时段长度(分钟)" name="slotMinutes">
        <a-input-number
          v-model:value="formState.slotMinutes"
          :min="30"
          :max="120"
          :step="15"
        />
      </a-form-item>
      
      <a-form-item label="可提前预约天数" name="bookAheadDays">
        <a-input-number
          v-model:value="formState.bookAheadDays"
          :min="1"
          :max="30"
        />
      </a-form-item>
      
      <a-form-item label="取消截止时间(分钟)" name="cancelCutoffMinutes">
        <a-input-number
          v-model:value="formState.cancelCutoffMinutes"
          :min="0"
          :max="1440"
        />
        <span class="form-hint">预约开始前多少分钟内不可取消</span>
      </a-form-item>
      
      <a-form-item label="每日限额" name="dailySlotLimit">
        <a-input-number
          v-model:value="formState.dailySlotLimit"
          :min="1"
          :max="10"
        />
        <span class="form-hint">每人每天最多预约时段数</span>
      </a-form-item>
      
      <a-form-item label="联系电话" name="phone">
        <a-input v-model:value="formState.phone" placeholder="请输入联系电话" />
      </a-form-item>
      
      <a-form-item label="简介" name="description">
        <a-textarea
          v-model:value="formState.description"
          placeholder="请输入简介"
          :rows="4"
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
import dayjs from 'dayjs'
import { getVenueDetail, createVenue, updateVenue } from '@/api/venue'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const isEdit = computed(() => !!route.params.id)
const venueId = computed(() => route.params.id)

const formState = reactive({
  name: '',
  address: '',
  sportTypes: [],
  openTime: null,
  closeTime: null,
  slotMinutes: 60,
  bookAheadDays: 7,
  cancelCutoffMinutes: 30,
  dailySlotLimit: 2,
  phone: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入球馆名称' }],
  address: [{ required: true, message: '请输入地址' }],
  sportTypes: [{ required: true, message: '请选择运动类型' }],
  openTime: [{ required: true, message: '请选择开馆时间' }],
  closeTime: [{ required: true, message: '请选择闭馆时间' }]
}

const loadVenue = async () => {
  if (!venueId.value) return
  try {
    const result = await getVenueDetail(venueId.value)
    Object.assign(formState, {
      ...result,
      openTime: result.openTime ? dayjs(result.openTime, 'HH:mm') : null,
      closeTime: result.closeTime ? dayjs(result.closeTime, 'HH:mm') : null
    })
  } catch (e) {
    console.error(e)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    const data = {
      ...formState,
      openTime: formState.openTime ? dayjs(formState.openTime).format('HH:mm') : null,
      closeTime: formState.closeTime ? dayjs(formState.closeTime).format('HH:mm') : null
    }
    
    if (isEdit.value) {
      await updateVenue(venueId.value, data)
      message.success('更新成功')
    } else {
      await createVenue(data)
      message.success('创建成功')
    }
    router.push('/venue')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (isEdit.value) {
    loadVenue()
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

.form-hint {
  margin-left: 8px;
  color: #999;
  font-size: 12px;
}
</style>
