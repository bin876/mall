import { get, post } from '@/utils/request'
import type { SeckillInfoDTO, SeckillRequest } from './type'

export const getSeckillInfoApi = (spuId: number) =>
  get<SeckillInfoDTO>(`/api/seckill/info/${spuId}`)

export const doSeckillApi = (data: SeckillRequest) => post<boolean>('/api/seckill/do', data)

export const getSeckillStockApi = (skuId: number) =>
  get<{ skuId: number; stock: number }>(`/api/seckill/metrics/stock/${skuId}`)
