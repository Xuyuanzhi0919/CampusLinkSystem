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
  return get<SystemConfig[]>('/config/list')
}

export function updateConfig(configKey: string, configValue: string, description?: string) {
  return put<void>(`/config/${configKey}`, { configValue, description })
}

export function createConfig(data: { configKey: string; configValue: string; description?: string }) {
  return post<void>('/config', data)
}

export function deleteConfig(configKey: string) {
  return del<void>(`/config/${configKey}`)
}
