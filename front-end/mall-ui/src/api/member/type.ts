// src/api/member/type.ts
export interface MemberInfo {
  memberId: number
  username: string
  nickname: string | null
  phone: string | null
  email: string | null
  avatarUrl: string | null
  gender: number
  createTime: string
}

export interface UpdateMemberInfo {
  username: string
  nickname?: string
  phone?: string
  email?: string
  gender: number
  avatarUrl?: string
}

export interface UpdatePassword {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export interface RegisterMember {
  username: string
  password: string
  confirmPassword?: string
  nickname?: string
  phone: string
  smsCode?: string
  email?: string
}
