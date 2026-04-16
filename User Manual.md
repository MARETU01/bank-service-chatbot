# Bank Service Chatbot System - User Manual

**Version:** 1.0  
**Last Updated:** April 2026  
**Applicable Systems:** Windows 11 / Linux / macOS

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [System Requirements](#2-system-requirements)
3. [Installation and Configuration](#3-installation-and-configuration)
4. [Starting the System](#4-starting-the-system)
5. [Functionality Testing Guide](#5-functionality-testing-guide)
6. [Frequently Asked Questions](#6-frequently-asked-questions)
7. [Appendix](#7-appendix)

---

## 1. System Overview

The Bank Service Chatbot System is a microservices-based banking simulation system that provides user management, account management, transaction processing, transfer services, and an AI-powered chatbot.

### 1.1 System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Frontend (Vue.js)                      │
│                     http://localhost:80                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Gateway                                │
│                     port: 8080                              │
└─────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
    ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
    │  User Service   │ │  Bank Service   │ │  Chatbot Svc    │
    │  port: 8081     │ │  port: 8082     │ │  port: 8083     │
    └─────────────────┘ └─────────────────┘ └─────────────────┘
              │               │               │
              └───────────────┼───────────────┘
                              ▼
    ┌─────────────────────────────────────────────────────────┐
    │   MySQL (3306)    │    Redis    │    Nacos (8848)       │
    └─────────────────────────────────────────────────────────┘
```

### 1.2 Main Features

| Module | Function Description |
|--------|---------------------|
| User Management | Registration, login, password reset, profile management |
| Account Management | View account information, balance inquiry |
| Transaction Management | Transaction history query, transaction details |
| Transfer Service | Internal transfers, transfer limit management |
| Chatbot | AI customer service, FAQ |
| Admin Panel | User management, knowledge base management, chat statistics |

---

## 2. System Requirements

### 2.1 Hardware Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| CPU | 4 cores | 8 cores or higher |
| RAM | 8 GB | 16 GB or higher |
| Storage | 10 GB free space | 256 GB SSD |
| Network | Broadband internet | High-speed internet |

### 2.2 Software Requirements

Before starting, ensure your system has the following software installed:

| Software | Version | Verification Command |
|----------|---------|---------------------|
| Java | JDK 21 | `java -version` |
| Node.js | 24+ | `node --version` |
| npm | 11+ | `npm --version` |
| Docker | Latest stable | `docker --version` |
| Maven | 3.8+ | `mvn --version` |

### 2.3 Software Installation Guide

#### 2.3.1 Installing Java 21

**Windows:**
1. Visit [Oracle JDK Download Page](https://www.oracle.com/java/technologies/downloads/)
2. Download JDK 21 Windows x64 Installer
3. Run the installer and follow the wizard
4. Set the `JAVA_HOME` environment variable

**Verify Installation:**
```bash
java -version
```

> **Screenshot 1:** Insert screenshot of `java -version` command output showing Java 21 is correctly installed.

#### 2.3.2 Installing Node.js 24+

**Windows:**
1. Visit [Node.js Official Website](https://nodejs.org/)
2. Download and install Node.js 24+ LTS version
3. The installer will automatically configure environment variables

**Verify Installation:**
```bash
node --version
npm --version
```

> **Screenshot 2:** Insert screenshot of Node.js and npm version output.

#### 2.3.3 Installing Docker

**Windows:**
1. Visit [Docker Desktop Download Page](https://www.docker.com/products/docker-desktop/)
2. Download and install Docker Desktop
3. Launch Docker Desktop and ensure it's running properly

**Verify Installation:**
```bash
docker --version
docker ps
```

> **Screenshot 3:** Insert screenshot of Docker Desktop running interface.

#### 2.3.4 Installing Maven

**Windows:**
1. Visit [Maven Download Page](https://maven.apache.org/download.cgi)
2. Download Maven 3.8+ binary distribution
3. Extract to a directory (e.g., `C:\Program Files\Maven`)
4. Set `MAVEN_HOME` environment variable and add to `PATH`

**Verify Installation:**
```bash
mvn --version
```

> **Screenshot 4:** Insert screenshot of `mvn --version` command output.

---

## 3. Installation and Configuration

### 3.1 Getting the Project Code

Open terminal or command prompt and navigate to your project directory:

```bash
git clone https://github.com/MARETU01/bank-service-chatbot.git
cd bank-service-chatbot
```

> **Screenshot 5:** Insert screenshot of successful project cloning in terminal.

### 3.2 Starting Infrastructure Services

#### 3.2.1 Starting MySQL

```bash
cd docker/mysql
./start.sh
```

**Windows PowerShell Users:**
```powershell
cd docker/mysql
bash start.sh
```

**Windows CMD Users:**
```cmd
cd docker\mysql
bash start.sh
```

> **Screenshot 6:** Insert screenshot of MySQL container starting successfully.

**Check MySQL Startup Logs:**
```bash
docker logs -f mysql
```

Wait for logs similar to the following indicating MySQL has started successfully:
```
[Server] [MY-010931] [Server] /usr/sbin/mysqld: ready for connections.
Version: '8.0.xx'  socket: '/var/run/mysqld/mysqld.sock'  port: 3306
```

Press `Ctrl+C` to exit log view.

> **Screenshot 7:** Insert screenshot of MySQL successful startup logs.

**Test MySQL Connection:**

Use a database tool (such as Navicat, DataGrip, or command line) to test connection:
- Host: `localhost`
- Port: `3306`
- Username: `root`
- Password: `maretu`

**Command Line Test:**
```bash
docker exec -it mysql mysql -uroot -pmaretu
```

> **Screenshot 8:** Insert screenshot of successful MySQL connection.

#### 3.2.2 Starting Redis

```bash
cd ../redis
./start.sh
```

> **Screenshot 9:** Insert screenshot of Redis container starting successfully.

#### 3.2.3 Starting Nacos

```bash
cd ../nacos
./start.sh
```

> **Screenshot 10:** Insert screenshot of Nacos container starting successfully.

**Check All Running Containers:**
```bash
docker ps
```

> **Screenshot 11:** Insert screenshot of `docker ps` command output showing all containers running.

### 3.3 Configuring Nacos

#### 3.3.1 Access Nacos Console

Open in browser: `http://localhost:8848/nacos`

> **Screenshot 12:** Insert screenshot of Nacos login page.

#### 3.3.2 Login to Nacos

Login with default credentials:
- Username: `nacos`
- Password: `maretu`

> **Screenshot 13:** Insert screenshot of Nacos main interface after login.

#### 3.3.3 Import Configuration File

1. Click "Configuration Management" -> "Configuration List" in the left menu
2. Click the "Import" button in the top right corner
3. Select the `/docker/nacos_config.zip` file from the project directory
4. Click "OK" to complete the import

> **Screenshot 14:** Insert screenshot of configuration file import interface.
> **Screenshot 15:** Insert screenshot of configuration list after import completion.

### 3.4 Configuring Backend Services

#### 3.4.1 Setting Docker Server Address

Set the `DOCKER_SERVER_ADDR` environment variable based on your operating system:

**Windows PowerShell:**
```powershell
$env:DOCKER_SERVER_ADDR="192.168.1.100"
```

**Windows CMD:**
```cmd
set DOCKER_SERVER_ADDR=192.168.1.100
```

**macOS / Linux:**
```bash
export DOCKER_SERVER_ADDR="192.168.1.100"
```

> **Note:** Replace `192.168.1.100` with your actual Docker server IP address.

#### 3.4.2 Configuring Chatbot Service (Optional)

If you have your own OpenAI API key, you can modify the configuration file:

Open file: `backend/chatbot-service/src/main/resources/application.yaml`

```yaml
spring:
  ai:
    openai:
      api-key: "your-api-key"
      base-url: "your-api-base-url"
      chat:
        options:
          model: "deepseek-ai/DeepSeek-V3.2"
```

> **Screenshot 16:** Insert screenshot of configuration file editing.

### 3.5 Compiling Backend Project

```bash
cd backend
mvn clean install
```

Compilation may take a few minutes. Wait for the `BUILD SUCCESS` message.

> **Screenshot 17:** Insert screenshot of successful Maven build.

### 3.6 Configuring Frontend

Open file: `frontend/src/http.js`

Modify `baseURL` to your backend gateway address:

```javascript
const instance = axios.create({
    baseURL: 'http://192.168.1.100:8080',  // Replace with your server IP
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})
```

> **Screenshot 18:** Insert screenshot of http.js file editing.

---

## 4. Starting the System

### 4.1 Starting Backend Services

You need to open four terminal windows to start each service separately.

#### 4.1.1 Starting Gateway Service

**Terminal Window 1:**
```bash
cd backend/gateway
mvn spring-boot:run
```

Wait for logs similar to:
```
Started GatewayApplication in xx.xxx seconds
```

> **Screenshot 19:** Insert screenshot of gateway service successful startup.

#### 4.1.2 Starting Bank Service

**Terminal Window 2:**
```bash
cd backend/bank-service
mvn spring-boot:run
```

> **Screenshot 20:** Insert screenshot of bank service successful startup.

#### 4.1.3 Starting User Service

**Terminal Window 3:**
```bash
cd backend/user-service
mvn spring-boot:run
```

> **Screenshot 21:** Insert screenshot of user service successful startup.

#### 4.1.4 Starting Chatbot Service

**Terminal Window 4:**
```bash
cd backend/chatbot-service
mvn spring-boot:run
```

> **Screenshot 22:** Insert screenshot of chatbot service successful startup.

#### 4.1.5 Verifying All Services Status

After all services start, you should see four terminals running.

> **Screenshot 23:** Insert screenshot of four terminal windows running simultaneously.

### 4.2 Starting Frontend Service

Open a new terminal window:

```bash
cd frontend

# Install dependencies (first time only)
npm install

# Start development server
npm run serve
```

Wait for logs similar to:
```
App running at:
- Local:   http://localhost:80/
```

> **Screenshot 24:** Insert screenshot of frontend service successful startup.

### 4.3 Accessing the System

Open in browser: `http://localhost:80`

> **Screenshot 25:** Insert screenshot of system homepage.

---

## 5. Functionality Testing Guide

### 5.1 Login System

#### 5.1.1 Login with Admin Account

On the login page, enter:
- Email: `admin@bank.com`
- Password: `admin123`

Click the "Login" button.

> **Screenshot 26:** Insert screenshot of login page.
> **Screenshot 27:** Insert screenshot of dashboard after successful login.

### 5.2 Dashboard Testing

After successful login, you will see the system dashboard displaying:
- Total number of accounts
- Total balance
- Today's transactions
- System statistics

> **Screenshot 28:** Insert screenshot of complete dashboard.

### 5.3 Account Management Testing

#### 5.3.1 View Account List

1. Click "Account Management" in the left menu
2. View the account list including account numbers, balances, status, etc.

> **Screenshot 29:** Insert screenshot of account list page.

#### 5.3.2 View Account Details

1. Click any account in the account list
2. View detailed account information

> **Screenshot 30:** Insert screenshot of account details page.

### 5.4 Transaction Records Testing

#### 5.4.1 View Transaction History

1. Click "Transaction Records" in the left menu
2. View transaction history list

> **Screenshot 31:** Insert screenshot of transaction records page.

#### 5.4.2 Transaction Query Function

1. Filter transactions by date range
2. Filter by transaction type
3. View query results

> **Screenshot 32:** Insert screenshot of transaction query function.

### 5.5 Transfer Function Testing

#### 5.5.1 Execute Transfer

1. Click "Transfer Service" in the left menu
2. Enter recipient account number
3. Enter transfer amount
4. Click "Confirm Transfer"

> **Screenshot 33:** Insert screenshot of transfer page.
> **Screenshot 34:** Insert screenshot of successful transfer confirmation.

#### 5.5.2 Verify Transfer Results

1. Return to account list to check balance changes
2. View transaction records to confirm transfer entry

> **Screenshot 35:** Insert screenshot of updated balance after transfer.

### 5.6 Chatbot Testing

#### 5.6.1 Open Chatbot

1. Click "Chatbot" in the left menu
2. Wait for chat interface to load

> **Screenshot 36:** Insert screenshot of chatbot interface.

#### 5.6.2 Test FAQ Questions

Try sending the following questions:
- "How to reset password?"
- "What is the transfer limit?"
- "How to view transaction records?"

> **Screenshot 37:** Insert screenshot of chatbot answering questions.

#### 5.6.3 Test AI Conversation

Send custom questions to test AI response capabilities.

> **Screenshot 38:** Insert screenshot of AI conversation.

### 5.7 User Management Testing (Admin Only)

#### 5.7.1 Access User Management

1. Login with admin account
2. Click "Admin Panel" -> "User Management"

> **Screenshot 39:** Insert screenshot of user management page.

#### 5.7.2 Test User Operations

- View user list
- Search for specific users
- View user details

> **Screenshot 40:** Insert screenshot of user operations.

### 5.8 Knowledge Base Management Testing (Admin Only)

#### 5.8.1 Access Knowledge Base Management

1. Click "Admin Panel" -> "Knowledge Base Management"

> **Screenshot 41:** Insert screenshot of knowledge base management page.

#### 5.8.2 Test Knowledge Base Operations

- View existing knowledge entries
- Add new knowledge entries
- Edit or delete entries

> **Screenshot 42:** Insert screenshot of knowledge base operations.

### 5.9 New User Registration Testing

#### 5.9.1 Logout Current Account

Click the avatar in the top right corner and select "Logout".

#### 5.9.2 Register New Account

1. Click "Register" button
2. Fill in registration form:
   - Name
   - Email
   - Password
   - Confirm password
3. Click "Register"

> **Screenshot 43:** Insert screenshot of registration page.
> **Screenshot 44:** Insert screenshot of successful registration.

#### 5.9.3 Login with New Account

Login to the system using the newly registered account.

> **Screenshot 45:** Insert screenshot of successful login with new account.

### 5.10 Password Reset Testing

#### 5.10.1 Access Password Reset Page

1. Click "Forgot Password" on login page
2. Enter registered email
3. Follow prompts to reset password

> **Screenshot 46:** Insert screenshot of password reset page.

---

## 6. Frequently Asked Questions

### 6.1 Startup Issues

#### Q1: MySQL Container Fails to Start

**Solution:**
1. Check if port 3306 is in use:
   ```bash
   netstat -ano | findstr :3306
   ```
2. If port is in use, stop the program using it or modify MySQL port
3. Check if Docker is running properly

#### Q2: Nacos Fails to Start

**Solution:**
1. Ensure MySQL is running and accessible
2. Check Nacos logs:
   ```bash
   docker logs nacos
   ```
3. Confirm nacos database has been created in MySQL

#### Q3: Port Conflict When Starting Backend Services

**Solution:**
1. Check if any services are already running
2. Use following command to check port usage:
   ```bash
   netstat -ano | findstr :8080
   ```
3. Stop conflicting services or modify ports in configuration files

#### Q4: Maven Build Fails

**Solution:**
1. Check network connection
2. Clear Maven cache:
   ```bash
   mvn clean
   ```
3. Verify Java version is 21

### 6.2 Connection Issues

#### Q5: Frontend Cannot Connect to Backend

**Solution:**
1. Check `baseURL` configuration in `frontend/src/http.js`
2. Ensure gateway service is running
3. Check firewall settings

#### Q6: Chatbot Not Responding

**Solution:**
1. Verify OpenAI API configuration is correct
2. Check network connection
3. View chatbot-service logs

### 6.3 Database Issues

#### Q7: Cannot Connect to Database

**Solution:**
1. Confirm MySQL container is running:
   ```bash
   docker ps | grep mysql
   ```
2. Check database password is correct (default: maretu)
3. Try restarting MySQL container

---

## 7. Appendix

### 7.1 Default Configuration

| Component | Setting | Default Value |
|-----------|---------|---------------|
| MySQL | Password | maretu |
| MySQL | Port | 3306 |
| Nacos | Port | 8848 |
| Nacos | Username | nacos |
| Nacos | Password | maretu |
| Gateway | Port | 8080 |
| User-service | Port | 8081 |
| Bank-service | Port | 8082 |
| Chat-service | Port | 8083 |
| Frontend | Port | 80 |

### 7.2 Default Login Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@bank.com | admin123 |

### 7.3 Service Startup Checklist

Before testing, ensure all following services are running:

- [ ] MySQL container running
- [ ] Redis container running
- [ ] Nacos container running
- [ ] Nacos configuration imported
- [ ] Gateway service running (port 8080)
- [ ] User-service running (port 8081)
- [ ] Bank-service running (port 8082)
- [ ] Chatbot-service running (port 8083)
- [ ] Frontend service running (port 80)

### 7.4 Performance Testing Recommendations

#### 7.4.1 Concurrent User Testing

1. Open multiple sessions using different browsers or incognito mode
2. Execute same operations simultaneously (e.g., transfers)
3. Observe system response times and logs

#### 7.4.2 Large Data Volume Testing

1. Create large volumes of transaction records
2. Test transaction query performance
3. Monitor database and memory usage

#### 7.4.3 Chatbot Stress Testing

1. Send large volumes of consecutive requests
2. Test AI response latency
3. Monitor system resource usage

### 7.5 Log Locations

| Component | Log Location |
|-----------|--------------|
| MySQL | `docker logs mysql` |
| Redis | `docker logs redis` |
| Nacos | `docker logs nacos` |
| Gateway | Terminal output |
| Bank-service | Terminal output |
| User-service | Terminal output |
| Chatbot-service | Terminal output |

### 7.6 Troubleshooting Commands

```bash
# View all running containers
docker ps

# View specific container logs
docker logs <container_name>

# Restart container
docker restart <container_name>

# Stop all containers
docker stop $(docker ps -q)

# View port usage (Windows)
netstat -ano | findstr :<port>

# View port usage (macOS/Linux)
lsof -i :<port>
```

### 7.7 Contact Support

If you encounter issues not covered in this manual:
1. Check project README.md file
2. View GitHub Issues
3. Contact project maintainers

---

**End of Document**

> **Tip:** Please insert actual screenshots at the marked screenshot positions so users can visually understand the expected results of each step.
