import request from '@/utils/request'
import type { UserCoupon } from './type'

export const getMyCouponsApi = (status?: number) => {
  return request.get<UserCoupon[]>('/api/coupon/my', { status })
}

export const getAvailableCouponsApi = (amount: number) => {
  return request.get<UserCoupon[]>(`/api/coupon/available?amount=${amount}`)
}

// export const getAvailableCouponsApi = (amount: number) => {
//   return request.get<UserCoupon[]>(`/coupon/available`, { params: { amount } })
// }
