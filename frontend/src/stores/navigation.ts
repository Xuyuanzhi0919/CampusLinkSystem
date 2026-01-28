import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 导航状态管理 Store
 * 统一管理桌面端和移动端的导航激活状态
 */
export const useNavigationStore = defineStore('navigation', () => {
  // ============ 状态 ============

  /**
   * 当前激活的导航路径
   * 默认为首页
   */
  const activePath = ref<string>('/pages/home/index')

  /**
   * 导航历史记录（用于返回功能）
   */
  const history = ref<string[]>(['/pages/home/index'])

  /**
   * 导航栏是否可见（用于滚动隐藏功能）
   */
  const isNavVisible = ref(true)

  /**
   * 上次滚动位置（用于判断滚动方向）
   */
  let lastScrollTop = 0

  // ============ Tab 配置 ============

  /**
   * TabBar 配置（移动端）
   */
  const tabList = [
    { text: '首页', path: '/pages/home/index', icon: 'home' },
    { text: '资源', path: '/pages/resource/index', icon: 'resource' },
    { text: '发布', path: '/pages/publish/index', icon: 'publish', action: true },
    { text: '社区', path: '/pages/community/index', icon: 'community' },
    { text: '我的', path: '/pages/user/index', icon: 'user' }
  ] as const

  /**
   * 导航项配置（桌面端）
   */
  const navItems = [
    { label: '首页', path: '/pages/home/index', isTab: true },
    { label: '资源', path: '/pages/resource/index', isTab: true },
    { label: '问答', path: '/pages/question/index', isTab: false },
    { label: '任务', path: '/pages/task/index', isTab: false },
    { label: '社区', path: '/pages/community/index', isTab: true }
  ] as const

  // ============ 计算属性 ============

  /**
   * 当前激活的 Tab 索引（移动端）
   */
  const activeTabIndex = computed(() => {
    const index = tabList.findIndex(item => item.path === activePath.value)
    return index >= 0 ? index : 0
  })

  /**
   * 判断给定路径是否激活
   */
  const isActive = (path: string, exact = false) => {
    if (exact) {
      return activePath.value === path
    }
    // 支持前缀匹配（用于详情页等子页面）
    return activePath.value.startsWith(path)
  }

  // ============ Actions ============

  /**
   * 设置当前激活路径
   */
  const setActivePath = (path: string) => {
    if (path !== activePath.value) {
      activePath.value = path
      // 添加到历史记录
      history.value.push(path)
      // 限制历史记录长度
      if (history.value.length > 50) {
        history.value = history.value.slice(-50)
      }
    }
  }

  /**
   * 根据当前页面自动设置激活路径
   */
  const syncActivePath = () => {
    const pages = getCurrentPages()
    if (pages.length === 0) return

    const currentPage = pages[pages.length - 1]
    const route = '/' + currentPage.route
    setActivePath(route)
  }

  /**
   * 处理页面滚动，自动隐藏/显示导航栏
   */
  const handleScroll = (scrollTop: number) => {
    // 向下滚动超过 100px 时隐藏
    if (scrollTop > lastScrollTop && scrollTop > 100) {
      isNavVisible.value = false
    } else {
      isNavVisible.value = true
    }
    lastScrollTop = scrollTop
  }

  /**
   * 重置导航栏可见性
   */
  const showNav = () => {
    isNavVisible.value = true
  }

  /**
   * 隐藏导航栏
   */
  const hideNav = () => {
    isNavVisible.value = false
  }

  return {
    // 状态
    activePath,
    history,
    isNavVisible,
    // 配置
    tabList,
    navItems,
    // 计算属性
    activeTabIndex,
    // 方法
    isActive,
    setActivePath,
    syncActivePath,
    handleScroll,
    showNav,
    hideNav
  }
})
