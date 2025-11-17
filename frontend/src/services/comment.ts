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
  return request<{
    list: ResourceComment[]
    total: number
    page: number
    pageSize: number
    totalPages: number
  }>({
    url: `/resource/${resourceId}/comments`,
    method: 'GET',
    params
  })
}

/**
 * Add comment or reply
 */
export function addComment(resourceId: number, data: AddCommentRequest) {
  return request<{ commentId: number }>({
    url: `/resource/${resourceId}/comments`,
    method: 'POST',
    data
  })
}

/**
 * Delete comment
 */
export function deleteComment(commentId: number) {
  return request({
    url: `/resource/comments/${commentId}`,
    method: 'DELETE'
  })
}
