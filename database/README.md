# 银行服务聊天bot数据库设计

本项目为银行服务聊天bot毕业设计提供了三个微服务的数据库设计。

## 数据库概览

### 1. 用户服务数据库 (user.sql)

**数据库名称**: `user_service`

**主要表结构**:
- `users` - 用户信息表
- `roles` - 角色表
- `user_roles` - 用户角色关联表
- `user_security` - 用户安全信息表

**功能说明**:
- 用户认证和授权管理
- 用户基本信息存储（用户名、邮箱、手机号等）
- 角色权限管理（普通用户、管理员、客服）
- 安全功能（双因素认证、登录失败锁定、安全问题）
- 登录记录跟踪

**默认账户**:
- 管理员账号: `admin`
- 管理员密码: `admin123`

---

### 2. 银行业务服务数据库 (bank.sql)

**数据库名称**: `bank_service`

**主要表结构**:
- `accounts` - 账户表
- `transactions` - 交易记录表
- `bills` - 账单表
- `transfers` - 转账记录表
- `account_limits` - 账户限额表

**功能说明**:
- 账户管理（储蓄账户、支票账户、信用卡）
- 交易记录查询（存款、取款、转账、支付）
- 账单管理（信用卡账单、贷款账单）
- 转账功能（行内转账、跨行转账）
- 账户限额控制（日限额、单笔限额）

**示例数据**:
- 2个测试账户（储蓄账户和支票账户）
- 示例交易记录
- 示例账单数据

---

### 3. 聊天服务数据库 (chat.sql)

**数据库名称**: `chat_service`

**主要表结构**:
- `chat_sessions` - 聊天会话表
- `chat_messages` - 聊天消息表
- `quick_replies` - 快捷回复表
- `faqs` - 常见问题表
- `ai_replies` - 智能回复记录表
- `attachments` - 附件表
- `agent_stats` - 客服工作统计表
- `chat_tags` - 聊天标签表

**功能说明**:
- 聊天会话管理（会话创建、分配、关闭）
- 消息存储和追踪（用户消息、客服回复、系统消息）
- 快捷回复功能（提高客服效率）
- 常见问题库（FAQ管理）
- AI智能回复记录（置信度、意图识别）
- 附件管理（图片、文件上传）
- 客服工作统计（工作量、满意度统计）
- 会话标签（便于分类和检索）

**示例数据**:
- 5个快捷回复模板
- 5个常见问题
- 示例聊天会话和消息
- 示例AI回复记录

---

## 使用方法

### 1. 创建数据库

使用MySQL客户端执行以下命令：

```bash
# 创建用户服务数据库
mysql -u root -p < database/user.sql

# 创建银行业务数据库
mysql -u root -p < database/bank.sql

# 创建聊天服务数据库
mysql -u root -p < database/chat.sql
```

或者分别登录MySQL后执行：

```sql
source database/user.sql;
source database/bank.sql;
source database/chat.sql;
```

### 2. 配置微服务连接

在每个微服务的`application.yaml`中配置对应的数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_service?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

---

## 数据库设计特点

### 1. 微服务架构
- 每个微服务有独立的数据库
- 数据隔离，降低耦合
- 便于独立扩展和维护

### 2. 索引优化
- 所有外键和常用查询字段都创建了索引
- 提高查询性能

### 3. 完整性约束
- 使用外键约束保证数据一致性
- 唯一索引防止重复数据

### 4. 时间追踪
- 每个表都包含`created_at`和`updated_at`字段
- 便于数据审计和追踪

### 5. 状态管理
- 使用状态字段（status）标记记录状态
- 支持软删除和业务状态流转

### 6. 安全性
- 密码使用BCrypt加密存储
- 登录失败锁定机制
- 双因素认证支持

---

## 扩展建议

### 1. 性能优化
- 对于大数据量表，考虑分区策略
- 历史数据归档机制
- 读写分离配置

### 2. 功能扩展
- 添加数据审计日志表
- 实现数据版本控制
- 添加缓存机制

### 3. 监控告警
- 数据库性能监控
- 慢查询分析
- 异常告警机制

---

## 注意事项

1. 所有数据库使用`utf8mb4`字符集，支持emoji和特殊字符
2. 使用InnoDB引擎，支持事务和外键约束
3. 根据实际需求调整字段长度和类型
4. 生产环境需修改默认账户密码
5. 定期备份数据库

---

## 技术栈

- MySQL 8.0+
- Spring Boot 3.x
- MyBatis/JPA
- Druid连接池

---

## 联系方式

如有问题或建议，请提交Issue。
