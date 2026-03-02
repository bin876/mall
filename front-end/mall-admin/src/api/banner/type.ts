export interface BannerItem {
  bannerId: number
  title: string
  imageUrl: string
  targetUrl: string
  sort: number
  status: number
}

export interface BannerCreateDto {
  title: string
  imageUrl: string
  targetUrl: string
  sort: number
  status: number
}

export interface BannerUpdateDto {
  bannerId: number
  title: string
  imageUrl: string
  targetUrl: string
  sort: number
  status: number
}

export interface BannerQueryParams {
  status: number | null
  pageNum: number
  pageSize: number
}
