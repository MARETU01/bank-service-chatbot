<template>
  <div class="login-page">
    <div class="login-wrapper">
      <div class="login-card">
        <div class="login-header">
          <h1>🏦 银行服务机器人</h1>
          <p>欢迎登录</p>
        </div>
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="login-email">邮箱</label>
            <input 
              v-model="loginForm.email" 
              type="text" 
              id="login-email" 
              placeholder="请输入邮箱"
              required
            >
          </div>
          <div class="form-group">
            <label for="login-password">密码</label>
            <input 
              v-model="loginForm.password" 
              type="password" 
              id="login-password" 
              placeholder="请输入密码"
              required
            >
          </div>
          <button type="submit" class="login-btn">登录</button>
        </form>
        <div class="login-footer">
          <p>还没有账号？<router-link to="/register">立即注册</router-link></p>
          <p><router-link to="/reset-password">忘记密码？</router-link></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      loginForm: {
        email: '',
        password: ''
      }
    }
  },
  methods: {
    handleLogin() {
      this.$http.post('/users/login', this.loginForm)
        .then(response => {
          console.log('Login response:', response.data);
          if (response.data.code === 1) {
            // 登录成功，存储 token 到 localStorage
            localStorage.setItem('token', response.data.data);
            
            alert('登录成功');
            // 跳转到根路径'/'
            this.$router.push('/');
          } else {
            // 登录失败，显示错误信息
            alert(response.data.message || '登录失败，请重试');
          }
        })
        .catch(error => {
          console.error('Login error:', error);
          alert(error.response?.data?.message || '网络错误，请稍后重试');
        });
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a87 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-wrapper {
  width: 100%;
  max-width: 420px;
}

.login-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 {
  font-size: 24px;
  color: #1e3a5f;
  margin: 0 0 8px 0;
}

.login-header p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s, box-shadow 0.3s;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #1e3a5f;
  box-shadow: 0 0 0 3px rgba(30, 58, 95, 0.1);
}

.form-group input::placeholder {
  color: #999;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a87 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 58, 95, 0.3);
}

.login-btn:active {
  transform: translateY(0);
}

.login-footer {
  margin-top: 24px;
  text-align: center;
}

.login-footer p {
  margin: 12px 0 0 0;
  color: #666;
  font-size: 14px;
}

.login-footer p:first-child {
  margin-top: 0;
}

.login-footer a {
  color: #1e3a5f;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.login-footer a:hover {
  color: #2d5a87;
  text-decoration: underline;
}
</style>
