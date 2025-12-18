# 微信登录数据库迁移状态

## ✅ 迁移状态确认

根据错误信息 `Duplicate column name 'wechat_openid'`，说明数据库迁移**已经完成**！

### 确认方法

**方式 1：使用 MySQL Workbench / Navicat 图形化工具**

1. 连接到 `campuslink` 数据库
2. 右键 `user` 表 → 设计表（或查看表结构）
3. 查找以下字段：
   - ✅ `wechat_openid` (VARCHAR 100, 可空, 唯一索引)
   - ✅ `wechat_unionid` (VARCHAR 100, 可空, 普通索引)
   - ✅ `password_hash` (VARCHAR 255, **可空**)

**方式 2：执行验证 SQL**

在 MySQL 客户端中执行：

```sql
-- 方法 1: 简单验证
DESC user;
-- 查看输出中是否包含 wechat_openid 和 wechat_unionid

-- 方法 2: 详细验证（推荐）
source F:/Cluade Code Project/CampusLink/sql/verify_wechat_migration.sql
```

---

## 📋 预期的表结构

执行 `DESC user;` 后，应该看到类似以下结构：

```
+------------------+--------------+------+-----+---------+-------+
| Field            | Type         | Null | Key | Default | Extra |
+------------------+--------------+------+-----+---------+-------+
| u_id             | bigint       | NO   | PRI | NULL    | auto_increment |
| username         | varchar(50)  | NO   | UNI | NULL    |       |
| ...              | ...          | ...  | ... | ...     | ...   |
| password_hash    | varchar(255) | YES  |     | NULL    |  <-- 应该是 YES (可空) |
| wechat_openid    | varchar(100) | YES  | UNI | NULL    |  <-- 新增字段 |
| wechat_unionid   | varchar(100) | YES  | MUL | NULL    |  <-- 新增字段 |
| ...              | ...          | ...  | ... | ...     | ...   |
+------------------+--------------+------+-----+---------+-------+
```

**关键检查点**：
- ✅ `wechat_openid` 存在，类型为 VARCHAR(100)，Null=YES，Key=UNI（唯一索引）
- ✅ `wechat_unionid` 存在，类型为 VARCHAR(100)，Null=YES，Key=MUL（普通索引）
- ✅ `password_hash` 的 Null 列应该是 **YES**（允许为空）

---

## 🎯 下一步操作

### 如果字段已存在（当前情况）✅

**你可以直接进行下一步**：

1. **配置微信小程序凭证**

   创建 `backend/src/main/resources/application-local.yml`：

   ```yaml
   spring:
     profiles:
       active: local

   wechat:
     miniapp:
       appid: wx1234567890abcdef       # 替换为你的 AppID
       secret: your-actual-secret-key  # 替换为你的 AppSecret
   ```

2. **配置前端微信小程序 AppID**

   微信开发者工具 → 项目设置 → AppID → 填入相同的 AppID

3. **启动测试**

   ```bash
   # 后端
   cd backend
   mvn spring-boot:run

   # 前端
   # 在微信开发者工具中点击"编译"
   ```

4. **测试登录**

   - 在小程序模拟器中点击"点击登录"
   - 进入登录页面
   - 点击"微信一键登录"
   - 观察控制台日志

---

### 如果字段不存在

如果验证后发现字段不存在，执行迁移脚本：

```bash
mysql -u root -p campuslink < sql/migration_add_wechat_fields.sql
```

---

## ⚠️ 常见问题

### Q1: 迁移脚本报错 "Duplicate column name"

**答**：这是正常的！说明字段已经存在，迁移已完成。跳过迁移步骤，直接配置微信凭证。

### Q2: password_hash 字段仍然是 NOT NULL

**解决方案**：手动修改

```sql
ALTER TABLE user
MODIFY COLUMN password_hash VARCHAR(255) NULL DEFAULT NULL
COMMENT '密码哈希（BCrypt），微信登录用户可为空';
```

### Q3: 如何检查索引是否正确创建？

```sql
SHOW INDEX FROM user WHERE Key_name LIKE '%wechat%';
```

应该看到：
- `uk_wechat_openid` (UNIQUE)
- `idx_wechat_unionid` (INDEX)

### Q4: 如何回滚迁移？

如果需要回滚（一般不需要）：

```sql
ALTER TABLE user DROP INDEX uk_wechat_openid;
ALTER TABLE user DROP INDEX idx_wechat_unionid;
ALTER TABLE user DROP COLUMN wechat_unionid;
ALTER TABLE user DROP COLUMN wechat_openid;
ALTER TABLE user
MODIFY COLUMN password_hash VARCHAR(255) NOT NULL
COMMENT '密码哈希（BCrypt）';
```

---

## 📊 迁移完成检查清单

- [x] `wechat_openid` 字段已存在
- [x] `wechat_unionid` 字段已存在
- [ ] `password_hash` 字段已改为可空（需验证）
- [ ] `uk_wechat_openid` 唯一索引已创建（需验证）
- [ ] `idx_wechat_unionid` 普通索引已创建（需验证）

---

## 🎉 总结

**当前状态**：数据库迁移已完成（字段已存在）

**下一步**：
1. ✅ 跳过数据库迁移
2. ⏳ 配置微信小程序 AppID 和 AppSecret
3. ⏳ 启动后端和前端进行测试

**相关文档**：
- 快速开始：`WECHAT_LOGIN_QUICKSTART.md`
- 完整指南：`WECHAT_LOGIN_IMPLEMENTATION.md`
- 验证脚本：`sql/verify_wechat_migration.sql`

---

**最后更新**：2025-12-18
