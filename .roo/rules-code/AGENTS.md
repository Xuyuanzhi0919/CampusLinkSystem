# Code Mode Rules

## 后端编码规则

- Service 层必须接口+实现类分离（`service/XxxService.java` + `service/impl/XxxServiceImpl.java`）
- 实体类同时需要 JPA 注解和 MyBatisPlus 注解（两套 ORM 共存）
- 使用 `MyBatisMetaObjectHandler` 自动填充时间字段，无需手动设置 `created_at`/`updated_at`
- 逻辑删除字段 `deleted` 已在 MyBatisPlus 配置，查询时自动过滤

## 前端编码规则

- HTTP 请求必须通过 `utils/request.ts` 的 `Request` 类，不要直接使用 `uni.request`
- Token 刷新逻辑已在 Request 类内置，无需手动处理
- 主题切换使用 `uni.$emit('theme-change')` 广播，组件监听此事件响应
- TabBar 由 `components/layout/CustomTabBar.vue` 实现，非原生
- 响应式断点：`windowWidth > 768px` 区分桌面/移动端
