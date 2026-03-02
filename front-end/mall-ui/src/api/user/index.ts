import { del, get, post, put } from '@/utils/request'
import type { LoginRequest, UserInfo } from './type'

export const loginApi = (data: LoginRequest) => post('/api/auth/login', data)

export const logoutApi = () => get('/api/auth/logout')

export const getUserInfoApi = () => get<UserInfo>('/api/member/info')
