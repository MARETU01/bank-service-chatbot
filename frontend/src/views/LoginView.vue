<template>
  <div class="login-page">
    <div class="login-container">
      <!-- Left Brand Section -->
      <div class="login-brand">
        <div class="brand-content">
          <div class="brand-logo">🏦</div>
          <h1>Bank Service Chatbot</h1>
          <p class="brand-slogan">Intelligent Service · Secure & Convenient · Always Here</p>
          <p class="brand-description">
            Experience a new way of banking with our AI-powered assistant available 24/7,
            providing professional, efficient, and secure financial services.
          </p>
          <div class="brand-features">
            <div class="feature-item">
              <span class="feature-icon">🔒</span>
              <span>Secure & Encrypted</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">⚡</span>
              <span>Fast Response</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🤖</span>
              <span>AI-Powered</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🌟</span>
              <span>Professional Service</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Right Form Section -->
      <div class="login-form-section">
        <div class="login-form-wrapper">
          <div class="form-header">
            <router-link to="/" class="home-link" title="Back to Home">
              <span class="home-icon">←</span>
              <span>Back to Home</span>
            </router-link>
          </div>
          
          <div class="form-title-section">
            <h1>Welcome Back</h1>
            <p>Sign in to your account to start intelligent banking services</p>
          </div>
          
          <form @submit.prevent="handleLogin">
            <div class="form-group">
              <label for="login-account">
                <span class="label-icon">📱</span>
                Phone / Email
                <span class="required">*</span>
              </label>
              <div class="input-wrapper">
                <input 
                  v-model="loginForm.account" 
                  type="text" 
                  id="login-account" 
                  placeholder="Enter phone number or email"
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
                Password
                <span class="required">*</span>
              </label>
              <div class="input-wrapper password-input">
                <input 
                  v-model="loginForm.password" 
                  :type="showPassword ? 'text' : 'password'" 
                  id="login-password" 
                  placeholder="Enter password"
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
                Forgot Password?
              </router-link>
            </div>
            
            <button type="submit" class="login-btn" :disabled="loading">
              <span v-if="loading" class="loading-spinner"></span>
              <span>{{ loading ? 'Signing in...' : 'Sign In' }}</span>
            </button>
          </form>
          
          <div class="login-footer">
            <p>Don't have an account? <router-link to="/register">Register Now</router-link></p>
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
      this.accountError = 'Please enter a valid phone number or email address';
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
          this.$message.error(result.message || 'Login failed, please try again');
        }
      } catch (error) {
        console.error('Login error:', error);
        this.$message.error('Network error, please try again later');
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
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl);
}

.login-container {
  width: 100%;
  max-width: 1100px;
  background: var(--color-white);
  border-radius: var(--radius-3xl);
  box-shadow: var(--shadow-2xl);
  overflow: hidden;
  display: flex;
  min-height: 650px;
}

/* 左侧品牌区域 */
.login-brand {
  flex: 1;
  background: var(--gradient-primary);
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
  color: var(--color-white);
  text-align: center;
}

.brand-logo {
  font-size: 80px;
  margin-bottom: var(--spacing-2xl);
  filter: drop-shadow(0 4px 10px rgba(0,0,0,0.2));
}

.brand-content h1 {
  font-size: var(--font-size-6xl);
  font-weight: var(--font-weight-bold);
  margin: 0 0 var(--spacing-lg) 0;
  letter-spacing: 2px;
}

.brand-slogan {
  font-size: var(--font-size-lg);
  opacity: 0.9;
  margin: 0 0 var(--spacing-3xl) 0;
  font-weight: var(--font-weight-normal);
}

.brand-description {
  font-size: var(--font-size-md);
  opacity: 0.8;
  line-height: var(--line-height-relaxed);
  margin: 0 0 var(--spacing-4xl) 0;
  max-width: 350px;
}

.brand-features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-2xl);
  justify-items: center;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
  background: var(--glass-bg);
  padding: var(--spacing-lg) var(--spacing-2xl);
  border-radius: var(--radius-2xl);
  backdrop-filter: var(--glass-blur);
  transition: transform var(--transition-normal), background var(--transition-normal);
}

.feature-item:hover {
  transform: translateY(-5px);
  background: var(--glass-bg-hover);
}

.feature-icon {
  font-size: var(--font-size-5xl);
}

.feature-item span:last-child {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
}

/* 右侧表单区域 */
.login-form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-5xl);
  background: var(--color-white);
}

.login-form-wrapper {
  width: 100%;
  max-width: 380px;
}

.form-header {
  margin-bottom: var(--spacing-3xl);
}

.home-link {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--text-secondary);
  text-decoration: none;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  padding: var(--spacing-sm) var(--spacing-lg);
  border-radius: var(--radius-full);
  transition: all var(--transition-normal);
  background: var(--color-gray-50);
}

.home-link:hover {
  background: var(--color-primary-light);
  color: var(--color-white);
  transform: translateX(-3px);
}

.home-icon {
  font-size: var(--font-size-2xl);
  font-weight: bold;
}

.form-title-section {
  margin-bottom: 35px;
}

.form-title-section h1 {
  font-size: var(--font-size-6xl);
  font-weight: var(--font-weight-bold);
  color: #1a1a2e;
  margin: 0 0 var(--spacing-lg) 0;
}

.form-title-section p {
  color: var(--text-tertiary);
  margin: 0;
  font-size: var(--font-size-lg);
}

/* 表单样式 */
.form-group {
  margin-bottom: var(--spacing-2xl);
}

.form-group label {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-sm);
  font-size: var(--font-size-md);
  color: var(--text-primary);
  font-weight: var(--font-weight-semibold);
}

.label-icon {
  font-size: var(--font-size-xl);
}

.required {
  color: var(--color-danger);
  margin-left: var(--spacing-xs);
}

.input-wrapper {
  position: relative;
}

.input-wrapper input {
  width: 100%;
  padding: 14px var(--spacing-lg);
  border: 2px solid var(--color-gray-200);
  border-radius: var(--radius-xl);
  font-size: var(--font-size-lg);
  transition: all var(--transition-normal);
  box-sizing: border-box;
  background: var(--color-gray-50);
}

.input-wrapper input:focus {
  background: var(--color-white);
  border-color: var(--color-primary-light);
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  outline: none;
}

.input-wrapper input::placeholder {
  color: var(--color-gray-400);
}

.input-wrapper input.input-error {
  border-color: var(--color-danger);
  background: var(--color-danger-light);
}

.password-input input {
  padding-right: 50px;
}

.password-toggle {
  position: absolute;
  right: var(--spacing-lg);
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  font-size: var(--font-size-2xl);
  padding: var(--spacing-xs);
  opacity: 0.6;
  transition: opacity var(--transition-normal);
}

.password-toggle:hover {
  opacity: 1;
}

/* 错误提示 */
.error-message {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--color-danger);
  font-size: var(--font-size-xs);
  margin-top: var(--spacing-sm);
  padding-left: var(--spacing-xs);
}

.error-icon {
  font-size: var(--font-size-md);
}

/* 表单选项 */
.form-options {
  margin-bottom: var(--spacing-2xl);
  text-align: right;
}

.forgot-link {
  color: var(--color-primary-light);
  text-decoration: none;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  transition: color var(--transition-normal);
}

.forgot-link:hover {
  color: #764ba2;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  padding: var(--spacing-lg);
  background: var(--gradient-primary);
  color: var(--color-white);
  border: none;
  border-radius: var(--radius-xl);
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  cursor: pointer;
  transition: all var(--transition-normal);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-lg);
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
  border-top-color: var(--color-white);
  border-radius: var(--radius-full);
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 页脚 */
.login-footer {
  margin-top: var(--spacing-2xl);
  text-align: center;
}

.login-footer p {
  margin: 0;
  color: var(--text-secondary);
  font-size: var(--font-size-md);
}

.login-footer a {
  color: var(--color-primary-light);
  text-decoration: none;
  font-weight: var(--font-weight-semibold);
  transition: color var(--transition-normal);
}

.login-footer a:hover {
  color: #764ba2;
}

/* Fade 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: all var(--transition-normal);
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
    padding: var(--spacing-4xl) var(--spacing-3xl);
    min-height: auto;
  }
  
  .brand-logo {
    font-size: 60px;
  }
  
  .brand-content h1 {
    font-size: var(--font-size-4xl);
  }
  
  .brand-features {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .feature-item {
    padding: var(--spacing-lg) var(--spacing-lg);
  }
  
  .feature-icon {
    font-size: var(--font-size-4xl);
  }
  
  .feature-item span:last-child {
    font-size: var(--font-size-xs);
  }
  
  .login-form-section {
    padding: var(--spacing-4xl) var(--spacing-3xl);
  }
}

@media (max-width: 500px) {
  .login-page {
    padding: var(--spacing-lg);
  }
  
  .brand-features {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .form-title-section h1 {
    font-size: var(--font-size-4xl);
  }
  
  .form-options {
    text-align: center;
  }
}
</style>
