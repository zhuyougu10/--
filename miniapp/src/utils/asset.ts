import { APP_BASE_URL } from '@/config/network'

export const resolveAssetUrl = (url?: string): string => {
  if (!url) return '/static/default-venue.svg'
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  if (url.startsWith('/')) {
    return `${APP_BASE_URL}${url}`
  }
  return `${APP_BASE_URL}/${url}`
}
