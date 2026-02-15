export interface User {
  id: number
  name?: string
  phone?: string
  avatar?: string
  studentNo?: string
  userType: number
  userTypeText?: string
  status?: number
  statusText?: string
  noShowCount?: number
  lastNoShowAt?: string
  bannedUntil?: string
  isBound?: number
  createdAt?: string
}

export interface LoginResult {
  token: string
  refreshToken: string
  userId: number
  userType: string
  isNewUser: boolean
  isBound: boolean
  needBind: boolean
}
