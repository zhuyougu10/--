export const formatDate = (date: Date): string => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export const formatTime = (time: string): string => {
  return time.substring(0, 5)
}

export const formatDateTime = (datetime: string | null | undefined): string => {
  if (!datetime) return ''
  return datetime.replace('T', ' ').substring(0, 19)
}

export const generateDateList = (daysAhead: number = 7) => {
  const dates: Array<{ value: string; week: string; day: number }> = []
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  const today = new Date()

  for (let i = 0; i < daysAhead; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    dates.push({
      value: formatDate(date),
      week: i === 0 ? '今天' : '周' + weekDays[date.getDay()],
      day: date.getDate()
    })
  }

  return dates
}

export const isVenueOpen = (venue: { status: number; openTime: string; closeTime: string }): boolean => {
  if (venue.status !== 1) return false
  const now = new Date()
  const currentTime = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
  return currentTime >= venue.openTime && currentTime <= venue.closeTime
}
