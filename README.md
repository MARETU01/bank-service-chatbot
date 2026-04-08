# Bank Service Chatbot - 启动说明

## 环境要求

- **JDK 21**
- **Node.js 24+**
- **Maven 3.8+**
- **Docker** (用于启动基础设施)

## 启动步骤

### 1. 启动基础设施（MySQL、Redis、Nacos）

```bash
cd docker/mysql && ./start.sh
cd ../redis && ./start.sh
cd ../nacos && ./start.sh
```

### 2. 初始化数据库

```bash
cd database
mysql -u root -p < user.sql
mysql -u root -p < bank.sql
mysql -u root -p < chat.sql
```

### 3. 启动后端微服务

打开 4 个终端窗口，分别启动：

```bash
# 终端 1 - User Service
cd backend/user-service
mvn spring-boot:run

# 终端 2 - Bank Service
cd backend/bank-service
mvn spring-boot:run

# 终端 3 - Chatbot Service
cd backend/chatbot-service
mvn spring-boot:run

# 终端 4 - Gateway
cd backend/gateway
mvn spring-boot:run
```

或使用 IDE 直接运行各服务的 `Application.java` 文件。

### 4. 启动前端

```bash
cd frontend
npm install
npm run serve
```

## 访问地址

- **前端**: http://localhost:80
- **Nacos 控制台**: http://localhost:8848/nacos (用户名：nacos, 密码：maretu)

## 服务端口

| 服务 | 端口 |
|------|------|
| Gateway | 8080 |
| User Service | 8081 |
| Bank Service | 8082 |
| Chatbot Service | 8083 |
| Nacos | 8848 |
