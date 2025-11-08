# CampusLink 第三方服务配置文档

## 一、概述

本文档列出了CampusLink项目开发和运行所需的所有第三方服务账号、配置说明和申请流程。

### 1.1 服务总览速查表
| 服务 | 主要用途 | 优先级 | 当前状态 | 负责人/备注 |
|------|----------|--------|----------|-------------|
| 微信小程序 | 多端流量入口及账号体系 | 高 | 待申请或迁移 | 需企业主体与类目审核 |
| 阿里云OSS | 资料/图片/日志对象存储 | 高 | 已规划，待开通Bucket | 建议启用私有+STS临时凭证 |
| MySQL数据库 | 核心业务数据 | 高 | 可先自建，计划迁移RDS | 生产需主从+备份 |
| Redis缓存 | Session、热点、锁 | 高 | 可自建，后续托管 | 与MySQL位于同一VPC |
| OpenAI/国内大模型 | AI答复、推荐 | 中 | 取决于合规策略 | 需流量/费用预估与兜底模型 |
| Elasticsearch | 全文搜索 | 中 | 数据量达到阈值后启用 | 先准备索引模板 |
| 短信服务 | 登录验证码/通知 | 中 | 申请签名模板中 | 需双供应商冗余 |
| 邮件服务 | 低频通知 | 低 | 可使用SMTP备用 | 注意发送频率限制 |
| 云服务器(ECS) | 部署后端/中间件 | 高 | 已购开发机，生产待扩容 | 建议按环境隔离 |
| 域名&SSL | 统一域名/HTTPS | 高 | 域名待备案，证书可自动化 | 证书续期需自动脚本 |

> 使用建议：立项周会按照表格状态更新，方便跨团队同步依赖项；负责人列如暂未指定，可用“待指派”标记。

---

## 二、必需服务（高优先级）

### 1. 微信小程序

#### 服务说明
用于小程序端的开发、发布和运行。

#### 申请流程
1. 访问：https://mp.weixin.qq.com/
2. 注册类型：企业/组织（需要营业执照或组织机构代码证）
3. 认证费用：300元/年（可选，但建议认证以获得更多权限）

#### 所需信息
- AppID（小程序ID）
- AppSecret（小程序密钥）
- 服务器域名配置（需要备案域名）

#### 配置项
```yaml
# application.yml
wechat:
  miniprogram:
    appid: wx1234567890abcdef
    secret: your_app_secret
    token: your_token
    aesKey: your_aes_key
```

#### 权限配置
- 用户信息：获取用户昵称、头像
- 消息推送：订阅消息模板
- 支付功能：微信支付（如需积分充值）

#### 注意事项
- 服务器域名必须备案
- 需要配置业务域名、服务器域名、下载域名
- 小程序类目需要与实际功能匹配

#### 联调与发布建议
- 在「成员管理」中添加至少2个开发者、5个体验者，确保多角色可随时验证功能。
- 规范化上传流程：开发版 → 体验版 → 提审 → 正式版，记录每次提交的版本号与变更说明，便于回滚。
- 关注「小程序违规处理」公告，涉及社交/积分功能需准备运营规则、用户协议与隐私政策截图。

---

### 2. 阿里云OSS（对象存储）

#### 服务说明
用于存储用户上传的文件（课程资料、图片、头像等）。

#### 申请流程
1. 访问：https://www.aliyun.com/
2. 注册阿里云账号
3. 实名认证（个人或企业）
4. 开通OSS服务

#### 费用估算
- 存储费用：0.12元/GB/月（标准存储）
- 流量费用：0.5元/GB（外网流出流量）
- 请求费用：0.01元/万次
- **预估**：初期100GB存储 + 100GB流量 ≈ 62元/月

#### 配置项
```yaml
# application.yml
aliyun:
  oss:
    endpoint: oss-cn-beijing.aliyuncs.com
    accessKeyId: your_access_key_id
    accessKeySecret: your_access_key_secret
    bucketName: campuslink-resources
    urlPrefix: https://campuslink-resources.oss-cn-beijing.aliyuncs.com
```

#### Bucket配置
- Bucket名称：campuslink-resources
- 区域：根据用户分布选择（如华北2-北京）
- 读写权限：私有（通过签名URL访问）
- 跨域设置：允许前端直传

#### 安全配置
- 开启防盗链
- 配置Referer白名单
- 设置访问控制策略（RAM）
- 开启服务端加密
- 对前端直传场景使用STS临时凭证（有效期不超过1小时），避免泄露主AK/SK
- 配置生命周期规则：原始上传文件30天后转冷存储，临时缓存文件7天删除
- 若需要大流量下载，建议绑定CDN并启用IP限频，降低OSS原站压力

---

### 3. MySQL数据库

#### 服务说明
主数据库，存储所有业务数据。

#### 部署方式

**方式一：云数据库（推荐）**
- 阿里云RDS：https://www.aliyun.com/product/rds/mysql
- 腾讯云MySQL：https://cloud.tencent.com/product/cdb
- 费用：约200-500元/月（2核4G配置）

**方式二：自建数据库**
- 购买云服务器（ECS）
- 自行安装MySQL 8.0
- 费用：约100-300元/月（服务器费用）

#### 配置要求
- 版本：MySQL 8.0+
- 配置：最低2核4G，推荐4核8G
- 存储：SSD，最低50GB
- 字符集：utf8mb4

#### 配置项
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campuslink?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

#### 备份策略
- 自动备份：每天凌晨2:00
- 保留时间：7天
- 异地备份：每周一次
- 启用binlog并保存至少7天，便于按时间点恢复
- 每季度做一次「备份→恢复」演练，验证脚本可靠性并记录SOP

---

### 4. Redis缓存

#### 服务说明
用于缓存热点数据、Session存储、分布式锁等。

#### 部署方式

**方式一：云Redis（推荐）**
- 阿里云Redis：https://www.aliyun.com/product/kvstore
- 腾讯云Redis：https://cloud.tencent.com/product/crs
- 费用：约100-300元/月（1GB标准版）

**方式二：自建Redis**
- 在云服务器上安装Redis
- 费用：包含在服务器费用中

#### 配置要求
- 版本：Redis 6.0+
- 内存：最低1GB，推荐2GB
- 持久化：开启AOF

#### 配置项
```yaml
# application.yml
spring:
  redis:
    host: localhost
    port: 6379
    password: your_password
    database: 0
    timeout: 3000
    lettuce:
      pool:
        max-active: 8
        max-wait: -1
        max-idle: 8
        min-idle: 0
```

#### 运维建议
- 生产环境开启哨兵或主从集群，确保单点故障可自动切换。
- 定义「冷热Key」清单，高风险Key设置合理的过期时间并用监控脚本观察命中率。
- 对分布式锁、队列等关键功能编写压测脚本，避免在持久化阻塞时放大雪崩。

---

### 5. OpenAI API（GPT服务）

#### 服务说明
用于AI智能答复、内容推荐等功能。

#### 申请流程
1. 访问：https://platform.openai.com/
2. 注册账号（需要国外手机号或虚拟号）
3. 绑定支付方式（信用卡）
4. 获取API Key

#### 费用估算
- GPT-3.5-turbo：$0.002/1K tokens
- GPT-4：$0.03/1K tokens（输入）
- **预估**：使用GPT-3.5，每月1000次调用 ≈ $5-10

#### 替代方案
如果无法使用OpenAI，可以考虑：
- **国内替代**：
  - 文心一言（百度）：https://yiyan.baidu.com/
  - 通义千问（阿里）：https://tongyi.aliyun.com/
  - 讯飞星火：https://xinghuo.xfyun.cn/
- **开源模型**：
  - ChatGLM：https://github.com/THUDM/ChatGLM-6B
  - LLaMA：需要自己部署

#### 配置项
```yaml
# application.yml (FastAPI服务)
openai:
  api_key: sk-xxxxxxxxxxxxxxxxxxxxxxxx
  model: gpt-3.5-turbo
  max_tokens: 500
  temperature: 0.7
```

#### 接入与兜底策略
- 统一封装AI服务网关，支持OpenAI/国内模型按策略切换，并对外暴露一致的响应结构。
- 记录每次调用的tokens与耗时，写入Prometheus/日志，方便计算真实成本。
- 设置超时（如8s）和最多2次重试，超限后立即回退到缓存答案或提示语，避免用户等待过久。
- 若业务对数据出境敏感，应在国内模型上启用专有网络并完成合规评估。

---

## 三、可选服务（中优先级）

### 6. Elasticsearch

#### 服务说明
用于全文搜索（问题、资源搜索）。

#### 部署方式

**方式一：云Elasticsearch**
- 阿里云Elasticsearch：https://www.aliyun.com/product/elasticsearch
- 费用：约300-600元/月（2核4G）

**方式二：自建Elasticsearch**
- 在云服务器上安装
- 费用：包含在服务器费用中

#### 配置要求
- 版本：Elasticsearch 8.x
- 配置：最低2核4G
- 存储：SSD，最低50GB

#### 配置项
```yaml
# application.yml
spring:
  elasticsearch:
    uris: http://localhost:9200
    username: elastic
    password: your_password
```

#### 注意事项
- 初期可以不使用，用MySQL的FULLTEXT索引代替
- 数据量大时再迁移到Elasticsearch
- 提前规划索引模板（mapping)、分析器（如IK、pinyin）以及分词同义词库，避免上线后调整成本。
- 结合ILM策略将历史索引转至低成本节点或按月删除，控制磁盘占用。
- 安全层面至少开启HTTP Basic Auth或VPC可见性，并限制只读用户角色。

---

### 7. 短信服务

#### 服务说明
用于发送验证码、通知短信。

#### 服务商选择

**阿里云短信**
- 官网：https://www.aliyun.com/product/sms
- 费用：0.045元/条（验证码短信）
- 预估：每月1000条 ≈ 45元

**腾讯云短信**
- 官网：https://cloud.tencent.com/product/sms
- 费用：0.045元/条
- 预估：每月1000条 ≈ 45元

#### 申请流程
1. 注册云服务商账号
2. 实名认证
3. 申请短信签名（如：【校园互助】）
4. 申请短信模板（如：验证码{code}，{time}分钟内有效）
5. 等待审核（1-3个工作日）

#### 配置项
```yaml
# application.yml
aliyun:
  sms:
    accessKeyId: your_access_key_id
    accessKeySecret: your_access_key_secret
    signName: 校园互助
    templateCode: SMS_123456789
```

#### 稳定性建议
- 同时接入两家短信供应商，网关层实现自动重试/切换，降低单点风险。
- 建立发送监控，统计成功率、延迟与失败原因码（如isv.BUSINESS_LIMIT_CONTROL），异常时及时告警。
- 对验证码模板设置防刷逻辑：同手机号/同IP在10分钟内最多发送N次。

---

### 8. 邮件服务

#### 服务说明
用于发送邮箱验证码、系统通知邮件。

#### 服务商选择

**方式一：SMTP服务**
- QQ邮箱：免费，每天限额500封
- 网易邮箱：免费，每天限额200封
- Gmail：免费，需要科学上网

**方式二：专业邮件服务**
- SendGrid：https://sendgrid.com/（免费额度：100封/天）
- 阿里云邮件推送：https://www.aliyun.com/product/directmail
- 腾讯云邮件推送：https://cloud.tencent.com/product/ses

#### 配置项
```yaml
# application.yml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your_email@qq.com
    password: your_smtp_password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

#### 投递建议
- 配置SPF/DKIM/DMARC记录，提升到达率并防止被标记为垃圾邮件。
- 运营侧维护退订列表或黑名单，尊重用户意愿并符合《个人信息保护法》。
- 低频运营邮件建议在专业服务（SendGrid、SES）发送，验证码类邮件可留在SMTP方案。

---

### 9. 云服务器（ECS）

#### 服务说明
用于部署后端服务、数据库等。

#### 配置推荐

**开发/测试环境**
- CPU：2核
- 内存：4GB
- 硬盘：40GB SSD
- 带宽：1-3Mbps
- 费用：约100-200元/月

**生产环境**
- CPU：4核
- 内存：8GB
- 硬盘：100GB SSD
- 带宽：5-10Mbps
- 费用：约300-600元/月

#### 服务商选择
- 阿里云ECS：https://www.aliyun.com/product/ecs
- 腾讯云CVM：https://cloud.tencent.com/product/cvm
- 华为云ECS：https://www.huaweicloud.com/product/ecs.html

#### 系统选择
- 推荐：Ubuntu 22.04 LTS 或 CentOS 8
- 统一使用自定义镜像（含基础依赖、监控Agent），便于快速扩容和灾备。
- 使用Terraform/Ansible等IaC工具定义实例、VPC、安全组，确保环境可重复创建。
- 所有ECS纳入统一的日志、监控、补丁管理流程，避免孤岛服务器。

---

### 10. 域名与SSL证书

#### 域名申请
- 阿里云万网：https://wanwang.aliyun.com/
- 腾讯云域名：https://dnspod.cloud.tencent.com/
- 费用：.com域名约55元/年，.cn域名约29元/年

#### 域名备案
- 必须备案才能在国内服务器使用
- 备案时间：15-20个工作日
- 费用：免费

#### SSL证书
- 免费证书：Let's Encrypt（3个月有效，可自动续期）
- 付费证书：阿里云、腾讯云（约1000元/年）
- 推荐：使用免费证书 + 自动续期
- 生产环境务必开启HTTPS全站强制跳转、HSTS及中间人防护。
- 若有多端子域名，可使用通配符证书或ACME自动申请脚本集中管理。

---

## 四、开发工具服务（低优先级）

### 11. Git代码仓库

**选择一：GitHub**
- 官网：https://github.com/
- 费用：免费（私有仓库也免费）
- 优点：全球最大代码托管平台
- 缺点：国内访问较慢

**选择二：Gitee（码云）**
- 官网：https://gitee.com/
- 费用：免费（私有仓库5个）
- 优点：国内访问快
- 缺点：功能相对较少

**选择三：GitLab**
- 官网：https://gitlab.com/
- 费用：免费
- 优点：功能强大，可自建
- 缺点：国内访问较慢

---

### 12. CI/CD服务

**Jenkins**
- 自建CI/CD平台
- 费用：免费（需要服务器）

**GitHub Actions**
- GitHub自带CI/CD
- 费用：公开仓库免费，私有仓库每月2000分钟免费

**GitLab CI**
- GitLab自带CI/CD
- 费用：免费

---

### 13. 监控与日志

**Sentry（错误监控）**
- 官网：https://sentry.io/
- 费用：免费额度5000 events/月
- 用途：前端和后端错误监控

**阿里云日志服务（SLS）**
- 官网：https://www.aliyun.com/product/sls
- 费用：按量计费，约50-200元/月
- 用途：日志收集、分析、告警

---

## 五、配置文件模板

### application.yml（Spring Boot）
```yaml
server:
  port: 8080

spring:
  application:
    name: campuslink-backend
  
  datasource:
    url: jdbc:mysql://localhost:3306/campuslink?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
  
  mail:
    host: smtp.qq.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}

# 微信小程序配置
wechat:
  miniprogram:
    appid: ${WECHAT_APPID}
    secret: ${WECHAT_SECRET}

# 阿里云OSS配置
aliyun:
  oss:
    endpoint: ${OSS_ENDPOINT}
    accessKeyId: ${OSS_ACCESS_KEY_ID}
    accessKeySecret: ${OSS_ACCESS_KEY_SECRET}
    bucketName: ${OSS_BUCKET_NAME}
  
  sms:
    accessKeyId: ${SMS_ACCESS_KEY_ID}
    accessKeySecret: ${SMS_ACCESS_KEY_SECRET}
    signName: ${SMS_SIGN_NAME}
    templateCode: ${SMS_TEMPLATE_CODE}

# JWT配置
jwt:
  secret: ${JWT_SECRET:your-secret-key-change-in-production}
  expiration: 7200
  refreshExpiration: 604800
```

### .env（FastAPI AI服务）
```env
# OpenAI配置
OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxx
OPENAI_MODEL=gpt-3.5-turbo
OPENAI_MAX_TOKENS=500
OPENAI_TEMPERATURE=0.7

# 后端服务地址
BACKEND_API_URL=http://localhost:8080/api/v1

# 服务端口
PORT=8000
```

---

## 六、费用总结

### 最小可行方案（MVP）
| 服务 | 费用 | 说明 |
|------|------|------|
| 云服务器（2核4G） | 150元/月 | 部署所有服务 |
| 域名 | 55元/年 | .com域名 |
| 域名备案 | 免费 | - |
| SSL证书 | 免费 | Let's Encrypt |
| 阿里云OSS | 50元/月 | 50GB存储+流量 |
| 短信服务 | 50元/月 | 约1000条 |
| OpenAI API | 50元/月 | GPT-3.5 |
| **月度总计** | **约350元/月** | - |
| **年度总计** | **约4255元/年** | - |

### 推荐方案（生产环境）
| 服务 | 费用 | 说明 |
|------|------|------|
| 云服务器（4核8G） | 400元/月 | 后端服务 |
| 云数据库MySQL | 300元/月 | RDS |
| 云Redis | 150元/月 | 2GB |
| 域名 | 55元/年 | .com域名 |
| SSL证书 | 免费 | Let's Encrypt |
| 阿里云OSS | 100元/月 | 100GB存储+流量 |
| Elasticsearch | 300元/月 | 搜索服务 |
| 短信服务 | 100元/月 | 约2000条 |
| OpenAI API | 100元/月 | GPT-3.5 |
| 日志服务 | 100元/月 | SLS |
| **月度总计** | **约1550元/月** | - |
| **年度总计** | **约18655元/年** | - |

---

## 七、申请优先级与时间规划

### 第一周（立即申请）
- [x] 云服务器（用于开发测试）
- [x] 域名注册
- [x] 域名备案（提交申请）
- [x] 阿里云OSS
- [x] MySQL数据库（可先用服务器自建）
- [x] Redis（可先用服务器自建）

### 第二周
- [ ] 微信小程序账号（需要营业执照或组织代码）
- [ ] 短信服务
- [ ] 邮件服务
- [ ] OpenAI API（或国内替代）

### 第三周
- [ ] SSL证书配置
- [ ] Elasticsearch（可选）
- [ ] 监控服务（可选）

---

## 八、注意事项

1. **环境变量管理**：敏感信息（密钥、密码）不要写入代码，使用环境变量
2. **密钥安全**：定期更换密钥，不要泄露到公开仓库
3. **成本控制**：初期使用最小配置，根据实际需求扩容
4. **备份策略**：重要数据必须备份，建议异地备份
5. **监控告警**：配置服务器监控和告警，及时发现问题

---

## 九、联系方式与技术支持

### 阿里云
- 客服电话：95187
- 工单系统：https://workorder.console.aliyun.com/

### 腾讯云
- 客服电话：95716
- 工单系统：https://console.cloud.tencent.com/workorder

### 微信开放平台
- 客服：https://developers.weixin.qq.com/community/pay

---

## 十、快速开始检查清单

- [ ] 云服务器已购买并配置
- [ ] 域名已注册
- [ ] 域名备案已提交
- [ ] MySQL数据库已安装
- [ ] Redis已安装
- [ ] 阿里云OSS已开通
- [ ] 微信小程序账号已注册
- [ ] 短信服务已开通
- [ ] OpenAI API Key已获取
- [ ] SSL证书已配置
- [ ] 所有配置项已填入配置文件
- [ ] 环境变量已设置
- [ ] 数据库已初始化
- [ ] 服务已部署并测试

---

## 十一、环境与配置策略

### 11.1 多环境隔离
- **开发/测试**：共享低成本ECS与数据库实例，但使用独立的数据库schema、Redis DB与OSS目录，避免脏数据污染。
- **预发布**：与生产一致的VPC与安全组策略，可复现场景、联调支付/短信等真实第三方服务。
- **生产**：独立账号或至少独立VPC，所有公网访问通过负载均衡+WAF，禁止直接暴露数据库/缓存端口。

### 11.2 配置与密钥管理
- 敏感配置统一存放在配置中心（如Nacos、AWS Secrets Manager）或加密后的`.env.enc`，通过CI/CD解密注入。
- 约定密钥轮换周期（建议90天），并在文档内登记负责人与下一次轮换日期。
- 本地开发使用`.env.example`对齐字段，防止因缺少变量导致启动失败。

### 11.3 访问控制与审计
- 所有云账号开启MFA，运维操作需通过RAM子账号完成，禁止共享主账号。
- 记录重要操作（如删库、修改安全组、关闭实例）到审计日志，并定期回顾。
- 建立“离职/角色变更”流程，3个工作日内回收其访问云资源、Git仓库及第三方后台的权限。

---

## 十二、成本监控与扩缩容

### 12.1 成本基线
- 参照第六章费用表，为每项服务设定「预算上限」与「预警阈值」（如80%预算），写入财务共享文档。
- 对按量计费服务（OSS流量、短信、OpenAI tokens）记录每日/每周用量，便于评估趋势。

### 12.2 监控与告警
- 启用云厂商账单告警、OSS流量告警、短信发送量告警；同时在自研监控系统展示关键费用指标。
- 当费用异常上涨时，先检查是否存在滥用（爬虫、刷短信、AI接口被攻击），再考虑扩容。

### 12.3 扩缩容与优化
- 通过Terraform或云厂商弹性伸缩策略预设扩容模板，确保峰值时可在10分钟内加机器。
- 对OSS、日志、数据库开启生命周期或冷热分层，减少长期存储费用。
- 为OpenAI/大模型调用引入缓存与分级模型（如先尝试小模型，不足再调用大模型），降低单位请求成本。

---

**文档版本**：v1.0  
**最后更新**：2024-01-01  
**维护人员**：开发团队
