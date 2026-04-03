# Bank-Service-Chatbot 压测清单

## 一、系统架构概览

```
                    ┌──────────┐
                    │  前端 Vue │
                    └────┬─────┘
                         │
                    ┌────▼─────┐
                    │  Gateway │  (端口 8080，统一入口)
                    └────┬─────┘
              ┌──────────┼──────────┐
              │          │          │
        ┌─────▼──┐  ┌───▼────┐  ┌──▼──────┐
        │ user   │  │ bank   │  │ chatbot │
        │service │  │service │  │ service │
        └────────┘  └────────┘  └─────────┘
```

- **Gateway**：Spring Cloud Gateway，统一入口，JWT 认证过滤
- **user-service**：用户注册/登录、Token 管理、支付密码、角色权限
- **bank-service**：银行账户、交易记录、转账
- **chatbot-service**：AI 聊天（流式响应）、会话管理、知识库

---

## 二、全部接口清单（共 30 个）

### 2.1 user-service（用户服务）— 15 个接口

| # | 方法 | 路径 | 功能 | 认证 |
|---|------|------|------|:---:|
| 1 | POST | `/users/login` | 用户登录 | ❌ |
| 2 | POST | `/users/register` | 用户注册 | ❌ |
| 3 | POST | `/users/code` | 发送验证码 | ❌/✅ |
| 4 | POST | `/users/reset-password` | 重置密码 | ❌/✅ |
| 5 | POST | `/users/refresh` | 刷新 Token | ✅ |
| 6 | GET | `/users/me` | 获取当前用户信息 | ✅ |
| 7 | PUT | `/users/profile` | 更新用户资料 | ✅ |
| 8 | POST | `/users/pay-password` | 设置支付密码 | ✅ |
| 9 | PUT | `/users/pay-password` | 修改支付密码 | ✅ |
| 10 | POST | `/users/pay-password/verify` | 验证支付密码 | ✅ |
| 11 | GET | `/users/pay-password/status` | 查询支付密码状态 | ✅ |
| 12 | GET | `/users/roles` | 获取用户角色 | ✅ |
| 13 | GET | `/users/list` | 管理员-用户列表 | ✅(Admin) |
| 14 | PUT | `/users/status/{userId}` | 管理员-切换用户状态 | ✅(Admin) |
| 15 | PUT | `/users/roles/{userId}` | 管理员-分配角色 | ✅(Admin) |

### 2.2 bank-service（银行服务）— 9 个接口

| # | 方法 | 路径 | 功能 | 认证 |
|---|------|------|------|:---:|
| 1 | POST | `/accounts` | 创建账户 | ✅ |
| 2 | GET | `/accounts` | 获取用户所有账户 | ✅ |
| 3 | GET | `/accounts/{id}` | 获取账户详情 | ✅ |
| 4 | GET | `/accounts/dashboard/stats` | 仪表盘统计 | ✅ |
| 5 | PUT | `/accounts/{id}` | 更新账户信息 | ✅ |
| 6 | PUT | `/accounts/{id}/status` | 冻结/解冻账户 | ✅ |
| 7 | GET | `/transactions` | 查询交易记录（支持多条件筛选+分页） | ✅ |
| 8 | GET | `/transfers` | 查询转账记录（支持分页） | ✅ |
| 9 | POST | `/transfers` | 发起转账 | ✅ |

### 2.3 chatbot-service（聊天机器人服务）— 7 个接口

| # | 方法 | 路径 | 功能 | 认证 |
|---|------|------|------|:---:|
| 1 | GET | `/chat/session` | 获取会话列表 | ✅ |
| 2 | POST | `/chat/session` | 创建会话 | ✅ |
| 3 | DELETE | `/chat/session/{sessionId}` | 删除会话 | ✅ |
| 4 | PUT | `/chat/session/{sessionId}` | 重命名会话 | ✅ |
| 5 | GET | `/chat/message/{sessionId}` | 获取聊天记录 | ✅ |
| 6 | POST | `/chat` | 发送消息（AI 流式回复） | ✅ |
| 7 | GET | `/chat/stats` | 对话统计（管理员） | ✅(Admin) |
| 8 | POST | `/chat/knowledge` | 上传知识库文档 | ✅(Admin) |
| 9 | DELETE | `/chat/knowledge` | 清空知识库 | ✅(Admin) |

---

## 三、压测接口优先级

### 🔴 P0 - 必须压测（核心高频 + 高风险）

| 接口 | 压测原因 | 关注指标 | 建议并发 |
|------|----------|----------|----------|
| `POST /users/login` | 系统入口，高并发场景（早高峰）；涉及密码校验、JWT 生成、IP 校验 | QPS、RT、错误率 | 500+ |
| `POST /transfers` | 💰 **最核心资金操作**，涉及事务、余额校验、日限额检查、跨账户操作；并发可能导致超额/数据不一致 | 数据一致性、死锁率、RT | 100+ |
| `POST /chat` | AI 流式回复，调用外部 AI API（长连接），系统中**响应最慢**的接口；可能打满线程池 | 长连接数、超时率、内存占用 | 50-100 |
| `GET /accounts/dashboard/stats` | 仪表盘首页接口，每个用户登录后必调；涉及多表聚合统计查询 | QPS、慢查询 | 200+ |

### 🟡 P1 - 建议压测（高频查询接口）

| 接口 | 压测原因 | 关注指标 | 建议并发 |
|------|----------|----------|----------|
| `GET /accounts` | 用户每次进入账户页都会调用，高频读接口 | QPS、RT | 200+ |
| `GET /transactions` | 交易记录查询，支持多条件筛选+分页，SQL 可能较复杂 | 慢查询、RT | 200+ |
| `GET /transfers` | 转账记录查询，涉及多账户 ID 联合查询 | RT、QPS | 200+ |
| `GET /users/me` | 几乎每个页面都会调用（获取用户信息），极高频 | QPS、RT | 500+ |
| `POST /users/refresh` | Token 刷新，前端定期自动调用 | QPS | 300+ |
| `GET /chat/session` | 进入聊天页面必调 | QPS | 200+ |
| `GET /chat/message/{sessionId}` | 切换会话时加载历史消息 | RT | 200+ |

### 🟢 P2 - 可选压测（低频但需验证）

| 接口 | 压测原因 | 建议并发 |
|------|----------|----------|
| `POST /users/register` | 注册涉及验证码校验、唯一性检查 | 100+ |
| `POST /users/code` | 发送验证码，可能涉及邮件服务 | 50+ |
| `POST /accounts` | 创建账户，验证账号唯一性 | 100+ |
| `PUT /accounts/{id}/status` | 冻结/解冻，需与转账组合测试 | 50+ |
| `POST /chat/knowledge` | 上传知识库文档，大文件+向量化 | 10-20 |

---

## 四、压测通过标准

| 指标 | P0 接口标准 | P1 接口标准 | P2 接口标准 |
|------|------------|------------|------------|
| **平均响应时间 (Avg RT)** | < 200ms | < 500ms | < 1000ms |
| **P99 响应时间** | < 1000ms | < 2000ms | < 5000ms |
| **错误率** | < 0.1% | < 0.5% | < 1% |
| **QPS** | 根据业务预估 | 根据业务预估 | - |
| **数据一致性**（转账） | 100% 正确 | - | - |

> ⚠️ `POST /chat`（AI 聊天）因依赖外部 AI API，RT 标准可放宽至 30s，但需确保无超时断连。
