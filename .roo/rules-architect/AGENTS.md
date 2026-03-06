# Architect Mode Rules

## 架构约束

### 后端分层
- Controller → Service（接口+实现类） → Mapper（MyBatisPlus BaseMapper） → MySQL
- DTO 按功能模块分 18 个子包，每个模块有 Request 和 VO 对象
- 实体类同时需要 JPA 和 MyBatisPlus 注解

### 认证架构
- 两层拦截器顺序不可颠倒：OptionalJwtAuthInterceptor → JwtAuthInterceptor
- JWT 双令牌机制：Access Token 2小时 + Refresh Token 7天

### 前端状态管理
- Pinia Store 仅用于 user、question、navigation
- 主题切换通过 CSS 自定义属性 + 全局事件实现，无独立 Store
- Token 刷新逻辑内置在 `utils/request.ts`，非 Store 管理

### 响应式设计
- 断点：`windowWidth > 768px` 区分桌面/移动端
- 组件分离：`components/mobile/` 和 `components/desktop/`

## 数据库设计要点

- 所有表有逻辑删除字段 `deleted`（MyBatisPlus 自动处理）
- 时间字段 `created_at`/`updated_at` 由 MyBatisMetaObjectHandler 自动填充
- 任务状态流转：0待接单 → 1进行中 → 2已完成/3已取消
