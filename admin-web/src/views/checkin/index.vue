<template>
  <div class="checkin-page">
    <div class="page-header">
      <h2>扫码核销</h2>
    </div>
    
    <a-row :gutter="24">
      <a-col :span="12">
        <a-card title="手动核销">
          <a-input-search
            v-model:value="bookingNo"
            placeholder="输入预约单号"
            enter-button="核销"
            size="large"
            @search="handleManualCheckin"
          />
          <p class="hint">输入用户预约单号进行核销</p>
        </a-card>
      </a-col>
      
      <a-col :span="12">
        <a-card title="扫码核销">
          <div class="scanner-box">
            <ScanOutlined class="scan-icon" />
            <p>扫描用户核销码</p>
            <a-button type="primary" @click="startScan">
              开始扫码
            </a-button>
          </div>
        </a-card>
      </a-col>
    </a-row>
    
    <a-modal
      v-model:open="resultVisible"
      :title="checkinResult.success ? '核销成功' : '核销失败'"
      :footer="null"
      width="500px"
    >
      <a-result
        :status="checkinResult.success ? 'success' : 'error'"
        :title="checkinResult.message"
      >
        <template #extra>
          <div class="checkin-info" v-if="checkinResult.success">
            <a-descriptions :column="1" bordered size="small">
              <a-descriptions-item label="预约人">{{ checkinResult.userName }}</a-descriptions-item>
              <a-descriptions-item label="球馆">{{ checkinResult.venueName }}</a-descriptions-item>
              <a-descriptions-item label="场地">{{ checkinResult.courtName }}</a-descriptions-item>
              <a-descriptions-item label="时段">{{ checkinResult.startTime }} - {{ checkinResult.endTime }}</a-descriptions-item>
            </a-descriptions>
          </div>
          <a-button type="primary" @click="resultVisible = false" style="margin-top: 16px">
            确定
          </a-button>
        </template>
      </a-result>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { ScanOutlined } from '@ant-design/icons-vue'
import { manualCheckin } from '@/api/checkin'
import { ApiError } from '@/utils/request'

const bookingNo = ref('')
const resultVisible = ref(false)
const checkinResult = reactive({
  success: false,
  message: '',
  userName: '',
  venueName: '',
  courtName: '',
  startTime: '',
  endTime: ''
})

const startScan = () => {
  message.info('请在微信小程序中实现扫码功能')
}

const handleManualCheckin = async () => {
  if (!bookingNo.value) {
    message.warning('请输入预约单号')
    return
  }
  
  try {
    const result = await manualCheckin(bookingNo.value)
    const data = result.data
    checkinResult.success = true
    checkinResult.message = result.message || '核销成功'
    checkinResult.userName = data.userName
    checkinResult.venueName = data.venueName
    checkinResult.courtName = data.courtName
    checkinResult.startTime = data.startTime
    checkinResult.endTime = data.endTime
    resultVisible.value = true
    bookingNo.value = ''
  } catch (e) {
    checkinResult.success = false
    if (e instanceof ApiError) {
      checkinResult.message = e.message
    } else {
      checkinResult.message = '核销失败'
    }
    resultVisible.value = true
  }
}
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

.scanner-box {
  text-align: center;
  padding: 40px 0;
}

.scan-icon {
  font-size: 64px;
  color: #1890ff;
  margin-bottom: 16px;
}

.hint {
  margin-top: 12px;
  color: #999;
  font-size: 12px;
}

.checkin-info {
  text-align: left;
}
</style>
