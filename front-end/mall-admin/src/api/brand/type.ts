export interface BrandQueryParams {
  name?: string
  pageNum: number
  pageSize: number
}

export interface BrandItem {
  id: number
  name: string
  logo: string
  firstLetter: string
  categoryIds: number[]
  createTime: string
  updateTime: string
}

export interface BrandCreateDto {
  name: string
  logo: string
  categoryIds: number[]
}

export interface BrandUpdateDto {
  id: number
  name: string
  logo: string
  categoryIds: number[]
}
