/**
 * Resource comment types
 */

// Comment item in list
export interface ResourceComment {
  commentId: number
  resourceId: number
  userId: number
  userName: string
  userAvatar: string
  parentId?: number
  content: string
  createdAt: string
  replies: ResourceComment[]
  replyCount: number
}

// Add comment request
export interface AddCommentRequest {
  parentId?: number
  content: string
}

// Comment list query params
export interface CommentListParams {
  page?: number
  pageSize?: number
}
