export interface Booking {
  bookingNo: string
  venueId: number
  venueName: string
  courtId: number
  courtName: string
  bookingDate: string
  startTime: string
  endTime: string
  status: BookingStatus
  createdAt: string
  cancelledAt?: string
  cancelReason?: string
  checkinTime?: string
}

export type BookingStatus = 1 | 2 | 3 | 4

export interface CreateBookingParams {
  venueId: number
  courtId: number
  bookingDate: string
  startTime: string
  endTime: string
}
