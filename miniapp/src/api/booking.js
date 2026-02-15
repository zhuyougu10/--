import request from '@/utils/request'

export const createBooking = (data) => {
  return request({
    url: '/bookings',
    method: 'POST',
    data
  })
}

export const cancelBooking = (bookingNo, reason) => {
  return request({
    url: `/bookings/${bookingNo}/cancel`,
    method: 'POST',
    data: { reason }
  })
}

export const getBookingDetail = (bookingNo) => {
  return request({
    url: `/bookings/${bookingNo}`
  })
}

export const getMyBookings = (status) => {
  return request({
    url: '/bookings/my',
    params: status ? { status } : {}
  })
}

export const getQrCode = (bookingNo) => {
  return request({
    url: `/qrcode/booking/${bookingNo}`
  })
}

export const getRecommendations = (data) => {
  return request({
    url: '/recommendations',
    method: 'POST',
    data
  })
}
