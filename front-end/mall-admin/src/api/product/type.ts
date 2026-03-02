export interface SPUImage {
  url: string
  sort: number
}

export interface SKUImage {
  url: string
  sort: number
}

export interface SKUDTO {
  name: string
  price: number
  stock: number
  images: SKUImage[]
  saleAttrs: {
    attrId: number
    attrValue: string
  }[]
}

export interface PublishSpuRequest {
  name: string
  description: string
  categoryId: number
  brandId: number
  weight: number
  detail: string
  publishStatus: number
  defaultImg: string
  spuImages: SPUImage[]
  basicAttrs: {
    attrId: number
    attrValue: string
  }[]
  skus: SKUDTO[]
}
