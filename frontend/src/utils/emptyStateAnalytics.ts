/**
 * 空状态分析工具
 * 用于追踪和分析空状态的出现频率和用户行为
 */

export interface EmptyStateLog {
  type: 'empty' | 'error' | 'network' | 'search' | 'filter' | 'create'
  action: 'show' | 'button_click' | 'recommendation_click'
  timestamp: number
  data?: any
}

/**
 * 获取所有空状态日志
 */
export function getEmptyStateLogs(): EmptyStateLog[] {
  try {
    return uni.getStorageSync('empty_state_logs') || []
  } catch (e) {
    console.error('获取空状态日志失败:', e)
    return []
  }
}

/**
 * 清空日志
 */
export function clearEmptyStateLogs(): void {
  try {
    uni.removeStorageSync('empty_state_logs')
  } catch (e) {
    console.error('清空空状态日志失败:', e)
  }
}

/**
 * 分析空状态出现频率
 */
export function analyzeEmptyStateFrequency(): Record<string, number> {
  const logs = getEmptyStateLogs()
  const frequency: Record<string, number> = {}
  
  logs.forEach(log => {
    if (log.action === 'show') {
      frequency[log.type] = (frequency[log.type] || 0) + 1
    }
  })
  
  return frequency
}

/**
 * 分析用户交互率
 */
export function analyzeInteractionRate(): {
  type: string
  showCount: number
  clickCount: number
  rate: number
}[] {
  const logs = getEmptyStateLogs()
  const stats: Record<string, { show: number; click: number }> = {}
  
  logs.forEach(log => {
    if (!stats[log.type]) {
      stats[log.type] = { show: 0, click: 0 }
    }
    
    if (log.action === 'show') {
      stats[log.type].show++
    } else if (log.action === 'button_click' || log.action === 'recommendation_click') {
      stats[log.type].click++
    }
  })
  
  return Object.entries(stats).map(([type, data]) => ({
    type,
    showCount: data.show,
    clickCount: data.click,
    rate: data.show > 0 ? (data.click / data.show) * 100 : 0
  }))
}

/**
 * 获取最近的空状态记录
 */
export function getRecentEmptyStates(limit: number = 10): EmptyStateLog[] {
  const logs = getEmptyStateLogs()
  return logs.slice(-limit).reverse()
}

/**
 * 导出日志为 JSON
 */
export function exportLogsAsJSON(): string {
  const logs = getEmptyStateLogs()
  const analysis = {
    totalLogs: logs.length,
    frequency: analyzeEmptyStateFrequency(),
    interactionRate: analyzeInteractionRate(),
    recentLogs: getRecentEmptyStates(20),
    exportTime: new Date().toISOString()
  }
  
  return JSON.stringify(analysis, null, 2)
}

/**
 * 打印分析报告到控制台
 */
export function printAnalysisReport(): void {
  console.log('========== 空状态分析报告 ==========')
  
  const frequency = analyzeEmptyStateFrequency()
  console.log('\n📊 出现频率:')
  Object.entries(frequency).forEach(([type, count]) => {
    console.log(`  ${type}: ${count} 次`)
  })
  
  const interactionRate = analyzeInteractionRate()
  console.log('\n👆 交互率:')
  interactionRate.forEach(item => {
    console.log(`  ${item.type}: ${item.rate.toFixed(2)}% (${item.clickCount}/${item.showCount})`)
  })
  
  const recentLogs = getRecentEmptyStates(5)
  console.log('\n🕐 最近记录:')
  recentLogs.forEach(log => {
    const time = new Date(log.timestamp).toLocaleString()
    console.log(`  [${time}] ${log.type} - ${log.action}`)
  })
  
  console.log('\n====================================')
}

