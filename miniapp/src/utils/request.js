const BASE_URL = 'http://localhost:8080/api'

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    
    let url = BASE_URL + options.url
    let data = options.data
    
    if (options.params && Object.keys(options.params).length > 0) {
      const queryString = Object.entries(options.params)
        .filter(([_, value]) => value !== undefined && value !== null)
        .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
        .join('&')
      if (queryString) {
        url += `?${queryString}`
      }
    }
    
    uni.request({
      url,
      method: options.method || 'GET',
      data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data.data)
          } else if (res.data.code === 401) {
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            uni.reLaunch({ url: '/pages/my/my' })
            reject(new Error('未登录'))
          } else {
            uni.showToast({
              title: res.data.message || '请求失败',
              icon: 'none'
            })
            reject(new Error(res.data.message))
          }
        } else {
          uni.showToast({
            title: '网络错误',
            icon: 'none'
          })
          reject(new Error('网络错误'))
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

export default request
