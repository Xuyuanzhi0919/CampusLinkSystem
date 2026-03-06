# CampusLink - 高校资源互助与问答平台

<div align="center">

![CampusLink Logo](https://via.placeholder.com/150)

一个基于 H5 的高校资源共享与问答社区平台

[功能特性](#功能特性) • [技术栈](#技术栈) • [快速开始](#快速开始) • [项目结构](#项目结构) • [部署指南](#部署指南) • [贡献指南](#贡献指南)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![uni-app](https://img.shields.io/badge/uni--app-Vue3-42b983.svg)](https://uniapp.dcloud.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0+-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

</div>

---

## 📖 项目简介

CampusLink 是一个专为高校学生打造的资源共享与问答社区平台，旨在帮助学生更便捷地获取学习资源、解决学习问题、参与社团活动。

**平台特性**：
- 🌐 **H5 多端支持**：支持桌面端和移动端浏览器，随时随地访问
- 📱 **响应式设计**：自适应不同屏幕尺寸，提供一致的用户体验
- ⚡ **高性能架构**：前后端分离，采用现代化技术栈

**核心价值**：
- 📚 **资源共享**：课件、试题、笔记一键上传下载
- 💬 **问答社区**：学术问题实时交流，采纳最佳答案
- 🎯 **任务大厅**：发布/接受校园任务，赚取积分
- 🏆 **社团活动**：活动发布、在线报名、签到打卡
- 💰 **积分系统**：完成任务获得积分，激励用户参与

> **注意**：本项目当前专注于 H5 端开发，提供最优的 Web 体验。小程序版本已移除，代码已在 `backup/miniprogram-code` 分支备份，未来可根据需求恢复。

---

## ✨ 功能特性

### 🎓 资源管理
- ✅ 资源上传（支持 PDF、Word、PPT、图片等多种格式）
- ✅ 资源分类（课件、试题、笔记、其他）
- ✅ 资源搜索（关键词、分类、标签）
- ✅ 资源评分与评论
- ✅ 资源收藏与下载
- ✅ 管理员审核机制

### 💬 问答社区
- ✅ 发布问题（支持富文本、图片）
- ✅ 回答问题（支持点赞、采纳）
- ✅ 问题分类（学术、生活、技术等）
- ✅ 热门问答榜
- ✅ 我的问答管理
- 🚧 AI 智能答复（规划中）

### 📋 任务系统
- ✅ 发布任务（设置悬赏积分）
- ✅ 任务大厅（浏览所有任务）
- ✅ 接受任务（状态流转管理）
- ✅ 任务提交与验收
- ✅ 我的任务（已发布/已接受）

### 🎯 社团活动
- ✅ 社团列表（浏览所有社团）
- ✅ 社团详情（成员、活动、简介）
- ✅ 活动发布与报名
- ✅ 活动签到（倒计时、二维码）
- ✅ 成员管理

### 💌 私信功能
- ✅ 私信列表（会话管理）
- ✅ 实时聊天（WebSocket）
- ✅ 消息已读/未读
- ✅ 表情选择器
- 🚧 图片/文件发送（规划中）

### 👤 用户中心
- ✅ 个人资料编辑
- ✅ 头像上传
- ✅ 密码修改
- ✅ 积分明细
- ✅ 我的收藏
- ✅ 每日签到

### 🔔 通知系统
- ✅ 系统通知（任务、活动、审核）
- ✅ 互动通知（点赞、评论、回复）
- ✅ 消息中心（统一管理）
- ✅ 未读消息提醒

---

## 🛠 技术栈

### 后端技术
- **核心框架**：Spring Boot 3.4.0
- **ORM 框架**：MyBatisPlus 3.5.9 + JPA
- **数据库**：MySQL 8.0+
- **缓存**：Redis 7.0+
- **认证**：JWT (io.jsonwebtoken 0.12.6)
- **API 文档**：Knife4j (Swagger 3)
- **WebSocket**：Spring WebSocket
- **对象存储**：阿里云 OSS

### 前端技术
- **框架**：uni-app (Vue 3 + TypeScript)
- **平台支持**：H5（桌面端 + 移动端）
- **状态管理**：Pinia
- **UI 组件**：自定义组件库（企业级设计系统）
- **HTTP 请求**：uni.request 封装（自动 Token 刷新、请求重试）
- **WebSocket**：原生 WebSocket（实时聊天）
- **构建工具**：Vite 5.2.8

### 数据库设计
- **17 张核心表**：用户、资源、问答、任务、社团、活动、消息、通知等
- **关系设计**：外键约束、级联删除
- **索引优化**：高频查询字段建立索引

---

## 🚀 快速开始

### 环境要求

- **JDK**: 17+
- **Node.js**: 16+
- **MySQL**: 8.0+
- **Redis**: 7.0+
- **Maven**: 3.6+

### 1. 克隆项目

```bash
git clone https://github.com/your-repo/CampusLink.git
cd CampusLink
```

### 2. 初始化数据库

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE campuslink CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入表结构
mysql -u root -p campuslink < sql/schema.sql

# 导入初始数据（可选）
mysql -u root -p campuslink < sql/init-data.sql
```

### 3. 配置后端

修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campuslink
    username: root
    password: your_password

  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password

jwt:
  secret: your_jwt_secret_key_at_least_32_characters_long
  expiration: 7200  # 2小时
  refresh-expiration: 604800  # 7天

# 阿里云 OSS 配置（可选）
aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    accessKeyId: your_access_key_id
    accessKeySecret: your_access_key_secret
    bucketName: campuslink
```

### 4. 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run

# 或者使用 IDE 运行 CampusLinkApplication.java
```

后端服务将在 `http://localhost:8080` 启动。

### 5. 配置前端

修改 `frontend/src/config.ts`：

```typescript
export const config = {
  baseURL: 'http://localhost:8080/api/v1',
  WS_URL: 'ws://localhost:8080',
  // ... 其他配置
}
```

### 6. 启动前端

```bash
cd frontend
npm install

# H5 开发模式（推荐）
npm run dev:h5
# 或者
npm run dev
```

H5 服务将在 `http://localhost:5173` 启动。

### 7. 访问应用

- **前端 H5**: http://localhost:5173
  - 桌面端：在 PC 浏览器中访问（推荐 Chrome、Edge）
  - 移动端：在手机浏览器中访问，或使用浏览器开发者工具模拟
- **后端 API**: http://localhost:8080
- **API 文档**: http://localhost:8080/doc.html (Knife4j)

---

## 📁 项目结构

```
CampusLink/
├── backend/                    # 后端 Spring Boot 项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/campuslink/
│   │   │   │   ├── config/    # 配置类（Swagger、MyBatis、CORS等）
│   │   │   │   ├── controller/ # REST API 端点
│   │   │   │   ├── service/    # 业务逻辑层
│   │   │   │   ├── mapper/     # MyBatisPlus 数据访问层
│   │   │   │   ├── entity/     # JPA 实体类
│   │   │   │   ├── dto/        # 数据传输对象
│   │   │   │   ├── common/     # 公共类（Result、PageResult等）
│   │   │   │   ├── middleware/ # 中间件（JWT、限流等）
│   │   │   │   ├── exception/  # 异常处理
│   │   │   │   ├── util/       # 工具类
│   │   │   │   └── websocket/  # WebSocket 处理器
│   │   │   └── resources/
│   │   │       ├── application.yml  # 配置文件
│   │   │       └── application-dev.yml
│   │   └── test/              # 测试代码
│   └── pom.xml                # Maven 依赖配置
│
├── frontend/                   # 前端 uni-app 项目
│   ├── src/
│   │   ├── pages/             # 页面组件
│   │   │   ├── home/          # 首页
│   │   │   ├── resource/      # 资源中心
│   │   │   ├── question/      # 问答社区
│   │   │   ├── task/          # 任务大厅
│   │   │   ├── club/          # 社团活动
│   │   │   ├── message/       # 私信
│   │   │   ├── user/          # 个人中心
│   │   │   ├── notification/  # 通知中心
│   │   │   └── error/         # 错误页面
│   │   ├── components/        # 可复用组件
│   │   ├── stores/            # Pinia 状态管理
│   │   ├── services/          # API 服务层
│   │   ├── types/             # TypeScript 类型定义
│   │   ├── utils/             # 工具函数
│   │   ├── config.ts          # 全局配置
│   │   ├── App.vue            # 应用入口
│   │   └── pages.json         # 页面配置
│   └── package.json           # npm 依赖配置
│
├── sql/                        # SQL 脚本
│   ├── schema.sql             # 数据库表结构
│   └── init-data.sql          # 初始化数据
│
├── docs/                       # 项目文档
│   ├── api-design.md          # API 接口文档
│   ├── database-design.md     # 数据库设计文档
│   └── deployment.md          # 部署文档
│
├── CLAUDE.md                   # Claude Code 开发指南
├── README.md                   # 项目说明文档
└── .gitignore                  # Git 忽略配置
```

---

## 📚 核心功能文档

### 积分系统规则

| 操作 | 积分变动 |
|------|---------|
| 注册奖励 | +100 |
| 每日签到 | +10 |
| 上传资源 | +10 |
| 下载资源 | -5 |
| 提问 | -2 |
| 回答问题 | +5 |
| 回答被采纳 | +20 |
| 完成任务 | +任务悬赏(1-100) |
| 活动签到 | +10 |

### 任务状态流转

```
待接单(0) → 进行中(1) → 已完成(2)
           ↘ 已取消(3)
```

**重要规则**：
- 用户不能接受自己发布的任务
- 接单操作需要事务保护（防止并发）
- 任务完成后自动扣除发布者积分，增加接单者积分

### API 认证流程

1. **登录**：`POST /auth/login` → 返回 `Token` 和 `RefreshToken`
2. **请求 API**：在 Header 中携带 `Authorization: Bearer {token}`
3. **Token 刷新**：Token 即将过期时，调用 `POST /auth/refresh` 刷新
4. **退出登录**：`POST /auth/logout` 清除 Token

---

## 🔧 API 文档

后端启动后，访问 Swagger UI 查看完整 API 文档：

**地址**：http://localhost:8080/doc.html

**主要模块**：
- **认证模块**：登录、注册、Token 刷新
- **用户模块**：个人资料、积分、签到
- **资源模块**：上传、搜索、下载、评分
- **问答模块**：提问、回答、点赞、采纳
- **任务模块**：发布、接受、提交、验收
- **社团模块**：列表、详情、成员
- **活动模块**：发布、报名、签到
- **私信模块**：发送、接收、会话列表
- **通知模块**：系统通知、消息中心

---

## 🐳 Docker 部署

### 1. 使用 Docker Compose（推荐）

```bash
# 启动所有服务（MySQL + Redis + Backend）
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 2. 手动构建镜像

```bash
# 构建后端镜像
cd backend
docker build -t campuslink-backend:latest .

# 运行后端容器
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/campuslink \
  -e SPRING_REDIS_HOST=redis \
  --name campuslink-backend \
  campuslink-backend:latest
```

---

## 📊 性能优化

### 前端优化
- ✅ **骨架屏加载**：提升用户感知速度
- ✅ **图片懒加载**：减少首屏加载时间
- ✅ **请求防重复**：防止用户多次点击
- ✅ **Token 自动刷新**：无感知续期
- ✅ **数据缓存**：减少重复请求（5分钟TTL）

### 后端优化
- ✅ **MyBatisPlus 分页**：高效查询
- ✅ **Redis 缓存**：热点数据缓存
- ✅ **数据库索引**：优化常用查询
- ✅ **限流中间件**：防止接口滥用
- 🚧 **读写分离**：规划中
- 🚧 **消息队列**：规划中

---

## 🧪 测试

### 后端测试

```bash
cd backend

# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=UserServiceTest

# 生成测试报告
mvn surefire-report:report
```

### 前端测试

```bash
cd frontend

# TypeScript 类型检查
npm run type-check

# 构建测试
npm run build:h5
```

---

## 📝 开发规范

### Git Commit 规范

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

**示例**：
```bash
git commit -m "feat: 添加资源评分功能"
git commit -m "fix: 修复任务接单并发问题"
```

### 代码风格
- **Java**: 遵循阿里巴巴 Java 开发手册
- **TypeScript**: 使用 ESLint + Prettier
- **命名**: 驼峰命名（类 PascalCase，方法 camelCase）
- **注释**: 公共方法必须添加注释

---

## 🤝 贡献指南

欢迎贡献代码、提出问题或建议！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交改动 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

---

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

---

## 👥 团队成员

- **后端开发**：[Your Name]
- **前端开发**：[Your Name]
- **UI/UX 设计**：[Your Name]
- **产品经理**：[Your Name]

---

## 📞 联系方式

- **项目主页**：https://github.com/your-repo/CampusLink
- **问题反馈**：https://github.com/your-repo/CampusLink/issues
- **邮箱**：contact@campuslink.com

---

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis Plus](https://baomidou.com/)
- [uni-app](https://uniapp.dcloud.io/)
- [Vue.js](https://vuejs.org/)
- [Knife4j](https://doc.xiaominfo.com/)

---

<div align="center">

**⭐ 如果觉得项目不错，请给个 Star 支持一下！**

Made with ❤️ by CampusLink Team

</div>
