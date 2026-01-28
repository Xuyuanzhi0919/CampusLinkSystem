# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

CampusLink 是一个基于 H5 技术的高校资源共享与问答社区平台，采用前后端分离架构。

**核心功能**：资源共享、问答社区、任务大厅、社团活动、积分系统

## 技术栈

### 后端 (Spring Boot 3.4.0)
- JDK 17+, Spring Boot 3.4.0, MyBatisPlus 3.5.9, JPA
- MySQL 8.0+, Redis 7.0+
- JWT 认证 (io.jsonwebtoken 0.12.6)
- WebSocket 实时通信
- Knife4j (Swagger 3) API 文档
- 阿里云 OSS 对象存储

### 前端 (uni-app + Vue 3)
- TypeScript + Vite 5.2.8
- Pinia 状态管理
- uni-ui 组件库
- Markdown 支持 (markdown-it + KaTeX)
- PDF 预览 (pdfjs-dist)

## 开发命令

### 后端
```bash
cd backend
mvn clean install              # 构建项目
mvn spring-boot:run           # 启动开发服务器
mvn test                      # 运行测试
```

### 前端
```bash
cd frontend
npm install                   # 安装依赖
npm run dev:h5               # H5 开发模式（推荐）
npm run build:h5             # H5 构建生产版本
npm run type-check           # TypeScript 类型检查
```

### 服务地址
- 前端 H5：http://localhost:5173
- 后端 API：http://localhost:8080
- API 文档：http://localhost:8080/doc.html

## 目录结构

```
CampusLink/
├── backend/                    # Spring Boot 后端
│   └── src/main/java/com/campuslink/
│       ├── config/            # 配置类（Swagger、MyBatis、CORS等）
│       ├── controller/        # REST API 控制器
│       ├── service/           # 业务逻辑层
│       ├── mapper/            # MyBatisPlus 数据访问
│       ├── entity/            # JPA 实体类
│       ├── dto/               # 数据传输对象
│       ├── websocket/         # WebSocket 处理器
│       └── middleware/        # JWT、限流等中间件
├── frontend/                   # uni-app 前端
│   └── src/
│       ├── pages/             # 页面组件
│       ├── components/        # 可复用组件
│       ├── stores/            # Pinia 状态管理
│       ├── services/          # API 服务层
│       ├── types/             # TypeScript 类型定义
│       └── utils/             # 工具函数
├── sql/                        # 数据库脚本
└── docs/                       # 项目文档
```

## 架构要点

### 前后端分离
- API 路径统一使用 `/api/v1` 前缀
- 统一响应格式：`Result<T>` 和 `PageResult<T>`
- JWT Token 认证，前端自动刷新 Token

### 前端状态管理
- Pinia stores 位于 `frontend/src/stores/`
- 支持持久化：用户信息、Token
- 主要 Store：user、question 等

### 数据库
- 17 张核心表，逻辑删除支持
- MyBatisPlus + JPA 双 ORM
- SQL 脚本位于 `sql/` 目录

### API 文档
- 后端集成 Knife4j，访问 `/doc.html`
- API 设计文档位于 `docs/api-design.md`

## Git Commit 规范

```
feat: 新功能
fix: 修复 Bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
perf: 性能优化
test: 测试相关
chore: 构建/工具链相关
```

## 代码规范

- **Java**：遵循阿里巴巴 Java 开发手册
- **TypeScript**：驼峰命名，公共方法必须注释
- 命名：类 PascalCase，方法 camelCase
- RESTful API 风格设计
