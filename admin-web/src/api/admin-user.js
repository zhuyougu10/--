import request from '@/utils/request'

export const getAdminUserList = () => {
  return request({
    url: '/admin/admin-users'
  })
}

export const updateAdminUserVenues = (id, venueIds) => {
  return request({
    url: `/admin/admin-users/${id}/venues`,
    method: 'PUT',
    data: { venueIds }
  })
}
