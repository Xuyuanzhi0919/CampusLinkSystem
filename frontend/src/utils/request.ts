import config from '@/config'
import logger from './logger'
import { getFriendlyErrorMessage } from './errorHandler'

interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: any
  timeout?: number
}

interface Response<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

class Request {
  private baseURL: string
  private timeout: number
  private tokenKey: string
  private refreshTokenKey: string
  private isRefreshing: boolean = false // 是否正在刷新Token
  private requestQueue: Array<() => void> = [] // 请求队列

  constructor() {
    this.baseURL = config.baseURL
    this.timeout = config.timeout
    this.tokenKey = config.tokenKey
    this.refreshTokenKey = config.refreshTokenKey
  }

  // 获取 Token
  private getToken(): string {
    return uni.getStorageSync(this.tokenKey) || ''
  }

  // 获取 Refresh Token
  private getRefreshToken(): string {
    return uni.getStorageSync(this.refreshTokenKey) || ''
  }

  // 设置 Token
  private setToken(token: string): void {
    uni.setStorageSync(this.tokenKey, token)
  }

  // 设置 Refresh Token
  private setRefreshToken(refreshToken: string): void {
    uni.setStorageSync(this.refreshTokenKey, refreshToken)
  }

  // 清除 Token
  private clearToken(): void {
    uni.removeStorageSync(this.tokenKey)
    uni.removeStorageSync(this.refreshTokenKey)
    uni.removeStorageSync(config.userInfoKey)
  }

  // 解析JWT Token获取过期时间
  private parseJWT(token: string): any {
    try {
      const parts = token.split('.')
      if (parts.length !== 3) return null
      const payload = JSON.parse(atob(parts[1]))
      return payload
    } catch (e) {
      logger.error('JWT解析失败:', e)
      return null
    }
  }

  // 检查Token是否即将过期 (剩余时间少于15分钟)
  private isTokenExpiringSoon(token: string): boolean {
    const payload = this.parseJWT(token)
    if (!payload || !payload.exp) return false

    const expirationTime = payload.exp * 1000 // 转换为毫秒
    const currentTime = Date.now()
    const timeLeft = expirationTime - currentTime
    const fifteenMinutes = 15 * 60 * 1000 // 15分钟

    return timeLeft > 0 && timeLeft < fifteenMinutes
  }

  // 刷新Token
  private async refreshAccessToken(): Promise<boolean> {
    const refreshToken = this.getRefreshToken()
    if (!refreshToken) {
      return false
    }

    try {
      const response = await uni.request({
        url: this.baseURL + '/auth/refresh',
        method: 'POST',
        header: {
          'Content-Type': 'application/json',
          'Refresh-Token': refreshToken,
        },
        timeout: this.timeout,
      })

      const res = response.data as Response<any>
      if (res.code === 200 && res.data) {
        // 更新Token
        this.setToken(res.data.token)
        if (res.data.refreshToken) {
          this.setRefreshToken(res.data.refreshToken)
        }
        return true
      }
      return false
    } catch (e) {
      logger.error('Token刷新失败:', e)
      return false
    }
  }

  // 请求拦截器
  private async beforeRequest(options: RequestOptions): Promise<RequestOptions> {
    const token = this.getToken()

    // 检查Token是否即将过期,自动刷新
    if (token && this.isTokenExpiringSoon(token) && !this.isRefreshing) {
      this.isRefreshing = true
      const success = await this.refreshAccessToken()
      this.isRefreshing = false

      if (success) {
        logger.info('Token已自动刷新')
      } else {
        logger.warn('Token自动刷新失败')
      }
    }

    // 添加 Token (使用最新的token)
    const currentToken = this.getToken()
    if (currentToken) {
      options.header = {
        ...options.header,
        Authorization: `Bearer ${currentToken}`,
      }
    }

    // 添加通用 header
    options.header = {
      'Content-Type': 'application/json',
      ...options.header,
    }

    return options
  }

  // 响应拦截器
  private async afterResponse<T>(
    response: UniApp.RequestSuccessCallbackResult,
    originalOptions: RequestOptions
  ): Promise<T> {
    const res = response.data as Response<T>

    // Token 过期，尝试刷新后重试
    if (res.code === 401) {
      // 如果正在刷新，等待刷新完成
      if (this.isRefreshing) {
        return new Promise((resolve) => {
          this.requestQueue.push(() => {
            this.request<T>(originalOptions).then(resolve)
          })
        })
      }

      // 尝试刷新Token
      this.isRefreshing = true
      const refreshSuccess = await this.refreshAccessToken()
      this.isRefreshing = false

      if (refreshSuccess) {
        // Token刷新成功，重试原请求
        logger.info('Token过期已刷新，重试请求')

        // 执行队列中的请求
        this.requestQueue.forEach((cb) => cb())
        this.requestQueue = []

        return this.request<T>(originalOptions)
      } else {
        // Token刷新失败，清除登录信息并跳转首页
        this.clearToken()
        const friendlyMessage = getFriendlyErrorMessage({ code: 401 })
        uni.showToast({
          title: friendlyMessage,
          icon: 'none',
        })
        setTimeout(() => {
          uni.reLaunch({
            url: '/pages/home/index',
          })
        }, 1500)
        return Promise.reject(new Error(res.message))
      }
    }

    // 业务错误
    if (res.code !== 200 && res.code !== 201 && res.code !== 204) {
      // 不在这里自动显示Toast，让调用方自己处理
      // uni.showToast({
      //   title: res.message || '请求失败',
      //   icon: 'none',
      // })
      return Promise.reject(new Error(res.message))
    }

    return Promise.resolve(res.data)
  }

  // 统一请求方法
  async request<T = any>(options: RequestOptions): Promise<T> {
    // 请求拦截 (现在是异步的)
    options = await this.beforeRequest(options)

    return new Promise((resolve, reject) => {
      uni.request({
        url: this.baseURL + options.url,
        method: options.method || 'GET',
        data: options.data,
        header: options.header,
        timeout: options.timeout || this.timeout,
        success: (res) => {
          this.afterResponse<T>(res, options).then(resolve).catch(reject)
        },
        fail: (err) => {
          logger.error('Request failed:', err)
          const friendlyMessage = getFriendlyErrorMessage({ message: 'Network request failed' })
          uni.showToast({
            title: friendlyMessage,
            icon: 'none',
          })
          reject(err)
        },
      })
    })
  }

  // GET 请求
  get<T = any>(url: string, data?: any): Promise<T> {
    return this.request<T>({
      url,
      method: 'GET',
      data,
    })
  }

  // POST 请求
  post<T = any>(url: string, data?: any): Promise<T> {
    return this.request<T>({
      url,
      method: 'POST',
      data,
    })
  }

  // PUT 请求
  put<T = any>(url: string, data?: any): Promise<T> {
    return this.request<T>({
      url,
      method: 'PUT',
      data,
    })
  }

  // DELETE 请求
  delete<T = any>(url: string, data?: any): Promise<T> {
    return this.request<T>({
      url,
      method: 'DELETE',
      data,
    })
  }
}

// 导出实例
export default new Request()
