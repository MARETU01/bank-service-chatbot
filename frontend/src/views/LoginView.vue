<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧品牌区域 -->
      <div class="login-brand">
        <div class="brand-content">
          <div class="brand-logo">🏦</div>
          <h1>银行服务机器人</h1>
          <p class="brand-slogan">智能服务 · 安全便捷 · 随时相伴</p>
          <p class="brand-description">
            体验全新的银行服务方式，AI 智能助手 24 小时在线，
            为您提供专业、高效、安全的金融服务。
          </p>
          <div class="brand-features">
            <div class="feature-item">
              <span class="feature-icon">🔒</span>
              <span>安全加密</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">⚡</span>
              <span>极速响应</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🤖</span>
              <span>AI 智能</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🌟</span>
              <span>专业服务</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧表单区域 -->
      <div class="login-form-section">
        <div class="login-form-wrapper">
          <div class="form-header">
            <router-link to="/" class="home-link" title="返回首页">
              <span class="home-icon">←</span>
              <span>返回首页</span>
            </router-link>
          </div>
          
          <div class="form-title-section">
            <h1>欢迎回来</h1>
            <p>登录您的账户，开启智能银行服务</p>
          </div>
          
          <form @submit.prevent="handleLogin">
            <div class="form-group">
              <label for="login-account">
                <span class="label-icon">📱</span>
                手机号 / 邮箱
                <span class="required">*</span>
              </label>
              <div class="input-wrapper">
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
              </div>
              <transition name="fade">
                <span v-if="accountError" class="error-message">
                  <span class="error-icon">⚠️</span> {{ accountError }}
                </span>
              </transition>
            </div>
            
            <div class="form-group">
              <label for="login-password">
                <span class="label-icon">🔒</span>
                密码
                <span class="required">*</span>
              </label>
              <div class="input-wrapper password-input">
                <input 
                  v-model="loginForm.password" 
                  :type="showPassword ? 'text' : 'password'" 
                  id="login-password" 
                  placeholder="请输入密码"
                  required
                >
                <button 
                  type="button" 
                  class="password-toggle"
                  @click="showPassword = !showPassword"
                  tabindex="-1"
                >
                  {{ showPassword ? '🙈' : '👁️' }}
                </button>
              </div>
            </div>
            
            <div class="form-options">
              <router-link to="/reset-password" class="forgot-link">
                忘记密码？
              </router-link>
            </div>
            
            <button type="submit" class="login-btn" :disabled="loading">
              <span v-if="loading" class="loading-spinner"></span>
              <span>{{ loading ? '登录中...' : '立即登录' }}</span>
            </button>
          </form>
          
          <div class="login-footer">
            <p>还没有账号？<router-link to="/register">立即注册</router-link></p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'

export default {
  data() {
    return {
      loginForm: {
        account: '',
        password: ''
      },
      accountError: '',
      showPassword: false
    }
  },
  computed: {
    ...mapState(['loading'])
  },
  methods: {
    isEmail(str) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return emailRegex.test(str);
    },
    isPhone(str) {
      const phoneRegex = /^\d{11}$/;
      return phoneRegex.test(str);
    },
    validateAccount() {
      const account = this.loginForm.account;
      if (!account) {
        this.accountError = '';
        return;
      }
      if (this.isEmail(account)) {
        this.accountError = '';
        return;
      }
      if (this.isPhone(account)) {
        this.accountError = '';
        return;
      }
      this.accountError = '请输入有效的手机号或邮箱地址';
    },
    async handleLogin() {
      this.validateAccount();
      if (this.accountError) {
        return;
      }
      
      const loginData = {
        password: this.loginForm.password
      };
      if (this.isEmail(this.loginForm.account)) {
        loginData.email = this.loginForm.account;
      } else {
        loginData.phone = this.loginForm.account;
      }
      
      try {
        const result = await this.$store.dispatch('login', loginData);
        
        if (result.success) {
          // 登录成功，获取 redirect 参数或跳转到仪表盘
          const redirect = this.$route.query.redirect || '/dashboard';
          this.$router.push(redirect);
        } else {
          this.$message.error(result.message || '登录失败，请重试');
        }
      } catch (error) {
        console.error('Login error:', error);
        this.$message.error('网络错误，请稍后重试');
      }
    }
  }
}
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 1100px;
  background: white;
  border-radius: 24px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  display: flex;
  min-height: 650px;
}

/* 左侧品牌区域 */
.login-brand {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 50px;
  position: relative;
  overflow: hidden;
}

.login-brand::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: pulse 15s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1) rotate(0deg); }
  50% { transform: scale(1.1) rotate(180deg); }
}

.brand-content {
  position: relative;
  z-index: 1;
  color: white;
  text-align: center;
}

.brand-logo {
  font-size: 80px;
  margin-bottom: 20px;
  filter: drop-shadow(0 4px 10px rgba(0,0,0,0.2));
}

.brand-content h1 {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 15px 0;
  letter-spacing: 2px;
}

.brand-slogan {
  font-size: 16px;
  opacity: 0.9;
  margin: 0 0 30px 0;
  font-weight: 300;
}

.brand-description {
  font-size: 14px;
  opacity: 0.8;
  line-height: 1.8;
  margin: 0 0 40px 0;
  max-width: 350px;
}

.brand-features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  justify-items: center;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.15);
  padding: 15px 25px;
  border-radius: 16px;
  backdrop-filter: blur(10px);
  transition: transform 0.3s, background 0.3s;
}

.feature-item:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.25);
}

.feature-icon {
  font-size: 28px;
}

.feature-item span:last-child {
  font-size: 13px;
  font-weight: 500;
}

/* 右侧表单区域 */
.login-form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 50px;
  background: white;
}

.login-form-wrapper {
  width: 100%;
  max-width: 380px;
}

.form-header {
  margin-bottom: 30px;
}

.home-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #666;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.3s;
  background: #f5f5f5;
}

.home-link:hover {
  background: #667eea;
  color: white;
  transform: translateX(-3px);
}

.home-icon {
  font-size: 18px;
  font-weight: bold;
}

.form-title-section {
  margin-bottom: 35px;
}

.form-title-section h1 {
  font-size: 32px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 10px 0;
}

.form-title-section p {
  color: #888;
  margin: 0;
  font-size: 15px;
}

/* 表单样式 */
.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
  font-weight: 600;
}

.label-icon {
  font-size: 16px;
}

.required {
  color: #e74c3c;
  margin-left: 2px;
}

.input-wrapper {
  position: relative;
}

.input-wrapper input {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  font-size: 15px;
  transition: all 0.3s;
  box-sizing: border-box;
  background: #fafafa;
}

.input-wrapper input:focus {
  background: white;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  outline: none;
}

.input-wrapper input::placeholder {
  color: #aaa;
}

.input-wrapper input.input-error {
  border-color: #e74c3c;
  background: #fef5f5;
}

.password-input input {
  padding-right: 50px;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  font-size: 20px;
  padding: 5px;
  opacity: 0.6;
  transition: opacity 0.3s;
}

.password-toggle:hover {
  opacity: 1;
}

/* 错误提示 */
.error-message {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #e74c3c;
  font-size: 13px;
  margin-top: 8px;
  padding-left: 4px;
}

.error-icon {
  font-size: 14px;
}

/* 表单选项 */
.form-options {
  margin-bottom: 25px;
  text-align: right;
}

.forgot-link {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.3s;
}

.forgot-link:hover {
  color: #764ba2;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 页脚 */
.login-footer {
  margin-top: 25px;
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
  transition: color 0.3s;
}

.login-footer a:hover {
  color: #764ba2;
}

/* Fade 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}

/* 响应式设计 */
@media (max-width: 900px) {
  .login-container {
    flex-direction: column;
    max-width: 500px;
  }
  
  .login-brand {
    padding: 40px 30px;
    min-height: auto;
  }
  
  .brand-logo {
    font-size: 60px;
  }
  
  .brand-content h1 {
    font-size: 26px;
  }
  
  .brand-features {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .feature-item {
    padding: 10px 15px;
  }
  
  .feature-icon {
    font-size: 24px;
  }
  
  .feature-item span:last-child {
    font-size: 12px;
  }
  
  .login-form-section {
    padding: 40px 30px;
  }
}

@media (max-width: 500px) {
  .login-page {
    padding: 10px;
  }
  
  .brand-features {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .form-title-section h1 {
    font-size: 26px;
  }
  
  .form-options {
    text-align: center;
  }
}
</style>
