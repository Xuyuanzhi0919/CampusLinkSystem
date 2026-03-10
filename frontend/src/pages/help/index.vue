<template>
  <view class="help-page">
    <CNavBar title="帮助中心" />

    <view class="page-body">
      <!-- Hero + 搜索 -->
      <view class="hero-section">
        <text class="hero-title">有什么可以帮你？</text>
        <text class="hero-sub">搜索问题或浏览常见问题分类</text>
        <view class="search-wrap" :class="{ focused: searchFocused }">
          <svg class="search-ico" viewBox="0 0 24 24" fill="none">
            <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
            <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <input
            v-model="searchQuery"
            class="search-inp"
            type="text"
            placeholder="搜索帮助问题..."
            @focus="searchFocused = true"
            @blur="searchFocused = false"
          />
          <view v-if="searchQuery" class="search-clear" @click="searchQuery = ''">
            <svg viewBox="0 0 24 24" fill="none" width="16" height="16">
              <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </view>
        </view>
      </view>

      <view class="content-container">
        <!-- 分类标签 -->
        <scroll-view class="cats-scroll" scroll-x :show-scrollbar="false">
          <view class="cats-row">
            <view
              v-for="c in categories"
              :key="c.id"
              class="cat-chip"
              :class="{ active: activeCat === c.id }"
              @click="activeCat = c.id"
            >{{ c.label }}</view>
          </view>
        </scroll-view>

        <!-- FAQ 列表 -->
        <view class="faq-section">
          <template v-if="filteredFAQ.length > 0">
            <view
              v-for="item in filteredFAQ"
              :key="item.id"
              class="faq-item"
              :class="{ open: openId === item.id }"
              @click="toggle(item.id)"
            >
              <view class="faq-header">
                <view class="faq-q-wrap">
                  <view class="faq-dot"></view>
                  <text class="faq-q">{{ item.q }}</text>
                </view>
                <view class="faq-chevron">
                  <svg viewBox="0 0 24 24" fill="none" width="18" height="18">
                    <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </view>
              </view>
              <view v-if="openId === item.id" class="faq-body">
                <text class="faq-a">{{ item.a }}</text>
              </view>
            </view>
          </template>

          <!-- 无搜索结果 -->
          <view v-else class="faq-empty">
            <svg viewBox="0 0 24 24" fill="none" width="52" height="52" class="empty-svg">
              <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="1.5"/>
              <path d="M21 21L16.65 16.65" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <path d="M11 8v3M11 14h.01" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <text class="empty-title">未找到相关问题</text>
            <text class="empty-hint">换个关键词试试，或联系我们</text>
          </view>
        </view>

        <!-- 底部联系卡片 -->
        <view class="contact-card">
          <view class="contact-icon-wrap">
            <svg viewBox="0 0 24 24" fill="none" width="26" height="26">
              <path d="M21 15C21 15.53 20.79 16.04 20.41 16.41C20.04 16.79 19.53 17 19 17H7L3 21V5C3 4.47 3.21 3.96 3.59 3.59C3.96 3.21 4.47 3 5 3H19C19.53 3 20.04 3.21 20.41 3.59C20.79 3.96 21 4.47 21 5V15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
          <view class="contact-body">
            <text class="contact-title">没找到答案？</text>
            <text class="contact-desc">我们的团队会在 24 小时内回复您</text>
          </view>
          <view class="contact-btn" @click="goFeedback">发送反馈</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const searchQuery = ref('')
const searchFocused = ref(false)
const activeCat = ref('all')
const openId = ref<string | null>(null)

const categories = [
  { id: 'all', label: '全部' },
  { id: 'account', label: '账号安全' },
  { id: 'resource', label: '资源共享' },
  { id: 'qa', label: '问答社区' },
  { id: 'task', label: '任务中心' },
  { id: 'points', label: '积分系统' },
]

const faqData = [
  { id: 'a1', cat: 'account', q: '如何修改账号密码？', a: '进入「我的」→「账号设置」→「修改密码」，输入原密码和新密码后确认即可。建议设置包含大小写字母和数字的强密码，定期更换以保护账号安全。' },
  { id: 'a2', cat: 'account', q: '忘记密码怎么办？', a: '在登录页点击「忘记密码」，输入绑定的手机号或邮箱，通过验证码验证身份后重置密码。如长时间未收到验证码，请检查垃圾邮件或稍后重试。' },
  { id: 'a3', cat: 'account', q: '如何修改个人信息？', a: '进入「我的」→「编辑资料」，可修改昵称、头像、个人简介、学校等信息。部分信息修改后需重新审核，请耐心等待。' },
  { id: 'a4', cat: 'account', q: '如何注销账号？', a: '进入「我的」→「账号设置」→「注销账号」。注意：注销后所有数据将永久删除且无法恢复，请提前备份重要内容，确认无误后再操作。' },
  { id: 'r1', cat: 'resource', q: '上传资源有什么限制？', a: '单文件不超过 100MB，支持 PDF、Word、PPT、Excel、图片、压缩包等格式。资源须为本人学习相关内容，不得包含违法、侵权内容，否则将被删除并扣除积分。' },
  { id: 'r2', cat: 'resource', q: '下载资源需要多少积分？', a: '每次下载消耗 5 积分。积分不足时可通过上传优质资源（+10）、回答问题（+5）、每日签到（+10）等方式补充。' },
  { id: 'r3', cat: 'resource', q: '我上传的资源为什么被删除？', a: '资源可能因以下原因被删除：包含违规内容、侵犯版权、文件损坏无法打开、内容与标题严重不符。如有异议，请联系客服申诉。' },
  { id: 'r4', cat: 'resource', q: '如何举报违规资源？', a: '在资源详情页点击右上角「⋯」菜单，选择「举报」，填写举报原因后提交。我们将在 48 小时内审核处理，感谢您维护社区环境。' },
  { id: 'q1', cat: 'qa', q: '如何采纳最佳回答？', a: '问题提出者在对话详情页可将某个回答标记为「最佳答案」。每个问题只能采纳一个答案，采纳后回答者将获得 20 积分奖励，采纳操作不可撤销。' },
  { id: 'q2', cat: 'qa', q: '提问有字数限制吗？', a: '问题标题最多 100 字，详细描述最多 2000 字。建议详细描述背景、已尝试的方法和期望结果，以便获得更精准的回答。' },
  { id: 'q3', cat: 'qa', q: '如何关注感兴趣的话题？', a: '在问答页点击话题标签，进入话题详情页后点击「关注」。关注后，该话题下的新问题将优先出现在您的首页推荐中。' },
  { id: 't1', cat: 'task', q: '如何发布悬赏任务？', a: '点击首页中央「+」按钮，选择「发布任务」，填写任务标题、详细说明、截止时间和积分悬赏金额后提交审核。审核通过后任务将公开显示。' },
  { id: 't2', cat: 'task', q: '任务完成后如何结算积分？', a: '接单者提交完成后，发布者确认验收即可触发积分结算。如发布者 48 小时内未确认，系统将自动完成结算并转账积分。' },
  { id: 't3', cat: 'task', q: '接取任务后可以放弃吗？', a: '可以放弃，但频繁放弃会影响信誉分。信誉分低于 60 分时将限制接单权限。建议在接单前充分评估任务可行性。' },
  { id: 'p1', cat: 'points', q: '积分可以用来做什么？', a: '积分可用于：下载资源（每次 -5）、发布悬赏任务。未来计划支持积分兑换实物奖品、会员特权、校园周边等，敬请期待。' },
  { id: 'p2', cat: 'points', q: '如何快速获取积分？', a: '• 注册首次登录 +100\n• 每日签到 +10\n• 上传审核通过的资源 +10\n• 回答问题 +5\n• 答案被采纳 +20\n• 完成悬赏任务 +悬赏积分\n• 活动签到 +10' },
  { id: 'p3', cat: 'points', q: '积分会过期吗？', a: '积分永不过期，可放心积累。但请注意，违规行为（如发布违规内容、恶意举报等）将导致积分扣除甚至账号封禁。' },
]

const filteredFAQ = computed(() => {
  let list = faqData
  if (activeCat.value !== 'all') {
    list = list.filter(f => f.cat === activeCat.value)
  }
  const q = searchQuery.value.trim().toLowerCase()
  if (q) {
    list = list.filter(f =>
      f.q.toLowerCase().includes(q) || f.a.toLowerCase().includes(q)
    )
  }
  return list
})

const toggle = (id: string) => {
  openId.value = openId.value === id ? null : id
}

const goFeedback = () => {
  uni.navigateTo({ url: '/pages/feedback/index' })
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.help-page {
  min-height: 100vh;
  background: $gray-50;
}

.page-body {
  display: flex;
  flex-direction: column;
}

// ===== Hero =====
.hero-section {
  background: linear-gradient(135deg, $primary 0%, #1D4ED8 60%, #4F46E5 100%);
  padding: 36px 24px 48px;
  text-align: center;
  position: relative;
}

.hero-title {
  display: block;
  font-size: 24px;
  font-weight: $font-weight-bold;
  color: $white;
  margin-bottom: 6px;
  letter-spacing: -0.02em;
}

.hero-sub {
  display: block;
  font-size: 14px;
  color: rgba(255,255,255,0.75);
  margin-bottom: 24px;
}

.search-wrap {
  max-width: 560px;
  margin: 0 auto;
  background: $white;
  border: 2px solid transparent;
  border-radius: 14px;
  padding: 10px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  transition: all 0.2s;

  &.focused {
    border-color: rgba(255,255,255,0.6);
    box-shadow: 0 4px 24px rgba(0,0,0,0.2), 0 0 0 4px rgba(255,255,255,0.15);
  }
}

.search-ico {
  width: 20px;
  height: 20px;
  color: $gray-400;
  flex-shrink: 0;
}

.search-inp {
  flex: 1;
  font-size: 15px;
  color: $gray-900;
  border: none;
  outline: none;
  background: transparent;

  &::placeholder { color: $gray-400; }
}

.search-clear {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $gray-100;
  border-radius: 50%;
  cursor: pointer;
  flex-shrink: 0;
  color: $gray-500;

  &:active { transform: scale(0.9); }
}

// ===== Content =====
.content-container {
  max-width: 680px;
  width: 100%;
  margin: -20px auto 0;
  padding: 0 20px 40px;
}

// ===== Category Chips =====
.cats-scroll {
  width: 100%;
  margin-bottom: 20px;
}

.cats-row {
  display: flex;
  gap: 8px;
  padding: 4px 0 8px;
  white-space: nowrap;
}

.cat-chip {
  display: inline-flex;
  align-items: center;
  padding: 7px 16px;
  background: $white;
  border: 1.5px solid $gray-200;
  border-radius: 20px;
  font-size: 13px;
  font-weight: $font-weight-medium;
  color: $gray-600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);

  &:hover {
    border-color: rgba($primary, 0.4);
    color: $primary;
  }

  &.active {
    background: $primary;
    border-color: $primary;
    color: $white;
    box-shadow: 0 2px 8px rgba($primary, 0.3);
  }
}

// ===== FAQ =====
.faq-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 24px;
}

.faq-item {
  background: $white;
  border: 1.5px solid $gray-100;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);

  &:hover {
    border-color: rgba($primary, 0.25);
    box-shadow: 0 2px 10px rgba($primary, 0.08);
  }

  &.open {
    border-color: rgba($primary, 0.3);
    box-shadow: 0 3px 14px rgba($primary, 0.1);

    .faq-chevron {
      transform: rotate(180deg);
      color: $primary;
    }

    .faq-dot {
      background: $primary;
    }

    .faq-q {
      color: $primary;
    }
  }
}

.faq-header {
  padding: 16px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.faq-q-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.faq-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: $gray-300;
  flex-shrink: 0;
  transition: background 0.2s;
}

.faq-q {
  font-size: 15px;
  font-weight: $font-weight-medium;
  color: $gray-800;
  line-height: 1.4;
  transition: color 0.2s;
}

.faq-chevron {
  flex-shrink: 0;
  color: $gray-400;
  transition: transform 0.25s ease, color 0.2s;
}

.faq-body {
  padding: 0 18px 18px;
  border-top: 1px solid $gray-100;
  padding-top: 14px;
  animation: slideDown 0.2s ease-out;
}

.faq-a {
  font-size: 14px;
  color: $gray-600;
  line-height: 1.75;
  white-space: pre-line;
  display: block;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-6px); }
  to   { opacity: 1; transform: translateY(0); }
}

// ===== Empty State =====
.faq-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 24px;
  gap: 10px;
}

.empty-svg {
  color: $gray-300;
  margin-bottom: 8px;
}

.empty-title {
  font-size: 16px;
  font-weight: $font-weight-medium;
  color: $gray-600;
}

.empty-hint {
  font-size: 13px;
  color: $gray-400;
}

// ===== Contact Card =====
.contact-card {
  background: linear-gradient(135deg, rgba($primary, 0.06), rgba($primary, 0.03));
  border: 1.5px solid rgba($primary, 0.15);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.contact-icon-wrap {
  width: 48px;
  height: 48px;
  background: rgba($primary, 0.1);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $primary;
  flex-shrink: 0;
}

.contact-body {
  flex: 1;
  min-width: 0;
}

.contact-title {
  display: block;
  font-size: 15px;
  font-weight: $font-weight-bold;
  color: $gray-900;
  margin-bottom: 2px;
}

.contact-desc {
  display: block;
  font-size: 12px;
  color: $gray-500;
}

.contact-btn {
  flex-shrink: 0;
  padding: 9px 18px;
  background: $primary;
  color: $white;
  font-size: 13px;
  font-weight: $font-weight-medium;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba($primary, 0.3);

  &:hover { background: darken($primary, 6%); }
  &:active { transform: scale(0.97); }
}

// ===== PC =====
@media (min-width: 768px) {
  .hero-section {
    padding: 56px 40px 64px;
  }

  .hero-title { font-size: 30px; }
  .hero-sub   { font-size: 16px; }

  .content-container {
    padding: 0 32px 60px;
  }

  .faq-item {
    border-radius: 16px;
  }

  .faq-header { padding: 18px 22px; }

  .faq-body {
    padding: 0 22px 18px;
    padding-top: 14px;
  }

  .faq-q { font-size: 16px; }
  .faq-a { font-size: 15px; }
}

// ===== Mobile =====
@include mobile {
  .hero-section { padding: 28px 20px 40px; }
  .content-container { padding: 0 16px 32px; }
}
</style>
