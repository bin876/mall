import { get, post, put, del } from '@/utils/request'
import type { SkuQueryParams, SkuItem, BatchUpdateSkuParams } from './type'
import type { PageResult } from '@/api/type'

export const skuListApi = (params: SkuQueryParams) =>
  post<PageResult<SkuItem>>('/admin/sku/list', params)

export const updateSkuApi = (id: number, data: any) => put<string>(`/admin/sku/${id}`, data)

export const batchUpdateSkuApi = (data: BatchUpdateSkuParams) =>
  put<string>('/admin/sku/batch', data)

export const deleteSkuApi = (id: number) => del<string>(`/admin/sku/${id}`)
