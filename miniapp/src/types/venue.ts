export interface Venue {
  id: number
  name: string
  code?: string
  sportType: string
  location: string
  description?: string
  imageUrl?: string
  openDays?: string
  openTime: string
  closeTime: string
  slotMinutes?: number
  bookAheadDays?: number
  cancelCutoffMinutes?: number
  dailySlotLimit?: number
  status: number
  courtCount?: number
  courts?: Court[]
}

export interface Court {
  id: number
  venueId?: number
  venueName?: string
  name: string
  courtNo?: string
  sportType?: string
  floorType?: string
  features?: string
  status: number
  statusReason?: string
  statusUntil?: string
  sortOrder?: number
}

export interface TimeSlot {
  courtId?: number
  courtName?: string
  date?: string
  startTime: string
  endTime: string
  status: string
  booking?: BookingInfo
}

export interface BookingInfo {
  bookingNo: string
  userName: string
  userPhone: string
}
