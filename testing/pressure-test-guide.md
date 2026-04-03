# Bank-Service-Chatbot 压测流程指南

## 一、压测前准备

### 1.1 环境准备

```bash
# 1. 确保所有服务已启动
# - Nacos 注册中心
# - MySQL 数据库
# - Redis 缓存
# - Gateway 网关
# - user-service
# - bank-service
# - chatbot-service

# 2. 安装 JMeter（需要 JDK 8+）
# 下载地址：https://jmeter.apache.org/download_jmeter.cgi
# 解压后启动：
bin/jmeter.sh        # Linux/macOS
bin/jmeter.bat       # Windows
```

### 1.2 测试数据准备

```sql
-- 1. 批量创建测试用户（建议 1000 个）
-- 可以写脚本调用 /users/register 接口批量注册
-- 或者直接往数据库插入测试数据

-- 2. 为每个测试用户创建 2-3 个银行账户，并设置初始余额

-- 3. 为每个账户生成历史交易记录（建议每账户 1000+ 条）
-- 用于测试分页查询性能

-- 4. 为部分用户创建聊天会话和历史消息
```

### 1.3 Token 预生成

```
压测需要认证的接口前，必须先批量获取 Token：

1. 在 JMeter 中创建一个 "setUp Thread Group"
2. 用所有测试用户调用 POST /users/login
3. 用 JSON Extractor 提取返回的 Token
4. 存入 CSV 文件或 JMeter 变量，供后续线程组使用
```

### 1.4 JMeter 插件安装（推荐）

```
推荐安装以下插件（通过 JMeter Plugins Manager）：
- jpgc-graphs-basic：基础图表（响应时间、TPS 等）
- jpgc-graphs-additional：额外图表（连接时间、字节吞吐量等）
- jpgc-casutg：并发线程组（Concurrency Thread Group）
- jpgc-tst：吞吐量整形定时器
```

---

## 二、JMeter 测试计划结构

```
📁 Test Plan: Bank-Service-Chatbot 压测
│
├── 📄 User Defined Variables（全局变量）
│   ├── BASE_URL = localhost
│   ├── PORT = 8080
│   └── PROTOCOL = http
│
├── 📄 CSV Data Set Config（测试用户数据）
│   └── users.csv (user_id, email, password, token)
│
├── 📁 setUp Thread Group（预热：批量登录获取 Token）
│   ├── POST /users/login
│   ├── JSON Extractor → token
│   └── BeanShell PostProcessor → 写入 CSV
│
├── 📁 Thread Group 1: P0 - 登录压测
│   ├── POST /users/login
│   ├── Response Assertion (code=200)
│   └── Summary Report
│
├── 📁 Thread Group 2: P0 - 转账并发安全
│   ├── HTTP Header Manager (Authorization: Bearer ${token})
│   ├── POST /users/pay-password/verify
│   ├── POST /transfers
│   ├── Response Assertion
│   └── Summary Report
│
├── 📁 Thread Group 3: P0 - AI 聊天压测
│   ├── HTTP Header Manager
│   ├── POST /chat/session
│   ├── JSON Extractor → sessionId
│   ├── POST /chat（流式）
│   └── Summary Report
│
├── 📁 Thread Group 4: P0 - 仪表盘统计
│   ├── HTTP Header Manager
│   ├── GET /accounts/dashboard/stats
│   └── Summary Report
│
├── 📁 Thread Group 5: P1 - 高频查询混合
│   ├── HTTP Header Manager
│   ├── GET /accounts
│   ├── GET /transactions?page=1&size=10
│   ├── GET /transfers?page=1&size=10
│   ├── GET /users/me
│   └── Aggregate Report
│
├── 📁 Thread Group 6: 混合业务场景
│   ├── HTTP Header Manager
│   ├── Throughput Controller (40%) → 查询操作
│   ├── Throughput Controller (20%) → 登录/刷新
│   ├── Throughput Controller (20%) → AI 聊天
│   ├── Throughput Controller (15%) → 仪表盘
│   ├── Throughput Controller (5%)  → 转账
│   └── Aggregate Report
│
├── 📊 View Results Tree（调试用，正式压测关闭）
├── 📊 Aggregate Report（聚合报告）
├── 📊 Response Time Graph（响应时间图）
└── 📊 Transactions per Second（TPS 图）
```

---

## 三、各场景详细配置

### 3.1 场景一：登录高峰压测

**目标**：验证登录接口在高并发下的性能和稳定性

```
Thread Group 配置：
├── Number of Threads: 500
├── Ramp-Up Period: 30s（30 秒内逐步启动 500 线程）
└── Loop Count: 10（每个线程循环 10 次，共 5000 次请求）

HTTP Request:
├── Method: POST
├── Path: /users/login
├── Headers:
│   └── Content-Type: application/json
└── Body:
    {
      "email": "${email}",
      "password": "${password}"
    }

CSV Data Set Config:
├── Filename: users.csv
├── Variable Names: user_id,email,password
├── Recycle on EOF: True
└── Sharing mode: All threads

断言：
├── Response Code = 200
└── Response Body contains "success" 或 包含 token 字段

关注指标：
├── 平均 RT < 200ms
├── P99 RT < 1000ms
├── 错误率 < 0.1%
└── QPS > 200
```

### 3.2 场景二：转账并发安全压测

**目标**：验证并发转账时数据一致性，余额不会变负，不会超日限额

```
Thread Group 配置：
├── Number of Threads: 100
├── Ramp-Up Period: 5s（尽量同时发起）
└── Loop Count: 1（每个线程只转一次）

前置条件：
├── 准备一个账户，余额 10000 元
├── 每笔转账金额 200 元
└── 预期：最多成功 50 笔，其余应返回余额不足

HTTP Request:
├── Method: POST
├── Path: /transfers
├── Headers:
│   ├── Content-Type: application/json
│   └── user-info: ${user_info_json}
└── Body:
    {
      "fromAccountId": ${from_account_id},
      "toAccountNumber": "${to_account_number}",
      "toAccountName": "测试收款人",
      "toBankName": "测试银行",
      "amount": 200,
      "remark": "压测转账",
      "payPassword": "${pay_password}"
    }

验证方式：
├── 压测结束后查询账户余额，验证 = 10000 - (成功笔数 × 200)
├── 检查是否有死锁日志
├── 检查交易记录数 = 成功笔数
└── 检查日限额是否正确扣减
```

### 3.3 场景三：AI 聊天长连接压测

**目标**：验证 AI 流式响应在并发下的稳定性

```
Thread Group 配置：
├── Number of Threads: 50
├── Ramp-Up Period: 10s
└── Loop Count: 5（每个用户发 5 条消息）

步骤 1 - 创建会话：
├── Method: POST
├── Path: /chat/session
└── JSON Extractor: sessionId = $.data.sessionId

步骤 2 - 发送消息：
├── Method: POST
├── Path: /chat
├── Headers:
│   ├── Content-Type: application/json
│   └── Accept: text/html
└── Body:
    {
      "sessionId": "${sessionId}",
      "content": "请问如何查询我的账户余额？"
    }

⚠️ 注意事项：
├── POST /chat 返回的是 SSE 流式响应
├── JMeter 默认会等待响应完成，需设置较长超时（60s+）
├── 建议使用 "Response timeout" = 60000ms
└── 关注线程池使用率和内存变化

关注指标：
├── 超时率 < 5%
├── 错误率 < 1%
├── 服务内存无持续增长（无内存泄漏）
└── 线程池未耗尽
```

### 3.4 场景四：高频查询混合压测

**目标**：验证日常高频查询接口的性能

```
Thread Group 配置：
├── Number of Threads: 200
├── Ramp-Up Period: 20s
└── Loop Count: 50

请求列表（使用 Random Controller 随机执行）：
├── GET /users/me
├── GET /accounts
├── GET /accounts/${accountId}
├── GET /accounts/dashboard/stats
├── GET /transactions?page=1&size=10&allAccounts=true
├── GET /transfers?page=1&size=10
├── GET /chat/session
└── GET /chat/message/${sessionId}

关注指标：
├── 各接口平均 RT < 500ms
├── P99 RT < 2000ms
├── 总 QPS > 500
└── 错误率 < 0.5%
```

### 3.5 场景五：混合业务场景（最接近真实）

**目标**：模拟真实用户行为比例，验证系统整体承载能力

```
Thread Group 配置：
├── Number of Threads: 300
├── Ramp-Up Period: 60s
├── Loop Count: ∞（持续运行）
└── Duration: 600s（持续 10 分钟）

业务比例（使用 Throughput Controller）：
├── 40% → 查询操作（账户、交易、转账记录）
├── 20% → 登录 / 刷新 Token
├── 20% → AI 聊天
├── 15% → 仪表盘统计
└── 5%  → 转账操作

关注指标：
├── 整体 QPS
├── 各接口 P99 延迟
├── 错误率 < 0.5%
├── 各服务 CPU 使用率 < 80%
├── 各服务内存使用率 < 80%
├── MySQL 连接池使用率 < 80%
├── Redis 连接数和内存
└── Gateway 转发延迟
```

---

## 四、压测执行流程

### 4.1 执行步骤

```
步骤 1：环境检查
  ├── 确认所有服务正常运行
  ├── 确认数据库连接正常
  ├── 确认 Redis 连接正常
  └── 确认 Nacos 服务注册正常

步骤 2：基准测试（单线程）
  ├── 每个接口单线程跑一遍，确认功能正常
  ├── 记录基准响应时间
  └── 确认断言全部通过

步骤 3：逐步加压
  ├── 第一轮：50 并发，持续 2 分钟
  ├── 第二轮：100 并发，持续 5 分钟
  ├── 第三轮：200 并发，持续 5 分钟
  ├── 第四轮：300 并发，持续 10 分钟
  └── 第五轮：500 并发，持续 10 分钟（极限压测）

步骤 4：专项测试
  ├── 转账并发安全测试
  ├── AI 聊天长连接测试
  └── 登录高峰测试

步骤 5：浸泡测试
  ├── 200 并发，持续 1 小时
  └── 观察内存泄漏、连接泄漏等问题
```

### 4.2 命令行执行（正式压测必须用命令行）

```bash
# ⚠️ GUI 模式仅用于创建和调试！正式压测必须用命令行模式！

# 执行压测并生成 HTML 报告
jmeter -n \
  -t bank-service-pressure-test.jmx \
  -l result.jtl \
  -e -o ./report \
  -Jthreads=300 \
  -Jrampup=60 \
  -Jduration=600

# 参数说明：
# -n          非 GUI 模式
# -t          测试计划文件
# -l          结果输出文件
# -e -o       生成 HTML 报告到指定目录
# -J          传递自定义参数（可在 JMX 中用 ${__P(threads)} 引用）
```

---

## 五、监控与观测

### 5.1 需要监控的指标

```
┌─────────────────────────────────────────────────────────┐
│                    监控指标清单                           │
├──────────────┬──────────────────────────────────────────┤
│   层级       │   监控项                                  │
├──────────────┼──────────────────────────────────────────┤
│   Gateway    │ 请求转发延迟、连接数、错误率               │
│   应用服务    │ CPU、内存、线程数、GC 频率和耗时           │
│   MySQL      │ 连接池使用率、慢查询数、QPS、锁等待        │
│   Redis      │ 连接数、内存使用、命中率、延迟             │
│   JVM        │ 堆内存、GC 次数、线程池队列长度            │
│   系统       │ CPU、内存、磁盘 IO、网络带宽              │
└──────────────┴──────────────────────────────────────────┘
```

### 5.2 推荐监控工具

| 工具 | 用途 |
|------|------|
| **Prometheus + Grafana** | 应用指标监控和可视化 |
| **Arthas** | Java 应用在线诊断（线程、内存、方法耗时） |
| **MySQL slow_query_log** | 慢查询分析 |
| **Redis INFO** | Redis 运行状态 |
| **top / htop** | 系统资源监控 |
| **JVisualVM / JConsole** | JVM 监控 |

---

## 六、压测报告模板

```markdown
# 压测报告

## 基本信息
- 测试日期：YYYY-MM-DD
- 测试环境：（生产/预发/测试）
- 测试工具：JMeter x.x
- 测试人员：xxx

## 测试场景
| 场景 | 并发数 | 持续时间 | 循环次数 |
|------|--------|----------|----------|
| xxx  | xxx    | xxx      | xxx      |

## 测试结果

### 汇总数据
| 接口 | 请求数 | 平均RT | P90 | P95 | P99 | 最大RT | 错误率 | QPS |
|------|--------|--------|-----|-----|-----|--------|--------|-----|
| xxx  | xxx    | xxx    | xxx | xxx | xxx | xxx    | xxx    | xxx |

### 资源使用情况
| 服务 | CPU峰值 | 内存峰值 | 线程峰值 | DB连接峰值 |
|------|---------|----------|----------|-----------|
| xxx  | xxx     | xxx      | xxx      | xxx       |

### 发现的问题
1. 问题描述 + 影响 + 建议

## 结论
- [ ] 通过
- [ ] 不通过（原因：xxx）
```

---

## 七、常见问题与注意事项

### 7.1 压测前注意

| 注意事项 | 说明 |
|----------|------|
| **不要压测生产环境** | 使用独立的压测环境，数据库和 Redis 与生产隔离 |
| **Gateway 可能是瓶颈** | 所有请求都经过 Gateway（8080），需关注其性能 |
| **预先生成 Token** | 除 `/users/**` 外的接口都需要 JWT 认证 |
| **AI 接口特殊处理** | `POST /chat` 调用外部 AI API，响应时间不可控，建议单独压测 |
| **关闭 View Results Tree** | 正式压测时关闭此监听器，否则会严重影响 JMeter 性能 |

### 7.2 压测中注意

| 注意事项 | 说明 |
|----------|------|
| **观察错误日志** | 实时 `tail -f` 各服务日志，关注异常堆栈 |
| **监控数据库** | 关注慢查询、死锁、连接池耗尽 |
| **监控内存** | 关注 JVM 堆内存和 GC，防止 OOM |
| **逐步加压** | 不要一上来就拉满并发，逐步增加观察拐点 |

### 7.3 压测后注意

| 注意事项 | 说明 |
|----------|------|
| **清理测试数据** | 压测产生的账户、交易、转账记录需要清理 |
| **保存压测报告** | 保存 JMeter 的 HTML 报告和 jtl 原始数据 |
| **对比分析** | 与上次压测结果对比，观察性能变化趋势 |
| **验证数据一致性** | 特别是转账场景，检查所有账户余额是否正确 |
