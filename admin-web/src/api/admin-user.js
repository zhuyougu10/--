import request from '@/utils/request'

export const getAdminUserList = () => {
  return request({
    url: '/admin/admin-users'
  })
}

export const createAdminUser = (data) => {
  return request({
    url: '/admin/admin-users',
    method: 'POST',
    data
  })
}

export const updateAdminUserVenues = (id, venueIds) => {
  return request({
    url: `/admin/admin-users/${id}/venues`,
    method: 'PUT',
    data: { venueIds }
  })
}
