-- ========================================
-- 聊天服务数据库
-- Chat Service Database
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS chat_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE chat_service;

-- ========================================
-- 聊天会话表
-- ========================================
CREATE TABLE chat_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会话ID',
    session_id VARCHAR(50) NOT NULL UNIQUE COMMENT '会话唯一标识',
    user_id BIGINT NOT NULL COMMENT '用户ID(关联用户服务)',
    session_type TINYINT DEFAULT 1 COMMENT '会话类型: 1-普通咨询, 2-业务办理, 3-投诉建议, 4-紧急求助',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-已关闭, 1-进行中, 2-等待回复',
    priority TINYINT DEFAULT 1 COMMENT '优先级: 1-普通, 2-紧急, 3-非常紧急',
    topic VARCHAR(100) COMMENT '会话主题',
    summary TEXT COMMENT '会话摘要',
    assigned_to BIGINT COMMENT '分配给哪个客服人员',
    satisfaction_score TINYINT COMMENT '满意度评分: 1-5分',
    feedback TEXT COMMENT '用户反馈',
    closed_at DATETIME COMMENT '关闭时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- ========================================
-- 聊天消息表
-- ========================================
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    message_id VARCHAR(50) NOT NULL UNIQUE COMMENT '消息唯一标识',
    sender_type TINYINT NOT NULL COMMENT '发送者类型: 1-用户, 2-客服, 3-系统',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    sender_name VARCHAR(100) COMMENT '发送者名称',
    message_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型: TEXT-文本, IMAGE-图片, FILE-文件, QUICK_REPLY-快捷回复',
    content TEXT NOT NULL COMMENT '消息内容',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    read_at DATETIME COMMENT '阅读时间',
    extra_data JSON COMMENT '额外数据(图片URL、文件信息等)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_session_id (session_id),
    INDEX idx_message_id (message_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- ========================================
-- 快捷回复表
-- ========================================
CREATE TABLE quick_replies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    category VARCHAR(50) NOT NULL COMMENT '分类',
    title VARCHAR(100) NOT NULL COMMENT '快捷回复标题',
    content TEXT NOT NULL COMMENT '快捷回复内容',
    keywords VARCHAR(200) COMMENT '关键词(逗号分隔)',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='快捷回复表';

-- ========================================
-- 常见问题表
-- ========================================
CREATE TABLE faqs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    question VARCHAR(200) NOT NULL COMMENT '问题',
    answer TEXT NOT NULL COMMENT '答案',
    category VARCHAR(50) COMMENT '分类',
    keywords VARCHAR(200) COMMENT '关键词(逗号分隔)',
    priority INT DEFAULT 0 COMMENT '优先级',
    view_count INT DEFAULT 0 COMMENT '查看次数',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='常见问题表';

-- ========================================
-- 智能回复记录表
-- ========================================
CREATE TABLE ai_replies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    message_id BIGINT NOT NULL COMMENT '关联的消息ID',
    user_question TEXT NOT NULL COMMENT '用户问题',
    ai_answer TEXT NOT NULL COMMENT 'AI回答',
    confidence DECIMAL(5, 4) COMMENT '置信度',
    intent VARCHAR(100) COMMENT '识别的意图',
    is_accepted BOOLEAN COMMENT '是否被用户接受',
    manual_override BOOLEAN DEFAULT FALSE COMMENT '是否被人工覆盖',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_message_id (message_id),
    INDEX idx_confidence (confidence)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能回复记录表';

-- ========================================
-- 附件表
-- ========================================
CREATE TABLE attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    message_id BIGINT NOT NULL COMMENT '关联消息ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_size BIGINT NOT NULL COMMENT '文件大小(字节)',
    file_type VARCHAR(50) NOT NULL COMMENT '文件类型',
    file_url VARCHAR(500) NOT NULL COMMENT '文件URL',
    storage_path VARCHAR(500) COMMENT '存储路径',
    uploader_type TINYINT NOT NULL COMMENT '上传者类型: 1-用户, 2-客服',
    uploader_id BIGINT NOT NULL COMMENT '上传者ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_message_id (message_id),
    INDEX idx_file_type (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='附件表';

-- ========================================
-- 客服工作统计表
-- ========================================
CREATE TABLE agent_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    agent_id BIGINT NOT NULL COMMENT '客服人员ID',
    agent_name VARCHAR(100) COMMENT '客服人员名称',
    stat_date DATE NOT NULL COMMENT '统计日期',
    total_sessions INT DEFAULT 0 COMMENT '处理会话总数',
    closed_sessions INT DEFAULT 0 COMMENT '已关闭会话数',
    total_messages INT DEFAULT 0 COMMENT '处理消息总数',
    avg_response_time INT DEFAULT 0 COMMENT '平均响应时间(秒)',
    satisfaction_avg DECIMAL(3, 2) COMMENT '平均满意度',
    work_hours DECIMAL(5, 2) COMMENT '工作时长(小时)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_agent_date (agent_id, stat_date),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服工作统计表';

-- ========================================
-- 聊天标签表
-- ========================================
CREATE TABLE chat_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    tagged_by BIGINT NOT NULL COMMENT '打标签人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_session_id (session_id),
    INDEX idx_tag_name (tag_name),
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天标签表';

-- ========================================
-- 插入示例数据
-- ========================================
-- 示例快捷回复
INSERT INTO quick_replies (category, title, content, keywords, sort_order) VALUES
('账户查询', '查询余额', '您可以登录网上银行或手机银行查看账户余额，也可以告诉我您的账号，我帮您查询。', '余额,查询余额,有多少钱', 1),
('账户查询', '查询交易记录', '您可以在手机银行的"交易明细"中查看最近的交易记录。需要我帮您查询特定日期的交易吗？', '交易记录,明细,账单', 2),
('业务办理', '如何转账', '登录手机银行后，点击"转账"按钮，选择转账类型，输入对方账号和金额即可完成转账。', '转账,汇款', 3),
('业务办理', '修改密码', '您可以通过手机银行"安全中心"修改登录密码，或携带身份证到柜台办理。', '密码,修改密码', 4),
('常见问题', '忘记密码怎么办', '忘记密码可以在登录页面点击"忘记密码"，通过手机号验证码重置密码。', '忘记密码,找回密码', 5);

-- 示例常见问题
INSERT INTO faqs (question, answer, category, keywords, priority) VALUES
('如何查询账户余额？', '您可以通过以下方式查询账户余额：1. 登录手机银行APP查看；2. 拨打客服电话查询；3. 前往银行柜台或ATM机查询。', '账户查询', '余额,查询,账户', 100),
('转账多久到账？', '行内转账实时到账，跨行转账一般在2小时内到账，最晚不超过24小时。', '转账', '转账,到账,时间', 90),
('如何开通手机银行？', '您可以携带身份证和银行卡前往任意网点开通手机银行，开通后下载APP即可使用。', '业务办理', '手机银行,开通,注册', 80),
('忘记密码怎么办？', '在登录页面点击"忘记密码"，通过手机验证码或安全问题验证后可以重置密码。', '账户安全', '忘记密码,重置', 70),
('银行卡丢失怎么办？', '请立即拨打客服电话挂失，或登录手机银行自助挂失，然后前往网点补办新卡。', '账户安全', '挂失,丢失,补卡', 95);

-- 示例聊天会话
INSERT INTO chat_sessions (session_id, user_id, session_type, status, priority, topic) VALUES
('SESSION20250101001', 1, 1, 1, 1, '查询账户余额');

-- 示例聊天消息
INSERT INTO chat_messages (session_id, message_id, sender_type, sender_id, sender_name, message_type, content) VALUES
(1, 'MSG20250101001', 1, 1, '张三', 'TEXT', '你好，我想查询一下我的账户余额'),
(1, 'MSG20250101002', 2, 2, '客服小王', 'TEXT', '您好！请提供您的账号，我帮您查询。');

-- 示例智能回复记录
INSERT INTO ai_replies (message_id, user_question, ai_answer, confidence, intent, is_accepted) VALUES
(1, '你好，我想查询一下我的账户余额', '您好！我可以帮您查询账户余额，请提供您的账号。', 0.9523, 'query_balance', NULL);

-- 示例标签
INSERT INTO chat_tags (session_id, tag_name, tagged_by) VALUES
(1, '账户查询', 2),
(1, '简单问题', 2);
