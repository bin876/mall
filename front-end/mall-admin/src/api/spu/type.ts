export interface SpuQueryParams {
  name?: string
  categoryId?: number | null
  brandId?: number | null
  publishStatus?: number | null
  pageNum: number
  pageSize: number
}

export interface SpuItem {
  spuId: number
  name: string
  description: string
  categoryId: number
  categoryName: string
  brandId: number
  brandName: string
  publishStatus: number // 0-下架, 1-上架
  saleCount: number
  defaultImg: string
  createTime: string
  updateTime: string
}

export interface UpdateSpuDto {
  spuId: number
  name: string
  description: string
  publishStatus: number
}
