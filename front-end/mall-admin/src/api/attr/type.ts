export interface AttrQueryParams {
  attrName?: string
  attrType?: number | null
  categoryId?: number | null
  pageNum: number
  pageSize: number
}

export interface AttrItem {
  attrId: number
  attrName: string
  attrType: number // 0-销售, 1-基本
  valueType: number // 0-单选, 1-多选
  valueList: string | null
  categoryId: number
  categoryName?: string
  createTime: string
}

export interface AttrCreateDto {
  attrName: string
  attrType: number
  valueType: number
  valueList?: string
  categoryId: number
}

export interface AttrUpdateDto {
  attrId: number
  attrName: string
  attrType: number
  valueType: number
  valueList?: string
  categoryId: number
}
