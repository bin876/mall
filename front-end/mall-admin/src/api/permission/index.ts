import { get, post, put, del } from '@/utils/request'
import type {
  PermissionQueryParams,
  PermissionItem,
  PermissionCreateDto,
  PermissionUpdateDto,
  SimplePermissionItem,
} from './type'
import type { PageResult } from '@/api/type'

export const permissionListApi = (params: PermissionQueryParams) =>
  post<PageResult<PermissionItem>>('/admin/permission/list', params)

export const permissionSimpleListApi = () =>
  get<SimplePermissionItem[]>('/admin/permission/simple-list')

export const createPermissionApi = (data: PermissionCreateDto) =>
  post<string>('/admin/permission', data)

export const updatePermissionApi = (data: PermissionUpdateDto) =>
  put<string>('/admin/permission', data)

export const deletePermissionApi = (id: number) => del<string>(`/admin/permission/${id}`)
