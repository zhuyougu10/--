export interface Venue {
  id: number
  name: string
  location: string
  sportType: 'badminton' | 'basketball' | 'table_tennis' | 'tennis'
  openTime: string
  closeTime: string
  imageUrl?: string
  phone?: string
  status: number
  courts?: Court[]
  courtCount?: number
}

export interface Court {
  id: number
  name: string
  indoor: boolean
  status: number
}

export interface TimeSlot {
  startTime: string
  endTime: string
  status: 'free' | 'occupied'
}
