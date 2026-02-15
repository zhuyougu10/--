export interface User {
  id: number
  nickname?: string
  avatar?: string
  userType: 'student' | 'teacher' | 'staff'
}

export interface LoginResult {
  token: string
  user: User
}
