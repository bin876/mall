import { post } from '@/utils/request'
import type { PublishSpuRequest } from './type'

export const publishSpuApi = (data: PublishSpuRequest) => post<string>('/admin/spu', data)

export const uploadSpuImageApi = (formData: FormData) =>
  post<string>('/admin/spu/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })

export const uploadSkuImageApi = (formData: FormData) =>
  post<string>('/admin/sku/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
