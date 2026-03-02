import { get, post, put, del } from '@/utils/request'
import type { BannerItem, BannerCreateDto, BannerUpdateDto, BannerQueryParams } from './type'
import type { PageResult } from '../type'

export const uploadBannerImageApi = (data: FormData) => {
  return post<string>('/admin/banner/image', data, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export const getBannerListApi = (params: BannerQueryParams) => {
  return post<PageResult<BannerItem>>('/admin/banner/list', params)
}

export const createBannerApi = (data: BannerCreateDto) => {
  return post<string>('/admin/banner', data)
}

export const updateBannerApi = (data: BannerUpdateDto) => {
  return put<string>(`/admin/banner/${data.bannerId}`, data)
}

export const deleteBannerApi = (bannerId: number) => {
  return del<string>(`/admin/banner/${bannerId}`)
}
