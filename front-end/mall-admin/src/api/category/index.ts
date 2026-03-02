import { get, post, put, del } from '@/utils/request'
import type {
  CategoryQueryParams,
  CategoryItem,
  CategoryTree,
  CategoryCreateDto,
  CategoryUpdateDto,
  BatchUpdateCategoryStatusDto,
} from './type'
import type { PageResult } from '@/api/type'

export const categoryListApi = (params: CategoryQueryParams) =>
  post<PageResult<CategoryItem>>('/admin/category/list', params)

export const getCategoryTreeApi = () => get<CategoryTree[]>('/admin/category/tree')

export const createCategoryApi = (data: CategoryCreateDto) => post<string>('/admin/category', data)

export const updateCategoryApi = (data: CategoryUpdateDto) => put<string>('/admin/category', data)

export const deleteCategoryApi = (id: number) => del<string>(`/admin/category/${id}`)

export const batchUpdateCategoryStatusApi = (data: BatchUpdateCategoryStatusDto) =>
  put<string>('/admin/category/batch-status', data)

export const uploadCategoryIconApi = (formData: FormData) =>
  post<string>('/admin/category/icon', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
