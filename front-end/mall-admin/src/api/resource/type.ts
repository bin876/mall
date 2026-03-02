export interface ResourceQueryParams {
  name?: string
  path?: string
  permissionCode?: string
  status?: number | null
  pageNum: number
  pageSize: number
}

export interface ResourceItem {
  resourceId: number
  name: string
  path: string
  method: string
  permissionCode: string
  status: number
  createTime: string
  updateTime: string
}

export interface ResourceCreateDto {
  name: string
  path: string
  method: string
  permissionCode: string
}

export interface ResourceUpdateDto {
  resourceId: number
  name: string
  path: string
  method: string
  permissionCode: string
  status: number
}
