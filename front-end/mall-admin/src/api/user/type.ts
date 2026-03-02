export interface LoginRequest {
  username: string
  password: string
  clientId: string
}

export interface UserQueryParams {
  username?: string
  realName?: string
  phone?: string
  status?: number | null
  pageNum: number
  pageSize: number
}

export interface UserItem {
  userId: number
  username: string
  realName: string
  nickname: string
  email: string
  phone: string
  gender: number
  status: number
  createTime: string
  updateTime: string
}

export interface UserCreateDto {
  username: string
  password: string
  realName: string
  nickname: string
  email: string
  phone: string
  gender: number
  roleIds: number[]
}

export interface UserUpdateDto {
  userId: number
  realName: string
  nickname: string
  email: string
  phone: string
  gender: number
  status: number
  roleIds: number[]
}

export interface SimpleUserItem {
  userId: number
  username: string
  realName: string
}

export interface SysUserInfo {
  userId: number
  username: string
  nickname?: string
  avatarUrl?: string
  email?: string
}
