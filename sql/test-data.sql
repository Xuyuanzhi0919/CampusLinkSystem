-- 测试数据脚本
USE `campuslink`;

-- 插入测试学校数据
INSERT INTO `school` (`school_id`, `school_name`, `province`, `city`, `address`, `logo_url`, `status`, `created_at`)
VALUES
(1, '清华大学', '北京市', '北京市', '北京市海淀区清华园1号', NULL, 1, NOW()),
(2, '北京大学', '北京市', '北京市', '北京市海淀区颐和园路5号', NULL, 1, NOW()),
(3, '浙江大学', '浙江省', '杭州市', '浙江省杭州市西湖区余杭塘路866号', NULL, 1, NOW());
