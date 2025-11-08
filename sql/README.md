# CampusLink 数据库脚本使用说明

## 📁 文件说明

### 1. schema.sql
**数据库建表脚本**，包含：
- 数据库创建
- 17张核心表的完整定义
- 所有索引和外键约束
- 表注释和字段注释

### 2. init-data.sql
**初始化数据脚本**，包含：
- 10所示例学校数据
- 1个管理员账号 + 5个测试用户
- 系统配置参数
- 示例资源、问题、回答、任务、社团、活动等数据

---

## 🚀 快速开始

### 方式一：命令行执行（推荐）

#### 1. 创建数据库和表结构
```bash
mysql -u root -p < schema.sql
```

#### 2. 导入初始化数据
```bash
mysql -u root -p < init-data.sql
```

#### 3. 验证安装
```bash
mysql -u root -p -e "USE campuslink; SHOW TABLES;"
```

---

### 方式二：MySQL客户端执行

#### 1. 登录MySQL
```bash
mysql -u root -p
```

#### 2. 执行建表脚本
```sql
SOURCE /path/to/schema.sql;
```

#### 3. 执行初始化数据脚本
```sql
SOURCE /path/to/init-data.sql;
```

---

### 方式三：使用Navicat/DBeaver等图形化工具

1. 连接到MySQL服务器
2. 右键选择"运行SQL文件"
3. 依次执行 `schema.sql` 和 `init-data.sql`

---

## 📊 数据库结构

### 核心表列表（17张表）

| 序号 | 表名 | 说明 | 主要字段 |
|------|------|------|---------|
| 1 | school | 学校信息表 | school_id, school_name, city |
| 2 | user | 用户表 | u_id, username, email, points, level |
| 3 | resource | 资源表 | r_id, title, file_url, downloads, likes |
| 4 | question | 问题表 | q_id, title, content, ai_answer, is_solved |
| 5 | answer | 回答表 | a_id, question_id, content, is_accepted |
| 6 | task | 任务表 | t_id, title, task_type, reward_points, status |
| 7 | message | 私信表 | m_id, sender_id, receiver_id, content |
| 8 | club | 社团表 | club_id, club_name, school_id, member_count |
| 9 | club_member | 社团成员表 | cm_id, club_id, user_id, role |
| 10 | activity | 活动表 | activity_id, club_id, title, start_time |
| 11 | activity_participant | 活动参与表 | ap_id, activity_id, user_id, is_signed_in |
| 12 | report | 举报表 | report_id, target_type, target_id, status |
| 13 | points_log | 积分记录表 | log_id, user_id, points_change, reason |
| 14 | download_log | 下载记录表 | dl_id, user_id, resource_id, points_cost |
| 15 | comment | 评论表 | comment_id, target_type, target_id, content |
| 16 | notification | 通知表 | notification_id, user_id, notify_type, is_read |
| 17 | system_config | 系统配置表 | config_id, config_key, config_value |

---

## 👤 默认账号

### 管理员账号
- **用户名**: `admin`
- **密码**: `admin123`
- **邮箱**: `admin@campuslink.com`
- **角色**: 管理员
- **积分**: 10000

### 测试用户账号
所有测试用户的密码都是：`123456`

| 用户名 | 昵称 | 学号 | 专业 | 积分 | 等级 |
|--------|------|------|------|------|------|
| zhangsan | 张三 | 2021001 | 计算机科学与技术 | 500 | 3 |
| lisi | 李四 | 2021002 | 软件工程 | 300 | 2 |
| wangwu | 王五 | 2020001 | 电子信息工程 | 800 | 5 |
| zhaoliu | 赵六 | 2022001 | 数据科学与大数据技术 | 200 | 1 |
| sunqi | 孙七 | 2019001 | 人工智能 | 1200 | 8 |

**⚠️ 注意**：以上密码哈希值是示例，实际部署时需要使用BCrypt重新生成！

---

## 🔧 配置说明

### 数据库配置要求
- **MySQL版本**: 8.0+
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci
- **存储引擎**: InnoDB
- **时区**: Asia/Shanghai

### 修改数据库配置
如果需要修改数据库名称，请编辑 `schema.sql` 文件的第9行：
```sql
CREATE DATABASE IF NOT EXISTS `campuslink` 
```

---

## 📝 初始化数据说明

### 1. 学校数据
包含10所国内知名高校的基本信息（清华、北大、复旦等）

### 2. 系统配置
包含30个系统配置项，涵盖：
- 积分规则（上传、下载、回答等）
- 等级阈值（1-10级）
- 上传限制（文件大小、类型）
- AI功能开关

### 3. 示例数据
- 5个示例资源（课件、试题、笔记）
- 4个示例问题
- 5个示例回答
- 3个示例任务
- 3个示例社团
- 4个示例活动

---

## 🔐 安全建议

### 1. 修改默认密码
**生产环境部署前必须修改所有默认密码！**

使用BCrypt生成新密码哈希：
```java
// Java示例
String hashedPassword = BCrypt.hashpw("your_password", BCrypt.gensalt());
```

```python
# Python示例
from bcrypt import hashpw, gensalt
hashed = hashpw(b"your_password", gensalt())
```

### 2. 数据库用户权限
不要使用root用户连接应用，创建专用数据库用户：
```sql
-- 创建应用专用用户
CREATE USER 'campuslink_app'@'localhost' IDENTIFIED BY 'strong_password';

-- 授予权限
GRANT SELECT, INSERT, UPDATE, DELETE ON campuslink.* TO 'campuslink_app'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;
```

### 3. 备份策略
- 每天自动备份数据库
- 保留至少7天的备份
- 定期测试备份恢复

---

## 🛠️ 常用维护命令

### 查看数据库大小
```sql
SELECT 
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'campuslink'
GROUP BY table_schema;
```

### 查看各表数据量
```sql
SELECT 
    table_name AS 'Table',
    table_rows AS 'Rows',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.tables
WHERE table_schema = 'campuslink'
ORDER BY (data_length + index_length) DESC;
```

### 优化所有表
```sql
USE campuslink;
OPTIMIZE TABLE user, resource, question, answer, task, message, club, activity;
```

### 备份数据库
```bash
mysqldump -u root -p campuslink > campuslink_backup_$(date +%Y%m%d).sql
```

### 恢复数据库
```bash
mysql -u root -p campuslink < campuslink_backup_20240101.sql
```

---

## 🐛 常见问题

### Q1: 执行脚本时报错 "Unknown database 'campuslink'"
**A**: 确保先执行 `schema.sql` 创建数据库，再执行 `init-data.sql`

### Q2: 外键约束错误
**A**: 检查MySQL是否启用了外键支持（InnoDB引擎），确保按顺序执行脚本

### Q3: 字符集问题导致中文乱码
**A**: 确保MySQL配置文件（my.cnf/my.ini）中设置了正确的字符集：
```ini
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci

[client]
default-character-set=utf8mb4
```

### Q4: 时区问题
**A**: 在连接字符串中指定时区：
```
jdbc:mysql://localhost:3306/campuslink?serverTimezone=Asia/Shanghai
```

---

## 📚 相关文档

- [数据库设计文档](../docs/database-design.md) - 详细的表结构设计说明
- [API接口文档](../docs/api-design.md) - RESTful API接口定义
- [第三方服务配置](../docs/third-party-services.md) - 第三方服务申请指南

---

## 📞 技术支持

如有问题，请查看项目文档或提交Issue。

**最后更新**: 2024-01-01  
**版本**: v1.0

