# 银行服务聊天机器人项目

## 环境要求

- Java 21
- Node.js 24+ 和 npm 11+
- Docker
- Maven 3.8+

## 启动步骤

### 1. 启动基础设施

```bash
# 启动 MySQL
cd docker/mysql
./start.sh

# 启动 Redis
cd ../redis
./start.sh

# 启动 Nacos
cd ../nacos
./start.sh
```

### 2. 配置 Nacos

1. 浏览器访问 Nacos 控制台：`http://localhost:8848/nacos`
2. 设置默认账号密码（用户名/密码：`nacos/maretu`）
3. 用刚刚设置的账号密码登录进nacos
4. 上传配置文件：进入配置管理页面，上传 `/docker/nacos_config.zip` 文件导入配置

### 3. 配置后端服务

在启动后端服务前，需要修改各服务的配置文件 `application.yaml`，将配置信息改为 Docker 容器网络地址：

**需要修改的配置项：**

| 服务 | 配置文件路径 | 需要修改的配置 |
|------|-------------|---------------|
| gateway | `backend/gateway/src/main/resources/application.yaml` | Nacos 地址改为 `host.docker.internal:8848` |
| bank-service | `backend/bank-service/src/main/resources/application.yaml` | Nacos 地址改为 `host.docker.internal:8848`，MySQL host、Redis host 改为 `host.docker.internal` |
| user-service | `backend/user-service/src/main/resources/application.yaml` | Nacos 地址改为 `host.docker.internal:8848`，MySQL host、Redis host 改为 `host.docker.internal` |
| chatbot-service | `backend/chatbot-service/src/main/resources/application.yaml` | Nacos 地址改为 `host.docker.internal:8848`，MySQL host、Redis host 改为 `host.docker.internal` |

> **注意**：`host.docker.internal` 是 Docker 提供的主机网络访问地址，用于从容器内访问宿主机上的服务。

### 4. 启动后端服务

```bash
# 方式一：使用 IDEA 分别启动各服务模块
# 依次运行以下服务的主启动类：
# - GatewayApplication
# - BankServiceApplication
# - UserServiceApplication
# - ChatbotServiceApplication

# 方式二：使用 Maven 命令启动
cd backend/gateway
mvn spring-boot:run

# 新开终端，启动 bank-service
cd backend/bank-service
mvn spring-boot:run

# 新开终端，启动 user-service
cd backend/user-service
mvn spring-boot:run

# 新开终端，启动 chatbot-service
cd backend/chatbot-service
mvn spring-boot:run
```

### 5. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run serve
```

前端应用将在 `http://localhost:80` 启动。

## 默认配置

- **Nacos 地址**: ipv6.maretu.top:8848
- **MySQL 密码**: maretu
- **数据库端口**: 3306
