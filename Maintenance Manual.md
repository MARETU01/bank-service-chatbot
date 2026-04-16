# Bank Service Chatbot System - Maintenance Manual

**Version:** 1.0  
**Last Updated:** April 2026  
**Applicable Systems:** Windows 11 / macOS / Linux

---

## Table of Contents

1. [System Installation Instructions](#1-system-installation-instructions)
2. [System Compilation/Build Instructions](#2-system-compilationbuild-instructions)
3. [Hardware/Software Dependencies](#3-hardwaresoftware-dependencies)
4. [System File Organization](#4-system-file-organization)
5. [Space and Memory Requirements](#5-space-and-memory-requirements)
6. [Source Code File List](#6-source-code-file-list)
7. [Key Constants](#7-key-constants)
8. [Main Classes, Methods, and Data Structures](#8-main-classes-methods-and-data-structures)
9. [File Path Names](#9-file-path-names)
10. [Future Improvement Directions](#10-future-improvement-directions)
11. [Error Reporting](#11-error-reporting)

---

## 1. System Installation Instructions

### 1.1 Prerequisites

Before installing the system, ensure the following software is installed:

| Software | Version | Download Link |
|----------|---------|---------------|
| Java JDK | 21 | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) |
| Node.js | 24+ | [Node.js](https://nodejs.org/) |
| Docker | Latest | [Docker Desktop](https://www.docker.com/products/docker-desktop/) |
| Maven | 3.8+ | [Apache Maven](https://maven.apache.org/download.cgi) |

### 1.2 Installation Steps

#### Step 1: Clone the Repository

```bash
git clone https://github.com/MARETU01/bank-service-chatbot.git
cd bank-service-chatbot
```

#### Step 2: Start Infrastructure Services

```bash
# Start MySQL
cd docker/mysql
./start.sh

# Check MySQL is running
docker logs -f mysql
# Press Ctrl+C to exit

# Start Redis
cd ../redis
./start.sh

# Start Nacos
cd ../nacos
./start.sh
```

#### Step 3: Configure Nacos

1. Access Nacos console: `http://localhost:8848/nacos`
2. Login with credentials: `nacos` / `maretu`
3. Go to Configuration Management -> Configuration List
4. Click Import and upload `/docker/nacos_config.zip`

#### Step 4: Set Environment Variables

**Windows PowerShell:**
```powershell
$env:DOCKER_SERVER_ADDR="192.168.1.100"
```

**Windows CMD:**
```cmd
set DOCKER_SERVER_ADDR=192.168.1.100
```

**macOS/Linux:**
```bash
export DOCKER_SERVER_ADDR="192.168.1.100"
```

#### Step 5: Configure Frontend

Edit `frontend/src/http.js` and update `baseURL`:

```javascript
const instance = axios.create({
    baseURL: 'http://192.168.1.100:8080',  // Update with your server IP
    // ...
})
```

#### Step 6: Install Frontend Dependencies

```bash
cd frontend
npm install
```

---

## 2. System Compilation/Build Instructions

### 2.1 Backend Build

#### Compile Backend Project

```bash
cd backend
mvn clean install
```

This command will:
- Clean previous build artifacts
- Compile all source code
- Run unit tests
- Package JAR files for each module

#### Expected Output

```
[INFO] BUILD SUCCESS
[INFO] Total time:  XX.XXX s
```

### 2.2 Start Backend Services

Open four separate terminal windows:

**Terminal 1 - Gateway:**
```bash
cd backend/gateway
mvn spring-boot:run
```

**Terminal 2 - Bank Service:**
```bash
cd backend/bank-service
mvn spring-boot:run
```

**Terminal 3 - User Service:**
```bash
cd backend/user-service
mvn spring-boot:run
```

**Terminal 4 - Chatbot Service:**
```bash
cd backend/chatbot-service
mvn spring-boot:run
```

### 2.3 Start Frontend

```bash
cd frontend
npm run serve
```

The application will be available at `http://localhost:80`

### 2.4 Build for Production

#### Backend Production Build

```bash
cd backend
mvn clean package
```

JAR files will be generated in each module's `target/` directory.

#### Frontend Production Build

```bash
cd frontend
npm run build
```

Production files will be generated in `frontend/dist/` directory.

---

## 3. Hardware/Software Dependencies

### 3.1 Hardware Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| CPU | 4 cores | 8 cores or higher |
| RAM | 8 GB | 16 GB or higher |
| Storage | 10 GB free space | 20 GB SSD |
| Network | Broadband internet | High-speed internet |

### 3.2 Software Dependencies

#### Runtime Dependencies

| Software | Version | Purpose |
|----------|---------|---------|
| Java Runtime | JDK 21 | Backend services runtime |
| Node.js Runtime | 24+ | Frontend development and runtime |
| Docker Engine | Latest | Container runtime for MySQL, Redis, Nacos |

#### Build Dependencies

| Software | Version | Purpose |
|----------|---------|---------|
| Maven | 3.8+ | Java project build tool |
| npm | 11+ | Node.js package manager |

#### Database Dependencies

| Database | Version | Purpose |
|----------|---------|---------|
| MySQL | 8.0.28 | Primary data storage |
| Redis | Latest | Session cache and token storage |

#### Configuration Management

| Software | Version | Purpose |
|----------|---------|---------|
| Nacos | 2022.x | Service discovery and configuration management |

### 3.3 Backend Parent POM Properties

The parent POM (`backend/pom.xml`) defines the following key versions:

| Property | Version | Description |
|----------|---------|-------------|
| Java | 21 | Source and target compiler version |
| Spring Cloud | 2024.0.0 | Spring Cloud BOM |
| Spring Cloud Alibaba | 2022.0.0.0 | Spring Cloud Alibaba BOM |
| Spring Boot | 3.4.4 | Spring Boot BOM |
| Spring AI | 1.1.2 | Spring AI BOM |
| JWT | 0.12.6 | JSON Web Token library |
| MySQL Connector | 8.0.28 | MySQL JDBC driver |
| MyBatis-Plus | 3.5.15 | ORM framework |
| Lombok | 1.18.34 | Code generation library |

### 3.4 Maven Dependencies by Module

#### Common Module (`common/pom.xml`)

| Dependency | Version | Purpose |
|------------|---------|---------|
| spring-boot-starter-web | 3.4.4 | Web framework (provided scope) |
| jjwt-api | 0.12.6 | JWT API |
| jjwt-impl | 0.12.6 | JWT implementation (runtime) |
| jjwt-jackson | 0.12.6 | JWT Jackson serializer (runtime) |
| mybatis-plus-spring-boot3-starter | 3.5.15 | MyBatis-Plus for Spring Boot 3 |
| mybatis-plus-jsqlparser | 3.5.15 | SQL parser for MyBatis-Plus |

#### API Module (`api/pom.xml`)

| Dependency | Version | Purpose |
|------------|---------|---------|
| common | 1.0.0 | Common module |
| spring-cloud-starter-openfeign | 2024.x | Declarative HTTP client |
| spring-cloud-starter-loadbalancer | 2024.x | Client-side load balancer |
| lombok | 1.18.34 | Code generation |

#### Gateway Module (`gateway/pom.xml`)

| Dependency | Version | Purpose |
|------------|---------|---------|
| common | 1.0.0 | Common module |
| spring-cloud-starter-gateway | 2024.x | API Gateway |
| spring-cloud-starter-alibaba-nacos-discovery | 2022.x | Service discovery via Nacos |
| spring-cloud-starter-alibaba-nacos-config | 2022.x | Configuration management via Nacos |
| spring-cloud-starter-loadbalancer | 2024.x | Client-side load balancer |

#### User Service (`user-service/pom.xml`)

| Dependency | Version | Purpose |
|------------|---------|---------|
| common | 1.0.0 | Common module |
| api | 1.0.0 | API client module |
| spring-boot-starter-web | 3.4.4 | Web framework |
| spring-cloud-starter-alibaba-nacos-discovery | 2022.x | Service discovery |
| spring-cloud-starter-alibaba-nacos-config | 2022.x | Configuration management |
| spring-cloud-starter-openfeign | 2024.x | Declarative HTTP client |
| feign-okhttp | 2024.x | OkHttp for Feign |
| mysql-connector-java | 8.0.28 | MySQL JDBC driver |
| spring-boot-starter-data-redis | 3.4.4 | Redis client |
| spring-session-data-redis | 3.4.4 | Spring Session with Redis |
| commons-pool2 | 2.x | Connection pooling |
| spring-boot-starter-mail | 3.4.4 | Email support |

#### Bank Service (`bank-service/pom.xml`)

| Dependency | Version | Purpose |
|------------|---------|---------|
| common | 1.0.0 | Common module |
| api | 1.0.0 | API client module |
| spring-boot-starter-web | 3.4.4 | Web framework |
| spring-cloud-starter-alibaba-nacos-discovery | 2022.x | Service discovery |
| spring-cloud-starter-alibaba-nacos-config | 2022.x | Configuration management |
| spring-cloud-starter-openfeign | 2024.x | Declarative HTTP client |
| feign-okhttp | 2024.x | OkHttp for Feign |
| mysql-connector-java | 8.0.28 | MySQL JDBC driver |
| spring-boot-starter-data-redis | 3.4.4 | Redis client |
| spring-session-data-redis | 3.4.4 | Spring Session with Redis |
| commons-pool2 | 2.x | Connection pooling |

#### Chatbot Service (`chatbot-service/pom.xml`)

| Dependency | Version | Purpose |
|------------|---------|---------|
| common | 1.0.0 | Common module |
| api | 1.0.0 | API client module |
| spring-boot-starter-web | 3.4.4 | Web framework |
| spring-ai-starter-model-openai | 1.1.2 | OpenAI model support |
| spring-ai-starter-model-ollama | 1.1.2 | Ollama model support |
| spring-boot-starter-webflux | 3.4.4 | Reactive web framework |
| spring-cloud-starter-alibaba-nacos-discovery | 2022.x | Service discovery |
| spring-cloud-starter-alibaba-nacos-config | 2022.x | Configuration management |
| spring-cloud-starter-openfeign | 2024.x | Declarative HTTP client |
| feign-okhttp | 2024.x | OkHttp for Feign |
| mysql-connector-java | 8.0.28 | MySQL JDBC driver |
| spring-boot-starter-data-redis | 3.4.4 | Redis client |
| spring-session-data-redis | 3.4.4 | Spring Session with Redis |
| spring-ai-starter-vector-store-redis | 1.1.2 | Redis vector store for AI |
| spring-ai-pdf-document-reader | 1.1.2 | PDF document reader for AI |
| commons-pool2 | 2.x | Connection pooling |

### 3.5 Frontend npm Dependencies

#### Production Dependencies (`frontend/package.json`)

| Dependency | Version | Purpose |
|------------|---------|---------|
| axios | ^1.13.5 | HTTP client for API requests |
| core-js | ^3.8.3 | JavaScript polyfills |
| marked | ^17.0.4 | Markdown parser for chatbot responses |
| vue | ^3.2.13 | Vue.js 3 framework |
| vue-router | ^4.0.3 | Vue Router for navigation |
| vuex | ^4.0.0 | Vuex state management |

#### Development Dependencies (`frontend/package.json`)

| Dependency | Version | Purpose |
|------------|---------|---------|
| @babel/core | ^7.12.16 | Babel compiler |
| @babel/eslint-parser | ^7.12.16 | ESLint parser for Babel |
| @vue/cli-plugin-babel | ~5.0.0 | Vue CLI Babel plugin |
| @vue/cli-plugin-eslint | ~5.0.0 | Vue CLI ESLint plugin |
| @vue/cli-plugin-router | ~5.0.0 | Vue CLI Router plugin |
| @vue/cli-plugin-vuex | ~5.0.0 | Vue CLI Vuex plugin |
| @vue/cli-service | ~5.0.0 | Vue CLI service |
| eslint | ^7.32.0 | JavaScript linter |
| eslint-plugin-vue | ^8.0.3 | Vue ESLint plugin |

### 3.6 Docker Images

| Image | Purpose |
|-------|---------|
| mysql:8.0 | MySQL database server |
| redis:latest | Redis cache server |
| nacos/nacos-server:latest | Nacos service discovery and configuration |

---

## 4. System File Organization

### 4.1 Directory Structure

```
bank-service-chatbot/
├── backend/                          # Backend microservices
│   ├── pom.xml                       # Parent Maven configuration
│   ├── api/                          # API client interfaces
│   │   └── src/main/java/com/maretu/api/
│   │       ├── client/               # Feign clients (BankClient, UserClient, ChatClient)
│   │       ├── config/               # Feign configuration
│   │       └── dto/                  # Data transfer objects
│   ├── common/                       # Common utilities and configurations
│   │   └── src/main/java/com/maretu/common/
│   │       ├── config/               # Shared configurations (MybatisPlusConfig)
│   │       ├── dto/                  # Common DTOs (Context, PageDTO, QueryDTO)
│   │       └── utils/                # Utility classes (JwtUtils, Result, etc.)
│   ├── gateway/                      # API Gateway service
│   │   └── src/main/java/com/maretu/gateway/
│   │       ├── GatewayApplication.java    # Main entry point
│   │       ├── config/               # Gateway configurations
│   │       └── filter/               # Custom filters (AuthFilter)
│   ├── bank-service/                 # Banking service
│   │   └── src/main/java/com/maretu/bank/
│   │       ├── BankServiceApplication.java
│   │       ├── controller/           # REST controllers
│   │       ├── service/              # Business logic
│   │       ├── mapper/               # MyBatis mappers
│   │       ├── pojo/                 # Entity classes
│   │       └── dto/                  # Service DTOs
│   ├── user-service/                 # User management service
│   │   └── src/main/java/com/maretu/user/
│   │       ├── UserServiceApplication.java
│   │       ├── controller/
│   │       ├── service/
│   │       ├── mapper/
│   │       ├── pojo/
│   │       ├── vo/                   # View objects
│   │       └── enums/                # Enumerations
│   └── chatbot-service/              # Chatbot service
│       └── src/main/java/com/maretu/chat/
│           ├── ChatbotServiceApplication.java
│           ├── controller/
│           ├── service/
│           ├── mapper/
│           ├── pojo/
│           └── dto/
├── database/                         # Database initialization scripts
│   ├── bank.sql                      # Banking database schema
│   ├── user.sql                      # User database schema
│   └── chat.sql                      # Chat database schema
├── docker/                           # Docker configurations
│   ├── mysql/
│   │   ├── start.sh                  # MySQL startup script
│   │   └── init/                     # Database initialization scripts
│   ├── redis/
│   │   └── start.sh                  # Redis startup script
│   ├── nacos/
│   │   └── start.sh                  # Nacos startup script
│   └── nacos_config.zip              # Nacos configuration export
├── frontend/                         # Vue.js frontend application
│   ├── package.json                  # npm dependencies
│   ├── vue.config.js                 # Vue configuration
│   ├── public/
│   │   └── index.html                # HTML template
│   └── src/
│       ├── main.js                   # Application entry point
│       ├── App.vue                   # Root component
│       ├── http.js                   # Axios configuration
│       ├── router/
│       │   └── index.js              # Vue Router configuration
│       ├── store/
│       │   └── index.js              # Vuex store
│       ├── api/
│       │   └── api.js                # API calls
│       ├── components/
│       │   └── Layout.vue            # Main layout component
│       ├── views/                    # Page components
│       │   ├── LoginView.vue
│       │   ├── RegisterView.vue
│       │   ├── Dashboard.vue
│       │   ├── Accounts.vue
│       │   ├── Transactions.vue
│       │   ├── Transfers.vue
│       │   ├── Chatbot.vue
│       │   ├── Profile.vue
│       │   └── admin/                # Admin pages
│       │       ├── UserManage.vue
│       │       ├── KnowledgeManage.vue
│       │       └── ChatStats.vue
│       ├── assets/
│       │   ├── logo.png
│       │   └── styles/
│       │       ├── global.css
│       │       └── variables.css
│       └── utils/
│           ├── message.js            # Message utilities
│           └── desensitize.js        # Data masking utilities
├── README.md                         # Project readme
├── User Manual.md                    # User manual
└── Maintenance Manual.md             # This file
```

### 4.2 Temporary Files

| Location | Description |
|----------|-------------|
| `backend/*/target/` | Maven build artifacts (can be deleted) |
| `frontend/node_modules/` | npm dependencies (can be regenerated) |
| `frontend/dist/` | Production build output |
| `*.log` | Application log files |

---

## 5. Space and Memory Requirements

### 5.1 Disk Space Requirements

| Component | Space Required |
|-----------|----------------|
| Source code | ~50 MB |
| Maven dependencies | ~500 MB |
| npm dependencies | ~300 MB |
| Docker images (MySQL, Redis, Nacos) | ~2 GB |
| Database data (estimated) | ~500 MB |
| **Total (minimum)** | **~3.5 GB** |
| **Total (recommended)** | **~10 GB** |

### 5.2 Memory Requirements

| Component | Memory Usage |
|-----------|--------------|
| MySQL container | ~512 MB |
| Redis container | ~64 MB |
| Nacos container | ~512 MB |
| Gateway service | ~256 MB |
| Bank service | ~256 MB |
| User service | ~256 MB |
| Chatbot service | ~256 MB |
| Frontend (development) | ~128 MB |
| **Total (minimum)** | **~2 GB** |
| **Total (recommended)** | **~4 GB** |

### 5.3 CPU Requirements

| Component | CPU Usage (idle) | CPU Usage (active) |
|-----------|------------------|-------------------|
| Backend services | ~5% | ~20-30% |
| Frontend | ~2% | ~10% |
| Docker containers | ~5% | ~15% |
| **Total** | **~12%** | **~55%** |

---

## 6. Source Code File List

### 6.1 Backend Files

#### Gateway Service

| File | Description |
|------|-------------|
| `GatewayApplication.java` | Main entry point for Gateway service |
| `config/GatewayConfig.java` | Gateway route configuration |
| `filter/AuthFilter.java` | Authentication filter for JWT validation |

#### Bank Service

| File | Description |
|------|-------------|
| `BankServiceApplication.java` | Main entry point for Bank service |
| `controller/AccountController.java` | Account management REST endpoints |
| `controller/TransactionController.java` | Transaction query endpoints |
| `controller/TransferController.java` | Transfer operation endpoints |
| `service/IAccountsService.java` | Account service interface |
| `service/impl/AccountsServiceImpl.java` | Account service implementation |
| `mapper/AccountsMapper.java` | MyBatis mapper for accounts table |
| `pojo/Accounts.java` | Account entity class |
| `pojo/Transactions.java` | Transaction entity class |
| `pojo/Transfers.java` | Transfer entity class |
| `dto/TransferReq.java` | Transfer request DTO |
| `dto/DashboardStats.java` | Dashboard statistics DTO |

#### User Service

| File | Description |
|------|-------------|
| `UserServiceApplication.java` | Main entry point for User service |
| `controller/UserController.java` | User management endpoints |
| `controller/AuthController.java` | Authentication endpoints |
| `service/IUsersService.java` | User service interface |
| `service/impl/UsersServiceImpl.java` | User service implementation |
| `mapper/UsersMapper.java` | MyBatis mapper for users table |
| `pojo/Users.java` | User entity class |
| `vo/LoginVO.java` | Login view object |
| `vo/RegisterVO.java` | Registration view object |
| `enums/RoleEnum.java` | User role enumeration |

#### Chatbot Service

| File | Description |
|------|-------------|
| `ChatbotServiceApplication.java` | Main entry point for Chatbot service |
| `controller/ChatController.java` | Chat endpoints |
| `service/IChatService.java` | Chat service interface |
| `service/impl/ChatServiceImpl.java` | Chat service implementation |
| `mapper/KnowledgeMapper.java` | Knowledge base mapper |
| `pojo/Knowledge.java` | Knowledge entity class |

#### Common Module

| File | Description |
|------|-------------|
| `config/MybatisPlusConfig.java` | MyBatis-Plus configuration |
| `dto/Context.java` | User context holder |
| `dto/PageDTO.java` | Pagination DTO |
| `dto/QueryDTO.java` | Query parameters DTO |
| `utils/Result.java` | Standard API response wrapper |
| `utils/JwtUtils.java` | JWT token utilities |
| `utils/RedisConstants.java` | Redis key constants |
| `utils/ChatGuardUtils.java` | Chat security utilities |
| `utils/ImagesUtils.java` | Image processing utilities |

#### API Module

| File | Description |
|------|-------------|
| `client/BankClient.java` | Feign client for Bank service |
| `client/UserClient.java` | Feign client for User service |
| `client/ChatClient.java` | Feign client for Chatbot service |
| `config/DefaultFeignConfig.java` | Default Feign configuration |
| `dto/AccountDTO.java` | Account data transfer object |
| `dto/UserDTO.java` | User data transfer object |
| `dto/TransactionDTO.java` | Transaction data transfer object |

### 6.2 Frontend Files

| File | Description |
|------|-------------|
| `main.js` | Vue application entry point |
| `App.vue` | Root Vue component |
| `http.js` | Axios HTTP client configuration |
| `router/index.js` | Vue Router configuration |
| `store/index.js` | Vuex state management |
| `api/api.js` | API function wrappers |
| `components/Layout.vue` | Main application layout |
| `views/LoginView.vue` | Login page component |
| `views/RegisterView.vue` | Registration page component |
| `views/Dashboard.vue` | Dashboard page component |
| `views/Accounts.vue` | Account management page |
| `views/Transactions.vue` | Transaction history page |
| `views/Transfers.vue` | Transfer page |
| `views/Chatbot.vue` | Chatbot interface page |
| `views/Profile.vue` | User profile page |
| `views/admin/UserManage.vue` | Admin user management |
| `views/admin/KnowledgeManage.vue` | Admin knowledge base management |
| `views/admin/ChatStats.vue` | Admin chat statistics |
| `utils/message.js` | Message notification utilities |
| `utils/desensitize.js` | Data masking utilities |

### 6.3 Configuration Files

| File | Description |
|------|-------------|
| `backend/pom.xml` | Parent Maven configuration |
| `backend/*/pom.xml` | Module-specific Maven configuration |
| `backend/*/src/main/resources/application.yaml` | Service configuration |
| `frontend/package.json` | npm package configuration |
| `frontend/vue.config.js` | Vue CLI configuration |
| `docker/mysql/start.sh` | MySQL Docker startup script |
| `docker/redis/start.sh` | Redis Docker startup script |
| `docker/nacos/start.sh` | Nacos Docker startup script |

---

## 7. Key Constants

### 7.1 Service Ports

| Service | Port | Configuration File |
|---------|------|-------------------|
| Gateway | 8080 | `backend/gateway/src/main/resources/application.yaml` |
| User Service | 8081 | `backend/user-service/src/main/resources/application.yaml` |
| Bank Service | 8082 | `backend/bank-service/src/main/resources/application.yaml` |
| Chatbot Service | 8083 | `backend/chatbot-service/src/main/resources/application.yaml` |
| Frontend | 80 | `frontend/vue.config.js` |
| Nacos | 8848 | `docker/nacos/start.sh` |
| MySQL | 3306 | `docker/mysql/start.sh` |

### 7.2 Default Credentials

| System | Username | Password | Location |
|--------|----------|----------|----------|
| Nacos | nacos | maretu | Configuration |
| MySQL | root | maretu | `docker/mysql/init/*.sql` |
| Admin Account | admin@bank.com | admin123 | Database seed data |

### 7.3 Key Constants in Code

#### Result.java

```java
public static final Integer SUCCESS_CODE = 1;
public static final Integer FAILURE_CODE = 0;
```

#### RedisConstants.java

| Constant | Value | Purpose |
|----------|-------|---------|
| `USER_TOKEN_KEY` | "user:token:" | User token cache key prefix |
| `USER_INFO_KEY` | "user:info:" | User info cache key prefix |
| `TOKEN_EXPIRE_TIME` | 7200 | Token expiration time (seconds) |

#### Environment Variables

| Variable | Default | Purpose |
|----------|---------|---------|
| `DOCKER_SERVER_ADDR` | ipv6.maretu.top | Docker server address for service discovery |

### 7.4 API Endpoints

| Endpoint | Method | Service | Description |
|----------|--------|---------|-------------|
| `/users/login` | POST | User Service | User login |
| `/users/register` | POST | User Service | User registration |
| `/users/refresh` | POST | User Service | Refresh token |
| `/accounts` | GET | Bank Service | Get user accounts |
| `/accounts/{id}` | GET | Bank Service | Get account details |
| `/transactions` | GET | Bank Service | Get transactions |
| `/transfers` | POST | Bank Service | Execute transfer |
| `/chat/message` | POST | Chatbot Service | Send chat message |

---

## 8. Main Classes, Methods, and Data Structures

### 8.1 Backend Main Classes

#### Gateway Service

**GatewayApplication.java**
```java
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

#### Bank Service

**BankServiceApplication.java**
```java
@SpringBootApplication(scanBasePackages = {"com.maretu.bank", "com.maretu.common.config"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.maretu.api.client")
@MapperScan("com.maretu.bank.mapper")
@EnableAsync
public class BankServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankServiceApplication.class, args);
    }
}
```

#### User Service

**UserServiceApplication.java**
```java
@SpringBootApplication(scanBasePackages = {"com.maretu.user", "com.maretu.common.config"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.maretu.api.client")
@MapperScan("com.maretu.user.mapper")
@EnableAsync
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

#### Chatbot Service

**ChatbotServiceApplication.java**
```java
@SpringBootApplication(scanBasePackages = {"com.maretu.chat", "com.maretu.common.config"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.maretu.api.client")
@MapperScan("com.maretu.chat.mapper")
@EnableAsync
public class ChatbotServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatbotServiceApplication.class, args);
    }
}
```

### 8.2 Key Controllers

#### AccountController.java

| Method | Endpoint | Description |
|--------|----------|-------------|
| `createAccount()` | POST /accounts | Create new account |
| `getAccounts()` | GET /accounts | Get all user accounts |
| `getAccountDetail()` | GET /accounts/{id} | Get account details |
| `getDashboardStats()` | GET /accounts/dashboard/stats | Get dashboard statistics |
| `updateAccount()` | PUT /accounts/{id} | Update account info |
| `updateStatus()` | PUT /accounts/{id}/status | Update account status (freeze/unfreeze) |

### 8.3 Key Data Structures

#### Context.java (User Context)
```java
public class Context {
    private Integer userId;
    private String username;
    private String email;
    private String ip;
}
```

#### Result.java (API Response Wrapper)
```java
public class Result<T> {
    public Integer code;      // 1=success, 0=failure
    public String message;    // Response message
    public T data;            // Response data
    public Long timestamp;    // Response timestamp
}
```

#### Accounts.java (Account Entity)
```java
public class Accounts {
    private Long id;
    private Long userId;
    private String accountNumber;
    private BigDecimal balance;
    private String status;    // ACTIVE, FROZEN, CLOSED
    private String type;      // SAVINGS, CHECKING
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### Transactions.java (Transaction Entity)
```java
public class Transactions {
    private Long id;
    private String transactionId;
    private Long accountId;
    private String type;      // DEPOSIT, WITHDRAWAL, TRANSFER
    private BigDecimal amount;
    private String status;    // PENDING, COMPLETED, FAILED
    private LocalDateTime createdAt;
}
```

### 8.4 Frontend Main Components

#### App.vue (Root Component)
```vue
<template>
  <div id="app">
    <router-view/>
  </div>
</template>
```

#### http.js (Axios Configuration)
- Request interceptor: Adds JWT token and user info headers
- Response interceptor: Handles 401 errors and token refresh

#### store/index.js (Vuex Store)
| Mutation | Description |
|----------|-------------|
| `SET_TOKEN` | Set JWT token in store |
| `SET_USER_INFO` | Set user info in store |
| `CLEAR_USER` | Clear user data (logout) |

---

## 9. File Path Names

### 9.1 Configuration File Paths

| File | Absolute Path (relative to project root) |
|------|------------------------------------------|
| Gateway config | `backend/gateway/src/main/resources/application.yaml` |
| User service config | `backend/user-service/src/main/resources/application.yaml` |
| Bank service config | `backend/bank-service/src/main/resources/application.yaml` |
| Chatbot config | `backend/chatbot-service/src/main/resources/application.yaml` |
| Frontend HTTP config | `frontend/src/http.js` |
| Nacos config export | `docker/nacos_config.zip` |

### 9.2 Database Script Paths

| File | Purpose |
|------|---------|
| `database/bank.sql` | Banking database schema |
| `database/user.sql` | User database schema |
| `database/chat.sql` | Chat database schema |
| `docker/mysql/init/bank.sql` | MySQL initialization - banking tables |
| `docker/mysql/init/user.sql` | MySQL initialization - user tables |
| `docker/mysql/init/chat.sql` | MySQL initialization - chat tables |
| `docker/mysql/init/nacos.sql` | MySQL initialization - Nacos tables |

### 9.3 Data File Paths

| Data Type | Location |
|-----------|----------|
| MySQL data | Docker volume (mysql_data) |
| Redis data | Docker volume (redis_data) |
| Nacos data | Docker volume (nacos_data) |
| Application logs | Console output / log files in service directories |

### 9.4 Build Output Paths

| Component | Output Path |
|-----------|-------------|
| Gateway JAR | `backend/gateway/target/gateway-*.jar` |
| Bank Service JAR | `backend/bank-service/target/bank-service-*.jar` |
| User Service JAR | `backend/user-service/target/user-service-*.jar` |
| Chatbot Service JAR | `backend/chatbot-service/target/chatbot-service-*.jar` |
| Frontend build | `frontend/dist/` |

---

## 10. Future Improvement Directions

### 10.1 Security Enhancements

1. **Two-Factor Authentication (2FA)**
   - Add SMS or email verification for sensitive operations
   - Implement TOTP-based 2FA for admin accounts

2. **Enhanced JWT Security**
   - Implement JWT blacklisting for logout
   - Add refresh token rotation

3. **Rate Limiting**
   - Add per-user rate limiting for API endpoints
   - Implement CAPTCHA for failed login attempts

### 10.2 Feature Additions

1. **International Transfers**
   - Support multi-currency accounts
   - Add exchange rate integration
   - Implement SWIFT transfer simulation

2. **Loan Management**
   - Add loan application workflow
   - Implement loan repayment scheduling
   - Add credit score calculation

3. **Investment Services**
   - Add stock trading simulation
   - Implement fund management
   - Add portfolio tracking

4. **Mobile Application**
   - Develop native iOS/Android apps
   - Add biometric authentication
   - Implement push notifications

### 10.3 Performance Improvements

1. **Database Optimization**
   - Add read-write splitting
   - Implement database sharding for large datasets
   - Add query result caching

2. **Microservices Optimization**
   - Implement circuit breaker pattern
   - Add service mesh for better observability
   - Implement distributed tracing

3. **Frontend Optimization**
   - Implement code splitting
   - Add service worker for offline support
   - Optimize bundle size

### 10.4 Chatbot Improvements

1. **AI Model Upgrades**
   - Integrate with more advanced LLM models
   - Add multi-language support
   - Implement context-aware conversations

2. **Knowledge Base Enhancement**
   - Add automatic knowledge extraction from documents
   - Implement feedback-based learning
   - Add voice input/output support

3. **Chat Analytics**
   - Add sentiment analysis
   - Implement conversation flow visualization
   - Add automated report generation

### 10.5 DevOps Improvements

1. **CI/CD Pipeline**
   - Set up automated testing
   - Implement blue-green deployment
   - Add automated rollback

2. **Monitoring and Alerting**
   - Integrate with Prometheus/Grafana
   - Add custom business metrics
   - Implement alerting rules

3. **Container Orchestration**
   - Migrate to Kubernetes
   - Implement auto-scaling
   - Add service mesh (Istio)

---

## 11. Error Reporting

### 11.1 Common Errors and Solutions

#### Database Connection Errors

**Error:** `Communications link failure`

**Cause:** MySQL container not running or wrong credentials

**Solution:**
```bash
# Check MySQL container status
docker ps | grep mysql

# Restart MySQL if needed
docker restart mysql

# Verify connection
docker exec -it mysql mysql -uroot -pmaretu -e "SELECT 1"
```

#### Nacos Connection Errors

**Error:** `Nacos connection refused`

**Cause:** Nacos not started or MySQL not ready

**Solution:**
```bash
# Check Nacos logs
docker logs nacos

# Ensure MySQL is running first
docker ps | grep mysql

# Restart Nacos
docker restart nacos
```

#### Port Conflict Errors

**Error:** `Port 8080 is already in use`

**Cause:** Another service using the port

**Solution:**
```bash
# Windows - Find process using port 8080
netstat -ano | findstr :8080

# Kill the process
taskkill /PID <PID> /F

# Or change port in application.yaml
```

#### Token Expiration Errors

**Error:** `401 Unauthorized`

**Cause:** JWT token expired

**Solution:**
- Frontend automatically handles token refresh
- If refresh fails, user will be redirected to login page
- Check Redis is running for token storage

#### Maven Build Errors

**Error:** `BUILD FAILURE - Could not resolve dependencies`

**Cause:** Network issues or missing dependencies

**Solution:**
```bash
# Clear Maven cache
mvn clean

# Force update dependencies
mvn clean install -U

# Check network connection
```

#### Frontend Build Errors

**Error:** `npm ERR! code ENOENT`

**Cause:** Missing node_modules or corrupted installation

**Solution:**
```bash
# Remove node_modules and reinstall
rm -rf node_modules package-lock.json
npm install

# Check Node.js version
node --version
```

### 11.2 Error Logging

#### Backend Logging

Logs are output to console by default. To enable file logging:

1. Edit Nacos configuration `shared-log.yaml`
2. Add file appender configuration
3. Restart affected services

#### Frontend Logging

Browser console logs are available in developer tools. For production:

1. Configure error tracking service (e.g., Sentry)
2. Add error boundary components
3. Implement error reporting API

### 11.3 Debugging Guide

#### Backend Debugging

1. **IDE Setup (IntelliJ IDEA)**
   - Open backend project
   - Create Run Configuration for each service
   - Set breakpoints in code
   - Start in Debug mode

2. **Remote Debugging**
   ```bash
   # Add JVM args to enable remote debugging
   -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
   ```

#### Frontend Debugging

1. **Browser DevTools**
   - Open Chrome/Firefox DevTools
   - Use Vue DevTools extension
   - Set breakpoints in Sources tab
   - Use console for debugging

2. **VS Code Debugging**
   - Install Vue.js extension
   - Configure launch.json
   - Start debugging session

### 11.4 Reporting Bugs

When reporting bugs, include:

1. **Environment Information**
   - Operating system and version
   - Java version
   - Node.js version
   - Docker version

2. **Steps to Reproduce**
   - Detailed steps to trigger the bug
   - Expected behavior
   - Actual behavior

3. **Logs and Screenshots**
   - Relevant log output
   - Screenshots of errors
   - Network request details

4. **Submit to GitHub Issues**
   - Visit project repository
   - Click "Issues" tab
   - Click "New Issue"
   - Fill in bug report template

---

**End of Document**
