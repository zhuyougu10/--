import { ref } from 'vue'
import { 
  createBooking, 
  getBookingDetail, 
  getMyBookings, 
  cancelBooking as cancelBookingApi,
  getQrCode 
} from '@/api/booking'
import type { Booking, CreateBookingParams } from '@/types'

export function useBooking() {
  const bookings = ref<Booking[]>([])
  const currentBooking = ref<Booking | null>(null)
  const loading = ref(false)
  const submitting = ref(false)

  const loadBookings = async (status?: number) => {
    loading.value = true
    try {
      bookings.value = await getMyBookings(status ?? null)
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  const loadBookingDetail = async (bookingNo: string) => {
    loading.value = true
    try {
      currentBooking.value = await getBookingDetail(bookingNo)
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  const submitBooking = async (params: CreateBookingParams): Promise<string | null> => {
    submitting.value = true
    try {
      const result = await createBooking(params)
      return result.bookingNo
    } catch (e) {
      console.error(e)
      return null
    } finally {
      submitting.value = false
    }
  }

  const cancelBooking = async (bookingNo: string, reason: string): Promise<boolean> => {
    try {
      await cancelBookingApi(bookingNo, reason)
      return true
    } catch (e) {
      console.error(e)
      return false
    }
  }

  return {
    bookings,
    currentBooking,
    loading,
    submitting,
    loadBookings,
    loadBookingDetail,
    submitBooking,
    cancelBooking
  }
}

export function useQrCode() {
  const qrCodeUrl = ref('')
  const qrExpireAt = ref<string | null>(null)

  const refreshQrCode = async (bookingNo: string) => {
    try {
      const result = await getQrCode(bookingNo)
      qrCodeUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(result.token)}`
      qrExpireAt.value = result.expiresAt
    } catch (e) {
      console.error(e)
    }
  }

  return {
    qrCodeUrl,
    qrExpireAt,
    refreshQrCode
  }
}

export function useBookingStatus() {
  const statusTextMap: Record<number, string> = {
    1: '待使用',
    2: '已取消',
    3: '已完成',
    4: '爽约'
  }

  const statusClassMap: Record<number, string> = {
    1: 'pending',
    2: 'cancelled',
    3: 'completed',
    4: 'no-show'
  }

  const getStatusText = (status: number): string => {
    return statusTextMap[status] || '未知'
  }

  const getStatusClass = (status: number): string => {
    return statusClassMap[status] || ''
  }

  return {
    getStatusText,
    getStatusClass
  }
}
