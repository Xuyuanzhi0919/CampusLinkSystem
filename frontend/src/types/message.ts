/**
 * 私信模块类型定义
 */

/**
 * 消息类型枚举
 */
export enum MessageType {
  TEXT = 1,      // 文本消息
  IMAGE = 2,     // 图片消息
  FILE = 3       // 文件消息
}

/**
 * 会话列表项
 */
export interface Conversation {
  userId: number                 // 对方用户ID
  nickname: string               // 对方用户昵称
  avatarUrl: string              // 对方用户头像
  lastMessage: string            // 最后一条消息内容
  lastMessageType: MessageType   // 最后一条消息类型
  lastMessageTime: string        // 最后一条消息时间
  unreadCount: number            // 未读消息数量
}

/**
 * 消息
 */
export interface Message {
  mId: number                    // 消息ID
  senderId: number               // 发送者ID
  senderName: string             // 发送者昵称
  senderAvatar: string           // 发送者头像
  receiverId: number             // 接收者ID
  receiverName: string           // 接收者昵称
  receiverAvatar: string         // 接收者头像
  content: string                // 消息内容
  msgType: MessageType           // 消息类型
  isRead: boolean                // 是否已读
  createdAt: string              // 创建时间
}

/**
 * 发送消息请求
 */
export interface SendMessageRequest {
  receiverId: number             // 接收者ID
  content: string                // 消息内容
  msgType: MessageType           // 消息类型
}
