-- ============================================
-- CampusLink 初始化数据脚本
-- 版本: v1.0
-- 创建时间: 2024-01-01
-- ============================================

USE `campuslink`;

-- ============================================
-- 1. 初始化学校数据
-- ============================================
INSERT INTO `school` (`school_name`, `province`, `city`, `address`, `status`) VALUES
('清华大学', '北京市', '北京市', '北京市海淀区清华园1号', 1),
('北京大学', '北京市', '北京市', '北京市海淀区颐和园路5号', 1),
('复旦大学', '上海市', '上海市', '上海市杨浦区邯郸路220号', 1),
('上海交通大学', '上海市', '上海市', '上海市闵行区东川路800号', 1),
('浙江大学', '浙江省', '杭州市', '浙江省杭州市西湖区余杭塘路866号', 1),
('南京大学', '江苏省', '南京市', '江苏省南京市栖霞区仙林大道163号', 1),
('中国科学技术大学', '安徽省', '合肥市', '安徽省合肥市包河区金寨路96号', 1),
('武汉大学', '湖北省', '武汉市', '湖北省武汉市武昌区八一路299号', 1),
('华中科技大学', '湖北省', '武汉市', '湖北省武汉市洪山区珞喻路1037号', 1),
('西安交通大学', '陕西省', '西安市', '陕西省西安市碑林区咸宁西路28号', 1);

-- ============================================
-- 2. 初始化管理员账号
-- ============================================
-- 密码: admin123 (BCrypt加密后的哈希值，实际使用时需要用BCrypt生成)
INSERT INTO `user` (`username`, `nickname`, `email`, `phone`, `password_hash`, `school_id`, `role`, `points`, `level`, `status`, `is_verified`) VALUES
('admin', '系统管理员', 'admin@campuslink.com', '13800000000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 1, 'admin', 10000, 10, 1, 1);

-- ============================================
-- 3. 初始化测试用户
-- ============================================
-- 密码: 123456 (BCrypt加密后的哈希值，实际使用时需要用BCrypt生成)
INSERT INTO `user` (`username`, `nickname`, `email`, `phone`, `password_hash`, `student_id`, `school_id`, `major`, `grade`, `role`, `points`, `level`, `status`, `is_verified`) VALUES
('zhangsan', '张三', 'zhangsan@example.com', '13800000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '2021001', 1, '计算机科学与技术', 2021, 'student', 500, 3, 1, 1),
('lisi', '李四', 'lisi@example.com', '13800000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '2021002', 1, '软件工程', 2021, 'student', 300, 2, 1, 1),
('wangwu', '王五', 'wangwu@example.com', '13800000003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '2020001', 1, '电子信息工程', 2020, 'student', 800, 5, 1, 1),
('zhaoliu', '赵六', 'zhaoliu@example.com', '13800000004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '2022001', 1, '数据科学与大数据技术', 2022, 'student', 200, 1, 1, 0),
('sunqi', '孙七', 'sunqi@example.com', '13800000005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '2019001', 1, '人工智能', 2019, 'student', 1200, 8, 1, 1);

-- ============================================
-- 4. 初始化系统配置
-- ============================================
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('points.upload_resource', '10', '上传资源获得的积分'),
('points.download_resource', '-5', '下载资源消耗的积分'),
('points.answer_question', '5', '回答问题获得的积分'),
('points.answer_accepted', '20', '回答被采纳获得的积分'),
('points.ask_question', '-2', '提问消耗的积分'),
('points.complete_task', '10', '完成任务获得的积分'),
('points.daily_signin', '2', '每日签到获得的积分'),
('points.activity_signin', '10', '活动签到获得的积分'),
('level.threshold_1', '0', '等级1所需积分'),
('level.threshold_2', '100', '等级2所需积分'),
('level.threshold_3', '300', '等级3所需积分'),
('level.threshold_4', '600', '等级4所需积分'),
('level.threshold_5', '1000', '等级5所需积分'),
('level.threshold_6', '1500', '等级6所需积分'),
('level.threshold_7', '2100', '等级7所需积分'),
('level.threshold_8', '2800', '等级8所需积分'),
('level.threshold_9', '3600', '等级9所需积分'),
('level.threshold_10', '5000', '等级10所需积分'),
('upload.max_file_size', '52428800', '最大上传文件大小（字节，默认50MB）'),
('upload.allowed_types', 'pdf,doc,docx,ppt,pptx,xls,xlsx,zip,rar,jpg,png', '允许上传的文件类型'),
('resource.review_required', 'true', '资源是否需要审核'),
('message.max_length', '1000', '私信最大长度'),
('comment.max_length', '500', '评论最大长度'),
('task.max_reward_points', '100', '任务最大悬赏积分'),
('question.max_reward_points', '50', '问题最大悬赏积分'),
('ai.enabled', 'true', 'AI功能是否启用'),
('ai.auto_answer', 'true', '是否自动生成AI答案'),
('ai.answer_delay', '5', 'AI答案生成延迟（秒）');

-- ============================================
-- 5. 初始化示例资源
-- ============================================
INSERT INTO `resource` (`title`, `description`, `uploader_id`, `file_url`, `file_name`, `file_size`, `file_type`, `category`, `course_name`, `school_id`, `score`, `downloads`, `likes`, `status`) VALUES
('数据结构课件-第一章', '数据结构基础知识，包括线性表、栈、队列等内容', 2, 'https://oss.example.com/resources/ds-chapter1.pdf', '数据结构-第一章.pdf', 2048576, 'pdf', '课件', '数据结构', 1, 5, 150, 35, 1),
('操作系统期末复习资料', '操作系统期末考试重点总结，包含历年真题', 3, 'https://oss.example.com/resources/os-review.pdf', '操作系统复习.pdf', 3145728, 'pdf', '试题', '操作系统', 1, 10, 200, 50, 1),
('计算机网络笔记', '计算机网络课程笔记，涵盖TCP/IP协议栈', 4, 'https://oss.example.com/resources/network-notes.pdf', '网络笔记.pdf', 1572864, 'pdf', '笔记', '计算机网络', 1, 3, 80, 20, 1),
('算法设计与分析PPT', '算法课程PPT，包含动态规划、贪心算法等', 5, 'https://oss.example.com/resources/algorithm.pptx', '算法设计.pptx', 4194304, 'pptx', '课件', '算法设计与分析', 1, 8, 120, 28, 1),
('数据库系统概论习题集', '数据库课程习题及答案解析', 6, 'https://oss.example.com/resources/db-exercises.pdf', '数据库习题.pdf', 2621440, 'pdf', '试题', '数据库系统概论', 1, 6, 95, 18, 1);

-- ============================================
-- 6. 初始化示例问题
-- ============================================
INSERT INTO `question` (`title`, `content`, `asker_id`, `category`, `tags`, `reward_points`, `views`, `answer_count`, `is_solved`) VALUES
('如何学习数据结构？', '我是大一新生，想学习数据结构，有什么好的建议吗？需要掌握哪些基础知识？', 5, '学习', '["数据结构", "学习方法"]', 10, 150, 3, 1),
('Python和Java哪个更适合初学者？', '我想学习编程，但不知道选Python还是Java，请大家给点建议', 5, '技术', '["Python", "Java", "编程语言"]', 5, 200, 5, 0),
('图书馆自习室怎么预约？', '请问学校图书馆的自习室需要提前预约吗？怎么预约？', 5, '生活', '["图书馆", "自习室"]', 0, 80, 2, 1),
('考研需要准备多久？', '打算考研，想问问学长学姐们一般需要准备多长时间？', 2, '学习', '["考研", "备考"]', 15, 300, 8, 0);

-- ============================================
-- 7. 初始化示例回答
-- ============================================
INSERT INTO `answer` (`question_id`, `responder_id`, `content`, `likes`, `is_accepted`) VALUES
(1, 4, '学习数据结构建议从基础的线性表开始，然后学习栈、队列、树、图等。推荐《数据结构与算法分析》这本书，同时多刷LeetCode上的题目。', 20, 1),
(1, 3, '我的经验是理论和实践结合，看书的同时一定要自己动手实现各种数据结构，这样理解会更深刻。', 15, 0),
(1, 6, '可以先学C语言基础，然后再学数据结构。推荐浙大的数据结构MOOC课程，讲得很好。', 10, 0),
(3, 3, '图书馆自习室可以通过学校的"智慧校园"APP预约，每天早上8点开放预约，先到先得。', 8, 1),
(3, 4, '也可以直接去图书馆前台登记，但高峰期可能没有位置。', 5, 0);

-- ============================================
-- 8. 初始化示例任务
-- ============================================
INSERT INTO `task` (`publisher_id`, `title`, `content`, `task_type`, `reward_points`, `location`, `deadline`, `status`) VALUES
(2, '帮忙取快递', '菜鸟驿站有个快递，帮忙取一下，取件码：1234', 'errand', 5, '菜鸟驿站', DATE_ADD(NOW(), INTERVAL 1 DAY), 0),
(3, '图书馆借书', '帮忙去图书馆借一本《算法导论》，书号：TP301.6/C73', 'borrow', 8, '图书馆', DATE_ADD(NOW(), INTERVAL 2 DAY), 0),
(5, '代签到', '明天上午第一节课帮忙签到，教室：教学楼A101', 'sign', 3, '教学楼A101', DATE_ADD(NOW(), INTERVAL 1 DAY), 0);

-- ============================================
-- 9. 初始化示例社团
-- ============================================
INSERT INTO `club` (`club_name`, `description`, `school_id`, `founder_id`, `member_count`, `status`) VALUES
('计算机协会', '致力于计算机技术交流与学习，定期举办技术分享会和编程比赛', 1, 6, 150, 1),
('创业俱乐部', '为有创业想法的同学提供交流平台，分享创业经验', 1, 4, 80, 1),
('摄影社', '摄影爱好者的聚集地，定期组织外拍活动', 1, 3, 120, 1);

-- ============================================
-- 10. 初始化社团成员
-- ============================================
INSERT INTO `club_member` (`club_id`, `user_id`, `role`) VALUES
(1, 6, 'founder'),
(1, 2, 'admin'),
(1, 3, 'member'),
(1, 4, 'member'),
(1, 5, 'member'),
(2, 4, 'founder'),
(2, 2, 'member'),
(2, 6, 'member'),
(3, 3, 'founder'),
(3, 5, 'member');

-- ============================================
-- 11. 初始化示例活动
-- ============================================
INSERT INTO `activity` (`club_id`, `title`, `description`, `location`, `start_time`, `end_time`, `max_participants`, `current_participants`, `reward_points`, `status`) VALUES
(1, '编程马拉松', '24小时编程挑战赛，组队参加，优胜者有丰厚奖品', '图书馆三楼', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 8 DAY), 50, 30, 20, 0),
(1, '算法讲座', '邀请知名企业工程师分享算法面试经验', '教学楼A201', DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), 100, 60, 10, 0),
(2, '创业分享会', '成功创业校友回校分享创业经历', '大礼堂', DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY), 200, 80, 5, 0),
(3, '校园风光摄影大赛', '拍摄校园美景，优秀作品将在校园展出', '全校园', DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 20 DAY), NULL, 45, 15, 0);

-- ============================================
-- 12. 初始化活动参与记录
-- ============================================
INSERT INTO `activity_participant` (`activity_id`, `user_id`, `is_signed_in`) VALUES
(1, 2, 0),
(1, 3, 0),
(1, 4, 0),
(1, 5, 0),
(2, 2, 0),
(2, 4, 0),
(2, 6, 0),
(3, 3, 0),
(3, 5, 0),
(4, 3, 0),
(4, 5, 0);

-- ============================================
-- 13. 初始化积分记录
-- ============================================
INSERT INTO `points_log` (`user_id`, `points_change`, `points_after`, `reason`, `related_type`, `related_id`) VALUES
(2, 100, 100, '注册奖励', 'system', NULL),
(2, 10, 110, '上传资源', 'resource', 1),
(2, 5, 115, '回答问题', 'answer', 1),
(3, 100, 100, '注册奖励', 'system', NULL),
(3, 10, 110, '上传资源', 'resource', 2),
(4, 100, 100, '注册奖励', 'system', NULL),
(4, 10, 110, '上传资源', 'resource', 3),
(4, 20, 130, '回答被采纳', 'answer', 1),
(5, 100, 100, '注册奖励', 'system', NULL),
(5, -10, 90, '提问悬赏', 'question', 1),
(6, 100, 100, '注册奖励', 'system', NULL),
(6, 10, 110, '上传资源', 'resource', 4);

-- ============================================
-- 14. 初始化通知
-- ============================================
INSERT INTO `notification` (`user_id`, `title`, `content`, `notify_type`, `related_type`, `related_id`, `is_read`) VALUES
(2, '欢迎加入CampusLink', '欢迎使用CampusLink校园互助平台，开始你的互助之旅吧！', 'system', NULL, NULL, 1),
(5, '你的问题有新回答', '你的问题"如何学习数据结构？"收到了新的回答', 'answer', 'question', 1, 0),
(4, '你的回答被采纳', '你的回答被采纳了，获得20积分奖励', 'answer', 'answer', 1, 0),
(2, '活动报名成功', '你已成功报名"编程马拉松"活动', 'activity', 'activity', 1, 1);

-- ============================================
-- 完成初始化
-- ============================================
SELECT '数据库初始化完成！' AS message;
SELECT CONCAT('学校数量: ', COUNT(*)) AS schools FROM school;
SELECT CONCAT('用户数量: ', COUNT(*)) AS users FROM user;
SELECT CONCAT('资源数量: ', COUNT(*)) AS resources FROM resource;
SELECT CONCAT('问题数量: ', COUNT(*)) AS questions FROM question;
SELECT CONCAT('回答数量: ', COUNT(*)) AS answers FROM answer;
SELECT CONCAT('任务数量: ', COUNT(*)) AS tasks FROM task;
SELECT CONCAT('社团数量: ', COUNT(*)) AS clubs FROM club;
SELECT CONCAT('活动数量: ', COUNT(*)) AS activities FROM activity;

