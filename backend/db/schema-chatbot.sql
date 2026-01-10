-- 聊天机器人服务数据库表结构

-- 聊天会话表
CREATE TABLE chat_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    user_id BIGINT COMMENT '用户ID',
    session_id VARCHAR(100) NOT NULL UNIQUE COMMENT '会话标识符',
    title VARCHAR(255) COMMENT '会话标题',
    status TINYINT DEFAULT 1 COMMENT '会话状态: 0-已结束, 1-进行中',
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '会话开始时间',
    ended_at TIMESTAMP NULL COMMENT '会话结束时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_chat_sessions_user_id (user_id),
    INDEX idx_chat_sessions_session_id (session_id)
) COMMENT '聊天会话表';

-- 聊天消息表
CREATE TABLE chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    message_type VARCHAR(20) NOT NULL COMMENT '消息类型: user-用户消息, bot-机器人消息, system-系统消息',
    content TEXT NOT NULL COMMENT '消息内容',
    intent VARCHAR(100) COMMENT '用户意图',
    confidence DECIMAL(3,2) COMMENT '置信度',
    response_time INT COMMENT '响应时间(毫秒)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_chat_messages_session_id (session_id),
    INDEX idx_chat_messages_message_type (message_type),
    INDEX idx_chat_messages_created_at (created_at)
) COMMENT '聊天消息表';

-- 常见问题表
CREATE TABLE faq (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'FAQ ID',
    question TEXT NOT NULL COMMENT '问题',
    answer TEXT NOT NULL COMMENT '答案',
    category VARCHAR(50) COMMENT '分类',
    keywords VARCHAR(255) COMMENT '关键词',
    priority INT DEFAULT 0 COMMENT '优先级',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_faq_category (category),
    INDEX idx_faq_keywords (keywords)
) COMMENT '常见问题表';

-- 意图识别表
CREATE TABLE intents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '意图ID',
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '意图名称',
    description VARCHAR(255) COMMENT '意图描述',
    sample_questions TEXT COMMENT '示例问题',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_intents_name (name)
) COMMENT '意图识别表';

-- 实体识别表
CREATE TABLE entities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '实体ID',
    intent_id BIGINT NOT NULL COMMENT '意图ID',
    name VARCHAR(100) NOT NULL COMMENT '实体名称',
    entity_type VARCHAR(50) NOT NULL COMMENT '实体类型',
    description VARCHAR(255) COMMENT '实体描述',
    sample_values TEXT COMMENT '示例值',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    FOREIGN KEY (intent_id) REFERENCES intents(id),
    INDEX idx_entities_intent_id (intent_id)
) COMMENT '实体识别表';

-- 聊天机器人配置表
CREATE TABLE chatbot_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    description VARCHAR(255) COMMENT '配置描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_chatbot_config_key (config_key)
) COMMENT '聊天机器人配置表';
