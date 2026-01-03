/**
 * AI 智能助手服务层
 */

import type { Message, MessageCategory, AIResponse } from '@/types/ai'

// 模拟 AI 响应（后续替换为真实 API）
export async function sendMessage(
  message: string,
  history: Message[],
  category?: MessageCategory
): Promise<AIResponse> {
  // 模拟网络延迟
  await new Promise(resolve => setTimeout(resolve, 800 + Math.random() * 400))

  // 根据分类返回不同的模拟响应
  const responses: Record<MessageCategory, string[]> = {
    study: [
      '根据您的学习进度和当前知识点，我建议您可以从以下几个方面入手：\n\n1. **理论基础巩固**：先确保核心概念理解透彻\n2. **实践练习**：通过习题集加深理解\n3. **知识关联**：将新知识与已学内容建立联系\n\n您想从哪个方向开始呢？',
      '这个知识点确实有难度，让我为您详细解析：\n\n**核心概念**：首先要理解其本质含义\n**应用场景**：在实际问题中如何运用\n**常见误区**：需要特别注意的地方\n\n建议您先看看相关的示例，然后尝试自己动手实践一下。',
      '关于这个问题，我在资源库中找到了几份优质学习资料：\n\n• 《核心概念精讲》- 适合快速掌握要点\n• 《实战案例分析》- 帮助理解应用\n• 《历年真题解析》- 检验学习成果\n\n需要我为您推送这些资料吗？'
    ],
    resource: [
      '我在资源库中找到了以下相关资源：\n\n**课件资料**（3份）\n- 《专业课程讲义》PDF 格式\n- 《知识点速查手册》思维导图\n\n**往年真题**（2套）\n- 2023年期末试卷 + 答案解析\n- 2024年模拟题库\n\n**笔记分享**（5份学霸笔记）\n\n这些资源都经过严格审核，质量有保障。需要我发送下载链接吗？',
      '根据您的需求，我为您筛选了最匹配的资源：\n\n✨ **推荐指数 ⭐⭐⭐⭐⭐**\n《高分突破指南》- 下载量 2.3k，评分 4.8\n内容全面，讲解清晰，配有完整习题\n\n📚 **同类优质资源**\n• 《考前冲刺宝典》- 评分 4.6\n• 《知识点精编》- 评分 4.5\n\n所有资源均可免费下载，部分需要消耗少量积分。',
      '已为您匹配到 12 份相关资源：\n\n**按热度排序**\n1. 《核心知识点总结》- 1.2万次下载\n2. 《历年真题汇编》- 8900次下载\n3. 《学习笔记分享》- 7600次下载\n\n**按时间排序**\n最新上传资源来自本周，内容紧跟最新教学大纲。\n\n您可以直接前往资源广场查看详情，或告诉我您更关注哪一类资源。'
    ],
    tech: [
      '这个技术问题的核心在于理解其底层原理：\n\n**问题分析**\n您遇到的情况通常是由于配置不当或版本兼容性导致的。\n\n**解决方案**\n```javascript\n// 推荐的代码实现方式\nconst solution = () => {\n  // 关键步骤说明\n}\n```\n\n**注意事项**\n⚠ 确保环境变量配置正确\n⚠ 检查依赖版本是否匹配\n\n需要更详细的代码示例吗？',
      '您遇到的报错通常是因为以下原因：\n\n**常见原因排查**\n1. ❌ 依赖包版本冲突\n2. ❌ 配置文件路径错误\n3. ❌ 权限不足\n\n**快速修复步骤**\n```bash\n# 第一步：清理缓存\nnpm clean cache --force\n\n# 第二步：重新安装依赖\nnpm install\n\n# 第三步：验证配置\nnpm run check\n```\n\n如果问题依然存在，请提供完整的错误日志，我帮您深入分析。',
      '关于这个技术栈的选型，我的建议如下：\n\n**方案对比**\n\n| 技术方案 | 优势 | 劣势 | 适用场景 |\n|---------|------|------|----------|\n| 方案A | 性能优 | 学习成本高 | 大型项目 |\n| 方案B | 易上手 | 扩展性一般 | 中小项目 |\n\n**综合建议**\n根据您的项目规模和团队技术栈，我推荐采用方案A，理由是：\n• 长期维护成本更低\n• 社区生态更完善\n• 性能表现更稳定\n\n您想深入了解哪个方案的具体实现？'
    ],
    other: [
      '感谢您的提问！让我为您详细解答：\n\n**核心要点**\n这个问题涉及多个方面，我会从以下角度为您分析：\n\n1️⃣ **基本概念**：首先明确定义和范围\n2️⃣ **关键因素**：影响结果的主要变量\n3️⃣ **实践建议**：如何在实际中应用\n\n如果您需要更具体的指导，欢迎继续追问！',
      '这是一个很好的问题！我理解您关心的核心诉求。\n\n**情况说明**\n根据当前平台规则和实际操作经验：\n\n✅ **可行方案**\n• 途径一：通过正常流程申请\n• 途径二：联系管理员协助处理\n\n⏰ **处理时效**\n通常在 1-3 个工作日内会有反馈\n\n**温馨提示**\n建议您准备好相关材料，以便加快审核进度。',
      '我已经理解您的需求，这里提供一个完整的解决思路：\n\n**Step 1：前期准备**\n□ 明确目标和预期结果\n□ 收集必要的资料和数据\n\n**Step 2：具体实施**\n□ 按照标准流程操作\n□ 记录关键节点和数据\n\n**Step 3：验证优化**\n□ 检查结果是否符合预期\n□ 根据反馈进行调整\n\n整个过程大约需要 X 个步骤，您现在处于哪个阶段呢？'
    ]
  }

  const categoryResponses = responses[category || 'other']
  const randomResponse = categoryResponses[Math.floor(Math.random() * categoryResponses.length)]

  return {
    content: randomResponse,
    category
  }
}

// 保存聊天记录
export function saveChatHistory(messages: Message[]): void {
  try {
    uni.setStorageSync('ai_chat_history', JSON.stringify(messages))
  } catch (error) {
    console.error('保存聊天记录失败:', error)
  }
}

// 加载聊天记录
export function loadChatHistory(): Message[] {
  try {
    const historyStr = uni.getStorageSync('ai_chat_history')
    if (historyStr) {
      return JSON.parse(historyStr)
    }
  } catch (error) {
    console.error('加载聊天记录失败:', error)
  }
  return []
}

// 清空聊天记录
export function clearChatHistory(): void {
  try {
    uni.removeStorageSync('ai_chat_history')
  } catch (error) {
    console.error('清空聊天记录失败:', error)
  }
}
