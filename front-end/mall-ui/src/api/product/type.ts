export interface CategoryTree {
  categoryId: number
  name: string
  level: number
  icon: string
  children: CategoryTree[]
}

export interface SPU {
  spuId: number
  name: string
  minPrice: number
  maxPrice: number
  saleCount: number
  defaultImg: string
  createTime: string
}

export interface SpuDetail {
  spu: {
    spuId: number
    name: string
    description: string
    categoryId: number
    brandId: number
    weight: number
    publishStatus: number
    detail: string
    defaultImg: string
  }
  spuImages: {
    url: string
    sort: number
  }[]
  spuAttrs: {
    attrId: number
    attrName: string
    attrValue: string
  }[]
  skus: {
    skuId: number
    name: string
    title: string
    subtitle: string
    price: number
    stock: number
    defaultImg: string
    attrs: { attrId: number; attrName: string; attrValue: string }[]
    images: { url: string; sort: number }[]
  }[]
}

export interface BrandItem {
  id: number
  name: string
  logo: string
  firstLetter: string
}

// 订单列表项
export interface OrderSimple {
  orderId: number
  orderSn: string
  totalAmount: number
  payAmount: number
  status: number
  statusDesc: string
  createTime: string
  productCount: number
  firstProductImg: string
}

// 查询参数
export interface OrderListQuery {
  status?: number | null
  pageNum: number
  pageSize: number
}

// 订单详情
export interface OrderDetail {
  orderId: number
  orderSn: string
  totalAmount: number
  freightAmount: number
  promotionAmount: number
  payAmount: number
  status: number
  statusDesc: string
  payType: number
  payTypeDesc: string
  createTime: string
  payTime: string | null
  deliveryTime: string | null
  receiveTime: string | null
  closeTime: string | null
  receiverName: string
  receiverPhone: string
  receiverPostCode: string | null
  receiverProvince: string
  receiverCity: string
  receiverRegion: string
  receiverDetailAddress: string
  autoConfirmDay: number
  note: string | null
  orderItems: OrderItem[]
}

// 订单商品项
export interface OrderItem {
  itemId: number
  skuId: number
  skuName: string
  skuPic: string
  skuPrice: number
  quantity: number
  promotionName: string | null
}

// 支付请求
export interface PaySubmitRequest {
  orderSn: string
  payType: number
}
