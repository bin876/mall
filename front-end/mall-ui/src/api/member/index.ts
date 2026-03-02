import { get, post, put, del } from '@/utils/request'
import type { MemberInfo, RegisterMember, UpdateMemberInfo, UpdatePassword } from './type'

export const getMemberInfoApi = () => get<MemberInfo>('/api/member/info')

export const updateMemberInfoApi = (data: UpdateMemberInfo) => put<string>('/api/member/info', data)

export const updatePasswordApi = (data: UpdatePassword) => put<string>('/api/member/password', data)

export const registerApi = (data: Omit<RegisterMember, 'confirmPassword' | 'smsCode'>) =>
  post<string>('/api/member/register', data)

export const uploadAvatarApi = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return post<string>('/api/member/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}
