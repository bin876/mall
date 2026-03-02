import type { PageResult } from '@/api/type'

export interface PaymentQueryParams {
  orderSn?: string
  paySn?: string
  payType?: number | null
  status?: number | null
  pageNum: number
  pageSize: number
}

export interface PaymentItem {
  paymentId: number
  memberId: number
  orderId: number
  orderSn: string
  paySn: string
  payType: number
  payTypeDesc: string
  amount: number
  status: number
  statusDesc: string
  payTime: string
  createTime: string
  updateTime: string
  callbackContent: string | null
}
