# Ask Mode Rules

## 文档位置

- API 设计文档：`docs/api-design.md`
- 数据库设计：`docs/database-design.md`
- 部署指南：`docs/deployment.md`
- 前端对接文档：`docs/API前端对接文档.md`

## 关键概念解释

- **双 ORM 共存**：项目同时使用 JPA 和 MyBatisPlus，实体类需要两套注解
- **两层拦截器**：OptionalJwtAuthInterceptor（可选认证）先于 JwtAuthInterceptor（强制认证）执行
- **Token 刷新机制**：前端在 Token 剩余 < 15 分钟时自动刷新，使用队列防止并发刷新
- **主题切换**：通过 CSS 自定义属性实现，非 Pinia Store 管理

## 服务地址

- 前端 H5：http://localhost:5173
- 后端 API：http://localhost:8080/api/v1
- API 文档：http://localhost:8080/doc.html（Knife4j）
