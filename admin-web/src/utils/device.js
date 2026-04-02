export const isMobileDevice = () => {
  if (typeof window === 'undefined') return false
  const ua = navigator.userAgent || ''
  const mobileUA = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(ua)
  return mobileUA || window.innerWidth <= 768
}
