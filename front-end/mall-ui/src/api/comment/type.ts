export interface CommentQueryDTO {
  spuId: number
  pageNum?: number
  pageSize?: number
}

export interface CommentDTO {
  children(children: any): unknown
  commentId: number
  content: string
  memberId: number
  memberUsername: string
  parentId: number | null
  replyToMemberId: number | null
  replyToMemberUsername: string | null
  createTime: string
  replies?: CommentDTO[]
}

export interface CommentCreateDTO {
  spuId: number
  content: string
  parentId: number | null
  replyToMemberId: number | null
}
