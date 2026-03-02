import { get, post, put, del } from '@/utils/request'
import type {
  UserQueryParams,
  UserItem,
  UserCreateDto,
  UserUpdateDto,
  SimpleUserItem,
  LoginRequest,
  SysUserInfo,
} from './type'
import type { PageResult } from '@/api/type'

export const loginApi = (data: LoginRequest) => post('/api/auth/login', data)

export const logoutApi = () => get('/api/auth/logout')

export const getRoleListApi = () => get('/admin/role/list', { pageNum: 1, pageSize: 100 })

export const userListApi = (params: UserQueryParams) =>
  post<PageResult<UserItem>>('/admin/user/list', params)

export const userSimpleListApi = () => get<SimpleUserItem[]>('/admin/user/simple-list')

export const createUserApi = (data: UserCreateDto) => post<string>('/admin/user', data)

export const updateUserApi = (data: UserUpdateDto) => put<string>('/admin/user', data)

export const deleteUserApi = (id: number) => del<string>(`/admin/user/${id}`)

export const getUserRolesApi = (userId: number) => get<number[]>(`/admin/user/${userId}/roles`)

export const getSysUserInfoApi = () => get<SysUserInfo>('/admin/user/info')

export const updateSysUserInfoApi = (data: SysUserInfo) => put<string>('/admin/user/info', data)

export const getUserPermissionsApi = () => get<string[]>('/admin/user/permissions')
