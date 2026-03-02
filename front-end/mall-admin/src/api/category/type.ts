export interface CategoryQueryParams {
  name?: string
  status?: number | null
  pageNum: number
  pageSize: number
}

export interface CategoryItem {
  categoryId: number
  parentId: number
  name: string
  level: number
  icon: string
  status: number
  parentName?: string
  createTime: string
  updateTime: string
}

export interface CategoryTree {
  categoryId: number
  parentId: number
  name: string
  level: number
  icon: string
  status: number
  children: CategoryTree[]
}

export interface CategoryCreateDto {
  parentId: number
  name: string
  icon: string
  status: number
}

export interface CategoryUpdateDto {
  categoryId: number
  parentId: number
  name: string
  icon: string
  status: number
}

export interface BatchUpdateCategoryStatusDto {
  ids: number[]
  status: number
}
