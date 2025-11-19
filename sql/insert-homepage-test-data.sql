-- 首页测试数据插入脚本
-- 用于测试 校园公告卡片 和 社团动态卡片
USE `campuslink`;

-- ============================================
-- 1. 插入测试用户（如果不存在）
-- ============================================
INSERT IGNORE INTO `user` (`u_id`, `username`, `password`, `nickname`, `email`, `phone`, `avatar`, `role`, `school_id`, `points`, `status`, `created_at`, `updated_at`)
VALUES
(1, 'test_user_1', '$2a$10$N.zmdr9k7uOV8qDdN5mLYejTFdEAUHDZW0Hj4q8Q3hPKQBQh3Yjdm', '测试用户1', 'test1@example.com', '13800138001', NULL, 'student', 1, 100, 1, NOW(), NOW()),
(2, 'test_user_2', '$2a$10$N.zmdr9k7uOV8qDdN5mLYejTFdEAUHDZW0Hj4q8Q3hPKQBQh3Yjdm', '测试用户2', 'test2@example.com', '13800138002', NULL, 'student', 1, 150, 1, NOW(), NOW()),
(3, 'test_user_3', '$2a$10$N.zmdr9k7uOV8qDdN5mLYejTFdEAUHDZW0Hj4q8Q3hPKQBQh3Yjdm', '测试用户3', 'test3@example.com', '13800138003', NULL, 'teacher', 1, 200, 1, NOW(), NOW());

-- ============================================
-- 2. 插入测试社团（如果不存在）
-- ============================================
INSERT IGNORE INTO `club` (`club_id`, `club_name`, `description`, `avatar`, `founder_id`, `member_count`, `status`, `created_at`, `updated_at`)
VALUES
(1, '计算机协会', '致力于计算机技术的学习与交流', 'https://picsum.photos/200/200?random=1', 3, 50, 1, NOW(), NOW()),
(2, '摄影协会', '用镜头记录美好时光', 'https://picsum.photos/200/200?random=2', 3, 30, 1, NOW(), NOW()),
(3, '音乐协会', '音乐爱好者的聚集地', 'https://picsum.photos/200/200?random=3', 3, 40, 1, NOW(), NOW());

-- ============================================
-- 3. 插入系统通知测试数据（校园公告）
-- ============================================
DELETE FROM `notification` WHERE `user_id` IN (1, 2, 3);

INSERT INTO `notification` (`user_id`, `title`, `content`, `notify_type`, `related_type`, `related_id`, `is_read`, `created_at`)
VALUES
-- 用户1的通知
(1, '系统维护通知', '系统将于本周六凌晨2:00-4:00进行维护升级，期间暂停服务', 'system', NULL, NULL, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(1, '新功能上线', 'CampusLink 3.0 正式上线！新增AI智能答疑、语音搜索等功能', 'system', NULL, NULL, 0, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(1, '安全提醒', '请定期修改密码，保护账号安全', 'system', NULL, NULL, 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, '学期末考试安排', '2025年春季学期末考试将于6月15日开始，请提前做好复习准备', 'system', NULL, NULL, 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, '图书馆开放时间调整', '图书馆自本月起延长开放时间至晚上10点', 'system', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),

-- 用户2的通知
(2, '系统维护通知', '系统将于本周六凌晨2:00-4:00进行维护升级，期间暂停服务', 'system', NULL, NULL, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, '新功能上线', 'CampusLink 3.0 正式上线！新增AI智能答疑、语音搜索等功能', 'system', NULL, NULL, 0, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(2, '活动通知', '你报名的技术分享会即将开始', 'activity', 'activity', 1, 0, DATE_SUB(NOW(), INTERVAL 1 HOUR)),

-- 用户3的通知
(3, '系统维护通知', '系统将于本周六凌晨2:00-4:00进行维护升级，期间暂停服务', 'system', NULL, NULL, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(3, '新功能上线', 'CampusLink 3.0 正式上线！新增AI智能答疑、语音搜索等功能', 'system', NULL, NULL, 1, DATE_SUB(NOW(), INTERVAL 5 HOUR));

-- ============================================
-- 4. 插入社团活动测试数据（社团动态）
-- ============================================
DELETE FROM `activity` WHERE `club_id` IN (1, 2, 3);

INSERT INTO `activity` (`club_id`, `title`, `description`, `location`, `start_time`, `end_time`, `current_participants`, `max_participants`, `reward_points`, `cover_image`, `status`, `created_at`, `updated_at`)
VALUES
-- 未开始的活动 (status=0)
(1, '技术分享会：深入理解Spring Boot', '本次分享会将深入讲解Spring Boot核心原理与最佳实践', '教学楼A101', DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), 30, 100, 10, 'https://picsum.photos/240/180?random=101', 0, NOW(), NOW()),
(1, 'Java编程竞赛', '测试你的编程能力，前三名有丰厚奖品！', '实验楼B202', DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), 15, 50, 20, 'https://picsum.photos/240/180?random=102', 0, NOW(), NOW()),
(2, '校园摄影大赛', '用镜头捕捉校园最美瞬间，优秀作品将在校园展出', '全校', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 14 DAY), 45, 200, 15, 'https://picsum.photos/240/180?random=103', 0, NOW(), NOW()),
(2, '人像摄影工作坊', '邀请专业摄影师教授人像摄影技巧', '艺术楼C301', DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), 20, 30, 10, 'https://picsum.photos/240/180?random=104', 0, NOW(), NOW()),
(3, '春季音乐会', '校园乐队专场演出，精彩不容错过！', '大礼堂', DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), 80, 500, 5, 'https://picsum.photos/240/180?random=105', 0, NOW(), NOW()),
(3, '吉他入门课程', '零基础学吉他，从入门到精通', '音乐教室D101', DATE_ADD(NOW(), INTERVAL 6 DAY), DATE_ADD(NOW(), INTERVAL 6 DAY), 12, 20, 10, 'https://picsum.photos/240/180?random=106', 0, NOW(), NOW()),

-- 进行中的活动 (status=1)
(1, '前端开发训练营', '为期一周的前端开发集训，学习React、Vue等框架', '实验楼B203', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 6 DAY), 25, 40, 30, 'https://picsum.photos/240/180?random=107', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),

-- 已结束的活动 (status=2)
(2, '秋季外拍活动', '前往西湖进行外拍创作', '西湖景区', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 18, 25, 10, 'https://picsum.photos/240/180?random=108', 2, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY));

-- ============================================
-- 5. 插入活动参与记录（可选）
-- ============================================
INSERT IGNORE INTO `activity_participant` (`activity_id`, `user_id`, `status`, `is_signed_in`, `joined_at`)
VALUES
(1, 1, 1, 0, NOW()),
(1, 2, 1, 0, NOW()),
(2, 1, 1, 0, NOW()),
(3, 2, 1, 0, NOW());

-- ============================================
-- 完成！现在可以测试前端页面了
-- ============================================
SELECT '✅ 测试数据插入成功！' AS status;
SELECT CONCAT('用户数量: ', COUNT(*)) AS result FROM user WHERE u_id IN (1, 2, 3);
SELECT CONCAT('通知数量: ', COUNT(*)) AS result FROM notification WHERE user_id IN (1, 2, 3);
SELECT CONCAT('活动数量（未开始）: ', COUNT(*)) AS result FROM activity WHERE club_id IN (1, 2, 3) AND status = 0;
SELECT CONCAT('活动数量（总计）: ', COUNT(*)) AS result FROM activity WHERE club_id IN (1, 2, 3);
