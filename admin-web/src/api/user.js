import request from '@/utils/request'

export const getUserList = (params) => {
  return request({
    url: '/admin/users',
    params
  })
}

export const createPresetUser = (data) => {
  return request({
    url: '/admin/users',
    method: 'POST',
    data
  })
}

export const getUserDetail = (id) => {
  return request({
    url: `/admin/users/${id}`
  })
}

export const getUserBookings = (id, params) => {
  return request({
    url: `/admin/users/${id}/bookings`,
    params
  })
}

export const getUserViolations = (id, params) => {
  return request({
    url: `/admin/users/${id}/violations`,
    params
  })
}

export const updateUserStatus = (id, status) => {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'PATCH',
    params: { status }
  })
}
