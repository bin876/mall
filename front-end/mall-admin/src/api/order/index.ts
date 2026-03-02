import { get, post, put } from '@/utils/request'
import type { OrderQueryParams, OrderListItem } from './type'
import type { PageResult } from '@/api/type'

export const orderListApi = (params: OrderQueryParams) =>
  post<PageResult<OrderListItem>>('/admin/order/list', params)

export const updateOrderStatusApi = (orderId: number, status: number) =>
  put<string>(`/admin/order/${orderId}/status`, { status })
