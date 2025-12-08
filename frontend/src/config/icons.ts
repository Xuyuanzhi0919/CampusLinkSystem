/**
 * 图标配置文件
 *
 * 统一管理全站图标系统
 * 使用 Iconify (lucide 图标集) 作为主要图标来源
 */

/**
 * Iconify 图标映射（lucide 图标集）
 * 使用统一的线性风格图标，保持视觉一致性
 */
export const ICONIFY_MAP: Record<string, string> = {
  // ========== Meta 信息图标 ==========
  'icon-eye': 'lucide:eye',
  'icon-message': 'lucide:message-circle',
  'icon-heart': 'lucide:heart',
  'icon-time': 'lucide:clock',
  'icon-download': 'lucide:download',
  'icon-star': 'lucide:star',
  'icon-user-group': 'lucide:users',
  'icon-coin': 'lucide:coins',

  // ========== 操作图标 ==========
  'icon-edit': 'lucide:edit-3',
  'icon-check': 'lucide:check',
  'icon-lightbulb': 'lucide:lightbulb',
  'icon-plus': 'lucide:plus',
  'icon-minus': 'lucide:minus',
  'icon-close': 'lucide:x',
  'icon-search': 'lucide:search',
  'icon-filter': 'lucide:filter',
  'icon-more': 'lucide:more-horizontal',

  // ========== 文件类型图标 ==========
  'icon-file': 'lucide:file',
  'icon-file-pdf': 'lucide:file-text',      // PDF 使用红色变体
  'icon-file-word': 'lucide:file-text',     // Word 使用蓝色变体
  'icon-file-ppt': 'lucide:presentation',
  'icon-file-excel': 'lucide:table',
  'icon-file-zip': 'lucide:archive',
  'icon-file-text': 'lucide:file-text',
  'icon-file-image': 'lucide:image',
  'icon-file-video': 'lucide:video',

  // ========== 活动 / 社团图标 ==========
  'icon-activity': 'lucide:activity',
  'icon-calendar': 'lucide:calendar',
  'icon-location': 'lucide:map-pin',
  'icon-book': 'lucide:book-open',
  'icon-trophy': 'lucide:trophy',
  'icon-basketball': 'lucide:circle',        // 可替换为运动类图标
  'icon-group': 'lucide:users',
  'icon-ticket': 'lucide:ticket',
  'icon-map': 'lucide:map',

  // ========== 用户 / 认证图标 ==========
  'icon-user': 'lucide:user',
  'icon-verified': 'lucide:badge-check',
  'icon-shield': 'lucide:shield-check',
  'icon-crown': 'lucide:crown',

  // ========== 导航图标 ==========
  'icon-home': 'lucide:home',
  'icon-compass': 'lucide:compass',
  'icon-bell': 'lucide:bell',
  'icon-settings': 'lucide:settings',
  'icon-arrow-right': 'lucide:arrow-right',
  'icon-arrow-left': 'lucide:arrow-left',
  'icon-arrow-up': 'lucide:arrow-up',
  'icon-chevron-right': 'lucide:chevron-right',
  'icon-chevron-down': 'lucide:chevron-down',
  'icon-grid': 'lucide:grid-3x3',
  'icon-list': 'lucide:list',

  // ========== 状态图标 ==========
  'icon-success': 'lucide:check-circle',
  'icon-error': 'lucide:alert-circle',
  'icon-warning': 'lucide:alert-triangle',
  'icon-info': 'lucide:info',
  'icon-loading': 'lucide:loader-2',
  'icon-check-circle': 'lucide:check-circle-2',
  'icon-help-circle': 'lucide:help-circle',

  // ========== 代码/技术图标 ==========
  'icon-code': 'lucide:code',
  'icon-terminal': 'lucide:terminal',
  'icon-tag': 'lucide:tag',

  // ========== 其他常用图标 ==========
  'icon-share': 'lucide:share-2',
  'icon-bookmark': 'lucide:bookmark',
  'icon-flag': 'lucide:flag',
  'icon-thumbs-up': 'lucide:thumbs-up',
  'icon-thumbs-down': 'lucide:thumbs-down',
  'icon-send': 'lucide:send',
  'icon-upload': 'lucide:upload',
  'icon-link': 'lucide:link',
  'icon-external-link': 'lucide:external-link',
  'icon-copy': 'lucide:copy'
}

/**
 * Emoji 降级映射（当 Iconify 不可用时）
 */
export const EMOJI_MAP: Record<string, string> = {
  // Meta 图标
  'icon-eye': '👁',
  'icon-message': '💬',
  'icon-heart': '❤️',
  'icon-time': '🕒',
  'icon-download': '⬇️',
  'icon-star': '⭐',
  'icon-user-group': '👥',
  'icon-coin': '💰',

  // 操作图标
  'icon-edit': '✏️',
  'icon-check': '✓',
  'icon-lightbulb': '💡',

  // 文件类型图标
  'icon-file': '📄',
  'icon-file-pdf': '📕',
  'icon-file-word': '📘',
  'icon-file-ppt': '📙',
  'icon-file-excel': '📗',
  'icon-file-zip': '📦',
  'icon-file-text': '📄',

  // 活动图标
  'icon-activity': '🎯',
  'icon-calendar': '📅',
  'icon-location': '📍',
  'icon-book': '📚',
  'icon-trophy': '🏆',
  'icon-basketball': '🏀',
  'icon-group': '👥',

  // 其他
  'icon-verified': '✓'
}

/**
 * 获取 Iconify 图标名称
 * @param iconName 图标名称（如 'icon-eye'）
 * @returns Iconify 图标字符串（如 'lucide:eye'）
 */
export function getIconifyName(iconName: string): string | null {
  return ICONIFY_MAP[iconName] || null
}

/**
 * 获取 Emoji 图标（降级方案）
 * @param iconName 图标名称（如 'icon-eye'）
 * @returns Emoji 符号
 */
export function getIcon(iconName: string): string {
  return EMOJI_MAP[iconName] || '•'
}

/**
 * 图标组件配置
 */
export const ICON_CONFIG = {
  // 是否使用 emoji（降级方案）
  useEmoji: false,

  // 图标库类型（已切换到 iconify）
  type: 'iconify' as 'emoji' | 'iconify' | 'iconfont',

  // 默认图标尺寸（单位：px，会自动转换）
  size: {
    xs: '12px',
    sm: '14px',
    base: '16px',
    md: '18px',
    lg: '20px',
    xl: '24px',
    '2xl': '32px'
  }
}

/**
 * 文件类型到图标的映射（用于资源卡片）
 */
export const FILE_TYPE_ICONS: Record<string, { icon: string; color: string }> = {
  pdf: { icon: 'icon-file-pdf', color: '#E74C3C' },      // 红色
  doc: { icon: 'icon-file-word', color: '#2B579A' },     // 蓝色
  docx: { icon: 'icon-file-word', color: '#2B579A' },
  ppt: { icon: 'icon-file-ppt', color: '#D24726' },      // 橙红色
  pptx: { icon: 'icon-file-ppt', color: '#D24726' },
  xls: { icon: 'icon-file-excel', color: '#1D6F42' },    // 绿色
  xlsx: { icon: 'icon-file-excel', color: '#1D6F42' },
  zip: { icon: 'icon-file-zip', color: '#F39C12' },      // 黄色
  rar: { icon: 'icon-file-zip', color: '#F39C12' },
  txt: { icon: 'icon-file-text', color: '#7F8C8D' },     // 灰色
  default: { icon: 'icon-file', color: '#95A5A6' }       // 默认灰色
}

/**
 * 获取文件类型图标配置
 * @param fileType 文件扩展名（如 'pdf'）
 * @returns 图标配置对象
 */
export function getFileTypeIcon(fileType: string): { icon: string; color: string } {
  const type = fileType.toLowerCase()
  return FILE_TYPE_ICONS[type] || FILE_TYPE_ICONS.default
}
