import { get, post, put, del } from '@/utils/request'
import type { BrandQueryParams, BrandItem, BrandCreateDto, BrandUpdateDto } from './type'
import type { PageResult } from '@/api/type'

export const brandListApi = (params: BrandQueryParams) =>
  post<PageResult<BrandItem>>('/admin/brand/list', params)

export const createBrandApi = (data: BrandCreateDto) => post<string>('/admin/brand', data)

export const updateBrandApi = (data: BrandUpdateDto) => put<string>('/admin/brand', data)

export const deleteBrandApi = (id: number) => del<string>(`/admin/brand/${id}`)

export const uploadBrandLogoApi = (formData: FormData) =>
  post<string>('/admin/brand/logo', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
