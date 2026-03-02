import request from '@/utils/request'
import type { CommentDTO, CommentCreateDTO, CommentQueryDTO } from './type'
import type { PageResult } from '../type'

export const getCommentListApi = (query: CommentQueryDTO): Promise<PageResult<CommentDTO>> => {
  return request.get('/api/product/comment/list', query)
}

export const createCommentApi = (data: CommentCreateDTO): Promise<void> => {
  return request.post('/api/product/comment', data)
}

export const deleteCommentApi = (commentId: number): Promise<void> => {
  return request.del(`/api/product/comment/${commentId}`)
}
