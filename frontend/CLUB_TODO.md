# 社团广场优化 - 待办事项清单

## ✅ 已完成的优化 (2024-12)

### 高优先级 (P0)
- [x] 修复右侧栏数据逻辑 - 分离全量数据和筛选数据
- [x] 将搜索/分类/排序逻辑移到后端处理
- [x] 修复活动数量统计不一致问题
- [x] 实现完整的分页加载机制

### 中等优先级 (P1)
- [x] 添加滑动加载更多功能
- [x] 优化分类计数性能 (O(n*m) → O(n))
- [x] 添加下拉刷新功能
- [x] 添加错误重试机制

### 低优先级 (P2)
- [x] 清理 mock 数据 (userJoinedCount 等)

---

## 🔄 需要后端支持的字段

### ClubResponse DTO 需要新增的字段

#### 1. 官方认证标识 (高优先级)
```java
/**
 * 是否官方/校级社团
 */
private Boolean isOfficial;
```
**用途**: 右侧栏"官方社团"筛选,当前使用临时逻辑(取前2个)

**临时方案**: `frontend/src/pages/club/list.vue:579`
```typescript
// TODO: 后端应返回 isOfficial 字段
officialClubs.value = allClubs.value.slice(0, 2)
```

---

#### 2. 最近活动时间 (高优先级)
```java
/**
 * 最近一次活动时间
 */
private LocalDateTime lastActivityAt;
```
**用途**:
- 活跃度判断 (是否显示"活跃"标签)
- 显示"本周有活动"/"本月有活动"等时间信号

**临时方案**: `frontend/src/pages/club/list.vue:472-488`
```typescript
// TODO: 后端应返回 lastActivityAt 字段
// 临时逻辑: 使用创建时间模拟
const getRecentActivityTime = (club: ClubItem): string => {
  const createdDate = new Date(club.createdAt)
  const now = new Date()
  const diffDays = Math.floor((now.getTime() - createdDate.getTime()) / (1000 * 60 * 60 * 24))

  if (diffDays < 7) return '本周有活动'
  if (diffDays < 30) return '本月有活动'
  if (diffDays < 90) return '近期有活动'

  return '' // 超过3个月不显示
}
```

**建议实现**:
```java
// ClubService.java
private ClubResponse convertToResponse(Club club, Long userId) {
    // ... 现有代码 ...

    // 查询最近活动时间
    LambdaQueryWrapper<Activity> activityWrapper = new LambdaQueryWrapper<>();
    activityWrapper.eq(Activity::getClubId, club.getClubId())
                   .orderByDesc(Activity::getCreatedAt)
                   .last("LIMIT 1");
    Activity latestActivity = activityMapper.selectOne(activityWrapper);
    response.setLastActivityAt(latestActivity != null ? latestActivity.getCreatedAt() : null);

    return response;
}
```

---

#### 3. 用户加入位置 (中等优先级)
```java
/**
 * 用户加入该社团的顺序 (第几位成员)
 * 仅当 isMember = true 时有值
 */
private Integer joinPosition;
```
**用途**: 社团详情页显示"你是第 X 位加入的成员"

**临时方案**: 当前使用 `memberCount` 直接显示
```typescript
// frontend/src/pages/club/detail.vue:423-428
const memberPosition = computed(() => {
  if (!isMember.value || !club.value?.memberCount) return 0
  // 简化:显示当前成员总数作为位置
  return club.value.memberCount
})
```

**建议实现**:
```java
// ClubService.java
private ClubResponse convertToResponse(Club club, Long userId) {
    // ... 现有代码 ...

    if (response.getIsMember()) {
        // 查询用户加入时的成员序号
        LambdaQueryWrapper<ClubMember> positionWrapper = new LambdaQueryWrapper<>();
        positionWrapper.eq(ClubMember::getClubId, club.getClubId())
                       .le(ClubMember::getJoinedAt, userMember.getJoinedAt());
        Long position = clubMemberMapper.selectCount(positionWrapper);
        response.setJoinPosition(position.intValue());
    }

    return response;
}
```

---

#### 4. 社团分类字段 (中等优先级)
```java
/**
 * 社团分类 (技术/学习/体育/艺术/公益/兴趣)
 */
private String category;
```
**用途**: 分类筛选和显示分类标签

**临时方案**: `frontend/src/pages/club/list.vue:457-466`
```typescript
// TODO: 后端应在 Club 实体类中添加 category 字段
const getClubCategory = (club: ClubItem): string => {
  const name = club.clubName.toLowerCase()

  if (name.includes('计算机') || name.includes('编程') || name.includes('技术')) return '技术'
  if (name.includes('学习') || name.includes('科学') || name.includes('研究')) return '学习'
  if (name.includes('体育') || name.includes('篮球') || name.includes('足球')) return '体育'
  if (name.includes('音乐') || name.includes('美术') || name.includes('艺术')) return '艺术'
  if (name.includes('志愿') || name.includes('公益') || name.includes('服务')) return '公益'

  return '兴趣' // 默认标签
}
```

**建议实现**:
1. 在 `club` 表中添加 `category` 字段
2. 创建社团时要求用户选择分类
3. 在 `ClubResponse` 中返回该字段
4. 前端直接使用后端返回的分类,移除推断逻辑

---

## 🚀 待实现的功能

### 1. 取消加入申请 (中等优先级)
**位置**: `frontend/src/pages/club/list.vue:724`
```typescript
// TODO: 调用取消申请接口
uni.showToast({
  title: '申请已取消',
  icon: 'success'
})
club.isPending = false
```

**需要的 API**: `DELETE /api/v1/club/{clubId}/member/application`

---

### 2. 加入社团 (高优先级)
**位置**: `frontend/src/pages/club/list.vue:741`
```typescript
// TODO: 调用加入社团接口
uni.showToast({
  title: '申请已提交',
  icon: 'success'
})
club.isPending = true
```

**需要的 API**: `POST /api/v1/club/{clubId}/member/join`

---

### 3. 推荐社团功能 (低优先级)
**位置**: `frontend/src/pages/club/list.vue:657`
```typescript
// TODO: 后续实现推荐逻辑
// uni.navigateTo({ url: '/pages/club/recommend' })
```

**建议实现**:
- 基于用户已加入的社团推荐相似社团
- 基于学校和专业推荐相关社团
- 基于活跃度和成员数推荐热门社团

---

### 4. 创建社团功能 (中等优先级)
**位置**: `frontend/src/pages/club/list.vue:669`
```typescript
// TODO: 后续实现创建社团逻辑
// uni.navigateTo({ url: '/pages/club/create' })
```

**需要的页面**: `/pages/club/create.vue`

**需要的 API**: `POST /api/v1/club`

---

## 📊 性能优化建议

### 已完成
- ✅ 分类计数优化 (O(n*m) → O(n))
- ✅ 右侧栏数据缓存 (只加载一次)
- ✅ 后端分页 + 前端无限滚动

### 可选优化
- [ ] 图片懒加载 (使用 LazyImage 组件)
- [ ] 虚拟列表 (社团数量 >100 时)
- [ ] 骨架屏优化 (更接近真实卡片布局)

---

## 🎨 UI 优化建议

### 已完成
- ✅ 错误状态 UI (带重试按钮)
- ✅ 加载更多提示
- ✅ 空状态优化

### 可选优化
- [ ] 搜索历史记录
- [ ] 分类 Tabs 横向滚动优化 (添加滚动指示器)
- [ ] 社团卡片骨架屏动画效果

---

## 📝 代码质量优化

### 待清理的代码
1. **已加入社团数量统计** - 应从用户信息中获取
   ```typescript
   // 当前实现
   const joinedClubs = computed(() => {
     return clubs.value.filter(club => club.isMember === true)
   })

   // 建议优化: 后端返回用户的加入社团列表
   // GET /api/v1/user/joined-clubs
   ```

2. **社团详情页 mock 数据** - 动态、资料、相关社团
   - 位置: `frontend/src/pages/club/detail.vue:497-522`
   - 需要的 API:
     - `GET /api/v1/club/{clubId}/feeds` - 社团动态
     - `GET /api/v1/club/{clubId}/resources` - 社团资料
     - `GET /api/v1/club/{clubId}/related` - 相关社团

---

## 🔐 权限和安全

### 需要后端验证的操作
- [x] 用户是否已登录 (加入社团)
- [ ] 用户是否已加入该社团 (访问社团资料)
- [ ] 用户是否是社团管理员 (编辑社团信息)
- [ ] 防止重复加入社团
- [ ] 防止恶意刷接口 (限流)

---

## 📅 优先级排序

### 本周建议完成 (P0)
1. 添加 `isOfficial` 字段 (影响右侧栏展示)
2. 添加 `lastActivityAt` 字段 (影响活跃度判断)
3. 实现加入社团 API (核心功能)

### 下周建议完成 (P1)
1. 添加 `category` 字段 (优化分类筛选)
2. 实现取消申请 API
3. 添加 `joinPosition` 字段 (优化用户体验)

### 月度计划 (P2)
1. 社团动态/资料/相关社团 API
2. 创建社团功能
3. 推荐社团算法

---

## 📞 联系方式

如有问题或建议,请联系前端开发团队。

**最后更新**: 2024-12-17
**文档维护**: Claude Code
