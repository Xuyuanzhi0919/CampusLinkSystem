<template>
  <view class="terms-page">
    <!-- 顶部导航栏 -->
    <view class="page-header">
      <view class="back-btn" @click="handleBack">
        <svg viewBox="0 0 24 24" fill="none" class="back-icon">
          <path d="M19 12H5M5 12L12 19M5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </view>
      <text class="page-title">用户协议</text>
    </view>

    <!-- 文档内容 -->
    <view class="content-wrapper">
      <!-- 文档头部 -->
      <view class="doc-header">
        <text class="doc-title">CampusLink 用户服务协议</text>
        <text class="doc-subtitle">欢迎使用 CampusLink 校园资源互助平台</text>
        <view class="doc-meta">
          <text class="meta-item">生效日期：2024年1月1日</text>
          <text class="meta-item">最后更新：2024年11月15日</text>
        </view>
      </view>

      <!-- 前言 -->
      <view class="preamble-section">
        <view class="note-box note-box--info">
          <view class="note-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M12 8V12M12 16H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </view>
          <view class="note-content">
            <text class="note-title">协议说明</text>
            <text class="note-text">请您仔细阅读本协议。使用 CampusLink 服务即表示您同意遵守本协议的所有条款。如您不同意本协议，请停止使用我们的服务。</text>
          </view>
        </view>
      </view>

      <!-- 目录导航 -->
      <view class="toc-section">
        <text class="toc-title">目录</text>
        <view class="toc-list">
          <view class="toc-item" @click="scrollToSection('section-1')">
            <text class="toc-number">1</text>
            <text class="toc-text">服务说明</text>
          </view>
          <view class="toc-item" @click="scrollToSection('section-2')">
            <text class="toc-number">2</text>
            <text class="toc-text">账号管理</text>
          </view>
          <view class="toc-item" @click="scrollToSection('section-3')">
            <text class="toc-number">3</text>
            <text class="toc-text">用户行为规范</text>
          </view>
          <view class="toc-item" @click="scrollToSection('section-4')">
            <text class="toc-number">4</text>
            <text class="toc-text">知识产权</text>
          </view>
          <view class="toc-item" @click="scrollToSection('section-5')">
            <text class="toc-number">5</text>
            <text class="toc-text">免责声明</text>
          </view>
          <view class="toc-item" @click="scrollToSection('section-6')">
            <text class="toc-number">6</text>
            <text class="toc-text">协议变更</text>
          </view>
          <view class="toc-item" @click="scrollToSection('section-7')">
            <text class="toc-number">7</text>
            <text class="toc-text">法律适用</text>
          </view>
          <view class="toc-item" @click="scrollToSection('section-8')">
            <text class="toc-number">8</text>
            <text class="toc-text">联系我们</text>
          </view>
        </view>
      </view>

      <!-- 文档正文 -->
      <view class="doc-content">
        <!-- 1. 服务说明 -->
        <view id="section-1" class="content-section">
          <text class="section-title">1. 服务说明</text>

          <view class="subsection">
            <text class="subsection-title">1.1 服务内容</text>
            <text class="subsection-text">CampusLink 是一个面向高校师生的资源互助与问答平台，提供以下核心服务：</text>
            <view class="feature-list">
              <view class="feature-item">
                <view class="feature-icon">
                  <svg viewBox="0 0 24 24" fill="none">
                    <path d="M9 12L11 14L15 10M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </view>
                <view class="feature-content">
                  <text class="feature-title">资源分享</text>
                  <text class="feature-desc">上传和下载课件、试题、笔记等学习资料</text>
                </view>
              </view>
              <view class="feature-item">
                <view class="feature-icon">
                  <svg viewBox="0 0 24 24" fill="none">
                    <path d="M8 12H8.01M12 12H12.01M16 12H16.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </view>
                <view class="feature-content">
                  <text class="feature-title">问答社区</text>
                  <text class="feature-desc">提问、回答学业问题，采纳最佳答案</text>
                </view>
              </view>
              <view class="feature-item">
                <view class="feature-icon">
                  <svg viewBox="0 0 24 24" fill="none">
                    <path d="M9 5H7C5.89543 5 5 5.89543 5 7V19C5 20.1046 5.89543 21 7 21H17C18.1046 21 19 20.1046 19 19V7C19 5.89543 18.1046 5 17 5H15M9 5C9 6.10457 9.89543 7 11 7H13C14.1046 7 15 6.10457 15 5M9 5C9 3.89543 9.89543 3 11 3H13C14.1046 3 15 3.89543 15 3.89543" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </view>
                <view class="feature-content">
                  <text class="feature-title">任务互助</text>
                  <text class="feature-desc">发布和接取校园任务，互帮互助</text>
                </view>
              </view>
              <view class="feature-item">
                <view class="feature-icon">
                  <svg viewBox="0 0 24 24" fill="none">
                    <path d="M17 20H22V18C22 16.3431 20.6569 15 19 15C18.0444 15 17.1931 15.4468 16.6438 16.1429M17 20H7M17 20V18C17 17.3438 16.8736 16.717 16.6438 16.1429M7 20H2V18C2 16.3431 3.34315 15 5 15C5.95561 15 6.80686 15.4468 7.35625 16.1429M7 20V18C7 17.3438 7.12642 16.717 7.35625 16.1429M7.35625 16.1429C8.0935 14.301 9.89482 13 12 13C14.1052 13 15.9065 14.301 16.6438 16.1429M15 7C15 8.65685 13.6569 10 12 10C10.3431 10 9 8.65685 9 7C9 5.34315 10.3431 4 12 4C13.6569 4 15 5.34315 15 7ZM21 10C21 11.1046 20.1046 12 19 12C17.8954 12 17 11.1046 17 10C17 8.89543 17.8954 8 19 8C20.1046 8 21 8.89543 21 10ZM7 10C7 11.1046 6.10457 12 5 12C3.89543 12 3 11.1046 3 10C3 8.89543 3.89543 8 5 8C6.10457 8 7 8.89543 7 10Z" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </view>
                <view class="feature-content">
                  <text class="feature-title">社团活动</text>
                  <text class="feature-desc">参与社团活动，报名签到</text>
                </view>
              </view>
            </view>
          </view>

          <view class="subsection">
            <text class="subsection-title">1.2 服务范围</text>
            <view class="info-list">
              <text class="info-item">• 本服务仅面向在校师生，需使用学校邮箱进行注册认证</text>
              <text class="info-item">• 我们保留随时修改或中断服务的权利，恕不另行通知</text>
              <text class="info-item">• 部分功能可能需要额外的积分或权限才能使用</text>
            </view>
          </view>
        </view>

        <!-- 2. 账号管理 -->
        <view id="section-2" class="content-section">
          <text class="section-title">2. 账号管理</text>

          <view class="subsection">
            <text class="subsection-title">2.1 账号注册</text>
            <view class="info-list">
              <text class="info-item">• 您需要提供真实、准确、完整的个人信息</text>
              <text class="info-item">• 一个学校邮箱仅能注册一个账号</text>
              <text class="info-item">• 您必须年满 18 周岁或获得监护人同意</text>
              <text class="info-item">• 禁止使用虚假身份或冒充他人注册</text>
            </view>
          </view>

          <view class="subsection">
            <text class="subsection-title">2.2 账号安全</text>
            <view class="security-tips">
              <view class="tip-card">
                <view class="tip-icon">🔐</view>
                <text class="tip-title">保护密码</text>
                <text class="tip-desc">请妥善保管您的账号和密码，不要与他人共享</text>
              </view>
              <view class="tip-card">
                <view class="tip-icon">📧</view>
                <text class="tip-title">绑定邮箱</text>
                <text class="tip-desc">建议绑定备用邮箱，以便账号找回</text>
              </view>
              <view class="tip-card">
                <view class="tip-icon">⚠️</view>
                <text class="tip-title">异常登录</text>
                <text class="tip-desc">如发现账号异常，请立即修改密码并联系我们</text>
              </view>
              <view class="tip-card">
                <view class="tip-icon">🚫</view>
                <text class="tip-title">禁止转让</text>
                <text class="tip-desc">账号仅限本人使用，禁止出售或转让</text>
              </view>
            </view>
          </view>

          <view class="subsection">
            <text class="subsection-title">2.3 账号注销</text>
            <text class="subsection-text">您可随时申请注销账号，注销后：</text>
            <view class="note-box note-box--warning">
              <view class="note-icon">
                <svg viewBox="0 0 24 24" fill="none">
                  <path d="M12 9V13M12 17H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2"/>
                </svg>
              </view>
              <view class="note-content">
                <text class="note-title">注销须知</text>
                <text class="note-text">• 账号内所有数据将被永久删除，无法恢复</text>
                <text class="note-text">• 未完成的任务和订单将自动取消</text>
                <text class="note-text">• 剩余积分将被清零，无法退还</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 3. 用户行为规范 -->
        <view id="section-3" class="content-section">
          <text class="section-title">3. 用户行为规范</text>

          <view class="subsection">
            <text class="subsection-title">3.1 允许的行为</text>
            <view class="behavior-grid allowed">
              <view class="behavior-card">
                <text class="behavior-icon">✅</text>
                <text class="behavior-text">分享优质学习资料</text>
              </view>
              <view class="behavior-card">
                <text class="behavior-icon">✅</text>
                <text class="behavior-text">发布真实有效的问题</text>
              </view>
              <view class="behavior-card">
                <text class="behavior-icon">✅</text>
                <text class="behavior-text">提供有价值的答案</text>
              </view>
              <view class="behavior-card">
                <text class="behavior-icon">✅</text>
                <text class="behavior-text">参与正当的校园活动</text>
              </view>
              <view class="behavior-card">
                <text class="behavior-icon">✅</text>
                <text class="behavior-text">遵守学术诚信原则</text>
              </view>
              <view class="behavior-card">
                <text class="behavior-icon">✅</text>
                <text class="behavior-text">尊重他人知识产权</text>
              </view>
            </view>
          </view>

          <view class="subsection">
            <text class="subsection-title">3.2 禁止的行为</text>
            <view class="behavior-grid forbidden">
              <view class="behavior-card">
                <text class="behavior-icon">❌</text>
                <text class="behavior-text">上传违法违规内容</text>
              </view>
              <view class="behavior-card">
                <text class="behavior-icon">❌</text>
                <text class="behavior-text">发布虚假信息或广告</text>
              </view>
              <view class="behavior-card">
                <text class="behavior-icon">❌</text>
                <text class="behavior-text">抄袭他人作品</text>
              </view>
              <view class="behavior-card">
                <text class="behavior-icon">❌</text>
                <text class="behavior-text">恶意刷分或作弊</text>
              </view>
              <view class="behavior-card">
                <text class="behavior-icon">❌</text>
                <text class="behavior-text">骚扰或诽谤他人</text>
              </view>
              <view class="behavior-card">
                <text class="behavior-icon">❌</text>
                <text class="behavior-text">传播病毒或恶意代码</text>
              </view>
              <view class="behavior-card">
                <text class="behavior-icon">❌</text>
                <text class="behavior-text">买卖账号或积分</text>
              </view>
              <view class="behavior-card">
                <text class="behavior-icon">❌</text>
                <text class="behavior-text">未经授权的商业活动</text>
              </view>
            </view>
          </view>

          <view class="subsection">
            <text class="subsection-title">3.3 违规处理</text>
            <text class="subsection-text">违反本协议的用户，我们将视情节严重程度采取以下措施：</text>
            <view class="penalty-list">
              <view class="penalty-item level-1">
                <view class="penalty-badge">轻度违规</view>
                <text class="penalty-desc">警告提示、删除违规内容、扣除积分</text>
              </view>
              <view class="penalty-item level-2">
                <view class="penalty-badge">中度违规</view>
                <text class="penalty-desc">限制功能使用、临时封禁账号（7-30天）</text>
              </view>
              <view class="penalty-item level-3">
                <view class="penalty-badge">重度违规</view>
                <text class="penalty-desc">永久封禁账号、移交执法机关</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 4. 知识产权 -->
        <view id="section-4" class="content-section">
          <text class="section-title">4. 知识产权</text>

          <view class="subsection">
            <text class="subsection-title">4.1 平台内容</text>
            <text class="subsection-text">CampusLink 平台的设计、代码、商标、图标等知识产权归平台所有，未经授权不得使用。</text>
          </view>

          <view class="subsection">
            <text class="subsection-title">4.2 用户内容</text>
            <view class="info-list">
              <text class="info-item">• 您上传的内容，知识产权归您本人所有</text>
              <text class="info-item">• 您授予平台非独占、免费、永久的使用许可</text>
              <text class="info-item">• 平台有权对用户内容进行审核、编辑或删除</text>
              <text class="info-item">• 您需确保上传内容不侵犯他人权利</text>
            </view>
          </view>

          <view class="subsection">
            <text class="subsection-title">4.3 侵权投诉</text>
            <text class="subsection-text">如发现侵权内容，请提供以下材料：</text>
            <view class="note-box note-box--action">
              <view class="note-icon">
                <svg viewBox="0 0 24 24" fill="none">
                  <path d="M9 12L11 14L15 10M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2"/>
                </svg>
              </view>
              <view class="note-content">
                <text class="note-title">投诉材料清单</text>
                <text class="note-text">1. 权利人身份证明文件</text>
                <text class="note-text">2. 权利归属证明材料</text>
                <text class="note-text">3. 侵权内容链接和截图</text>
                <text class="note-text">4. 联系方式（邮箱、电话）</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 5. 免责声明 -->
        <view id="section-5" class="content-section">
          <text class="section-title">5. 免责声明</text>

          <view class="subsection">
            <text class="subsection-title">5.1 服务可用性</text>
            <view class="info-list">
              <text class="info-item">• 我们将尽力保障服务稳定，但不保证服务不中断</text>
              <text class="info-item">• 系统维护、升级期间可能暂停服务</text>
              <text class="info-item">• 不可抗力因素导致的服务中断，我们不承担责任</text>
            </view>
          </view>

          <view class="subsection">
            <text class="subsection-title">5.2 内容准确性</text>
            <text class="subsection-text">用户发布的内容仅代表其个人观点，平台不对内容的真实性、准确性、完整性负责。</text>
            <view class="note-box note-box--warning">
              <view class="note-icon">
                <svg viewBox="0 0 24 24" fill="none">
                  <path d="M12 9V13M12 17H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2"/>
                </svg>
              </view>
              <view class="note-content">
                <text class="note-title">免责提示</text>
                <text class="note-text">因使用平台资料导致的学业问题，平台不承担任何责任。请您独立思考、审慎判断。</text>
              </view>
            </view>
          </view>

          <view class="subsection">
            <text class="subsection-title">5.3 第三方链接</text>
            <text class="subsection-text">平台可能包含第三方网站链接，我们不对这些网站的内容、隐私政策或安全性负责。</text>
          </view>

          <view class="subsection">
            <text class="subsection-title">5.4 损失赔偿</text>
            <text class="subsection-text">在法律允许的范围内，平台对以下情形不承担赔偿责任：</text>
            <view class="info-list">
              <text class="info-item">• 因用户违规操作导致的损失</text>
              <text class="info-item">• 因网络故障、黑客攻击等原因导致的数据丢失</text>
              <text class="info-item">• 因不可抗力因素导致的服务中断</text>
              <text class="info-item">• 因第三方原因导致的任何损失</text>
            </view>
          </view>
        </view>

        <!-- 6. 协议变更 -->
        <view id="section-6" class="content-section">
          <text class="section-title">6. 协议变更</text>

          <view class="subsection">
            <text class="subsection-title">6.1 变更通知</text>
            <text class="subsection-text">我们保留随时修改本协议的权利。重大变更将通过以下方式通知您：</text>
            <view class="notice-methods">
              <view class="method-card">
                <view class="method-icon">🔔</view>
                <text class="method-title">站内通知</text>
                <text class="method-desc">系统消息推送</text>
              </view>
              <view class="method-card">
                <view class="method-icon">📧</view>
                <text class="method-title">邮件通知</text>
                <text class="method-desc">发送至绑定邮箱</text>
              </view>
              <view class="method-card">
                <view class="method-icon">📱</view>
                <text class="method-title">弹窗提示</text>
                <text class="method-desc">登录时确认</text>
              </view>
              <view class="method-card">
                <view class="method-icon">📄</view>
                <text class="method-title">公告发布</text>
                <text class="method-desc">官网公告栏</text>
              </view>
            </view>
          </view>

          <view class="subsection">
            <text class="subsection-title">6.2 变更生效</text>
            <view class="info-list">
              <text class="info-item">• 协议变更后，继续使用服务即视为同意新协议</text>
              <text class="info-item">• 如不同意新协议，您可选择停止使用服务</text>
              <text class="info-item">• 重大变更将提前 7 天通知</text>
            </view>
          </view>
        </view>

        <!-- 7. 法律适用 -->
        <view id="section-7" class="content-section">
          <text class="section-title">7. 法律适用与争议解决</text>

          <view class="subsection">
            <text class="subsection-title">7.1 适用法律</text>
            <text class="subsection-text">本协议的订立、执行、解释及争议解决均适用中华人民共和国法律。</text>
          </view>

          <view class="subsection">
            <text class="subsection-title">7.2 争议解决</text>
            <text class="subsection-text">如发生争议，双方应友好协商解决；协商不成的，任何一方可向平台所在地人民法院提起诉讼。</text>
          </view>

          <view class="subsection">
            <text class="subsection-title">7.3 协议效力</text>
            <view class="info-list">
              <text class="info-item">• 本协议部分条款无效不影响其他条款效力</text>
              <text class="info-item">• 平台未行使权利不构成对该权利的放弃</text>
              <text class="info-item">• 本协议的标题仅为方便阅读，不影响条款解释</text>
            </view>
          </view>
        </view>

        <!-- 8. 联系我们 -->
        <view id="section-8" class="content-section">
          <text class="section-title">8. 联系我们</text>

          <view class="subsection">
            <text class="subsection-text">如您对本协议有任何疑问或建议，欢迎通过以下方式联系我们：</text>
            <view class="contact-grid">
              <view class="contact-card">
                <view class="contact-icon">
                  <svg viewBox="0 0 24 24" fill="none">
                    <path d="M3 8L10.89 13.26C11.5 13.67 12.5 13.67 13.11 13.26L21 8M5 19H19C20.1046 19 21 18.1046 21 17V7C21 5.89543 20.1046 5 19 5H5C3.89543 5 3 5.89543 3 7V17C3 18.1046 3.89543 19 5 19Z" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </view>
                <view class="contact-info">
                  <text class="contact-label">客服邮箱</text>
                  <text class="contact-value">support@campuslink.com</text>
                </view>
              </view>
              <view class="contact-card">
                <view class="contact-icon">
                  <svg viewBox="0 0 24 24" fill="none">
                    <path d="M3 5C3 3.89543 3.89543 3 5 3H8.27924C8.70967 3 9.09181 3.27543 9.22792 3.68377L10.7257 8.17721C10.8831 8.64932 10.6694 9.16531 10.2243 9.38787L7.96701 10.5165C9.06925 12.9612 11.0388 14.9308 13.4835 16.033L14.6121 13.7757C14.8347 13.3306 15.3507 13.1169 15.8228 13.2743L20.3162 14.7721C20.7246 14.9082 21 15.2903 21 15.7208V19C21 20.1046 20.1046 21 19 21H18C9.71573 21 3 14.2843 3 6V5Z" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </view>
                <view class="contact-info">
                  <text class="contact-label">客服热线</text>
                  <text class="contact-value">400-123-4567</text>
                </view>
              </view>
              <view class="contact-card">
                <view class="contact-icon">
                  <svg viewBox="0 0 24 24" fill="none">
                    <path d="M17.657 16.657L13.414 20.9C13.039 21.275 12.535 21.487 12.008 21.487C11.481 21.487 10.977 21.275 10.602 20.9L6.343 16.657C5.22422 15.5381 4.46234 14.1127 4.15369 12.5608C3.84504 11.009 4.00349 9.40047 4.60901 7.93868C5.21452 6.4769 6.2399 5.22749 7.55548 4.34846C8.87107 3.46943 10.4178 3 12 3C13.5822 3 15.1289 3.46943 16.4445 4.34846C17.7601 5.22749 18.7855 6.4769 19.391 7.93868C19.9965 9.40047 20.155 11.009 19.8463 12.5608C19.5377 14.1127 18.7758 15.5381 17.657 16.657V16.657Z" stroke="currentColor" stroke-width="2"/>
                    <path d="M15 11C15 12.6569 13.6569 14 12 14C10.3431 14 9 12.6569 9 11C9 9.34315 10.3431 8 12 8C13.6569 8 15 9.34315 15 11Z" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </view>
                <view class="contact-info">
                  <text class="contact-label">办公地址</text>
                  <text class="contact-value">北京市海淀区学院路XX号</text>
                </view>
              </view>
              <view class="contact-card">
                <view class="contact-icon">
                  <svg viewBox="0 0 24 24" fill="none">
                    <path d="M12 8V12L15 15M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2"/>
                  </svg>
                </view>
                <view class="contact-info">
                  <text class="contact-label">工作时间</text>
                  <text class="contact-value">周一至周五 9:00-18:00</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 文档底部 -->
      <view class="doc-footer">
        <view class="footer-divider"></view>
        <text class="footer-text">感谢您使用 CampusLink！</text>
        <text class="footer-text">让我们共同营造健康、积极的校园互助环境。</text>
        <text class="footer-version">版本号：v1.0.0 | 生效日期：2024年1月1日</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const handleBack = () => {
  const pages = getCurrentPages()

  if (pages.length > 1) {
    uni.navigateBack({ delta: 1 })
  } else {
    uni.switchTab({ url: '/pages/home/index' })
  }
}

const scrollToSection = (sectionId: string) => {
  uni.createSelectorQuery()
    .select(`#${sectionId}`)
    .boundingClientRect((rect: any) => {
      if (rect) {
        uni.pageScrollTo({
          scrollTop: rect.top - 80,
          duration: 300
        })
      }
    })
    .exec()
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

// ==================== 页面容器 ====================
.terms-page {
  min-height: 100vh;
  background: linear-gradient(180deg,
    rgba($primary, 0.02) 0%,
    rgba($primary, 0.04) 50%,
    $bg-page 100%
  );
}

// ==================== 顶部导航栏 ====================
.page-header {
  position: sticky;
  top: 0;
  z-index: $z-sticky;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  background: rgba($white, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid $border-light;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: $bg-page;
  cursor: pointer;
  transition: $transition-base;

  &:hover {
    background: $primary-50;

    .back-icon {
      color: $primary;
    }
  }

  .back-icon {
    width: 20px;
    height: 20px;
    color: $text-secondary;
    transition: $transition-base;
  }
}

.page-title {
  font-size: 18px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

// ==================== 内容区域 ====================
.content-wrapper {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 24px 80px;
}

// ==================== 文档头部 ====================
.doc-header {
  text-align: center;
  padding: 40px 32px;
  margin-bottom: 48px;
  background: linear-gradient(135deg,
    rgba($primary, 0.08) 0%,
    rgba($primary, 0.04) 100%
  );
  border-radius: 20px;
  border: 2px solid rgba($primary, 0.15);
}

.doc-title {
  display: block;
  font-size: 32px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin-bottom: 12px;
  line-height: 1.3;
}

.doc-subtitle {
  display: block;
  font-size: 16px;
  color: $text-secondary;
  margin-bottom: 24px;
}

.doc-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  flex-wrap: wrap;
}

.meta-item {
  font-size: 14px;
  color: $text-tertiary;

  &::before {
    content: '•';
    margin-right: 8px;
    color: $primary;
  }

  &:first-child::before {
    display: none;
  }
}

// ==================== 前言区域 ====================
.preamble-section {
  margin-bottom: 40px;
}

// ==================== 目录导航 ====================
.toc-section {
  margin-bottom: 48px;
  padding: 24px;
  background: $white;
  border-radius: 16px;
  border: 1px solid $border-light;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.toc-title {
  display: block;
  font-size: 18px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin-bottom: 20px;
}

.toc-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.toc-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: $bg-page;
  border-radius: 10px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: $transition-base;

  &:hover {
    background: $primary-50;
    border-color: $primary-200;
    transform: translateX(4px);
  }
}

.toc-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, $primary 0%, $primary-light 100%);
  border-radius: 8px;
  font-size: 14px;
  font-weight: $font-weight-semibold;
  color: $white;
  flex-shrink: 0;
}

.toc-text {
  font-size: 15px;
  color: $text-primary;
  font-weight: $font-weight-medium;
}

// ==================== 文档正文 ====================
.doc-content {
  padding: 0;
}

.content-section {
  margin-bottom: 56px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  display: block;
  font-size: 26px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 3px solid $primary;
  background: linear-gradient(90deg, $primary 0%, transparent 100%);
  background-size: 100% 3px;
  background-repeat: no-repeat;
  background-position: bottom;
}

.subsection {
  margin-bottom: 32px;

  &:last-child {
    margin-bottom: 0;
  }
}

.subsection-title {
  display: block;
  font-size: 18px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin-bottom: 16px;
}

.subsection-text {
  display: block;
  font-size: 15px;
  color: $text-secondary;
  line-height: 1.8;
  margin-bottom: 16px;
}

// ==================== 信息列表 ====================
.info-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-item {
  font-size: 15px;
  color: $text-secondary;
  line-height: 1.8;
  padding-left: 8px;
}

// ==================== 功能列表 ====================
.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
}

.feature-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: $white;
  border-radius: 12px;
  border: 1px solid $border-light;
  transition: $transition-base;

  &:hover {
    border-color: $primary-200;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
    transform: translateY(-2px);
  }
}

.feature-icon {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba($primary, 0.1) 0%, rgba($primary, 0.05) 100%);
  border-radius: 12px;

  svg {
    width: 28px;
    height: 28px;
    color: $primary;
  }
}

.feature-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.feature-title {
  font-size: 16px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.feature-desc {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.6;
}

// ==================== 安全提示卡片 ====================
.security-tips {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-top: 20px;
}

.tip-card {
  padding: 20px;
  background: $white;
  border-radius: 12px;
  border: 1px solid $border-light;
  transition: $transition-base;

  &:hover {
    border-color: $primary-200;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
    transform: translateY(-2px);
  }
}

.tip-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.tip-title {
  display: block;
  font-size: 16px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin-bottom: 8px;
}

.tip-desc {
  display: block;
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.6;
}

// ==================== 行为规范网格 ====================
.behavior-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 20px;

  &.allowed .behavior-card {
    border-color: rgba($success, 0.2);
    background: linear-gradient(135deg,
      rgba($success, 0.05) 0%,
      rgba($success, 0.02) 100%
    );

    &:hover {
      border-color: $success-200;
      box-shadow: 0 4px 12px rgba($success, 0.15);
    }
  }

  &.forbidden .behavior-card {
    border-color: rgba($error, 0.2);
    background: linear-gradient(135deg,
      rgba($error, 0.05) 0%,
      rgba($error, 0.02) 100%
    );

    &:hover {
      border-color: $error-100;
      box-shadow: 0 4px 12px rgba($error, 0.15);
    }
  }
}

.behavior-card {
  padding: 16px;
  background: $white;
  border-radius: 10px;
  border: 1px solid $border-light;
  text-align: center;
  transition: $transition-base;

  &:hover {
    transform: translateY(-4px);
  }
}

.behavior-icon {
  display: block;
  font-size: 32px;
  margin-bottom: 8px;
}

.behavior-text {
  display: block;
  font-size: 14px;
  font-weight: $font-weight-medium;
  color: $text-primary;
  line-height: 1.5;
}

// ==================== 违规处理列表 ====================
.penalty-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
}

.penalty-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: $white;
  border-radius: 12px;
  border-left: 4px solid;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

  &.level-1 {
    border-color: $warning;
    background: linear-gradient(90deg,
      rgba($warning, 0.05) 0%,
      $white 100%
    );
  }

  &.level-2 {
    border-color: $accent;
    background: linear-gradient(90deg,
      rgba($accent, 0.05) 0%,
      $white 100%
    );
  }

  &.level-3 {
    border-color: $error;
    background: linear-gradient(90deg,
      rgba($error, 0.05) 0%,
      $white 100%
    );
  }
}

.penalty-badge {
  padding: 6px 14px;
  background: $gray-900;
  border-radius: 20px;
  font-size: 13px;
  font-weight: $font-weight-semibold;
  color: $white;
  white-space: nowrap;
}

.penalty-desc {
  flex: 1;
  font-size: 15px;
  color: $text-secondary;
}

// ==================== 通知方式 ====================
.notice-methods {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 20px;
}

.method-card {
  padding: 20px;
  background: $white;
  border-radius: 12px;
  border: 1px solid $border-light;
  text-align: center;
  transition: $transition-base;

  &:hover {
    border-color: $primary-200;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
    transform: translateY(-2px);
  }
}

.method-icon {
  font-size: 36px;
  margin-bottom: 12px;
}

.method-title {
  display: block;
  font-size: 15px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin-bottom: 6px;
}

.method-desc {
  display: block;
  font-size: 13px;
  color: $text-tertiary;
}

// ==================== 联系方式网格 ====================
.contact-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-top: 24px;
}

.contact-card {
  display: flex;
  gap: 16px;
  padding: 24px;
  background: $white;
  border-radius: 12px;
  border: 1px solid $border-light;
  transition: $transition-base;

  &:hover {
    border-color: $primary-200;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
    transform: translateY(-2px);
  }
}

.contact-icon {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba($primary, 0.1) 0%, rgba($primary, 0.05) 100%);
  border-radius: 12px;

  svg {
    width: 28px;
    height: 28px;
    color: $primary;
  }
}

.contact-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.contact-label {
  font-size: 13px;
  color: $text-tertiary;
}

.contact-value {
  font-size: 15px;
  font-weight: $font-weight-medium;
  color: $text-primary;
}

// ==================== 提示框 ====================
.note-box {
  display: flex;
  gap: 16px;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 24px;
  border: 2px solid;

  &--info {
    background: rgba($primary, 0.06);
    border-color: $primary-200;
  }

  &--action {
    background: rgba($success, 0.06);
    border-color: $success-200;
  }

  &--warning {
    background: rgba($warning, 0.06);
    border-color: $warning-100;
  }
}

.note-icon {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  margin-top: 2px;

  .note-box--info & {
    color: $primary;
  }

  .note-box--action & {
    color: $success;
  }

  .note-box--warning & {
    color: $warning;
  }

  svg {
    width: 100%;
    height: 100%;
  }
}

.note-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.note-title {
  font-size: 16px;
  font-weight: $font-weight-semibold;
  margin-bottom: 8px;

  .note-box--info & {
    color: $primary-dark;
  }

  .note-box--action & {
    color: $success;
  }

  .note-box--warning & {
    color: $warning-dark;
  }
}

.note-text {
  display: block;
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.7;

  & + & {
    margin-top: 6px;
  }
}

// ==================== 文档底部 ====================
.doc-footer {
  margin-top: 64px;
  padding-top: 40px;
  text-align: center;
}

.footer-divider {
  width: 100px;
  height: 3px;
  margin: 0 auto 24px;
  background: linear-gradient(90deg,
    transparent 0%,
    $primary 50%,
    transparent 100%
  );
  border-radius: 2px;
}

.footer-text {
  display: block;
  font-size: 15px;
  color: $text-secondary;
  line-height: 1.8;
  margin-bottom: 8px;
}

.footer-version {
  display: block;
  font-size: 13px;
  color: $text-quaternary;
  margin-top: 16px;
}

// ==================== 响应式 ====================
@media (max-width: 768px) {
  .content-wrapper {
    padding: 24px 16px 60px;
  }

  .doc-header {
    padding: 32px 24px;
  }

  .doc-title {
    font-size: 24px;
  }

  .section-title {
    font-size: 20px;
  }

  .toc-list {
    grid-template-columns: 1fr;
  }

  .security-tips,
  .behavior-grid,
  .notice-methods,
  .contact-grid {
    grid-template-columns: 1fr;
  }
}
</style>
