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

# 查看 MySQL 启动日志（等待 MySQL 启动成功）
docker logs -f mysql
```

按 `Ctrl+C` 退出日志查看。

```bash
# 启动 Redis
cd ../redis
./start.sh

# 启动 Nacos（Nacos 依赖 MySQL，需确保 MySQL 已启动成功）
cd ../nacos
./start.sh
```

> **注意**：
> 1. Nacos 使用 MySQL 作为持久化存储，必须在 MySQL 启动成功后才能启动 Nacos。
> 2. 建议在启动 Nacos 前，先使用数据库连接工具（如 Navicat、DataGrip 等）测试能否连接 MySQL：
>    - 主机：`localhost` 或服务器 IP
>    - 端口：`3306`
>    - 用户名：`root`
>    - 密码：`maretu`
> 3. 确认 MySQL 连接成功后，再继续启动 Nacos。

### 2. 配置 Nacos

1. 浏览器访问 Nacos 控制台：`http://localhost:8848/nacos`
2. 设置默认账号密码（用户名/密码：`nacos/maretu`）
3. 用刚刚设置的账号密码登录进 nacos
4. 上传配置文件：进入配置管理页面，上传 `/docker/nacos_config.zip` 文件导入配置

### 3. 配置后端服务

通过设置环境变量 `DOCKER_SERVER_ADDR` 来配置服务地址：

**Windows (PowerShell):**
```powershell
$env:DOCKER_SERVER_ADDR="192.168.1.1"
```

**Windows (CMD):**
```cmd
set DOCKER_SERVER_ADDR=192.168.1.1
```

**macOS / Linux:**
```bash
export DOCKER_SERVER_ADDR=192.168.1.1
```

> **注意**：请将 `192.168.1.1` 替换为你实际的 docker 服务器 IP 地址（如本机局域网 IP）。如果不设置环境变量，默认使用 `ipv6.maretu.top` 作为服务器地址（不一定有效）。

### 3.5. 配置 Chatbot 服务（可选）

修改 `backend/chatbot-service/src/main/resources/application.yaml` 文件，将 OpenAI 相关配置改为自己的 API 密钥：

```yaml
spring:
  ai:
    openai:
      api-key: "你的 API 密钥"  # 替换为自己的 API 密钥
      base-url: "你的 API 基础地址"  # 替换为自己的 API 基础地址
      chat:
        options:
          model: "deepseek-ai/DeepSeek-V3.2"  # 可根据需要修改模型
```

> **注意**：默认配置的 API 密钥和 base-url 可能有调用次数限制、速率限制或无法连接的问题，可以替换为自己的 API 密钥和 API 基础地址。

### 4. 启动后端服务

首先编译后端项目：

```bash
cd backend
mvn clean install
```

然后启动各服务：

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

### 5. 配置前端

修改 `frontend/src/http.js` 文件，将 `baseURL` 配置为后端 Gateway 的地址：

```javascript
const instance = axios.create({
    baseURL: 'http://192.168.1.1:8080',  // 修改为实际的服务器 IP 地址
    // ...
})
```

> **注意**：Gateway 默认端口为 `8080`，请将 `192.168.1.1` 替换为实际的服务器 IP 地址。

### 6. 启动前端

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
