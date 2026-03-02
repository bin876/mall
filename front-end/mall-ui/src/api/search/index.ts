import request from '@/utils/request'

export const searchProductsApi = (keyword: string, page = 0, size = 10) => {
  return request.get('/api/product/search', {
    params: { keyword, page, size },
  })
}
