# Bank Service Chatbot

一个基于微服务架构的银行服务聊天机器人系统，提供账户管理、交易处理、用户管理和智能聊天功能。

## 项目概述

本项目是一个完整的银行服务解决方案，采用现代化的微服务架构设计，包含以下核心组件：

- **前端**: Vue.js 3 + Vue Router + Vuex
- **后端**: Spring Boot + Spring Cloud + Spring Cloud Alibaba
- **微服务架构**:
  - API Gateway: 统一网关服务
  - Bank Service: 银行核心业务服务
  - User Service: 用户管理服务
  - Chatbot Service: 智能聊天机器人服务
  - Common: 公共模块
  - API: 客户端接口定义
- **基础设施**: Nacos (服务注册与配置中心), MySQL, Redis

## 技术栈

### 后端技术
- **Java 21**: 主要编程语言
- **Spring Boot 3.4.4**: 应用框架
- **Spring Cloud 2024.0.0**: 微服务框架
- **Spring Cloud Alibaba 2022.0.0.0**: 阿里巴巴微服务组件
- **Spring AI 1.1.2**: AI集成框架
- **MyBatis Plus 3.5.15**: ORM框架
- **MySQL 8.0.28**: 数据库
- **Redis**: 缓存
- **JWT**: 身份认证
- **Nacos**: 服务注册与配置中心

### 前端技术
- **Vue.js 3.2.13**: 前端框架
- **Vue Router 4.0.3**: 路由管理
- **Vuex 4.0.0**: 状态管理
- **Axios 1.13.5**: HTTP客户端
- **Marked 17.0.4**: Markdown解析

### 开发工具
- **Maven**: Java项目构建
- **npm**: 前端包管理
- **Docker**: 容器化部署

## 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                   前端 (Vue.js)                         │
│                     端口: 80                           │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                 API Gateway (Spring Cloud Gateway)      │
│                      端口: 8080                         │
└───────┬──────────────┬──────────────┬───────────────────┘
        │              │              │
┌───────▼────┐ ┌──────▼─────┐ ┌──────▼──────┐
│ Bank       │ │ User       │ │ Chatbot    │
│ Service    │ │ Service    │ │ Service    │
│ 端口: 8082 │ │ 端口: 8081 │ │ 端口: 8083 │
└────────────┘ └────────────┘ └─────────────┘
        │              │              │
┌───────▼──────────────▼──────────────▼─────┐
│         Nacos (服务注册与配置中心)         │
│               端口: 8848                  │
└───────┬───────────────────────────────────┘
        │
┌───────▼────┐ ┌─────────────┐
│   MySQL    │ │    Redis    │
│  端口:3306 │ │  端口:6379  │
└────────────┘ └─────────────┘
```

## 快速开始

### 环境要求

1. **Java 21**: 需要安装 JDK 21
2. **Node.js 16+**: 需要安装 Node.js
3. **MySQL 8.0+**: 数据库
4. **Redis**: 缓存服务
5. **Maven 3.6+**: Java构建工具
6. **npm**: 前端包管理器
7. **Docker** (可选): 容器化部署

### 数据库初始化

1. 启动 MySQL 服务
2. 执行数据库脚本（按顺序）：
```bash
# 进入数据库目录
cd database

# 创建数据库和表结构
mysql -u root -p < user.sql
mysql -u root -p < bank.sql
mysql -u root -p < chat.sql
```

### 服务启动步骤

#### 1. 启动基础设施服务

使用 Docker 快速启动基础设施：

```bash
# 进入 Docker 目录
cd docker

# 启动 MySQL
cd mysql && ./start.sh

# 启动 Redis
cd ../redis && ./start.sh

# 启动 Nacos
cd ../nacos && ./start.sh
```

#### 2. 启动后端微服务

```bash
# 进入后端目录
cd backend

# 方式一：使用 Maven 构建并启动所有服务
mvn clean install
```

**启动各个微服务（需要按顺序启动）：**

1. **启动 Nacos 配置中心** (确保已经启动)
2. **启动 Common 模块** (如果需要)
3. **启动各个微服务**:

```bash
# 在 separate terminals 中分别启动：

# 启动 User Service
cd user-service
mvn spring-boot:run

# 启动 Bank Service
cd ../bank-service
mvn spring-boot:run

# 启动 Chatbot Service
cd ../chatbot-service
mvn spring-boot:run

# 启动 Gateway
cd ../gateway
mvn spring-boot:run
```

或者使用 IDE 直接运行各个服务的 `Application.java` 文件。

#### 3. 启动前端应用

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run serve
```

前端将在 `http://localhost:80` 启动。

### 服务端口说明

- **前端**: `http://localhost:80`
- **API Gateway**: `http://localhost:8080`
- **User Service**: `http://localhost:8081`
- **Bank Service**: `http://localhost:8082`
- **Chatbot Service**: `http://localhost:8083`
- **Nacos**: `http://localhost:8848` (用户名: nacos, 密码: maretu)

## 项目结构

```
bank-service-chatbot/
├── README.md                    # 项目说明文档
├── backend/                     # 后端微服务
│   ├── pom.xml                 # 父POM文件
│   ├── api/                    # API接口定义
│   ├── bank-service/           # 银行核心服务
│   ├── chatbot-service/        # 聊天机器人服务
│   ├── user-service/           # 用户管理服务
│   ├── gateway/                # API网关
│   ├── common/                 # 公共模块
│   └── target/                 # 构建输出
├── frontend/                   # 前端Vue应用
│   ├── package.json           # 前端依赖配置
│   ├── public/                # 静态资源
│   ├── src/                   # 源代码
│   │   ├── api/              # API调用
│   │   ├── assets/           # 静态资源
│   │   ├── components/       # 公共组件
│   │   ├── router/           # 路由配置
│   │   ├── store/            # 状态管理
│   │   ├── utils/            # 工具函数
│   │   └── views/            # 页面视图
│   └── vue.config.js         # Vue配置
├── database/                  # 数据库脚本
│   ├── bank.sql              # 银行服务数据库
│   ├── user.sql              # 用户服务数据库
│   └── chat.sql              # 聊天服务数据库
├── docker/                   # Docker配置
│   ├── mysql/               # MySQL容器配置
│   ├── nacos/               # Nacos容器配置
│   ├── redis/               # Redis容器配置
│   └── nacos_config.zip     # Nacos配置导出
└── .gitignore               # Git忽略配置
```

## 配置文件说明

### 后端配置
各个微服务的配置在 `src/main/resources/application.yaml` 中，主要配置项：
- 服务端口
- Nacos 服务注册地址
- 数据库连接
- Redis 配置

### 前端配置
- `vue.config.js`: 开发服务器配置（端口80）
- `src/http.js`: API请求配置
- `src/api/api.js`: API接口定义

## API文档

服务启动后，可以通过以下地址访问API文档：

- **Swagger UI**: `http://localhost:{port}/swagger-ui.html` (每个微服务)
- **Nacos控制台**: `http://localhost:8848/nacos` (用户名: nacos, 密码: maretu)

## 功能模块

### 用户模块
- 用户注册与登录
- JWT身份认证
- 个人信息管理
- 密码重置

### 银行模块
- 账户管理（查询、创建）
- 交易记录查询
- 转账功能
- 账户统计仪表板

### 聊天机器人模块
- 智能对话
- 银行业务咨询
- 交易查询助手
- 账户问题解答

### 管理后台
- 用户管理
- 聊天统计
- 知识库管理

## 开发指南

### 后端开发

1. **添加新的微服务**:
   - 在 `backend/pom.xml` 的 modules 中添加新模块
   - 复制现有服务结构
   - 更新端口配置

2. **数据库操作**:
   - 使用 MyBatis Plus 进行数据库操作
   - 在 `src/main/resources/mapper/` 中添加XML映射文件
   - 在 `pojo` 包中定义实体类

3. **API开发**:
   - 在 `controller` 包中定义Restful接口
   - 在 `service` 包中实现业务逻辑
   - 在 `dto` 包中定义数据传输对象

### 前端开发

1. **添加新页面**:
   - 在 `src/views/` 中创建新的Vue组件
   - 在 `src/router/index.js` 中配置路由
   - 在 `src/api/api.js` 中添加API调用

2. **状态管理**:
   - 使用 Vuex 进行全局状态管理
   - 在 `src/store/` 中定义状态模块

3. **样式开发**:
   - 使用 `src/assets/styles/` 中的全局样式
   - 支持CSS变量配置

## 部署说明

### 传统部署

1. **后端部署**:
```bash
cd backend
mvn clean package
# 生成的jar包在 target/ 目录
java -jar target/bank-service-1.0.0.jar
```

2. **前端部署**:
```bash
cd frontend
npm run build
# 生成的静态文件在 dist/ 目录
```

### Docker部署

项目提供了Docker配置，可以使用以下命令构建镜像：

```bash
# 构建后端服务镜像
cd backend
docker build -t bank-service .

# 构建前端镜像
cd ../frontend
docker build -t frontend .
```

## 常见问题

### 1. 服务无法注册到Nacos
- 检查Nacos服务是否正常运行
- 确认 `application.yaml` 中的Nacos配置正确
- 检查网络连接

### 2. 数据库连接失败
- 确认MySQL服务已启动
- 检查数据库用户名和密码
- 确认数据库已创建

### 3. 前端无法访问后端API
- 检查API Gateway是否正常运行
- 确认CORS配置正确
- 检查网络代理设置

### 4. 端口冲突
- 修改 `application.yaml` 或 `vue.config.js` 中的端口配置
- 停止占用端口的其他进程

## 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目仅供学习交流使用。

## 联系方式

如有问题或建议，请通过GitHub Issues提交。

---

**最后更新**: 2025年4月
