<template>
  <div class="register-page">
    <div class="register-wrapper">
      <div class="register-card">
        <div class="register-header">
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
            >
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
            >
          </div>
          <div class="form-group">
            <label for="register-confirm-password">确认密码</label>
            <input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              id="register-confirm-password" 
              placeholder="请再次输入密码"
              required
            >
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
                ref="verificationBtn"
                class="verification-btn"
              >获取验证码</button>
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
      isSendingCode: false
    }
  },
  methods: {
    sendVerificationCode() {
      console.log('1. 方法开始执行');
      if (this.isSendingCode) return;
      this.isSendingCode = true;
      console.log('2. 防重复点击逻辑通过');

      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        alert('两次密码不一致'); // 原生 alert
        this.isSendingCode = false;
        return;
      }
      console.log('3. 密码验证通过，准备发送请求');

      this.$http.post('/users/register/code', this.registerForm)
        .then(response => {
          console.log('验证码返回数据:', response.data);
          if (response.data.code === 1) {
            alert(response.data.message || '验证码发送成功'); // 成功提示
            this.startCountdown();
          } else {
            alert(response.data.message || '验证码发送失败'); // 失败提示
          }
        })
        .catch(error => {
          console.error('验证码请求错误:', error);
          alert('网络错误，请稍后重试');
        })
        .finally(() => {
          this.isSendingCode = false;
        });
    },
    
    startCountdown() {
      let countdown = 60;
      const btn = this.$refs.verificationBtn;
      btn.disabled = true;
      const timer = setInterval(() => {
        btn.textContent = `重新发送 (${countdown})`;
        countdown--;
        if (countdown < 0) {
          clearInterval(timer);
          btn.disabled = false;
          btn.textContent = '获取验证码';
        }
      }, 1000);
    },
    
    handleRegister() {
      if (this.registerForm.password !== this.registerForm.confirmPassword) {
        alert('两次密码不一致');
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
          verification: this.verification
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
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a87 100%);
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
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  padding: 40px;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-header h1 {
  font-size: 24px;
  color: #1e3a5f;
  margin: 0 0 8px 0;
}

.register-header p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.form-group {
  margin-bottom: 20px;
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

.verification-group {
  margin-bottom: 24px;
}

.verification-input-wrapper {
  display: flex;
  gap: 12px;
}

.verification-input-wrapper input {
  flex: 1;
}

.verification-btn {
  padding: 12px 20px;
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a87 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  white-space: nowrap;
}

.verification-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 58, 95, 0.3);
}

.verification-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

.register-btn {
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
  margin-top: 8px;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 58, 95, 0.3);
}

.register-btn:active {
  transform: translateY(0);
}

.register-footer {
  margin-top: 24px;
  text-align: center;
}

.register-footer p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.register-footer a {
  color: #1e3a5f;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.register-footer a:hover {
  color: #2d5a87;
  text-decoration: underline;
}
</style>
