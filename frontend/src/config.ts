// 全局配置文件
export const config = {
  // API 基础地址
  baseURL: process.env.NODE_ENV === 'production'
    ? 'https://api.campuslink.com/api/v1'
    : 'http://localhost:8080/api/v1',

  // 文件上传地址
  uploadURL: 'https://campuslink.oss-cn-hangzhou.aliyuncs.com',

  // Token 存储 key
  tokenKey: 'campuslink_token',
  refreshTokenKey: 'campuslink_refresh_token',
  userInfoKey: 'campuslink_user_info',

  // 请求超时时间
  timeout: 10000,

  // 分页配置
  defaultPageSize: 20,

  // 图片域名
  imageBaseURL: 'https://campuslink.oss-cn-hangzhou.aliyuncs.com',

  // 色彩系统（根据 UI 设计文档）
  colors: {
    // 主色调
    primary: '#409EFF',        // 青春蓝 - 导航、按钮、标题、图标
    primaryHover: '#337ECC',   // 主色调hover（加深10%）

    // 辅助色
    secondary: '#FF7D00',      // 活力橙 - 积分、紧急任务、报名按钮

    // 功能色
    success: '#00B42A',        // 成功色 - 完成状态
    warning: '#FF4D4F',        // 警告色 - 错误提示
    info: '#1890FF',           // 信息色 - 通知信息

    // 中性色
    white: '#FFFFFF',          // 纯白 - 卡片、导航栏背景
    bgGray: '#F5F7FA',         // 浅灰 - 页面底色、底部信息区
    borderGray: '#EEEEEE',     // 边框灰
    lightGray: '#F0F2F5',      // 浅灰 - hover背景
    midGray: '#C9CDD4',        // 中灰 - 辅助文字
    darkGray: '#4E5969',       // 深灰 - 标题文字
    textGray: '#86909C',       // 文字灰 - 正文
    black: '#1D2129',          // 纯黑 - 深色文字
  },

  // 字体大小（rpx）
  fontSize: {
    // PC端
    pc: {
      title: 40,      // 标题 20px
      subtitle: 36,   // 副标题 18px
      body: 32,       // 正文 16px
      small: 28,      // 小字 14px
      mini: 24,       // 最小 12px
    },
    // H5端
    h5: {
      title: 36,      // 标题 18px
      subtitle: 32,   // 副标题 16px
      body: 28,       // 正文 14px
      small: 26,      // 小字 13px
      mini: 22,       // 最小 11px
    },
  },

  // 间距（rpx）
  spacing: {
    xs: 8,
    sm: 16,
    md: 24,
    lg: 32,
    xl: 48,
  },

  // 圆角（rpx）
  borderRadius: {
    sm: 8,
    md: 12,
    lg: 24,
    xl: 32,
  },
}

export default config
