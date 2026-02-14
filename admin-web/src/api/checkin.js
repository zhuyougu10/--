import request from '@/utils/request'

export const scanCheckin = (token) => {
  return request({
    url: '/admin/checkin/scan',
    method: 'POST',
    data: { token, checkinMethod: 1 }
  })
}

export const manualCheckin = (bookingNo) => {
  return request({
    url: '/admin/checkin/manual',
    method: 'POST',
    data: { bookingNo }
  })
}

export const markNoShow = (bookingNo) => {
  return request({
    url: '/admin/violations/no-show',
    method: 'POST',
    data: { bookingNo }
  })
}

export const getCheckinRecords = (params) => {
  return request({
    url: '/admin/checkin/records',
    params
  })
}
