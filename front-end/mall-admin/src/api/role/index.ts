import { get, post, put, del } from '@/utils/request'
import type {
  RoleQueryParams,
  RoleItem,
  RoleCreateDto,
  RoleUpdateDto,
  SimpleRoleItem,
} from './type'
import type { PageResult } from '@/api/type'

export const roleListApi = (params: RoleQueryParams) =>
  post<PageResult<RoleItem>>('/admin/role/list', params)

export const roleSimpleListApi = () => get<SimpleRoleItem[]>('/admin/role/simple-list')

export const createRoleApi = (data: RoleCreateDto) => post<string>('/admin/role', data)

export const updateRoleApi = (data: RoleUpdateDto) => put<string>('/admin/role', data)

export const deleteRoleApi = (id: number) => del<string>(`/admin/role/${id}`)

export const getRolePermissionsApi = (roleId: number) =>
  get<number[]>(`/admin/role/${roleId}/permissions`)
