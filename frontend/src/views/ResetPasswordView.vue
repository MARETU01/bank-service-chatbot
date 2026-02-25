<template>
  <div class="reset-page">
    <div class="reset-container">
      <!-- 左侧品牌区域 -->
      <div class="reset-brand">
        <div class="brand-content">
          <div class="brand-logo">🔐</div>
          <h1>重设密码</h1>
          <p class="brand-slogan">安全验证 · 快速找回 · 安心使用</p>
          <p class="brand-description">
            验证您的身份并设置新密码，
            保障账户安全，我们与您同行。
          </p>
          <div class="brand-features">
            <div class="feature-item">
              <span class="feature-icon">📧</span>
              <span>邮箱验证</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🔒</span>
              <span>安全加密</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">⚡</span>
              <span>快速重置</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🛡️</span>
              <span>账户保护</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧表单区域 -->
      <div class="reset-form-section">
        <div class="reset-form-wrapper">
          <div class="form-header">
            <router-link to="/login" class="back-link" title="返回登录">
              <span class="back-icon">←</span>
              <span>返回登录</span>
            </router-link>
          </div>
          
          <div class="form-title-section">
            <h1>重设密码</h1>
            <p>验证身份后设置新密码</p>
          </div>
          
          <!-- 进度指示器 -->
          <div class="progress-steps">
            <div class="progress-step" :class="{ active: currentStep >= 1, completed: currentStep > 1 }">
              <div class="step-number">1</div>
              <div class="step-label">验证身份</div>
            </div>
            <div class="progress-line" :class="{ active: currentStep > 1 }"></div>
            <div class="progress-step" :class="{ active: currentStep >= 2, completed: currentStep > 2 }">
              <div class="step-number">2</div>
              <div class="step-label">设置新密码</div>
            </div>
            <div class="progress-line" :class="{ active: currentStep > 2 }"></div>
            <div class="progress-step" :class="{ active: currentStep >= 3 }">
              <div class="step-number">3</div>
              <div class="step-label">完成</div>
            </div>
          </div>
          
          <form @submit.prevent="handleSubmit">
            <!-- 步骤 1: 验证身份 -->
            <transition name="slide-fade">
              <div v-if="currentStep === 1" class="step-content">
                <div class="form-section-title">
                  <span class="section-number">01</span>
                  <span>验证身份</span>
                </div>
                
                <div class="form-group">
                  <label for="reset-email">
                    <span class="label-icon">📧</span>
                    注册邮箱
                    <span class="required">*</span>
                  </label>
                  <div class="input-wrapper">
                    <input 
                      v-model="resetForm.email" 
                      type="email" 
                      id="reset-email" 
                      placeholder="请输入注册时使用的邮箱"
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
                
                <div class="form-group verification-group">
                  <label for="reset-verification">
                    <span class="label-icon">🔑</span>
                    验证码
                    <span class="required">*</span>
                  </label>
                  <div class="verification-input-wrapper">
                    <input 
                      v-model="verification" 
                      type="text" 
                      id="reset-verification" 
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
                
                <button type="button" class="next-btn" @click="goToStep2" :disabled="isLoading">
                  <span v-if="isLoading" class="loading-spinner"></span>
                  <span>{{ isLoading ? '验证中...' : '下一步' }}</span>
                </button>
              </div>
            </transition>
            
            <!-- 步骤 2: 设置新密码 -->
            <transition name="slide-fade">
              <div v-if="currentStep === 2" class="step-content">
                <div class="form-section-title">
                  <span class="section-number">02</span>
                  <span>设置新密码</span>
                </div>
                
                <div class="form-row">
                  <div class="form-group">
                    <label for="reset-password">
                      <span class="label-icon">🔒</span>
                      新密码
                      <span class="required">*</span>
                    </label>
                    <div class="input-wrapper password-input">
                      <input 
                        v-model="resetForm.password" 
                        :type="showPassword ? 'text' : 'password'" 
                        id="reset-password" 
                        placeholder="请设置新密码"
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
                    <label for="reset-confirm-password">
                      <span class="label-icon">🔐</span>
                      确认密码
                      <span class="required">*</span>
                    </label>
                    <div class="input-wrapper password-input">
                      <input 
                        v-model="resetForm.confirmPassword" 
                        :type="showConfirmPassword ? 'text' : 'password'" 
                        id="reset-confirm-password" 
                        placeholder="请再次输入新密码"
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
                
                <div class="password-requirements">
                  <h4>密码要求：</h4>
                  <ul>
                    <li :class="{ valid: resetForm.password.length >= 6 }">至少 6 个字符</li>
                    <li :class="{ valid: resetForm.password.length >= 8 }">建议 8 个字符以上更安全</li>
                  </ul>
                </div>
                
                <div class="step-actions">
                  <button type="button" class="back-btn" @click="currentStep = 1">上一步</button>
                  <button type="submit" class="reset-btn" :disabled="isLoading">
                    <span v-if="isLoading" class="loading-spinner"></span>
                    <span>{{ isLoading ? '重置中...' : '确认重置' }}</span>
                  </button>
                </div>
              </div>
            </transition>
            
            <!-- 步骤 3: 完成 -->
            <transition name="slide-fade">
              <div v-if="currentStep === 3" class="step-content step-complete">
                <div class="complete-icon">✅</div>
                <h2>密码重置成功！</h2>
                <p>您的密码已成功更新，请使用新密码登录</p>
                <div class="complete-actions">
                  <router-link to="/login" class="login-btn">
                    <span>返回登录</span>
                  </router-link>
                </div>
              </div>
            </transition>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      currentStep: 1,
      resetForm: {
        email: '',
        password: '',
        confirmPassword: ''
      },
      verification: '',
      countdown: 0,
      emailError: '',
      passwordError: '',
      confirmPasswordError: '',
      showPassword: false,
      showConfirmPassword: false,
      isLoading: false,
      isVerified: false
    }
  },
  methods: {
    validateEmail() {
      const email = this.resetForm.email;
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
      const password = this.resetForm.password;
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
      const password = this.resetForm.password;
      const confirmPassword = this.resetForm.confirmPassword;
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
          email: this.resetForm.email
        }, {
          params: {
            type: 'reset-password'
          }
        });
        if (response.data.code === 1) {
          alert(response.data.message || '验证码发送成功');
          this.startCountdown();
        } else {
          alert(response.data.message || '验证码发送失败');
        }
      } catch (error) {
        console.error('验证码请求错误:', error);
        alert('网络错误，请稍后重试');
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
    
    async goToStep2() {
      this.validateEmail();
      
      if (this.emailError || !this.resetForm.email) {
        alert('请输入正确的邮箱地址');
        return;
      }
      
      if (!this.verification) {
        alert('请输入验证码');
        return;
      }
      
      this.isLoading = true;
      try {
        // 验证验证码
        const response = await this.$http.post('/users/verify', {
          email: this.resetForm.email,
          code: this.verification
        }, {
          params: {
            type: 'reset'
          }
        });
        
        if (response.data.code === 1) {
          this.isVerified = true;
          this.currentStep = 2;
        } else {
          alert(response.data.message || '验证码错误');
        }
      } catch (error) {
        console.error('验证码验证错误:', error);
        alert('验证码错误，请重试');
      } finally {
        this.isLoading = false;
      }
    },
    
    async handleSubmit() {
      this.validatePassword();
      this.validateConfirmPassword();
      
      if (this.passwordError || this.confirmPasswordError) {
        return;
      }

      if (this.resetForm.password.length < 6) {
        alert('密码长度不能少于 6 位');
        return;
      }

      this.isLoading = true;
      
      try {
        const response = await this.$http.post('/users/reset-password', {
          email: this.resetForm.email,
          password: this.resetForm.password,
          code: this.verification
        });
        
        console.log('重置密码返回数据:', response.data);
        if (response.data.code === 1) {
          this.currentStep = 3;
        } else {
          alert(response.data.message || '重置失败');
        }
      } catch (error) {
        console.error('重置密码请求错误:', error);
        alert(error.response?.data?.message || '重置失败，请检查网络后重试');
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

.reset-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.reset-container {
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
.reset-brand {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 50px;
  position: relative;
  overflow: hidden;
}

.reset-brand::before {
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
.reset-form-section {
  flex: 1.2;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 45px 50px;
  background: white;
  overflow-y: auto;
}

.reset-form-wrapper {
  width: 100%;
  max-width: 450px;
}

.form-header {
  margin-bottom: 25px;
}

.back-link {
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

.back-link:hover {
  background: #667eea;
  color: white;
  transform: translateX(-3px);
}

.back-icon {
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

/* 进度指示器 */
.progress-steps {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 35px;
  padding: 20px 10px;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
}

.step-number {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e8e8e8;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s;
}

.progress-step.active .step-number {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.progress-step.completed .step-number {
  background: #10b981;
  color: white;
}

.step-label {
  font-size: 12px;
  color: #999;
  font-weight: 500;
  transition: color 0.3s;
}

.progress-step.active .step-label,
.progress-step.completed .step-label {
  color: #667eea;
}

.progress-line {
  flex: 1;
  height: 3px;
  background: #e8e8e8;
  margin: 0 15px;
  margin-top: -25px;
  transition: background 0.3s;
}

.progress-line.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

/* 密码要求 */
.password-requirements {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 15px 18px;
  margin: 15px 0 25px 0;
}

.password-requirements h4 {
  margin: 0 0 10px 0;
  font-size: 13px;
  color: #666;
  font-weight: 600;
}

.password-requirements ul {
  margin: 0;
  padding-left: 20px;
  list-style: none;
}

.password-requirements li {
  font-size: 12px;
  color: #888;
  margin-bottom: 6px;
  position: relative;
  padding-left: 20px;
}

.password-requirements li::before {
  content: '○';
  position: absolute;
  left: 0;
  color: #ccc;
  font-size: 10px;
}

.password-requirements li.valid {
  color: #10b981;
}

.password-requirements li.valid::before {
  content: '✓';
  color: #10b981;
  font-weight: bold;
}

/* 步骤操作按钮 */
.step-actions {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

.back-btn {
  flex: 1;
  padding: 15px;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.back-btn:hover {
  background: #e8e8e8;
  color: #333;
}

.next-btn,
.reset-btn {
  flex: 2;
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
}

.next-btn:hover:not(:disabled),
.reset-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.next-btn:active:not(:disabled),
.reset-btn:active:not(:disabled) {
  transform: translateY(0);
}

.next-btn:disabled,
.reset-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 完成状态 */
.step-complete {
  text-align: center;
  padding: 40px 20px;
}

.complete-icon {
  font-size: 80px;
  margin-bottom: 20px;
  animation: bounce 0.6s ease;
}

@keyframes bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

.step-complete h2 {
  font-size: 26px;
  color: #1a1a2e;
  margin: 0 0 15px 0;
}

.step-complete p {
  color: #888;
  font-size: 15px;
  margin: 0 0 30px 0;
}

.complete-actions {
  margin-top: 30px;
}

.login-btn {
  display: inline-block;
  padding: 15px 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-decoration: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

/* 加载动画 */
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

/* Slide Fade 过渡动画 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
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
  .reset-container {
    flex-direction: column;
    max-width: 550px;
  }
  
  .reset-brand {
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
  
  .reset-form-section {
    padding: 40px 30px;
  }
}

@media (max-width: 550px) {
  .reset-page {
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
  
  .step-actions {
    flex-direction: column;
  }
  
  .back-btn,
  .next-btn,
  .reset-btn {
    flex: 1;
  }
  
  .progress-steps {
    padding: 15px 5px;
  }
  
  .step-label {
    font-size: 10px;
  }
  
  .step-number {
    width: 30px;
    height: 30px;
    font-size: 14px;
  }
}
</style>
