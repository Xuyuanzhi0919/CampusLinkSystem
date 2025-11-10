# CampusLink 热门标签功能 - 部署与测试指南

## 📋 功能概述

本次开发实现了动态热门标签功能，将原本硬编码在前端的标签数据迁移到数据库，支持：
- ✅ 动态获取热门标签（按使用次数排序）
- ✅ 标签分类管理（学习/生活/技术/娱乐）
- ✅ 标签搜索功能
- ✅ 标签与内容（问题/资源/任务）关联
- ✅ 标签使用次数统计

---

## 🗄️ 数据库变更

### 1. 新增表结构

本次新增了 **2 张表**：

#### 1.1 标签表 (tag)
存储所有标签及其热度统计

| 字段 | 类型 | 说明 |
|------|------|------|
| tag_id | BIGINT | 标签ID（主键） |
| tag_name | VARCHAR(50) | 标签名称（不含#号） |
| display_name | VARCHAR(50) | 显示名称（含#号） |
| use_count | INT | 使用次数（热度） |
| category | VARCHAR(20) | 标签分类 |
| description | VARCHAR(200) | 标签描述 |
| status | TINYINT | 状态：0-禁用，1-正常 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### 1.2 标签关联表 (tag_relation)
记录标签与内容的关联关系

| 字段 | 类型 | 说明 |
|------|------|------|
| relation_id | BIGINT | 关联ID（主键） |
| tag_id | BIGINT | 标签ID |
| target_type | VARCHAR(20) | 对象类型：question/resource/task |
| target_id | BIGINT | 对象ID |
| created_at | DATETIME | 创建时间 |

### 2. 初始化数据

脚本已预置 **12 个热门标签**：
- 考研资料、学习打卡、AI问答、Python基础
- 数据结构、英语四六级、算法刷题、前端开发
- Java学习、求职面试、实习经验、校园生活

---

## 🚀 部署步骤

### 步骤 1：执行数据库迁移脚本

```bash
# 方式1：直接执行SQL文件
mysql -u root -p campuslink < sql/create-tag-table.sql

# 方式2：使用MySQL客户端
mysql -u root -p
mysql> USE campuslink;
mysql> SOURCE /path/to/create-tag-table.sql;
```

### 步骤 2：验证数据库表

```sql
-- 查看表是否创建成功
SHOW TABLES LIKE 'tag%';

-- 查看初始化数据
SELECT * FROM tag ORDER BY use_count DESC LIMIT 10;

-- 查看视图是否创建
SELECT * FROM v_hot_tags LIMIT 10;
```

预期结果：
- `tag` 表包含 12 条初始数据
- `tag_relation` 表为空（等待后续关联）
- `v_hot_tags` 视图能正常查询

### 步骤 3：重启后端服务

```bash
# 方式1：使用Maven（开发环境）
cd backend
mvn spring-boot:run

# 方式2：使用Docker（生产环境）
docker-compose restart campuslink-backend

# 验证后端启动成功
curl http://localhost:8080/actuator/health
```

### 步骤 4：重启前端服务

```bash
# H5开发模式
cd frontend
npm run dev:h5

# 微信小程序开发模式
npm run dev:mp-weixin
```

---

## 🧪 测试指南

### 1. API 接口测试

#### 测试工具：Postman / Apifox / curl

#### 1.1 获取热门标签列表

```bash
# 请求
GET http://localhost:8080/tag/hot?limit=8

# 预期响应
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "tagId": 1,
      "tagName": "考研资料",
      "displayName": "#考研资料",
      "useCount": 120,
      "category": "学习",
      "description": "考研相关的资料和经验分享"
    },
    ...
  ]
}
```

#### 1.2 根据分类获取标签

```bash
# 请求
GET http://localhost:8080/tag/hot/category?category=学习&limit=5

# 预期响应
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "tagId": 1,
      "tagName": "考研资料",
      "displayName": "#考研资料",
      ...
    }
  ]
}
```

#### 1.3 搜索标签

```bash
# 请求
GET http://localhost:8080/tag/search?keyword=考研&limit=5

# 预期响应
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "tagId": 1,
      "tagName": "考研资料",
      "displayName": "#考研资料",
      ...
    }
  ]
}
```

### 2. 前端功能测试

#### 测试场景 1：首页热门标签显示

1. 打开首页
2. 滚动到右侧"热门标签"卡片
3. **预期结果**：
   - 显示 8 个热门标签（按使用次数排序）
   - 标签格式为 `#标签名`
   - 标签可点击

#### 测试场景 2：标签点击跳转

1. 点击任意标签（如 `#考研资料`）
2. **预期结果**：
   - 跳转到搜索结果页面
   - 搜索关键词为"考研资料"（已去除#号）
   - URL: `/pages/search/result?keyword=%E8%80%83%E7%A0%94%E8%B5%84%E6%96%99`

#### 测试场景 3：标签加载失败降级

1. 关闭后端服务或断网
2. 刷新首页
3. **预期结果**：
   - 显示默认标签列表（8个硬编码标签）
   - 控制台输出错误日志：`加载热门标签失败`
   - 不影响其他功能使用

### 3. 性能测试

#### 测试要点

| 测试项 | 预期指标 |
|--------|----------|
| API 响应时间 | < 100ms |
| 前端加载时间 | < 200ms |
| 数据库查询 | 使用索引，< 10ms |
| 并发支持 | 100 QPS |

#### 测试方法

```bash
# 使用Apache Bench测试API性能
ab -n 1000 -c 10 http://localhost:8080/tag/hot?limit=8

# 预期结果
# Requests per second: > 100 [#/sec]
# Time per request: < 100 [ms]
```

---

## 🔧 故障排查

### 问题 1：前端显示空白标签

**现象**：首页标签卡片为空或显示加载中

**排查步骤**：
1. 检查浏览器控制台是否有网络错误
2. 检查API接口是否正常：`curl http://localhost:8080/tag/hot`
3. 检查数据库表是否有数据：`SELECT COUNT(*) FROM tag;`

**解决方案**：
- 如果API返回空数组，执行初始化脚本插入数据
- 如果API报错，检查后端日志：`docker logs campuslink-backend`

---

### 问题 2：标签点击无反应

**现象**：点击标签后没有跳转

**排查步骤**：
1. 检查搜索页面路由是否存在：`frontend/src/pages/search/result.vue`
2. 检查前端控制台是否有路由错误
3. 检查标签点击事件是否触发：在 `handleTagClick` 中添加断点

**解决方案**：
- 如果搜索页面不存在，创建占位页面或修改跳转逻辑
- 临时降级：点击标签显示 Toast 提示

---

### 问题 3：数据库连接失败

**现象**：后端启动报错 `Communications link failure`

**排查步骤**：
1. 检查MySQL是否运行：`docker ps | grep mysql`
2. 检查数据库配置：`backend/src/main/resources/application.yml`
3. 验证连接：`mysql -h 127.0.0.1 -u root -p`

**解决方案**：
```bash
# 启动MySQL容器
docker-compose up -d mysql

# 重置数据库密码
docker exec -it campuslink-mysql mysql -u root -p
mysql> ALTER USER 'root'@'%' IDENTIFIED BY 'your_password';
```

---

## 📊 数据库索引优化

为保证查询性能，脚本已自动创建以下索引：

```sql
-- 标签表索引
CREATE INDEX idx_use_count ON tag(use_count DESC);  -- 热门排序
CREATE INDEX idx_status ON tag(status);             -- 状态过滤
CREATE INDEX idx_category ON tag(category);         -- 分类查询

-- 关联表索引
CREATE INDEX idx_target ON tag_relation(target_type, target_id);  -- 查询对象标签
CREATE INDEX idx_tag_id ON tag_relation(tag_id);                   -- 查询标签使用
```

---

## 🎯 后续扩展建议

1. **标签推荐算法**：根据用户浏览历史推荐个性化标签
2. **标签热度计算**：定时任务重新计算标签热度（加权：时间衰减）
3. **标签管理后台**：管理员可编辑、禁用、合并标签
4. **标签自动提取**：AI 从问题内容中自动提取标签
5. **标签关联分析**：统计哪些标签经常一起出现

---

## 📝 提交清单

### 已完成文件

#### 后端（7个文件）

1. `sql/create-tag-table.sql` - 数据库建表脚本
2. `backend/src/main/java/com/campuslink/entity/Tag.java` - 标签实体
3. `backend/src/main/java/com/campuslink/entity/TagRelation.java` - 关联实体
4. `backend/src/main/java/com/campuslink/mapper/TagMapper.java` - 标签Mapper
5. `backend/src/main/java/com/campuslink/mapper/TagRelationMapper.java` - 关联Mapper
6. `backend/src/main/java/com/campuslink/service/TagService.java` - 标签服务
7. `backend/src/main/java/com/campuslink/controller/TagController.java` - 标签控制器
8. `backend/src/main/java/com/campuslink/dto/tag/TagResponse.java` - 标签DTO

#### 前端（2个文件）

1. `frontend/src/services/tag.ts` - 标签API服务
2. `frontend/src/pages/home/components/HotRankingPanel.vue` - 首页组件（已修改）

#### 文档（2个文件）

1. `docs/api-design.md` - API文档（已更新）
2. `docs/hot-tags-feature-guide.md` - 本部署指南

### API 接口清单

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /tag/hot | 获取热门标签 |
| GET | /tag/hot/category | 根据分类获取热门标签 |
| GET | /tag/search | 搜索标签 |
| GET | /tag/target | 获取对象关联标签 |

---

## ✅ 验收标准

完成以下检查后即可上线：

- [ ] 数据库表创建成功，包含初始数据
- [ ] 后端服务启动正常，无报错
- [ ] API接口返回正确数据
- [ ] 前端首页显示热门标签
- [ ] 点击标签能正常跳转搜索页面
- [ ] API响应时间 < 100ms
- [ ] 代码通过 ESLint / Checkstyle 检查
- [ ] 已更新API文档

---

## 📞 技术支持

如遇问题，请联系：
- **开发者**：Claude Code
- **文档位置**：`docs/hot-tags-feature-guide.md`
- **Issue提交**：https://github.com/your-org/campuslink/issues

---

**最后更新**：2025-01-10
**版本**：v1.0
**适用环境**：开发环境、测试环境、生产环境
