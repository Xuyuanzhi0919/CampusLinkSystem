<template>
  <view class="privacy-page">
    <!-- 顶部导航 -->
    <view class="page-header">
      <view class="header-container">
        <view class="back-btn" @click="handleBack">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M19 12H5M5 12L12 19M5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>
        <text class="page-title">隐私政策</text>
      </view>
    </view>

    <!-- 文档内容 -->
    <view class="content-wrapper">
      <view class="document-container">
        <!-- 文档头部 -->
        <view class="doc-header">
          <text class="doc-title">CampusLink 隐私政策</text>
          <text class="doc-meta">生效日期：2025年1月1日 | 最后更新：2025年1月4日</text>
          <view class="doc-summary">
            <svg class="summary-icon" viewBox="0 0 24 24" fill="none">
              <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="2"/>
              <path d="M12 16V12M12 8H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <text class="summary-text">
              我们非常重视您的隐私保护。本隐私政策详细说明了 CampusLink 如何收集、使用、存储和保护您的个人信息。
            </text>
          </view>
        </view>

        <!-- 目录导航 -->
        <view class="toc-section">
          <text class="toc-title">目录</text>
          <view class="toc-list">
            <text
              v-for="(item, index) in tocItems"
              :key="index"
              class="toc-item"
              @click="scrollToSection(item.id)"
            >
              {{ index + 1 }}. {{ item.title }}
            </text>
          </view>
        </view>

        <!-- 正文内容 -->
        <view class="doc-content">
          <!-- 第1节 -->
          <view id="section-1" class="content-section">
            <text class="section-number">01</text>
            <text class="section-title">我们收集的信息</text>

            <view class="subsection">
              <text class="subsection-title">1.1 账户信息</text>
              <text class="paragraph">
                当您注册 CampusLink 账户时,我们会收集以下信息:
              </text>
              <view class="info-list">
                <view class="info-item">
                  <text class="bullet">•</text>
                  <text class="info-text"><text class="highlight">基本信息:</text> 用户名、昵称、头像、学校、院系、年级</text>
                </view>
                <view class="info-item">
                  <text class="bullet">•</text>
                  <text class="info-text"><text class="highlight">联系方式:</text> 手机号码、电子邮箱(可选)</text>
                </view>
                <view class="info-item">
                  <text class="bullet">•</text>
                  <text class="info-text"><text class="highlight">身份验证:</text> 学生证信息、实名认证信息(可选)</text>
                </view>
              </view>
            </view>

            <view class="subsection">
              <text class="subsection-title">1.2 使用信息</text>
              <text class="paragraph">
                为了提供更好的服务体验,我们会收集您使用平台时产生的信息:
              </text>
              <view class="info-list">
                <view class="info-item">
                  <text class="bullet">•</text>
                  <text class="info-text"><text class="highlight">内容数据:</text> 您上传的资源、提出的问题、发表的回答、发布的任务</text>
                </view>
                <view class="info-item">
                  <text class="bullet">•</text>
                  <text class="info-text"><text class="highlight">行为数据:</text> 浏览记录、搜索历史、点赞收藏、下载记录</text>
                </view>
                <view class="info-item">
                  <text class="bullet">•</text>
                  <text class="info-text"><text class="highlight">设备信息:</text> 设备型号、操作系统版本、IP地址、浏览器类型</text>
                </view>
              </view>
            </view>

            <view class="subsection">
              <text class="subsection-title">1.3 第三方信息</text>
              <text class="paragraph">
                如果您选择使用第三方账号登录(如微信),我们会获得该第三方平台授权的信息,包括昵称、头像等公开信息。
              </text>
            </view>
          </view>

          <!-- 第2节 -->
          <view id="section-2" class="content-section">
            <text class="section-number">02</text>
            <text class="section-title">信息使用方式</text>

            <view class="subsection">
              <text class="subsection-title">2.1 核心服务功能</text>
              <view class="info-list">
                <view class="info-item">
                  <text class="bullet">✓</text>
                  <text class="info-text">提供资源共享、问答互助、任务发布等核心功能</text>
                </view>
                <view class="info-item">
                  <text class="bullet">✓</text>
                  <text class="info-text">个性化推荐:根据您的兴趣和行为推荐相关内容</text>
                </view>
                <view class="info-item">
                  <text class="bullet">✓</text>
                  <text class="info-text">用户身份验证和账户安全保护</text>
                </view>
                <view class="info-item">
                  <text class="bullet">✓</text>
                  <text class="info-text">积分系统、签到奖励、等级体系的运营</text>
                </view>
              </view>
            </view>

            <view class="subsection">
              <text class="subsection-title">2.2 平台优化</text>
              <view class="info-list">
                <view class="info-item">
                  <text class="bullet">✓</text>
                  <text class="info-text">分析用户行为,改进产品功能和用户体验</text>
                </view>
                <view class="info-item">
                  <text class="bullet">✓</text>
                  <text class="info-text">进行数据统计和趋势分析(匿名化处理)</text>
                </view>
                <view class="info-item">
                  <text class="bullet">✓</text>
                  <text class="info-text">检测和防止欺诈、滥用和违法行为</text>
                </view>
              </view>
            </view>

            <view class="note-box note-box--info">
              <svg class="note-icon" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                <path d="M12 16V12M12 8H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <view class="note-content">
                <text class="note-title">重要说明</text>
                <text class="note-text">我们<text class="highlight">不会</text>出售、出租或以其他方式向第三方披露您的个人信息,除非获得您的明确同意或法律要求。</text>
              </view>
            </view>
          </view>

          <!-- 第3节 -->
          <view id="section-3" class="content-section">
            <text class="section-number">03</text>
            <text class="section-title">信息存储与保护</text>

            <view class="subsection">
              <text class="subsection-title">3.1 数据存储</text>
              <text class="paragraph">
                您的个人信息将存储在位于中国境内的安全服务器上,我们采用符合行业标准的安全措施保护您的数据:
              </text>
              <view class="security-grid">
                <view class="security-card">
                  <svg class="security-icon" viewBox="0 0 24 24" fill="none">
                    <path d="M12 22C12 22 20 18 20 12V5L12 2L4 5V12C4 18 12 22 12 22Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <text class="security-title">数据加密</text>
                  <text class="security-desc">HTTPS/TLS传输加密</text>
                </view>
                <view class="security-card">
                  <svg class="security-icon" viewBox="0 0 24 24" fill="none">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2"/>
                    <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  <text class="security-title">访问控制</text>
                  <text class="security-desc">严格的权限管理</text>
                </view>
                <view class="security-card">
                  <svg class="security-icon" viewBox="0 0 24 24" fill="none">
                    <path d="M21 16V8C21 6.89543 20.1046 6 19 6H5C3.89543 6 3 6.89543 3 8V16C3 17.1046 3.89543 18 5 18H19C20.1046 18 21 17.1046 21 16Z" stroke="currentColor" stroke-width="2"/>
                    <path d="M3 10H21M7 14H7.01M11 14H11.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                  <text class="security-title">安全审计</text>
                  <text class="security-desc">定期安全评估</text>
                </view>
                <view class="security-card">
                  <svg class="security-icon" viewBox="0 0 24 24" fill="none">
                    <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M2 17L12 22L22 17M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <text class="security-title">数据备份</text>
                  <text class="security-desc">定期备份机制</text>
                </view>
              </view>
            </view>

            <view class="subsection">
              <text class="subsection-title">3.2 保留期限</text>
              <text class="paragraph">
                我们仅在实现服务目的所必需的期限内保留您的个人信息:
              </text>
              <view class="info-list">
                <view class="info-item">
                  <text class="bullet">•</text>
                  <text class="info-text"><text class="highlight">账户信息:</text> 保留至您注销账户后30天</text>
                </view>
                <view class="info-item">
                  <text class="bullet">•</text>
                  <text class="info-text"><text class="highlight">发布内容:</text> 保留至您主动删除或账户注销</text>
                </view>
                <view class="info-item">
                  <text class="bullet">•</text>
                  <text class="info-text"><text class="highlight">行为日志:</text> 匿名化处理后保留6个月用于统计分析</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 第4节 -->
          <view id="section-4" class="content-section">
            <text class="section-number">04</text>
            <text class="section-title">您的权利</text>

            <view class="rights-grid">
              <view class="right-card">
                <view class="right-header">
                  <svg class="right-icon" viewBox="0 0 24 24" fill="none">
                    <path d="M15 12C15 13.6569 13.6569 15 12 15C10.3431 15 9 13.6569 9 12C9 10.3431 10.3431 9 12 9C13.6569 9 15 10.3431 15 12Z" stroke="currentColor" stroke-width="2"/>
                    <path d="M2.45825 12C3.73253 7.94288 7.52281 5 12 5C16.4772 5 20.2675 7.94291 21.5418 12C20.2675 16.0571 16.4772 19 12 19C7.52281 19 3.73253 16.0571 2.45825 12Z" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  <text class="right-title">访问权</text>
                </view>
                <text class="right-desc">随时查看和导出您的个人信息</text>
              </view>

              <view class="right-card">
                <view class="right-header">
                  <svg class="right-icon" viewBox="0 0 24 24" fill="none">
                    <path d="M11 4H4C3.46957 4 2.96086 4.21071 2.58579 4.58579C2.21071 4.96086 2 5.46957 2 6V20C2 20.5304 2.21071 21.0391 2.58579 21.4142C2.96086 21.7893 3.46957 22 4 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M18.5 2.5C18.8978 2.10217 19.4374 1.87868 20 1.87868C20.5626 1.87868 21.1022 2.10217 21.5 2.5C21.8978 2.89782 22.1213 3.43739 22.1213 4C22.1213 4.56261 21.8978 5.10217 21.5 5.5L12 15L8 16L9 12L18.5 2.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <text class="right-title">更正权</text>
                </view>
                <text class="right-desc">修改或更新不准确的个人信息</text>
              </view>

              <view class="right-card">
                <view class="right-header">
                  <svg class="right-icon" viewBox="0 0 24 24" fill="none">
                    <path d="M3 6H5H21M19 6V20C19 20.5304 18.7893 21.0391 18.4142 21.4142C18.0391 21.7893 17.5304 22 17 22H7C6.46957 22 5.96086 21.7893 5.58579 21.4142C5.21071 21.0391 5 20.5304 5 20V6M8 6V4C8 3.46957 8.21071 2.96086 8.58579 2.58579C8.96086 2.21071 9.46957 2 10 2H14C14.5304 2 15.0391 2.21071 15.4142 2.58579C15.7893 2.96086 16 3.46957 16 4V6M10 11V17M14 11V17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <text class="right-title">删除权</text>
                </view>
                <text class="right-desc">删除您的个人信息或注销账户</text>
              </view>

              <view class="right-card">
                <view class="right-header">
                  <svg class="right-icon" viewBox="0 0 24 24" fill="none">
                    <path d="M18 8C18 6.4087 17.3679 4.88258 16.2426 3.75736C15.1174 2.63214 13.5913 2 12 2C10.4087 2 8.88258 2.63214 7.75736 3.75736C6.63214 4.88258 6 6.4087 6 8C6 15 3 17 3 17H21C21 17 18 15 18 8Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M13.73 21C13.5542 21.3031 13.3019 21.5547 12.9982 21.7295C12.6946 21.9044 12.3504 21.9965 12 21.9965C11.6496 21.9965 11.3054 21.9044 11.0018 21.7295C10.6982 21.5547 10.4458 21.3031 10.27 21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <text class="right-title">撤回同意权</text>
                </view>
                <text class="right-desc">撤回之前授予的信息处理同意</text>
              </view>

              <view class="right-card">
                <view class="right-header">
                  <svg class="right-icon" viewBox="0 0 24 24" fill="none">
                    <rect x="5" y="2" width="14" height="20" rx="2" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 18H12.01M9 15H15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                  <text class="right-title">限制处理权</text>
                </view>
                <text class="right-desc">限制我们处理您个人信息的方式</text>
              </view>

              <view class="right-card">
                <view class="right-header">
                  <svg class="right-icon" viewBox="0 0 24 24" fill="none">
                    <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15M17 8L12 3M12 3L7 8M12 3V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <text class="right-title">数据可携权</text>
                </view>
                <text class="right-desc">以结构化格式导出您的数据</text>
              </view>
            </view>

            <view class="note-box note-box--action">
              <svg class="note-icon" viewBox="0 0 24 24" fill="none">
                <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <view class="note-content">
                <text class="note-title">如何行使您的权利</text>
                <text class="note-text">您可以通过个人中心的"隐私设置"或发送邮件至 privacy@campuslink.edu.cn 来行使上述权利。</text>
              </view>
            </view>
          </view>

          <!-- 第5节 -->
          <view id="section-5" class="content-section">
            <text class="section-number">05</text>
            <text class="section-title">Cookie 和跟踪技术</text>

            <view class="subsection">
              <text class="paragraph">
                我们使用 Cookie 和类似技术来改善用户体验、分析使用情况和提供个性化内容:
              </text>
              <view class="cookie-types">
                <view class="cookie-type">
                  <text class="cookie-label">必要 Cookie</text>
                  <text class="cookie-desc">维持网站基本功能,无法禁用</text>
                  <text class="cookie-status cookie-status--required">必需</text>
                </view>
                <view class="cookie-type">
                  <text class="cookie-label">功能 Cookie</text>
                  <text class="cookie-desc">记住您的偏好设置,如语言选择</text>
                  <text class="cookie-status cookie-status--optional">可选</text>
                </view>
                <view class="cookie-type">
                  <text class="cookie-label">分析 Cookie</text>
                  <text class="cookie-desc">帮助我们了解网站使用情况</text>
                  <text class="cookie-status cookie-status--optional">可选</text>
                </view>
                <view class="cookie-type">
                  <text class="cookie-label">广告 Cookie</text>
                  <text class="cookie-desc">当前未使用,未来可能用于推荐</text>
                  <text class="cookie-status cookie-status--disabled">禁用</text>
                </view>
              </view>
              <text class="paragraph">
                您可以通过浏览器设置管理或禁用 Cookie,但这可能影响部分功能的使用。
              </text>
            </view>
          </view>

          <!-- 第6节 -->
          <view id="section-6" class="content-section">
            <text class="section-number">06</text>
            <text class="section-title">未成年人保护</text>

            <view class="subsection">
              <text class="paragraph">
                CampusLink 高度重视未成年人的隐私保护:
              </text>
              <view class="info-list">
                <view class="info-item">
                  <text class="bullet">•</text>
                  <text class="info-text">我们的服务主要面向年满18周岁的在校大学生</text>
                </view>
                <view class="info-item">
                  <text class="bullet">•</text>
                  <text class="info-text">如果您未满18周岁,请在监护人同意和指导下使用我们的服务</text>
                </view>
                <view class="info-item">
                  <text class="bullet">•</text>
                  <text class="info-text">我们不会故意收集14周岁以下未成年人的个人信息</text>
                </view>
                <view class="info-item">
                  <text class="bullet">•</text>
                  <text class="info-text">如发现未成年人在未经监护人同意的情况下提供信息,我们将立即删除</text>
                </view>
              </view>
            </view>

            <view class="note-box note-box--warning">
              <svg class="note-icon" viewBox="0 0 24 24" fill="none">
                <path d="M12 9V13M12 17H12.01M5 19H19C19.5304 19 20.0391 18.7893 20.4142 18.4142C20.7893 18.0391 21 17.5304 21 17V7C21 6.46957 20.7893 5.96086 20.4142 5.58579C20.0391 5.21071 19.5304 5 19 5H5C4.46957 5 3.96086 5.21071 3.58579 5.58579C3.21071 5.96086 3 6.46957 3 7V17C3 17.5304 3.21071 18.0391 3.58579 18.4142C3.96086 18.7893 4.46957 19 5 19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <view class="note-content">
                <text class="note-title">监护人须知</text>
                <text class="note-text">如果您是未成年人的监护人,发现孩子在未经您同意的情况下向我们提供了个人信息,请联系我们,我们将采取措施尽快删除相关信息。</text>
              </view>
            </view>
          </view>

          <!-- 第7节 -->
          <view id="section-7" class="content-section">
            <text class="section-number">07</text>
            <text class="section-title">政策更新</text>

            <view class="subsection">
              <text class="paragraph">
                我们可能会不时更新本隐私政策,以反映我们的信息实践或适用法律的变更。我们会通过以下方式通知您:
              </text>
              <view class="update-steps">
                <view class="update-step">
                  <view class="step-number">1</view>
                  <view class="step-content">
                    <text class="step-title">站内公告</text>
                    <text class="step-desc">在首页显著位置发布更新通知</text>
                  </view>
                </view>
                <view class="update-step">
                  <view class="step-number">2</view>
                  <view class="step-content">
                    <text class="step-title">邮件通知</text>
                    <text class="step-desc">向注册邮箱发送政策变更说明</text>
                  </view>
                </view>
                <view class="update-step">
                  <view class="step-number">3</view>
                  <view class="step-content">
                    <text class="step-title">弹窗提示</text>
                    <text class="step-desc">重大变更将在登录时弹窗提示</text>
                  </view>
                </view>
              </view>
              <text class="paragraph">
                如果您不同意更新后的政策,可以选择停止使用我们的服务。继续使用即表示您接受更新后的政策。
              </text>
            </view>
          </view>

          <!-- 第8节 -->
          <view id="section-8" class="content-section">
            <text class="section-number">08</text>
            <text class="section-title">联系我们</text>

            <view class="subsection">
              <text class="paragraph">
                如果您对本隐私政策有任何疑问、意见或建议,或需要行使您的权利,欢迎通过以下方式联系我们:
              </text>

              <view class="contact-grid">
                <view class="contact-card">
                  <svg class="contact-icon" viewBox="0 0 24 24" fill="none">
                    <path d="M3 8L10.89 13.26C11.23 13.47 11.61 13.59 12 13.59C12.39 13.59 12.77 13.47 13.11 13.26L21 8M5 19H19C19.5304 19 20.0391 18.7893 20.4142 18.4142C20.7893 18.0391 21 17.5304 21 17V7C21 6.46957 20.7893 5.96086 20.4142 5.58579C20.0391 5.21071 19.5304 5 19 5H5C4.46957 5 3.96086 5.21071 3.58579 5.58579C3.21071 5.96086 3 6.46957 3 7V17C3 17.5304 3.21071 18.0391 3.58579 18.4142C3.96086 18.7893 4.46957 19 5 19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <text class="contact-label">电子邮件</text>
                  <text class="contact-value">privacy@campuslink.edu.cn</text>
                </view>

                <view class="contact-card">
                  <svg class="contact-icon" viewBox="0 0 24 24" fill="none">
                    <path d="M21 10C21 17 12 23 12 23C12 23 3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z" stroke="currentColor" stroke-width="2"/>
                    <circle cx="12" cy="10" r="3" stroke="currentColor" stroke-width="2"/>
                  </svg>
                  <text class="contact-label">通讯地址</text>
                  <text class="contact-value">北京市海淀区某某大学科技园</text>
                </view>

                <view class="contact-card">
                  <svg class="contact-icon" viewBox="0 0 24 24" fill="none">
                    <path d="M22 16.92V19.92C22.0011 20.1985 21.9441 20.4741 21.8325 20.7293C21.7209 20.9845 21.5573 21.2136 21.3521 21.4018C21.1468 21.5901 20.9046 21.7335 20.6407 21.8227C20.3769 21.9119 20.0974 21.9451 19.82 21.92C16.7428 21.5856 13.7869 20.5341 11.19 18.85C8.77382 17.3146 6.72533 15.2661 5.18999 12.85C3.49997 10.2412 2.44824 7.27097 2.11999 4.17997C2.095 3.90344 2.12787 3.62474 2.21649 3.3616C2.30512 3.09846 2.44756 2.85666 2.63476 2.6516C2.82196 2.44653 3.0498 2.28268 3.30379 2.1705C3.55777 2.05831 3.83233 2.00024 4.10999 1.99997H7.10999C7.5953 1.9952 8.06579 2.16705 8.43376 2.48351C8.80173 2.79996 9.04207 3.23942 9.10999 3.71997C9.23662 4.68004 9.47144 5.6227 9.80999 6.52997C9.94454 6.8879 9.97366 7.27689 9.8939 7.65086C9.81415 8.02482 9.62886 8.36809 9.35999 8.63998L8.08999 9.90997C9.51355 12.4135 11.5864 14.4864 14.09 15.91L15.36 14.64C15.6319 14.3711 15.9751 14.1858 16.3491 14.1061C16.7231 14.0263 17.1121 14.0554 17.47 14.19C18.3773 14.5285 19.3199 14.7634 20.28 14.89C20.7658 14.9585 21.2094 15.2032 21.5265 15.5775C21.8437 15.9518 22.0122 16.4296 22 16.92Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <text class="contact-label">客服热线</text>
                  <text class="contact-value">400-XXX-XXXX</text>
                </view>

                <view class="contact-card">
                  <svg class="contact-icon" viewBox="0 0 24 24" fill="none">
                    <rect x="2" y="6" width="20" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
                    <path d="M2 10H22M6 14H6.01M10 14H14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                  <text class="contact-label">工作时间</text>
                  <text class="contact-value">周一至周五 9:00-18:00</text>
                </view>
              </view>

              <text class="paragraph">
                我们将在收到您的请求后15个工作日内予以答复。对于复杂或大量请求,我们可能需要延长答复期限,但最长不超过30个工作日。
              </text>
            </view>
          </view>
        </view>

        <!-- 底部声明 -->
        <view class="doc-footer">
          <view class="footer-divider"></view>
          <view class="footer-content">
            <text class="footer-text">本隐私政策的解释权归 CampusLink 所有</text>
            <text class="footer-version">版本号: v2.0 | 生效日期: 2025年1月1日</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 目录数据
const tocItems = ref([
  { id: 'section-1', title: '我们收集的信息' },
  { id: 'section-2', title: '信息使用方式' },
  { id: 'section-3', title: '信息存储与保护' },
  { id: 'section-4', title: '您的权利' },
  { id: 'section-5', title: 'Cookie 和跟踪技术' },
  { id: 'section-6', title: '未成年人保护' },
  { id: 'section-7', title: '政策更新' },
  { id: 'section-8', title: '联系我们' },
])

// 返回上一页
const handleBack = () => {
  uni.navigateBack({
    fail: () => {
      uni.switchTab({
        url: '/pages/home/index'
      })
    }
  })
}

// 滚动到指定章节
const scrollToSection = (sectionId: string) => {
  // H5 环境使用原生滚动
  // #ifdef H5
  const element = document.getElementById(sectionId)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
  // #endif

  // 小程序环境使用 uni API
  // #ifndef H5
  uni.pageScrollTo({
    selector: `#${sectionId}`,
    duration: 300
  })
  // #endif
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.privacy-page {
  min-height: 100vh;
  background: $gray-50;
}

// ==================== 顶部导航 ====================
.page-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: $white;
  border-bottom: 1px solid $border-light;
  box-shadow: 0 2px 8px rgba($black, 0.04);
}

.header-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: $radius-sm;
  cursor: pointer;
  transition: $transition-fast;
  color: $text-secondary;

  svg {
    width: 20px;
    height: 20px;
  }

  &:hover {
    background: $gray-100;
    color: $text-primary;
  }

  &:active {
    transform: scale(0.95);
  }
}

.page-title {
  font-size: 18px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

// ==================== 内容区域 ====================
.content-wrapper {
  max-width: 1000px;
  margin: 0 auto;
  padding: 40px 24px 80px;
}

.document-container {
  background: $white;
  border-radius: $radius-xl;
  box-shadow: 0 2px 12px rgba($black, 0.06);
  overflow: hidden;
}

// ==================== 文档头部 ====================
.doc-header {
  padding: 48px 48px 32px;
  background: linear-gradient(135deg, rgba($primary, 0.05), rgba($primary, 0.02));
  border-bottom: 2px solid $primary-100;
}

.doc-title {
  display: block;
  font-size: 32px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin-bottom: 12px;
  line-height: 1.3;
}

.doc-meta {
  display: block;
  font-size: 14px;
  color: $text-tertiary;
  margin-bottom: 24px;
}

.doc-summary {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: $white;
  border-radius: $radius-lg;
  border-left: 4px solid $primary;
}

.summary-icon {
  width: 24px;
  height: 24px;
  color: $primary;
  flex-shrink: 0;
  margin-top: 2px;
}

.summary-text {
  flex: 1;
  font-size: 15px;
  line-height: 1.7;
  color: $text-secondary;
}

// ==================== 目录导航 ====================
.toc-section {
  padding: 32px 48px;
  background: $gray-50;
  border-bottom: 1px solid $border-light;
}

.toc-title {
  display: block;
  font-size: 16px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin-bottom: 16px;
}

.toc-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.toc-item {
  font-size: 14px;
  color: $primary;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: $radius-sm;
  transition: $transition-fast;

  &:hover {
    background: $primary-50;
    color: $primary-dark;
  }
}

// ==================== 正文内容 ====================
.doc-content {
  padding: 48px;
}

.content-section {
  margin-bottom: 48px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-number {
  display: inline-block;
  width: 56px;
  height: 56px;
  line-height: 56px;
  text-align: center;
  font-size: 20px;
  font-weight: $font-weight-bold;
  color: $primary;
  background: $primary-50;
  border-radius: $radius-lg;
  margin-bottom: 16px;
}

.section-title {
  display: block;
  font-size: 24px;
  font-weight: $font-weight-bold;
  color: $text-primary;
  margin-bottom: 24px;
  line-height: 1.4;
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

.paragraph {
  display: block;
  font-size: 15px;
  line-height: 1.8;
  color: $text-secondary;
  margin-bottom: 16px;
}

.highlight {
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

// 信息列表
.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.bullet {
  font-size: 18px;
  color: $primary;
  flex-shrink: 0;
  line-height: 1.8;
  font-weight: $font-weight-bold;
}

.info-text {
  flex: 1;
  font-size: 15px;
  line-height: 1.8;
  color: $text-secondary;
}

// 安全措施网格
.security-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin: 24px 0;
}

.security-card {
  padding: 24px;
  background: $gray-50;
  border-radius: $radius-lg;
  border: 2px solid $border-light;
  transition: $transition-fast;
  text-align: center;

  &:hover {
    border-color: $primary-200;
    background: $primary-50;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba($primary, 0.1);
  }
}

.security-icon {
  width: 40px;
  height: 40px;
  color: $primary;
  margin-bottom: 12px;
}

.security-title {
  display: block;
  font-size: 16px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin-bottom: 8px;
}

.security-desc {
  display: block;
  font-size: 13px;
  color: $text-tertiary;
}

// 权利网格
.rights-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 24px;
}

.right-card {
  padding: 24px;
  background: $white;
  border: 2px solid $border-light;
  border-radius: $radius-lg;
  transition: $transition-fast;

  &:hover {
    border-color: $primary-200;
    box-shadow: 0 4px 16px rgba($primary, 0.12);
    transform: translateY(-2px);
  }
}

.right-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.right-icon {
  width: 32px;
  height: 32px;
  color: $primary;
}

.right-title {
  font-size: 17px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.right-desc {
  display: block;
  font-size: 14px;
  line-height: 1.6;
  color: $text-secondary;
}

// Cookie 类型
.cookie-types {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin: 24px 0;
}

.cookie-type {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: $gray-50;
  border-radius: $radius-lg;
  border: 1px solid $border-light;
}

.cookie-label {
  font-size: 16px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  flex-shrink: 0;
  margin-right: 16px;
}

.cookie-desc {
  flex: 1;
  font-size: 14px;
  color: $text-secondary;
}

.cookie-status {
  padding: 4px 12px;
  border-radius: $radius-sm;
  font-size: 13px;
  font-weight: $font-weight-medium;
  flex-shrink: 0;

  &--required {
    background: $error-50;
    color: $error;
  }

  &--optional {
    background: $warning-50;
    color: $warning;
  }

  &--disabled {
    background: $gray-200;
    color: $text-tertiary;
  }
}

// 更新步骤
.update-steps {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin: 24px 0;
}

.update-step {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.step-number {
  width: 40px;
  height: 40px;
  line-height: 40px;
  text-align: center;
  font-size: 18px;
  font-weight: $font-weight-bold;
  color: $white;
  background: $primary;
  border-radius: 50%;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
  padding-top: 4px;
}

.step-title {
  display: block;
  font-size: 16px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin-bottom: 6px;
}

.step-desc {
  display: block;
  font-size: 14px;
  color: $text-secondary;
}

// 联系方式网格
.contact-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin: 24px 0;
}

.contact-card {
  padding: 20px;
  background: linear-gradient(135deg, $primary-50, rgba($primary, 0.02));
  border-radius: $radius-lg;
  border: 1px solid $primary-100;
  text-align: center;
  transition: $transition-fast;

  &:hover {
    border-color: $primary-200;
    box-shadow: 0 4px 12px rgba($primary, 0.15);
    transform: translateY(-2px);
  }
}

.contact-icon {
  width: 36px;
  height: 36px;
  color: $primary;
  margin-bottom: 12px;
}

.contact-label {
  display: block;
  font-size: 13px;
  color: $text-tertiary;
  margin-bottom: 6px;
}

.contact-value {
  display: block;
  font-size: 15px;
  font-weight: $font-weight-medium;
  color: $text-primary;
}

// 提示框
.note-box {
  display: flex;
  gap: 16px;
  padding: 20px;
  border-radius: $radius-lg;
  margin: 24px 0;

  &--info {
    background: rgba($primary, 0.06);
    border: 2px solid $primary-200;
  }

  &--action {
    background: rgba($success, 0.06);
    border: 2px solid $success-200;
  }

  &--warning {
    background: rgba($warning, 0.06);
    border: 2px solid $warning-100;
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
}

.note-content {
  flex: 1;
}

.note-title {
  display: block;
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
  line-height: 1.7;
  color: $text-secondary;
}

// ==================== 底部声明 ====================
.doc-footer {
  padding: 32px 48px;
}

.footer-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, $border-color, transparent);
  margin-bottom: 24px;
}

.footer-content {
  text-align: center;
}

.footer-text {
  display: block;
  font-size: 14px;
  color: $text-tertiary;
  margin-bottom: 8px;
}

.footer-version {
  display: block;
  font-size: 13px;
  color: $text-quaternary;
}

// ==================== 响应式 ====================
@media (max-width: 768px) {
  .content-wrapper {
    padding: 24px 16px 60px;
  }

  .doc-header,
  .toc-section,
  .doc-content,
  .doc-footer {
    padding-left: 24px;
    padding-right: 24px;
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

  .security-grid,
  .rights-grid,
  .contact-grid {
    grid-template-columns: 1fr;
  }
}
</style>
