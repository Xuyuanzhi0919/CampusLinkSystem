import { defineStore } from 'pinia'
import { ref, watchEffect } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'auto'

const STORAGE_KEY = 'cl-admin-theme'

function applyTheme(mode: ThemeMode, systemDark: boolean) {
  const isDark = mode === 'dark' || (mode === 'auto' && systemDark)
  document.documentElement.classList.toggle('dark', isDark)
  // 防止 auto 模式下 media query 与 class 冲突：用 data-theme 锁定
  if (mode === 'light') {
    document.documentElement.setAttribute('data-theme', 'light')
  } else if (mode === 'dark') {
    document.documentElement.setAttribute('data-theme', 'dark')
  } else {
    document.documentElement.removeAttribute('data-theme')
  }
}

export const useThemeStore = defineStore('theme', () => {
  const mode = ref<ThemeMode>(
    (localStorage.getItem(STORAGE_KEY) as ThemeMode | null) ?? 'auto'
  )

  // 系统深色偏好
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  const systemDark = ref(mediaQuery.matches)

  mediaQuery.addEventListener('change', (e) => {
    systemDark.value = e.matches
    applyTheme(mode.value, systemDark.value)
  })

  // 当 mode 或 systemDark 变化时重新应用
  watchEffect(() => {
    applyTheme(mode.value, systemDark.value)
    localStorage.setItem(STORAGE_KEY, mode.value)
  })

  /** 循环切换：auto → light → dark → auto */
  function toggle() {
    const next: Record<ThemeMode, ThemeMode> = { auto: 'light', light: 'dark', dark: 'auto' }
    mode.value = next[mode.value]
  }

  const isDark = () =>
    mode.value === 'dark' || (mode.value === 'auto' && systemDark.value)

  return { mode, toggle, isDark }
})
