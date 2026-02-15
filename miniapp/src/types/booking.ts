export interface Booking {
  id?: number
  bookingNo: string
  userId?: number
  userName?: string
  userPhone?: string
  venueId: number
  venueName: string
  courtId: number
  courtName: string
  bookingDate: string
  startTime: string
  endTime: string
  slotCount?: number
  bookingType?: number
  status: BookingStatus
  statusText?: string
  cancelReason?: string
  cancelledAt?: string
  checkedInAt?: string
  remark?: string
  createdAt: string
  qrToken?: string
}

export type BookingStatus = 1 | 2 | 3 | 4

export interface CreateBookingParams {
  venueId: number
  courtId: number
  bookingDate: string
  startTime: string
  endTime: string
  remark?: string
  bookingType?: number
}
