import { ref } from 'vue'
import { getVenueList, getVenueDetail, getTimeSlots } from '@/api/venue'
import type { Venue, TimeSlot } from '@/types'

export function useVenue() {
  const venues = ref<Venue[]>([])
  const currentVenue = ref<Venue | null>(null)
  const loading = ref(false)

  const loadVenues = async () => {
    loading.value = true
    try {
      venues.value = await getVenueList()
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  const loadVenueDetail = async (id: number) => {
    loading.value = true
    try {
      currentVenue.value = await getVenueDetail(id)
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  return {
    venues,
    currentVenue,
    loading,
    loadVenues,
    loadVenueDetail
  }
}

export function useTimeSlots() {
  const slots = ref<TimeSlot[]>([])
  const loading = ref(false)

  const loadSlots = async (venueId: number, courtId: number, date: string) => {
    loading.value = true
    try {
      slots.value = await getTimeSlots(venueId, courtId, date)
    } catch (e) {
      console.error(e)
    } finally {
      loading.value = false
    }
  }

  return {
    slots,
    loading,
    loadSlots
  }
}

export function useSportType() {
  const sportTypeMap: Record<string, string> = {
    badminton: '羽毛球',
    basketball: '篮球',
    table_tennis: '乒乓球',
    tennis: '网球'
  }

  const getSportTypeName = (type: string): string => {
    return sportTypeMap[type] || type
  }

  return {
    getSportTypeName
  }
}
