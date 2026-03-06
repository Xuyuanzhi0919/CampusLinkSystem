/**
 * 表单验证工具函数
 */

/**
 * 敏感词列表
 */
const SENSITIVE_WORDS = [
  // 政治敏感词
  '反动', '暴力', '恐怖', '分裂', '颠覆',
  // 色情低俗词
  '色情', '淫秽', '裸露',
  // 欺诈违法词
  '诈骗', '赌博', '毒品', '传销',
  // 侮辱歧视词
  '傻逼', '操你', '妈的', '草泥马', '智障', '废物', '垃圾人',
  // 广告相关
  '加微信', '扫码领', '免费送', '代考', '代写', '包过'
]

/**
 * 检测内容是否包含敏感词
 * @param content 待检测内容
 * @returns { hasSensitiveWord: boolean, matchedWords: string[] }
 */
export const checkSensitiveWords = (content: string): {
  hasSensitiveWord: boolean
  matchedWords: string[]
} => {
  if (!content) {
    return { hasSensitiveWord: false, matchedWords: [] }
  }

  const matchedWords: string[] = []
  const lowerContent = content.toLowerCase()

  for (const word of SENSITIVE_WORDS) {
    if (lowerContent.includes(word.toLowerCase())) {
      matchedWords.push(word)
    }
  }

  return {
    hasSensitiveWord: matchedWords.length > 0,
    matchedWords
  }
}

/**
 * 检测内容质量
 * @param content 待检测内容
 * @returns { isValid: boolean, message: string }
 */
export const checkContentQuality = (content: string): {
  isValid: boolean
  message: string
} => {
  if (!content || content.trim().length === 0) {
    return {
      isValid: false,
      message: '内容不能为空'
    }
  }

  // 去除空格后的内容
  const trimmedContent = content.trim()

  // 检查是否全是空格、换行符
  if (trimmedContent.length === 0) {
    return {
      isValid: false,
      message: '内容不能全是空格'
    }
  }

  // 检查是否全是标点符号
  const punctuationOnly = /^[\s\p{P}]+$/u.test(trimmedContent)
  if (punctuationOnly) {
    return {
      isValid: false,
      message: '内容不能全是标点符号'
    }
  }

  // 检查重复字符（超过10个相同字符）
  const repeatPattern = /(.)\1{9,}/
  if (repeatPattern.test(content)) {
    return {
      isValid: false,
      message: '内容包含过多重复字符'
    }
  }

  // 检查是否包含过多表情符号（超过30%）
  const emojiPattern = /[\u{1F600}-\u{1F64F}\u{1F300}-\u{1F5FF}\u{1F680}-\u{1F6FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}]/ug
  const emojiMatches = content.match(emojiPattern) || []
  const emojiRatio = emojiMatches.length / trimmedContent.length
  if (emojiRatio > 0.3) {
    return {
      isValid: false,
      message: '表情符号过多，请增加文字描述'
    }
  }

  return {
    isValid: true,
    message: ''
  }
}

/**
 * 验证标题
 * @param title 标题
 * @param minLength 最小长度
 * @param maxLength 最大长度
 */
export const validateTitle = (
  title: string,
  minLength = 5,
  maxLength = 100
): {
  isValid: boolean
  message: string
} => {
  if (!title || title.trim().length === 0) {
    return {
      isValid: false,
      message: '标题不能为空'
    }
  }

  const trimmedTitle = title.trim()

  if (trimmedTitle.length < minLength) {
    return {
      isValid: false,
      message: `标题至少需要${minLength}个字符`
    }
  }

  if (trimmedTitle.length > maxLength) {
    return {
      isValid: false,
      message: `标题不能超过${maxLength}个字符`
    }
  }

  // 检查内容质量
  const qualityCheck = checkContentQuality(trimmedTitle)
  if (!qualityCheck.isValid) {
    return qualityCheck
  }

  // 检查敏感词
  const sensitiveCheck = checkSensitiveWords(trimmedTitle)
  if (sensitiveCheck.hasSensitiveWord) {
    return {
      isValid: false,
      message: `标题包含敏感词: ${sensitiveCheck.matchedWords.join(', ')}`
    }
  }

  return {
    isValid: true,
    message: ''
  }
}

/**
 * 验证内容
 * @param content 内容
 * @param minLength 最小长度
 * @param maxLength 最大长度
 */
export const validateContent = (
  content: string,
  minLength = 10,
  maxLength = 2000
): {
  isValid: boolean
  message: string
} => {
  if (!content || content.trim().length === 0) {
    return {
      isValid: false,
      message: '内容不能为空'
    }
  }

  const trimmedContent = content.trim()

  if (trimmedContent.length < minLength) {
    return {
      isValid: false,
      message: `内容至少需要${minLength}个字符`
    }
  }

  if (trimmedContent.length > maxLength) {
    return {
      isValid: false,
      message: `内容不能超过${maxLength}个字符`
    }
  }

  // 检查内容质量
  const qualityCheck = checkContentQuality(trimmedContent)
  if (!qualityCheck.isValid) {
    return qualityCheck
  }

  // 检查敏感词
  const sensitiveCheck = checkSensitiveWords(trimmedContent)
  if (sensitiveCheck.hasSensitiveWord) {
    return {
      isValid: false,
      message: `内容包含敏感词: ${sensitiveCheck.matchedWords.join(', ')}`
    }
  }

  return {
    isValid: true,
    message: ''
  }
}

/**
 * 验证图片
 * @param images 图片数组
 * @param maxCount 最大数量
 */
export const validateImages = (
  images: string[],
  maxCount = 3
): {
  isValid: boolean
  message: string
} => {
  if (images.length > maxCount) {
    return {
      isValid: false,
      message: `最多上传${maxCount}张图片`
    }
  }

  return {
    isValid: true,
    message: ''
  }
}
