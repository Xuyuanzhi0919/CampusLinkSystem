import { get, put } from './request'
import type { AdminResource, AdminQuestion, AdminAnswer, PageResult } from '@/types'

export function listResources(params: { keyword?: string; status?: number; page?: number; pageSize?: number }) {
  return get<PageResult<AdminResource>>('/admin/content/resources', params as Record<string, unknown>)
}

export function updateResourceStatus(resourceId: number, status: number, reason?: string) {
  return put<void>(`/admin/content/resources/${resourceId}/status`, { status, reason })
}

export function listQuestions(params: { keyword?: string; status?: number; page?: number; pageSize?: number }) {
  return get<PageResult<AdminQuestion>>('/admin/content/questions', params as Record<string, unknown>)
}

export function updateQuestionStatus(questionId: number, status: number, reason?: string) {
  return put<void>(`/admin/content/questions/${questionId}/status`, { status, reason })
}

export function listAnswers(params: { keyword?: string; status?: number; page?: number; pageSize?: number }) {
  return get<PageResult<AdminAnswer>>('/admin/content/answers', params as Record<string, unknown>)
}

export function updateAnswerStatus(answerId: number, status: number) {
  return put<void>(`/admin/content/answers/${answerId}/status`, { status })
}
