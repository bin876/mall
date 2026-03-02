export interface OrderQueryParams {
  orderSn?: string
  memberId?: number | null
  username?: string
  status?: number | null
  startTime?: string
  endTime?: string
  pageNum: number
  pageSize: number
}

export interface OrderListItem {
  orderId: number
  orderSn: string
  memberId: number
  username: string
  totalAmount: number
  payAmount: number
  status: number
  receiverName: string
  receiverPhone: string
  receiverProvince: string
  receiverCity: string
  receiverRegion: string
  receiverDetailAddress: string
  createTime: string
  orderItems: OrderItem[]
}

export interface OrderItem {
  itemId: number
  skuId: number
  skuName: string
  skuPic: string
  skuPrice: number
  quantity: number
  promotionName: string | null
}
