import request from '@/utils/request'

export const getCourtList = (params) => {
  return request({
    url: '/admin/courts',
    params
  })
}

export const getCourtDetail = (id) => {
  return request({
    url: `/admin/courts/${id}`
  })
}

export const createCourt = (data) => {
  return request({
    url: '/admin/courts',
    method: 'POST',
    data
  })
}

export const updateCourt = (id, data) => {
  return request({
    url: `/admin/courts/${id}`,
    method: 'PUT',
    data
  })
}

export const updateCourtStatus = (id, status, reason) => {
  return request({
    url: `/admin/courts/${id}/status`,
    method: 'PATCH',
    params: { status, reason }
  })
}

export const deleteCourt = (id) => {
  return request({
    url: `/admin/courts/${id}`,
    method: 'DELETE'
  })
}

export const getCourtsByVenue = (venueId) => {
  return request({
    url: `/admin/courts/venue/${venueId}`
  })
}
