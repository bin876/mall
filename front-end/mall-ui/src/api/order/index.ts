import { get, post, put, del } from '@/utils/request'
import type {
  CartItem,
  CreateOrderRequest,
  CreateOrderResponse,
  OrderConfirmRequest,
  OrderConfirmResponse,
  PaySubmitRequest,
  UpdateCartParams,
} from './type'

export const addToCartApi = (data: { skuId: number; quantity: number }) =>
  post<string>('/api/cart/add', data)

export const updateCartApi = (data: UpdateCartParams) => put<string>('/api/cart/update', data)

export const getCartItemsApi = () => get<CartItem[]>('/api/cart/items')

export const deleteCartApi = (cartId: number) => del(`/api/cart/${cartId}`)

export const confirmOrderApi = (data: OrderConfirmRequest) =>
  post<OrderConfirmResponse>('/api/order/confirm', data)

export const createOrderApi = (data: CreateOrderRequest) =>
  post<CreateOrderResponse>('/api/order/create', data)

export const submitPaymentApi = (data: PaySubmitRequest) =>
  post<PaymentResponse>('/api/pay/submit', data)
