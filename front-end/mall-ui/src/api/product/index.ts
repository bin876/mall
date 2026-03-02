import { get, post, put, del } from '@/utils/request'
import type { BrandItem, CategoryTree, PaySubmitRequest, SPU, SpuDetail } from './type'
import type { PageResult } from '@/api/type'
import type { OrderSimple, OrderListQuery, OrderDetail } from './type'

export const getCategoryTreeApi = () => get<CategoryTree[]>('/api/product/category/tree')

export const getRecommendSpuApi = (params: { pageNum: number; pageSize: number }) =>
  get<PageResult<SPU>>('/api/product/recommend', params)

export const getSpuDetailApi = (spuId: number) => get<SpuDetail>(`/api/product/${spuId}`)

export const getBrandListApi = (params: {
  categoryId?: number
  pageNum: number
  pageSize: number
}) => get<PageResult<BrandItem>>('/api/product/brand/list', params)

// export const getProductListApi = (params: {
//   categoryId?: number
//   brandId?: number
//   keyword?: string
//   minPrice?: number
//   maxPrice?: number
//   sort?: string
//   pageNum: number
//   pageSize: number
// }) => get<PageResult<any>>('/api/product/list', params)

export const getProductListApi = (params: ProductListQuery) =>
  get<IPage<SpuSimpleDTO>>('/api/product/search', params)

export const getOrderListApi = (params: OrderListQuery) =>
  post<PageResult<OrderSimple>>('/api/order/list', params)

export const cancelOrderApi = (orderSn: string) => put<string>(`/api/order/${orderSn}/cancel`)

export const confirmReceiptApi = (orderSn: string) => put<string>(`/api/order/${orderSn}/confirm`)

export const deleteOrderApi = (orderSn: string) => del<string>(`/api/order/${orderSn}`)

export const getOrderDetailApi = (orderSn: string) => get<OrderDetail>(`/api/order/${orderSn}`)

export const submitPaymentApi = (data: PaySubmitRequest) => post<string>('/api/pay/submit', data)
