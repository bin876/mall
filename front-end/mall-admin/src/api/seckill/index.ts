import { get, post, put, del } from '@/utils/request'
import type {
  SeckillActivityQueryParams,
  SeckillActivityItem,
  SeckillActivityCreateRequest,
  SeckillActivityUpdateRequest,
  SeckillSkuDTO,
  SeckillActivityEditDTO,
} from './type'
import type { PageResult } from '@/api/type'

// 获取秒杀活动列表
export const seckillActivityListApi = (params: SeckillActivityQueryParams) =>
  get<PageResult<SeckillActivityItem>>('/admin/seckill/activities', { params })

// 获取秒杀活动详情
export const getSeckillActivityApi = (activityId: number) =>
  get<SeckillActivityItem>(`/admin/seckill/activities/${activityId}`)

// 创建秒杀活动
export const createSeckillActivityApi = (data: SeckillActivityCreateRequest) =>
  post<string>('/admin/seckill/activities', data)

// 更新秒杀活动
export const updateSeckillActivityApi = (activityId: number, data: SeckillActivityUpdateRequest) =>
  put<string>(`/admin/seckill/activities/${activityId}`, data)

// 删除秒杀活动
export const deleteSeckillActivityApi = (activityId: number) =>
  del<string>(`/admin/seckill/activities/${activityId}`)

// 预热秒杀库存
export const preloadSeckillStockApi = (activityId: number) =>
  post<string>(`/admin/seckill/activities/${activityId}/preload`)

// 手动结束秒杀活动
export const endSeckillActivityApi = (activityId: number) =>
  post<string>(`/admin/seckill/activities/${activityId}/end`)

// 获取SPU下的所有SKU（用于选择商品）
export const getSpuSkusForSeckillApi = (spuId: number) =>
  get<SkuDTO[]>(`/admin/seckill/spu/${spuId}/skus`)

export const getSeckillActivityForEditApi = (activityId: number) =>
  get<SeckillActivityEditDTO>(`/admin/seckill/activities/${activityId}/edit`)
