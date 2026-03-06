# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## 构建命令

```bash
# 后端
cd backend && mvn spring-boot:run                    # 启动开发服务器
mvn test -Dtest=ClassName                            # 运行单个测试类

# 前端
cd frontend && npm run dev:h5                        # H5 开发模式（推荐）
npm run type-check                                   # TypeScript 类型检查（无 lint 工具）
```

## 架构要点（非显而易见）

### 后端双 ORM 共存
`entity/` 为 JPA 实体，`mapper/` 用 MyBatisPlus BaseMapper；两套 ORM 共用同一实体类。MyBatisMetaObjectHandler 自动填充 `created_at`、`updated_at`；所有表有逻辑删除字段 `deleted`。

### 两层认证拦截器（顺序重要）
`WebConfig` 注册顺序：
1. **OptionalJwtAuthInterceptor**：先执行，识别用户但允许游客访问（用于 `/resource/list`、`/question/list`、`/task/list`、`/activity/list`、`/notification/list`）
2. **JwtAuthInterceptor**：后执行，强制登录

### 前端 Token 刷新机制
`utils/request.ts`：Token 剩余 < 15 分钟时提前刷新；`isRefreshing` 标志位 + `requestQueue` 数组防止并发刷新竞争。401 区分"已登录 token 过期"（弹窗）与"游客访问需登录接口"（静默 reject）。

### 前端主题切换
无独立 Store，通过 `App.vue` 中 CSS 自定义属性实现；`document.documentElement` 切换 `dark-mode` 类，`uni.$emit('theme-change')` 广播全局事件。

### 前端 TabBar
`pages.json` 中 `custom: true`，实际由 `components/layout/CustomTabBar.vue` 实现。

## 服务地址

- 前端 H5：http://localhost:5173
- 后端 API：http://localhost:8080（context-path: `/api/v1`）
- API 文档：http://localhost:8080/doc.html

## 代码规范

- **Java**：遵循阿里巴巴 Java 开发手册，Service 层接口+实现类分离
- **TypeScript**：驼峰命名，公共方法必须注释
- RESTful API，统一响应格式：`Result<T>` 和 `PageResult<T>`

## Git Commit 规范

`feat`/`fix`/`docs`/`style`/`refactor`/`perf`/`test`/`chore`
