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
}

export default config
