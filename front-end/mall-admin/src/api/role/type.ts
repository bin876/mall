export interface RoleQueryParams {
  roleCode?: string
  roleName?: string
  status?: number | null
  pageNum: number
  pageSize: number
}

export interface RoleItem {
  roleId: number
  roleCode: string
  roleName: string
  status: number
  createTime: string
  updateTime: string

  usedByUsers: number
  canDelete: boolean
}

export interface RoleCreateDto {
  roleCode: string
  roleName: string
  permissionIds: number[]
}

export interface RoleUpdateDto {
  roleId: number
  roleCode: string
  roleName: string
  status: number
  permissionIds: number[]
}

export interface SimpleRoleItem {
  roleId: number
  roleCode: string
  roleName: string
}
