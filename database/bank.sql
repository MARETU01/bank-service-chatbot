-- ========================================
-- 银行业务服务数据库 (毕业设计简化版)
-- Bank Service Database (Simplified for Graduation Project)
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS bank_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bank_service;

-- ========================================
-- 账户表
-- ========================================
CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '账户ID',
    account_number VARCHAR(20) NOT NULL UNIQUE COMMENT '账号',
    user_id BIGINT NOT NULL COMMENT '用户ID(关联用户服务)',
    account_name VARCHAR(50) DEFAULT '储蓄账户' COMMENT '账户名称',
    balance DECIMAL(15, 2) DEFAULT 0.00 COMMENT '账户余额',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '货币类型',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-冻结, 1-正常, 2-关闭',
    daily_limit DECIMAL(15, 2) DEFAULT 50000.00 COMMENT '单日交易限额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_account_number (account_number),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账户表';

-- ========================================
-- 账户日限额使用表
-- ========================================
CREATE TABLE account_daily_limits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    account_id BIGINT NOT NULL COMMENT '账户ID',
    limit_date DATE NOT NULL COMMENT '限额日期',
    daily_limit DECIMAL(15, 2) NOT NULL COMMENT '当日限额快照(来自accounts.daily_limit)',
    used_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00 COMMENT '当日已用额度(支出累计)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_account_date (account_id, limit_date),
    INDEX idx_limit_date (limit_date),
    INDEX idx_account_id (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账户日限额使用表';

-- ========================================
-- 交易记录表
-- ========================================
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '交易ID',
    transaction_id VARCHAR(50) NOT NULL UNIQUE COMMENT '交易流水号',
    account_id BIGINT NOT NULL COMMENT '账户ID',
    transaction_type VARCHAR(20) NOT NULL COMMENT '交易类型: DEPOSIT-存款, WITHDRAW-取款, TRANSFER_IN-转入, TRANSFER_OUT-转出, PAYMENT-支付',
    amount DECIMAL(15, 2) NOT NULL COMMENT '交易金额',
    balance_after DECIMAL(15, 2) NOT NULL COMMENT '交易后余额',
    description VARCHAR(200) COMMENT '交易描述',
    counterparty_account VARCHAR(50) COMMENT '对方账号',
    counterparty_name VARCHAR(100) COMMENT '对方名称',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-失败, 1-成功, 2-处理中',
    transaction_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_account_id (account_id),
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_transaction_time (transaction_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易记录表';

-- ========================================
-- 转账记录表
-- ========================================
CREATE TABLE transfers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '转账 ID',
    transfer_id VARCHAR(50) NOT NULL UNIQUE COMMENT '转账流水号',
    from_account_id BIGINT NOT NULL COMMENT '转出账户 ID',
    to_account_id BIGINT COMMENT '转入账户 ID',
    to_account_number VARCHAR(20) NOT NULL COMMENT '转入账号',
    to_account_name VARCHAR(100) COMMENT '转入账户名',
    amount DECIMAL(15, 2) NOT NULL COMMENT '转账金额',
    remark VARCHAR(200) COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-失败, 1-成功, 2-处理中',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_from_account (from_account_id),
    INDEX idx_transfer_id (transfer_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='转账记录表';

-- ========================================
-- 插入示例数据
-- ========================================
-- 示例账户数据 (每个用户一个储蓄账户)
INSERT INTO accounts (account_number, user_id, account_name, balance, currency, status, daily_limit) VALUES
('6222021234567890', 1, '我的储蓄卡', 125680.00, 'CNY', 1, 50000.00);

-- 示例交易记录
INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, balance_after, description, counterparty_account, counterparty_name, status, transaction_time) VALUES
('TXN20250101001', 1, 'DEPOSIT', 150000.00, 150000.00, '工资收入', '对公账户', 'XX科技有限公司', 1, '2025-01-01 09:00:00'),
('TXN20250102001', 1, 'PAYMENT', 500.00, 149500.00, '超市购物', '商户', 'XX超市', 1, '2025-01-02 18:30:00'),
('TXN20250103001', 1, 'TRANSFER_OUT', 5000.00, 144500.00, '转账给张三', '6222021234567891', '张三', 1, '2025-01-03 10:00:00'),
('TXN20250104001', 1, 'WITHDRAW', 2000.00, 142500.00, 'ATM取款', 'ATM001', 'ATM取款', 1, '2025-01-04 15:00:00'),
('TXN20250105001', 1, 'TRANSFER_IN', 8000.00, 150500.00, '收到李四转账', '6222021234567892', '李四', 1, '2025-01-05 14:00:00'),
('TXN20250106001', 1, 'PAYMENT', 1280.00, 149220.00, '餐饮消费', '商户', 'XX餐厅', 1, '2025-01-06 12:30:00'),
('TXN20250107001', 1, 'PAYMENT', 25000.00, 124220.00, '信用卡还款', '信用卡', 'XX银行', 1, '2025-01-07 10:00:00'),
('TXN20250108001', 1, 'DEPOSIT', 1500.00, 125720.00, '利息收入', '银行', 'XX银行', 1, '2025-01-08 00:00:00');

-- 示例转账记录
INSERT INTO transfers (transfer_id, from_account_id, to_account_number, to_account_name, amount, fee, remark, status, created_at) VALUES
('TRF20250103001', 1, '6222021234567891', '张三', 5000.00, 0.00, '借款归还', 1, '2025-01-03 10:00:00');
