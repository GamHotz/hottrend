-- =====================================================
-- HotTrend 数据库初始化脚本
-- 与 Python 版 HotTrend 表结构保持一致
-- 数据库: MySQL 8.0+
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS hottrends DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE hottrends;

-- =====================================================
-- 1. 平台信息表 (platforms)
-- 核心：id 不变，name 可变
-- =====================================================
DROP TABLE IF EXISTS platforms;
CREATE TABLE platforms (
    id VARCHAR(50) PRIMARY KEY COMMENT '平台唯一标识',
    name VARCHAR(100) NOT NULL COMMENT '显示名称',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='平台信息表';

-- =====================================================
-- 2. 新闻条目表 (news_items)
-- 以 URL + platform_id 为唯一标识，支持去重存储
-- =====================================================
DROP TABLE IF EXISTS news_items;
CREATE TABLE news_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(512) NOT NULL COMMENT '新闻标题',
    platform_id VARCHAR(50) NOT NULL COMMENT '来源平台ID',
    top_rank INT NOT NULL COMMENT '热度/排名',
    url VARCHAR(512) DEFAULT '' COMMENT '新闻链接',
    mobile_url VARCHAR(2048) DEFAULT '' COMMENT '移动端链接',
    first_crawl_time DATETIME NOT NULL COMMENT '首次抓取时间',
    last_crawl_time DATETIME NOT NULL COMMENT '最后抓取时间',
    crawl_count INT DEFAULT 1 COMMENT '抓取次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
		-- FOREIGN KEY (platform_id) REFERENCES platforms(id),
    INDEX idx_platform_id (platform_id),
    INDEX idx_crawl_time (last_crawl_time),
    INDEX idx_title (title),
    UNIQUE INDEX idx_url_platform (url, platform_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='新闻条目表';

-- =====================================================
-- 3. 标题变更历史表 (title_changes)
-- 记录同一 URL 下标题的变化
-- =====================================================
DROP TABLE IF EXISTS title_changes;
CREATE TABLE title_changes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    news_item_id BIGINT NOT NULL COMMENT '新闻ID',
    old_title VARCHAR(512) NOT NULL COMMENT '旧标题',
    new_title VARCHAR(512) NOT NULL COMMENT '新标题',
    changed_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    -- FOREIGN KEY (news_item_id) REFERENCES news_items(id),
    INDEX idx_news_item (news_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标题变更历史表';

-- =====================================================
-- 4. 排名历史表 (rank_history)
-- 记录每次抓取时的排名变化
-- =====================================================
DROP TABLE IF EXISTS rank_history;
CREATE TABLE rank_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    news_item_id BIGINT NOT NULL COMMENT '新闻ID',
    hot_rank INT NOT NULL COMMENT '排名',
    crawl_time DATETIME NOT NULL COMMENT '抓取时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    -- FOREIGN KEY (news_item_id) REFERENCES news_items(id),
    INDEX idx_news_item (news_item_id),
    INDEX idx_crawl_time (crawl_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='排名历史表';

-- =====================================================
-- 5. 抓取记录表 (crawl_records)
-- 记录每次抓取的时间和数量
-- =====================================================
DROP TABLE IF EXISTS crawl_records;
CREATE TABLE crawl_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    crawl_time DATETIME NOT NULL UNIQUE COMMENT '抓取时间',
    total_items INT DEFAULT 0 COMMENT '总条目数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_crawl_time (crawl_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='抓取记录表';

-- =====================================================
-- 6. 抓取来源状态表 (crawl_source_status)
-- 记录每次抓取各平台的成功/失败状态
-- =====================================================
DROP TABLE IF EXISTS crawl_source_status;
CREATE TABLE crawl_source_status (
    crawl_record_id BIGINT NOT NULL COMMENT '抓取记录ID',
    platform_id VARCHAR(50) NOT NULL COMMENT '平台ID',
    status VARCHAR(20) NOT NULL COMMENT '状态: success/failed',
    PRIMARY KEY (crawl_record_id, platform_id),
    -- FOREIGN KEY (crawl_record_id) REFERENCES crawl_records(id),
    -- FOREIGN KEY (platform_id) REFERENCES platforms(id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='抓取来源状态表';

-- =====================================================
-- 7. 时间段执行记录表 (period_executions)
-- 记录每天每个时间段的执行状态（用于 once 功能）
-- =====================================================
DROP TABLE IF EXISTS period_executions;
CREATE TABLE period_executions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    execution_date DATE NOT NULL COMMENT '执行日期',
    period_key VARCHAR(50) NOT NULL COMMENT '时间段的稳定key',
    action VARCHAR(50) NOT NULL COMMENT '操作: analyze/push',
    executed_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    UNIQUE INDEX idx_exec_lookup (execution_date, period_key, action),
    INDEX idx_date (execution_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='时间段执行记录表';

-- =====================================================
-- 8. RSS 源配置表 (rss_feeds)
-- 存储订阅源的基本信息
-- =====================================================
DROP TABLE IF EXISTS rss_feeds;
CREATE TABLE rss_feeds (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '显示名称',
    feed_url VARCHAR(2048) DEFAULT '' COMMENT 'RSS/Atom URL',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    last_fetch_time DATETIME COMMENT '最后抓取时间',
    last_fetch_status VARCHAR(20) COMMENT '最后抓取状态',
    item_count INT DEFAULT 0 COMMENT '当日条目数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='RSS源配置表';

-- =====================================================
-- 9. RSS 条目表 (rss_items)
-- 以 URL + feed_id 为唯一标识，支持去重存储
-- =====================================================
DROP TABLE IF EXISTS rss_items;
CREATE TABLE rss_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(512) NOT NULL COMMENT '文章标题',
    feed_id VARCHAR(50) NOT NULL COMMENT '所属RSS源',
    url VARCHAR(2048) NOT NULL COMMENT '文章链接',
    published_at DATETIME COMMENT 'RSS发布时间',
    summary VARCHAR(2048) COMMENT '文章摘要',
    author VARCHAR(100) COMMENT '作者',
    first_crawl_time DATETIME NOT NULL COMMENT '首次抓取时间',
    last_crawl_time DATETIME NOT NULL COMMENT '最后抓取时间',
    crawl_count INT DEFAULT 1 COMMENT '抓取次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    -- FOREIGN KEY (feed_id) REFERENCES rss_feeds(id),
    INDEX idx_feed_id (feed_id),
    INDEX idx_published (published_at),
    INDEX idx_crawl_time (last_crawl_time),
    INDEX idx_title (title)
    -- UNIQUE INDEX idx_url_feed (url, feed_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='RSS文章表';

-- =====================================================
-- 10. RSS 抓取记录表 (rss_crawl_records)
-- =====================================================
DROP TABLE IF EXISTS rss_crawl_records;
CREATE TABLE rss_crawl_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    crawl_time TIME NOT NULL UNIQUE COMMENT '抓取时间',
    total_items INT DEFAULT 0 COMMENT '总条目数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='RSS抓取记录表';

-- =====================================================
-- 11. RSS 抓取来源状态表 (rss_crawl_status)
-- =====================================================
DROP TABLE IF EXISTS rss_crawl_status;
CREATE TABLE rss_crawl_status (
    crawl_record_id BIGINT NOT NULL COMMENT '抓取记录ID',
    feed_id VARCHAR(50) NOT NULL COMMENT 'RSS源ID',
    status VARCHAR(20) NOT NULL COMMENT '状态: success/failed',
    error_message VARCHAR(1000) COMMENT '错误信息',
    PRIMARY KEY (crawl_record_id, feed_id)
    -- FOREIGN KEY (crawl_record_id) REFERENCES rss_crawl_records(id),
    -- FOREIGN KEY (feed_id) REFERENCES rss_feeds(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='RSS抓取来源状态表';

-- =====================================================
-- 12. RSS 推送记录表 (rss_push_records)
-- 用于 push_window once_per_day 功能
-- =====================================================
DROP TABLE IF EXISTS rss_push_records;
CREATE TABLE rss_push_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    date DATE NOT NULL UNIQUE COMMENT '日期',
    pushed TINYINT(1) DEFAULT 0 COMMENT '是否已推送',
    push_time DATETIME COMMENT '推送时间',
    ai_analyzed TINYINT(1) DEFAULT 0 COMMENT '是否已进行AI分析',
    ai_analysis_time DATETIME COMMENT 'AI分析时间',
    ai_analysis_mode VARCHAR(50) COMMENT 'AI分析模式',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='RSS推送记录表';

-- =====================================================
-- 11. 关键词配置表 (keywords)
-- =====================================================
DROP TABLE IF EXISTS keywords;
CREATE TABLE keywords (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    keyword VARCHAR(100) NOT NULL COMMENT '关键词内容',
    group_name VARCHAR(100) COMMENT '关键词分组',
    match_mode VARCHAR(20) DEFAULT 'fuzzy' COMMENT '匹配模式: exact-精确, fuzzy-模糊, regex-正则',
    weight INT DEFAULT 0 COMMENT '关键词权重（用于排序）',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_keyword (keyword),
    INDEX idx_group_name (group_name),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='关键词配置表';

-- =====================================================
-- 12. 通知发送记录表 (notification_records)
-- =====================================================
DROP TABLE IF EXISTS notification_records;
CREATE TABLE notification_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    channel VARCHAR(50) NOT NULL COMMENT '通知渠道: wechat, telegram, dingtalk, feishu, email',
    target VARCHAR(2048) COMMENT '发送目标',
    title VARCHAR(200) COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    news_count INT DEFAULT 0 COMMENT '关联的新闻数量',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '发送状态: pending, success, failed',
    error_message VARCHAR(1000) COMMENT '错误信息',
    sent_time DATETIME COMMENT '发送时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_channel (channel),
    INDEX idx_status (status),
    INDEX idx_sent_time (sent_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知发送记录表';

-- =====================================================
-- 13. AI 筛选兴趣标签表 (ai_filter_tags)
-- 存储从用户兴趣描述中 AI 提取的结构化标签
-- 按版本管理，提示词变更时旧版本标记 deprecated
-- =====================================================
DROP TABLE IF EXISTS ai_filter_tags;
CREATE TABLE ai_filter_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tag VARCHAR(100) NOT NULL COMMENT '标签名，如 "AI/大模型"',
    description VARCHAR(2048) DEFAULT '' COMMENT '标签描述，AI分类时参考',
    priority INT NOT NULL DEFAULT 9999 COMMENT '标签优先级（值越小优先级越高）',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active/deprecated',
    deprecated_at DATETIME COMMENT '废弃时间',
    version INT NOT NULL COMMENT '版本号，提示词变更时+1',
    prompt_hash VARCHAR(100) NOT NULL COMMENT '兴趣描述文件的hash',
    interests_file VARCHAR(100) NOT NULL DEFAULT 'ai_interests.txt' COMMENT '关联的兴趣文件名',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    INDEX idx_status (status),
    INDEX idx_version (version),
    INDEX idx_file_status (interests_file, status),
    INDEX idx_priority (interests_file, status, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI筛选兴趣标签表';

-- =====================================================
-- 14. AI 筛选分类结果表 (ai_filter_results)
-- 每条新闻 × 每个标签 = 一行
-- 引用 news_items.id 或 rss_items.id（通过 source_type 区分）
-- =====================================================
DROP TABLE IF EXISTS ai_filter_results;
CREATE TABLE ai_filter_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    news_item_id BIGINT NOT NULL COMMENT '引用 news_items.id 或 rss_items.id',
    source_type VARCHAR(20) NOT NULL DEFAULT 'hotlist' COMMENT '来源类型: hotlist/rss',
    tag_id BIGINT NOT NULL COMMENT '引用 ai_filter_tags.id',
    relevance_score DOUBLE DEFAULT 0 COMMENT '相关度 0.0~1.0',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active/deprecated',
    deprecated_at DATETIME COMMENT '废弃时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    UNIQUE INDEX idx_news_tag (news_item_id, source_type, tag_id),
    INDEX idx_status (status),
    INDEX idx_news_source (news_item_id, source_type),
    INDEX idx_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI筛选分类结果表';

-- =====================================================
-- 15. AI 筛选已分析新闻记录表 (ai_filter_analyzed_news)
-- 记录所有已被 AI 分析过的新闻（无论匹配与否）
-- 用于去重，避免重复发送给 AI 浪费 token
-- =====================================================
DROP TABLE IF EXISTS ai_filter_analyzed_news;
CREATE TABLE ai_filter_analyzed_news (
    news_item_id BIGINT NOT NULL COMMENT '引用 news_items.id 或 rss_items.id',
    source_type VARCHAR(20) NOT NULL DEFAULT 'hotlist' COMMENT '来源类型: hotlist/rss',
    interests_file VARCHAR(100) NOT NULL DEFAULT 'ai_interests.txt' COMMENT '关联的兴趣文件',
    prompt_hash VARCHAR(100) NOT NULL COMMENT '分析时使用的标签集hash',
    matched TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否匹配: 0-不匹配, 1-匹配',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (news_item_id, source_type, interests_file),
    INDEX idx_lookup (source_type, interests_file),
    INDEX idx_hash (interests_file, prompt_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI筛选已分析新闻记录表';

-- =====================================================
-- 初始化默认平台数据
-- =====================================================
INSERT INTO platforms (id, name, is_active) VALUES
('toutiao', '今日头条', 1),
('baidu', '百度热搜', 1),
('weibo', '微博', 1),
('douyin', '抖音', 1),
('bilibili-hot-search', 'B站热搜', 1),
('zhihu', '知乎', 1),
('thepaper', '澎湃新闻', 1),
('wallstreetcn-hot', '华尔街见闻', 1),
('cls-hot', '财联社热门', 1),
('ifeng', '凤凰网', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- =====================================================
-- 完成
-- =====================================================
SELECT '数据库初始化完成!' AS result;
