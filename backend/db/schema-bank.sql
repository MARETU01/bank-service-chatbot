-- 银行服务数据库表结构

-- 银行账户表
CREATE TABLE accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账户ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    account_number VARCHAR(50) NOT NULL UNIQUE COMMENT '账户号码',
    account_type VARCHAR(20) NOT NULL COMMENT '账户类型: savings-储蓄账户, checking-支票账户, credit-信用卡账户',
    balance DECIMAL(15,2) DEFAULT 0.00 COMMENT '账户余额',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '货币类型',
    status TINYINT DEFAULT 1 COMMENT '账户状态: 0-冻结, 1-正常, 2-注销',
    opened_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '开户时间',
    closed_at TIMESTAMP NULL COMMENT '销户时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_accounts_user_id (user_id),
    INDEX idx_accounts_account_number (account_number)
) COMMENT '银行账户表';

-- 交易记录表
CREATE TABLE transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '交易ID',
    account_id BIGINT NOT NULL COMMENT '账户ID',
    transaction_type VARCHAR(20) NOT NULL COMMENT '交易类型: deposit-存款, withdrawal-取款, transfer-转账, payment-支付',
    amount DECIMAL(15,2) NOT NULL COMMENT '交易金额',
    balance_after DECIMAL(15,2) NOT NULL COMMENT '交易后余额',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '货币类型',
    description VARCHAR(255) COMMENT '交易描述',
    reference_number VARCHAR(100) UNIQUE COMMENT '交易参考号',
    transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
    status TINYINT DEFAULT 1 COMMENT '交易状态: 0-失败, 1-成功, 2-处理中',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_transactions_account_id (account_id),
    INDEX idx_transactions_transaction_time (transaction_time)
) COMMENT '交易记录表';

-- 银行产品表
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '产品ID',
    product_code VARCHAR(50) NOT NULL UNIQUE COMMENT '产品代码',
    product_name VARCHAR(100) NOT NULL COMMENT '产品名称',
    product_type VARCHAR(50) NOT NULL COMMENT '产品类型: loan-贷款, deposit-存款, insurance-保险, fund-基金',
    description TEXT COMMENT '产品描述',
    interest_rate DECIMAL(5,4) COMMENT '利率',
    minimum_amount DECIMAL(15,2) COMMENT '最低金额',
    maximum_amount DECIMAL(15,2) COMMENT '最高金额',
    term_months INT COMMENT '期限(月)',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '货币类型',
    status TINYINT DEFAULT 1 COMMENT '产品状态: 0-下架, 1-上架',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_products_product_code (product_code),
    INDEX idx_products_product_type (product_type)
) COMMENT '银行产品表';

-- 用户产品关联表（用户购买的产品）
CREATE TABLE user_products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    account_id BIGINT COMMENT '关联账户ID',
    purchase_amount DECIMAL(15,2) NOT NULL COMMENT '购买金额',
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '购买日期',
    maturity_date TIMESTAMP NULL COMMENT '到期日期',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-已取消, 1-持有中, 2-已到期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    INDEX idx_user_products_user_id (user_id),
    INDEX idx_user_products_product_id (product_id)
) COMMENT '用户产品关联表';
