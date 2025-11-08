import config from '@/config'

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

  // 设置 Token
  private setToken(token: string): void {
    uni.setStorageSync(this.tokenKey, token)
  }

  // 清除 Token
  private clearToken(): void {
    uni.removeStorageSync(this.tokenKey)
    uni.removeStorageSync(this.refreshTokenKey)
    uni.removeStorageSync(config.userInfoKey)
  }

  // 请求拦截器
  private beforeRequest(options: RequestOptions): RequestOptions {
    const token = this.getToken()

    // 添加 Token
    if (token) {
      options.header = {
        ...options.header,
        Authorization: `Bearer ${token}`,
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
  private afterResponse<T>(response: UniApp.RequestSuccessCallbackResult): Promise<T> {
    const res = response.data as Response<T>

    // Token 过期，尝试刷新
    if (res.code === 401) {
      this.clearToken()
      uni.showToast({
        title: '登录已过期，请重新登录',
        icon: 'none',
      })
      setTimeout(() => {
        uni.reLaunch({
          url: '/pages/auth/login',
        })
      }, 1500)
      return Promise.reject(new Error(res.message))
    }

    // 业务错误
    if (res.code !== 200 && res.code !== 201 && res.code !== 204) {
      uni.showToast({
        title: res.message || '请求失败',
        icon: 'none',
      })
      return Promise.reject(new Error(res.message))
    }

    return Promise.resolve(res.data)
  }

  // 统一请求方法
  request<T = any>(options: RequestOptions): Promise<T> {
    // 请求拦截
    options = this.beforeRequest(options)

    return new Promise((resolve, reject) => {
      uni.request({
        url: this.baseURL + options.url,
        method: options.method || 'GET',
        data: options.data,
        header: options.header,
        timeout: options.timeout || this.timeout,
        success: (res) => {
          this.afterResponse<T>(res).then(resolve).catch(reject)
        },
        fail: (err) => {
          console.error('Request failed:', err)
          uni.showToast({
            title: '网络请求失败',
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
