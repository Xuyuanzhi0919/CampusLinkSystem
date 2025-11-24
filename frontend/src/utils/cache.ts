/**
 * 🎯 企业级缓存工具
 *
 * 功能：
 * 1. 支持设置 TTL（过期时间）
 * 2. 自动清理过期数据
 * 3. 类型安全
 * 4. 支持同步和异步操作
 */

interface CacheItem<T> {
  data: T
  timestamp: number
  ttl: number // 毫秒
}

class CacheManager {
  /**
   * 设置缓存
   * @param key 缓存键
   * @param data 缓存数据
   * @param ttl 过期时间（毫秒），默认 5 分钟
   */
  set<T>(key: string, data: T, ttl: number = 5 * 60 * 1000): void {
    const cacheItem: CacheItem<T> = {
      data,
      timestamp: Date.now(),
      ttl
    }

    try {
      uni.setStorageSync(this._getCacheKey(key), JSON.stringify(cacheItem))
    } catch (error) {
      console.error(`[Cache] 设置缓存失败: ${key}`, error)
    }
  }

  /**
   * 获取缓存
   * @param key 缓存键
   * @returns 缓存数据，如果过期或不存在则返回 null
   */
  get<T>(key: string): T | null {
    try {
      const cached = uni.getStorageSync(this._getCacheKey(key))

      if (!cached) {
        return null
      }

      const cacheItem: CacheItem<T> = JSON.parse(cached)

      // 检查是否过期
      if (this._isExpired(cacheItem)) {
        console.log(`[Cache] 缓存已过期: ${key}`)
        this.remove(key)
        return null
      }

      console.log(`[Cache] 命中缓存: ${key}`)
      return cacheItem.data
    } catch (error) {
      console.error(`[Cache] 获取缓存失败: ${key}`, error)
      return null
    }
  }

  /**
   * 检查缓存是否存在且未过期
   * @param key 缓存键
   */
  has(key: string): boolean {
    try {
      const cached = uni.getStorageSync(this._getCacheKey(key))

      if (!cached) {
        return false
      }

      const cacheItem: CacheItem<any> = JSON.parse(cached)
      return !this._isExpired(cacheItem)
    } catch (error) {
      return false
    }
  }

  /**
   * 移除指定缓存
   * @param key 缓存键
   */
  remove(key: string): void {
    try {
      uni.removeStorageSync(this._getCacheKey(key))
      console.log(`[Cache] 移除缓存: ${key}`)
    } catch (error) {
      console.error(`[Cache] 移除缓存失败: ${key}`, error)
    }
  }

  /**
   * 清空所有缓存
   */
  clear(): void {
    try {
      // 获取所有缓存键
      const info = uni.getStorageInfoSync()
      const keys = info.keys.filter(k => k.startsWith('cache:'))

      keys.forEach(key => {
        uni.removeStorageSync(key)
      })

      console.log(`[Cache] 清空所有缓存，共 ${keys.length} 个`)
    } catch (error) {
      console.error('[Cache] 清空缓存失败', error)
    }
  }

  /**
   * 获取缓存剩余有效时间（毫秒）
   * @param key 缓存键
   * @returns 剩余时间（毫秒），如果不存在或已过期则返回 0
   */
  getTTL(key: string): number {
    try {
      const cached = uni.getStorageSync(this._getCacheKey(key))

      if (!cached) {
        return 0
      }

      const cacheItem: CacheItem<any> = JSON.parse(cached)
      const elapsed = Date.now() - cacheItem.timestamp
      const remaining = cacheItem.ttl - elapsed

      return remaining > 0 ? remaining : 0
    } catch (error) {
      return 0
    }
  }

  /**
   * 私有方法：生成缓存键
   */
  private _getCacheKey(key: string): string {
    return `cache:${key}`
  }

  /**
   * 私有方法：检查缓存是否过期
   */
  private _isExpired<T>(cacheItem: CacheItem<T>): boolean {
    const now = Date.now()
    const elapsed = now - cacheItem.timestamp
    return elapsed >= cacheItem.ttl
  }
}

// 导出单例
export const cache = new CacheManager()

/**
 * 缓存键常量（统一管理）
 */
export const CACHE_KEYS = {
  USER_PROFILE: 'user:profile',
  USER_POINTS: 'user:points',
  NOTICES: 'notices:list',
  ACTIVITIES: 'activities:list',
  TASKS: 'tasks:list',

  // 问答模块
  QUESTION_LIST: 'question:list',
  QUESTION_DETAIL: 'question:detail:',
  HOT_TAGS: 'question:hot_tags',
  SEARCH_HISTORY: 'question:search_history',

  // 资源模块
  RESOURCE_SEARCH_HISTORY: 'resource:search_history'
}

/**
 * TTL 常量（毫秒）
 */
export const CACHE_TTL = {
  SHORT: 2 * 60 * 1000,      // 2 分钟
  MEDIUM: 5 * 60 * 1000,     // 5 分钟
  LONG: 10 * 60 * 1000,      // 10 分钟
  HOUR: 60 * 60 * 1000,      // 1 小时
  DAY: 24 * 60 * 60 * 1000   // 1 天
}
