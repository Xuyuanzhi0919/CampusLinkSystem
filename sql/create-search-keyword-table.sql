-- 搜索关键词统计表
-- 用于记录用户搜索行为，支持热词排行功能

CREATE TABLE IF NOT EXISTS search_keyword (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    keyword VARCHAR(100) NOT NULL COMMENT '搜索关键词',
    search_count INT DEFAULT 1 COMMENT '搜索次数',
    daily_count INT DEFAULT 1 COMMENT '今日搜索次数',
    weekly_count INT DEFAULT 1 COMMENT '本周搜索次数',
    last_rank INT DEFAULT NULL COMMENT '上次排名（用于计算趋势）',
    current_rank INT DEFAULT NULL COMMENT '当前排名',
    first_search_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '首次搜索时间',
    last_search_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后搜索时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE INDEX idx_keyword (keyword),
    INDEX idx_search_count (search_count DESC),
    INDEX idx_daily_count (daily_count DESC),
    INDEX idx_last_search_at (last_search_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索关键词统计表';

-- 插入一些初始热词数据
INSERT INTO search_keyword (keyword, search_count, daily_count, weekly_count, current_rank) VALUES
('高等数学', 1523, 89, 456, 1),
('英语四级', 1280, 76, 398, 2),
('数据结构', 956, 65, 312, 3),
('考研资料', 823, 58, 287, 4),
('Python入门', 654, 45, 234, 5),
('线性代数', 521, 38, 189, 6),
('计算机网络', 489, 32, 167, 7),
('Java编程', 456, 28, 145, 8),
('操作系统', 423, 25, 132, 9),
('大学物理', 398, 22, 118, 10),
('C语言', 367, 19, 98, 11),
('软件工程', 334, 16, 87, 12),
('数据库原理', 312, 14, 76, 13),
('离散数学', 289, 12, 65, 14),
('概率论', 267, 10, 54, 15);

-- 更新上次排名（模拟历史数据）
UPDATE search_keyword SET last_rank = current_rank + FLOOR(RAND() * 5) - 2 WHERE current_rank <= 10;
UPDATE search_keyword SET last_rank = NULL WHERE id IN (SELECT id FROM (SELECT id FROM search_keyword ORDER BY RAND() LIMIT 3) AS tmp);
