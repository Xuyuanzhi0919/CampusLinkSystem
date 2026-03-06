# 微信小程序登录数据库迁移指南

## 📋 迁移概述

本迁移为 `user` 表添加微信小程序登录所需的字段，支持：
- 微信一键登录（自动注册）
- 微信账号绑定到现有用户
- 统一的用户数据库管理

## 🎯 迁移内容

### 新增字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `wechat_openid` | VARCHAR(100) | 微信小程序用户唯一标识（OpenID） |
| `wechat_unionid` | VARCHAR(100) | 微信开放平台统一标识（UnionID） |

### 字段修改

| 字段名 | 原类型 | 新类型 | 说明 |
|--------|--------|--------|------|
| `password_hash` | NOT NULL | NULL | 允许微信登录用户无需密码 |

### 新增索引

| 索引名 | 类型 | 字段 | 说明 |
|--------|------|------|------|
| `uk_wechat_openid` | UNIQUE | wechat_openid | 防止同一微信账号重复绑定 |
| `idx_wechat_unionid` | INDEX | wechat_unionid | 加速 UnionID 查询 |

## 🚀 执行迁移

### 方式 1：使用 MySQL 命令行（推荐）

```bash
# 1. 连接到 MySQL（根据实际情况修改用户名和密码）
mysql -u root -p

# 2. 执行迁移脚本
source F:/Cluade Code Project/CampusLink/sql/migration_add_wechat_fields.sql

# 或者使用单行命令
mysql -u root -p campuslink < "F:/Cluade Code Project/CampusLink/sql/migration_add_wechat_fields.sql"
```

### 方式 2：使用 MySQL Workbench / Navicat 等图形化工具

1. 打开 MySQL 客户端工具
2. 连接到 `campuslink` 数据库
3. 打开 SQL 编辑器
4. 复制 `migration_add_wechat_fields.sql` 的内容
5. 执行 SQL 脚本
6. 查看执行结果

### 方式 3：手动执行 SQL 语句

如果上述方式不可用，可以手动复制以下 SQL 语句执行：

```sql
USE campuslink;

-- 1. 添加微信 OpenID 字段
ALTER TABLE `user`
ADD COLUMN `wechat_openid` VARCHAR(100) NULL DEFAULT NULL COMMENT '微信OpenID（小程序用户唯一标识）'
AFTER `password_hash`;

-- 2. 添加微信 UnionID 字段
ALTER TABLE `user`
ADD COLUMN `wechat_unionid` VARCHAR(100) NULL DEFAULT NULL COMMENT '微信UnionID（开放平台统一标识）'
AFTER `wechat_openid`;

-- 3. 为 wechat_openid 添加唯一索引
ALTER TABLE `user`
ADD UNIQUE INDEX `uk_wechat_openid`(`wechat_openid` ASC) USING BTREE COMMENT '微信OpenID唯一索引';

-- 4. 为 wechat_unionid 添加索引
ALTER TABLE `user`
ADD INDEX `idx_wechat_unionid`(`wechat_unionid` ASC) USING BTREE COMMENT '微信UnionID索引';

-- 5. 修改 password_hash 字段为可空
ALTER TABLE `user`
MODIFY COLUMN `password_hash` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码哈希（BCrypt），微信登录用户可为空';
```

## ✅ 验证迁移结果

执行以下 SQL 验证迁移是否成功：

```sql
-- 1. 查看 user 表结构（应该看到新增的字段）
DESC `user`;

-- 2. 查看索引（应该看到 uk_wechat_openid 和 idx_wechat_unionid）
SHOW INDEX FROM `user`;

-- 3. 查看字段详细信息
SHOW FULL COLUMNS FROM `user` WHERE Field IN ('wechat_openid', 'wechat_unionid', 'password_hash');
```

### 预期结果

执行 `DESC user;` 后应该看到类似以下字段：

```
+------------------+--------------+------+-----+---------+-------+
| Field            | Type         | Null | Key | Default | Extra |
+------------------+--------------+------+-----+---------+-------+
| ...              | ...          | ...  | ... | ...     | ...   |
| password_hash    | varchar(255) | YES  |     | NULL    |       |
| wechat_openid    | varchar(100) | YES  | UNI | NULL    |       |
| wechat_unionid   | varchar(100) | YES  | MUL | NULL    |       |
| ...              | ...          | ...  | ... | ...     | ...   |
+------------------+--------------+------+-----+---------+-------+
```

## 🔄 回滚迁移

如果需要回滚此迁移，执行以下 SQL：

```sql
USE campuslink;

-- 删除索引
ALTER TABLE `user` DROP INDEX `uk_wechat_openid`;
ALTER TABLE `user` DROP INDEX `idx_wechat_unionid`;

-- 删除字段
ALTER TABLE `user` DROP COLUMN `wechat_unionid`;
ALTER TABLE `user` DROP COLUMN `wechat_openid`;

-- 恢复 password_hash 为 NOT NULL
ALTER TABLE `user`
MODIFY COLUMN `password_hash` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码哈希（BCrypt）';
```

## 📝 迁移后续步骤

1. ✅ 数据库迁移完成
2. ⏳ 配置微信小程序 AppID 和 AppSecret（`application.yml` 或 `application-local.yml`）
3. ⏳ 启动后端服务，测试 `/auth/wechat/login` 接口
4. ⏳ 前端创建微信登录服务文件（`services/auth.ts`）
5. ⏳ 完善小程序登录页面（`pages/auth/mp-login.vue`）
6. ⏳ 端到端测试登录流程

## ⚠️ 注意事项

1. **备份数据**：迁移前建议备份 `user` 表数据
   ```bash
   mysqldump -u root -p campuslink user > user_backup_$(date +%Y%m%d).sql
   ```

2. **微信 AppID 配置**：需要先申请微信小程序并获取 AppID 和 AppSecret

3. **兼容性**：此迁移保持向后兼容，原有用户名密码登录功能不受影响

4. **测试环境优先**：建议先在测试环境执行迁移，验证无误后再迁移生产环境

## 🔗 相关文档

- 后端配置文件：`backend/src/main/resources/application.yml`
- 微信登录控制器：`backend/src/main/java/com/campuslink/controller/WechatAuthController.java`
- 微信服务：`backend/src/main/java/com/campuslink/service/WechatService.java`
- 用户实体类：`backend/src/main/java/com/campuslink/entity/User.java`
- 小程序登录页面：`frontend/src/pages/auth/mp-login.vue`

## 💡 FAQ

**Q: 为什么 password_hash 要改为可空？**
A: 微信登录用户通过 OpenID 认证，无需密码。如果后续用户绑定账号，可以设置密码。

**Q: OpenID 和 UnionID 有什么区别？**
A:
- OpenID：每个小程序独立，同一用户在不同小程序有不同 OpenID
- UnionID：同一微信开放平台下所有应用统一，需要在微信开放平台绑定小程序才能获取

**Q: 如何测试迁移是否成功？**
A: 执行验证 SQL，查看字段和索引是否正确创建。然后启动后端，调用 `/auth/wechat/login` 接口测试。

**Q: 迁移失败怎么办？**
A: 检查错误信息，如果是字段已存在，说明已经迁移过。如果是其他错误，可以执行回滚脚本后重新迁移。
