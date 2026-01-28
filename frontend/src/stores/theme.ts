/**
 * 主题状态管理
 * 支持：浅色 | 深色 | 跟随系统
 */

import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import config from '@/config'

export type ThemeMode = 'light' | 'dark' | 'auto'

export const useThemeStore = defineStore('theme', () => {
  // 主题模式（默认跟随系统）
  const themeMode = ref<ThemeMode>('auto')

  // 系统深色模式媒体查询
  let mediaQuery: MediaQueryList | null = null

  /**
   * 检测系统是否为深色模式
   */
  const getSystemDarkMode = (): boolean => {
    // #ifdef H5
    if (window.matchMedia) {
      return window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    // #endif
    return false
  }

  /**
   * 当前是否为深色模式（计算属性）
   * - 当 themeMode 为 'light' 时返回 false
   * - 当 themeMode 为 'dark' 时返回 true
   * - 当 themeMode 为 'auto' 时根据系统主题返回
   */
  const isDark = computed(() => {
    if (themeMode.value === 'light') return false
    if (themeMode.value === 'dark') return true
    return getSystemDarkMode()
  })

  /**
   * 初始化主题
   * - 从本地存储恢复主题设置
   * - 监听系统主题变化（当 mode 为 auto 时）
   */
  const init = () => {
    // 从本地存储恢复
    const savedTheme = uni.getStorageSync(config.themeKey) as ThemeMode
    if (savedTheme && ['light', 'dark', 'auto'].includes(savedTheme)) {
      themeMode.value = savedTheme
    }

    // 监听系统主题变化（仅 H5）
    // #ifdef H5
    if (window.matchMedia) {
      mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      const handleSystemThemeChange = (e: MediaQueryListEvent) => {
        // 只有在 auto 模式下才响应系统主题变化
        if (themeMode.value === 'auto') {
          console.log('[Theme] 系统主题变化:', e.matches ? '深色' : '浅色')
        }
      }
      mediaQuery.addEventListener('change', handleSystemThemeChange)
    }
    // #endif
  }

  /**
   * 设置主题模式
   */
  const setTheme = (mode: ThemeMode) => {
    themeMode.value = mode
    uni.setStorageSync(config.themeKey, mode)
    console.log('[Theme] 主题已切换为:', mode)
  }

  /**
   * 切换主题（在 light 和 dark 之间切换）
   * 如果当前是 auto，则切换到 dark
   */
  const toggleTheme = () => {
    const currentIsDark = isDark.value
    setTheme(currentIsDark ? 'light' : 'dark')
  }

  /**
   * 循环切换主题：light -> dark -> auto -> light
   */
  const cycleTheme = () => {
    const modes: ThemeMode[] = ['light', 'dark', 'auto']
    const currentIndex = modes.indexOf(themeMode.value)
    const nextIndex = (currentIndex + 1) % modes.length
    setTheme(modes[nextIndex])
  }

  // 监听主题变化，同步到 document
  watch(isDark, (dark) => {
    // #ifdef H5
    if (document.documentElement) {
      if (dark) {
        document.documentElement.classList.add('dark-mode')
      } else {
        document.documentElement.classList.remove('dark-mode')
      }
    }
    // #endif

    // 触发全局事件，通知组件更新
    uni.$emit('theme-change', { isDark: dark })
  }, { immediate: true })

  return {
    // 状态
    themeMode,
    isDark,

    // 方法
    init,
    setTheme,
    toggleTheme,
    cycleTheme,
  }
})
