import { get } from '@/utils/request'
import type { BannerItem } from './type'

export const getBannerListApi = (): Promise<BannerItem[]> => {
  return get('/api/product/banner/list')
}
