// src/api/coupon/type.ts
export interface CouponTemplateQueryParams {
  name?: string
  couponType?: number
  status?: number
  startTime?: string
  endTime?: string
  pageNum?: number
  pageSize?: number
}

export interface CouponTemplateCreateRequest {
  name: string
  description?: string
  couponType: number // 1-满减, 2-折扣, 3-无门槛
  discountAmount: BigDecimal | number
  minOrderAmount?: BigDecimal | number
  totalCount?: number // -1=不限
  perUserLimit: number
  validDays?: number
  startTime?: string
  endTime?: string
  status?: number // 0-禁用, 1-启用
}

export interface CouponTemplateUpdateRequest extends CouponTemplateCreateRequest {
  templateId: number
}

export interface CouponTemplateDTO {
  templateId: number
  name: string
  description?: string
  couponType: number
  couponTypeName: string
  discountAmount: number
  minOrderAmount?: number
  totalCount: number
  issuedCount: number
  perUserLimit: number
  validDays?: number
  startTime?: string
  endTime?: string
  status: number
  statusName: string
  createTime: string
}

export interface UserCouponQueryParams {
  keyword?: string
  memberId?: number
  status?: number
  startTime?: string
  endTime?: string
  pageNum?: number
  pageSize?: number
}

export interface UserCouponDTO {
  couponId: number
  templateId: number
  memberId: number
  memberName: string
  memberPhone: string
  couponCode: string
  templateName: string
  couponType: number
  couponTypeName: string
  discountAmount: number
  minOrderAmount?: number
  status: number
  statusName: string
  receiveTime: string
  useTime?: string
  orderSn?: string
  expireTime: string
}

export interface CouponIssueRequest {
  templateId: number
  memberIds: number[]
}

export interface CouponUpdateStatusRequest {
  couponId: number
  status: number // 0-未使用, 1-已使用, 2-已过期
  orderSn?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// src/api/coupon/type.ts
export interface UserCouponQueryParams {
  keyword?: string
  status?: number
  startTime?: string
  endTime?: string
}

export interface UserCouponDTO {
  couponId: number
  templateId: number
  memberId: number
  memberName: string
  memberPhone: string
  couponCode: string
  templateName: string
  couponType: number
  discountAmount: number
  minOrderAmount?: number
  status: number
  receiveTime: string
  useTime?: string
  orderSn?: string
  expireTime: string
}

export interface CouponIssueRequest {
  templateId: number
  memberIds: number[]
}

export interface CouponUpdateStatusRequest {
  couponId: number
  status: number // 0-未使用, 1-已使用, 2-已过期
  orderSn?: string
}

export interface CouponTemplateSearchRequest {
  pageNum?: number
  pageSize?: number
  params: any
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
