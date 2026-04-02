import { ref } from 'vue'
import { 
  createBooking, 
  getBookingDetail, 
  getMyBookings, 
  cancelBooking as cancelBookingApi,
  getQrCode 
} from '@/api/booking'
import type { Booking, BookingWithDisplay, CreateBookingParams } from '@/types'

export const isBookingExpired = (booking: Booking): boolean => {
  if (!booking || booking.status !== 1) return false
  if (!booking.bookingDate || !booking.endTime) return false
  const endAt = new Date(`${booking.bookingDate} ${booking.endTime}`)
  if (Number.isNaN(endAt.getTime())) return false
  return Date.now() > endAt.getTime()
}

export const getBookingDisplayStatus = (booking: Booking): { text: string; className: string; isExpired: boolean } => {
  if (isBookingExpired(booking)) {
    return {
      text: '已过期',
      className: 'expired',
      isExpired: true
    }
  }

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

  return {
    text: statusTextMap[booking.status] || '未知',
    className: statusClassMap[booking.status] || '',
    isExpired: false
  }
}

export const toBookingWithDisplay = (booking: Booking): BookingWithDisplay => {
  const display = getBookingDisplayStatus(booking)
  return {
    ...booking,
    displayStatusText: display.text,
    displayStatusClass: display.className,
    isExpired: display.isExpired
  }
}

export function useBooking() {
  const bookings = ref<BookingWithDisplay[]>([])
  const currentBooking = ref<BookingWithDisplay | null>(null)
  const loading = ref(false)
  const submitting = ref(false)

  const loadBookings = async (status?: number) => {
    loading.value = true
    try {
      const list = await getMyBookings(status ?? null)
      bookings.value = (list || []).map(toBookingWithDisplay)
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  const loadBookingDetail = async (bookingNo: string) => {
    loading.value = true
    try {
      const detail = await getBookingDetail(bookingNo)
      currentBooking.value = detail ? toBookingWithDisplay(detail) : null
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
  const qrToken = ref('')

  const refreshQrCode = async (bookingNo: string) => {
    try {
      const result = await getQrCode(bookingNo)
      qrToken.value = result.token
      qrCodeUrl.value = ''
      qrExpireAt.value = result.expiresAt
    } catch (e) {
      console.error(e)
    }
  }

  return {
    qrCodeUrl,
    qrToken,
    qrExpireAt,
    refreshQrCode
  }
}

export function useBookingStatus() {
  const getStatusText = (status: number, expired = false): string => {
    if (expired) return '已过期'
    return getBookingDisplayStatus({ status } as Booking).text
  }

  const getStatusClass = (status: number, expired = false): string => {
    if (expired) return 'expired'
    return getBookingDisplayStatus({ status } as Booking).className
  }

  return {
    getStatusText,
    getStatusClass
  }
}
