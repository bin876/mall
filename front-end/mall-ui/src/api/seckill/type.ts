// api/seckill/type.ts
export interface SeckillInfoDTO {
  activityId: number
  skuId: number
  seckillPrice: number
  totalStock: number
  availableStock: number
  startTime: string
  endTime: string
  status: number // 0-未开始, 1-进行中, 2-已结束
}

export interface SeckillRequest {
  skuId: number
}
