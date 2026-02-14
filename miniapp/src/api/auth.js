import request from '@/utils/request'

export const wechatLogin = (code) => {
  return request({
    url: '/auth/wechat/login',
    method: 'POST',
    data: { code }
  })
}

export const updateUserProfile = (data) => {
  return request({
    url: '/user/profile',
    method: 'PUT',
    data
  })
}

export const getUserProfile = () => {
  return request({
    url: '/user/profile'
  })
}
