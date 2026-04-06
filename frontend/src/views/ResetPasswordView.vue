<template>
  <div class="reset-page">
    <div class="reset-container">
      <!-- Left Brand Section -->
      <div class="reset-brand">
        <div class="brand-content">
          <div class="brand-logo">🔐</div>
          <h1>Reset Password</h1>
          <p class="brand-slogan">Secure Verification · Quick Recovery · Peace of Mind</p>
          <p class="brand-description">
            Verify your identity and set a new password,
            protecting your account security, we're with you.
          </p>
          <div class="brand-features">
            <div class="feature-item">
              <span class="feature-icon">📧</span>
              <span>Email Verification</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🔒</span>
              <span>Secure Encryption</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">⚡</span>
              <span>Quick Reset</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🛡️</span>
              <span>Account Protection</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Right Form Section -->
      <div class="reset-form-section">
        <div class="reset-form-wrapper">
          <div class="form-header">
            <router-link to="/login" class="back-link" title="Back to Sign In">
              <span class="back-icon">←</span>
              <span>Back to Sign In</span>
            </router-link>
          </div>
          
          <div class="form-title-section">
            <h1>Reset Password</h1>
            <p>Fill in the information below to reset your password</p>
          </div>
          
          <form @submit.prevent="handleSubmit">
            <div class="form-group">
              <label for="reset-email">
                <span class="label-icon">📧</span>
                Registered Email
                <span class="required">*</span>
              </label>
              <div class="input-wrapper">
                <input 
                  v-model="resetForm.email" 
                  type="email" 
                  id="reset-email" 
                  placeholder="Enter the email used during registration"
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
                Verification Code
                <span class="required">*</span>
              </label>
              <div class="verification-input-wrapper">
                <input 
                  v-model="verification" 
                  type="text" 
                  id="reset-verification" 
                  placeholder="Enter verification code"
                  required
                >
                <button 
                  type="button" 
                  @click="sendVerificationCode" 
                  class="verification-btn"
                  :disabled="countdown > 0 || isLoading"
                >
                  {{ countdown > 0 ? `Resend (${countdown})` : 'Get Code' }}
                </button>
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label for="reset-password">
                  <span class="label-icon">🔒</span>
                  New Password
                  <span class="required">*</span>
                </label>
                <div class="input-wrapper password-input">
                  <input 
                    v-model="resetForm.password" 
                    :type="showPassword ? 'text' : 'password'" 
                    id="reset-password" 
                    placeholder="Set your new password"
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
                  Confirm Password
                  <span class="required">*</span>
                </label>
                <div class="input-wrapper password-input">
                  <input 
                    v-model="resetForm.confirmPassword" 
                    :type="showConfirmPassword ? 'text' : 'password'" 
                    id="reset-confirm-password" 
                    placeholder="Confirm your new password"
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
              <h4>Password Requirements:</h4>
              <ul>
                <li :class="{ valid: resetForm.password.length >= 6 }">At least 6 characters</li>
                <li :class="{ valid: resetForm.password.length >= 8 }">8+ characters recommended for better security</li>
              </ul>
            </div>
            
            <!-- Success Message -->
            <transition name="slide-fade">
              <div v-if="isSuccess" class="success-message">
                <div class="success-icon">✅</div>
                <h3>Password Reset Successful!</h3>
                <p>Please sign in with your new password</p>
              </div>
            </transition>
            
            <button type="submit" class="reset-btn" :disabled="isLoading || isSuccess">
              <span v-if="isLoading" class="loading-spinner"></span>
              <span v-if="isSuccess">Reset Successful</span>
              <span v-else>{{ isLoading ? 'Resetting...' : 'Confirm Reset' }}</span>
            </button>
            
            <div v-if="isSuccess" class="success-actions">
              <router-link to="/login" class="login-btn">
                <span>Back to Sign In</span>
              </router-link>
            </div>
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
      isSuccess: false
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
        this.emailError = 'Please enter a valid email address';
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
        this.passwordError = 'Password must be at least 6 characters';
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
        this.confirmPasswordError = 'Passwords do not match';
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
          this.$message.success(response.data.message || 'Verification code sent successfully');
          this.startCountdown();
        } else {
          this.$message.error(response.data.message || 'Failed to send verification code');
        }
      } catch (error) {
        console.error('Verification code request error:', error);
        this.$message.error('Network error, please try again later');
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
    
    async handleSubmit() {
      this.validateEmail();
      this.validatePassword();
      this.validateConfirmPassword();
      
      if (this.emailError || this.passwordError || this.confirmPasswordError) {
        return;
      }

      if (!this.resetForm.email || !this.verification) {
        this.$message.warning('Please fill in your email and verification code');
        return;
      }

      if (this.resetForm.password.length < 6) {
        this.$message.warning('Password must be at least 6 characters');
        return;
      }

      this.isLoading = true;
      
      try {
        const response = await this.$http.post('/users/reset-password', {
          email: this.resetForm.email,
          newPassword: this.resetForm.password
        }, {
          params: {
            verifyCode: this.verification
          }
        });
        
        console.log('Reset password response:', response.data);
        if (response.data.code === 1) {
          this.isSuccess = true;
          this.$message.success('Password reset successful!');
        } else {
          this.$message.error(response.data.message || 'Reset failed');
        }
      } catch (error) {
        console.error('Reset password request error:', error);
        this.$message.error(error.response?.data?.message || 'Reset failed, please check your network and try again');
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

/* 成功消息 */
.success-message {
  text-align: center;
  padding: 30px 20px;
  background: linear-gradient(135deg, #d4edda 0%, #c3e6cb 100%);
  border-radius: 12px;
  margin-bottom: 20px;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.success-icon {
  font-size: 60px;
  margin-bottom: 15px;
}

.success-message h3 {
  font-size: 22px;
  color: #155724;
  margin: 0 0 10px 0;
}

.success-message p {
  color: #155724;
  margin: 0;
  font-size: 14px;
}

.success-actions {
  margin-top: 20px;
  text-align: center;
}

/* 重置按钮 */
.reset-btn {
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

.reset-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
}

.reset-btn:active:not(:disabled) {
  transform: translateY(0);
}

.reset-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 登录按钮 */
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
  
  .success-actions {
    margin-top: 15px;
  }
  
  .login-btn {
    width: 100%;
  }
}
</style>
