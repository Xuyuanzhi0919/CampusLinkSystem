/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : campuslink

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 27/01/2026 14:52:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity`  (
  `activity_id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `club_id` bigint NULL DEFAULT NULL COMMENT '社团ID（社团活动必填，其他类型可为空）',
  `activity_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'club' COMMENT '活动类型: club-社团活动, campus-校园活动, official-官方活动',
  `organizer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组织者名称（非社团活动时使用，如\"校学生会\"、\"教务处\"）',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '活动描述',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动地点',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `max_participants` int NULL DEFAULT NULL COMMENT '最大参与人数',
  `current_participants` int NOT NULL DEFAULT 0 COMMENT '当前参与人数',
  `reward_points` int NOT NULL DEFAULT 0 COMMENT '参与奖励积分',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束，3-已取消',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`activity_id`) USING BTREE,
  INDEX `idx_club_id`(`club_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_activity_type`(`activity_type` ASC, `status` ASC, `start_time` ASC) USING BTREE,
  INDEX `idx_organizer`(`organizer_name` ASC) USING BTREE,
  CONSTRAINT `fk_activity_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 126 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity
-- ----------------------------
INSERT INTO `activity` VALUES (84, NULL, 'campus', '就业指导中心', '2025春季校园招聘会', '特邀500+知名企业现场招聘，提供实习及全职岗位。涵盖互联网、金融、制造等多个行业，欢迎各专业同学参加。', '体育馆一层', '2025-03-15 09:00:00', '2025-03-15 17:00:00', 2000, 356, 0, 'https://via.placeholder.com/800x450/4F46E5/FFFFFF?text=Campus+Job+Fair', 0, '2025-12-17 13:53:25', '2025-12-17 13:53:25');
INSERT INTO `activity` VALUES (85, NULL, 'official', '校党委宣传部', '校庆70周年文艺晚会', '庆祝建校70周年主题晚会，包含歌舞、话剧、器乐演奏等精彩节目。特邀知名校友回校演出，共庆母校华诞。', '大礼堂', '2025-04-20 19:00:00', '2025-04-20 21:30:00', 1500, 892, 0, 'https://via.placeholder.com/800x450/EC4899/FFFFFF?text=70th+Anniversary', 0, '2025-12-17 13:53:25', '2025-12-17 13:53:25');
INSERT INTO `activity` VALUES (86, NULL, 'campus', '校医院', '急救知识专题讲座', '邀请市人民医院急救科专家讲解心肺复苏、海姆立克急救法等实用技能，现场演示+互动体验。', '图书馆报告厅', '2025-03-25 14:00:00', '2025-03-25 16:00:00', 300, 127, 0, 'https://via.placeholder.com/800x450/10B981/FFFFFF?text=First+Aid+Lecture', 0, '2025-12-17 13:53:25', '2025-12-17 13:53:25');
INSERT INTO `activity` VALUES (87, NULL, 'official', '教务处', '考研经验分享会', '邀请2024届成功上岸的学长学姐分享备考经验，涵盖择校、复习规划、心态调整等话题，并提供一对一答疑。', '教学楼A101', '2025-03-10 18:30:00', '2025-03-10 20:30:00', 500, 423, 0, 'https://via.placeholder.com/800x450/F59E0B/FFFFFF?text=Postgraduate+Talk', 0, '2025-12-17 13:53:25', '2025-12-17 13:53:25');
INSERT INTO `activity` VALUES (88, NULL, 'official', '就业指导中心', '2025春季校园招聘会', '特邀500+知名企业现场招聘，提供实习及全职岗位。涵盖互联网、金融、制造等多个行业，欢迎各专业同学参加。', '体育馆一层', '2025-03-15 09:00:00', '2025-03-15 17:00:00', 2000, 356, 0, 'https://via.placeholder.com/800x450/4F46E5/FFFFFF?text=Campus+Job+Fair', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (89, NULL, 'official', '校党委宣传部', '校庆70周年文艺晚会', '庆祝建校70周年主题晚会，包含歌舞、话剧、器乐演奏等精彩节目。特邀知名校友回校演出，共庆母校华诞。', '大礼堂', '2025-04-20 19:00:00', '2025-04-20 21:30:00', 1500, 892, 0, 'https://via.placeholder.com/800x450/EC4899/FFFFFF?text=70th+Anniversary', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (90, NULL, 'official', '保卫处', '国家安全教育主题讲座', '邀请国家安全专家解读总体国家安全观，提高师生国家安全意识。讲座涵盖网络安全、反间谍、反恐等内容。', '图书馆报告厅', '2025-04-15 14:00:00', '2025-04-15 16:00:00', 400, 198, 0, 'https://via.placeholder.com/800x450/DC2626/FFFFFF?text=National+Security', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (91, NULL, 'official', '学生工作处', '优秀学生表彰大会', '表彰2024年度国家奖学金、校长奖学金获得者及三好学生。优秀学生代表分享学习经验，树立榜样力量。', '大礼堂', '2025-05-10 09:00:00', '2025-05-10 11:00:00', 1200, 856, 0, 'https://via.placeholder.com/800x450/F59E0B/FFFFFF?text=Award+Ceremony', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (92, NULL, 'official', '就业指导中心', '毕业生就业指导系列讲座（第一场）', '面向2025届毕业生，解读最新就业政策、简历撰写技巧、面试注意事项。邀请HR专家现场点评简历。', '教学楼A301', '2025-12-19 15:13:49', '2025-12-19 17:13:49', 300, 245, 0, 'https://via.placeholder.com/800x450/3B82F6/FFFFFF?text=Career+Guidance', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (93, NULL, 'official', '心理健康教育中心', '心理健康教育周开幕式', '主题：关爱心理，拥抱阳光。包含心理剧表演、心理测评体验、心理咨询义诊等环节。活动持续一周。', '学生活动中心广场', '2025-12-22 15:13:49', '2025-12-22 18:13:49', 800, 432, 0, 'https://via.placeholder.com/800x450/10B981/FFFFFF?text=Mental+Health+Week', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (94, NULL, 'official', '体育教学部', '全校羽毛球团体赛', '以学院为单位组队参赛，每队5人（3男2女）。比赛采用淘汰赛制，设团体冠、亚、季军奖。', '体育馆羽毛球场', '2025-03-22 08:30:00', '2025-03-23 17:00:00', 160, 128, 0, 'https://via.placeholder.com/800x450/EF4444/FFFFFF?text=Badminton+Match', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (95, NULL, 'official', '保卫处', '消防安全演练', '全校师生消防疏散演练，演示灭火器使用、逃生技巧。消防队现场指导，增强师生安全意识。', '各宿舍楼+操场', '2025-12-18 15:13:49', '2025-12-18 17:13:49', 5000, 3246, 0, 'https://via.placeholder.com/800x450/F97316/FFFFFF?text=Fire+Drill', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (96, NULL, 'official', '图书馆', '图书馆数字资源使用培训', '详解知网、万方、SCI等数据库检索技巧，EndNote文献管理软件使用。适合撰写毕业论文的同学参加。', '图书馆电子阅览室', '2025-12-24 15:13:49', '2025-12-24 17:13:49', 100, 67, 0, 'https://via.placeholder.com/800x450/8B5CF6/FFFFFF?text=Library+Training', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (97, NULL, 'official', '教务处 & 团委', '创新创业大赛决赛暨颁奖典礼', '2025年校级创新创业大赛决赛，10支团队现场路演答辩。设特等奖1名（奖金5万元）、一等奖3名、二等奖6名。', '国际会议中心', '2025-05-25 13:30:00', '2025-05-25 17:30:00', 600, 534, 0, 'https://via.placeholder.com/800x450/06B6D4/FFFFFF?text=Innovation+Contest', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (98, NULL, 'campus', '校医院', '急救知识专题讲座', '邀请市人民医院急救科专家讲解心肺复苏、海姆立克急救法等实用技能，现场演示+互动体验。', '图书馆报告厅', '2025-03-25 14:00:00', '2025-03-25 16:00:00', 300, 127, 0, 'https://via.placeholder.com/800x450/10B981/FFFFFF?text=First+Aid+Lecture', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (99, NULL, 'campus', '教务处', '考研经验分享会', '邀请2024届成功上岸的学长学姐分享备考经验，涵盖择校、复习规划、心态调整等话题，并提供一对一答疑。', '教学楼A101', '2025-03-10 18:30:00', '2025-03-10 20:30:00', 500, 423, 0, 'https://via.placeholder.com/800x450/F59E0B/FFFFFF?text=Postgraduate+Talk', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (100, NULL, 'campus', '外国语学院', '英语四六级备考冲刺班', '免费英语四六级考前辅导，由资深教师讲解听力、阅读、写作技巧。提供内部资料和模拟题。', '外语楼301', '2025-12-20 15:13:49', '2025-12-20 17:13:49', 200, 178, 0, 'https://via.placeholder.com/800x450/3B82F6/FFFFFF?text=CET+Training', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (101, NULL, 'campus', '学生会体育部', '春日校园定向越野赛', '以小组形式（3-5人）在校园内完成任务点打卡。融合趣味游戏、知识问答、体能挑战，赢取神秘奖品！', '全校区', '2025-03-30 09:00:00', '2025-03-30 12:00:00', 500, 387, 0, 'https://via.placeholder.com/800x450/EC4899/FFFFFF?text=Orienteering', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (102, NULL, 'campus', '校艺术团', '校园歌手大赛海选', '面向全校学生选拔优秀歌手，海选通过者晋级复赛。冠军将代表学校参加市大学生歌手大赛。', '学生活动中心剧场', '2025-12-21 15:13:49', '2025-12-21 18:13:49', 150, 142, 0, 'https://via.placeholder.com/800x450/F43F5E/FFFFFF?text=Singer+Contest', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (103, NULL, 'campus', '学生会生活部', '二手书籍跳蚤市场', '学期末二手书交易市场，教材、考研资料、课外读物低价出售。环保又实惠，快来淘宝贝！', '食堂门口广场', '2025-06-15 10:00:00', '2025-06-15 18:00:00', 1000, 0, 0, 'https://via.placeholder.com/800x450/14B8A6/FFFFFF?text=Book+Bazaar', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (104, NULL, 'campus', '计算机学院学生会', '编程马拉松48小时挑战赛', '48小时不间断编程，团队协作完成项目开发。提供免费餐饮、休息区。优胜团队奖励丰厚！', '计算机楼实验室', '2025-04-05 18:00:00', '2025-04-07 18:00:00', 120, 96, 0, 'https://via.placeholder.com/800x450/8B5CF6/FFFFFF?text=Hackathon+48h', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (105, NULL, 'campus', '后勤管理处', '宿舍文化节寝室装饰大赛', '以寝室为单位参赛，主题自选。评委将从创意、美观、整洁度等维度评分。一等奖寝室奖励空调清洗券！', '各宿舍楼', '2025-05-01 00:00:00', '2025-05-07 23:59:59', 800, 234, 0, 'https://via.placeholder.com/800x450/F59E0B/FFFFFF?text=Dorm+Decoration', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (106, NULL, 'campus', '摄影协会', '校园摄影大赛作品征集', '主题：发现校园之美。作品需原创，展现校园风光、人文、生活。优秀作品将在图书馆展出并颁发证书。', '线上提交+图书馆展览', '2025-12-27 15:13:49', '2025-05-20 23:59:59', 300, 87, 0, 'https://via.placeholder.com/800x450/06B6D4/FFFFFF?text=Photo+Contest', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (107, NULL, 'campus', '经济管理学院', '职场礼仪与形象管理工作坊', '职业形象顾问讲解商务着装、会议礼仪、邮件规范等职场必备技能。互动环节现场模拟面试场景。', '管理楼301', '2025-12-23 15:13:49', '2025-12-23 17:13:49', 80, 72, 0, 'https://via.placeholder.com/800x450/10B981/FFFFFF?text=Etiquette+Workshop', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (108, NULL, 'campus', '体育教学部', '校园马拉松接力赛', '5人接力完成42.195公里全程马拉松。路线环绕校园湖畔，风景优美。完赛队伍颁发纪念奖牌！', '校园环湖跑道', '2025-04-12 07:00:00', '2025-04-12 12:00:00', 300, 267, 0, 'https://via.placeholder.com/800x450/EF4444/FFFFFF?text=Marathon+Relay', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (109, NULL, 'campus', '国际交流处', '留学申请经验交流会', '已拿到海外名校offer的学长学姐分享申请经验，涵盖选校、文书、推荐信、面试等环节。现场答疑。', '国际教育学院会议室', '2025-12-25 15:13:49', '2025-12-25 18:13:49', 100, 78, 0, 'https://via.placeholder.com/800x450/8B5CF6/FFFFFF?text=Study+Abroad', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (110, NULL, 'campus', '后勤管理处 & 环保协会', '校园植树节公益活动', '在校园东区种植樱花树100棵。参与者可认领小树苗，挂上心愿牌。为母校增添一抹春色！', '东区生态园', '2025-03-12 08:00:00', '2025-03-12 11:00:00', 200, 156, 0, 'https://via.placeholder.com/800x450/22C55E/FFFFFF?text=Tree+Planting', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (111, NULL, 'campus', '科研处', '实验室开放日暨科技成果展', '开放重点实验室供学生参观，展示最新科研成果。体验AI、VR、机器人等前沿技术，激发科研兴趣。', '科技楼各实验室', '2025-05-18 13:00:00', '2025-05-18 17:00:00', 500, 298, 0, 'https://via.placeholder.com/800x450/3B82F6/FFFFFF?text=Lab+Open+Day', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (112, NULL, 'campus', '学生会', '毕业生跳蚤市场', '毕业季专场，学长学姐出售闲置物品（书籍、电器、家具等）。价格美丽，先到先得！支持线上预约。', '操场', '2025-06-10 09:00:00', '2025-06-10 17:00:00', 2000, 0, 0, 'https://via.placeholder.com/800x450/F97316/FFFFFF?text=Graduation+Market', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (113, NULL, 'official', '校团委', '元旦晚会', '2025年元旦联欢晚会，全校师生欢聚一堂，共迎新年。节目精彩纷呈，气氛热烈。', '大礼堂', '2024-12-30 19:00:00', '2024-12-30 22:00:00', 1500, 1500, 0, 'https://via.placeholder.com/800x450/DC2626/FFFFFF?text=New+Year+Gala', 2, '2025-11-27 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (114, NULL, 'campus', '团委实践部', '寒假社会实践动员大会', '布置寒假社会实践任务，讲解报告撰写规范。优秀实践团队将获评校级荣誉。', '教学楼B202', '2024-12-25 14:00:00', '2024-12-25 16:00:00', 300, 267, 0, 'https://via.placeholder.com/800x450/6366F1/FFFFFF?text=Practice+Mobilization', 2, '2025-11-22 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (115, NULL, 'official', '教务处', '期末考试诚信教育大会', '强调考试纪律和诚信考试的重要性，签署诚信承诺书。违纪者将受到严肃处理。', '各学院教室', '2024-12-20 15:00:00', '2024-12-20 17:00:00', 5000, 4876, 0, 'https://via.placeholder.com/800x450/EF4444/FFFFFF?text=Exam+Integrity', 2, '2025-11-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (116, NULL, 'campus', '体育教学部', '冬季长跑比赛', '男子5000米、女子3000米长跑比赛。考验耐力与毅力，前三名颁发奖牌和证书。', '田径场', '2024-12-15 09:00:00', '2024-12-15 11:30:00', 200, 186, 0, 'https://via.placeholder.com/800x450/F59E0B/FFFFFF?text=Winter+Running', 2, '2025-11-12 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (117, NULL, 'official', '校团委', '年度优秀社团评选答辩会', '全校50+社团竞争10个优秀社团名额。各社团现场展示年度成果，评委打分。', '学生活动中心', '2024-12-10 13:00:00', '2024-12-10 18:00:00', 300, 289, 0, 'https://via.placeholder.com/800x450/8B5CF6/FFFFFF?text=Club+Evaluation', 2, '2025-11-07 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (118, NULL, 'campus', '图书馆 & 读书协会', '线上读书打卡活动', '为期30天的读书打卡挑战！每日在小程序分享读书笔记，坚持打卡满21天即可获得图书馆VIP借阅权限（可借15本书）。', '线上小程序', '2025-12-07 15:13:49', '2026-01-06 15:13:49', 1000, 456, 0, 'https://via.placeholder.com/800x450/10B981/FFFFFF?text=Reading+Challenge', 1, '2025-12-02 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (119, NULL, 'campus', '环保协会', '校园环保行动周', '活动内容：垃圾分类宣传、废旧物品回收、环保知识竞赛、绿色骑行。参与3项活动即可获得环保购物袋。', '全校区', '2025-12-15 15:13:49', '2025-12-22 15:13:49', 2000, 876, 0, 'https://via.placeholder.com/800x450/22C55E/FFFFFF?text=Eco+Week', 1, '2025-12-12 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (120, NULL, 'official', '教务处', '毕业设计中期检查', '2025届本科生毕业设计中期答辩。各学院组织检查组听取学生汇报，评估完成进度，提出改进建议。', '各学院', '2025-12-16 15:13:49', '2025-12-23 15:13:49', 3000, 2456, 0, 'https://via.placeholder.com/800x450/3B82F6/FFFFFF?text=Thesis+Review', 1, '2025-12-14 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (121, NULL, 'campus', '摄影协会', '校园摄影作品线上投票', '校园摄影大赛30幅入围作品线上展示，扫码投票选出你最喜欢的作品。投票截止后公布获奖名单。', '线上投票平台', '2025-12-14 15:13:49', '2025-12-21 15:13:49', 5000, 2134, 0, 'https://via.placeholder.com/800x450/EC4899/FFFFFF?text=Photo+Voting', 1, '2025-12-12 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (122, NULL, 'official', '学生工作处', '寒假留校学生新春联欢会', '为留校学生举办新春联欢活动，包含年夜饭、游戏互动、抽奖环节。让留校学生感受到家的温暖。', '学生食堂三楼', '2025-12-17 10:13:49', '2025-12-17 18:13:49', 150, 132, 0, 'https://via.placeholder.com/800x450/F59E0B/FFFFFF?text=Spring+Festival', 1, '2025-12-07 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (123, NULL, 'campus', '就业指导中心', '知名企业高管职业规划讲座', '邀请某互联网大厂副总裁分享职业发展经验，解答学生关于职业选择的困惑。仅限100人，先到先得！', '国际会议中心', '2025-12-20 15:13:49', '2025-12-20 17:13:49', 100, 97, 0, 'https://via.placeholder.com/800x450/4F46E5/FFFFFF?text=CEO+Talk', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (124, NULL, 'campus', '计算机学院', 'Python编程入门实战工作坊', '零基础学Python，2天集训营。配备电脑和编程环境，讲师手把手教学。结业考核通过颁发证书。', '计算机楼机房', '2025-12-22 15:13:49', '2025-12-23 15:13:49', 50, 47, 0, 'https://via.placeholder.com/800x450/8B5CF6/FFFFFF?text=Python+Workshop', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');
INSERT INTO `activity` VALUES (125, NULL, 'official', '校长办公室', '校长午餐会', '校长与20名学生代表共进午餐，面对面交流学校发展、教学改革、后勤服务等话题。学生可提出建议和意见。', '行政楼贵宾餐厅', '2025-12-21 15:13:49', '2025-12-21 17:13:49', 20, 18, 0, 'https://via.placeholder.com/800x450/DC2626/FFFFFF?text=Lunch+with+President', 0, '2025-12-17 15:13:49', '2025-12-17 15:13:49');

-- ----------------------------
-- Table structure for activity_participant
-- ----------------------------
DROP TABLE IF EXISTS `activity_participant`;
CREATE TABLE `activity_participant`  (
  `ap_id` bigint NOT NULL AUTO_INCREMENT COMMENT '参与记录ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `is_signed_in` tinyint NOT NULL DEFAULT 0 COMMENT '是否已签到：0-否，1-是',
  `signed_in_at` datetime NULL DEFAULT NULL COMMENT '签到时间',
  `joined_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  PRIMARY KEY (`ap_id`) USING BTREE,
  UNIQUE INDEX `uk_activity_user`(`activity_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_activity_participant_activity` FOREIGN KEY (`activity_id`) REFERENCES `activity` (`activity_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_activity_participant_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 104 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动参与表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity_participant
-- ----------------------------
INSERT INTO `activity_participant` VALUES (102, 95, 1, 0, NULL, '2025-12-17 15:17:47');
INSERT INTO `activity_participant` VALUES (103, 123, 1, 0, NULL, '2025-12-18 16:21:17');

-- ----------------------------
-- Table structure for answer
-- ----------------------------
DROP TABLE IF EXISTS `answer`;
CREATE TABLE `answer`  (
  `a_id` bigint NOT NULL AUTO_INCREMENT COMMENT '回答ID',
  `question_id` bigint NOT NULL COMMENT '问题ID',
  `responder_id` bigint NOT NULL COMMENT '回答者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回答内容',
  `images` json NULL COMMENT '图片URL列表（JSON数组）',
  `likes` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `is_accepted` tinyint NOT NULL DEFAULT 0 COMMENT '是否被采纳：0-否，1-是',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`a_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE,
  INDEX `idx_responder_id`(`responder_id` ASC) USING BTREE,
  INDEX `idx_is_accepted`(`is_accepted` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_answer_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`q_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_answer_responder` FOREIGN KEY (`responder_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '回答表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of answer
-- ----------------------------
INSERT INTO `answer` VALUES (1, 1, 4, '学习数据结构建议从基础的线性表开始，然后学习栈、队列、树、图等。推荐《数据结构与算法分析》这本书，同时多刷LeetCode上的题目。', NULL, 20, 1, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `answer` VALUES (2, 1, 3, '我的经验是理论和实践结合，看书的同时一定要自己动手实现各种数据结构，这样理解会更深刻。', NULL, 15, 0, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `answer` VALUES (3, 1, 6, '可以先学C语言基础，然后再学数据结构。推荐浙大的数据结构MOOC课程，讲得很好。', NULL, 10, 0, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `answer` VALUES (4, 3, 3, '图书馆自习室可以通过学校的\"智慧校园\"APP预约，每天早上8点开放预约，先到先得。', NULL, 8, 1, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `answer` VALUES (5, 3, 4, '也可以直接去图书馆前台登记，但高峰期可能没有位置。', NULL, 5, 0, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `answer` VALUES (6, 4, 1, '12131231231312', NULL, 0, 0, 1, '2025-11-19 13:41:24', '2025-11-19 13:41:24');
INSERT INTO `answer` VALUES (7, 304, 1, '11111111111111111111111111', NULL, 0, 0, 0, '2025-11-19 16:00:47', '2025-12-09 21:04:17');
INSERT INTO `answer` VALUES (8, 304, 1, '我建议你先把老师上课讲的重点内容过一遍,然后       多做往年真题,这样效果比较好。我这里有一些资料可以分享给你。', NULL, 0, 0, 0, '2025-11-19 16:12:36', '2025-12-09 21:04:17');
INSERT INTO `answer` VALUES (9, 305, 1, '我建议你先把老师上课讲的重点内容过一遍,然后       多做往年真题,这样效果比较好。我这里有一些资料可以分享给你。', NULL, 0, 1, 1, '2025-11-19 16:15:19', '2025-11-24 08:07:38');
INSERT INTO `answer` VALUES (10, 306, 1, '11111111111111111111', NULL, 2, 1, 1, '2025-11-19 16:30:05', '2025-11-19 16:41:39');
INSERT INTO `answer` VALUES (11, 306, 48, '111111111111111111111', NULL, 2, 0, 1, '2025-11-19 16:31:47', '2025-11-19 16:31:47');
INSERT INTO `answer` VALUES (12, 306, 1, '11111111111', NULL, 1, 0, 0, '2025-11-19 16:36:12', '2025-11-19 16:46:05');
INSERT INTO `answer` VALUES (13, 305, 1, '1傻逼  11111111111111111', NULL, 0, 0, 0, '2025-11-19 17:07:14', '2025-11-19 17:07:17');
INSERT INTO `answer` VALUES (14, 305, 1, '啊啊啊啊啊啊啊啊啊啊啊', NULL, 0, 0, 0, '2025-11-19 17:07:26', '2025-11-19 19:50:56');
INSERT INTO `answer` VALUES (15, 314, 48, '整不出得法SFF11', NULL, 0, 1, 1, '2025-11-21 13:49:27', '2025-11-21 14:25:29');
INSERT INTO `answer` VALUES (16, 310, 1, '佛挡杀佛身份证发烧1', NULL, 1, 0, 0, '2025-11-21 13:50:26', '2025-12-05 09:02:34');
INSERT INTO `answer` VALUES (17, 310, 48, '322423423432424234', NULL, 0, 0, 0, '2025-11-21 13:50:37', '2025-11-21 13:58:13');
INSERT INTO `answer` VALUES (18, 310, 48, '423423424242', NULL, 0, 0, 0, '2025-11-21 13:54:48', '2025-11-21 13:58:10');
INSERT INTO `answer` VALUES (19, 310, 48, '432423424323424', NULL, 0, 0, 0, '2025-11-21 13:54:55', '2025-11-21 13:58:12');
INSERT INTO `answer` VALUES (20, 310, 48, '324242432342', NULL, 0, 0, 0, '2025-11-21 13:58:15', '2025-11-21 13:58:22');
INSERT INTO `answer` VALUES (21, 310, 48, '3424242424', NULL, 0, 0, 0, '2025-11-21 13:58:22', '2025-11-21 13:58:32');
INSERT INTO `answer` VALUES (22, 310, 48, '42324243324', NULL, 0, 0, 0, '2025-11-21 13:58:35', '2025-11-21 14:01:00');
INSERT INTO `answer` VALUES (23, 310, 48, '3224242342', NULL, 6, 0, 1, '2025-11-21 14:01:06', '2025-11-21 14:01:06');
INSERT INTO `answer` VALUES (24, 310, 1, '测试测试测试测试测试测试测试测试测试', NULL, 0, 0, 1, '2025-12-09 21:15:43', '2025-12-09 21:15:43');
INSERT INTO `answer` VALUES (25, 131, 1, '11111111111111111111111111111111111111111', NULL, 0, 0, 1, '2025-12-12 14:00:02', '2025-12-12 14:00:02');
INSERT INTO `answer` VALUES (26, 307, 56, '加油加油加油加油加油加油加油加油加油加油加油加油加油加油加油加油', NULL, 0, 0, 1, '2025-12-12 14:45:59', '2025-12-12 14:45:59');
INSERT INTO `answer` VALUES (27, 317, 2, '推荐先学习Java基础...', NULL, 0, 1, 1, '2025-12-12 19:33:28', '2025-12-12 19:37:46');
INSERT INTO `answer` VALUES (28, 317, 3, '我的学习路线是...', NULL, 0, 0, 1, '2025-12-12 19:33:28', '2025-12-12 19:33:28');
INSERT INTO `answer` VALUES (29, 317, 4, '推荐几本好书...', NULL, 0, 0, 1, '2025-12-12 19:33:28', '2025-12-12 19:33:28');
INSERT INTO `answer` VALUES (30, 318, 2, '推荐使用ThreadPoolExecutor自定义线程池，核心参数配置建议...', NULL, 16, 0, 1, '2025-12-12 19:38:52', '2025-12-12 19:38:52');
INSERT INTO `answer` VALUES (31, 318, 3, '我的实践经验：1. 合理设置队列容量 2. 选择合适的拒绝策略...', NULL, 22, 0, 1, '2025-12-12 19:38:52', '2025-12-12 19:38:52');
INSERT INTO `answer` VALUES (32, 318, 4, '补充一点：一定要监控线程池的运行状态，推荐使用Micrometer...', NULL, 8, 0, 1, '2025-12-12 19:38:52', '2025-12-12 19:38:52');

-- ----------------------------
-- Table structure for answer_like
-- ----------------------------
DROP TABLE IF EXISTS `answer_like`;
CREATE TABLE `answer_like`  (
  `like_id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `answer_id` bigint NOT NULL COMMENT '答案ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`like_id`) USING BTREE,
  UNIQUE INDEX `uk_user_answer`(`user_id` ASC, `answer_id` ASC) USING BTREE COMMENT '用户答案唯一索引，防止重复点赞',
  INDEX `idx_answer_id`(`answer_id` ASC) USING BTREE COMMENT '答案ID索引',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '用户ID索引',
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE COMMENT '创建时间索引',
  CONSTRAINT `fk_answer_like_answer` FOREIGN KEY (`answer_id`) REFERENCES `answer` (`a_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_answer_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '答案点赞表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of answer_like
-- ----------------------------
INSERT INTO `answer_like` VALUES (3, 1, 23, '2025-11-21 14:54:14');
INSERT INTO `answer_like` VALUES (5, 1, 16, '2025-11-21 14:54:19');
INSERT INTO `answer_like` VALUES (8, 48, 23, '2025-11-21 14:54:38');
INSERT INTO `answer_like` VALUES (12, 1, 30, '2025-12-15 18:02:34');

-- ----------------------------
-- Table structure for club
-- ----------------------------
DROP TABLE IF EXISTS `club`;
CREATE TABLE `club`  (
  `club_id` bigint NOT NULL AUTO_INCREMENT COMMENT '社团ID',
  `club_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '社团名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '社团简介',
  `logo_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '社团Logo',
  `school_id` bigint NOT NULL COMMENT '所属学校ID',
  `founder_id` bigint NOT NULL COMMENT '创建者ID',
  `member_count` int NOT NULL DEFAULT 0 COMMENT '成员数量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-已解散，1-正常',
  `is_official` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否官方/校级社团：0-否，1-是',
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '兴趣' COMMENT '社团分类: 技术/学习/体育/艺术/公益/兴趣',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`club_id`) USING BTREE,
  INDEX `idx_school_id`(`school_id` ASC) USING BTREE,
  INDEX `idx_founder_id`(`founder_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_is_official`(`is_official` ASC, `status` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `fk_club_founder` FOREIGN KEY (`founder_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_club_school` FOREIGN KEY (`school_id`) REFERENCES `school` (`school_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 60 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '社团表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of club
-- ----------------------------
INSERT INTO `club` VALUES (5, '计算机科学协会', '专注于计算机科学技术交流与学习的学生社团', 'https://picsum.photos/200/200?random=1', 1, 48, 1, 1, 0, '兴趣', '2025-11-13 16:53:40', '2025-11-13 16:53:40');
INSERT INTO `club` VALUES (10, 'Python编程俱乐部', '学习Python编程,从入门到精通,每周技术分享', 'https://picsum.photos/200/200?random=10', 1, 1, 23, 1, 0, '兴趣', '2025-10-01 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (11, '人工智能研究社', '探索AI前沿技术,机器学习、深度学习实战项目', 'https://picsum.photos/200/200?random=11', 1, 2, 45, 1, 0, '兴趣', '2025-09-15 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (12, 'Web开发工作室', '全栈开发学习,React/Vue/Node.js项目实践', 'https://picsum.photos/200/200?random=12', 2, 3, 38, 1, 0, '兴趣', '2025-09-20 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (13, '网络安全小组', '白帽黑客培养,CTF竞赛,渗透测试技术交流', 'https://picsum.photos/200/200?random=13', 1, 4, 31, 1, 0, '兴趣', '2025-10-05 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (14, '移动应用开发社', 'Android/iOS/小程序开发,校园应用孵化平台', 'https://picsum.photos/200/200?random=14', 3, 5, 27, 1, 0, '兴趣', '2025-10-10 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (15, '开源技术联盟', '参与开源项目,Git协作,开源文化推广', 'https://picsum.photos/200/200?random=15', 2, 1, 19, 1, 0, '兴趣', '2025-10-15 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (16, '数据科学协会', '大数据分析,数据可视化,Kaggle竞赛小组', 'https://picsum.photos/200/200?random=16', 1, 2, 34, 1, 0, '兴趣', '2025-09-25 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (17, '游戏开发工作室', 'Unity/Unreal引擎学习,独立游戏开发', 'https://picsum.photos/200/200?random=17', 4, 3, 22, 1, 0, '兴趣', '2025-10-20 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (18, '算法竞赛队', 'ACM/ICPC训练,LeetCode刷题,算法思维培养', 'https://picsum.photos/200/200?random=18', 1, 4, 16, 1, 0, '兴趣', '2025-11-01 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (19, '区块链技术研究社', '区块链原理,智能合约开发,Web3.0探索', 'https://picsum.photos/200/200?random=19', 2, 5, 14, 1, 0, '兴趣', '2025-11-05 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (20, '英语学习角', '英语口语练习,外教交流,雅思托福备考', 'https://picsum.photos/200/200?random=20', 1, 1, 67, 1, 0, '兴趣', '2025-09-01 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (21, '数学建模协会', '数学建模竞赛培训,MATLAB/Python工具学习', 'https://picsum.photos/200/200?random=21', 3, 2, 42, 1, 0, '兴趣', '2025-09-10 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (22, '考研互助小组', '考研经验分享,学习资料交流,每日打卡督学', 'https://picsum.photos/200/200?random=22', 1, 3, 89, 1, 0, '兴趣', '2025-08-20 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (23, '日语学习社', '日语入门到N1,日本文化交流,动漫日语', 'https://picsum.photos/200/200?random=23', 2, 4, 56, 1, 0, '兴趣', '2025-09-05 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (24, '物理实验俱乐部', '趣味物理实验,科学竞赛,物理知识科普', 'https://picsum.photos/200/200?random=24', 1, 5, 28, 1, 0, '兴趣', '2025-10-01 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (25, '化学爱好者协会', '化学实验展示,化学竞赛,生活化学应用', 'https://picsum.photos/200/200?random=25', 4, 1, 21, 1, 0, '兴趣', '2025-10-10 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (26, '经济学研讨会', '经济学原理学习,金融市场分析,投资理财', 'https://picsum.photos/200/200?random=26', 2, 2, 33, 1, 0, '兴趣', '2025-09-15 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (27, '心理学研究社', '心理学知识普及,心理测试,心理健康互助', 'https://picsum.photos/200/200?random=27', 1, 3, 47, 1, 0, '兴趣', '2025-09-20 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (28, '历史文化研究会', '中国历史研究,文物鉴赏,历史遗迹考察', 'https://picsum.photos/200/200?random=28', 3, 4, 18, 1, 0, '兴趣', '2025-10-15 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (29, '哲学思辨社', '哲学经典阅读,思辨讨论,逻辑思维训练', 'https://picsum.photos/200/200?random=29', 1, 5, 12, 1, 0, '兴趣', '2025-11-01 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (30, '篮球队', '校内篮球赛,篮球技术训练,篮球文化推广', 'https://picsum.photos/200/200?random=30', 1, 1, 78, 1, 0, '兴趣', '2025-08-15 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (31, '足球俱乐部', '足球比赛,足球训练,观赛活动组织', 'https://picsum.photos/200/200?random=31', 2, 2, 65, 1, 0, '兴趣', '2025-08-20 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (32, '羽毛球协会', '羽毛球训练,校际比赛,羽毛球爱好者交流', 'https://picsum.photos/200/200?random=32', 1, 3, 52, 1, 0, '兴趣', '2025-09-01 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (33, '乒乓球社', '乒乓球技术提升,友谊赛,国球文化传播', 'https://picsum.photos/200/200?random=33', 3, 4, 43, 1, 0, '兴趣', '2025-09-05 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (34, '网球俱乐部', '网球入门教学,校内联赛,网球社交', 'https://picsum.photos/200/200?random=34', 2, 5, 29, 1, 0, '兴趣', '2025-09-10 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (35, '跑步协会', '晨跑夜跑组织,马拉松训练,健康生活倡导', 'https://picsum.photos/200/200?random=35', 1, 1, 61, 1, 0, '兴趣', '2025-09-15 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (36, '瑜伽社', '瑜伽课程,身心放松,健康塑形', 'https://picsum.photos/200/200?random=36', 4, 2, 37, 1, 0, '兴趣', '2025-10-01 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (37, '轮滑社团', '轮滑技巧学习,花式轮滑表演,校园巡游', 'https://picsum.photos/200/200?random=37', 1, 3, 24, 1, 0, '兴趣', '2025-10-05 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (38, '攀岩俱乐部', '攀岩技术训练,户外攀岩活动,勇气挑战', 'https://picsum.photos/200/200?random=38', 2, 4, 16, 1, 0, '兴趣', '2025-10-20 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (39, '游泳队', '游泳技术提升,水上安全培训,游泳比赛', 'https://picsum.photos/200/200?random=39', 1, 5, 31, 1, 0, '兴趣', '2025-10-25 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (40, '摄影协会', '摄影技术交流,外拍活动,摄影展览', 'https://picsum.photos/200/200?random=40', 1, 1, 54, 1, 0, '兴趣', '2025-09-01 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (41, '吉他社', '吉他弹唱教学,音乐会演出,校园民谣', 'https://picsum.photos/200/200?random=41', 2, 2, 48, 1, 0, '兴趣', '2025-09-10 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (42, '动漫社', '动漫文化交流,Cosplay表演,漫展组织', 'https://picsum.photos/200/200?random=42', 1, 3, 72, 1, 0, '兴趣', '2025-08-25 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (43, '话剧社', '话剧排练演出,戏剧理论学习,校园戏剧节', 'https://picsum.photos/200/200?random=43', 3, 4, 35, 1, 0, '兴趣', '2025-09-15 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (44, '书法协会', '书法练习,书法展览,传统文化传承', 'https://picsum.photos/200/200?random=44', 1, 5, 27, 1, 0, '兴趣', '2025-10-01 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (45, '绘画社', '素描水彩油画,艺术创作,画展策划', 'https://picsum.photos/200/200?random=45', 2, 1, 41, 1, 0, '兴趣', '2025-09-20 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (46, '街舞社', 'Hip-Hop/Breaking/Popping,舞蹈表演比赛', 'https://picsum.photos/200/200?random=46', 1, 2, 59, 1, 0, '兴趣', '2025-09-05 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (47, '合唱团', '合唱训练,音乐会演出,校园十佳歌手', 'https://picsum.photos/200/200?random=47', 4, 3, 36, 1, 0, '兴趣', '2025-10-10 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (48, '手工艺术社', 'DIY手工制作,非遗手工艺学习,创意市集', 'https://picsum.photos/200/200?random=48', 1, 4, 22, 1, 0, '兴趣', '2025-10-15 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (49, '电影鉴赏社', '经典电影观影,影评写作,微电影创作', 'https://picsum.photos/200/200?random=49', 2, 5, 44, 1, 0, '兴趣', '2025-09-25 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (50, '青年志愿者协会', '社区服务,支教助学,环保公益活动', 'https://picsum.photos/200/200?random=50', 1, 1, 83, 1, 0, '兴趣', '2025-08-10 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (51, '环保行动小组', '校园环保宣传,垃圾分类推广,绿色生活倡导', 'https://picsum.photos/200/200?random=51', 3, 2, 39, 1, 0, '兴趣', '2025-09-10 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (52, '义工服务队', '敬老院服务,义卖募捐,社会关爱行动', 'https://picsum.photos/200/200?random=52', 1, 3, 51, 1, 0, '兴趣', '2025-09-15 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (53, '爱心支教团', '乡村支教,留守儿童关爱,教育公平推动', 'https://picsum.photos/200/200?random=53', 2, 4, 28, 1, 0, '兴趣', '2025-10-01 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (54, '动物保护协会', '流浪动物救助,动物保护宣传,领养活动', 'https://picsum.photos/200/200?random=54', 1, 5, 33, 1, 0, '兴趣', '2025-10-10 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (55, '桌游社', '狼人杀/剧本杀/三国杀,桌游聚会,策略游戏', 'https://picsum.photos/200/200?random=55', 1, 1, 46, 1, 0, '兴趣', '2025-09-20 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (56, '美食烘焙社', '烘焙教学,美食分享,校园美食节', 'https://picsum.photos/200/200?random=56', 2, 2, 58, 1, 0, '兴趣', '2025-09-05 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (57, '旅行探险社', '周末郊游,户外探险,旅行攻略分享', 'https://picsum.photos/200/200?random=57', 1, 3, 37, 1, 0, '兴趣', '2025-10-01 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (58, '魔术社', '魔术表演学习,近景魔术,舞台魔术秀', 'https://picsum.photos/200/200?random=58', 3, 4, 19, 1, 0, '兴趣', '2025-10-20 10:00:00', '2025-12-16 10:00:00');
INSERT INTO `club` VALUES (59, '茶艺社', '茶艺表演,茶文化学习,品茶交流', 'https://picsum.photos/200/200?random=59', 1, 5, 25, 1, 0, '兴趣', '2025-10-25 10:00:00', '2025-12-16 10:00:00');

-- ----------------------------
-- Table structure for club_member
-- ----------------------------
DROP TABLE IF EXISTS `club_member`;
CREATE TABLE `club_member`  (
  `cm_id` bigint NOT NULL AUTO_INCREMENT COMMENT '成员关系ID',
  `club_id` bigint NOT NULL COMMENT '社团ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` enum('member','admin','founder') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'member' COMMENT '角色',
  `joined_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`cm_id`) USING BTREE,
  UNIQUE INDEX `uk_club_user`(`club_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_club_member_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_club_member_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '社团成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of club_member
-- ----------------------------

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `comment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `user_id` bigint NOT NULL COMMENT '评论者ID',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论对象类型（resource/question/answer等）',
  `target_id` bigint NOT NULL COMMENT '评论对象ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父评论ID（用于回复）',
  `likes` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`comment_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`comment_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, 43, 'resource', 1, '这个资源非常有用，帮助我通过了考试！感谢分享。', NULL, 4, 0, '2025-11-09 14:06:35');
INSERT INTO `comment` VALUES (2, 44, 'resource', 1, '同意楼上，这个资料整理得很详细，值得下载！', NULL, 1, 1, '2025-11-09 14:06:41');
INSERT INTO `comment` VALUES (3, 43, 'resource', 1, '是的，我也觉得整理得很好，特别是重点部分标注得很清楚。', 2, 0, 1, '2025-11-09 14:06:45');
INSERT INTO `comment` VALUES (4, 45, 'resource', 1, '确实不错，我已经下载了，很实用！', 2, 0, 1, '2025-11-09 14:06:50');
INSERT INTO `comment` VALUES (5, 44, 'question', 1, '这个问题我也遇到过，建议先检查一下配置文件。', NULL, 0, 1, '2025-11-09 14:06:52');
INSERT INTO `comment` VALUES (6, 43, 'answer', 1, '这个回答很详细，解决了我的疑惑！', NULL, 0, 1, '2025-11-09 14:06:54');
INSERT INTO `comment` VALUES (7, 45, 'activity', 1, '这个活动很有意思，期待参加！', NULL, 0, 1, '2025-11-09 14:06:56');
INSERT INTO `comment` VALUES (11, 43, 'resource', 1, '0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789这是超过1000字符的内容用于测试验证规则是否生效', NULL, 0, 1, '2025-11-09 14:09:04');
INSERT INTO `comment` VALUES (12, 44, 'resource', 1, '补充一下，还有另一个重点章节也很重要。', 3, 0, 1, '2025-11-09 14:13:01');
INSERT INTO `comment` VALUES (13, 45, 'resource', 1, '同意，那个章节的确很关键，考试必考。', 3, 0, 1, '2025-11-09 14:13:04');
INSERT INTO `comment` VALUES (14, 43, 'activity', 2, '这个社团活动很赞！', NULL, 0, 1, '2025-11-09 14:13:12');
INSERT INTO `comment` VALUES (15, 44, 'activity', 2, '活动时间是什么时候？', NULL, 0, 1, '2025-11-09 14:13:14');
INSERT INTO `comment` VALUES (16, 45, 'activity', 2, '地点在哪里呢？需要报名吗？', NULL, 0, 1, '2025-11-09 14:13:15');

-- ----------------------------
-- Table structure for download_log
-- ----------------------------
DROP TABLE IF EXISTS `download_log`;
CREATE TABLE `download_log`  (
  `dl_id` bigint NOT NULL AUTO_INCREMENT COMMENT '下载记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `points_cost` int NOT NULL DEFAULT 0 COMMENT '消耗积分',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下载时间',
  PRIMARY KEY (`dl_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_download_log_resource` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`r_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_download_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '下载记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of download_log
-- ----------------------------
INSERT INTO `download_log` VALUES (1, 38, 1, 5, '2025-11-09 13:51:57');
INSERT INTO `download_log` VALUES (2, 38, 2, 10, '2025-11-09 13:52:06');
INSERT INTO `download_log` VALUES (3, 38, 3, 3, '2025-11-09 13:52:30');
INSERT INTO `download_log` VALUES (4, 1, 16, 3, '2025-11-16 23:26:20');
INSERT INTO `download_log` VALUES (5, 1, 3, 3, '2025-11-16 23:28:07');
INSERT INTO `download_log` VALUES (6, 1, 13, 5, '2025-11-16 23:46:28');
INSERT INTO `download_log` VALUES (7, 1, 2, 10, '2025-11-17 09:12:02');
INSERT INTO `download_log` VALUES (8, 1, 4, 8, '2025-11-18 15:51:06');
INSERT INTO `download_log` VALUES (9, 1, 1, 5, '2025-11-18 16:49:37');
INSERT INTO `download_log` VALUES (10, 53, 16, 3, '2025-11-20 19:26:29');
INSERT INTO `download_log` VALUES (11, 53, 4, 8, '2025-11-20 19:39:36');
INSERT INTO `download_log` VALUES (12, 57, 16, 3, '2025-12-18 15:39:16');
INSERT INTO `download_log` VALUES (13, 57, 4, 8, '2025-12-18 15:59:47');

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`  (
  `favorite_id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收藏对象类型（task/resource/question等）',
  `target_id` bigint NOT NULL COMMENT '收藏对象ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`favorite_id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 58 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (6, 48, 'task', 2, '2025-11-11 19:53:55');
INSERT INTO `favorite` VALUES (12, 1, 'activity', 19, '2025-11-15 14:38:42');
INSERT INTO `favorite` VALUES (20, 1, 'activity', 7, '2025-11-15 16:03:34');
INSERT INTO `favorite` VALUES (22, 1, 'activity', 15, '2025-11-15 17:22:35');
INSERT INTO `favorite` VALUES (23, 1, 'activity', 17, '2025-11-15 17:38:10');
INSERT INTO `favorite` VALUES (35, 1, 'activity', 73, '2025-11-18 16:52:19');
INSERT INTO `favorite` VALUES (47, 1, 'question', 263, '2025-12-05 09:27:05');
INSERT INTO `favorite` VALUES (48, 1, 'resource', 13, '2025-12-05 09:35:21');
INSERT INTO `favorite` VALUES (49, 1, 'question', 314, '2025-12-05 16:18:09');
INSERT INTO `favorite` VALUES (52, 1, 'resource', 16, '2025-12-15 19:56:39');
INSERT INTO `favorite` VALUES (53, 1, 'resource', 1, '2025-12-15 19:56:42');
INSERT INTO `favorite` VALUES (54, 1, 'resource', 2, '2025-12-15 20:09:03');

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `m_id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `msg_type` tinyint NOT NULL DEFAULT 1 COMMENT '消息类型：1-文本，2-图片，3-文件',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联类型: task/resource/question',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联对象ID',
  PRIMARY KEY (`m_id`) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_related`(`related_type` ASC, `related_id` ASC) USING BTREE,
  CONSTRAINT `fk_message_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '私信表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message
-- ----------------------------
INSERT INTO `message` VALUES (2, 25, 2, 'https://example.com/image.jpg', 2, 0, '2025-11-08 20:59:24', NULL, NULL);
INSERT INTO `message` VALUES (3, 25, 2, 'https://example.com/document.pdf', 3, 0, '2025-11-08 20:59:27', NULL, NULL);
INSERT INTO `message` VALUES (4, 26, 1, '好的,我有一些学习资料可以分享给你', 1, 1, '2025-11-08 20:59:30', NULL, NULL);
INSERT INTO `message` VALUES (5, 25, 2, '太好了,非常感谢!', 1, 0, '2025-11-08 20:59:32', NULL, NULL);
INSERT INTO `message` VALUES (6, 25, 1, '给自己发消息', 1, 1, '2025-11-08 21:00:30', NULL, NULL);
INSERT INTO `message` VALUES (7, 25, 3, '你好,用户3', 1, 0, '2025-11-08 21:00:40', NULL, NULL);
INSERT INTO `message` VALUES (8, 25, 2, '消息1', 1, 0, '2025-11-08 21:00:51', NULL, NULL);
INSERT INTO `message` VALUES (9, 25, 2, '消息2', 1, 0, '2025-11-08 21:00:53', NULL, NULL);
INSERT INTO `message` VALUES (10, 25, 2, '消息3', 1, 0, '2025-11-08 21:00:54', NULL, NULL);
INSERT INTO `message` VALUES (11, 25, 2, '消息4', 1, 0, '2025-11-08 21:00:55', NULL, NULL);
INSERT INTO `message` VALUES (12, 25, 2, '消息5', 1, 0, '2025-11-08 21:00:57', NULL, NULL);
INSERT INTO `message` VALUES (13, 1, 26, '11111', 1, 0, '2025-11-22 22:41:18', NULL, NULL);
INSERT INTO `message` VALUES (14, 1, 26, '111', 1, 0, '2025-11-23 15:20:05', NULL, NULL);
INSERT INTO `message` VALUES (15, 1, 26, '11111', 1, 0, '2025-11-23 15:24:13', NULL, NULL);
INSERT INTO `message` VALUES (16, 1, 25, '3243424', 1, 0, '2025-11-23 15:25:08', NULL, NULL);
INSERT INTO `message` VALUES (17, 1, 25, '43333333333333', 1, 0, '2025-11-23 15:31:19', NULL, NULL);
INSERT INTO `message` VALUES (18, 1, 25, '🐱🐱🐱🐱', 1, 0, '2025-11-23 15:49:26', NULL, NULL);
INSERT INTO `message` VALUES (19, 1, 25, '🐭🐯🐯🦁🤔🤐🤨🦁🦁', 1, 0, '2025-11-23 16:00:21', NULL, NULL);
INSERT INTO `message` VALUES (20, 1, 25, '🐹🐰2121🐭', 1, 0, '2025-11-23 16:03:13', NULL, NULL);
INSERT INTO `message` VALUES (21, 48, 1, '在吗', 1, 1, '2025-11-24 18:10:17', NULL, NULL);
INSERT INTO `message` VALUES (22, 1, 48, '在的', 1, 1, '2025-11-24 18:10:25', NULL, NULL);
INSERT INTO `message` VALUES (23, 1, 48, '明天出去玩？', 1, 1, '2025-11-24 18:10:45', NULL, NULL);
INSERT INTO `message` VALUES (24, 48, 1, '去哪里\n😋', 1, 1, '2025-11-24 18:17:30', NULL, NULL);
INSERT INTO `message` VALUES (25, 1, 48, '黄山', 1, 1, '2025-11-24 18:17:48', NULL, NULL);
INSERT INTO `message` VALUES (26, 48, 1, '可以', 1, 1, '2025-11-24 18:17:56', NULL, NULL);
INSERT INTO `message` VALUES (27, 48, 1, '221212121212', 1, 1, '2025-11-24 18:26:12', NULL, NULL);
INSERT INTO `message` VALUES (28, 48, 1, '1112121', 1, 1, '2025-11-24 18:32:38', NULL, NULL);
INSERT INTO `message` VALUES (29, 48, 1, '你好', 1, 1, '2025-11-24 19:43:04', NULL, NULL);
INSERT INTO `message` VALUES (31, 1, 48, '😛', 1, 1, '2025-11-24 19:44:24', NULL, NULL);
INSERT INTO `message` VALUES (32, 1, 48, '21212', 1, 1, '2025-11-24 19:44:40', NULL, NULL);
INSERT INTO `message` VALUES (33, 48, 1, '1', 1, 1, '2025-11-24 19:45:29', NULL, NULL);
INSERT INTO `message` VALUES (34, 1, 48, '1', 1, 1, '2025-11-24 19:45:39', NULL, NULL);
INSERT INTO `message` VALUES (35, 48, 1, '在吗', 1, 1, '2025-11-24 20:06:24', NULL, NULL);
INSERT INTO `message` VALUES (36, 1, 48, '在的', 1, 1, '2025-11-24 20:06:45', NULL, NULL);
INSERT INTO `message` VALUES (37, 1, 48, '在干嘛', 1, 1, '2025-11-24 20:06:54', NULL, NULL);
INSERT INTO `message` VALUES (38, 1, 48, '还', 1, 1, '2025-11-24 20:07:01', NULL, NULL);
INSERT INTO `message` VALUES (39, 1, 48, '还在吗', 1, 1, '2025-11-24 20:07:26', NULL, NULL);
INSERT INTO `message` VALUES (40, 48, 1, '🍊', 1, 1, '2025-11-24 20:07:45', NULL, NULL);
INSERT INTO `message` VALUES (41, 1, 48, 'https://campuslink01.oss-cn-hangzhou.aliyuncs.com/uploads/a78ffa7edaf547af967a9f86b56f03a5', 2, 1, '2025-11-24 20:53:50', NULL, NULL);
INSERT INTO `message` VALUES (42, 1, 48, '{\"url\":\"https://campuslink01.oss-cn-hangzhou.aliyuncs.com/uploads/b7c35328af0d4ed199a9ebda8d739f80.txt\",\"name\":\"1763454214658.txt\",\"size\":83}', 3, 1, '2025-11-24 20:54:21', NULL, NULL);
INSERT INTO `message` VALUES (43, 1, 48, '{\"url\":\"https://campuslink01.oss-cn-hangzhou.aliyuncs.com/uploads/1876cdeb24214aa6b37f6aa034047ea0.zip\",\"name\":\"download.zip\",\"size\":1819}', 3, 1, '2025-11-24 20:55:18', NULL, NULL);
INSERT INTO `message` VALUES (44, 48, 1, '1121212212', 1, 1, '2025-11-24 21:20:56', NULL, NULL);
INSERT INTO `message` VALUES (45, 48, 1, '2', 1, 1, '2025-11-24 21:21:00', NULL, NULL);
INSERT INTO `message` VALUES (46, 48, 1, '我觉得还不错', 1, 1, '2025-11-25 16:20:13', NULL, NULL);
INSERT INTO `message` VALUES (47, 48, 1, '你觉得呢', 1, 1, '2025-11-25 16:20:25', NULL, NULL);
INSERT INTO `message` VALUES (48, 48, 1, '1', 1, 1, '2025-11-25 16:20:36', NULL, NULL);
INSERT INTO `message` VALUES (49, 48, 1, '1', 1, 1, '2025-11-25 16:20:39', NULL, NULL);
INSERT INTO `message` VALUES (50, 48, 1, '1', 1, 1, '2025-11-25 16:20:51', NULL, NULL);
INSERT INTO `message` VALUES (51, 48, 1, '1', 1, 1, '2025-11-25 16:20:53', NULL, NULL);
INSERT INTO `message` VALUES (52, 48, 1, '1', 1, 1, '2025-11-25 16:21:10', NULL, NULL);
INSERT INTO `message` VALUES (53, 1, 48, '11111', 1, 1, '2025-11-25 16:26:22', NULL, NULL);
INSERT INTO `message` VALUES (54, 48, 1, '112', 1, 1, '2025-11-25 16:26:39', NULL, NULL);
INSERT INTO `message` VALUES (55, 48, 1, '1', 1, 1, '2025-11-25 17:04:37', NULL, NULL);
INSERT INTO `message` VALUES (56, 48, 1, '1', 1, 1, '2025-11-25 17:17:14', NULL, NULL);
INSERT INTO `message` VALUES (57, 1, 48, '1', 1, 1, '2025-11-25 17:17:50', NULL, NULL);
INSERT INTO `message` VALUES (58, 1, 48, '1', 1, 1, '2025-11-25 17:46:27', NULL, NULL);
INSERT INTO `message` VALUES (59, 48, 1, '1', 1, 1, '2025-11-25 17:46:33', NULL, NULL);
INSERT INTO `message` VALUES (60, 48, 1, '1', 1, 1, '2025-11-25 17:46:37', NULL, NULL);
INSERT INTO `message` VALUES (61, 48, 1, '1', 1, 1, '2025-11-25 17:46:46', NULL, NULL);
INSERT INTO `message` VALUES (62, 48, 1, '1', 1, 1, '2025-11-25 17:46:48', NULL, NULL);
INSERT INTO `message` VALUES (63, 48, 1, '在吗', 1, 1, '2025-11-29 17:09:22', NULL, NULL);
INSERT INTO `message` VALUES (64, 48, 1, '在吗', 1, 1, '2025-11-29 17:19:21', NULL, NULL);
INSERT INTO `message` VALUES (65, 48, 1, '1111111', 1, 1, '2025-11-29 17:30:42', NULL, NULL);
INSERT INTO `message` VALUES (66, 48, 1, '11111111111111111', 1, 1, '2025-11-29 17:31:06', NULL, NULL);
INSERT INTO `message` VALUES (67, 1, 48, '知道了', 1, 1, '2025-11-29 17:31:19', NULL, NULL);
INSERT INTO `message` VALUES (68, 48, 1, '在干嘛', 1, 1, '2025-11-29 17:31:42', NULL, NULL);
INSERT INTO `message` VALUES (69, 48, 1, '你在哪呢', 1, 1, '2025-11-30 09:47:54', NULL, NULL);
INSERT INTO `message` VALUES (70, 48, 1, '1', 1, 1, '2025-11-30 10:26:28', NULL, NULL);
INSERT INTO `message` VALUES (71, 48, 1, '1', 1, 1, '2025-11-30 10:26:28', NULL, NULL);
INSERT INTO `message` VALUES (72, 48, 1, '1', 1, 1, '2025-11-30 10:26:29', NULL, NULL);
INSERT INTO `message` VALUES (73, 48, 1, '1', 1, 1, '2025-11-30 10:26:31', NULL, NULL);
INSERT INTO `message` VALUES (74, 48, 1, '1', 1, 1, '2025-11-30 10:26:32', NULL, NULL);
INSERT INTO `message` VALUES (75, 48, 1, '1', 1, 1, '2025-11-30 10:26:33', NULL, NULL);
INSERT INTO `message` VALUES (76, 48, 1, '11', 1, 1, '2025-11-30 10:26:34', NULL, NULL);
INSERT INTO `message` VALUES (77, 48, 1, '11111', 1, 1, '2025-11-30 10:26:37', NULL, NULL);
INSERT INTO `message` VALUES (78, 48, 1, '1111', 1, 1, '2025-11-30 10:26:38', NULL, NULL);
INSERT INTO `message` VALUES (79, 48, 1, '1111', 1, 1, '2025-11-30 10:26:39', NULL, NULL);
INSERT INTO `message` VALUES (80, 48, 1, '1111', 1, 1, '2025-11-30 10:29:53', NULL, NULL);
INSERT INTO `message` VALUES (81, 48, 1, '1111', 1, 1, '2025-11-30 10:29:54', NULL, NULL);
INSERT INTO `message` VALUES (82, 48, 1, '1111', 1, 1, '2025-11-30 10:29:57', NULL, NULL);
INSERT INTO `message` VALUES (83, 48, 1, '1111', 1, 1, '2025-11-30 10:29:57', NULL, NULL);
INSERT INTO `message` VALUES (84, 48, 1, '1111', 1, 1, '2025-11-30 10:29:58', NULL, NULL);
INSERT INTO `message` VALUES (85, 48, 1, '1111', 1, 1, '2025-11-30 10:29:59', NULL, NULL);
INSERT INTO `message` VALUES (86, 48, 1, '1111', 1, 1, '2025-11-30 10:30:01', NULL, NULL);
INSERT INTO `message` VALUES (87, 48, 1, '1111', 1, 1, '2025-11-30 10:30:02', NULL, NULL);
INSERT INTO `message` VALUES (88, 48, 1, '1111', 1, 1, '2025-11-30 10:30:03', NULL, NULL);
INSERT INTO `message` VALUES (89, 48, 1, '1111', 1, 1, '2025-11-30 10:30:04', NULL, NULL);
INSERT INTO `message` VALUES (90, 48, 1, '1111', 1, 1, '2025-11-30 10:30:05', NULL, NULL);
INSERT INTO `message` VALUES (91, 48, 1, '1111', 1, 1, '2025-11-30 10:30:06', NULL, NULL);
INSERT INTO `message` VALUES (92, 48, 1, '1111', 1, 1, '2025-11-30 10:30:07', NULL, NULL);
INSERT INTO `message` VALUES (93, 48, 1, '1111', 1, 1, '2025-11-30 10:30:07', NULL, NULL);
INSERT INTO `message` VALUES (94, 48, 1, '1111', 1, 1, '2025-11-30 10:30:08', NULL, NULL);
INSERT INTO `message` VALUES (95, 48, 1, '1111', 1, 1, '2025-11-30 10:30:09', NULL, NULL);
INSERT INTO `message` VALUES (96, 48, 1, '1111', 1, 1, '2025-11-30 10:30:10', NULL, NULL);
INSERT INTO `message` VALUES (97, 48, 1, '1111', 1, 1, '2025-11-30 10:30:11', NULL, NULL);
INSERT INTO `message` VALUES (98, 48, 1, '1111', 1, 1, '2025-11-30 10:30:12', NULL, NULL);
INSERT INTO `message` VALUES (99, 48, 1, '1111', 1, 1, '2025-11-30 10:30:14', NULL, NULL);

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `notification_id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收者ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知内容',
  `notify_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型（system/answer/task/activity等）',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联对象类型',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联对象ID',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`notification_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 60 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification
-- ----------------------------
INSERT INTO `notification` VALUES (3, 2, '新任务分配', '你被分配了新任务「完成图书馆志愿服务」，请在本周五前完成。', 'task', 'task', 1, 0, '2025-11-08 21:15:35');
INSERT INTO `notification` VALUES (5, 2, '你收到了新私信', '用户「通知测试用户1」给你发送了私信', 'message', 'message', 1, 0, '2025-11-08 21:15:40');
INSERT INTO `notification` VALUES (7, 3, '社团活动通知', '计算机协会将于本周六举行技术分享会，欢迎参加。', 'club', 'club', 1, 0, '2025-11-08 21:15:47');
INSERT INTO `notification` VALUES (9, 2, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (10, 3, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (11, 4, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (12, 5, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (13, 6, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (14, 18, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (15, 19, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (16, 20, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (17, 21, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (18, 22, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (19, 23, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (20, 24, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (21, 25, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (22, 26, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (23, 27, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (24, 28, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (26, 30, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (27, 31, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (28, 32, '平台公告', '欢迎使用CampusLink平台！我们致力于为高校师生提供便捷的资源互助与问答服务。', 'system', NULL, NULL, 0, '2025-11-08 21:15:50');
INSERT INTO `notification` VALUES (29, 2, '测试通知', '这是一条测试通知', 'system', NULL, NULL, 0, '2025-11-08 21:15:54');
INSERT INTO `notification` VALUES (30, 2, '测试通知', '这是一条测试通知', 'system', NULL, NULL, 0, '2025-11-08 21:16:07');
INSERT INTO `notification` VALUES (31, 2, '测试通知', '这是一条测试通知', 'system', NULL, NULL, 0, '2025-11-08 21:16:35');
INSERT INTO `notification` VALUES (34, 48, '任务已完成', '您完成的任务「测试完成通知」已被确认，获得 20 积分奖励！', 'task', 'task', 20, 1, '2025-11-22 11:47:56');
INSERT INTO `notification` VALUES (36, 1, '您的任务有人接单了', '用户 2164601212 接受了您的任务「测试快捷操作按钮」', 'task', 'task', 21, 1, '2025-11-22 14:28:32');
INSERT INTO `notification` VALUES (37, 1, '任务已被放弃', '您发布的任务「测试快捷操作按钮」已被接单者放弃,任务重新回到待接单状态', 'task', 'task', 21, 1, '2025-11-22 14:28:58');
INSERT INTO `notification` VALUES (38, 1, '您的任务有人接单了', '用户 2164601212 接受了您的任务「测试快捷操作按钮」', 'task', 'task', 21, 1, '2025-11-22 14:29:08');
INSERT INTO `notification` VALUES (39, 1, '任务已被放弃', '您发布的任务「测试快捷操作按钮」已被接单者放弃,任务重新回到待接单状态', 'task', 'task', 21, 1, '2025-11-22 14:29:10');
INSERT INTO `notification` VALUES (40, 1, '您的任务有人接单了', '用户 2164601212 接受了您的任务「测试快捷操作按钮」', 'task', 'task', 21, 1, '2025-11-22 14:31:59');
INSERT INTO `notification` VALUES (41, 1, '任务已被放弃', '您发布的任务「测试快捷操作按钮」已被接单者放弃,任务重新回到待接单状态', 'task', 'task', 21, 1, '2025-11-22 14:32:00');
INSERT INTO `notification` VALUES (42, 1, '您的任务有人接单了', '用户 2164601212 接受了您的任务「测试快捷操作按钮」', 'task', 'task', 21, 1, '2025-11-22 14:41:28');
INSERT INTO `notification` VALUES (43, 1, '任务已被放弃', '您发布的任务「测试快捷操作按钮」已被接单者放弃,任务重新回到待接单状态', 'task', 'task', 21, 1, '2025-11-22 14:41:30');
INSERT INTO `notification` VALUES (44, 1, '您的任务有人接单了', '用户 2164601212 接受了您的任务「测试快捷操作按钮」', 'task', 'task', 21, 1, '2025-11-22 14:41:47');
INSERT INTO `notification` VALUES (45, 1, '任务已被放弃', '您发布的任务「测试快捷操作按钮」已被接单者放弃,任务重新回到待接单状态', 'task', 'task', 21, 1, '2025-11-22 14:41:57');
INSERT INTO `notification` VALUES (46, 1, '您的任务有人接单了', '用户 2164601212 接受了您的任务「测试快捷操作按钮」', 'task', 'task', 21, 1, '2025-11-22 14:50:23');
INSERT INTO `notification` VALUES (47, 1, '任务已被放弃', '您发布的任务「测试快捷操作按钮」已被接单者放弃,任务重新回到待接单状态', 'task', 'task', 21, 1, '2025-11-22 14:50:48');
INSERT INTO `notification` VALUES (48, 1, '您的任务有人接单了', '用户 2164601212 接受了您的任务「测试快捷操作按钮」', 'task', 'task', 21, 1, '2025-11-22 14:51:02');
INSERT INTO `notification` VALUES (49, 1, '任务已被放弃', '您发布的任务「测试快捷操作按钮」已被接单者放弃,任务重新回到待接单状态', 'task', 'task', 21, 1, '2025-11-22 14:57:40');
INSERT INTO `notification` VALUES (50, 1, '您的任务有人接单了', '用户 2164601212 接受了您的任务「测试快捷操作按钮」', 'task', 'task', 21, 1, '2025-11-22 14:57:44');
INSERT INTO `notification` VALUES (51, 48, '任务已完成', '您完成的任务「测试快捷操作按钮」已被确认，获得 20 积分奖励！', 'task', 'task', 21, 1, '2025-11-22 15:13:19');
INSERT INTO `notification` VALUES (52, 1, '任务已确认完成', '您发布的任务「测试快捷操作按钮」已确认完成', 'task', 'task', 21, 1, '2025-11-22 15:13:19');
INSERT INTO `notification` VALUES (53, 1, '您收到了新私信', '2164601212 给您发送了私信', 'message', 'MESSAGE', 48, 1, '2025-11-29 17:30:42');
INSERT INTO `notification` VALUES (54, 1, '您收到了新私信', '2164601212 给您发送了私信', 'message', 'MESSAGE', 48, 1, '2025-11-29 17:31:06');
INSERT INTO `notification` VALUES (55, 48, '您收到了新私信', 'Yuan0919 给您发送了私信', 'message', 'MESSAGE', 1, 0, '2025-11-29 17:31:19');
INSERT INTO `notification` VALUES (56, 1, '您收到了新私信', '2164601212 给您发送了私信', 'message', 'MESSAGE', 48, 1, '2025-11-29 17:31:42');
INSERT INTO `notification` VALUES (57, 1, '您收到了新私信', '2164601212: 你在哪呢', 'message', 'MESSAGE', 48, 1, '2025-11-30 09:47:55');
INSERT INTO `notification` VALUES (58, 1, '您收到了新私信', '2164601212 给您发送了多条私信', 'message', 'MESSAGE', 48, 1, '2025-11-30 10:26:39');
INSERT INTO `notification` VALUES (59, 1, '您收到了新私信', '2164601212 给您发送了多条私信', 'message', 'MESSAGE', 48, 1, '2025-11-30 10:30:14');

-- ----------------------------
-- Table structure for points_log
-- ----------------------------
DROP TABLE IF EXISTS `points_log`;
CREATE TABLE `points_log`  (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `points_change` int NOT NULL COMMENT '积分变化（正数为增加，负数为减少）',
  `points_after` int NOT NULL COMMENT '变化后的积分',
  `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '变化原因',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联对象类型',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联对象ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_points_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 68 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '积分记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of points_log
-- ----------------------------
INSERT INTO `points_log` VALUES (1, 23, -10, 90, '发布任务：测试任务 - 用于验证积分日志', 'task', 7, '2025-11-08 20:27:10');
INSERT INTO `points_log` VALUES (2, 24, 10, 110, '完成任务：测试任务 - 用于验证积分日志', 'task', 7, '2025-11-08 20:27:42');
INSERT INTO `points_log` VALUES (3, 23, -5, 85, '发布任务：测试任务2 - 用于取消', 'task', 8, '2025-11-08 20:27:53');
INSERT INTO `points_log` VALUES (4, 23, 5, 90, '取消任务退还积分：测试任务2 - 用于取消', 'task', 8, '2025-11-08 20:27:55');
INSERT INTO `points_log` VALUES (5, 49, 10, 110, '每日签到', 'check_in', 49, '2025-11-12 21:14:55');
INSERT INTO `points_log` VALUES (6, 49, 10, 120, '每日签到', 'check_in', 49, '2025-11-13 15:17:28');
INSERT INTO `points_log` VALUES (7, 48, 10, 110, '每日签到', 'check_in', 48, '2025-11-13 15:18:11');
INSERT INTO `points_log` VALUES (8, 50, 10, 110, '每日签到', 'check_in', 50, '2025-11-13 15:22:00');
INSERT INTO `points_log` VALUES (9, 51, 10, 110, '每日签到', 'check_in', 51, '2025-11-13 15:29:46');
INSERT INTO `points_log` VALUES (10, 1, 10, 10010, '每日签到', 'check_in', 1, '2025-11-13 19:02:00');
INSERT INTO `points_log` VALUES (11, 48, 10, 120, '每日签到', 'check_in', 48, '2025-11-14 10:32:33');
INSERT INTO `points_log` VALUES (12, 1, 10, 10020, '每日签到', 'check_in', 1, '2025-11-15 09:21:46');
INSERT INTO `points_log` VALUES (13, 48, 10, 130, '每日签到', 'check_in', 48, '2025-11-15 20:00:21');
INSERT INTO `points_log` VALUES (14, 1, 10, 9996, '每日签到', 'check_in', 1, '2025-11-18 16:52:33');
INSERT INTO `points_log` VALUES (15, 1, 10, 10006, '每日签到', 'check_in', 1, '2025-11-19 14:19:55');
INSERT INTO `points_log` VALUES (16, 48, 10, 140, '每日签到', 'check_in', 48, '2025-11-19 16:31:35');
INSERT INTO `points_log` VALUES (17, 1, 10, 9986, '每日签到', 'check_in', 1, '2025-11-20 11:44:18');
INSERT INTO `points_log` VALUES (18, 1, -11, 9975, '发布任务：2111111111111111', 'task', 10, '2025-11-20 12:50:58');
INSERT INTO `points_log` VALUES (19, 1, 11, 9986, '取消任务退还积分：2111111111111111', 'task', 10, '2025-11-20 12:51:34');
INSERT INTO `points_log` VALUES (20, 55, 10, 110, '每日签到', 'check_in', 55, '2025-11-20 19:02:39');
INSERT INTO `points_log` VALUES (21, 1, 10, 9808, '每日签到', 'check_in', 1, '2025-11-21 13:39:09');
INSERT INTO `points_log` VALUES (22, 48, 10, 150, '每日签到', 'check_in', 48, '2025-11-21 13:49:07');
INSERT INTO `points_log` VALUES (23, 1, -112, 9594, '发布任务：测试接单任务', 'task', 11, '2025-11-22 10:08:54');
INSERT INTO `points_log` VALUES (24, 1, 112, 9706, '取消任务退还积分：测试接单任务', 'task', 11, '2025-11-22 10:14:49');
INSERT INTO `points_log` VALUES (25, 1, -12, 9694, '发布任务：测试截单时间任务', 'task', 12, '2025-11-22 10:15:37');
INSERT INTO `points_log` VALUES (26, 1, -12, 9682, '发布任务：测试截单时间任务', 'task', 13, '2025-11-22 10:16:16');
INSERT INTO `points_log` VALUES (27, 1, -12, 9670, '发布任务：测试截止时间显示', 'task', 14, '2025-11-22 10:34:53');
INSERT INTO `points_log` VALUES (28, 48, 12, 287, '完成任务：测试截止时间显示', 'task', 14, '2025-11-22 10:36:16');
INSERT INTO `points_log` VALUES (29, 1, -12, 9658, '发布任务：测试截止时间显示1', 'task', 15, '2025-11-22 10:40:13');
INSERT INTO `points_log` VALUES (30, 1, 12, 9670, '取消任务退还积分：测试截止时间显示1', 'task', 15, '2025-11-22 10:40:17');
INSERT INTO `points_log` VALUES (31, 1, -23, 9671, '发布任务：测试地点发布', 'task', 16, '2025-11-22 11:02:51');
INSERT INTO `points_log` VALUES (32, 48, 23, 310, '完成任务：测试地点发布', 'task', 16, '2025-11-22 11:03:22');
INSERT INTO `points_log` VALUES (33, 1, -20, 9651, '发布任务：测试地点发布1', 'task', 17, '2025-11-22 11:05:08');
INSERT INTO `points_log` VALUES (34, 1, 20, 9671, '取消任务退还积分：测试地点发布1', 'task', 17, '2025-11-22 11:09:36');
INSERT INTO `points_log` VALUES (35, 1, -20, 9651, '发布任务：测试地点发布', 'task', 18, '2025-11-22 11:10:16');
INSERT INTO `points_log` VALUES (36, 48, 20, 330, '完成任务：测试地点发布', 'task', 18, '2025-11-22 11:23:51');
INSERT INTO `points_log` VALUES (37, 1, -20, 9631, '发布任务：测试接单通知', 'task', 19, '2025-11-22 11:24:34');
INSERT INTO `points_log` VALUES (38, 48, 20, 350, '完成任务：测试接单通知', 'task', 19, '2025-11-22 11:41:34');
INSERT INTO `points_log` VALUES (39, 1, -20, 9611, '发布任务：测试完成通知', 'task', 20, '2025-11-22 11:47:42');
INSERT INTO `points_log` VALUES (40, 48, 20, 370, '完成任务：测试完成通知', 'task', 20, '2025-11-22 11:47:56');
INSERT INTO `points_log` VALUES (41, 1, 10, 9621, '每日签到', 'check_in', 1, '2025-11-22 11:51:27');
INSERT INTO `points_log` VALUES (42, 1, -20, 9601, '发布任务：测试快捷操作按钮', 'task', 21, '2025-11-22 14:28:21');
INSERT INTO `points_log` VALUES (43, 48, 20, 390, '完成任务：测试快捷操作按钮', 'task', 21, '2025-11-22 15:13:19');
INSERT INTO `points_log` VALUES (44, 48, 10, 400, '参与活动签到：测试签到功能', 'activity', 79, '2025-11-22 21:28:00');
INSERT INTO `points_log` VALUES (45, 1, 10, 9611, '每日签到', 'check_in', 1, '2025-11-23 17:38:33');
INSERT INTO `points_log` VALUES (46, 1, 10, 9621, '每日签到', 'check_in', 1, '2025-11-24 07:54:58');
INSERT INTO `points_log` VALUES (47, 48, 10, 410, '每日签到', 'check_in', 48, '2025-11-24 18:01:14');
INSERT INTO `points_log` VALUES (48, 1, 10, 9651, '每日签到', 'check_in', 1, '2025-11-25 19:21:11');
INSERT INTO `points_log` VALUES (49, 1, 10, 9661, '每日签到', 'check_in', 1, '2025-11-26 09:01:30');
INSERT INTO `points_log` VALUES (50, 1, 10, 9671, '每日签到', 'check_in', 1, '2025-11-27 13:29:08');
INSERT INTO `points_log` VALUES (51, 1, 10, 9681, '每日签到', 'check_in', 1, '2025-11-29 11:08:23');
INSERT INTO `points_log` VALUES (52, 48, 10, 420, '每日签到', 'check_in', 48, '2025-11-29 16:36:19');
INSERT INTO `points_log` VALUES (53, 1, 10, 9691, '每日签到', 'check_in', 1, '2025-11-30 12:21:40');
INSERT INTO `points_log` VALUES (54, 1, 10, 9701, '每日签到', 'check_in', 1, '2025-12-05 09:02:24');
INSERT INTO `points_log` VALUES (55, 1, 10, 9706, '每日签到', 'check_in', 1, '2025-12-08 17:18:01');
INSERT INTO `points_log` VALUES (56, 1, 10, 9716, '每日签到', 'check_in', 1, '2025-12-09 14:47:40');
INSERT INTO `points_log` VALUES (57, 1, 10, 9743, '每日签到', 'check_in', 1, '2025-12-10 14:16:56');
INSERT INTO `points_log` VALUES (58, 56, 10, 110, '每日签到', 'check_in', 56, '2025-12-12 14:45:25');
INSERT INTO `points_log` VALUES (59, 1, 10, 9758, '每日签到', 'check_in', 1, '2025-12-12 21:20:18');
INSERT INTO `points_log` VALUES (60, 1, 10, 9768, '每日签到', 'check_in', 1, '2025-12-17 15:51:52');
INSERT INTO `points_log` VALUES (61, 57, 100, 100, '注册奖励', 'register', 57, '2025-12-18 10:58:16');
INSERT INTO `points_log` VALUES (62, 1, 10, 9778, '每日签到', 'check_in', 1, '2025-12-29 14:50:34');
INSERT INTO `points_log` VALUES (63, 1, 10, 9788, '每日签到', 'check_in', 1, '2025-12-30 20:05:39');
INSERT INTO `points_log` VALUES (64, 1, 10, 9798, '每日签到', 'check_in', 1, '2026-01-02 16:34:23');
INSERT INTO `points_log` VALUES (65, 1, 10, 9808, '每日签到', 'check_in', 1, '2026-01-03 21:35:05');
INSERT INTO `points_log` VALUES (66, 1, 10, 9818, '每日签到', 'check_in', 1, '2026-01-07 13:46:22');
INSERT INTO `points_log` VALUES (67, 1, 10, 9828, '每日签到', 'check_in', 1, '2026-01-11 12:46:30');

-- ----------------------------
-- Table structure for question
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question`  (
  `q_id` bigint NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问题标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问题内容',
  `asker_id` bigint NOT NULL COMMENT '提问者ID',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类（学习/生活/技术等）',
  `tags` json NULL COMMENT '标签（JSON数组）',
  `images` json NULL COMMENT '图片URL列表（JSON数组）',
  `ai_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'AI生成的答案',
  `ai_generated_at` datetime NULL DEFAULT NULL COMMENT 'AI答案生成时间',
  `views` int NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `answer_count` int NOT NULL DEFAULT 0 COMMENT '回答数量',
  `is_solved` tinyint NOT NULL DEFAULT 0 COMMENT '是否已解决：0-否，1-是',
  `reward_points` int NOT NULL DEFAULT 0 COMMENT '悬赏积分',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`q_id`) USING BTREE,
  INDEX `idx_asker_id`(`asker_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_is_solved`(`is_solved` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_question_asker` FOREIGN KEY (`asker_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 319 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '问题表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of question
-- ----------------------------
INSERT INTO `question` VALUES (1, '如何学习数据结构？', '我是大一新生，想学习数据结构，有什么好的建议吗？需要掌握哪些基础知识？', 5, '学习', '[\"数据结构\", \"学习方法\"]', NULL, NULL, NULL, 160, 3, 1, 10, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `question` VALUES (2, 'Python和Java哪个更适合初学者？', '我想学习编程，但不知道选Python还是Java，请大家给点建议', 5, '技术', '[\"Python\", \"Java\", \"编程语言\"]', NULL, NULL, NULL, 203, 5, 0, 5, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `question` VALUES (3, '图书馆自习室怎么预约？', '请问学校图书馆的自习室需要提前预约吗？怎么预约？', 5, '生活', '[\"图书馆\", \"自习室\"]', NULL, NULL, NULL, 81, 2, 1, 0, 1, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `question` VALUES (4, '考研需要准备多久？', '打算考研，想问问学长学姐们一般需要准备多长时间？', 2, '学习', '[\"考研\", \"备考\"]', NULL, NULL, NULL, 303, 9, 0, 15, 1, '2025-11-08 19:09:44', '2025-11-19 13:41:24');
INSERT INTO `question` VALUES (105, '高等数学极限问题求解', '请问lim(x→0) (sin x)/x 怎么证明等于1？老师讲的太快没听懂', 1, '学习', '[\"高等数学\", \"极限\", \"微积分\"]', NULL, NULL, NULL, 45, 3, 1, 5, 1, '2025-10-20 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (106, '线性代数矩阵求逆方法', '3x3矩阵求逆除了伴随矩阵法还有什么更快的方法吗？', 2, '学习', '[\"线性代数\", \"矩阵\"]', NULL, NULL, NULL, 32, 2, 1, 10, 1, '2025-10-22 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (107, '大学英语四级备考经验', '12月份要考四级了，有什么高效的备考方法推荐吗？', 3, '学习', '[\"英语\", \"四级\", \"备考\"]', NULL, NULL, NULL, 129, 8, 1, 0, 1, '2025-10-25 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (108, '数据结构二叉树遍历', '前序、中序、后序遍历的递归和非递归实现有什么区别？', 4, '学习', '[\"数据结构\", \"二叉树\", \"算法\"]', NULL, NULL, NULL, 67, 5, 1, 15, 1, '2025-10-27 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (109, '概率论贝叶斯公式应用', '贝叶斯公式在实际问题中怎么应用？能举个例子吗？', 5, '学习', '[\"概率论\", \"贝叶斯\"]', NULL, NULL, NULL, 54, 4, 1, 8, 1, '2025-10-28 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (110, '操作系统进程调度算法', '先来先服务、短作业优先、时间片轮转各有什么优缺点？', 6, '学习', '[\"操作系统\", \"进程调度\"]', NULL, NULL, NULL, 90, 6, 1, 12, 1, '2025-10-30 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (111, '计算机网络TCP三次握手', 'TCP为什么需要三次握手而不是两次？四次挥手呢？', 18, '学习', '[\"计算机网络\", \"TCP\"]', NULL, NULL, NULL, 156, 9, 1, 10, 1, '2025-11-01 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (112, 'Java多线程synchronized关键字', 'synchronized锁的是对象还是代码块？有什么区别？', 19, '学习', '[\"Java\", \"多线程\", \"并发\"]', NULL, NULL, NULL, 78, 5, 1, 20, 1, '2025-11-02 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (113, 'Python爬虫反爬机制', '爬取网站数据时遇到403错误，怎么绕过反爬虫机制？', 20, '学习', '[\"Python\", \"爬虫\", \"反爬\"]', NULL, NULL, NULL, 234, 12, 1, 15, 1, '2025-11-04 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (114, '数据库索引优化问题', 'MySQL什么情况下索引会失效？怎么优化查询性能？', 21, '学习', '[\"数据库\", \"MySQL\", \"索引\"]', NULL, NULL, NULL, 145, 7, 1, 25, 1, '2025-11-05 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (115, '机器学习过拟合问题', '训练集准确率很高但测试集很低，怎么解决过拟合？', 22, '学习', '[\"机器学习\", \"过拟合\"]', NULL, NULL, NULL, 198, 10, 1, 30, 1, '2025-11-07 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (116, '微积分定积分计算技巧', '∫(0→π) sin²x dx 怎么计算？有什么技巧吗？', 23, '学习', '[\"微积分\", \"定积分\"]', NULL, NULL, NULL, 56, 4, 1, 5, 1, '2025-11-08 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (117, '离散数学图论最短路径', 'Dijkstra算法和Floyd算法有什么区别？什么时候用哪个？', 24, '学习', '[\"离散数学\", \"图论\", \"算法\"]', NULL, NULL, NULL, 92, 6, 1, 15, 1, '2025-11-09 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (118, '编译原理词法分析', '正则表达式和有限自动机的关系是什么？', 25, '学习', '[\"编译原理\", \"词法分析\"]', NULL, NULL, NULL, 43, 3, 0, 10, 1, '2025-11-10 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (119, '软件工程UML类图', 'UML类图中的关联、聚合、组合有什么区别？', 26, '学习', '[\"软件工程\", \"UML\"]', NULL, NULL, NULL, 67, 5, 1, 8, 1, '2025-11-11 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (120, '算法设计动态规划', '背包问题的动态规划解法能详细讲解一下吗？', 27, '学习', '[\"算法\", \"动态规划\", \"背包问题\"]', NULL, NULL, NULL, 134, 8, 1, 20, 1, '2025-11-12 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (121, '人工智能神经网络', '反向传播算法的数学原理是什么？', 28, '学习', '[\"人工智能\", \"神经网络\"]', NULL, NULL, NULL, 176, 9, 1, 25, 1, '2025-11-13 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (122, '大学物理电磁学', '麦克斯韦方程组的物理意义是什么？', 29, '学习', '[\"大学物理\", \"电磁学\"]', NULL, NULL, NULL, 54, 4, 1, 10, 1, '2025-11-14 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (123, '有机化学反应机理', '亲电加成和亲核取代的区别是什么？', 30, '学习', '[\"有机化学\", \"反应机理\"]', NULL, NULL, NULL, 38, 2, 0, 5, 1, '2025-11-15 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (124, '经济学宏观调控', '财政政策和货币政策有什么区别？', 31, '学习', '[\"经济学\", \"宏观调控\"]', NULL, NULL, NULL, 89, 6, 1, 8, 1, '2025-11-16 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (125, '会计学借贷记账法', '为什么资产增加记借方，负债增加记贷方？', 32, '学习', '[\"会计学\", \"借贷记账\"]', NULL, NULL, NULL, 72, 5, 1, 5, 1, '2025-11-17 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (126, '管理学组织结构', '直线职能制和事业部制各有什么优缺点？', 37, '学习', '[\"管理学\", \"组织结构\"]', NULL, NULL, NULL, 45, 3, 0, 10, 1, '2025-11-18 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (127, '统计学假设检验', 'p值小于0.05就一定拒绝原假设吗？', 38, '学习', '[\"统计学\", \"假设检验\"]', NULL, NULL, NULL, 61, 4, 1, 8, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (128, '电路分析基尔霍夫定律', '基尔霍夫电压定律和电流定律怎么应用？', 39, '学习', '[\"电路分析\", \"基尔霍夫\"]', NULL, NULL, NULL, 34, 2, 0, 5, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (129, '信号与系统傅里叶变换', '时域和频域的关系怎么理解？', 40, '学习', '[\"信号与系统\", \"傅里叶变换\"]', NULL, NULL, NULL, 98, 7, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (130, '数字电路组合逻辑', '用与非门实现或门的电路怎么设计？', 41, '学习', '[\"数字电路\", \"组合逻辑\"]', NULL, NULL, NULL, 52, 3, 1, 10, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (131, '模拟电路运算放大器', '反相放大器和同相放大器的区别是什么？', 42, '学习', '[\"模拟电路\", \"运放\"]', NULL, NULL, NULL, 49, 4, 0, 8, 1, '2025-11-19 14:01:14', '2025-12-12 14:00:02');
INSERT INTO `question` VALUES (132, '通信原理调制解调', 'AM调制和FM调制有什么区别？', 43, '学习', '[\"通信原理\", \"调制\"]', NULL, NULL, NULL, 56, 4, 1, 10, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (133, '自动控制原理稳定性', '劳斯判据怎么判断系统稳定性？', 44, '学习', '[\"自动控制\", \"稳定性\"]', NULL, NULL, NULL, 43, 3, 0, 12, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (134, '嵌入式系统中断处理', 'ARM Cortex-M的中断优先级怎么设置？', 45, '学习', '[\"嵌入式\", \"中断\"]', NULL, NULL, NULL, 67, 5, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (135, '计算机组成原理流水线', 'CPU流水线的数据冒险怎么解决？', 46, '学习', '[\"计算机组成\", \"流水线\"]', NULL, NULL, NULL, 89, 6, 1, 18, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (136, '汇编语言寻址方式', 'x86汇编的寻址方式有哪几种？', 47, '学习', '[\"汇编语言\", \"寻址\"]', NULL, NULL, NULL, 38, 2, 0, 8, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (137, '软件测试白盒测试', '语句覆盖、分支覆盖、路径覆盖的区别？', 48, '学习', '[\"软件测试\", \"白盒测试\"]', NULL, NULL, NULL, 55, 4, 1, 10, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (138, '项目管理关键路径', 'CPM和PERT方法有什么区别？', 49, '学习', '[\"项目管理\", \"关键路径\"]', NULL, NULL, NULL, 41, 3, 0, 8, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (139, '信息安全RSA加密', 'RSA加密算法的数学原理是什么？', 50, '学习', '[\"信息安全\", \"RSA\"]', NULL, NULL, NULL, 123, 7, 1, 20, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (140, '云计算虚拟化技术', 'Docker和虚拟机有什么区别？', 51, '学习', '[\"云计算\", \"虚拟化\", \"Docker\"]', NULL, NULL, NULL, 187, 10, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (141, '大数据MapReduce', 'MapReduce的工作原理能详细讲解吗？', 1, '学习', '[\"大数据\", \"MapReduce\"]', NULL, NULL, NULL, 145, 8, 1, 25, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (142, '区块链共识机制', 'PoW和PoS的区别是什么？', 2, '学习', '[\"区块链\", \"共识机制\"]', NULL, NULL, NULL, 98, 6, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (143, '物联网通信协议', 'MQTT和CoAP协议各有什么特点？', 3, '学习', '[\"物联网\", \"通信协议\"]', NULL, NULL, NULL, 76, 5, 1, 12, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (144, '移动开发跨平台框架', 'Flutter和React Native哪个更好？', 4, '学习', '[\"移动开发\", \"跨平台\"]', NULL, NULL, NULL, 234, 12, 1, 10, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (145, '前端框架Vue vs React', 'Vue和React的响应式原理有什么区别？', 5, '学习', '[\"前端\", \"Vue\", \"React\"]', NULL, NULL, NULL, 198, 11, 1, 20, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (146, '后端架构微服务', '微服务架构的优缺点是什么？', 6, '学习', '[\"后端\", \"微服务\"]', NULL, NULL, NULL, 167, 9, 1, 18, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (147, 'DevOps持续集成', 'CI/CD流程怎么搭建？', 18, '学习', '[\"DevOps\", \"CI/CD\"]', NULL, NULL, NULL, 134, 8, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (148, '容器编排Kubernetes', 'K8s的Pod和Container有什么关系？', 19, '学习', '[\"Kubernetes\", \"容器编排\"]', NULL, NULL, NULL, 156, 9, 1, 22, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (149, '消息队列Kafka', 'Kafka的高吞吐量是怎么实现的？', 20, '学习', '[\"消息队列\", \"Kafka\"]', NULL, NULL, NULL, 123, 7, 1, 20, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (150, '缓存Redis数据结构', 'Redis的五种基本数据类型怎么选择？', 21, '学习', '[\"Redis\", \"缓存\"]', NULL, NULL, NULL, 189, 10, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (151, '搜索引擎Elasticsearch', 'ES的倒排索引原理是什么？', 22, '学习', '[\"Elasticsearch\", \"搜索\"]', NULL, NULL, NULL, 145, 8, 1, 25, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (152, '负载均衡Nginx', 'Nginx的负载均衡策略有哪些？', 23, '学习', '[\"Nginx\", \"负载均衡\"]', NULL, NULL, NULL, 112, 7, 1, 12, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (153, '校园卡丢失怎么办', '今天发现校园卡不见了，去哪里补办？需要带什么材料？', 26, '生活', '[\"校园卡\", \"补办\"]', NULL, NULL, NULL, 89, 6, 1, 0, 1, '2025-10-21 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (154, '宿舍热水供应时间', '咱们学校宿舍热水几点到几点供应？', 27, '生活', '[\"宿舍\", \"热水\"]', NULL, NULL, NULL, 45, 3, 1, 0, 1, '2025-10-23 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (155, '图书馆占座规则', '图书馆可以提前占座吗？有什么规定？', 28, '生活', '[\"图书馆\", \"占座\"]', NULL, NULL, NULL, 123, 8, 1, 0, 1, '2025-10-24 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (156, '食堂哪家最好吃', '新生求推荐，学校哪个食堂最好吃？', 29, '生活', '[\"食堂\", \"推荐\"]', NULL, NULL, NULL, 234, 15, 1, 0, 1, '2025-10-26 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (157, '快递代收点位置', '学校有几个快递代收点？分别在哪里？', 30, '生活', '[\"快递\", \"代收点\"]', NULL, NULL, NULL, 67, 4, 1, 0, 1, '2025-10-29 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (158, '校医院看病流程', '校医院看病需要预约吗？医保怎么报销？', 31, '生活', '[\"校医院\", \"医保\"]', NULL, NULL, NULL, 98, 7, 1, 0, 1, '2025-10-31 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (159, '自习室推荐', '除了图书馆，还有哪些安静的自习室？', 32, '生活', '[\"自习室\", \"推荐\"]', NULL, NULL, NULL, 156, 10, 1, 0, 1, '2025-11-03 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (160, '校园网办理流程', '新生怎么办理校园网？多少钱一个月？', 37, '生活', '[\"校园网\", \"办理\"]', NULL, NULL, NULL, 145, 9, 1, 0, 1, '2025-11-06 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (161, '洗衣机使用方法', '宿舍楼的共用洗衣机怎么用？支持什么支付方式？', 38, '生活', '[\"洗衣机\", \"宿舍\"]', NULL, NULL, NULL, 78, 5, 1, 0, 1, '2025-11-08 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (162, '校车时刻表', '学校到火车站的校车时刻表谁有？', 39, '生活', '[\"校车\", \"时刻表\"]', NULL, NULL, NULL, 112, 7, 1, 0, 1, '2025-11-10 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (163, '体育馆开放时间', '体育馆什么时候开放？可以打羽毛球吗？', 40, '生活', '[\"体育馆\", \"开放时间\"]', NULL, NULL, NULL, 89, 6, 1, 0, 1, '2025-11-12 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (164, '宿舍空调使用', '宿舍空调怎么收费？一度电多少钱？', 41, '生活', '[\"宿舍\", \"空调\"]', NULL, NULL, NULL, 134, 8, 1, 0, 1, '2025-11-14 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (165, '校园超市营业时间', '校园超市几点关门？周末也开吗？', 42, '生活', '[\"超市\", \"营业时间\"]', NULL, NULL, NULL, 56, 4, 1, 0, 1, '2025-11-16 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (166, '打印店位置', '学校哪里有打印店？打印多少钱一张？', 43, '生活', '[\"打印\", \"价格\"]', NULL, NULL, NULL, 98, 6, 1, 0, 1, '2025-11-18 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (167, '失物招领处', '捡到东西应该交到哪里？失物招领处在哪？', 44, '生活', '[\"失物招领\"]', NULL, NULL, NULL, 67, 5, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (168, '宿舍门禁时间', '宿舍门禁几点？晚归怎么办？', 45, '生活', '[\"宿舍\", \"门禁\"]', NULL, NULL, NULL, 145, 9, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (169, '校园兼职信息', '有没有靠谱的校园兼职推荐？', 46, '生活', '[\"兼职\", \"推荐\"]', NULL, NULL, NULL, 267, 16, 1, 5, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (170, '理发店推荐', '学校附近哪家理发店比较好？价格怎么样？', 47, '生活', '[\"理发\", \"推荐\"]', NULL, NULL, NULL, 123, 8, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (171, '银行ATM位置', '校园里有哪些银行的ATM？', 48, '生活', '[\"ATM\", \"银行\"]', NULL, NULL, NULL, 54, 3, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (172, '快递寄件点', '想寄快递去哪里？有顺丰吗？', 49, '生活', '[\"快递\", \"寄件\"]', NULL, NULL, NULL, 78, 5, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (173, '宿舍WiFi密码', '宿舍WiFi密码是多少？信号怎么样？', 50, '生活', '[\"WiFi\", \"宿舍\"]', NULL, NULL, NULL, 189, 11, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (174, '校园卡充值', '校园卡在哪里充值？可以用支付宝吗？', 51, '生活', '[\"校园卡\", \"充值\"]', NULL, NULL, NULL, 112, 7, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (175, '宿舍调换申请', '想换宿舍怎么申请？需要什么理由？', 1, '生活', '[\"宿舍\", \"调换\"]', NULL, NULL, NULL, 98, 6, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (176, '校园一卡通功能', '校园一卡通除了吃饭还能干什么？', 2, '生活', '[\"一卡通\", \"功能\"]', NULL, NULL, NULL, 87, 5, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (177, '宿舍电费查询', '怎么查询宿舍剩余电费？', 3, '生活', '[\"宿舍\", \"电费\"]', NULL, NULL, NULL, 134, 8, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (178, '校园跑腿服务', '有没有校园跑腿服务？怎么收费？', 4, '生活', '[\"跑腿\", \"服务\"]', NULL, NULL, NULL, 156, 9, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (179, '宿舍快递代拿', '宿舍楼下可以找人代拿快递吗？', 5, '生活', '[\"快递\", \"代拿\"]', NULL, NULL, NULL, 67, 4, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (180, '校园美食推荐', '学校周边有什么好吃的餐厅？', 6, '生活', '[\"美食\", \"推荐\"]', NULL, NULL, NULL, 235, 14, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (181, '宿舍晾衣服', '宿舍阳台可以晾衣服吗？有烘干机吗？', 18, '生活', '[\"宿舍\", \"晾衣服\"]', NULL, NULL, NULL, 78, 5, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (182, '校园快递柜', '快递柜超时收费吗？多久算超时？', 19, '生活', '[\"快递柜\", \"收费\"]', NULL, NULL, NULL, 98, 6, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (183, '宿舍养宠物', '宿舍可以养宠物吗？有什么规定？', 20, '生活', '[\"宿舍\", \"宠物\"]', NULL, NULL, NULL, 145, 9, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (184, '校园二手市场', '有没有校园二手交易群？', 21, '生活', '[\"二手\", \"交易\"]', NULL, NULL, NULL, 189, 11, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (185, '宿舍用电限制', '宿舍可以用吹风机吗？功率限制多少？', 22, '生活', '[\"宿舍\", \"用电\"]', NULL, NULL, NULL, 167, 10, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (186, '校园快递丢失', '快递显示已签收但没收到怎么办？', 23, '生活', '[\"快递\", \"丢失\"]', NULL, NULL, NULL, 123, 8, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (187, '宿舍网速慢', '宿舍网速特别慢怎么办？', 24, '生活', '[\"宿舍\", \"网速\"]', NULL, NULL, NULL, 145, 9, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (188, '校园卡挂失', '校园卡丢了怎么挂失？会扣钱吗？', 25, '生活', '[\"校园卡\", \"挂失\"]', NULL, NULL, NULL, 89, 6, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (189, '宿舍钥匙丢失', '宿舍钥匙丢了怎么办？换锁多少钱？', 26, '生活', '[\"宿舍\", \"钥匙\"]', NULL, NULL, NULL, 112, 7, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (190, '校园快递代收费', '快递代收点收费吗？多少钱？', 27, '生活', '[\"快递\", \"代收\"]', NULL, NULL, NULL, 67, 4, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (191, '宿舍报修流程', '宿舍东西坏了怎么报修？', 28, '生活', '[\"宿舍\", \"报修\"]', NULL, NULL, NULL, 98, 6, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (192, '校园外卖配送', '外卖可以送到宿舍楼下吗？', 29, '生活', '[\"外卖\", \"配送\"]', NULL, NULL, NULL, 178, 10, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (193, '宿舍卫生检查', '宿舍卫生检查多久一次？标准是什么？', 30, '生活', '[\"宿舍\", \"卫生检查\"]', NULL, NULL, NULL, 134, 8, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (194, '校园快递时效', '校内快递一般几天到？', 31, '生活', '[\"快递\", \"时效\"]', NULL, NULL, NULL, 56, 3, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (195, '宿舍空调维修', '宿舍空调坏了找谁修？', 32, '生活', '[\"宿舍\", \"空调维修\"]', NULL, NULL, NULL, 78, 5, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (196, '校园卡余额查询', '怎么查询校园卡余额？', 37, '生活', '[\"校园卡\", \"余额\"]', NULL, NULL, NULL, 89, 6, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (197, '宿舍插座不够', '宿舍插座不够用怎么办？可以用插排吗？', 38, '生活', '[\"宿舍\", \"插座\"]', NULL, NULL, NULL, 123, 7, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (198, '校园快递退货', '在学校买的东西怎么退货？', 39, '生活', '[\"快递\", \"退货\"]', NULL, NULL, NULL, 67, 4, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (199, '宿舍噪音问题', '宿舍太吵怎么办？可以投诉吗？', 40, '生活', '[\"宿舍\", \"噪音\"]', NULL, NULL, NULL, 145, 9, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (200, '校园打印优惠', '打印店有学生优惠吗？', 41, '生活', '[\"打印\", \"优惠\"]', NULL, NULL, NULL, 78, 5, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (201, '宿舍断电时间', '宿舍几点断电？周末也断吗？', 42, '生活', '[\"宿舍\", \"断电\"]', NULL, NULL, NULL, 167, 10, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (202, '校园快递保管期', '快递在代收点能放几天？', 43, '生活', '[\"快递\", \"保管\"]', NULL, NULL, NULL, 56, 3, 1, 0, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (203, 'Git分支管理策略', 'Git Flow和GitHub Flow有什么区别？', 44, '技术', '[\"Git\", \"版本控制\"]', NULL, NULL, NULL, 234, 12, 1, 20, 1, '2025-10-22 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (204, 'Spring Boot自动配置', 'Spring Boot的自动配置原理是什么？', 45, '技术', '[\"Spring Boot\", \"Java\"]', NULL, NULL, NULL, 189, 10, 1, 25, 1, '2025-10-24 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (205, 'Vue3 Composition API', 'Vue3的Composition API相比Options API有什么优势？', 46, '技术', '[\"Vue3\", \"前端\"]', NULL, NULL, NULL, 267, 14, 1, 20, 1, '2025-10-26 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (206, 'React Hooks使用', 'useEffect的依赖数组怎么正确使用？', 47, '技术', '[\"React\", \"Hooks\"]', NULL, NULL, NULL, 198, 11, 1, 18, 1, '2025-10-28 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (207, 'MySQL事务隔离级别', '四种事务隔离级别分别解决什么问题？', 48, '技术', '[\"MySQL\", \"事务\"]', NULL, NULL, NULL, 178, 9, 1, 25, 1, '2025-10-30 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (208, 'Redis持久化机制', 'RDB和AOF持久化各有什么优缺点？', 49, '技术', '[\"Redis\", \"持久化\"]', NULL, NULL, NULL, 156, 8, 1, 20, 1, '2025-11-01 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (209, 'Docker镜像优化', '怎么减小Docker镜像的体积？', 50, '技术', '[\"Docker\", \"优化\"]', NULL, NULL, NULL, 145, 8, 1, 15, 1, '2025-11-03 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (210, 'Nginx反向代理配置', 'Nginx反向代理怎么配置？', 51, '技术', '[\"Nginx\", \"反向代理\"]', NULL, NULL, NULL, 134, 7, 1, 12, 1, '2025-11-05 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (211, 'JWT Token安全性', 'JWT Token怎么防止被盗用？', 1, '技术', '[\"JWT\", \"安全\"]', NULL, NULL, NULL, 167, 9, 1, 20, 1, '2025-11-07 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (212, 'WebSocket实时通信', 'WebSocket和HTTP长轮询有什么区别？', 2, '技术', '[\"WebSocket\", \"实时通信\"]', NULL, NULL, NULL, 145, 8, 1, 15, 1, '2025-11-09 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (213, 'RESTful API设计', 'RESTful API的最佳实践有哪些？', 3, '技术', '[\"RESTful\", \"API设计\"]', NULL, NULL, NULL, 189, 10, 1, 18, 1, '2025-11-11 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (214, 'GraphQL vs REST', 'GraphQL相比REST有什么优势？', 4, '技术', '[\"GraphQL\", \"REST\"]', NULL, NULL, NULL, 123, 7, 1, 15, 1, '2025-11-13 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (215, 'TypeScript泛型', 'TypeScript泛型的高级用法有哪些？', 5, '技术', '[\"TypeScript\", \"泛型\"]', NULL, NULL, NULL, 156, 8, 1, 20, 1, '2025-11-15 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (216, 'Python装饰器', 'Python装饰器的实现原理是什么？', 6, '技术', '[\"Python\", \"装饰器\"]', NULL, NULL, NULL, 134, 7, 1, 15, 1, '2025-11-17 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (217, 'Go语言并发', 'Go的goroutine和channel怎么使用？', 18, '技术', '[\"Go\", \"并发\"]', NULL, NULL, NULL, 178, 9, 1, 22, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (218, 'Rust所有权系统', 'Rust的所有权、借用、生命周期怎么理解？', 19, '技术', '[\"Rust\", \"所有权\"]', NULL, NULL, NULL, 145, 8, 1, 25, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (219, 'Kotlin协程', 'Kotlin协程相比线程有什么优势？', 20, '技术', '[\"Kotlin\", \"协程\"]', NULL, NULL, NULL, 112, 6, 1, 18, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (220, 'Swift SwiftUI', 'SwiftUI和UIKit有什么区别？', 21, '技术', '[\"Swift\", \"SwiftUI\"]', NULL, NULL, NULL, 98, 5, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (221, 'Android Jetpack', 'Android Jetpack组件怎么使用？', 22, '技术', '[\"Android\", \"Jetpack\"]', NULL, NULL, NULL, 134, 7, 1, 20, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (222, 'iOS MVVM架构', 'iOS开发中MVVM架构怎么实现？', 23, '技术', '[\"iOS\", \"MVVM\"]', NULL, NULL, NULL, 123, 7, 1, 18, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (223, '微信小程序性能优化', '小程序首屏加载慢怎么优化？', 24, '技术', '[\"微信小程序\", \"性能优化\"]', NULL, NULL, NULL, 189, 10, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (224, 'uni-app跨平台', 'uni-app开发有哪些坑？', 25, '技术', '[\"uni-app\", \"跨平台\"]', NULL, NULL, NULL, 167, 9, 1, 12, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (225, 'Flutter状态管理', 'Flutter的Provider和Riverpod哪个更好？', 26, '技术', '[\"Flutter\", \"状态管理\"]', NULL, NULL, NULL, 145, 8, 1, 20, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (226, 'React Native性能', 'React Native性能优化有哪些方法？', 27, '技术', '[\"React Native\", \"性能\"]', NULL, NULL, NULL, 134, 7, 1, 18, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (227, 'Webpack打包优化', 'Webpack打包速度慢怎么优化？', 28, '技术', '[\"Webpack\", \"打包优化\"]', NULL, NULL, NULL, 156, 8, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (228, 'Vite vs Webpack', 'Vite相比Webpack有什么优势？', 29, '技术', '[\"Vite\", \"Webpack\"]', NULL, NULL, NULL, 178, 9, 1, 12, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (229, 'CSS-in-JS方案', 'styled-components和Emotion哪个更好？', 30, '技术', '[\"CSS-in-JS\", \"前端\"]', NULL, NULL, NULL, 98, 5, 1, 10, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (230, 'Tailwind CSS', 'Tailwind CSS的优缺点是什么？', 31, '技术', '[\"Tailwind\", \"CSS\"]', NULL, NULL, NULL, 145, 8, 1, 8, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (231, '前端性能监控', '前端性能监控怎么实现？', 32, '技术', '[\"前端\", \"性能监控\"]', NULL, NULL, NULL, 123, 7, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (232, '前端错误监控', '怎么捕获和上报前端错误？', 37, '技术', '[\"前端\", \"错误监控\"]', NULL, NULL, NULL, 135, 7, 1, 12, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (233, '单元测试Jest', 'Jest单元测试怎么写？', 38, '技术', '[\"Jest\", \"单元测试\"]', NULL, NULL, NULL, 112, 6, 1, 10, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (234, 'E2E测试Cypress', 'Cypress E2E测试怎么使用？', 39, '技术', '[\"Cypress\", \"E2E测试\"]', NULL, NULL, NULL, 98, 5, 1, 12, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (235, '代码质量ESLint', 'ESLint配置有哪些最佳实践？', 40, '技术', '[\"ESLint\", \"代码质量\"]', NULL, NULL, NULL, 123, 7, 1, 8, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (236, '代码格式化Prettier', 'Prettier和ESLint冲突怎么解决？', 41, '技术', '[\"Prettier\", \"ESLint\"]', NULL, NULL, NULL, 145, 8, 1, 10, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (237, 'Git Commit规范', 'Git Commit Message怎么写才规范？', 42, '技术', '[\"Git\", \"Commit规范\"]', NULL, NULL, NULL, 167, 9, 1, 8, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (238, '代码审查Code Review', 'Code Review应该关注哪些方面？', 43, '技术', '[\"Code Review\", \"代码质量\"]', NULL, NULL, NULL, 134, 7, 1, 12, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (239, '敏捷开发Scrum', 'Scrum开发流程是怎样的？', 44, '技术', '[\"Scrum\", \"敏捷开发\"]', NULL, NULL, NULL, 112, 6, 1, 10, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (240, '技术债务管理', '怎么管理和偿还技术债务？', 45, '技术', '[\"技术债务\", \"项目管理\"]', NULL, NULL, NULL, 98, 5, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (241, '重构最佳实践', '代码重构有哪些原则和方法？', 46, '技术', '[\"重构\", \"代码质量\"]', NULL, NULL, NULL, 145, 8, 1, 18, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (242, '设计模式应用', '常用的设计模式有哪些？什么场景使用？', 47, '技术', '[\"设计模式\", \"架构\"]', NULL, NULL, NULL, 189, 10, 1, 20, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (243, '领域驱动设计DDD', 'DDD的核心概念是什么？', 48, '技术', '[\"DDD\", \"架构设计\"]', NULL, NULL, NULL, 156, 8, 1, 25, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (244, 'SOLID原则', 'SOLID五大原则怎么理解？', 49, '技术', '[\"SOLID\", \"设计原则\"]', NULL, NULL, NULL, 167, 9, 1, 20, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (245, 'Clean Code', '怎么写出Clean Code？', 50, '技术', '[\"Clean Code\", \"代码质量\"]', NULL, NULL, NULL, 134, 7, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (246, '技术选型标准', '新项目技术选型应该考虑哪些因素？', 51, '技术', '[\"技术选型\", \"架构\"]', NULL, NULL, NULL, 178, 9, 1, 18, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (247, 'API版本管理', 'API版本管理有哪些策略？', 1, '技术', '[\"API\", \"版本管理\"]', NULL, NULL, NULL, 123, 7, 1, 12, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (248, '数据库迁移', '数据库表结构变更怎么平滑迁移？', 2, '技术', '[\"数据库\", \"迁移\"]', NULL, NULL, NULL, 145, 8, 1, 20, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (249, '灰度发布策略', '灰度发布怎么实现？', 3, '技术', '[\"灰度发布\", \"DevOps\"]', NULL, NULL, NULL, 156, 8, 1, 18, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (250, '服务降级熔断', '服务降级和熔断有什么区别？', 4, '技术', '[\"降级\", \"熔断\"]', NULL, NULL, NULL, 167, 9, 1, 22, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (251, '限流算法实现', '常见的限流算法有哪些？', 5, '技术', '[\"限流\", \"算法\"]', NULL, NULL, NULL, 145, 8, 1, 20, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (252, '分布式锁实现', 'Redis分布式锁怎么实现？', 6, '技术', '[\"分布式锁\", \"Redis\"]', NULL, NULL, NULL, 189, 10, 1, 25, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (253, '秋招春招时间', '大厂的秋招春招一般什么时候开始？', 18, '就业', '[\"秋招\", \"春招\"]', NULL, NULL, NULL, 345, 18, 1, 10, 1, '2025-10-20 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (254, '简历怎么写', '技术岗简历应该怎么写才能吸引HR？', 19, '就业', '[\"简历\", \"求职\"]', NULL, NULL, NULL, 456, 22, 1, 15, 1, '2025-10-22 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (255, '面试算法题', '面试常考的算法题有哪些？', 20, '就业', '[\"面试\", \"算法\"]', NULL, NULL, NULL, 567, 28, 1, 20, 1, '2025-10-24 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (256, '实习转正概率', '实习转正的概率大吗？需要注意什么？', 21, '就业', '[\"实习\", \"转正\"]', NULL, NULL, NULL, 234, 14, 1, 10, 1, '2025-10-26 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (257, '大厂面试流程', '字节跳动的面试流程是怎样的？', 22, '就业', '[\"面试\", \"字节跳动\"]', NULL, NULL, NULL, 679, 32, 1, 15, 1, '2025-10-28 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (258, 'offer选择', '拿到多个offer怎么选择？', 23, '就业', '[\"offer\", \"选择\"]', NULL, NULL, NULL, 389, 20, 1, 12, 1, '2025-10-30 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (259, '薪资谈判技巧', '应届生怎么和HR谈薪资？', 24, '就业', '[\"薪资\", \"谈判\"]', NULL, NULL, NULL, 445, 24, 1, 18, 1, '2025-11-01 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (260, '实习经历重要性', '没有实习经历能找到好工作吗？', 25, '就业', '[\"实习\", \"求职\"]', NULL, NULL, NULL, 298, 16, 1, 8, 1, '2025-11-03 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (261, '项目经验准备', '简历上的项目经验怎么准备？', 26, '就业', '[\"项目经验\", \"简历\"]', NULL, NULL, NULL, 512, 26, 1, 15, 1, '2025-11-05 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (262, '技术栈选择', '前端后端怎么选？哪个更好找工作？', 27, '就业', '[\"技术栈\", \"职业规划\"]', NULL, NULL, NULL, 623, 30, 1, 10, 1, '2025-11-07 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (263, '考研还是工作', '计算机专业考研还是直接工作？', 28, '就业', '[\"考研\", \"工作\"]', NULL, NULL, NULL, 799, 38, 1, 20, 1, '2025-11-09 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (264, '大厂还是小厂', '应届生去大厂还是创业公司？', 29, '就业', '[\"大厂\", \"创业公司\"]', NULL, NULL, NULL, 534, 28, 1, 12, 1, '2025-11-11 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (265, '远程工作机会', '有哪些公司提供远程工作？', 30, '就业', '[\"远程工作\", \"求职\"]', NULL, NULL, NULL, 367, 18, 1, 8, 1, '2025-11-13 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (266, '职业发展路径', '程序员的职业发展路径有哪些？', 31, '就业', '[\"职业发展\", \"程序员\"]', NULL, NULL, NULL, 445, 22, 1, 15, 1, '2025-11-15 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (267, '跳槽最佳时机', '工作多久跳槽比较合适？', 32, '就业', '[\"跳槽\", \"职业规划\"]', NULL, NULL, NULL, 398, 20, 1, 10, 1, '2025-11-17 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (268, '技术面试准备', '技术面试应该准备哪些内容？', 37, '就业', '[\"技术面试\", \"准备\"]', NULL, NULL, NULL, 567, 28, 1, 18, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (269, 'HR面试技巧', 'HR面试常问的问题有哪些？', 38, '就业', '[\"HR面试\", \"技巧\"]', NULL, NULL, NULL, 423, 21, 1, 12, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (270, '背景调查', '背景调查一般查什么？', 39, '就业', '[\"背景调查\", \"入职\"]', NULL, NULL, NULL, 289, 15, 1, 8, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (271, '试用期注意事项', '试用期需要注意什么？', 40, '就业', '[\"试用期\", \"注意事项\"]', NULL, NULL, NULL, 335, 17, 1, 10, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (272, '五险一金', '五险一金的缴纳比例是多少？', 41, '就业', '[\"五险一金\", \"福利\"]', NULL, NULL, NULL, 456, 23, 1, 5, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (273, '加班文化', '互联网公司的加班文化怎么样？', 42, '就业', '[\"加班\", \"互联网\"]', NULL, NULL, NULL, 680, 34, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (274, '股票期权', '公司给的股票期权值钱吗？', 43, '就业', '[\"股票期权\", \"薪资\"]', NULL, NULL, NULL, 389, 19, 1, 12, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (275, '年终奖', '互联网公司年终奖一般几个月？', 44, '就业', '[\"年终奖\", \"薪资\"]', NULL, NULL, NULL, 512, 26, 1, 10, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (276, '劳动合同', '签劳动合同要注意什么？', 45, '就业', '[\"劳动合同\", \"入职\"]', NULL, NULL, NULL, 267, 14, 1, 8, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (277, '竞业协议', '竞业协议合理吗？可以不签吗？', 46, '就业', '[\"竞业协议\", \"劳动法\"]', NULL, NULL, NULL, 445, 22, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (278, '离职流程', '离职需要提前多久申请？', 47, '就业', '[\"离职\", \"流程\"]', NULL, NULL, NULL, 334, 17, 1, 5, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (279, '裁员赔偿', '被裁员能拿到多少赔偿？', 48, '就业', '[\"裁员\", \"赔偿\"]', NULL, NULL, NULL, 567, 28, 1, 18, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (280, '内推渠道', '怎么找到大厂的内推？', 49, '就业', '[\"内推\", \"求职\"]', NULL, NULL, NULL, 623, 30, 1, 12, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (281, '笔试题型', '大厂笔试一般考什么？', 50, '就业', '[\"笔试\", \"题型\"]', NULL, NULL, NULL, 489, 24, 1, 10, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (282, '群面技巧', '群面应该怎么表现？', 51, '就业', '[\"群面\", \"技巧\"]', NULL, NULL, NULL, 356, 18, 1, 8, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (283, '压力面试', '遇到压力面试怎么应对？', 1, '就业', '[\"压力面试\", \"应对\"]', NULL, NULL, NULL, 423, 21, 1, 12, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (284, '反问环节', '面试最后反问环节问什么好？', 2, '就业', '[\"反问\", \"面试\"]', NULL, NULL, NULL, 535, 27, 1, 10, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (285, '面试着装', '技术岗面试穿什么合适？', 3, '就业', '[\"着装\", \"面试\"]', NULL, NULL, NULL, 234, 12, 1, 5, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (286, '面试礼仪', '面试有哪些礼仪需要注意？', 4, '就业', '[\"礼仪\", \"面试\"]', NULL, NULL, NULL, 289, 15, 1, 5, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (287, '面试被拒', '面试被拒后还能再投吗？', 5, '就业', '[\"面试\", \"被拒\"]', NULL, NULL, NULL, 398, 20, 1, 8, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (288, 'offer等待', 'offer一般多久能收到？', 6, '就业', '[\"offer\", \"等待\"]', NULL, NULL, NULL, 312, 16, 1, 5, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (289, '违约金', '毁约要赔违约金吗？', 18, '就业', '[\"违约金\", \"毁约\"]', NULL, NULL, NULL, 445, 22, 1, 10, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (290, '档案户口', '档案和户口怎么处理？', 19, '就业', '[\"档案\", \"户口\"]', NULL, NULL, NULL, 267, 14, 1, 8, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (291, '落户政策', '北上广深的落户政策是怎样的？', 20, '就业', '[\"落户\", \"政策\"]', NULL, NULL, NULL, 567, 28, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (292, '租房补贴', '公司有租房补贴吗？', 21, '就业', '[\"租房补贴\", \"福利\"]', NULL, NULL, NULL, 334, 17, 1, 5, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (293, '通勤时间', '通勤时间多久比较合理？', 22, '就业', '[\"通勤\", \"工作\"]', NULL, NULL, NULL, 290, 15, 1, 5, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (294, '工作环境', '互联网公司的工作环境怎么样？', 23, '就业', '[\"工作环境\", \"互联网\"]', NULL, NULL, NULL, 423, 21, 1, 8, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (295, '团队氛围', '怎么判断团队氛围好不好？', 24, '就业', '[\"团队氛围\", \"工作\"]', NULL, NULL, NULL, 357, 18, 1, 10, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (296, '技术成长', '在大厂能学到什么？', 25, '就业', '[\"技术成长\", \"大厂\"]', NULL, NULL, NULL, 490, 24, 1, 12, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (297, '晋升机制', '互联网公司的晋升机制是怎样的？', 26, '就业', '[\"晋升\", \"职业发展\"]', NULL, NULL, NULL, 513, 26, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (298, '绩效考核', '绩效考核标准是什么？', 27, '就业', '[\"绩效\", \"考核\"]', NULL, NULL, NULL, 398, 20, 1, 10, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (299, '35岁危机', '程序员35岁危机是真的吗？', 28, '就业', '[\"35岁\", \"职业危机\"]', NULL, NULL, NULL, 793, 38, 1, 20, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (300, '转行可能性', '工作几年后还能转行吗？', 29, '就业', '[\"转行\", \"职业规划\"]', NULL, NULL, NULL, 446, 22, 1, 12, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (301, '副业选择', '程序员可以做什么副业？', 30, '就业', '[\"副业\", \"收入\"]', NULL, NULL, NULL, 626, 30, 1, 15, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (302, '技术博客', '写技术博客对求职有帮助吗？', 31, '就业', '[\"技术博客\", \"求职\"]', NULL, NULL, NULL, 368, 18, 1, 8, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (303, '开源贡献', '参与开源项目对简历有加分吗？', 32, '就业', '[\"开源\", \"简历\"]', NULL, NULL, NULL, 536, 27, 1, 12, 1, '2025-11-19 14:01:14', '2025-11-19 14:01:14');
INSERT INTO `question` VALUES (304, '111111111111111111', '1111111111111111111', 1, '生活', NULL, NULL, NULL, NULL, 13, 2, 0, 10, 0, '2025-11-19 15:58:15', '2025-12-09 21:04:17');
INSERT INTO `question` VALUES (305, '如何准备高等数学期末考试?', '我是大一新生,马上要期末考试了,想问一     \n  下如何高效复习高等数学,有什么好的学习资料推荐吗', 1, '生活', NULL, NULL, NULL, NULL, 19, 1, 1, 20, 1, '2025-11-19 16:10:02', '2025-11-24 08:07:38');
INSERT INTO `question` VALUES (306, '222222222222222222222', '22222222222222222222222', 1, '学习', '[\"课程\", \"复习\", \"考研\"]', '[\"https://campuslink01.oss-cn-hangzhou.aliyuncs.com/uploads/445d7dbff9f44682b0491531f6d9ef4c\"]', NULL, NULL, 46, 2, 1, 10, 1, '2025-11-19 16:29:52', '2025-11-19 16:46:05');
INSERT INTO `question` VALUES (307, '如何学习 Spring Boot？', '我是Java新手，想学习Spring Boot，有什么好的学习路径推荐吗？', 53, '学习', '[\"课程\"]', '[\"https://campuslink01.oss-cn-hangzhou.aliyuncs.com/uploads/99c70e2789d146ecbfc97ad272a29dcd\"]', NULL, NULL, 6, 1, 0, 20, 1, '2025-11-21 13:06:38', '2025-12-12 14:45:59');
INSERT INTO `question` VALUES (308, '如何学习 Spring Boot？1', '我是Java新手，想学习Spring Boot，有什么好的学习路径推荐吗？1', 53, '生活', NULL, '[\"https://campuslink01.oss-cn-hangzhou.aliyuncs.com/uploads/43b1518122d54bdf8747bae2ca688791\"]', NULL, NULL, 2, 0, 0, 50, 1, '2025-11-21 13:07:48', '2025-11-21 13:07:48');
INSERT INTO `question` VALUES (309, '无钱无权我去问驱蚊器', '我去违法的去问驱蚊器我万千瓦', 53, '学习', '[\"课程\"]', '[\"https://campuslink01.oss-cn-hangzhou.aliyuncs.com/uploads/927118f455784a39804038feaa999f60\"]', NULL, NULL, 3, 0, 0, 10, 1, '2025-11-21 13:18:34', '2025-11-21 13:18:34');
INSERT INTO `question` VALUES (310, '无钱无权我去问驱蚊器', '我去违法的去问驱蚊器我万千瓦', 53, '学习', '[\"课程\"]', '[\"https://campuslink01.oss-cn-hangzhou.aliyuncs.com/uploads/927118f455784a39804038feaa999f60\"]', NULL, NULL, 47, 2, 0, 5, 1, '2025-11-21 13:18:48', '2025-12-09 21:15:43');
INSERT INTO `question` VALUES (311, '大苏打大大大sa', '打撒萨达打撒大苏打11', 1, '生活', '[\"饮食\", \"交通\"]', '[\"https://campuslink01.oss-cn-hangzhou.aliyuncs.com/uploads/31518be8901b406795f0918a1c499354\"]', NULL, NULL, 2, 0, 0, 50, 0, '2025-11-21 13:26:19', '2025-11-21 13:41:32');
INSERT INTO `question` VALUES (312, '发士大夫石帆胜丰是否', '3213232323324大师傅士大夫撒旦', 1, '生活', NULL, NULL, NULL, NULL, 1, 0, 0, 86, 0, '2025-11-21 13:36:20', '2025-11-21 13:37:36');
INSERT INTO `question` VALUES (313, '豆腐干豆腐干大概', '感豆腐干大概豆腐干大概感到翻跟斗', 1, '生活', '[\"饮食\"]', NULL, NULL, NULL, 1, 0, 0, 46, 0, '2025-11-21 13:38:56', '2025-11-21 13:39:58');
INSERT INTO `question` VALUES (314, '郭大哥大哥范大哥', '个地方个地方官方的规定规定', 1, '生活', '[\"饮食\", \"交通\"]', NULL, NULL, NULL, 43, 1, 1, 100, 1, '2025-11-21 13:40:52', '2025-11-21 14:25:29');
INSERT INTO `question` VALUES (315, '郭大哥大哥范大哥11', '个地方个地方官方的规定规定111', 1, '生活', '[\"饮食\", \"交通\"]', NULL, NULL, NULL, 2, 0, 0, 99, 0, '2025-11-21 13:41:15', '2025-11-21 13:48:08');
INSERT INTO `question` VALUES (316, '测试删除功能', '测试删除功能测试删除功能测试删除功能', 1, '学习', '[\"课程\"]', NULL, NULL, NULL, 1, 0, 0, 20, 0, '2025-12-09 21:06:56', '2025-12-09 21:07:49');
INSERT INTO `question` VALUES (317, '如何学习Spring Boot框架？', '求推荐学习路线和资源', 1, '技术问答', NULL, NULL, NULL, NULL, 528, 5, 1, 0, 1, '2025-12-12 19:33:28', '2025-12-12 19:37:46');
INSERT INTO `question` VALUES (318, 'Spring Boot 多线程并发处理最佳实践？', '在开发高并发系统时，如何正确使用线程池？有哪些坑需要避免？', 1, '技术问答', NULL, NULL, NULL, NULL, 882, 5, 0, 50, 1, '2025-12-12 19:38:52', '2025-12-12 19:38:52');

-- ----------------------------
-- Table structure for report
-- ----------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report`  (
  `report_id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporter_id` bigint NOT NULL COMMENT '举报者ID',
  `report_type` tinyint NOT NULL COMMENT '举报类型：1-帖子，2-评论，3-用户，4-活动',
  `target_id` bigint NOT NULL COMMENT '举报对象ID',
  `reason_type` tinyint NOT NULL COMMENT '举报原因类型：1-垃圾信息，2-违规内容，3-骚扰辱骂，4-侵权，5-其他',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报详细描述',
  `evidence_images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '举报凭证图片(多张用逗号分隔)',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-已处理，2-已驳回',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `handle_result` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理结果说明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `handled_at` datetime NULL DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`report_id`) USING BTREE,
  INDEX `idx_reporter_id`(`reporter_id` ASC) USING BTREE,
  INDEX `idx_report_type`(`report_type` ASC) USING BTREE,
  INDEX `idx_target_id`(`target_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `fk_report_handler`(`handler_id` ASC) USING BTREE,
  CONSTRAINT `fk_report_handler` FOREIGN KEY (`handler_id`) REFERENCES `user` (`u_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '举报表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of report
-- ----------------------------
INSERT INTO `report` VALUES (1, 27, 1, 1, 1, '这个帖子是广告垃圾信息,请尽快处理', 'https://example.com/evidence1.jpg,https://example.com/evidence2.jpg', 1, 28, '经核实,该内容确实违反社区规范,已对相关内容进行删除处理,并对发布者进行警告', '2025-11-08 21:03:43', '2025-11-08 21:04:37');
INSERT INTO `report` VALUES (2, 27, 2, 1, 2, '该评论包含违规内容,涉及敏感话题', 'https://example.com/evidence3.jpg', 2, 28, '经核实,该内容未发现明显违规,驳回此次举报', '2025-11-08 21:03:45', '2025-11-08 21:04:40');
INSERT INTO `report` VALUES (4, 27, 4, 1, 4, '该活动未经授权使用了我们社团的名称和logo', '', 0, NULL, NULL, '2025-11-08 21:03:51', NULL);
INSERT INTO `report` VALUES (5, 27, 1, 2, 5, '其他原因:该帖子内容与板块主题不符', '', 1, 28, '已核实并删除违规内容,感谢您的举报', '2025-11-08 21:03:53', '2025-11-08 21:06:11');
INSERT INTO `report` VALUES (6, 27, 1, 99999, 1, '测试举报不存在的帖子', '', 0, NULL, NULL, '2025-11-08 21:04:46', NULL);
INSERT INTO `report` VALUES (7, 27, 1, 1, 1, '重复举报测试', '', 0, NULL, NULL, '2025-11-08 21:05:03', NULL);
INSERT INTO `report` VALUES (8, 27, 1, 5, 2, '该帖子包含不当内容,请管理员审核', 'https://example.com/evidence.jpg', 0, NULL, NULL, '2025-11-08 21:05:58', NULL);

-- ----------------------------
-- Table structure for resource
-- ----------------------------
DROP TABLE IF EXISTS `resource`;
CREATE TABLE `resource`  (
  `r_id` bigint NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '资源描述',
  `uploader_id` bigint NOT NULL COMMENT '上传者ID',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件URL（OSS）',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `file_size` bigint NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件类型（pdf/doc/ppt等）',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类（课件/试题/笔记等）',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程名称',
  `school_id` bigint NULL DEFAULT NULL COMMENT '所属学校ID',
  `score` int NOT NULL DEFAULT 0 COMMENT '所需积分',
  `downloads` int NOT NULL DEFAULT 0 COMMENT '下载次数',
  `likes` int NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`r_id`) USING BTREE,
  INDEX `idx_uploader_id`(`uploader_id` ASC) USING BTREE,
  INDEX `idx_school_id`(`school_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_resource_school` FOREIGN KEY (`school_id`) REFERENCES `school` (`school_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_resource_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of resource
-- ----------------------------
INSERT INTO `resource` VALUES (1, '数据结构课件-第一章', '数据结构基础知识，包括线性表、栈、队列等内容', 2, 'https://oss.example.com/resources/ds-chapter1.pdf', '数据结构-第一章.pdf', 2048576, 'pdf', '课件', '数据结构', NULL, 5, 156, 37, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `resource` VALUES (2, '操作系统期末复习资料', '操作系统期末考试重点总结，包含历年真题', 3, 'https://oss.example.com/resources/os-review.pdf', '操作系统复习.pdf', 3145728, 'pdf', '试题', '操作系统', NULL, 10, 204, 51, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `resource` VALUES (3, '计算机网络笔记', '计算机网络课程笔记，涵盖TCP/IP协议栈', 4, 'https://oss.example.com/resources/network-notes.pdf', '网络笔记.pdf', 1572864, 'pdf', '笔记', '计算机网络', NULL, 3, 83, 20, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `resource` VALUES (4, '算法设计与分析PPT', '算法课程PPT，包含动态规划、贪心算法等', 5, 'https://oss.example.com/resources/algorithm.pptx', '算法设计.pptx', 4194304, 'pptx', '课件', '算法设计与分析', NULL, 8, 127, 29, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `resource` VALUES (5, '数据库系统概论习题集', '数据库课程习题及答案解析', 6, 'https://oss.example.com/resources/db-exercises.pdf', '数据库习题.pdf', 2621440, 'pdf', '试题', '数据库系统概论', NULL, 6, 99, 18, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `resource` VALUES (13, '数据结构与算法 - 期末复习资料', '包含所有章节的知识点总结、经典例题和历年真题解析。适合期末复习使用。', 37, 'https://campuslink-oss.example.com/resources/data-structure-review.pdf', '数据结构期末复习.pdf', 5242880, 'pdf', '课件', '数据结构与算法', NULL, 5, 1, 1, 1, NULL, '2025-11-09 13:11:15', '2025-11-09 13:11:42');
INSERT INTO `resource` VALUES (14, '操作系统 - 进程管理PPT', '操作系统第三章进程管理完整PPT，包含详细的注释和示例代码。', 37, 'https://campuslink-oss.example.com/resources/os-process.pptx', '进程管理.pptx', 3145728, 'pptx', '课件', '操作系统', NULL, 3, 0, 0, 0, NULL, '2025-11-09 13:12:01', '2025-11-09 13:12:01');
INSERT INTO `resource` VALUES (15, '高等数学 - 2024年期中考试试卷', '2024年春季学期高等数学期中考试真题及详细答案解析。', 37, 'https://campuslink-oss.example.com/resources/math-midterm-2024.pdf', '高数期中试卷2024.pdf', 2097152, 'pdf', '试题', '高等数学', NULL, 8, 0, 0, 2, '资源内容不符合平台规范，包含敏感信息，请修改后重新上传。', '2025-11-09 13:12:03', '2025-11-09 13:12:21');
INSERT INTO `resource` VALUES (16, '完整流程测试资源', '用于测试完整审核流程的资源', 37, 'https://www.ibm.com/docs/zh/SSZJDU_6.4.0/pdf/dpxmst.pdf', 'workflow-test.pdf', 2048000, 'pdf', '其他', '测试课程', NULL, 3, 4, 2, 1, NULL, '2025-11-09 13:13:17', '2025-11-17 21:57:57');
INSERT INTO `resource` VALUES (17, '测试拒绝流程资源', '这个资源将被管理员拒绝', 37, 'https://campuslink-oss.example.com/resources/reject-test.pdf', 'reject-test.pdf', 1024000, 'pdf', '其他', '测试课程', NULL, 2, 0, 0, 2, '资源质量不达标，内容过于简单，不具备分享价值。建议补充详细内容后重新提交。', '2025-11-09 13:14:06', '2025-11-09 13:14:16');
INSERT INTO `resource` VALUES (18, '《网页设计》课程考查答题纸', '测试文档进行oss上传', 1, 'https://campuslink01.oss-cn-hangzhou.aliyuncs.com/uploads/376f570fb9a945ed950d77ef8ba90a5f.doc', '《网页设计》课程考查答题纸.doc', 9520640, 'doc', '课件', NULL, NULL, 5, 0, 0, 0, NULL, '2025-11-18 18:47:52', '2025-11-18 18:47:52');
INSERT INTO `resource` VALUES (19, '《网页设计》课程考查答题纸', '请问请问请问请问请问', 1, 'https://campuslink01.oss-cn-hangzhou.aliyuncs.com/uploads/1a343f8e68fc42b7ad31357a718a9b30.doc', '《网页设计》课程考查答题纸.doc', 9520640, 'doc', '笔记', '请问请问', NULL, 5, 0, 0, 0, NULL, '2025-11-18 18:48:36', '2025-11-18 18:48:36');
INSERT INTO `resource` VALUES (20, '《网页设计》课程考查答题纸', '11111111111111111111111111', 1, 'https://campuslink01.oss-cn-hangzhou.aliyuncs.com/uploads/67c007bae572403cae5f1a02f7aefda5.doc', '《网页设计》课程考查答题纸.doc', 9520640, 'doc', '课件', NULL, NULL, 5, 0, 0, 0, NULL, '2025-11-18 18:55:07', '2025-11-18 18:55:07');
INSERT INTO `resource` VALUES (21, '《网页设计》课程考查答题纸2', '111111111111111111111111111', 1, 'https://campuslink01.oss-cn-hangzhou.aliyuncs.com/uploads/c150006460604c06b351dcb53e34e8e1.doc', '《网页设计》课程考查答题纸2.doc', 7732736, 'doc', '教材', NULL, NULL, 5, 0, 0, 0, NULL, '2025-11-18 19:07:57', '2025-11-18 19:07:57');
INSERT INTO `resource` VALUES (22, '《网页设计》课程考查答题纸', '11111111111111111111111', 1, 'https://campuslink01.oss-cn-hangzhou.aliyuncs.com/uploads/db099173ebdd4b4cbd29e2635dc29391.doc', '《网页设计》课程考查答题纸.doc', 9520640, 'doc', '实验报告', NULL, NULL, 5, 0, 0, 0, NULL, '2025-11-18 19:18:12', '2025-11-18 19:18:12');

-- ----------------------------
-- Table structure for resource_comment
-- ----------------------------
DROP TABLE IF EXISTS `resource_comment`;
CREATE TABLE `resource_comment`  (
  `comment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `user_id` bigint NOT NULL COMMENT '评论者用户ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父评论ID（NULL表示一级评论，非NULL表示回复）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '软删除标志（0=正常，1=已删除）',
  PRIMARY KEY (`comment_id`) USING BTREE,
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE COMMENT '资源ID索引',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '用户ID索引',
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE COMMENT '父评论ID索引',
  INDEX `idx_created_at`(`created_at` DESC) USING BTREE COMMENT '创建时间索引',
  CONSTRAINT `fk_resource_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `resource_comment` (`comment_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_resource_comment_resource` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`r_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_resource_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资源评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of resource_comment
-- ----------------------------
INSERT INTO `resource_comment` VALUES (1, 13, 1, NULL, '121', '2025-11-17 20:01:29', '2025-11-17 20:01:29', 0);
INSERT INTO `resource_comment` VALUES (2, 16, 1, NULL, '12', '2025-11-17 20:55:40', '2025-11-17 20:55:40', 0);
INSERT INTO `resource_comment` VALUES (3, 16, 1, 2, '1', '2025-11-17 20:55:49', '2025-11-17 20:55:49', 0);
INSERT INTO `resource_comment` VALUES (4, 16, 1, NULL, '1', '2025-11-17 20:55:57', '2025-11-17 20:55:57', 0);
INSERT INTO `resource_comment` VALUES (5, 16, 53, 4, '121212121212', '2025-11-20 19:57:02', '2025-11-20 19:57:02', 0);
INSERT INTO `resource_comment` VALUES (6, 13, 1, 1, '111', '2025-12-17 12:27:19', '2025-12-17 12:27:19', 0);
INSERT INTO `resource_comment` VALUES (7, 13, 1, NULL, '11', '2025-12-17 12:27:28', '2025-12-17 12:27:28', 0);

-- ----------------------------
-- Table structure for resource_like
-- ----------------------------
DROP TABLE IF EXISTS `resource_like`;
CREATE TABLE `resource_like`  (
  `like_id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`like_id`) USING BTREE,
  UNIQUE INDEX `uk_user_resource`(`user_id` ASC, `resource_id` ASC) USING BTREE COMMENT '用户资源唯一索引，防止重复点赞',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '用户ID索引',
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE COMMENT '资源ID索引',
  CONSTRAINT `fk_resource_like_resource` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`r_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_resource_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资源点赞表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of resource_like
-- ----------------------------
INSERT INTO `resource_like` VALUES (7, 1, 4, '2025-11-18 16:49:49');
INSERT INTO `resource_like` VALUES (10, 1, 2, '2025-11-23 17:08:04');
INSERT INTO `resource_like` VALUES (15, 1, 16, '2025-12-15 18:32:26');
INSERT INTO `resource_like` VALUES (16, 1, 13, '2025-12-17 12:27:15');

-- ----------------------------
-- Table structure for resource_rating
-- ----------------------------
DROP TABLE IF EXISTS `resource_rating`;
CREATE TABLE `resource_rating`  (
  `rating_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评分ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `user_id` bigint NOT NULL COMMENT '评分用户ID',
  `rating` int NOT NULL COMMENT '评分(1-5)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评分时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`rating_id`) USING BTREE,
  UNIQUE INDEX `uk_user_resource`(`user_id` ASC, `resource_id` ASC) USING BTREE,
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `resource_rating_chk_1` CHECK ((`rating` >= 1) and (`rating` <= 5))
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '资源评分表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of resource_rating
-- ----------------------------
INSERT INTO `resource_rating` VALUES (2, 16, 1, 1, '2025-11-18 15:31:16', '2025-12-15 17:37:38');
INSERT INTO `resource_rating` VALUES (3, 16, 53, 4, '2025-11-20 19:41:34', '2025-11-20 19:50:47');
INSERT INTO `resource_rating` VALUES (4, 1, 1, 3, '2025-11-23 16:55:48', '2025-12-08 20:32:12');
INSERT INTO `resource_rating` VALUES (5, 3, 1, 2, '2025-12-08 20:16:56', '2025-12-08 20:16:56');
INSERT INTO `resource_rating` VALUES (6, 4, 1, 3, '2025-12-08 20:17:09', '2025-12-08 20:17:09');
INSERT INTO `resource_rating` VALUES (7, 2, 1, 4, '2025-12-15 19:51:04', '2025-12-15 19:51:11');

-- ----------------------------
-- Table structure for school
-- ----------------------------
DROP TABLE IF EXISTS `school`;
CREATE TABLE `school`  (
  `school_id` bigint NOT NULL AUTO_INCREMENT COMMENT '学校ID',
  `school_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学校名称',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详细地址',
  `logo_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学校Logo',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`school_id`) USING BTREE,
  INDEX `idx_city`(`city` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学校信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of school
-- ----------------------------
INSERT INTO `school` VALUES (1, '北京大学(更新)', '北京市', '北京市', '北京市海淀区颐和园路5号(新地址)', 'https://www.pku.edu.cn/logo.png', 1, '2025-11-09 13:37:46');
INSERT INTO `school` VALUES (2, '清华大学', '北京市', '北京市', '北京市海淀区清华园1号', 'https://www.tsinghua.edu.cn/new-logo.png', 1, '2025-11-09 13:37:48');
INSERT INTO `school` VALUES (3, '复旦大学', '上海市', '上海市', '上海市杨浦区邯郸路220号', 'https://www.fudan.edu.cn/logo.png', 1, '2025-11-09 13:37:50');
INSERT INTO `school` VALUES (4, '浙江大学', '浙江省', '杭州市', '浙江省杭州市西湖区余杭塘路866号', 'https://www.zju.edu.cn/logo.png', 1, '2025-11-09 13:37:52');
INSERT INTO `school` VALUES (16, '南京大学', '江苏省', '南京市', '江苏省南京市栖霞区仙林大道163号', 'https://www.nju.edu.cn/logo.png', 1, '2025-11-09 13:41:56');

-- ----------------------------
-- Table structure for search_keyword
-- ----------------------------
DROP TABLE IF EXISTS `search_keyword`;
CREATE TABLE `search_keyword`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `keyword` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '搜索关键词',
  `search_count` int NULL DEFAULT 1 COMMENT '搜索次数',
  `daily_count` int NULL DEFAULT 1 COMMENT '今日搜索次数',
  `weekly_count` int NULL DEFAULT 1 COMMENT '本周搜索次数',
  `last_rank` int NULL DEFAULT NULL COMMENT '上次排名（用于计算趋势）',
  `current_rank` int NULL DEFAULT NULL COMMENT '当前排名',
  `first_search_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次搜索时间',
  `last_search_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后搜索时间',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_keyword`(`keyword` ASC) USING BTREE,
  INDEX `idx_search_count`(`search_count` DESC) USING BTREE,
  INDEX `idx_daily_count`(`daily_count` DESC) USING BTREE,
  INDEX `idx_last_search_at`(`last_search_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '搜索关键词统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of search_keyword
-- ----------------------------
INSERT INTO `search_keyword` VALUES (1, '高等数学', 1523, 89, 456, 2, 1, '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49');
INSERT INTO `search_keyword` VALUES (2, '英语四级', 1280, 76, 398, 0, 2, '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49');
INSERT INTO `search_keyword` VALUES (3, '数据结构', 956, 65, 312, NULL, 3, '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49');
INSERT INTO `search_keyword` VALUES (4, '考研资料', 823, 58, 287, 4, 4, '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49');
INSERT INTO `search_keyword` VALUES (5, 'Python入门', 654, 45, 234, 3, 5, '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49');
INSERT INTO `search_keyword` VALUES (6, '线性代数', 521, 38, 189, 7, 6, '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49');
INSERT INTO `search_keyword` VALUES (7, '计算机网络', 489, 32, 167, 5, 7, '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49');
INSERT INTO `search_keyword` VALUES (8, 'Java编程', 456, 28, 145, NULL, 8, '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49');
INSERT INTO `search_keyword` VALUES (9, '操作系统', 423, 25, 132, 10, 9, '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49');
INSERT INTO `search_keyword` VALUES (10, '大学物理', 398, 22, 118, NULL, 10, '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49');
INSERT INTO `search_keyword` VALUES (11, 'C语言', 367, 19, 98, NULL, 11, '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49');
INSERT INTO `search_keyword` VALUES (12, '软件工程', 334, 16, 87, NULL, 12, '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49');
INSERT INTO `search_keyword` VALUES (13, '数据库原理', 312, 14, 76, NULL, 13, '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49');
INSERT INTO `search_keyword` VALUES (14, '离散数学', 289, 12, 65, NULL, 14, '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49');
INSERT INTO `search_keyword` VALUES (15, '概率论', 267, 10, 54, NULL, 15, '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49', '2025-12-09 17:26:49');

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置值',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置说明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_config
-- ----------------------------
INSERT INTO `system_config` VALUES (1, 'points.upload_resource', '10', '上传资源获得的积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (2, 'points.download_resource', '-5', '下载资源消耗的积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (3, 'points.answer_question', '5', '回答问题获得的积分', '2025-11-08 19:09:44', '2025-11-09 14:20:04');
INSERT INTO `system_config` VALUES (4, 'points.answer_accepted', '20', '回答被采纳获得的积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (5, 'points.ask_question', '-2', '提问消耗的积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (6, 'points.complete_task', '10', '完成任务获得的积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (7, 'points.daily_signin', '15', '每日签到获得的积分数（已调整）', '2025-11-08 19:09:44', '2025-11-09 14:19:38');
INSERT INTO `system_config` VALUES (8, 'points.activity_signin', '10', '活动签到获得的积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (9, 'level.threshold_1', '0', '等级1所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (10, 'level.threshold_2', '100', '等级2所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (11, 'level.threshold_3', '300', '等级3所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (12, 'level.threshold_4', '600', '等级4所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (13, 'level.threshold_5', '1000', '等级5所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (14, 'level.threshold_6', '1500', '等级6所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (15, 'level.threshold_7', '2100', '等级7所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (16, 'level.threshold_8', '2800', '等级8所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (17, 'level.threshold_9', '3600', '等级9所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (18, 'level.threshold_10', '5000', '等级10所需积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (19, 'upload.max_file_size', '209715200', '上传文件最大大小（字节），调整为200MB', '2025-11-08 19:09:44', '2025-11-09 14:20:12');
INSERT INTO `system_config` VALUES (20, 'upload.allowed_types', 'pdf,doc,docx,ppt,pptx,xls,xlsx,zip,rar,jpg,png', '允许上传的文件类型', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (21, 'resource.review_required', 'true', '资源是否需要审核', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (22, 'message.max_length', '1000', '私信最大长度', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (23, 'comment.max_length', '500', '评论最大长度', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (24, 'task.max_reward_points', '100', '任务最大悬赏积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (25, 'question.max_reward_points', '50', '问题最大悬赏积分', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (26, 'ai.enabled', 'true', 'AI功能是否启用', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (27, 'ai.auto_answer', 'true', '是否自动生成AI答案', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (28, 'ai.answer_delay', '5', 'AI答案生成延迟（秒）', '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `system_config` VALUES (29, 'system.name', 'CampusLink 校园平台', '系统名称（批量更新）', '2025-11-09 14:18:24', '2025-11-09 14:19:48');
INSERT INTO `system_config` VALUES (30, 'points.daily_signin1', '10', '每日签到获得的积分数', '2025-11-09 14:18:43', '2025-11-09 14:18:43');
INSERT INTO `system_config` VALUES (31, 'upload.allowed_file_types', 'pdf,doc,docx,ppt,pptx,xls,xlsx,zip,rar,jpg,png', '允许上传的文件类型', '2025-11-09 14:18:54', '2025-11-09 14:18:54');
INSERT INTO `system_config` VALUES (33, 'email.smtp_port', '587', 'SMTP服务器端口', '2025-11-09 14:19:48', '2025-11-09 14:19:48');
INSERT INTO `system_config` VALUES (34, 'points.answer_adopted', '30', '回答被采纳获得的积分（已提高奖励）', '2025-11-09 14:20:04', '2025-11-09 14:20:09');
INSERT INTO `system_config` VALUES (35, 'points.publish_task', '0', '发布任务消耗的积分', '2025-11-09 14:20:04', '2025-11-09 14:20:04');
INSERT INTO `system_config` VALUES (36, 'upload.image_max_size', '5242880', '图片上传最大大小（5MB）', '2025-11-09 14:20:11', '2025-11-09 14:20:11');
INSERT INTO `system_config` VALUES (37, '1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890', '测试值', '100字符的配置键', '2025-11-09 14:20:18', '2025-11-09 14:20:18');
INSERT INTO `system_config` VALUES (38, 'test.max_desc', '测试值', '这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制这是一段很长的描述用于测试字数限制', '2025-11-09 14:20:20', '2025-11-09 14:20:20');
INSERT INTO `system_config` VALUES (39, 'test.long_value', '这是一段很长很长的配置值，可以包含大量文本内容，因为数据库字段类型是TEXT，理论上可以存储非常长的内容。这是一段很长很长的配置值，可以包含大量文本内容，因为数据库字段类型是TEXT，理论上可以存储非常长的内容。这是一段很长很长的配置值，可以包含大量文本内容，因为数据库字段类型是TEXT，理论上可以存储非常长的内容。这是一段很长很长的配置值，可以包含大量文本内容，因为数据库字段类型是TEXT，理论上可以存储非常长的内容。', '测试长配置值', '2025-11-09 14:20:25', '2025-11-09 14:20:25');
INSERT INTO `system_config` VALUES (40, 'test.json_config', '{\"feature1\": true, \"feature2\": false, \"maxCount\": 100}', 'JSON格式的配置', '2025-11-09 14:20:27', '2025-11-09 14:20:27');

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `tag_id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID（主键）',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称（不含#号）',
  `display_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '显示名称（含#号，如：#考研资料）',
  `use_count` int NOT NULL DEFAULT 0 COMMENT '使用次数（热度）',
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签分类：学习/生活/技术/娱乐等',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`tag_id`) USING BTREE,
  UNIQUE INDEX `uk_tag_name`(`tag_name` ASC) USING BTREE,
  INDEX `idx_use_count`(`use_count` DESC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tag
-- ----------------------------
INSERT INTO `tag` VALUES (1, '考研资料', '#考研资料', 120, '学习', '考研相关的资料和经验分享', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (2, '学习打卡', '#学习打卡', 95, '学习', '记录和分享学习进度', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (3, 'AI问答', '#AI问答', 88, '技术', 'AI和人工智能相关问答', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (4, 'Python基础', '#Python基础', 76, '技术', 'Python编程基础知识', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (5, '数据结构', '#数据结构', 82, '学习', '数据结构与算法相关内容', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (6, '英语四六级', '#英语四六级', 110, '学习', '英语四六级考试资料和经验', 1, '2025-11-10 16:52:39', '2025-11-10 18:58:37');
INSERT INTO `tag` VALUES (7, '算法刷题', '#算法刷题', 68, '技术', '算法题目和解题思路', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (8, '前端开发', '#前端开发', 72, '技术', '前端技术和开发经验', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (9, 'Java学习', '#Java学习', 64, '技术', 'Java编程学习资料', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (10, '求职面试', '#求职面试', 58, '生活', '求职和面试经验分享', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (11, '实习经验', '#实习经验', 52, '生活', '实习相关经验分享', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');
INSERT INTO `tag` VALUES (12, '校园生活', '#校园生活', 90, '生活', '校园日常生活分享', 1, '2025-11-10 16:52:39', '2025-11-10 16:52:39');

-- ----------------------------
-- Table structure for tag_relation
-- ----------------------------
DROP TABLE IF EXISTS `tag_relation`;
CREATE TABLE `tag_relation`  (
  `relation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID（主键）',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联对象类型：question/resource/task',
  `target_id` bigint NOT NULL COMMENT '关联对象ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`relation_id`) USING BTREE,
  UNIQUE INDEX `uk_tag_target`(`tag_id` ASC, `target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tag_relation
-- ----------------------------

-- ----------------------------
-- Table structure for task
-- ----------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task`  (
  `t_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `publisher_id` bigint NOT NULL COMMENT '发布者ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务内容',
  `task_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务类型（errand/borrow/sign等）',
  `reward_points` int NOT NULL DEFAULT 0 COMMENT '悬赏积分',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地点',
  `deadline` datetime NULL DEFAULT NULL COMMENT '截止时间',
  `accepter_id` bigint NULL DEFAULT NULL COMMENT '接单者ID',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待接单，1-进行中，2-已完成，3-已取消',
  `images` json NULL COMMENT '图片URL列表（JSON数组）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `result_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '执行者提交的结果描述',
  `result_attachments` json NULL COMMENT '结果附件URL列表',
  `submitted_at` datetime NULL DEFAULT NULL COMMENT '提交结果时间',
  `rejection_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发布者拒绝原因',
  `accepted_at` datetime NULL DEFAULT NULL COMMENT '接单时间',
  `started_at` datetime NULL DEFAULT NULL COMMENT '开始执行时间',
  PRIMARY KEY (`t_id`) USING BTREE,
  INDEX `idx_publisher_id`(`publisher_id` ASC) USING BTREE,
  INDEX `idx_accepter_id`(`accepter_id` ASC) USING BTREE,
  INDEX `idx_task_type`(`task_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_status_deadline`(`status` ASC, `deadline` ASC) USING BTREE,
  INDEX `idx_accepter_status`(`accepter_id` ASC, `status` ASC) USING BTREE,
  CONSTRAINT `fk_task_accepter` FOREIGN KEY (`accepter_id`) REFERENCES `user` (`u_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_task_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of task
-- ----------------------------
INSERT INTO `task` VALUES (1, 2, '帮忙取快递', '菜鸟驿站有个快递，帮忙取一下，取件码：1234', 'errand', 5, '菜鸟驿站', '2025-11-09 19:09:44', NULL, 6, NULL, '2025-11-08 19:09:44', '2025-11-22 11:00:00', NULL, NULL, NULL, NULL, NULL, '2025-11-11 18:47:48', NULL);
INSERT INTO `task` VALUES (2, 3, '图书馆借书', '帮忙去图书馆借一本《算法导论》，书号：TP301.6/C73', 'borrow', 8, '图书馆', '2025-11-10 19:09:44', NULL, 6, NULL, '2025-11-08 19:09:44', '2025-11-22 11:00:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `task` VALUES (3, 5, '代签到', '明天上午第一节课帮忙签到，教室：教学楼A101', 'sign', 3, '教学楼A101', '2025-11-09 19:09:44', NULL, 6, NULL, '2025-11-08 19:09:44', '2025-11-22 11:00:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `task` VALUES (7, 23, '测试任务 - 用于验证积分日志', '这是一个测试任务，用于验证积分日志记录功能', 'other', 10, '测试地点', '2025-11-10 18:00:00', 24, 4, NULL, '2025-11-08 20:27:10', '2025-11-11 16:06:24', '2025-11-08 20:27:42', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `task` VALUES (8, 23, '测试任务2 - 用于取消', '这个任务将被取消，用于测试退还积分', 'other', 5, '测试地点', '2025-11-10 20:00:00', NULL, 6, NULL, '2025-11-08 20:27:53', '2025-11-22 11:00:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `task` VALUES (11, 1, '测试接单任务', '威慑放松放松的方式方法第三方都是', 'sign', 112, NULL, '2025-11-22 16:00:00', NULL, 6, NULL, '2025-11-22 10:08:54', '2025-11-22 16:00:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `task` VALUES (12, 1, '测试截单时间任务', '测试截单时间任务测试截单时间任务测试截单时间任务', 'sign', 12, NULL, '2025-11-22 02:30:00', NULL, 6, NULL, '2025-11-22 10:15:37', '2025-11-22 11:00:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `task` VALUES (13, 1, '测试截单时间任务', '测试截单时间任务测试截单时间任务', 'borrow', 12, '21', '2025-11-22 03:30:00', NULL, 6, NULL, '2025-11-22 10:16:16', '2025-11-22 11:00:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `task` VALUES (14, 1, '测试截止时间显示', '测试截止时间显示测试截止时间显示', 'borrow', 12, NULL, '2025-11-22 11:00:00', 48, 6, NULL, '2025-11-22 10:34:53', '2025-11-22 11:00:00', '2025-11-22 10:36:16', NULL, NULL, NULL, NULL, '2025-11-22 10:35:25', NULL);
INSERT INTO `task` VALUES (15, 1, '测试截止时间显示1', '测试截止时间显示测试截止时间显示', 'borrow', 12, NULL, '2025-11-22 00:00:00', NULL, 6, NULL, '2025-11-22 10:40:13', '2025-11-22 11:00:00', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `task` VALUES (16, 1, '测试地点发布', '测试地点发布测试地点发布', 'errand', 23, '139.640832, 35.681075', '2025-11-22 11:30:00', 48, 6, NULL, '2025-11-22 11:02:51', '2025-11-22 12:00:00', '2025-11-22 11:03:22', NULL, NULL, NULL, NULL, '2025-11-22 11:03:10', NULL);
INSERT INTO `task` VALUES (17, 1, '测试地点发布1', '测试地点发布测试地点发布', 'errand', 20, '139.640832, 35.681075', NULL, NULL, 3, NULL, '2025-11-22 11:05:08', '2025-11-22 11:09:36', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `task` VALUES (18, 1, '测试地点发布', '测试地点发布测试地点发布', 'errand', 20, '139.640832, 35.681075', '2025-11-22 16:00:00', 48, 6, NULL, '2025-11-22 11:10:16', '2025-11-22 16:00:00', '2025-11-22 11:23:51', NULL, NULL, NULL, NULL, '2025-11-22 11:10:28', NULL);
INSERT INTO `task` VALUES (19, 1, '测试接单通知', '测试接单通知测试接单通知', 'borrow', 20, '139.640832, 35.681075', '2025-11-24 00:00:00', 48, 6, NULL, '2025-11-22 11:24:34', '2025-11-24 00:00:00', '2025-11-22 11:41:34', NULL, NULL, NULL, NULL, '2025-11-22 11:24:46', NULL);
INSERT INTO `task` VALUES (20, 1, '测试完成通知', '测试完成通知测试完成通知', 'errand', 20, NULL, '2025-11-23 00:00:00', 48, 6, NULL, '2025-11-22 11:47:42', '2025-11-23 00:00:00', '2025-11-22 11:47:56', NULL, NULL, NULL, NULL, '2025-11-22 11:47:52', NULL);
INSERT INTO `task` VALUES (21, 1, '测试快捷操作按钮', '测试快捷操作按钮测试快捷操作按钮', 'sign', 20, '139.640832, 35.681075', '2025-11-23 01:00:00', 48, 4, NULL, '2025-11-22 14:28:21', '2025-11-22 15:13:18', '2025-11-22 15:13:18', '任务已按要求完成，请查收。如有问题请联系我。', NULL, '2025-11-22 15:07:09', NULL, '2025-11-22 14:57:44', '2025-11-22 14:57:44');

-- ----------------------------
-- Table structure for task_log
-- ----------------------------
DROP TABLE IF EXISTS `task_log`;
CREATE TABLE `task_log`  (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `user_id` bigint NOT NULL COMMENT '操作用户ID',
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型: publish/accept/start/submit/confirm/reject/cancel',
  `old_status` int NULL DEFAULT NULL COMMENT '操作前状态',
  `new_status` int NOT NULL COMMENT '操作后状态',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注说明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_task_log_task` FOREIGN KEY (`task_id`) REFERENCES `task` (`t_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_task_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 60 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of task_log
-- ----------------------------

-- ----------------------------
-- Table structure for task_rating
-- ----------------------------
DROP TABLE IF EXISTS `task_rating`;
CREATE TABLE `task_rating`  (
  `rating_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `rater_id` bigint NOT NULL COMMENT '评价者ID',
  `rated_id` bigint NOT NULL COMMENT '被评价者ID',
  `rating` int NOT NULL COMMENT '评分(1-5)',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '评价内容',
  `tags` json NULL COMMENT '评价标签(如: [\"准时\", \"高效\", \"友好\"])',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`rating_id`) USING BTREE,
  UNIQUE INDEX `uk_task_rater`(`task_id` ASC, `rater_id` ASC) USING BTREE,
  INDEX `idx_rated_id`(`rated_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `fk_task_rating_rater`(`rater_id` ASC) USING BTREE,
  CONSTRAINT `fk_task_rating_rated` FOREIGN KEY (`rated_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_task_rating_rater` FOREIGN KEY (`rater_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_task_rating_task` FOREIGN KEY (`task_id`) REFERENCES `task` (`t_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_rating_value` CHECK (`rating` between 1 and 5)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of task_rating
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `u_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码哈希（BCrypt），微信登录用户可为空',
  `wechat_openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信OpenID（小程序用户唯一标识）',
  `wechat_unionid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '微信UnionID（开放平台统一标识）',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `student_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号',
  `school_id` bigint NULL DEFAULT NULL COMMENT '所属学校ID',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业',
  `grade` int NULL DEFAULT NULL COMMENT '年级',
  `role` enum('student','teacher','admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'student' COMMENT '角色',
  `points` int NOT NULL DEFAULT 0 COMMENT '积分',
  `level` int NOT NULL DEFAULT 1 COMMENT '等级',
  `credit_score` decimal(3, 1) NULL DEFAULT 5.0 COMMENT '信用评分(0.0-5.0)',
  `rating_count` int NULL DEFAULT 0 COMMENT '被评价次数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `is_verified` tinyint NOT NULL DEFAULT 0 COMMENT '是否实名认证：0-否，1-是',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`u_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `uk_wechat_openid`(`wechat_openid` ASC) USING BTREE COMMENT '微信OpenID唯一索引',
  INDEX `idx_school_id`(`school_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_wechat_unionid`(`wechat_unionid` ASC) USING BTREE COMMENT '微信UnionID索引',
  CONSTRAINT `fk_user_school` FOREIGN KEY (`school_id`) REFERENCES `school` (`school_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 58 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', 'Yuan0919', 'admin@campuslink.com', '18888888888', '0c89ea0cfd926a1dc82bcc18c5e270e6', NULL, NULL, 'https://campuslink01.oss-cn-hangzhou.aliyuncs.com/uploads/4cf85a66d368459e8320a4a4e46ce081.blob:http://localhost:5173/ea422cd8-87f2-4cab-ad72-834ba85ef159', '2024120283', NULL, '软件工程', 2025, 'admin', 9828, 10, 5.0, 0, 1, 1, '2026-01-02 16:32:49', '2025-11-08 19:09:44', '2026-01-11 12:46:30');
INSERT INTO `user` VALUES (2, 'zhangsan', '张三', 'zhangsan@example.com', '13800000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', NULL, NULL, NULL, '2021001', NULL, '计算机科学与技术', 2021, 'student', 540, 3, 5.0, 0, 1, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `user` VALUES (3, 'lisi', '李四', 'lisi@example.com', '13800000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', NULL, NULL, NULL, '2021002', NULL, '软件工程', 2021, 'student', 356, 2, 5.0, 0, 1, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `user` VALUES (4, 'wangwu', '王五', 'wangwu@example.com', '13800000003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', NULL, NULL, NULL, '2020001', NULL, '电子信息工程', 2020, 'student', 809, 5, 5.0, 0, 1, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `user` VALUES (5, 'zhaoliu', '赵六', 'zhaoliu@example.com', '13800000004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', NULL, NULL, NULL, '2022001', NULL, '数据科学与大数据技术', 2022, 'student', 262, 1, 5.0, 0, 1, 0, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `user` VALUES (6, 'sunqi', '孙七', 'sunqi@example.com', '13800000005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', NULL, NULL, NULL, '2019001', NULL, '人工智能', 2019, 'student', 1224, 8, 5.0, 0, 1, 1, NULL, '2025-11-08 19:09:44', '2025-11-08 19:09:44');
INSERT INTO `user` VALUES (18, 'task_publisher_007', '任务发布者', 'publisher@campuslink.com', '13800010005', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'student', 95, 1, 5.0, 0, 1, 0, NULL, '2025-11-08 19:40:34', '2025-11-08 19:40:34');
INSERT INTO `user` VALUES (19, 'task_accepter_007', '任务接单者', 'acceter@campuslink.com', '13800000302', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'student', 105, 1, 5.0, 0, 1, 0, NULL, '2025-11-08 19:40:50', '2025-11-08 19:40:50');
INSERT INTO `user` VALUES (20, 'club_creator_001', '社团创建者001', 'creator001@campuslink.com', '13900001001', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-08 20:14:16', '2025-11-08 20:14:16');
INSERT INTO `user` VALUES (21, 'club_member_002', '社团成员002', 'member002@campuslink.com', '13900001002', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-08 20:14:28', '2025-11-08 20:14:28');
INSERT INTO `user` VALUES (22, 'club_member_003', '社团成员003', 'member003@campuslink.com', '13900001003', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-08 20:17:14', '2025-11-08 20:17:14');
INSERT INTO `user` VALUES (23, 'user_test_001', '新昵称', 'user001@campuslink.com', '13900000002', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, 'https://example.com/new-avatar.jpg', '2021001001', NULL, '计算机科学与技术', 2021, 'student', 90, 1, 5.0, 0, 1, 0, NULL, '2025-11-08 20:26:31', '2025-11-08 20:26:52');
INSERT INTO `user` VALUES (24, 'user_test_002', '测试用户002', 'user002@campuslink.com', '13900000003', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'student', 110, 1, 5.0, 0, 1, 0, NULL, '2025-11-08 20:27:23', '2025-11-08 20:27:23');
INSERT INTO `user` VALUES (25, 'message_user_001', '消息测试用户1', 'message_user1@test.com', '13900000001', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-08 20:58:59', '2025-11-08 20:58:59');
INSERT INTO `user` VALUES (26, 'message_user_002', '消息测试用户2', 'message_user2@test.com', '13830000002', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-08 20:59:12', '2025-11-08 20:59:12');
INSERT INTO `user` VALUES (27, 'report_user_001', '举报测试用户', 'report_user@test.com', '13800000010', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-08 21:01:07', '2025-11-08 21:01:07');
INSERT INTO `user` VALUES (28, 'report_admin_001', '举报管理员', 'report_admin@test.com', '13800000011', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'admin', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-08 21:01:19', '2025-11-08 21:01:19');
INSERT INTO `user` VALUES (29, 'notify_user_001', '通知测试用户1', 'notify001@campus.com', '13800000021', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-08 21:14:37', '2025-11-08 21:14:37');
INSERT INTO `user` VALUES (30, 'notify_user_002', '通知测试用户2', 'notify002@campus.com', '13800000022', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-08 21:14:54', '2025-11-08 21:14:54');
INSERT INTO `user` VALUES (31, 'notify_user_003', '通知测试用户3', 'notify003@campus.com', '13800000023', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-08 21:15:05', '2025-11-08 21:15:05');
INSERT INTO `user` VALUES (32, 'notify_admin', '通知管理员', 'notifyadmin@campus.com', '13800000024', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'teacher', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-08 21:15:16', '2025-11-08 21:15:16');
INSERT INTO `user` VALUES (37, 'resource_user_001', '资源上传者1', 'resource001@campus.com', '13900000031', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'student', 117, 1, 5.0, 0, 1, 0, NULL, '2025-11-09 13:09:31', '2025-11-09 13:09:31');
INSERT INTO `user` VALUES (38, 'resource_user_002', '资源下载者1', 'resource002@campus.com', '13900000032', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'student', 79, 1, 5.0, 0, 1, 0, NULL, '2025-11-09 13:09:45', '2025-11-09 13:09:45');
INSERT INTO `user` VALUES (39, 'resource_user_003', '资源下载者2', 'resource003@campus.com', '13900000033', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-09 13:10:04', '2025-11-09 13:10:04');
INSERT INTO `user` VALUES (40, 'resource_admin', '资源管理员', 'resadmin@campus.com', '13900000034', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'teacher', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-09 13:10:16', '2025-11-09 13:10:16');
INSERT INTO `user` VALUES (41, 'school_admin', '学校管理员', 'schooladmin@campus.com', '13900000041', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'teacher', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-09 13:36:52', '2025-11-09 13:36:52');
INSERT INTO `user` VALUES (42, 'school_user_001', '普通用户', 'schooluser@campus.com', '13900000042', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-09 13:37:01', '2025-11-09 13:37:01');
INSERT INTO `user` VALUES (43, 'comment_user_001', '评论测试用户1', 'comment001@campus.com', '13900030041', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, 1, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-09 14:05:50', '2025-11-09 14:05:50');
INSERT INTO `user` VALUES (44, 'comment_user_002', '评论测试用户2', 'comment002@campus.com', '13900020042', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, 1, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-09 14:06:10', '2025-11-09 14:06:10');
INSERT INTO `user` VALUES (45, 'comment_user_003', '评论测试用户3', 'comment003@campus.com', '13900100043', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, 1, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-09 14:06:24', '2025-11-09 14:06:24');
INSERT INTO `user` VALUES (46, 'config_admin', '配置管理员', 'configadmin@campus.com', '13900000051', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, 1, NULL, NULL, 'admin', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-09 14:17:50', '2025-11-09 14:17:50');
INSERT INTO `user` VALUES (47, 'config_user', '普通用户', 'configuser@campus.com', '13900000052', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, 1, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-09 14:18:04', '2025-11-09 14:18:04');
INSERT INTO `user` VALUES (48, '2164601212', '2164601212', '2164601212@qq.com', NULL, 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, 1, NULL, NULL, 'student', 420, 1, 5.0, 0, 1, 0, '2025-11-29 17:07:31', '2025-11-10 21:03:00', '2025-11-29 16:36:19');
INSERT INTO `user` VALUES (49, '2164601202', '2164601202', '2164601202@qq.com', NULL, 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, 1, NULL, NULL, 'student', 120, 1, 5.0, 0, 1, 0, '2025-11-22 11:25:53', '2025-11-12 21:12:30', '2025-11-13 15:17:28');
INSERT INTO `user` VALUES (50, '216', '216', '216@qq.com', NULL, 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, 1, NULL, NULL, 'student', 110, 1, 5.0, 0, 1, 0, NULL, '2025-11-13 15:21:56', '2025-11-13 15:22:00');
INSERT INTO `user` VALUES (51, '121', '121', '121@qq.com', NULL, 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, 1, NULL, NULL, 'student', 110, 1, 5.0, 0, 1, 0, '2025-11-13 15:31:06', '2025-11-13 15:29:43', '2025-11-13 15:29:46');
INSERT INTO `user` VALUES (52, 'user_tes1t_001', '测试用户001', 'user0011@campuslink.com', '13000000001', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, 1, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-19 19:57:36', '2025-11-19 19:57:36');
INSERT INTO `user` VALUES (53, 'testuser001', '测试用户001更新', 'testuser001@example.com', NULL, '126cfbcd4d16ae6d25c9bfcae76d8ee4', NULL, NULL, NULL, NULL, NULL, '计算机科学', 2023, 'student', 0, 1, 5.0, 0, 1, 0, '2025-11-20 19:08:51', '2025-11-20 18:12:42', '2025-11-20 18:14:23');
INSERT INTO `user` VALUES (54, 'testuser002', 'testuser002', 'testuser002@example.com`', NULL, '126cfbcd4d16ae6d25c9bfcae76d8ee4', NULL, NULL, NULL, NULL, 1, NULL, NULL, 'student', 100, 1, 5.0, 0, 1, 0, NULL, '2025-11-20 18:33:00', '2025-11-20 18:33:00');
INSERT INTO `user` VALUES (55, 'test002', 'test002', 'test002@example.com', NULL, 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, 1, NULL, NULL, 'student', 110, 1, 5.0, 0, 1, 0, NULL, '2025-11-20 19:02:32', '2025-11-20 19:02:39');
INSERT INTO `user` VALUES (56, 'NuyoaH', 'NuyoaH', '725667073@qq.com', NULL, 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, 1, NULL, NULL, 'student', 115, 1, 5.0, 0, 1, 0, NULL, '2025-12-12 14:45:18', '2025-12-12 14:45:25');
INSERT INTO `user` VALUES (57, 'wx_oZQOu7WN', '微信用户_4gfpav', NULL, NULL, NULL, 'oZQOu7WN5QmOouW1Q73eObcT5GzM', NULL, NULL, NULL, NULL, NULL, NULL, 'student', 89, 1, 5.0, 0, 1, 0, '2025-12-18 15:38:56', '2025-12-18 10:58:16', '2025-12-18 15:38:56');

-- ----------------------------
-- View structure for v_hot_tags
-- ----------------------------
DROP VIEW IF EXISTS `v_hot_tags`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_hot_tags` AS select `tag`.`tag_id` AS `tag_id`,`tag`.`tag_name` AS `tag_name`,`tag`.`display_name` AS `display_name`,`tag`.`use_count` AS `use_count`,`tag`.`category` AS `category`,`tag`.`description` AS `description` from `tag` where (`tag`.`status` = 1) order by `tag`.`use_count` desc limit 20;

-- ----------------------------
-- Procedure structure for sp_increment_tag_use_count
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_increment_tag_use_count`;
delimiter ;;
CREATE PROCEDURE `sp_increment_tag_use_count`(IN p_tag_name VARCHAR(50),
  IN p_increment INT)
BEGIN
  DECLARE v_tag_id BIGINT;

  -- 查找标签ID
  SELECT `tag_id` INTO v_tag_id
  FROM `tag`
  WHERE `tag_name` = p_tag_name AND `status` = 1;

  -- 如果标签存在，增加使用次数
  IF v_tag_id IS NOT NULL THEN
    UPDATE `tag`
    SET `use_count` = `use_count` + p_increment
    WHERE `tag_id` = v_tag_id;
  END IF;
END
;;
delimiter ;

-- ----------------------------
-- Table structure for reward_items（积分商品表）
-- ----------------------------
DROP TABLE IF EXISTS `reward_items`;
CREATE TABLE `reward_items` (
  `item_id`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name`         VARCHAR(50)  NOT NULL COMMENT '商品名称',
  `description`  VARCHAR(200) NOT NULL COMMENT '商品描述',
  `cover_img`    VARCHAR(255) DEFAULT NULL COMMENT '封面图片URL',
  `points_cost`  INT          NOT NULL COMMENT '所需积分',
  `stock`        INT          NOT NULL DEFAULT -1 COMMENT '库存，-1 表示无限',
  `category`     VARCHAR(30)  NOT NULL COMMENT '分类：download|privilege|badge|coupon',
  `effect_type`  VARCHAR(50)  NOT NULL COMMENT '效果类型，对应后端发放逻辑',
  `effect_value` INT          NOT NULL DEFAULT 1 COMMENT '效果数值（下载次数、天数等）',
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '0=下架 1=上架',
  `sort`         INT          NOT NULL DEFAULT 0 COMMENT '排序权重，越小越靠前',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`item_id`),
  INDEX `idx_category_status` (`category`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分商品表';

-- ----------------------------
-- Table structure for redeem_records（积分兑换记录表）
-- ----------------------------
DROP TABLE IF EXISTS `redeem_records`;
CREATE TABLE `redeem_records` (
  `record_id`    BIGINT      NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id`      BIGINT      NOT NULL COMMENT '用户ID',
  `item_id`      BIGINT      NOT NULL COMMENT '商品ID',
  `item_name`    VARCHAR(50) NOT NULL COMMENT '商品名称快照',
  `points_cost`  INT         NOT NULL COMMENT '消耗积分快照',
  `effect_type`  VARCHAR(50) NOT NULL COMMENT '效果类型快照，用于权益校验',
  `effect_value` INT         NOT NULL DEFAULT 1 COMMENT '效果数值快照',
  `status`       TINYINT     NOT NULL DEFAULT 0 COMMENT '0=待发放 1=已发放 2=已失效',
  `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '兑换时间',
  PRIMARY KEY (`record_id`),
  INDEX `idx_user_id`      (`user_id`),
  INDEX `idx_user_effect`  (`user_id`, `effect_type`, `status`),
  CONSTRAINT `fk_redeem_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_redeem_item` FOREIGN KEY (`item_id`) REFERENCES `reward_items` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换记录表';

-- ----------------------------
-- Seed data for reward_items（初始商品数据）
-- ----------------------------
INSERT INTO `reward_items` (`name`, `description`, `points_cost`, `stock`, `category`, `effect_type`, `effect_value`, `sort`) VALUES
('额外下载 5 次',     '获得 5 次资源下载额度，下载资源时无需消耗积分',                  50,  -1,  'download',  'extra_download', 5,  1),
('额外下载 20 次',    '获得 20 次资源下载额度，超值优惠',                               160, -1,  'download',  'extra_download', 20, 2),
('问题置顶 7 天',     '你发布的 1 个问题将在问答列表置顶展示 7 天',                     100, -1,  'privilege', 'question_top',   7,  3),
('「资深答者」标识',  '获得专属身份标识，展示在个人主页及所有回答中，限量发行',          200, 100, 'badge',     'badge_expert',   1,  4),
('悬赏加成券 ×1.5',  '发布任务时悬赏积分效果提升 1.5 倍，有效期 30 天（一次性）',       300, -1,  'coupon',    'task_bonus',     30, 5),
('会员体验 7 天',     '无限资源下载 + 问题推荐优先展示，有效期 7 天',                   500, -1,  'privilege', 'vip_trial',      7,  6);

SET FOREIGN_KEY_CHECKS = 1;
