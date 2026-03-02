export interface CartItem {
  cartId: number
  skuId: number
  skuName: string
  skuPrice: number
  skuPic: string
  quantity: number
  checked: boolean
}

export interface UpdateCartParams {
  cartId: number
  quantity?: number
  checked?: boolean
}

export interface OrderConfirmRequest {
  cartIds?: number[]
  skuId?: number
  quantity?: number
}

export interface OrderConfirmResponse {
  skus: {
    skuId: number
    skuName: string
    skuPrice: number
    skuPic: string
    quantity: number
  }[]
  totalAmount: number
  payAmount: number
  receiverName: string
  receiverPhone: string
  receiverProvince: string
  receiverCity: string
  receiverRegion: string
  receiverDetailAddress: string
}

export interface CreateOrderRequest {
  cartIds?: number[]
  skuId?: number
  quantity?: number

  receiverName: string
  receiverPhone: string
  receiverProvince: string
  receiverCity: string
  receiverRegion: string
  receiverDetailAddress: string
}

export interface CreateOrderResponse {
  orderId: number
  orderSn: string
}

export interface PaymentResponse {
  message: string
  orderSn: string
}

export interface PaySubmitRequest {
  orderSn: string // ← 必须是 string
  payType: number
}
