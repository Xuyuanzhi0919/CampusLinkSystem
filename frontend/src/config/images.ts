/**
 * 图片资源配置文件
 * 统一管理所有图片资源，方便后续替换和维护
 * 
 * 使用说明：
 * 1. 当前使用 Unsplash 免费图片API（国内可访问）
 * 2. 如需替换为本地图片，只需修改对应的 URL
 * 3. 建议生产环境使用 CDN 或 OSS 存储
 */

// ==================== Banner 轮播图 ====================
export const BANNER_IMAGES = {
  // Banner 1: 校园学习场景
  campusStudy: 'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=750&h=440&fit=crop&q=80',
  
  // Banner 2: 图书馆/资源共享
  library: 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=750&h=440&fit=crop&q=80',
  
  // Banner 3: 学生讨论/智能问答
  discussion: 'https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=750&h=440&fit=crop&q=80',
}

// ==================== 社团活动海报 ====================
export const ACTIVITY_POSTERS = {
  // 音乐/歌手大赛
  music: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&h=300&fit=crop&q=80',
  
  // 编程/科技
  coding: 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400&h=300&fit=crop&q=80',
  
  // 篮球/体育
  basketball: 'https://images.unsplash.com/photo-1546519638-68e109498ffc?w=400&h=300&fit=crop&q=80',
  
  // 摄影/艺术
  photography: 'https://images.unsplash.com/photo-1452587925148-ce544e77e70d?w=400&h=300&fit=crop&q=80',
  
  // 舞蹈
  dance: 'https://images.unsplash.com/photo-1508700929628-666bc8bd84ea?w=400&h=300&fit=crop&q=80',
  
  // 话剧/戏剧
  drama: 'https://images.unsplash.com/photo-1503095396549-807759245b35?w=400&h=300&fit=crop&q=80',
}

// ==================== 用户头像 ====================
export const USER_AVATARS = {
  // 男生头像
  male1: 'https://i.pravatar.cc/150?img=12',
  male2: 'https://i.pravatar.cc/150?img=13',
  male3: 'https://i.pravatar.cc/150?img=14',
  male4: 'https://i.pravatar.cc/150?img=15',
  male5: 'https://i.pravatar.cc/150?img=33',
  
  // 女生头像
  female1: 'https://i.pravatar.cc/150?img=5',
  female2: 'https://i.pravatar.cc/150?img=9',
  female3: 'https://i.pravatar.cc/150?img=10',
  female4: 'https://i.pravatar.cc/150?img=20',
  female5: 'https://i.pravatar.cc/150?img=23',
  
  // 默认头像
  default: 'https://i.pravatar.cc/150?img=1',
}

// ==================== 资源封面图 ====================
export const RESOURCE_COVERS = {
  // 课件
  courseware: 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=300&h=200&fit=crop&q=80',
  
  // 试题
  exam: 'https://images.unsplash.com/photo-1434030216411-0b793f4b4173?w=300&h=200&fit=crop&q=80',
  
  // 笔记
  notes: 'https://images.unsplash.com/photo-1455390582262-044cdead277a?w=300&h=200&fit=crop&q=80',
  
  // 论文
  paper: 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?w=300&h=200&fit=crop&q=80',
}

// ==================== 占位图 ====================
export const PLACEHOLDER_IMAGES = {
  // 用户头像占位图
  avatar: 'https://via.placeholder.com/150/409EFF/FFFFFF?text=User',
  
  // 图片加载失败占位图
  error: 'https://via.placeholder.com/400x300/CCCCCC/666666?text=Image+Not+Found',
  
  // 通用占位图
  default: 'https://via.placeholder.com/400x300/F5F7FA/999999?text=Loading',
}

// ==================== 工具函数 ====================

/**
 * 获取随机用户头像
 * @param gender 性别：'male' | 'female' | 'random'
 * @returns 头像 URL
 */
export function getRandomAvatar(gender: 'male' | 'female' | 'random' = 'random'): string {
  if (gender === 'male') {
    const maleAvatars = [
      USER_AVATARS.male1,
      USER_AVATARS.male2,
      USER_AVATARS.male3,
      USER_AVATARS.male4,
      USER_AVATARS.male5,
    ]
    return maleAvatars[Math.floor(Math.random() * maleAvatars.length)]
  }
  
  if (gender === 'female') {
    const femaleAvatars = [
      USER_AVATARS.female1,
      USER_AVATARS.female2,
      USER_AVATARS.female3,
      USER_AVATARS.female4,
      USER_AVATARS.female5,
    ]
    return femaleAvatars[Math.floor(Math.random() * femaleAvatars.length)]
  }
  
  // 随机性别
  const allAvatars = [
    ...Object.values(USER_AVATARS).filter(url => url !== USER_AVATARS.default),
  ]
  return allAvatars[Math.floor(Math.random() * allAvatars.length)]
}

/**
 * 获取资源类型对应的封面图
 * @param type 资源类型
 * @returns 封面图 URL
 */
export function getResourceCover(type: 'courseware' | 'exam' | 'notes' | 'paper'): string {
  return RESOURCE_COVERS[type] || PLACEHOLDER_IMAGES.default
}

/**
 * 图片加载错误处理
 * @param event 错误事件
 */
export function handleImageError(event: any) {
  if (event && event.target) {
    event.target.src = PLACEHOLDER_IMAGES.error
  }
}

// ==================== 备用图片源（国内CDN） ====================
// 如果 Unsplash 访问不稳定，可以使用以下备用图片源

export const BACKUP_IMAGES = {
  // 使用 picsum.photos（国内可访问）
  banner1: 'https://picsum.photos/750/440?random=1',
  banner2: 'https://picsum.photos/750/440?random=2',
  banner3: 'https://picsum.photos/750/440?random=3',
  
  // 使用 Lorem Picsum 的特定图片
  activity1: 'https://picsum.photos/id/1/400/300',
  activity2: 'https://picsum.photos/id/20/400/300',
  activity3: 'https://picsum.photos/id/40/400/300',
  activity4: 'https://picsum.photos/id/60/400/300',
}

// ==================== 本地图片路径（可选） ====================
// 如果需要使用本地图片，请将图片放在 static/images/ 目录下
// 然后使用以下路径格式

export const LOCAL_IMAGES = {
  // Banner
  banner1: '/static/images/banner/banner1.jpg',
  banner2: '/static/images/banner/banner2.jpg',
  banner3: '/static/images/banner/banner3.jpg',
  
  // 活动海报
  activity1: '/static/images/activity/music.jpg',
  activity2: '/static/images/activity/coding.jpg',
  activity3: '/static/images/activity/basketball.jpg',
  activity4: '/static/images/activity/photography.jpg',
  
  // 默认头像
  defaultAvatar: '/static/images/avatar/default.png',
}

/**
 * 使用说明：
 * 
 * 1. 在组件中导入：
 *    import { BANNER_IMAGES, USER_AVATARS, getRandomAvatar } from '@/config/images'
 * 
 * 2. 使用示例：
 *    <image :src="BANNER_IMAGES.campusStudy" />
 *    <image :src="getRandomAvatar('male')" />
 * 
 * 3. 如需切换到本地图片：
 *    将 BANNER_IMAGES 等导出改为 LOCAL_IMAGES
 * 
 * 4. 图片优化建议：
 *    - 使用 WebP 格式减小体积
 *    - 使用 CDN 加速访问
 *    - 添加图片懒加载
 *    - 设置合适的图片尺寸
 */

