<template>
  <div class="register-page">
    <div class="register-wrapper">
      <div class="register-card">
        <div class="register-header">
          <router-link to="/" class="home-link" title="返回首页">
            <span class="home-icon">🏠</span>
            <span>返回首页</span>
          </router-link>
          <h1>🏦 银行服务机器人</h1>
          <p>创建新账号</p>
        </div>
        <form @submit.prevent="handleRegister">
          <div class="form-group">
            <label for="register-username">用户名</label>
            <input 
              v-model="registerForm.username" 
              type="text" 
              id="register-username" 
              placeholder="请设置用户名"
              required
            >
          </div>
          <div class="form-group">
            <label for="register-realname">真实姓名</label>
            <input 
              v-model="registerForm.realName" 
              type="text" 
              id="register-realname" 
              placeholder="请输入真实姓名"
              required
            >
          </div>
          <div class="form-group">
            <label for="register-email">邮箱</label>
            <input 
              v-model="registerForm.email" 
              type="email" 
              id="register-email" 
              placeholder="请输入邮箱"
              required
              :class="{ 'input-error': emailError }"
              @blur="validateEmail"
              @input="validateEmail"
            >
            <span v-if="emailError" class="error-message">{{ emailError }}</span>
          </div>
          <div class="form-group">
            <label for="register-phone">手机号</label>
            <input 
              v-model="registerForm.phone" 
              type="tel" 
              id="register-phone" 
              placeholder="请输入手机号"
              required
            >
          </div>
          <div class="form-group">
            <label for="register-password">密码</label>
            <input 
              v-model="registerForm.password" 
              type="password" 
              id="register-password" 
              placeholder="请设置密码"
              required
              :class="{ 'input-error': passwordError }"
              @blur="validatePassword"
              @input="validatePassword"
            >
            <span v-if="passwordError" class="error-message">{{ passwordError }}</span>
          </div>
          <div class="form-group">
            <label for="register-confirm-password">确认密码</label>
            <input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              id="register-confirm-password" 
              placeholder="请再次输入密码"
              required
              :class="{ 'input-error': confirmPasswordError }"
              @blur="validateConfirmPassword"
              @input="validateConfirmPassword"
            >
            <span v-if="confirmPasswordError" class="error-message">{{ confirmPasswordError }}</span>
          </div>
          <div class="form-group verification-group">
            <label for="register-verification">验证码</label>
            <div class="verification-input-wrapper">
              <input 
                v-model="verification" 
                type="text" 
                id="register-verification" 
                placeholder="请输入验证码"
                required
              >
              <button 
                type="button" 
                @click="sendVerificationCode" 
                class="verification-btn"
                :disabled="countdown > 0"
              >{{ countdown > 0 ? `重新发送 (${countdown})` : '获取验证码' }}</button>
            </div>
          </div>
          <button type="submit" class="register-btn">注册</button>
        </form>
        <div class="register-footer">
          <p>已有账号？<router-link to="/login">返回登录</router-link></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      registerForm: {
        username: '',
        realName: '',
        email: '',
        phone: '',
        password: '',
        confirmPassword: ''
      },
      verification: '',
      isSendingCode: false,
      countdown: 0,
      emailError: '',
      passwordError: '',
      confirmPasswordError: ''
    }
  },
  methods: {
    validateEmail() {
      const email = this.registerForm.email;
      if (!email) {
        this.emailError = '';
        return;
      }
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(email)) {
        this.emailError = '请输入正确的邮箱格式';
      } else {
        this.emailError = '';
      }
    },
    
    validatePassword() {
      const password = this.registerForm.password;
      if (!password) {
        this.passwordError = '';
        return;
      }
      if (password.length < 6) {
        this.passwordError = '密码长度不能少于 6 位';
      } else {
        this.passwordError = '';
      }
    },
    
    validateConfirmPassword() {
      const password = this.registerForm.password;
      const confirmPassword = this.registerForm.confirmPassword;
      if (!confirmPassword) {
        this.confirmPasswordError = '';
        return;
      }
      if (password !== confirmPassword) {
        this.confirmPasswordError = '两次输入的密码不一致';
      } else {
        this.confirmPasswordError = '';
      }
    },
    
    sendVerificationCode() {
      // 如果正在倒计时，直接返回
      if (this.countdown > 0) return;
      
      // 验证邮箱
      this.validateEmail();
      if (this.emailError) {
        return;
      }

      this.$http.post('/users/code', {
        email: this.registerForm.email
      }, {
        params: {
          type: 'register'
        }
      })
        .then(response => {
          if (response.data.code === 1) {
            alert(response.data.message || '验证码发送成功');
            this.startCountdown();
          } else {
            alert(response.data.message || '验证码发送失败');
          }
        })
        .catch(error => {
          console.error('验证码请求错误:', error);
          alert('网络错误，请稍后重试');
        });
    },
    
    startCountdown() {
      this.countdown = 60;
      const timer = setInterval(() => {
        this.countdown--;
        if (this.countdown <= 0) {
          clearInterval(timer);
        }
      }, 1000);
    },
    
    handleRegister() {
      // 验证所有字段
      this.validateEmail();
      this.validatePassword();
      this.validateConfirmPassword();
      
      if (this.emailError || this.passwordError || this.confirmPasswordError) {
        return;
      }

      const payload = {
        username: this.registerForm.username,
        realName: this.registerForm.realName,
        password: this.registerForm.password,
        email: this.registerForm.email,
        phone: this.registerForm.phone
      };

      this.$http.post('/users/register', payload, {
        params: {
          verifyCode: this.verification
        }
      })
        .then(response => {
          console.log('注册返回数据:', response.data);
          if (response.data.code === 1) {
            if (confirm(response.data.message || '注册成功，请登录')) { // 带确认按钮的弹窗
              this.$router.push('/login');
            }
          } else {
            alert(response.data.message || '注册失败');
          }
        })
        .catch(error => {
          console.error('注册请求错误:', error);
          alert(error.response?.data?.message || '注册失败，请检查网络后重试');
        });
    }
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-wrapper {
  width: 100%;
  max-width: 480px;
}

.register-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  padding: 45px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.register-header {
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

.register-header h1 {
  font-size: 26px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px 0;
}

.register-header p {
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

.verification-group {
  margin-bottom: 24px;
}

.verification-input-wrapper {
  display: flex;
  gap: 10px;
}

.verification-input-wrapper input {
  flex: 1;
  min-width: 0;
}

.verification-btn {
  padding: 11px 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
  flex-shrink: 0;
}

.verification-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.verification-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

.register-btn {
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

.register-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.register-btn:active {
  transform: translateY(0);
}

.register-footer {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  text-align: center;
}

.register-footer p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.register-footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s;
}

.register-footer a:hover {
  color: #764ba2;
  text-decoration: none;
}
</style>
