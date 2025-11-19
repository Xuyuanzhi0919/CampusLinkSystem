/**
 * Resource comment API service
 */
import request from '@/utils/request'
import type { ResourceComment, AddCommentRequest, CommentListParams } from '@/types/comment'

/**
 * Get resource comments
 */
export function getResourceComments(
  resourceId: number,
  params: CommentListParams = {}
) {
  return request.get<{
    list: ResourceComment[]
    total: number
    page: number
    pageSize: number
    totalPages: number
  }>(`/resource/${resourceId}/comments`, params)
}

/**
 * Add comment or reply
 */
export function addComment(resourceId: number, data: AddCommentRequest) {
  return request.post<{ commentId: number }>(`/resource/${resourceId}/comments`, data)
}

/**
 * Delete comment
 */
export function deleteComment(commentId: number) {
  return request.delete(`/resource/comments/${commentId}`)
}
