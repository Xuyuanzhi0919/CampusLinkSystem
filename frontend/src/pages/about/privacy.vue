<template>
  <view class="privacy-page">
    <!-- 顶部导航 -->
    <CNavBar title="隐私政策" />

    <!-- 文档内容 -->
    <view class="content-wrapper">
      <view class="document-container">
        <!-- 文档头部 -->
        <view class="doc-header">
          <text class="doc-title">CampusLink 隐私政策</text>
          <text class="doc-meta">生效日期：2025年1月1日 | 最后更新：2025年1月4日</text>
          <view class="doc-summary">
            <Icon name="info" :size="20" class="summary-icon" />
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
              <Icon name="info" :size="20" class="note-icon" />
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
                  <Icon name="shield" :size="32" class="security-icon" />
                  <text class="security-title">数据加密</text>
                  <text class="security-desc">HTTPS/TLS传输加密</text>
                </view>
                <view class="security-card">
                  <Icon name="lock" :size="32" class="security-icon" />
                  <text class="security-title">访问控制</text>
                  <text class="security-desc">严格的权限管理</text>
                </view>
                <view class="security-card">
                  <Icon name="badge-check" :size="32" class="security-icon" />
                  <text class="security-title">安全审计</text>
                  <text class="security-desc">定期安全评估</text>
                </view>
                <view class="security-card">
                  <Icon name="layers" :size="32" class="security-icon" />
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
                  <Icon name="eye" :size="24" class="right-icon" />
                  <text class="right-title">访问权</text>
                </view>
                <text class="right-desc">随时查看和导出您的个人信息</text>
              </view>

              <view class="right-card">
                <view class="right-header">
                  <Icon name="edit" :size="24" class="right-icon" />
                  <text class="right-title">更正权</text>
                </view>
                <text class="right-desc">修改或更新不准确的个人信息</text>
              </view>

              <view class="right-card">
                <view class="right-header">
                  <Icon name="trash" :size="24" class="right-icon" />
                  <text class="right-title">删除权</text>
                </view>
                <text class="right-desc">删除您的个人信息或注销账户</text>
              </view>

              <view class="right-card">
                <view class="right-header">
                  <Icon name="bell" :size="24" class="right-icon" />
                  <text class="right-title">撤回同意权</text>
                </view>
                <text class="right-desc">撤回之前授予的信息处理同意</text>
              </view>

              <view class="right-card">
                <view class="right-header">
                  <Icon name="settings" :size="24" class="right-icon" />
                  <text class="right-title">限制处理权</text>
                </view>
                <text class="right-desc">限制我们处理您个人信息的方式</text>
              </view>

              <view class="right-card">
                <view class="right-header">
                  <Icon name="share" :size="24" class="right-icon" />
                  <text class="right-title">数据可携权</text>
                </view>
                <text class="right-desc">以结构化格式导出您的数据</text>
              </view>
            </view>

            <view class="note-box note-box--action">
              <Icon name="zap" :size="20" class="note-icon" />
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
              <Icon name="alert-triangle" :size="20" class="note-icon" />
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
                  <Icon name="mail" :size="28" class="contact-icon" />
                  <text class="contact-label">电子邮件</text>
                  <text class="contact-value">privacy@campuslink.edu.cn</text>
                </view>

                <view class="contact-card">
                  <Icon name="map-pin" :size="28" class="contact-icon" />
                  <text class="contact-label">通讯地址</text>
                  <text class="contact-value">北京市海淀区某某大学科技园</text>
                </view>

                <view class="contact-card">
                  <Icon name="phone" :size="28" class="contact-icon" />
                  <text class="contact-label">客服热线</text>
                  <text class="contact-value">400-XXX-XXXX</text>
                </view>

                <view class="contact-card">
                  <Icon name="clock" :size="28" class="contact-icon" />
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
import { CNavBar } from '@/components/layout'
import Icon from '@/components/icons/index.vue'

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
