export interface MemberQueryParams {
  username?: string
  nickname?: string
  phone?: string
  status?: number | null
  pageNum: number
  pageSize: number
}

export interface MemberItem {
  memberId: number
  username: string
  nickname: string | null
  phone: string | null
  email: string | null
  avatarUrl: string | null
  gender: number
  genderDesc: string
  status: number
  statusDesc: string
  createTime: string
  updateTime: string
}

export interface UpdateMemberDto {
  username: string
  nickname?: string
  phone?: string
  email?: string
  gender: number
}

// 重置密码 DTO
export interface ResetPasswordDto {
  newPassword: string
}
