<template>
  <div class="checkin-page">
    <div class="page-header">
      <h2>扫码核销</h2>
    </div>
    
    <a-row :gutter="16">
      <a-col :xs="24" :md="12">
        <a-card title="手动核销">
          <a-input-search
            v-model:value="manualInput"
            placeholder="输入预约单号或核销码"
            enter-button="核销"
            size="large"
            @search="handleManualInput"
          />
          <p class="hint">可输入预约单号进行手动核销，或输入核销码进行扫码核销</p>
        </a-card>
      </a-col>
      
      <a-col :xs="24" :md="12">
        <a-card title="扫码核销">
          <div class="scanner-box">
            <qrcode-stream
              v-if="scanning"
              class="scanner-video"
              :paused="scanPaused"
              :constraints="cameraConstraints"
              @detect="onDetect"
              @error="onScanError"
            />
            <div v-if="!scanning" class="scan-placeholder">
              <ScanOutlined class="scan-icon" />
              <p>扫描用户核销码</p>
            </div>
            <p class="hint">{{ scanStatus }}</p>
            <a-space>
              <a-button type="primary" @click="startScan" :disabled="scanning">
                开始扫码
              </a-button>
              <a-button @click="openImageCapture">
                拍照扫码
              </a-button>
              <a-button @click="stopScan" :disabled="!scanning">
                停止扫码
              </a-button>
            </a-space>
            <input
              ref="fileInputRef"
              type="file"
              accept="image/*"
              capture="environment"
              style="display: none"
              @change="handleImageFile"
            />
          </div>
        </a-card>
      </a-col>
    </a-row>
    
    <a-modal
      v-model:open="resultVisible"
      :title="checkinResult.success ? '核销成功' : '核销失败'"
      :footer="null"
      :width="isMobile ? '92%' : 500"
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
import { ref, reactive, onBeforeUnmount } from 'vue'
import { message } from 'ant-design-vue'
import { ScanOutlined } from '@ant-design/icons-vue'
import { manualCheckin, scanCheckin } from '@/api/checkin'
import { ApiError } from '@/utils/request'
import jsQR from 'jsqr'
import { isMobileDevice } from '@/utils/device'
import { QrcodeStream } from 'vue-qrcode-reader'

const manualInput = ref('')
const fileInputRef = ref(null)
const scanning = ref(false)
const scanPaused = ref(false)
const scanStatus = ref('点击开始扫码，若设备不支持将自动降级为手动输入')
const isMobile = isMobileDevice()
const cameraConstraints = {
  facingMode: 'environment'
}

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

const stopScan = () => {
  scanning.value = false
  scanPaused.value = false
}

const applyCheckinResult = (result) => {
  const data = result.data
  checkinResult.success = true
  checkinResult.message = result.message || '核销成功'
  checkinResult.userName = data.userName
  checkinResult.venueName = data.venueName
  checkinResult.courtName = data.courtName
  checkinResult.startTime = data.startTime
  checkinResult.endTime = data.endTime
  resultVisible.value = true
}

const handleCheckinError = (e, fallbackMsg = '核销失败') => {
  checkinResult.success = false
  if (e instanceof ApiError) {
    checkinResult.message = e.message
  } else {
    checkinResult.message = fallbackMsg
  }
  resultVisible.value = true
}

const handleScanToken = async (token) => {
  if (scanPaused.value) return
  scanPaused.value = true
  try {
    const result = await scanCheckin(token)
    applyCheckinResult(result)
    stopScan()
  } catch (e) {
    handleCheckinError(e)
    scanPaused.value = false
  }
}

const decodeImageFile = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        canvas.width = img.width
        canvas.height = img.height
        const ctx = canvas.getContext('2d', { willReadFrequently: true })
        if (!ctx) {
          reject(new Error('无法解析图片'))
          return
        }
        ctx.drawImage(img, 0, 0)
        const imageData = ctx.getImageData(0, 0, img.width, img.height)
        const result = jsQR(imageData.data, img.width, img.height, {
          inversionAttempts: 'attemptBoth'
        })
        if (result?.data) {
          resolve(result.data)
        } else {
          reject(new Error('未识别到二维码'))
        }
      }
      img.onerror = () => reject(new Error('图片加载失败'))
      img.src = reader.result
    }
    reader.onerror = () => reject(new Error('图片读取失败'))
    reader.readAsDataURL(file)
  })
}

const openImageCapture = () => {
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
    fileInputRef.value.click()
  }
}

const handleImageFile = async (event) => {
  const file = event.target?.files?.[0]
  if (!file) return
  try {
    scanStatus.value = '正在解析图片二维码...'
    const token = await decodeImageFile(file)
    scanStatus.value = '识别成功，正在核销...'
    await handleScanToken(token)
  } catch (e) {
    scanStatus.value = '图片识别失败，请重试或手动输入核销码'
    message.warning(e.message || '图片识别失败')
  }
}

const onDetect = async (detectedCodes) => {
  if (!detectedCodes || detectedCodes.length === 0) return
  const raw = detectedCodes[0]?.rawValue || detectedCodes[0]?.raw || ''
  const token = typeof raw === 'string' ? raw.trim() : ''
  if (!token) return
  scanStatus.value = '识别成功，正在核销...'
  await handleScanToken(token)
}

const onScanError = (error) => {
  const name = error?.name || 'UnknownError'
  if (name === 'NotAllowedError') {
    scanStatus.value = '未授予摄像头权限，请在浏览器设置中允许后重试'
    message.warning('未授予摄像头权限，请允许访问后重试')
    return
  }
  if (name === 'NotFoundError') {
    scanStatus.value = '未检测到可用摄像头，请改用拍照扫码'
    message.warning('未检测到可用摄像头，请使用拍照扫码')
    return
  }
  if (name === 'NotSupportedError') {
    scanStatus.value = '当前环境不支持实时扫码，请使用拍照扫码'
    message.warning('当前环境不支持实时扫码，请使用拍照扫码')
    return
  }
  scanStatus.value = `扫码异常：${name}，请改用拍照扫码`
}

const startScan = async () => {
  if (!window.isSecureContext) {
    scanStatus.value = '当前为非安全上下文（HTTP），请使用 HTTPS 启动管理端后再实时扫码'
    message.warning('实时扫码需要 HTTPS。可执行 npm run dev:https 后用 https://IP:3000 访问')
    return
  }
  scanning.value = true
  scanPaused.value = false
  scanStatus.value = '请将二维码置于取景框内'
}

const handleManualInput = async () => {
  if (!manualInput.value) {
    message.warning('请输入预约单号或核销码')
    return
  }

  const value = manualInput.value.trim()
  if (value.toUpperCase().startsWith('QR')) {
    await handleScanToken(value)
    manualInput.value = ''
    return
  }
  
  try {
    const result = await manualCheckin(value)
    applyCheckinResult(result)
    manualInput.value = ''
  } catch (e) {
    handleCheckinError(e)
  }
}

onBeforeUnmount(() => {
  stopScan()
})
</script>

<style scoped>
.checkin-page {
  width: 100%;
}

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
  padding: 16px 0;
}

.scanner-video {
  width: 100%;
  max-height: 320px;
  border-radius: 12px;
  background: #000;
  margin-bottom: 12px;
}

.scan-placeholder {
  padding: 32px 0;
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

@media (max-width: 768px) {
  .page-header {
    margin-bottom: 16px;
  }

  .page-header h2 {
    font-size: 20px;
  }

  .scan-icon {
    font-size: 48px;
  }

  .scanner-video {
    max-height: 260px;
  }
}
</style>
