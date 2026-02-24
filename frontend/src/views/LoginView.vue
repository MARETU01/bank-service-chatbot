<template>
  <div class="login-page">
    <div class="login-wrapper">
      <div class="login-card">
        <div class="login-header">
          <router-link to="/" class="home-link" title="返回首页">
            <span class="home-icon">🏠</span>
            <span>返回首页</span>
          </router-link>
          <h1>🏦 银行服务机器人</h1>
          <p>欢迎登录</p>
        </div>
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="login-account">手机号 / 邮箱</label>
            <input 
              v-model="loginForm.account" 
              type="text" 
              id="login-account" 
              placeholder="请输入手机号或邮箱"
              required
              :class="{ 'input-error': accountError }"
              @blur="validateAccount"
              @input="validateAccount"
            >
            <span v-if="accountError" class="error-message">{{ accountError }}</span>
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
        account: '',
        password: ''
      },
      accountError: ''
    }
  },
  methods: {
    // 验证输入是否为邮箱格式
    isEmail(str) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return emailRegex.test(str);
    },
    // 验证输入是否为手机号格式（支持国际手机号）
    isPhone(str) {
      // 只验证是否为纯数字，长度在 7-15 位之间（国际手机号标准）
      const phoneRegex = /^\d{7,15}$/;
      return phoneRegex.test(str);
    },
    // 验证账号输入（只验证邮箱格式，不验证手机号）
    validateAccount() {
      const account = this.loginForm.account;
      if (!account) {
        this.accountError = '';
        return;
      }
      // 如果是邮箱格式，验证通过
      if (this.isEmail(account)) {
        this.accountError = '';
        return;
      }
      // 如果是手机号（纯数字 7-15 位），验证通过
      if (this.isPhone(account)) {
        this.accountError = '';
        return;
      }
      // 既不是邮箱也不是手机号，显示错误
      this.accountError = '请输入有效的手机号或邮箱地址';
    },
    handleLogin() {
      // 验证输入格式
      this.validateAccount();
      if (this.accountError) {
        return;
      }
      
      // 根据输入类型设置对应的登录参数
      const loginData = {
        password: this.loginForm.password
      };
      if (this.isEmail(this.loginForm.account)) {
        loginData.email = this.loginForm.account;
      } else {
        loginData.phone = this.loginForm.account;
      }
      
      this.$http.post('/users/login', loginData)
        .then(response => {
          console.log('Login response:', response.data);
          if (response.data.code === 1) {
            // 登录成功，存储 token 到 localStorage
            localStorage.setItem('token', response.data.data);
            
            alert('登录成功');
            // 跳转到根路径'/'
            this.$router.push('/dashboard');
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  padding: 45px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.home-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.3s;
  background: rgba(102, 126, 234, 0.1);
  margin-bottom: 20px;
}

.home-link:hover {
  background: rgba(102, 126, 234, 0.2);
  color: #764ba2;
  transform: translateX(-2px);
}

.home-icon {
  font-size: 16px;
}

.login-header h1 {
  font-size: 26px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px 0;
}

.login-header p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.form-group {
  margin-bottom: 18px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: #555;
  font-weight: 500;
  text-align: left;
  padding-left: 4px;
}

.form-group input {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s, box-shadow 0.3s;
  box-sizing: border-box;
  background: #fafafa;
}

.form-group input:focus {
  background: white;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.form-group input::placeholder {
  color: #bbb;
}

.form-group input.input-error {
  border-color: #f44336;
  background: #ffebee;
}

.form-group input.input-error:focus {
  border-color: #f44336;
  box-shadow: 0 0 0 3px rgba(244, 67, 54, 0.15);
}

.error-message {
  display: block;
  color: #f44336;
  font-size: 12px;
  margin-top: 4px;
  padding-left: 4px;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 12px;
}

.login-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.login-footer {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  text-align: center;
}

.login-footer p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.login-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s;
}

.login-footer a:hover {
  color: #764ba2;
  text-decoration: none;
}
</style>
