# Debug Mode Rules

## 后端调试

- 后端 `.http` 测试文件在 `backend/` 目录下（需 IntelliJ HTTP Client 执行）
- WebSocket 配置使用 `@Conditional` 避免测试环境启动失败
- 通知线程池配置在 `AsyncConfig`（core=4, max=8, queue=200, CallerRunsPolicy）

## 前端调试

- 401 错误区分两种情况："已登录 token 过期"（弹窗提示）与"游客访问需登录接口"（静默 reject）
- Token 刷新竞态问题：检查 `isRefreshing` 标志位和 `requestQueue` 数组
- 主题切换不生效：检查是否监听了 `uni.$emit('theme-change')` 事件

## 常见问题

- 前端请求 404：检查后端 context-path 是否为 `/api/v1`
- 数据库时间字段未自动填充：确认 `MyBatisMetaObjectHandler` 是否正常加载
- 游客访问被拦截：检查路由是否在 `OptionalJwtAuthInterceptor` 白名单中
