import { get, post, put, del } from '@/utils/request'
import type {
  ResourceQueryParams,
  ResourceItem,
  ResourceCreateDto,
  ResourceUpdateDto,
} from './type'
import type { PageResult } from '@/api/type'

export const resourceListApi = (params: ResourceQueryParams) =>
  post<PageResult<ResourceItem>>('/admin/resource/list', params)

export const createResourceApi = (data: ResourceCreateDto) => post<string>('/admin/resource', data)

export const updateResourceApi = (data: ResourceUpdateDto) => put<string>('/admin/resource', data)

export const deleteResourceApi = (id: number) => del<string>(`/admin/resource/${id}`)
