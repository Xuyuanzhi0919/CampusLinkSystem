/**
 * 统一日志管理工具
 * 根据环境变量控制日志输出，生产环境只保留错误日志
 */

type LogLevel = 'log' | 'info' | 'warn' | 'error' | 'debug'

interface LoggerConfig {
  enabled: boolean
  logLevel: LogLevel[]
  prefix?: string
}

class Logger {
  private config: LoggerConfig

  constructor() {
    // 根据环境配置
    const isDev = import.meta.env.DEV

    this.config = {
      enabled: true,
      logLevel: isDev
        ? ['log', 'info', 'warn', 'error', 'debug']
        : ['error'], // 生产环境仅保留错误日志
      prefix: '[CampusLink]'
    }
  }

  /**
   * 普通日志（开发环境）
   */
  log(...args: any[]) {
    if (this.shouldLog('log')) {
      console.log(this.config.prefix, ...args)
    }
  }

  /**
   * 信息日志（开发环境）
   */
  info(...args: any[]) {
    if (this.shouldLog('info')) {
      console.info(this.config.prefix, ...args)
    }
  }

  /**
   * 警告日志（开发环境）
   */
  warn(...args: any[]) {
    if (this.shouldLog('warn')) {
      console.warn(this.config.prefix, ...args)
    }
  }

  /**
   * 错误日志（所有环境）
   */
  error(...args: any[]) {
    if (this.shouldLog('error')) {
      console.error(this.config.prefix, ...args)

      // 生产环境可以在这里上报错误到监控系统
      if (!import.meta.env.DEV) {
        this.reportError(args)
      }
    }
  }

  /**
   * 调试日志（开发环境）
   */
  debug(...args: any[]) {
    if (this.shouldLog('debug')) {
      console.debug(this.config.prefix, ...args)
    }
  }

  /**
   * 检查是否应该输出日志
   */
  private shouldLog(level: LogLevel): boolean {
    return this.config.enabled && this.config.logLevel.includes(level)
  }

  /**
   * 上报错误（生产环境）
   * 可以集成 Sentry、阿里云日志等服务
   */
  private reportError(args: any[]) {
    // TODO: 集成错误监控服务
    // 示例：
    // if (window.Sentry) {
    //   window.Sentry.captureException(new Error(args.join(' ')))
    // }
  }

  /**
   * 性能计时开始
   */
  time(label: string) {
    if (this.shouldLog('debug')) {
      console.time(this.config.prefix + ' ' + label)
    }
  }

  /**
   * 性能计时结束
   */
  timeEnd(label: string) {
    if (this.shouldLog('debug')) {
      console.timeEnd(this.config.prefix + ' ' + label)
    }
  }

  /**
   * 分组日志开始
   */
  group(label: string) {
    if (this.shouldLog('log')) {
      console.group(this.config.prefix + ' ' + label)
    }
  }

  /**
   * 分组日志结束
   */
  groupEnd() {
    if (this.shouldLog('log')) {
      console.groupEnd()
    }
  }

  /**
   * 表格形式输出
   */
  table(data: any) {
    if (this.shouldLog('log')) {
      console.table(data)
    }
  }
}

// 导出单例
export const logger = new Logger()

// 默认导出
export default logger
