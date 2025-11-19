/**
 * 搜索历史管理工具
 * 支持添加、删除、清空、去重、限制数量
 */

import { CACHE_KEYS } from './cache'

const MAX_HISTORY_COUNT = 10 // 最多保存10条历史记录

/**
 * 搜索历史管理类
 */
class SearchHistoryManager {
  private storageKey: string

  constructor(key: string = CACHE_KEYS.SEARCH_HISTORY) {
    this.storageKey = key
  }

  /**
   * 获取搜索历史
   * @returns 搜索历史数组
   */
  getHistory(): string[] {
    try {
      const history = uni.getStorageSync(this.storageKey)
      return history ? JSON.parse(history) : []
    } catch (error) {
      console.error('[SearchHistory] 读取历史失败:', error)
      return []
    }
  }

  /**
   * 添加搜索记录
   * @param keyword 搜索关键词
   */
  add(keyword: string): void {
    if (!keyword || !keyword.trim()) return

    const trimmedKeyword = keyword.trim()
    let history = this.getHistory()

    // 移除已存在的相同关键词（去重）
    history = history.filter(item => item !== trimmedKeyword)

    // 将新关键词添加到开头
    history.unshift(trimmedKeyword)

    // 限制历史记录数量
    if (history.length > MAX_HISTORY_COUNT) {
      history = history.slice(0, MAX_HISTORY_COUNT)
    }

    this._save(history)
  }

  /**
   * 删除指定搜索记录
   * @param keyword 要删除的关键词
   */
  remove(keyword: string): void {
    const history = this.getHistory()
    const filtered = history.filter(item => item !== keyword)
    this._save(filtered)
  }

  /**
   * 清空所有搜索历史
   */
  clear(): void {
    try {
      uni.removeStorageSync(this.storageKey)
      console.log('[SearchHistory] 清空历史记录')
    } catch (error) {
      console.error('[SearchHistory] 清空历史失败:', error)
    }
  }

  /**
   * 获取历史记录数量
   */
  getCount(): number {
    return this.getHistory().length
  }

  /**
   * 检查是否有历史记录
   */
  hasHistory(): boolean {
    return this.getCount() > 0
  }

  /**
   * 私有方法：保存历史记录
   */
  private _save(history: string[]): void {
    try {
      uni.setStorageSync(this.storageKey, JSON.stringify(history))
    } catch (error) {
      console.error('[SearchHistory] 保存历史失败:', error)
    }
  }
}

// 导出单例（问答模块）
export const questionSearchHistory = new SearchHistoryManager(CACHE_KEYS.SEARCH_HISTORY)

// 导出类（可用于其他模块）
export { SearchHistoryManager }
