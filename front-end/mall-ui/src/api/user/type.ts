export interface LoginRequest {
  username: string
  password: string
  clientId: string
}

export interface UserInfo {
  memberId: number
  username: string
  nickname: string | null
  phone: string | null
  email: string | null
  avatarUrl: string | null
  gender: number
  createTime: string
}
