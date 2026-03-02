export interface CouponTemplate {
  templateId: number
  name: string
  description: string
  couponType: number
  discountAmount: number
  minOrderAmount: number | null
}

export interface UserCoupon {
  couponId: number
  templateId: number
  couponCode: string
  status: number
  receiveTime: string
  expireTime: string
  useTime: string | null
  orderSn: string | null
  template: CouponTemplate
}
