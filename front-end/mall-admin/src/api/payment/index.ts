import { get, post } from '@/utils/request'
import type { PaymentQueryParams, PaymentItem } from './type'
import type { PageResult } from '@/api/type'

export const paymentListApi = (params: PaymentQueryParams) =>
  post<PageResult<PaymentItem>>('/admin/payment/list', params)

export const paymentDetailApi = (id: number) => get<PaymentItem>(`/admin/payment/${id}`)
