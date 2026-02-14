import request from '@/utils/request'

export const login = (data) => {
  return request({
    url: '/admin/auth/login',
    method: 'POST',
    data
  })
}

export const getProfile = () => {
  return request({
    url: '/admin/auth/profile'
  })
}

export const logout = () => {
  return request({
    url: '/admin/auth/logout',
    method: 'POST'
  })
}
