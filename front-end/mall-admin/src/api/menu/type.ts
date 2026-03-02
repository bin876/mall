export interface MenuQueryParams {
  title?: string
  status?: number | null
  pageNum: number
  pageSize: number
}

export interface MenuItem {
  menuId: number
  parentId: number
  title: string
  name: string
  path: string
  component: string
  permissionCode: string
  icon: string
  type: number // 0-目录, 1-菜单, 2-按钮
  sort: number
  hidden: number // 0-显示, 1-隐藏
  status: number // 0-禁用, 1-启用
  createTime: string
  updateTime: string
  children: MenuItem[]
}

export interface MenuCreateDto {
  parentId: number
  title: string
  name: string
  path: string
  component: string
  permissionCode: string
  icon: string
  type: number
  sort: number
  hidden: number
}

export interface MenuUpdateDto {
  menuId: number
  title: string
  name: string
  path: string
  component: string
  permissionCode: string
  icon: string
  type: number
  sort: number
  hidden: number
  status: number
}

export interface MenuTreeDto {
  menuId: number
  parentId: number
  title: string
  name: string
  path: string
  component: string
  permissionCode: string
  icon: string
  type: number
  sort: number
  hidden: number
  status: number
  children: MenuTreeDto[]
}
