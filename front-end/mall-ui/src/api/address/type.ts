export interface AddressDTO {
  addressId: number
  receiverName: string
  receiverPhone: string
  receiverProvince: string
  receiverCity: string
  receiverRegion: string
  receiverDetailAddress: string
  receiverPostCode?: string
  isDefault: boolean
}

export interface AddressCreateDTO {
  receiverName: string
  receiverPhone: string
  receiverProvince: string
  receiverCity: string
  receiverRegion: string
  receiverDetailAddress: string
  receiverPostCode?: string
  isDefault: boolean
}

export interface AddressUpdateDTO {
  addressId: number
  receiverName: string
  receiverPhone: string
  receiverProvince: string
  receiverCity: string
  receiverRegion: string
  receiverDetailAddress: string
  receiverPostCode?: string
  isDefault: boolean
}
