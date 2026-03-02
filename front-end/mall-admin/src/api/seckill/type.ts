export interface SeckillSkuDTO {
  id?: number
  activityId?: number
  spuId: number
  skuId: number
  spuName?: string
  skuName?: string
  defaultImg?: string
  price?: number
  seckillPrice: number
  totalStock: number
  availableStock: number
}

export interface SeckillActivityItem {
  id: number
  name: string
  startTime: string
  endTime: string
  status: number
  createTime?: string
  skus?: SeckillSkuDTO[]
}

export interface SeckillActivityQueryParams {
  name?: string
  status?: number | null
  startTime?: string
  endTime?: string
  pageNum: number
  pageSize: number
}

export interface SeckillActivityCreateRequest {
  name: string
  startTime: string
  endTime: string
  skus: SeckillSkuDTO[]
}

export interface SeckillActivityUpdateRequest {
  id: number
  name: string
  startTime: string
  endTime: string
  skus: SeckillSkuDTO[]
}

export interface SeckillSkuEditDTO {
  id: number
  activityId: number
  spuId: number
  skuId: number
  spuName: string
  skuName: string
  defaultImg: string
  seckillPrice: number
  totalStock: number
  availableStock: number

  originalPrice?: number
  maxStock?: number
  originalStock?: number
  saleAttrs?: Array<{
    attrId: number
    attrName: string
    attrValue: string
  }>
  canEditStock?: boolean
  canEditPrice?: boolean
  priceLimitMessage?: string
  stockLimitMessage?: string
}

export interface SeckillActivityEditDTO {
  id: number
  name: string
  startTime: string
  endTime: string
  status: number

  skus: SeckillSkuEditDTO[]
  canEdit?: boolean
  canAddSkus?: boolean
  canRemoveSkus?: boolean
  editLimitMessage?: string
}
