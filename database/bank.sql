-- ========================================
-- 银行业务服务数据库
-- Bank Service Database
-- ========================================

-- 设置客户端字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

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
    account_type TINYINT NOT NULL COMMENT '账户类型: 1-储蓄账户, 2-支票账户, 3-信用卡',
    balance DECIMAL(15, 2) DEFAULT 0.00 COMMENT '账户余额',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '货币类型',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-冻结, 1-正常, 2-关闭',
    overdraft_limit DECIMAL(15, 2) DEFAULT 0.00 COMMENT '透支限额',
    available_balance DECIMAL(15, 2) DEFAULT 0.00 COMMENT '可用余额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_account_number (account_number),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账户表';

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
-- 账单表
-- ========================================
CREATE TABLE bills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '账单ID',
    account_id BIGINT NOT NULL COMMENT '账户ID',
    bill_type VARCHAR(20) NOT NULL COMMENT '账单类型: CREDIT_CARD-信用卡, LOAN-贷款, SERVICE-服务费',
    bill_number VARCHAR(50) NOT NULL UNIQUE COMMENT '账单编号',
    bill_amount DECIMAL(15, 2) NOT NULL COMMENT '账单金额',
    paid_amount DECIMAL(15, 2) DEFAULT 0.00 COMMENT '已付金额',
    outstanding_amount DECIMAL(15, 2) NOT NULL COMMENT '未付金额',
    due_date DATE NOT NULL COMMENT '到期日期',
    bill_date DATE NOT NULL COMMENT '账单日期',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-未付, 1-部分支付, 2-已付, 3-逾期',
    description VARCHAR(200) COMMENT '账单描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_account_id (account_id),
    INDEX idx_bill_number (bill_number),
    INDEX idx_due_date (due_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账单表';

-- ========================================
-- 转账记录表
-- ========================================
CREATE TABLE transfers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '转账ID',
    transfer_id VARCHAR(50) NOT NULL UNIQUE COMMENT '转账ID',
    from_account_id BIGINT NOT NULL COMMENT '转出账户ID',
    to_account_id BIGINT NOT NULL COMMENT '转入账户ID',
    from_account_number VARCHAR(20) NOT NULL COMMENT '转出账号',
    to_account_number VARCHAR(20) NOT NULL COMMENT '转入账号',
    to_account_name VARCHAR(100) COMMENT '转入账户名称',
    to_bank_name VARCHAR(100) COMMENT '转入银行名称',
    amount DECIMAL(15, 2) NOT NULL COMMENT '转账金额',
    fee DECIMAL(15, 2) DEFAULT 0.00 COMMENT '手续费',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '货币类型',
    transfer_type TINYINT DEFAULT 1 COMMENT '转账类型: 1-行内转账, 2-跨行转账',
    remark VARCHAR(200) COMMENT '转账备注',
    status TINYINT DEFAULT 2 COMMENT '状态: 0-失败, 1-成功, 2-处理中',
    transfer_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '转账时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_from_account (from_account_id),
    INDEX idx_to_account (to_account_id),
    INDEX idx_transfer_id (transfer_id),
    INDEX idx_transfer_time (transfer_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='转账记录表';

-- ========================================
-- 账户限额表
-- ========================================
CREATE TABLE account_limits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    account_id BIGINT NOT NULL UNIQUE COMMENT '账户ID',
    daily_transfer_limit DECIMAL(15, 2) DEFAULT 50000.00 COMMENT '日转账限额',
    daily_withdraw_limit DECIMAL(15, 2) DEFAULT 20000.00 COMMENT '日取现限额',
    single_transfer_limit DECIMAL(15, 2) DEFAULT 10000.00 COMMENT '单笔转账限额',
    single_withdraw_limit DECIMAL(15, 2) DEFAULT 5000.00 COMMENT '单笔取现限额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账户限额表';

-- ========================================
-- 插入示例数据
-- ========================================
-- 示例账户数据
INSERT INTO accounts (account_number, user_id, account_type, balance, currency, status, overdraft_limit, available_balance) VALUES
('6222021234567890', 1, 1, 50000.00, 'CNY', 1, 0.00, 50000.00),
('6222021234567891', 1, 2, 10000.00, 'CNY', 1, 10000.00, 20000.00);

-- 示例交易记录
INSERT INTO transactions (transaction_id, account_id, transaction_type, amount, balance_after, description, counterparty_account, counterparty_name, status, transaction_time) VALUES
('TXN20250101001', 1, 'DEPOSIT', 50000.00, 50000.00, '初始存款', NULL, NULL, 1, '2025-01-01 10:00:00'),
('TXN20250101002', 2, 'DEPOSIT', 10000.00, 10000.00, '初始存款', NULL, NULL, 1, '2025-01-01 10:05:00'),
('TXN20250102001', 1, 'WITHDRAW', 1000.00, 49000.00, 'ATM取款', 'ATM001', 'ATM取款', 1, '2025-01-02 15:30:00');

-- 示例账单数据
INSERT INTO bills (account_id, bill_type, bill_number, bill_amount, paid_amount, outstanding_amount, due_date, bill_date, status, description) VALUES
(2, 'CREDIT_CARD', 'BILL202501', 2500.00, 0.00, 2500.00, '2025-01-25', '2025-01-01', 0, '1月信用卡账单');

-- 示例账户限额
INSERT INTO account_limits (account_id, daily_transfer_limit, daily_withdraw_limit, single_transfer_limit, single_withdraw_limit) VALUES
(1, 50000.00, 20000.00, 10000.00, 5000.00),
(2, 50000.00, 20000.00, 10000.00, 5000.00);
