export interface PermissionQueryParams {
  permissionCode?: string
  permissionName?: string
  status?: number | null
  pageNum: number
  pageSize: number
}

export interface PermissionItem {
  permissionId: number
  permissionCode: string
  permissionName: string
  status: number
  createTime: string
  updateTime: string

  usedByRoles: number
  usedByMenus: number
  canDelete: boolean
}

export interface PermissionCreateDto {
  permissionCode: string
  permissionName: string
}

export interface PermissionUpdateDto {
  permissionId: number
  permissionCode: string
  permissionName: string
  status: number
}

export interface SimplePermissionItem {
  permissionId: number
  permissionCode: string
  permissionName: string
}
