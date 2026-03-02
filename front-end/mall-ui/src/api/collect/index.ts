import { get, post, del } from '@/utils/request'
import type { CollectDTO, CollectCreateDTO } from './type'
import type { PageResult } from '@/api/type'

export const getCollectListApi = (pageNum: number, pageSize: number) =>
  get<PageResult<CollectDTO>>('/api/product/collect/list', { pageNum, pageSize })

export const collectProductApi = (data: CollectCreateDTO) =>
  post<string>('/api/product/collect', data)

export const cancelCollectApi = (collectId: number) =>
  del<string>(`/api/product/collect/${collectId}`)

export const cancelCollectBySpuIdApi = (spuId: number) =>
  post<string>('/api/product/collect/cancel-by-spu', { spuId })

export const batchCancelCollectApi = (collectIds: number[]) =>
  post<string>('/api/product/collect/batch-cancel', collectIds)

export const checkCollectedApi = (spuId: number) =>
  get<boolean>(`/api/product/collect/check/${spuId}`)

export const getCollectCountApi = () => get<number>('/api/product/collect/count')
