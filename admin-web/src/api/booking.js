import request from '@/utils/request'

export const getTodaySchedule = (venueId) => {
  return request({
    url: '/admin/bookings/today',
    params: { venueId }
  })
}

export const getBookingList = (params) => {
  return request({
    url: '/admin/bookings',
    params
  })
}

export const getBookingDetail = (bookingNo) => {
  return request({
    url: `/admin/bookings/${bookingNo}`
  })
}

export const cancelBooking = (bookingNo) => {
  return request({
    url: `/admin/bookings/${bookingNo}/cancel`,
    method: 'POST'
  })
}
