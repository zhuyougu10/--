import request from '@/utils/request'

export const getVenueList = (params) => {
  return request({
    url: '/admin/venues',
    params
  })
}

export const getVenueDetail = (id) => {
  return request({
    url: `/admin/venues/${id}`
  })
}

export const createVenue = (data) => {
  return request({
    url: '/admin/venues',
    method: 'POST',
    data
  })
}

export const updateVenue = (id, data) => {
  return request({
    url: `/admin/venues/${id}`,
    method: 'PUT',
    data
  })
}

export const updateVenueStatus = (id, status) => {
  return request({
    url: `/admin/venues/${id}/status`,
    method: 'PATCH',
    params: { status }
  })
}

export const deleteVenue = (id) => {
  return request({
    url: `/admin/venues/${id}`,
    method: 'DELETE'
  })
}
