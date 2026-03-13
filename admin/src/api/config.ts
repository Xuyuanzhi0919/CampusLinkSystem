import { get, post, put, del } from './request'

export interface SystemConfig {
  configId: number
  configKey: string
  configValue: string
  description: string
  createdAt: string
  updatedAt: string
}

export function listConfigs() {
  return get<SystemConfig[]>('/admin/config')
}

export function updateConfig(configKey: string, configValue: string, description?: string) {
  return put<void>(`/admin/config/${configKey}`, { configValue, description })
}

export function createConfig(data: { configKey: string; configValue: string; description?: string }) {
  return post<void>('/admin/config', data)
}

export function deleteConfig(configKey: string) {
  return del<void>(`/admin/config/${configKey}`)
}

export function batchUpdateConfigs(data: Record<string, string>) {
  return put<void>('/admin/config/batch', data)
}
