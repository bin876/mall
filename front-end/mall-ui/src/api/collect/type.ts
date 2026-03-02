export interface CollectDTO {
  collectId: number
  spuId: number
  spuName: string
  spuPic: string
  spuPrice: number
  createTime: string
}

export interface CollectCreateDTO {
  spuId: number
}
