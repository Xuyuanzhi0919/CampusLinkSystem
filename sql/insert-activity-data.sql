-- 插入社团活动测试数据
-- 基于现有的 club_id = 5（计算机科学协会）
USE `campuslink`;

-- 插入活动数据
INSERT INTO `activity` (`club_id`, `title`, `description`, `location`, `start_time`, `end_time`, `current_participants`, `max_participants`, `reward_points`, `cover_image`, `status`, `created_at`, `updated_at`)
VALUES
-- 未开始的活动 (status=0) - 这些会在首页社团动态卡片显示
(5, '技术分享会：深入理解Spring Boot', '本次分享会将深入讲解Spring Boot核心原理与最佳实践，适合有一定Java基础的同学参加', '教学楼A101', DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), 30, 100, 10, 'https://picsum.photos/240/180?random=101', 0, NOW(), NOW()),

(5, 'Java编程竞赛', '测试你的编程能力！比赛分为初级组和高级组，前三名有丰厚奖品', '实验楼B202', DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), 15, 50, 20, 'https://picsum.photos/240/180?random=102', 0, NOW(), NOW()),

(5, 'Python数据分析工作坊', '学习使用Pandas、NumPy等库进行数据分析，会提供真实数据集进行实战', '实验楼B203', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 25, 40, 15, 'https://picsum.photos/240/180?random=103', 0, NOW(), NOW()),

(5, '算法竞赛训练营', '为ACM竞赛做准备，每周一次的算法刷题和讲解', '实验楼B204', DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), 20, 30, 10, 'https://picsum.photos/240/180?random=104', 0, NOW(), NOW()),

(5, '前端开发入门课程', '从零开始学习HTML、CSS、JavaScript，快速上手前端开发', '实验楼B205', DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), 35, 60, 12, 'https://picsum.photos/240/180?random=105', 0, NOW(), NOW()),

(5, '开源项目贡献指南', '教你如何参与开源项目，从Fork到Pull Request的完整流程', '实验楼B206', DATE_ADD(NOW(), INTERVAL 6 DAY), DATE_ADD(NOW(), INTERVAL 6 DAY), 18, 35, 10, 'https://picsum.photos/240/180?random=106', 0, NOW(), NOW()),

-- 进行中的活动 (status=1)
(5, 'React实战训练营', '为期一周的React开发集训，学习Hooks、状态管理、路由等', '实验楼B203', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 6 DAY), 25, 40, 30, 'https://picsum.photos/240/180?random=107', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),

-- 已结束的活动 (status=2)
(5, 'Git版本控制工作坊', '已举办的Git工作坊，学习分支管理、冲突解决等', '实验楼B202', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 28, 35, 10, 'https://picsum.photos/240/180?random=108', 2, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 插入一些参与记录（让活动看起来更真实）
-- 使用变量获取刚插入的活动ID
SET @activity1 = (SELECT activity_id FROM activity WHERE title = '技术分享会：深入理解Spring Boot' LIMIT 1);
SET @activity2 = (SELECT activity_id FROM activity WHERE title = 'Java编程竞赛' LIMIT 1);
SET @activity3 = (SELECT activity_id FROM activity WHERE title = 'Python数据分析工作坊' LIMIT 1);
SET @activity4 = (SELECT activity_id FROM activity WHERE title = '算法竞赛训练营' LIMIT 1);

INSERT INTO `activity_participant` (`activity_id`, `user_id`, `is_signed_in`, `joined_at`)
VALUES
(@activity1, 1, 0, NOW()),
(@activity1, 2, 0, NOW()),
(@activity1, 3, 0, NOW()),
(@activity2, 1, 0, NOW()),
(@activity2, 4, 0, NOW()),
(@activity3, 2, 0, NOW()),
(@activity3, 5, 0, NOW()),
(@activity4, 3, 0, NOW());

-- 查看插入结果
SELECT '✅ 活动数据插入成功！' AS status;
SELECT CONCAT('活动总数: ', COUNT(*)) AS result FROM activity WHERE club_id = 5;
SELECT CONCAT('未开始的活动数: ', COUNT(*)) AS result FROM activity WHERE club_id = 5 AND status = 0;
SELECT CONCAT('进行中的活动数: ', COUNT(*)) AS result FROM activity WHERE club_id = 5 AND status = 1;
SELECT CONCAT('已结束的活动数: ', COUNT(*)) AS result FROM activity WHERE club_id = 5 AND status = 2;
