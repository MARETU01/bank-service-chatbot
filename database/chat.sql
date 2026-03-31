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
CREATE TABLE session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会话 ID',
    session_id VARCHAR(36) NOT NULL UNIQUE COMMENT '会话 UUID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    title VARCHAR(100) DEFAULT '新会话' COMMENT '会话标题',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    INDEX idx_session_id (session_id(8)),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- ========================================
-- 聊天消息表
-- ========================================
CREATE TABLE message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息 ID',
    session_id VARCHAR(36) NOT NULL COMMENT '会话 UUID',
    sender_type TINYINT NOT NULL COMMENT '发送者类型：1-用户，2-ai',
    ai_metadata JSON COMMENT 'AI 元数据',
    content TEXT NOT NULL COMMENT '消息内容',
    message_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型：TEXT-文本，IMAGE-图片，FILE-文件',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_session_id (session_id),
    FOREIGN KEY (session_id) REFERENCES session(session_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- ========================================
-- 插入示例数据
-- ========================================

-- 示例聊天会话
INSERT INTO session (session_id, user_id, title) VALUES
('550e8400-e29b-41d4-a716-446655440001', 1, '账户余额查询');

-- 示例聊天消息
INSERT INTO message (session_id, sender_type, content) VALUES
('550e8400-e29b-41d4-a716-446655440001', 1, '你好，我想查询一下我的账户余额'),
('550e8400-e29b-41d4-a716-446655440001', 2, '您好！您可以登录手机银行 APP 查看账户余额，或者告诉我您的账号，我帮您查询。');
