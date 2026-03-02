import { get, post, del } from '@/utils/request'
import type { CartQueryParams, CartItem } from './type'
import type { PageResult } from '@/api/type'

export const cartListApi = (params: CartQueryParams) =>
  post<PageResult<CartItem>>('/admin/cart/list', params)

export const batchDeleteCartsApi = (cartIds: number[]) =>
  del<string>('/admin/cart/batch-delete', { params: { cartIds } })

export const clearUserCartApi = (memberId: number) => del<string>(`/admin/cart/clear/${memberId}`)
