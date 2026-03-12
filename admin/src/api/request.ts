import axios, { type AxiosInstance, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import type { ApiResponse } from '@/types'

const instance: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器：自动注入 Token
instance.interceptors.request.use(config => {
  const token = localStorage.getItem('admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一错误处理
instance.interceptors.response.use(
  (response: AxiosResponse): AxiosResponse => {
    const data = response.data as ApiResponse
    if (data.code === 200) {
      response.data = data.data
      return response
    }
    ElMessage.error(data.message || '请求失败')
    throw new Error(data.message)
  },
  error => {
    const status = error.response?.status
    if (status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      window.location.href = '/login'
    } else if (status === 403) {
      ElMessage.error('无权限操作')
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export function get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  return instance.get(url, { params }).then(r => r.data as T)
}

export function post<T>(url: string, data?: unknown): Promise<T> {
  return instance.post(url, data).then(r => r.data as T)
}

export function put<T>(url: string, data?: unknown): Promise<T> {
  return instance.put(url, data).then(r => r.data as T)
}

export function del<T>(url: string): Promise<T> {
  return instance.delete(url).then(r => r.data as T)
}
