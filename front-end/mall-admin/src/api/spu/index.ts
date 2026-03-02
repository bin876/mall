import { get, post, put, del } from '@/utils/request'
import type { SpuQueryParams, SpuItem, UpdateSpuDto } from './type'
import type { PageResult } from '@/api/type'

export const spuListApi = (params: SpuQueryParams) =>
  post<PageResult<SpuItem>>('/admin/spu/list', params)

export const updateSpuStatusApi = (id: number, publishStatus: number) =>
  put<string>(`/admin/spu/${id}/status`, { publishStatus })

export const deleteSpuApi = (id: number) => del<string>(`/admin/spu/${id}`)

export const updateSpuApi = (data: UpdateSpuDto) => put<string>('/admin/spu', data)

export const getSpuDetailApi = (id: number) => get<any>(`/admin/spu/${id}`)
