<template>
  <div class="register-page">
    <div class="register-container">
      <!-- 左侧品牌区域 -->
      <div class="register-brand">
        <div class="brand-content">
          <div class="brand-logo">🏦</div>
          <h1>银行服务机器人</h1>
          <p class="brand-slogan">智能服务 · 安全便捷 · 随时相伴</p>
          <p class="brand-description">
            立即注册，体验 AI 驱动的全新银行服务，
            让金融服务变得更简单、更智能。
          </p>
          <div class="brand-features">
            <div class="feature-item">
              <span class="feature-icon">🎯</span>
              <span>快速开户</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🛡️</span>
              <span>资金安全</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">📊</span>
              <span>智能理财</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">💬</span>
              <span>24h 客服</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧表单区域 -->
      <div class="register-form-section">
        <div class="register-form-wrapper">
          <div class="form-header">
            <router-link to="/" class="home-link" title="返回首页">
              <span class="home-icon">←</span>
              <span>返回首页</span>
            </router-link>
          </div>
          
          <div class="form-title-section">
            <h1>创建账户</h1>
            <p>填写以下信息，开启智能银行之旅</p>
          </div>
          
          <form @submit.prevent="handleRegister">
            <!-- 个人信息部分 -->
            <div class="form-section-title">
              <span class="section-number">01</span>
              <span>个人信息</span>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label for="register-username">
                  <span class="label-icon">👤</span>
                  用户名
                  <span class="required">*</span>
                </label>
                <div class="input-wrapper">
                  <input 
                    v-model="registerForm.username" 
                    type="text" 
                    id="register-username" 
                    placeholder="请设置用户名"
                    required
                  >
                </div>
              </div>
              
              <div class="form-group">
                <label for="register-realname">
                  <span class="label-icon">📝</span>
                  真实姓名
                  <span class="required">*</span>
                </label>
                <div class="input-wrapper">
                  <input 
                    v-model="registerForm.realName" 
                    type="text" 
                    id="register-realname" 
                    placeholder="请输入真实姓名"
                    required
                  >
                </div>
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label for="register-email">
                  <span class="label-icon">📧</span>
                  邮箱
                  <span class="required">*</span>
                </label>
                <div class="input-wrapper">
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
                </div>
                <transition name="fade">
                  <span v-if="emailError" class="error-message">
                    <span class="error-icon">⚠️</span> {{ emailError }}
                  </span>
                </transition>
              </div>
              
              <div class="form-group">
                <label for="register-phone">
                  <span class="label-icon">📱</span>
                  手机号
                  <span class="required">*</span>
                </label>
                <div class="input-wrapper">
                  <input 
                    v-model="registerForm.phone" 
                    type="tel" 
                    id="register-phone" 
                    placeholder="请输入手机号"
                    required
                  >
                </div>
              </div>
            </div>
            
            <!-- 账户安全部分 -->
            <div class="form-section-title">
              <span class="section-number">02</span>
              <span>账户安全</span>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label for="register-password">
                  <span class="label-icon">🔒</span>
                  密码
                  <span class="required">*</span>
                </label>
                <div class="input-wrapper password-input">
                  <input 
                    v-model="registerForm.password" 
                    :type="showPassword ? 'text' : 'password'" 
                    id="register-password" 
                    placeholder="请设置密码"
                    required
                    :class="{ 'input-error': passwordError }"
                    @blur="validatePassword"
                    @input="validatePassword"
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
                <transition name="fade">
                  <span v-if="passwordError" class="error-message">
                    <span class="error-icon">⚠️</span> {{ passwordError }}
                  </span>
                </transition>
              </div>
              
              <div class="form-group">
                <label for="register-confirm-password">
                  <span class="label-icon">🔐</span>
                  确认密码
                  <span class="required">*</span>
                </label>
                <div class="input-wrapper password-input">
                  <input 
                    v-model="registerForm.confirmPassword" 
                    :type="showConfirmPassword ? 'text' : 'password'" 
                    id="register-confirm-password" 
                    placeholder="请再次输入密码"
                    required
                    :class="{ 'input-error': confirmPasswordError }"
                    @blur="validateConfirmPassword"
                    @input="validateConfirmPassword"
                  >
                  <button 
                    type="button" 
                    class="password-toggle"
                    @click="showConfirmPassword = !showConfirmPassword"
                    tabindex="-1"
                  >
                    {{ showConfirmPassword ? '🙈' : '👁️' }}
                  </button>
                </div>
                <transition name="fade">
                  <span v-if="confirmPasswordError" class="error-message">
                    <span class="error-icon">⚠️</span> {{ confirmPasswordError }}
                  </span>
                </transition>
              </div>
            </div>
            
            <!-- 验证码 -->
            <div class="form-group verification-group">
              <label for="register-verification">
                <span class="label-icon">🔑</span>
                验证码
                <span class="required">*</span>
              </label>
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
                  :disabled="countdown > 0 || isLoading"
                >
                  {{ countdown > 0 ? `重新发送 (${countdown})` : '获取验证码' }}
                </button>
              </div>
            </div>
            
            <!-- 服务条款 -->
            <div class="terms-checkbox">
              <label class="checkbox-label">
                <input type="checkbox" v-model="agreeTerms">
                <span class="checkmark"></span>
                <span>我已阅读并同意 <a href="#" class="terms-link">《服务条款》</a> 和 <a href="#" class="terms-link">《隐私政策》</a></span>
              </label>
            </div>
            
            <button type="submit" class="register-btn" :disabled="isLoading">
              <span v-if="isLoading" class="loading-spinner"></span>
              <span>{{ isLoading ? '注册中...' : '立即注册' }}</span>
            </button>
          </form>
          
          <div class="register-footer">
            <p>已有账号？<router-link to="/login">返回登录</router-link></p>
          </div>
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
      confirmPasswordError: '',
      showPassword: false,
      showConfirmPassword: false,
      agreeTerms: false,
      isLoading: false
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
    
    async sendVerificationCode() {
      if (this.countdown > 0 || this.isLoading) return;
      
      this.validateEmail();
      if (this.emailError) {
        return;
      }

      this.isLoading = true;
      try {
        const response = await this.$http.post('/users/code', {
          email: this.registerForm.email
        }, {
          params: {
            type: 'register'
          }
        });
        if (response.data.code === 1) {
          this.$message.success(response.data.message || '验证码发送成功');
          this.startCountdown();
        } else {
          this.$message.error(response.data.message || '验证码发送失败');
        }
      } catch (error) {
        console.error('验证码请求错误:', error);
        this.$message.error('网络错误，请稍后重试');
      } finally {
        this.isLoading = false;
      }
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
    
    async handleRegister() {
      this.validateEmail();
      this.validatePassword();
      this.validateConfirmPassword();
      
      if (this.emailError || this.passwordError || this.confirmPasswordError) {
        return;
      }

      if (!this.agreeTerms) {
        this.$message.warning('请先同意服务条款和隐私政策');
        return;
      }

      this.isLoading = true;
      
      const payload = {
        username: this.registerForm.username,
        realName: this.registerForm.realName,
        password: this.registerForm.password,
        email: this.registerForm.email,
        phone: this.registerForm.phone
      };

      try {
        const response = await this.$http.post('/users/register', payload, {
          params: {
            verifyCode: this.verification
          }
        });
        console.log('注册返回数据:', response.data);
        if (response.data.code === 1) {
          this.$message.success(response.data.message || '注册成功，请登录');
          setTimeout(() => {
            this.$router.push('/login');
          }, 1500);
        } else {
          this.$message.error(response.data.message || '注册失败');
        }
      } catch (error) {
        console.error('注册请求错误:', error);
        this.$message.error(error.response?.data?.message || '注册失败，请检查网络后重试');
      } finally {
        this.isLoading = false;
      }
    }
  }
}
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-container {
  width: 100%;
  max-width: 1100px;
  background: white;
  border-radius: 24px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  display: flex;
  min-height: 750px;
}

/* 左侧品牌区域 */
.register-brand {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 50px;
  position: relative;
  overflow: hidden;
}

.register-brand::before {
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
.register-form-section {
  flex: 1.2;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 45px 50px;
  background: white;
  overflow-y: auto;
}

.register-form-wrapper {
  width: 100%;
  max-width: 450px;
}

.form-header {
  margin-bottom: 25px;
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
  margin-bottom: 30px;
}

.form-title-section h1 {
  font-size: 30px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px 0;
}

.form-title-section p {
  color: #888;
  margin: 0;
  font-size: 14px;
}

/* 分节标题 */
.form-section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 25px 0 18px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

.section-number {
  font-size: 14px;
  font-weight: 700;
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  padding: 4px 10px;
  border-radius: 6px;
}

.form-section-title span:last-child {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

/* 表单行布局 */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

/* 表单样式 */
.form-group {
  margin-bottom: 18px;
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
  padding: 12px 14px;
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  font-size: 14px;
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
  padding-right: 45px;
}

.password-toggle {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
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
  font-size: 12px;
  margin-top: 6px;
  padding-left: 4px;
}

.error-icon {
  font-size: 13px;
}

/* 验证码输入 */
.verification-group {
  margin-top: 20px;
}

.verification-input-wrapper {
  display: flex;
  gap: 10px;
}

.verification-input-wrapper input {
  flex: 1;
  min-width: 0;
  padding: 12px 14px;
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.3s;
  box-sizing: border-box;
  background: #fafafa;
}

.verification-input-wrapper input:focus {
  background: white;
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
  outline: none;
}

.verification-btn {
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
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

/* 服务条款 */
.terms-checkbox {
  margin: 20px 0;
}

.checkbox-label {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  cursor: pointer;
  font-size: 13px;
  color: #666;
  user-select: none;
  line-height: 1.5;
}

.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  margin-top: 2px;
  accent-color: #667eea;
  cursor: pointer;
}

.terms-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.terms-link:hover {
  color: #764ba2;
  text-decoration: underline;
}

/* 注册按钮 */
.register-btn {
  width: 100%;
  padding: 15px;
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
  margin-top: 10px;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.register-btn:active:not(:disabled) {
  transform: translateY(0);
}

.register-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 页脚 */
.register-footer {
  margin-top: 20px;
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
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
  transition: color 0.3s;
}

.register-footer a:hover {
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
  .register-container {
    flex-direction: column;
    max-width: 550px;
  }
  
  .register-brand {
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
  
  .register-form-section {
    padding: 40px 30px;
  }
}

@media (max-width: 550px) {
  .register-page {
    padding: 10px;
  }
  
  .form-row {
    grid-template-columns: 1fr;
    gap: 0;
  }
  
  .brand-features {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .form-title-section h1 {
    font-size: 26px;
  }
  
  .verification-input-wrapper {
    flex-direction: column;
  }
  
  .verification-btn {
    width: 100%;
  }
  
  .terms-checkbox {
    margin-top: 15px;
  }
  
  .checkbox-label {
    font-size: 12px;
  }
}
</style>
