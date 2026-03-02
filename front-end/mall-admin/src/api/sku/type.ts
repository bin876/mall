export interface SkuQueryParams {
  spuId?: number
  categoryId?: number
  brandId?: number
  keyword?: string
  status?: number
  pageNum: number
  pageSize: number
}

export interface SkuItem {
  skuId: number
  spuId: number
  spuName: string
  name: string
  title: string
  subtitle: string
  price: number
  stock: number
  status: number // 0-下架, 1-上架
  defaultImg: string
  createTime: string
  updateTime: string
  saleAttrs: {
    attrId: number
    attrName: string
    attrValue: string
  }[]
}

export interface UpdateSkuDto {
  name: string
  title: string
  subtitle: string
  price: number
  stock: number
  status: number
}

export interface BatchUpdateSkuDto {
  skuIds: number[]
  price?: number
  priceOperation?: 'set' | 'add' | 'reduce'
  stock?: number
  stockOperation?: 'set' | 'add' | 'reduce'
  status?: number
}

export interface BatchUpdateSkuParams {
  skuIds: number[]
  price?: number
  stock?: number
  status?: number
}
