const API_BASE_URL = 'http://localhost:8080'

export const resolveAssetUrl = (url?: string): string => {
  if (!url) return '/static/default-venue.svg'
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  if (url.startsWith('/')) {
    return `${API_BASE_URL}${url}`
  }
  return `${API_BASE_URL}/${url}`
}
