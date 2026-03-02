export interface CartQueryParams {
  memberId?: number | null
  skuName?: string
  pageNum: number
  pageSize: number
}

export interface CartItem {
  cartId: number
  memberId: number
  memberUsername: string
  skuId: number
  skuName: string
  skuPrice: number
  skuPic: string
  quantity: number
  checked: number // 0-未选中, 1-选中
  createTime: string
  updateTime: string
}
