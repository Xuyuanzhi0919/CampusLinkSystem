/**
 * Emoji 表情配置
 * 使用 Unicode 原生表情,支持所有平台
 */

export interface EmojiItem {
  emoji: string
  label: string
  category: string
}

/**
 * 表情分类
 */
export enum EmojiCategory {
  PEOPLE = 'people',      // 表情与人物
  NATURE = 'nature',      // 动物与自然
  FOOD = 'food',          // 食物与饮料
  ACTIVITY = 'activity',  // 活动
  TRAVEL = 'travel',      // 旅行与地点
  OBJECTS = 'objects',    // 物品
  SYMBOLS = 'symbols',    // 符号
  FLAGS = 'flags'         // 旗帜
}

/**
 * 表情数据
 */
export const emojiList: EmojiItem[] = [
  // 😀 表情与人物
  { emoji: '😀', label: '笑脸', category: EmojiCategory.PEOPLE },
  { emoji: '😃', label: '开心', category: EmojiCategory.PEOPLE },
  { emoji: '😄', label: '大笑', category: EmojiCategory.PEOPLE },
  { emoji: '😁', label: '露齿笑', category: EmojiCategory.PEOPLE },
  { emoji: '😆', label: '眯眼笑', category: EmojiCategory.PEOPLE },
  { emoji: '😅', label: '苦笑', category: EmojiCategory.PEOPLE },
  { emoji: '🤣', label: '笑哭', category: EmojiCategory.PEOPLE },
  { emoji: '😂', label: '喜极而泣', category: EmojiCategory.PEOPLE },
  { emoji: '🙂', label: '微笑', category: EmojiCategory.PEOPLE },
  { emoji: '🙃', label: '倒脸', category: EmojiCategory.PEOPLE },
  { emoji: '😉', label: '眨眼', category: EmojiCategory.PEOPLE },
  { emoji: '😊', label: '害羞', category: EmojiCategory.PEOPLE },
  { emoji: '😇', label: '天使', category: EmojiCategory.PEOPLE },
  { emoji: '🥰', label: '爱心笑脸', category: EmojiCategory.PEOPLE },
  { emoji: '😍', label: '花痴', category: EmojiCategory.PEOPLE },
  { emoji: '🤩', label: '星星眼', category: EmojiCategory.PEOPLE },
  { emoji: '😘', label: '飞吻', category: EmojiCategory.PEOPLE },
  { emoji: '😗', label: '亲亲', category: EmojiCategory.PEOPLE },
  { emoji: '😚', label: '害羞亲亲', category: EmojiCategory.PEOPLE },
  { emoji: '😙', label: '微笑亲亲', category: EmojiCategory.PEOPLE },
  { emoji: '🥲', label: '含泪微笑', category: EmojiCategory.PEOPLE },
  { emoji: '😋', label: '美味', category: EmojiCategory.PEOPLE },
  { emoji: '😛', label: '吐舌', category: EmojiCategory.PEOPLE },
  { emoji: '😜', label: '眨眼吐舌', category: EmojiCategory.PEOPLE },
  { emoji: '🤪', label: '疯狂', category: EmojiCategory.PEOPLE },
  { emoji: '😝', label: '调皮', category: EmojiCategory.PEOPLE },
  { emoji: '🤑', label: '发财', category: EmojiCategory.PEOPLE },
  { emoji: '🤗', label: '拥抱', category: EmojiCategory.PEOPLE },
  { emoji: '🤭', label: '捂嘴笑', category: EmojiCategory.PEOPLE },
  { emoji: '🤫', label: '嘘', category: EmojiCategory.PEOPLE },
  { emoji: '🤔', label: '思考', category: EmojiCategory.PEOPLE },
  { emoji: '🤐', label: '闭嘴', category: EmojiCategory.PEOPLE },
  { emoji: '🤨', label: '质疑', category: EmojiCategory.PEOPLE },
  { emoji: '😐', label: '面无表情', category: EmojiCategory.PEOPLE },
  { emoji: '😑', label: '无语', category: EmojiCategory.PEOPLE },
  { emoji: '😶', label: '无言以对', category: EmojiCategory.PEOPLE },
  { emoji: '😏', label: '得意', category: EmojiCategory.PEOPLE },
  { emoji: '😒', label: '不爽', category: EmojiCategory.PEOPLE },
  { emoji: '🙄', label: '翻白眼', category: EmojiCategory.PEOPLE },
  { emoji: '😬', label: '龇牙咧嘴', category: EmojiCategory.PEOPLE },
  { emoji: '🤥', label: '说谎', category: EmojiCategory.PEOPLE },
  { emoji: '😌', label: '如释重负', category: EmojiCategory.PEOPLE },
  { emoji: '😔', label: '沉思', category: EmojiCategory.PEOPLE },
  { emoji: '😪', label: '困', category: EmojiCategory.PEOPLE },
  { emoji: '🤤', label: '流口水', category: EmojiCategory.PEOPLE },
  { emoji: '😴', label: '睡觉', category: EmojiCategory.PEOPLE },
  { emoji: '😷', label: '口罩', category: EmojiCategory.PEOPLE },
  { emoji: '🤒', label: '生病', category: EmojiCategory.PEOPLE },
  { emoji: '🤕', label: '受伤', category: EmojiCategory.PEOPLE },
  { emoji: '🤢', label: '恶心', category: EmojiCategory.PEOPLE },
  { emoji: '🤮', label: '呕吐', category: EmojiCategory.PEOPLE },
  { emoji: '🤧', label: '打喷嚏', category: EmojiCategory.PEOPLE },
  { emoji: '🥵', label: '热', category: EmojiCategory.PEOPLE },
  { emoji: '🥶', label: '冷', category: EmojiCategory.PEOPLE },
  { emoji: '😵', label: '晕', category: EmojiCategory.PEOPLE },
  { emoji: '🤯', label: '爆炸', category: EmojiCategory.PEOPLE },
  { emoji: '🤠', label: '牛仔', category: EmojiCategory.PEOPLE },
  { emoji: '🥳', label: '庆祝', category: EmojiCategory.PEOPLE },
  { emoji: '😎', label: '墨镜', category: EmojiCategory.PEOPLE },
  { emoji: '🤓', label: '书呆子', category: EmojiCategory.PEOPLE },
  { emoji: '🧐', label: '单片眼镜', category: EmojiCategory.PEOPLE },
  { emoji: '😕', label: '困惑', category: EmojiCategory.PEOPLE },
  { emoji: '😟', label: '担心', category: EmojiCategory.PEOPLE },
  { emoji: '🙁', label: '皱眉', category: EmojiCategory.PEOPLE },
  { emoji: '☹️', label: '不开心', category: EmojiCategory.PEOPLE },
  { emoji: '😮', label: '惊讶', category: EmojiCategory.PEOPLE },
  { emoji: '😯', label: '震惊', category: EmojiCategory.PEOPLE },
  { emoji: '😲', label: '吃惊', category: EmojiCategory.PEOPLE },
  { emoji: '😳', label: '脸红', category: EmojiCategory.PEOPLE },
  { emoji: '🥺', label: '恳求', category: EmojiCategory.PEOPLE },
  { emoji: '😦', label: '失望', category: EmojiCategory.PEOPLE },
  { emoji: '😧', label: '痛苦', category: EmojiCategory.PEOPLE },
  { emoji: '😨', label: '害怕', category: EmojiCategory.PEOPLE },
  { emoji: '😰', label: '焦虑', category: EmojiCategory.PEOPLE },
  { emoji: '😥', label: '伤心', category: EmojiCategory.PEOPLE },
  { emoji: '😢', label: '哭', category: EmojiCategory.PEOPLE },
  { emoji: '😭', label: '大哭', category: EmojiCategory.PEOPLE },
  { emoji: '😱', label: '尖叫', category: EmojiCategory.PEOPLE },
  { emoji: '😖', label: '困扰', category: EmojiCategory.PEOPLE },
  { emoji: '😣', label: '坚持', category: EmojiCategory.PEOPLE },
  { emoji: '😞', label: '失落', category: EmojiCategory.PEOPLE },
  { emoji: '😓', label: '汗', category: EmojiCategory.PEOPLE },
  { emoji: '😩', label: '疲惫', category: EmojiCategory.PEOPLE },
  { emoji: '😫', label: '累', category: EmojiCategory.PEOPLE },
  { emoji: '🥱', label: '哈欠', category: EmojiCategory.PEOPLE },
  { emoji: '😤', label: '得意', category: EmojiCategory.PEOPLE },
  { emoji: '😡', label: '生气', category: EmojiCategory.PEOPLE },
  { emoji: '😠', label: '愤怒', category: EmojiCategory.PEOPLE },
  { emoji: '🤬', label: '骂人', category: EmojiCategory.PEOPLE },
  { emoji: '👍', label: '点赞', category: EmojiCategory.PEOPLE },
  { emoji: '👎', label: '踩', category: EmojiCategory.PEOPLE },
  { emoji: '👌', label: 'OK', category: EmojiCategory.PEOPLE },
  { emoji: '✌️', label: '胜利', category: EmojiCategory.PEOPLE },
  { emoji: '🤞', label: '祈祷', category: EmojiCategory.PEOPLE },
  { emoji: '🤟', label: '爱你', category: EmojiCategory.PEOPLE },
  { emoji: '🤘', label: '摇滚', category: EmojiCategory.PEOPLE },
  { emoji: '🤙', label: '打电话', category: EmojiCategory.PEOPLE },
  { emoji: '👏', label: '鼓掌', category: EmojiCategory.PEOPLE },
  { emoji: '🙌', label: '举手', category: EmojiCategory.PEOPLE },
  { emoji: '👐', label: '张开手', category: EmojiCategory.PEOPLE },
  { emoji: '🤲', label: '捧', category: EmojiCategory.PEOPLE },
  { emoji: '🤝', label: '握手', category: EmojiCategory.PEOPLE },
  { emoji: '🙏', label: '祈祷', category: EmojiCategory.PEOPLE },

  // ❤️ 符号
  { emoji: '❤️', label: '爱心', category: EmojiCategory.SYMBOLS },
  { emoji: '🧡', label: '橙心', category: EmojiCategory.SYMBOLS },
  { emoji: '💛', label: '黄心', category: EmojiCategory.SYMBOLS },
  { emoji: '💚', label: '绿心', category: EmojiCategory.SYMBOLS },
  { emoji: '💙', label: '蓝心', category: EmojiCategory.SYMBOLS },
  { emoji: '💜', label: '紫心', category: EmojiCategory.SYMBOLS },
  { emoji: '🖤', label: '黑心', category: EmojiCategory.SYMBOLS },
  { emoji: '🤍', label: '白心', category: EmojiCategory.SYMBOLS },
  { emoji: '🤎', label: '棕心', category: EmojiCategory.SYMBOLS },
  { emoji: '💔', label: '心碎', category: EmojiCategory.SYMBOLS },
  { emoji: '💕', label: '两颗心', category: EmojiCategory.SYMBOLS },
  { emoji: '💞', label: '旋转的心', category: EmojiCategory.SYMBOLS },
  { emoji: '💓', label: '心跳', category: EmojiCategory.SYMBOLS },
  { emoji: '💗', label: '成长的心', category: EmojiCategory.SYMBOLS },
  { emoji: '💖', label: '闪亮的心', category: EmojiCategory.SYMBOLS },
  { emoji: '💘', label: '丘比特之箭', category: EmojiCategory.SYMBOLS },
  { emoji: '💝', label: '爱心礼盒', category: EmojiCategory.SYMBOLS },
  { emoji: '💟', label: '心形装饰', category: EmojiCategory.SYMBOLS },
  { emoji: '✨', label: '闪光', category: EmojiCategory.SYMBOLS },
  { emoji: '⭐', label: '星星', category: EmojiCategory.SYMBOLS },
  { emoji: '🌟', label: '闪亮的星', category: EmojiCategory.SYMBOLS },
  { emoji: '💫', label: '眩晕', category: EmojiCategory.SYMBOLS },
  { emoji: '💥', label: '爆炸', category: EmojiCategory.SYMBOLS },
  { emoji: '💢', label: '怒', category: EmojiCategory.SYMBOLS },
  { emoji: '💦', label: '汗滴', category: EmojiCategory.SYMBOLS },
  { emoji: '💨', label: '快跑', category: EmojiCategory.SYMBOLS },
  { emoji: '🔥', label: '火', category: EmojiCategory.SYMBOLS },
  { emoji: '💯', label: '100分', category: EmojiCategory.SYMBOLS },

  // 🐶 动物与自然
  { emoji: '🐶', label: '狗', category: EmojiCategory.NATURE },
  { emoji: '🐱', label: '猫', category: EmojiCategory.NATURE },
  { emoji: '🐭', label: '老鼠', category: EmojiCategory.NATURE },
  { emoji: '🐹', label: '仓鼠', category: EmojiCategory.NATURE },
  { emoji: '🐰', label: '兔子', category: EmojiCategory.NATURE },
  { emoji: '🦊', label: '狐狸', category: EmojiCategory.NATURE },
  { emoji: '🐻', label: '熊', category: EmojiCategory.NATURE },
  { emoji: '🐼', label: '熊猫', category: EmojiCategory.NATURE },
  { emoji: '🐨', label: '考拉', category: EmojiCategory.NATURE },
  { emoji: '🐯', label: '老虎', category: EmojiCategory.NATURE },
  { emoji: '🦁', label: '狮子', category: EmojiCategory.NATURE },
  { emoji: '🐮', label: '牛', category: EmojiCategory.NATURE },
  { emoji: '🐷', label: '猪', category: EmojiCategory.NATURE },
  { emoji: '🐸', label: '青蛙', category: EmojiCategory.NATURE },
  { emoji: '🐵', label: '猴子', category: EmojiCategory.NATURE },
  { emoji: '🐔', label: '鸡', category: EmojiCategory.NATURE },
  { emoji: '🐧', label: '企鹅', category: EmojiCategory.NATURE },
  { emoji: '🐦', label: '鸟', category: EmojiCategory.NATURE },
  { emoji: '🐤', label: '小鸡', category: EmojiCategory.NATURE },
  { emoji: '🦆', label: '鸭子', category: EmojiCategory.NATURE },

  // 🍎 食物与饮料
  { emoji: '🍎', label: '苹果', category: EmojiCategory.FOOD },
  { emoji: '🍊', label: '橙子', category: EmojiCategory.FOOD },
  { emoji: '🍋', label: '柠檬', category: EmojiCategory.FOOD },
  { emoji: '🍌', label: '香蕉', category: EmojiCategory.FOOD },
  { emoji: '🍉', label: '西瓜', category: EmojiCategory.FOOD },
  { emoji: '🍇', label: '葡萄', category: EmojiCategory.FOOD },
  { emoji: '🍓', label: '草莓', category: EmojiCategory.FOOD },
  { emoji: '🍒', label: '樱桃', category: EmojiCategory.FOOD },
  { emoji: '🍑', label: '桃子', category: EmojiCategory.FOOD },
  { emoji: '🥭', label: '芒果', category: EmojiCategory.FOOD },
  { emoji: '🍍', label: '菠萝', category: EmojiCategory.FOOD },
  { emoji: '🥥', label: '椰子', category: EmojiCategory.FOOD },
  { emoji: '🥝', label: '猕猴桃', category: EmojiCategory.FOOD },
  { emoji: '🍅', label: '西红柿', category: EmojiCategory.FOOD },
  { emoji: '🥑', label: '牛油果', category: EmojiCategory.FOOD },
  { emoji: '🍔', label: '汉堡', category: EmojiCategory.FOOD },
  { emoji: '🍕', label: '披萨', category: EmojiCategory.FOOD },
  { emoji: '🍿', label: '爆米花', category: EmojiCategory.FOOD },
  { emoji: '☕', label: '咖啡', category: EmojiCategory.FOOD },
  { emoji: '🍺', label: '啤酒', category: EmojiCategory.FOOD },
  { emoji: '🍰', label: '蛋糕', category: EmojiCategory.FOOD },
  { emoji: '🎂', label: '生日蛋糕', category: EmojiCategory.FOOD },
  { emoji: '🍭', label: '棒棒糖', category: EmojiCategory.FOOD },
  { emoji: '🍬', label: '糖果', category: EmojiCategory.FOOD },

  // ⚽ 活动
  { emoji: '⚽', label: '足球', category: EmojiCategory.ACTIVITY },
  { emoji: '🏀', label: '篮球', category: EmojiCategory.ACTIVITY },
  { emoji: '🏈', label: '橄榄球', category: EmojiCategory.ACTIVITY },
  { emoji: '⚾', label: '棒球', category: EmojiCategory.ACTIVITY },
  { emoji: '🎾', label: '网球', category: EmojiCategory.ACTIVITY },
  { emoji: '🏐', label: '排球', category: EmojiCategory.ACTIVITY },
  { emoji: '🏓', label: '乒乓球', category: EmojiCategory.ACTIVITY },
  { emoji: '🎮', label: '游戏', category: EmojiCategory.ACTIVITY },
  { emoji: '🎯', label: '飞镖', category: EmojiCategory.ACTIVITY },
  { emoji: '🎲', label: '骰子', category: EmojiCategory.ACTIVITY },
  { emoji: '🎭', label: '戏剧', category: EmojiCategory.ACTIVITY },
  { emoji: '🎨', label: '艺术', category: EmojiCategory.ACTIVITY },
  { emoji: '🎬', label: '电影', category: EmojiCategory.ACTIVITY },
  { emoji: '🎤', label: '麦克风', category: EmojiCategory.ACTIVITY },
  { emoji: '🎧', label: '耳机', category: EmojiCategory.ACTIVITY },
  { emoji: '🎵', label: '音乐', category: EmojiCategory.ACTIVITY },
  { emoji: '🎸', label: '吉他', category: EmojiCategory.ACTIVITY },
  { emoji: '🎹', label: '钢琴', category: EmojiCategory.ACTIVITY },

  // 📚 物品
  { emoji: '📱', label: '手机', category: EmojiCategory.OBJECTS },
  { emoji: '💻', label: '电脑', category: EmojiCategory.OBJECTS },
  { emoji: '⌨️', label: '键盘', category: EmojiCategory.OBJECTS },
  { emoji: '🖱️', label: '鼠标', category: EmojiCategory.OBJECTS },
  { emoji: '🖥️', label: '台式机', category: EmojiCategory.OBJECTS },
  { emoji: '📷', label: '相机', category: EmojiCategory.OBJECTS },
  { emoji: '📸', label: '闪光灯相机', category: EmojiCategory.OBJECTS },
  { emoji: '📹', label: '摄像机', category: EmojiCategory.OBJECTS },
  { emoji: '📺', label: '电视', category: EmojiCategory.OBJECTS },
  { emoji: '📻', label: '收音机', category: EmojiCategory.OBJECTS },
  { emoji: '⏰', label: '闹钟', category: EmojiCategory.OBJECTS },
  { emoji: '⌚', label: '手表', category: EmojiCategory.OBJECTS },
  { emoji: '📚', label: '书籍', category: EmojiCategory.OBJECTS },
  { emoji: '📖', label: '书', category: EmojiCategory.OBJECTS },
  { emoji: '📝', label: '笔记', category: EmojiCategory.OBJECTS },
  { emoji: '✏️', label: '铅笔', category: EmojiCategory.OBJECTS },
  { emoji: '✉️', label: '信封', category: EmojiCategory.OBJECTS },
  { emoji: '📮', label: '邮筒', category: EmojiCategory.OBJECTS },
  { emoji: '🎁', label: '礼物', category: EmojiCategory.OBJECTS },
  { emoji: '🎈', label: '气球', category: EmojiCategory.OBJECTS },
  { emoji: '🎀', label: '蝴蝶结', category: EmojiCategory.OBJECTS },
]

/**
 * 按分类获取表情
 */
export const getEmojiByCategory = (category: EmojiCategory): EmojiItem[] => {
  return emojiList.filter(item => item.category === category)
}

/**
 * 获取常用表情 (前30个)
 */
export const getFrequentEmoji = (): EmojiItem[] => {
  return emojiList.slice(0, 30)
}

/**
 * 分类标签映射
 */
export const categoryLabels: Record<EmojiCategory, { label: string; icon: string }> = {
  [EmojiCategory.PEOPLE]: { label: '表情', icon: '😀' },
  [EmojiCategory.NATURE]: { label: '自然', icon: '🐶' },
  [EmojiCategory.FOOD]: { label: '食物', icon: '🍎' },
  [EmojiCategory.ACTIVITY]: { label: '活动', icon: '⚽' },
  [EmojiCategory.TRAVEL]: { label: '旅行', icon: '✈️' },
  [EmojiCategory.OBJECTS]: { label: '物品', icon: '📱' },
  [EmojiCategory.SYMBOLS]: { label: '符号', icon: '❤️' },
  [EmojiCategory.FLAGS]: { label: '旗帜', icon: '🚩' },
}
