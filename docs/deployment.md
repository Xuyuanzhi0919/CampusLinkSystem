# CampusLink 部署文档

本文档详细说明如何将 CampusLink 部署到生产环境。

---

## 📋 目录

- [环境准备](#环境准备)
- [数据库部署](#数据库部署)
- [Redis 部署](#redis-部署)
- [后端部署](#后端部署)
- [前端部署](#前端部署)
- [Nginx 配置](#nginx-配置)
- [Docker 部署](#docker-部署)
- [CI/CD 配置](#cicd-配置)
- [监控与日志](#监控与日志)

---

## 🖥 环境准备

### 服务器要求

**最低配置**（测试环境）：
- CPU: 2核
- 内存: 4GB
- 硬盘: 50GB
- 带宽: 3Mbps

**推荐配置**（生产环境）：
- CPU: 4核+
- 内存: 8GB+
- 硬盘: 100GB+ (SSD)
- 带宽: 10Mbps+

### 软件要求

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| 操作系统 | Ubuntu 20.04+ / CentOS 8+ | 推荐 Ubuntu 22.04 |
| JDK | 17+ | OpenJDK 或 Oracle JDK |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.0+ | 缓存 |
| Nginx | 1.20+ | 反向代理 |
| Node.js | 16+ | 前端构建（生产环境可不安装） |

---

## 🗄 数据库部署

### 1. 安装 MySQL

#### Ubuntu

```bash
# 更新包列表
sudo apt update

# 安装 MySQL
sudo apt install mysql-server -y

# 启动 MySQL 服务
sudo systemctl start mysql
sudo systemctl enable mysql

# 安全配置
sudo mysql_secure_installation
```

#### CentOS

```bash
# 安装 MySQL YUM 仓库
sudo yum install mysql-server -y

# 启动 MySQL
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 查看临时密码
sudo grep 'temporary password' /var/log/mysqld.log

# 安全配置
sudo mysql_secure_installation
```

### 2. 创建数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE campuslink CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 创建用户（可选）
CREATE USER 'campuslink'@'localhost' IDENTIFIED BY 'strong_password';
GRANT ALL PRIVILEGES ON campuslink.* TO 'campuslink'@'localhost';
FLUSH PRIVILEGES;

# 退出
EXIT;
```

### 3. 导入数据库结构

```bash
# 上传 SQL 文件到服务器
scp sql/schema.sql user@server:/tmp/

# 导入表结构
mysql -u root -p campuslink < /tmp/schema.sql

# 导入初始数据（可选）
mysql -u root -p campuslink < /tmp/init-data.sql
```

### 4. MySQL 优化配置

编辑 `/etc/mysql/mysql.conf.d/mysqld.cnf`（Ubuntu）或 `/etc/my.cnf`（CentOS）：

```ini
[mysqld]
# 基础配置
max_connections = 500
max_allowed_packet = 64M
wait_timeout = 600
interactive_timeout = 600

# 字符集
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# InnoDB 配置
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
innodb_flush_method = O_DIRECT

# 慢查询日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/mysql-slow.log
long_query_time = 2

# 二进制日志（用于主从复制）
log_bin = /var/log/mysql/mysql-bin.log
expire_logs_days = 7
max_binlog_size = 100M
```

重启 MySQL：

```bash
sudo systemctl restart mysql
```

---

## 📦 Redis 部署

### 1. 安装 Redis

#### Ubuntu

```bash
sudo apt update
sudo apt install redis-server -y
```

#### CentOS

```bash
sudo yum install redis -y
```

### 2. 配置 Redis

编辑 `/etc/redis/redis.conf`：

```conf
# 绑定地址（如果是同一服务器，使用 127.0.0.1；如果是分布式，使用内网 IP）
bind 127.0.0.1

# 端口
port 6379

# 密码（强烈推荐设置）
requirepass your_strong_redis_password

# 持久化
save 900 1
save 300 10
save 60 10000

appendonly yes
appendfsync everysec

# 最大内存（根据服务器配置调整）
maxmemory 2gb
maxmemory-policy allkeys-lru

# 日志
loglevel notice
logfile /var/log/redis/redis-server.log
```

### 3. 启动 Redis

```bash
sudo systemctl start redis
sudo systemctl enable redis

# 测试连接
redis-cli -a your_strong_redis_password ping
# 应该返回 PONG
```

---

## ☕ 后端部署

### 方式1：JAR 包部署（推荐）

#### 1. 本地构建

```bash
cd backend

# 修改配置文件 src/main/resources/application-prod.yml
# 配置数据库、Redis、OSS 等信息

# 打包
mvn clean package -DskipTests -P prod

# 生成的 JAR 文件位于 target/campuslink-0.0.1-SNAPSHOT.jar
```

#### 2. 上传到服务器

```bash
# 上传 JAR 文件
scp target/campuslink-0.0.1-SNAPSHOT.jar user@server:/opt/campuslink/

# 上传配置文件
scp src/main/resources/application-prod.yml user@server:/opt/campuslink/config/
```

#### 3. 创建启动脚本

创建 `/opt/campuslink/start.sh`：

```bash
#!/bin/bash

JAVA_OPTS="-Xms1024m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
APP_JAR="campuslink-0.0.1-SNAPSHOT.jar"
CONFIG_DIR="config"

nohup java $JAVA_OPTS \
  -Dspring.profiles.active=prod \
  -Dspring.config.location=$CONFIG_DIR/application-prod.yml \
  -jar $APP_JAR \
  > logs/app.log 2>&1 &

echo $! > app.pid
echo "CampusLink 后端已启动，PID: $(cat app.pid)"
```

创建停止脚本 `/opt/campuslink/stop.sh`：

```bash
#!/bin/bash

if [ -f app.pid ]; then
  PID=$(cat app.pid)
  kill $PID
  rm app.pid
  echo "CampusLink 后端已停止"
else
  echo "找不到 PID 文件"
fi
```

设置权限：

```bash
chmod +x /opt/campuslink/start.sh
chmod +x /opt/campuslink/stop.sh
```

#### 4. 启动服务

```bash
cd /opt/campuslink
./start.sh

# 查看日志
tail -f logs/app.log
```

### 方式2：Systemd 服务（更推荐）

创建服务文件 `/etc/systemd/system/campuslink.service`：

```ini
[Unit]
Description=CampusLink Backend Service
After=mysql.service redis.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/campuslink
Environment="JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64"
ExecStart=/usr/bin/java -Xms1024m -Xmx2048m -XX:+UseG1GC \
  -Dspring.profiles.active=prod \
  -Dspring.config.location=/opt/campuslink/config/application-prod.yml \
  -jar /opt/campuslink/campuslink-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
# 重新加载 systemd 配置
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start campuslink

# 设置开机自启
sudo systemctl enable campuslink

# 查看状态
sudo systemctl status campuslink

# 查看日志
sudo journalctl -u campuslink -f
```

---

## 🎨 前端部署

### 1. 本地构建

#### H5 版本

```bash
cd frontend

# 修改生产环境配置 src/config.ts
# 修改 baseURL 和 WS_URL 为生产环境地址

# 构建
npm run build:h5

# 生成的静态文件位于 dist/build/h5/
```

#### 微信小程序版本

```bash
npm run build:mp-weixin

# 生成的文件位于 dist/build/mp-weixin/
# 使用微信开发者工具上传发布
```

### 2. 上传到服务器

```bash
# 压缩构建产物
cd dist/build/h5
tar -czf h5.tar.gz *

# 上传到服务器
scp h5.tar.gz user@server:/var/www/campuslink/

# 解压
ssh user@server
cd /var/www/campuslink
tar -xzf h5.tar.gz
rm h5.tar.gz
```

---

## ⚙️ Nginx 配置

### 1. 安装 Nginx

```bash
# Ubuntu
sudo apt install nginx -y

# CentOS
sudo yum install nginx -y

# 启动 Nginx
sudo systemctl start nginx
sudo systemctl enable nginx
```

### 2. 配置站点

创建配置文件 `/etc/nginx/sites-available/campuslink`（Ubuntu）或 `/etc/nginx/conf.d/campuslink.conf`（CentOS）：

```nginx
# HTTP -> HTTPS 重定向
server {
    listen 80;
    server_name campuslink.com www.campuslink.com;
    return 301 https://$server_name$request_uri;
}

# HTTPS 主站
server {
    listen 443 ssl http2;
    server_name campuslink.com www.campuslink.com;

    # SSL 证书配置
    ssl_certificate /etc/nginx/ssl/campuslink.com.crt;
    ssl_certificate_key /etc/nginx/ssl/campuslink.com.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # 前端静态文件
    root /var/www/campuslink;
    index index.html;

    # Gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
    gzip_min_length 1000;

    # 前端路由（SPA）
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 超时配置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # WebSocket 代理
    location /ws/ {
        proxy_pass http://localhost:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

        # WebSocket 超时配置
        proxy_connect_timeout 7d;
        proxy_send_timeout 7d;
        proxy_read_timeout 7d;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
}
```

### 3. 启用配置并重启

```bash
# Ubuntu（创建软链接）
sudo ln -s /etc/nginx/sites-available/campuslink /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx
```

### 4. 申请 SSL 证书（Let's Encrypt）

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx -y

# 自动申请并配置证书
sudo certbot --nginx -d campuslink.com -d www.campuslink.com

# 自动续期（Certbot 会自动设置 cron 任务）
```

---

## 🐳 Docker 部署

### 1. 创建 Dockerfile

#### 后端 Dockerfile

创建 `backend/Dockerfile`：

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# 复制 JAR 文件
COPY target/campuslink-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-Xms1024m", "-Xmx2048m", "-XX:+UseG1GC", \
  "-Dspring.profiles.active=prod", "-jar", "app.jar"]
```

#### 前端 Dockerfile

创建 `frontend/Dockerfile`：

```dockerfile
# 构建阶段
FROM node:16-alpine as builder

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build:h5

# 生产阶段
FROM nginx:alpine

COPY --from=builder /app/dist/build/h5 /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### 2. 创建 docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: campuslink-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: campuslink
      MYSQL_USER: campuslink
      MYSQL_PASSWORD: campuslink_password
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/schema.sql:/docker-entrypoint-initdb.d/1-schema.sql
      - ./sql/init-data.sql:/docker-entrypoint-initdb.d/2-init-data.sql
    ports:
      - "3306:3306"
    networks:
      - campuslink-network

  redis:
    image: redis:7-alpine
    container_name: campuslink-redis
    restart: always
    command: redis-server --requirepass redis_password
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    networks:
      - campuslink-network

  backend:
    build: ./backend
    container_name: campuslink-backend
    restart: always
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/campuslink
      SPRING_DATASOURCE_USERNAME: campuslink
      SPRING_DATASOURCE_PASSWORD: campuslink_password
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PASSWORD: redis_password
    ports:
      - "8080:8080"
    networks:
      - campuslink-network

  frontend:
    build: ./frontend
    container_name: campuslink-frontend
    restart: always
    ports:
      - "80:80"
    networks:
      - campuslink-network

volumes:
  mysql_data:
  redis_data:

networks:
  campuslink-network:
    driver: bridge
```

### 3. 启动服务

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

---

## 🚀 CI/CD 配置

### GitHub Actions

创建 `.github/workflows/deploy.yml`：

```yaml
name: Deploy CampusLink

on:
  push:
    branches: [ main ]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Build Backend
      run: |
        cd backend
        mvn clean package -DskipTests -P prod

    - name: Set up Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '16'

    - name: Build Frontend
      run: |
        cd frontend
        npm install
        npm run build:h5

    - name: Deploy to Server
      uses: appleboy/scp-action@master
      with:
        host: ${{ secrets.SERVER_HOST }}
        username: ${{ secrets.SERVER_USER }}
        key: ${{ secrets.SERVER_SSH_KEY }}
        source: "backend/target/*.jar,frontend/dist/build/h5/*"
        target: "/opt/campuslink"

    - name: Restart Services
      uses: appleboy/ssh-action@master
      with:
        host: ${{ secrets.SERVER_HOST }}
        username: ${{ secrets.SERVER_USER }}
        key: ${{ secrets.SERVER_SSH_KEY }}
        script: |
          sudo systemctl restart campuslink
          sudo systemctl restart nginx
```

---

## 📊 监控与日志

### 1. 应用日志

后端日志配置（`logback-spring.xml`）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/opt/campuslink/logs/app.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/opt/campuslink/logs/app.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

### 2. Nginx 日志

查看访问日志：

```bash
tail -f /var/log/nginx/access.log
```

查看错误日志：

```bash
tail -f /var/log/nginx/error.log
```

### 3. 系统监控

安装监控工具：

```bash
# 安装 htop（进程监控）
sudo apt install htop -y

# 安装 iotop（磁盘 I/O 监控）
sudo apt install iotop -y

# 安装 nethogs（网络流量监控）
sudo apt install nethogs -y
```

---

## ✅ 部署检查清单

部署完成后，请确认以下事项：

- [ ] 数据库连接正常
- [ ] Redis 连接正常
- [ ] 后端服务启动成功（`http://server-ip:8080/actuator/health`）
- [ ] 前端页面可访问
- [ ] API 接口正常（Swagger: `http://server-ip:8080/doc.html`）
- [ ] WebSocket 连接正常
- [ ] SSL 证书配置正确
- [ ] Nginx 反向代理正常
- [ ] 文件上传功能正常（OSS）
- [ ] 定时任务正常运行
- [ ] 日志记录正常
- [ ] 监控告警配置完成

---

## 🔧 常见问题

### Q1: 后端启动失败

**排查步骤**：
1. 检查日志：`tail -f /opt/campuslink/logs/app.log`
2. 检查端口占用：`sudo lsof -i:8080`
3. 检查数据库连接：`mysql -u campuslink -p campuslink`
4. 检查 Redis 连接：`redis-cli -a password ping`

### Q2: 前端页面空白

**排查步骤**：
1. 检查 Nginx 配置：`sudo nginx -t`
2. 检查静态文件路径：`ls /var/www/campuslink`
3. 查看浏览器控制台错误
4. 检查 API 请求是否成功

### Q3: WebSocket 连接失败

**排查步骤**：
1. 检查 Nginx WebSocket 代理配置
2. 检查防火墙规则
3. 检查 SSL 证书（wss 协议需要 HTTPS）
4. 查看浏览器控制台 WebSocket 错误

---

## 📝 总结

完成以上步骤后，CampusLink 应该已经成功部署到生产环境。建议：

1. **定期备份**：每天自动备份数据库和上传文件
2. **监控告警**：配置服务器监控和应用监控
3. **日志分析**：定期查看日志，发现潜在问题
4. **性能优化**：根据访问量调整服务器配置
5. **安全加固**：定期更新系统和软件，修复安全漏洞

如有问题，请查阅项目文档或联系技术支持。
