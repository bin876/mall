// src/api/coupon/index.ts
import { get, post, put, del } from '@/utils/request'
import type {
  CouponTemplateQueryParams,
  CouponTemplateCreateRequest,
  CouponTemplateUpdateRequest,
  CouponTemplateDTO,
  PageResult,
  CouponIssueRequest,
  CouponTemplateSearchRequest,
  CouponUpdateStatusRequest,
  UserCouponDTO,
} from './type'

// 优惠券模板相关
export const getCouponTemplatesApi = (params: CouponTemplateQueryParams) =>
  post<PageResult<CouponTemplateDTO>>('/admin/coupons/templateList', params)

export const createCouponTemplateApi = (request: CouponTemplateCreateRequest) =>
  post<string>('/admin/coupons/templates', request)

export const updateCouponTemplateApi = (request: CouponTemplateUpdateRequest) =>
  put<string>(`/admin/coupons/templates/${request.templateId}`, request)

export const deleteCouponTemplateApi = (templateId: number) =>
  del<string>(`/admin/coupons/templates/${templateId}`)

export const updateTemplateStatusApi = (templateId: number, status: number) =>
  put<string>(`/admin/coupons/templates/${templateId}/status`, { status })

// 用户优惠券相关
export const getUserCouponsApi = (request: CouponTemplateSearchRequest) =>
  post<PageResult<UserCouponDTO>>('/admin/coupons/user-coupons', request)

export const getUserCouponApi = (couponId: number) =>
  get<UserCouponDTO>(`/admin/coupons/user-coupons/${couponId}`)

export const updateUserCouponStatusApi = (request: CouponUpdateStatusRequest) =>
  put<string>('/admin/coupons/user-coupons/status', request)

// 优惠券发放
export const issueCouponsApi = (request: CouponIssueRequest) =>
  post<string>('/admin/coupons/issue', request)

// 清理过期优惠券
export const cleanExpiredCouponsApi = () => post<number>('/admin/coupons/clean-expired')
