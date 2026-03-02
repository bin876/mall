import { get, post, put, del } from '@/utils/request'
import type { AddressDTO, AddressCreateDTO, AddressUpdateDTO } from './type'

export const getAddressListApi = () => get<AddressDTO[]>('/api/member/address/list')

export const createAddressApi = (data: AddressCreateDTO) =>
  post<string>('/api/member/address', data)

export const updateAddressApi = (data: AddressUpdateDTO) => put<string>('/api/member/address', data)

export const deleteAddressApi = (addressId: number) =>
  del<string>(`/api/member/address/${addressId}`)

export const setDefaultAddressApi = (addressId: number) =>
  put<string>(`/api/member/address/${addressId}/default`)

export const getDefaultAddressApi = () => get<AddressDTO | null>('/api/member/address/default')
