import { get, post, put, del } from '@/utils/request'
import type { MenuQueryParams, MenuItem, MenuCreateDto, MenuUpdateDto, MenuTreeDto } from './type'
import type { PageResult } from '@/api/type'

export const menuListApi = (params: MenuQueryParams) =>
  post<PageResult<MenuItem>>('/admin/menu/list', params)

export const menuTreeApi = () => get<MenuTreeDto[]>('/admin/menu/tree')

export const createMenuApi = (data: MenuCreateDto) => post<string>('/admin/menu', data)

export const updateMenuApi = (data: MenuUpdateDto) => put<string>('/admin/menu', data)

export const deleteMenuApi = (id: number) => del<string>(`/admin/menu/${id}`)
