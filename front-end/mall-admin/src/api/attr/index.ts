import { get, post, put, del } from '@/utils/request'
import type { AttrQueryParams, AttrItem, AttrCreateDto, AttrUpdateDto } from './type'
import type { PageResult } from '@/api/type'

export const attrListApi = (params: AttrQueryParams) =>
  post<PageResult<AttrItem>>('/admin/attr/list', params)

export const createAttrApi = (data: AttrCreateDto) => post<string>('/admin/attr', data)

export const updateAttrApi = (data: AttrUpdateDto) => put<string>('/admin/attr', data)

export const deleteAttrApi = (ids: number[]) => del<string>('/admin/attr', { params: { ids } })

export const getCategoryAttrsApi = (categoryId: number) => {
  const basicPromise = post<PageResult<AttrItem>>('/admin/attr/list', {
    categoryId,
    attrType: 1, // 基本属性
    pageNum: 1,
    pageSize: 100,
  })

  const salePromise = post<PageResult<AttrItem>>('/admin/attr/list', {
    categoryId,
    attrType: 0, // 销售属性
    pageNum: 1,
    pageSize: 100,
  })

  return Promise.all([basicPromise, salePromise]).then(([basicRes, saleRes]) => ({
    basicAttrs: basicRes.records,
    saleAttrs: saleRes.records,
  }))
}
