# AI 智能助手集成指南

## 📋 概述

CampusLink 已成功集成 DeepSeek AI 服务,提供智能学习助手功能。本文档说明如何配置和使用。

---

## ✅ 已实现功能

### 1. 后端 API (Spring Boot)

**文件结构**:
```
backend/src/main/java/com/campuslink/
├── controller/
│   └── AiAssistantController.java       # AI 控制器
├── service/
│   ├── AiAssistantService.java          # 服务接口
│   └── impl/
│       └── AiAssistantServiceImpl.java  # DeepSeek API 实现
└── dto/ai/
    ├── AiChatRequest.java               # 请求 DTO
    └── AiChatResponse.java              # 响应 DTO
```

**核心功能**:
- ✅ DeepSeek API 集成
- ✅ 对话历史上下文管理(最近10轮)
- ✅ Token 使用统计
- ✅ 智能系统提示词(专注学习辅导)
- ✅ API Key 未配置时的演示模式

**接口文档**:
- `POST /api/v1/ai/chat` - AI 对话
- `GET /api/v1/ai/health` - 健康检查

### 2. 前端实现 (uni-app)

**文件结构**:
```
frontend/src/
├── pages/ai/
│   └── chat.vue                        # AI 聊天页面
├── services/
│   └── ai.ts                           # AI 服务层
├── components/
│   └── MarkdownRenderer.vue            # Markdown 渲染组件
└── types/
    └── ai.ts                           # 类型定义
```

**核心功能**:
- ✅ 真实 DeepSeek API 调用
- ✅ Markdown 渲染支持(代码高亮、表格、列表等)
- ✅ 打字机效果(流式显示)
- ✅ 对话历史本地存储
- ✅ 错误处理优化
- ✅ 消息配对逻辑修复

**Markdown 特性**:
- ✅ 代码块语法高亮(10种语言)
- ✅ 粗体、斜体、链接
- ✅ 有序/无序列表
- ✅ 表格渲染
- ✅ 引用块
- ✅ 分隔线
- ✅ GitHub Dark 主题

---

## 🔧 配置步骤

### 步骤 1: 获取 DeepSeek API Key

1. 访问 [DeepSeek 开放平台](https://platform.deepseek.com/)
2. 注册/登录账号
3. 进入 API Keys 页面创建新 Key
4. 复制 API Key(格式: `sk-...`)

### 步骤 2: 配置后端

#### 方式 A: 环境变量(推荐生产环境)

```bash
# Linux/Mac
export DEEPSEEK_API_KEY=sk-your-actual-key-here

# Windows CMD
set DEEPSEEK_API_KEY=sk-your-actual-key-here

# Windows PowerShell
$env:DEEPSEEK_API_KEY="sk-your-actual-key-here"
```

#### 方式 B: 配置文件(推荐开发环境)

创建 `backend/src/main/resources/application-local.yml`:

```yaml
deepseek:
  api:
    key: sk-your-actual-key-here
```

**注意**: `application-local.yml` 已在 `.gitignore` 中,不会被提交到 Git。

#### 方式 C: 直接修改 application.yml(不推荐)

```yaml
deepseek:
  api:
    key: sk-your-actual-key-here  # 不要提交到 Git!
```

### 步骤 3: 启动后端

```bash
cd backend
mvn spring-boot:run
```

**验证**:访问 `http://localhost:8080/api/v1/ai/health`

### 步骤 4: 启动前端

```bash
cd frontend
npm run dev:h5
```

访问 AI 聊天页面测试功能。

---

## 🧪 测试指南

### 测试 API Key 配置

**未配置时**:
- AI 回复会显示演示模式提示
- 包含配置方法说明

**已配置时**:
- 正常调用 DeepSeek API
- 返回真实 AI 回复

### 测试 Markdown 渲染

发送以下消息测试各种格式:

```markdown
请用 Markdown 格式展示一个 Python 函数示例，包括代码块、列表和表格
```

预期 AI 回复应正确渲染:
- 代码块有语法高亮
- 列表格式正确
- 表格边框清晰

### 测试对话上下文

1. 发送: "我叫小明"
2. 发送: "我叫什么名字？"
3. AI 应能记住前文,回答"小明"

---

## ⚙️ 高级配置

### 修改 AI 模型参数

编辑 `backend/src/main/resources/application.yml`:

```yaml
deepseek:
  model: deepseek-chat              # 模型名称
  max-tokens: 2000                   # 最大生成 Token 数
  temperature: 0.7                   # 创造性(0-2,越高越随机)
```

**参数说明**:
- `max-tokens`: 单次回复最大长度(建议 1000-4000)
- `temperature`:
  - `0.0-0.3`: 精确、一致的回答
  - `0.4-0.7`: 平衡创造性和准确性
  - `0.8-2.0`: 高度创造性,可能不太准确

### 自定义系统提示词

修改 `AiAssistantServiceImpl.java` 的 `getSystemPrompt()` 方法:

```java
private String getSystemPrompt() {
    return """
            你是一个专业的学习助手...
            [根据需求自定义提示词]
            """;
}
```

### 调整对话历史长度

修改 `AiAssistantServiceImpl.java`:

```java
// 当前保留最近 20 条消息(10轮对话)
int startIndex = Math.max(0, history.size() - 20);

// 修改为保留 30 条(15轮)
int startIndex = Math.max(0, history.size() - 30);
```

**注意**: 历史越长,Token 消耗越大,成本越高。

---

## 💰 成本估算

**DeepSeek 定价**(截至2025年1月):
- 输入: ¥0.001/1K tokens
- 输出: ¥0.002/1K tokens

**估算示例**:
- 单次对话(含10轮历史): ~800-1500 tokens
- 1000次对话成本: ¥1-3元
- 月活1000用户,人均10次对话: 约 ¥10-30/月

**优化建议**:
- 限制对话历史长度(当前10轮)
- 设置每日请求上限
- 监控 Token 使用量

---

## 🐛 常见问题

### Q1: AI 一直返回演示模式提示?

**原因**: API Key 未正确配置

**解决**:
1. 检查环境变量: `echo $DEEPSEEK_API_KEY`
2. 检查 `application-local.yml` 是否存在且正确
3. 重启后端服务

### Q2: 报错 "AI 服务暂时不可用"?

**可能原因**:
1. API Key 无效或过期
2. 网络问题(国内访问 DeepSeek 可能需要代理)
3. DeepSeek 服务故障

**解决**:
1. 验证 API Key 有效性
2. 检查后端日志: `调用 DeepSeek API 失败`
3. 测试网络: `curl https://api.deepseek.com/v1/models`

### Q3: Markdown 代码块没有高亮?

**原因**: 语言标识不正确

**解决**:
- 确保代码块指定语言: \`\`\`java ... \`\`\`
- 支持的语言: javascript、typescript、python、java、cpp、css、html、bash、sql、json

### Q4: 打字机效果太快/太慢?

**调整速度**:编辑 `chat.vue`:

```typescript
// 当前 30ms/字符
const interval = setInterval(() => {
  // ...
}, 30)

// 改为 50ms(更慢)
}, 50)

// 改为 15ms(更快)
}, 15)
```

### Q5: 对话历史丢失?

**原因**: LocalStorage 被清除或超出 5MB 限制

**解决**:
1. 检查浏览器控制台是否有存储错误
2. 实施云端同步(需扩展后端)

---

## 📊 监控和日志

### 查看 API 调用日志

后端日志会记录:
```
发送 DeepSeek API 请求，用户 ID: 123
DeepSeek API 响应成功，Token 使用: 856
```

### 前端错误追踪

浏览器控制台:
```javascript
AI API 调用失败: {error details}
```

### 建议的监控指标

1. **请求成功率**: 成功请求/总请求
2. **平均响应时间**: API 调用耗时
3. **Token 消耗统计**: 每日/每月总 Token 数
4. **用户活跃度**: 使用 AI 的用户数

---

## 🚀 后续优化方向

### 1. 流式响应(SSE)

当前实现是等待完整响应后再打字机播放。可改为:
- 后端使用 `stream: true`
- 前端使用 SSE 接收流式数据
- 实时显示生成内容

### 2. 多会话管理

- 支持创建多个对话主题
- 会话列表持久化到云端
- 会话重命名、删除功能

### 3. 消息反馈机制

- 添加👍👎按钮
- 收集反馈用于优化提示词
- 统计高分/低分回复

### 4. 图片识别(多模态)

- 集成 GPT-4 Vision 或类似服务
- 支持上传作业题目截图
- AI 识别并解答图片内容

### 5. 语音输入输出

- 语音转文字(已有语音搜索基础)
- 文字转语音(TTS)
- 实现语音对话

---

## 📝 技术栈

**后端**:
- Spring Boot 3.4
- RestTemplate (HTTP 客户端)
- Jackson (JSON 处理)

**前端**:
- Vue 3 Composition API
- marked (Markdown 解析)
- highlight.js (代码高亮)
- uni-app (跨平台框架)

**AI 服务**:
- DeepSeek Chat API
- 模型: deepseek-chat

---

## 📞 技术支持

遇到问题可:
1. 查看本文档的常见问题章节
2. 检查后端日志和前端控制台
3. 提交 GitHub Issue

---

**最后更新**: 2026-01-03
**维护者**: Claude Code Assistant
