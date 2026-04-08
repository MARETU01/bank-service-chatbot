# Bank Service Chatbot

## Requirements

- Java 21
- Node.js 24+ and npm 11+
- Docker
- Maven 3.8+

## Startup Steps

### 1. Start Infrastructure

```bash
# Start MySQL
cd docker/mysql
./start.sh

# Check MySQL startup logs (wait for MySQL to start successfully)
docker logs -f mysql
```

Press `Ctrl+C` to exit log view.

```bash
# Start Redis
cd ../redis
./start.sh

# Start Nacos (Nacos depends on MySQL, ensure MySQL is running first)
cd ../nacos
./start.sh
```

> **Note:**
> 1. Nacos uses MySQL for persistent storage. MySQL must be running before starting Nacos.
> 2. It is recommended to test MySQL connection using database tools (such as Navicat, DataGrip, etc.) before starting Nacos:
>    - Host: `localhost` or server IP
>    - Port: `3306`
>    - Username: `root`
>    - Password: `maretu`
> 3. Confirm MySQL connection is successful before starting Nacos.

### 2. Configure Nacos

1. Access Nacos console in browser: `http://localhost:8848/nacos`
2. Set default credentials (username/password: `nacos/maretu`)
3. Log in to Nacos with the credentials
4. Upload configuration file: Go to Configuration Management page and upload `/docker/nacos_config.zip` to import configurations

### 3. Configure Backend Services

Set the `DOCKER_SERVER_ADDR` environment variable to configure service addresses:

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

> **Note:** Replace `192.168.1.1` with your actual Docker server IP address (such as your local network IP). If the environment variable is not set, `ipv6.maretu.top` will be used as the default server address (may not be reliable).

### 3.5. Configure Chatbot Service (Optional)

Modify `backend/chatbot-service/src/main/resources/application.yaml` to set your own OpenAI API configuration:

```yaml
spring:
  ai:
    openai:
      api-key: "your-api-key"  # Replace with your API key
      base-url: "your-api-base-url"  # Replace with your API base URL
      chat:
        options:
          model: "deepseek-ai/DeepSeek-V3.2"  # Can be changed as needed
```

> **Note:** The default API key and base-url may have call limits, rate limits, or connection issues. It is recommended to replace them with your own API key and API base URL.

### 4. Start Backend Services

First, compile the backend project:

```bash
cd backend
mvn clean install
```

Then start each service:

```bash
# Method 1: Start each service module using IDEA
# Run the main application classes in sequence:
# - GatewayApplication
# - BankServiceApplication
# - UserServiceApplication
# - ChatbotServiceApplication

# Method 2: Start using Maven commands
cd backend/gateway
mvn spring-boot:run

# Open a new terminal and start bank-service
cd backend/bank-service
mvn spring-boot:run

# Open a new terminal and start user-service
cd backend/user-service
mvn spring-boot:run

# Open a new terminal and start chatbot-service
cd backend/chatbot-service
mvn spring-boot:run
```

### 5. Configure Frontend

Modify `frontend/src/http.js` to set the `baseURL` to the backend Gateway address:

```javascript
const instance = axios.create({
    baseURL: 'http://192.168.1.1:8080',  // Replace with actual server IP address
    // ...
})
```

> **Note:** Gateway default port is `8080`. Replace `192.168.1.1` with your actual server IP address.

### 6. Start Frontend

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run serve
```

The frontend application will be available at `http://localhost:80`.

## Default Configuration

- **MySQL Password**: maretu
- **MySQL Port**: 3306
- **Nacos Port**: 8848
- **Gateway Port**: 8080
- **User-service Port**: 8081
- **Bank-service Port**: 8082
- **Chat-service Port**: 8083
- **Front-end Port**: 80

## Default Login Credentials

After starting the application, you can log in with the default admin account:

- **Email**: `admin@bank.com`
- **Password**: `admin123`
