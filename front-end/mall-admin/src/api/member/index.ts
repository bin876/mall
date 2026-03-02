import { get, post, put, del } from '@/utils/request'
import type { MemberQueryParams, MemberItem, UpdateMemberDto, ResetPasswordDto } from './type'
import type { PageResult } from '@/api/type'

export const memberListApi = (params: MemberQueryParams) =>
  post<PageResult<MemberItem>>('/admin/member/list', params)

export const updateMemberApi = (id: number, data: UpdateMemberDto) =>
  put<string>(`/admin/member/${id}`, data)

export const updateMemberStatusApi = (id: number, status: number) =>
  put<string>(`/admin/member/${id}/status`, { status })

export const resetPasswordApi = (id: number, data: ResetPasswordDto) =>
  put<string>(`/admin/member/${id}/password`, data)

export const deleteMemberApi = (id: number) => del<string>(`/admin/member/${id}`)
