import request from '@/utils/request'

export const getVenueList = () => {
  return request({
    url: '/venues'
  })
}

export const getVenueDetail = (id) => {
  return request({
    url: `/venues/${id}`
  })
}

export const getTimeSlots = (venueId, courtId, date) => {
  return request({
    url: `/venues/${venueId}/courts/${courtId}/slots`,
    data: { date }
  })
}
