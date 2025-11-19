# 评论功能集成指南

## 组件路径
- 评论列表组件：`src/components/comment/CommentList.vue`
- 类型定义：`src/types/comment.ts`
- API 服务：`src/services/comment.ts`

## 使用示例

### 1. 在资源详情页中使用

```vue
<template>
  <view class="resource-detail-page">
    <!-- 资源信息 -->
    <view class="resource-info">
      <!-- ... 资源详情内容 ... -->
    </view>

    <!-- 评论区 -->
    <view class="comment-section">
      <view class="section-title">评论区</view>
      <CommentList :resourceId="resourceId" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CommentList from '@/components/comment/CommentList.vue'

// 从路由参数获取资源ID
const resourceId = ref(Number(uni.getStorageSync('currentResourceId')))
</script>

<style scoped lang="scss">
.resource-detail-page {
  min-height: 100vh;
  background: #F5F5F5;
}

.resource-info {
  background: #FFFFFF;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.comment-section {
  background: #FFFFFF;
}

.section-title {
  padding: 24rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
  border-bottom: 1rpx solid #F0F0F0;
}
</style>
```

### 2. 在问答详情页中使用

```vue
<template>
  <view class="question-detail-page">
    <!-- 问题内容 -->
    <view class="question-content">
      <!-- ... 问题详情 ... -->
    </view>

    <!-- 答案列表 -->
    <view class="answers-section">
      <!-- ... 答案列表 ... -->
    </view>

    <!-- 评论讨论 -->
    <view class="comment-section">
      <view class="section-title">讨论区</view>
      <CommentList :resourceId="questionId" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CommentList from '@/components/comment/CommentList.vue'

const questionId = ref(1) // 实际应从路由获取
</script>
```

## 组件功能特性

### ✅ 已实现功能
1. **发表评论** - 支持发表一级评论
2. **回复评论** - 支持二级回复（不支持三级）
3. **删除评论** - 只能删除自己的评论
4. **分页加载** - 支持加载更多
5. **时间显示** - 智能时间格式化（刚刚/X分钟前/X小时前/X天前）
6. **登录校验** - 未登录时引导登录
7. **权限控制** - 只有评论作者可以删除
8. **实时更新** - 操作后自动刷新列表
9. **字数限制** - 评论内容最多500字
10. **加载状态** - 显示加载和空状态

### 🎨 UI 特点
- 简洁清爽的卡片式设计
- 渐变色按钮（橙色主题）
- 圆形头像
- 层级缩进显示回复
- 淡灰色背景区分回复区域

## API 接口

### 后端接口（已实现）
- `POST /resource/{id}/comments` - 添加评论/回复
- `GET /resource/{id}/comments` - 获取评论列表
- `DELETE /resource/comments/{commentId}` - 删除评论

### 前端 API 服务
```typescript
import { getResourceComments, addComment, deleteComment } from '@/services/comment'

// 获取评论
const comments = await getResourceComments(resourceId, { page: 1, pageSize: 10 })

// 添加评论
await addComment(resourceId, { content: '这是一条评论' })

// 添加回复
await addComment(resourceId, { parentId: 123, content: '这是一条回复' })

// 删除评论
await deleteComment(commentId)
```

## 数据库表结构

```sql
CREATE TABLE `resource_comment` (
  `comment_id` bigint NOT NULL AUTO_INCREMENT,
  `resource_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `parent_id` bigint DEFAULT NULL,
  `content` text NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`comment_id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 注意事项

1. **权限要求**：发表评论和回复需要登录
2. **评论层级**：只支持两级评论（评论 + 回复），不支持三级嵌套
3. **删除限制**：
   - 只能删除自己的评论
   - 删除一级评论会同时删除所有回复
4. **内容限制**：评论内容长度 1-500 字符
5. **分页参数**：默认每页 10 条，可自定义

## 自定义样式

组件样式已通过 scoped 限定作用域，如需自定义，可以：

1. **修改主题色**：在组件内修改渐变色值
2. **调整间距**：修改 padding、margin 值
3. **更改字体大小**：修改 font-size 值
4. **自定义头像**：传入 defaultAvatar 属性

## 后续优化建议

1. 添加点赞功能
2. 支持 @提及用户
3. 支持表情符号
4. 图片/文件附件支持
5. 评论举报功能
6. 热门评论排序
7. 评论搜索功能
