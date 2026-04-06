<template>
  <div class="profile">
    <div class="profile-container">
      <!-- Profile Card -->
      <div class="profile-card">
        <div class="card-header">
          <h2>👤 Profile</h2>
          <button class="edit-btn" @click="toggleEdit">
            {{ isEditing ? '💾 Save' : '✏️ Edit' }}
          </button>
        </div>
        <div class="card-body">
          <div class="avatar-section">
            <div class="avatar">
              <span>{{ userAvatar }}</span>
            </div>
            <div class="user-info">
              <h3>{{ userInfo.realName || userInfo.username || 'User' }}</h3>
              <p class="user-id">User ID: {{ userInfo.id }}</p>
              <span class="user-level" v-if="userInfo.status === 1">✓ Active</span>
              <span class="user-level warning" v-else>⚠ Inactive</span>
            </div>
          </div>
          
          <div class="info-grid">
            <div class="info-item">
              <div class="info-label-row">
                <label>👤 Username</label>
              </div>
              <div class="info-value">
                <span v-if="!isEditing">{{ userInfo.username }}</span>
                <input v-if="isEditing" v-model="editForm.username" type="text" class="edit-input" />
              </div>
            </div>
            <div class="info-item">
              <div class="info-label-row">
                <label>📝 Real Name</label>
                <button class="eye-btn" @click="showRealName = !showRealName" :title="showRealName ? 'Hide' : 'Show'" v-if="userInfo.realName && !isEditing">
                  {{ showRealName ? '👁️' : '👁️‍🗨️' }}
                </button>
              </div>
              <div class="info-value">
                <span v-if="!isEditing">{{ getMaskedRealName() || 'Not set' }}</span>
                <input v-if="isEditing" v-model="editForm.realName" type="text" class="edit-input" />
              </div>
            </div>
            <div class="info-item">
              <div class="info-label-row">
                <label>📱 Phone</label>
                <button class="eye-btn" @click="showPhone = !showPhone" :title="showPhone ? 'Hide' : 'Show'" v-if="userInfo.phone && !isEditing">
                  {{ showPhone ? '👁️' : '👁️‍🗨️' }}
                </button>
              </div>
              <div class="info-value">
                <span v-if="!isEditing">{{ getMaskedPhone() || 'Not set' }}</span>
                <input v-if="isEditing" v-model="editForm.phone" type="text" class="edit-input" />
              </div>
            </div>
            <div class="info-item">
              <div class="info-label-row">
                <label>📧 Email</label>
                <button class="eye-btn" @click="showEmail = !showEmail" :title="showEmail ? 'Hide' : 'Show'" v-if="userInfo.email && !isEditing">
                  {{ showEmail ? '👁️' : '👁️‍🗨️' }}
                </button>
              </div>
              <div class="info-value">
                <span>{{ getMaskedEmail() }}</span>
              </div>
            </div>
            <div class="info-item">
              <div class="info-label-row">
                <label>📅 Registration Date</label>
              </div>
              <div class="info-value">
                <span>{{ formatDateTime(userInfo.createdAt) }}</span>
              </div>
            </div>
            <div class="info-item">
              <div class="info-label-row">
                <label>🔐 Account Status</label>
              </div>
              <div class="info-value">
                <span :class="getStatusClass(userInfo.status)">{{ getStatusText(userInfo.status) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Security Settings -->
      <div class="profile-card">
        <div class="card-header">
          <h2>🔒 Security Settings</h2>
        </div>
        <div class="card-body">
          <div class="security-item">
            <div class="security-info">
              <span class="security-icon">🔑</span>
              <div>
                <h4>Login Password</h4>
                <p>Change password regularly to improve account security</p>
              </div>
            </div>
            <button class="action-btn" @click="showChangePassword = true">Change</button>
          </div>
          <div class="security-item">
            <div class="security-info">
              <span class="security-icon">📱</span>
              <div>
                <h4>Phone Verification</h4>
                <p>{{ userInfo.phoneVerified ? 'Verified: ' + formatPhone(userInfo.phone) : 'Not verified' }}</p>
              </div>
            </div>
            <button class="action-btn" v-if="!userInfo.phoneVerified" @click="showVerifyPhone = true">Verify</button>
            <span class="verified-badge" v-else>✓ Verified</span>
          </div>
          <div class="security-item">
            <div class="security-info">
              <span class="security-icon">📧</span>
              <div>
                <h4>Email Verification</h4>
                <p>{{ userInfo.emailVerified ? 'Verified: ' + userInfo.email : 'Not verified' }}</p>
              </div>
            </div>
            <button class="action-btn" v-if="!userInfo.emailVerified" @click="showVerifyEmail = true">Verify</button>
            <span class="verified-badge" v-else>✓ Verified</span>
          </div>
          <div class="security-item">
            <div class="security-info">
              <span class="security-icon">🔐</span>
              <div>
                <h4>Payment Password</h4>
                <p>{{ hasPayPassword ? 'Payment password set' : 'Not set, verification required for transfers' }}</p>
              </div>
            </div>
            <button class="action-btn" @click="openPayPasswordModal">{{ hasPayPassword ? 'Change' : 'Set' }}</button>
          </div>
        </div>
      </div>

      <!-- Account Overview -->
      <div class="profile-card">
        <div class="card-header">
          <h2>💳 My Accounts</h2>
          <router-link to="/accounts" class="view-all">View All</router-link>
        </div>
        <div class="card-body">
          <div class="account-summary" v-if="accounts.length > 0">
            <div class="summary-item">
              <div class="summary-label">Total Assets</div>
              <div class="summary-value">¥ {{ totalAssets }}</div>
            </div>
            <div class="summary-item" v-for="account in accounts" :key="account.id">
              <div class="summary-label">{{ account.accountName || 'Account' }}</div>
              <div class="summary-value">¥ {{ formatNumber(account.balance) }}</div>
            </div>
          </div>
          <div class="empty-state" v-else>
            <p>No account information</p>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="quick-actions">
        <h3>⚡ Quick Actions</h3>
        <div class="action-grid">
          <router-link to="/transfers" class="action-card">
            <span class="action-icon">💸</span>
            <span>Transfer</span>
          </router-link>
          <router-link to="/transactions" class="action-card">
            <span class="action-icon">📋</span>
            <span>Transactions</span>
          </router-link>
          <router-link to="/chatbot" class="action-card">
            <span class="action-icon">🤖</span>
            <span>Online Support</span>
          </router-link>
          <router-link to="/accounts" class="action-card">
            <span class="action-icon">📄</span>
            <span>Accounts</span>
          </router-link>
        </div>
      </div>
    </div>

    <!-- Change Password Modal -->
    <div class="modal-overlay" v-if="showChangePassword" @click="showChangePassword = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>🔑 Change Password</h3>
          <button class="close-btn" @click="showChangePassword = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Verification Code</label>
            <div class="code-input-group">
              <input type="text" v-model="passwordForm.verifyCode" placeholder="Please enter verification code" maxlength="6" />
              <button class="send-code-btn" @click="sendPasswordCode" :disabled="passwordCountdown > 0">
                {{ passwordCountdown > 0 ? passwordCountdown + 's' : 'Get Code' }}
              </button>
            </div>
            <p class="form-hint">Verification code will be sent to your registered email</p>
          </div>
          <div class="form-group">
            <label>New Password</label>
            <input type="password" v-model="passwordForm.newPassword" placeholder="Please enter new password" />
          </div>
          <div class="form-group">
            <label>Confirm New Password</label>
            <input type="password" v-model="passwordForm.confirmPassword" placeholder="Please enter new password again" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="showChangePassword = false">Cancel</button>
          <button class="confirm-btn" @click="changePassword" :disabled="changePasswordLoading">
            {{ changePasswordLoading ? 'Changing...' : 'Confirm' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Phone Verification Modal -->
    <div class="modal-overlay" v-if="showVerifyPhone" @click="showVerifyPhone = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>📱 Phone Verification</h3>
          <button class="close-btn" @click="showVerifyPhone = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Phone Number</label>
            <input type="text" v-model="verifyPhoneForm.phone" placeholder="Please enter phone number" />
          </div>
          <div class="form-group">
            <label>Verification Code</label>
            <div class="code-input-group">
              <input type="text" v-model="verifyPhoneForm.code" placeholder="Please enter verification code" maxlength="6" />
              <button class="send-code-btn" @click="sendPhoneCode" :disabled="countdown > 0">
                {{ countdown > 0 ? countdown + 's' : 'Send Code' }}
              </button>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="showVerifyPhone = false">Cancel</button>
          <button class="confirm-btn" @click="submitPhoneVerify" :disabled="verifyPhoneLoading">
            {{ verifyPhoneLoading ? 'Verifying...' : 'Verify' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Payment Password Modal -->
    <div class="modal-overlay" v-if="showPayPassword" @click="showPayPassword = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>🔐 {{ hasPayPassword ? 'Change Payment Password' : 'Set Payment Password' }}</h3>
          <button class="close-btn" @click="showPayPassword = false">×</button>
        </div>
        <div class="modal-body">
          <!-- Enter old password when changing -->
          <div class="form-group" v-if="hasPayPassword">
            <label>Old Payment Password</label>
            <input type="password" v-model="payPasswordForm.oldPayPassword" placeholder="Please enter old payment password" maxlength="6" />
          </div>
          <div class="form-group">
            <label>{{ hasPayPassword ? 'New Payment Password' : 'Payment Password' }}</label>
            <input type="password" v-model="payPasswordForm.payPassword" placeholder="Please enter 6-digit payment password" maxlength="6" />
          </div>
          <div class="form-group">
            <label>Confirm Payment Password</label>
            <input type="password" v-model="payPasswordForm.confirmPassword" placeholder="Please enter payment password again" maxlength="6" />
          </div>
          <p class="form-hint">Payment password is used for verification of sensitive operations such as transfers and withdrawals</p>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="showPayPassword = false">Cancel</button>
          <button class="confirm-btn" @click="submitPayPassword" :disabled="payPasswordLoading">
            {{ payPasswordLoading ? 'Processing...' : 'Confirm' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Email Verification Modal -->
    <div class="modal-overlay" v-if="showVerifyEmail" @click="showVerifyEmail = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>📧 Email Verification</h3>
          <button class="close-btn" @click="showVerifyEmail = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Email Address</label>
            <input type="email" v-model="verifyEmailForm.email" placeholder="Please enter email address" />
          </div>
          <div class="form-group">
            <label>Verification Code</label>
            <div class="code-input-group">
              <input type="text" v-model="verifyEmailForm.code" placeholder="Please enter verification code" maxlength="6" />
              <button class="send-code-btn" @click="sendEmailCode" :disabled="emailCountdown > 0">
                {{ emailCountdown > 0 ? emailCountdown + 's' : 'Send Code' }}
              </button>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="showVerifyEmail = false">Cancel</button>
          <button class="confirm-btn" @click="submitEmailVerify" :disabled="verifyEmailLoading">
            {{ verifyEmailLoading ? 'Verifying...' : 'Verify' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, getCurrentInstance } from 'vue'
import { useStore } from 'vuex'
import { accountApi, userApi } from '@/api/api'
import { maskRealName, maskPhone, maskEmail } from '@/utils/desensitize'

export default {
  name: 'Profile',
  setup() {
    const { proxy } = getCurrentInstance()
    const store = useStore()
    const isEditing = ref(false)
    const showChangePassword = ref(false)
    const showVerifyPhone = ref(false)
    const showVerifyEmail = ref(false)
    const changePasswordLoading = ref(false)
    const verifyPhoneLoading = ref(false)
    const verifyEmailLoading = ref(false)
    const countdown = ref(0)
    const emailCountdown = ref(0)
    
    // Sensitive information display status
    const showRealName = ref(false)
    const showPhone = ref(false)
    const showEmail = ref(false)

    const userInfo = ref({
      id: '',
      username: '',
      email: '',
      phone: '',
      realName: '',
      status: 1,
      emailVerified: false,
      phoneVerified: false,
      createdAt: ''
    })

    const accounts = ref([])

    // Edit form - corresponds to UpdateProfileReq {username, phone, realName}
    const editForm = reactive({
      username: '',
      phone: '',
      realName: ''
    })

    const passwordForm = reactive({
      verifyCode: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    // Change password countdown
    const passwordCountdown = ref(0)

    const verifyPhoneForm = reactive({
      phone: '',
      code: ''
    })

    const verifyEmailForm = reactive({
      email: '',
      code: ''
    })

    // Payment password related
    const showPayPassword = ref(false)
    const hasPayPassword = ref(false)
    const payPasswordLoading = ref(false)
    const payPasswordForm = reactive({
      oldPayPassword: '',
      payPassword: '',
      confirmPassword: ''
    })

    const userAvatar = computed(() => {
      const name = userInfo.value.realName || userInfo.value.username || 'U'
      return name.charAt(0).toUpperCase()
    })

    const totalAssets = computed(() => {
      const total = accounts.value.reduce((sum, acc) => {
        return sum + (parseFloat(acc.balance) || 0)
      }, 0)
      return total.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
    })

    const formatNumber = (num) => {
      if (!num) return '0.00'
      return Number(num).toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })
    }

    const formatPhone = (phone) => {
      if (!phone) return ''
      // Hide middle 4 digits
      if (phone.length >= 7) {
        return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
      }
      return phone
    }
    
    // Get masked real name
    const getMaskedRealName = () => {
      return showRealName.value ? userInfo.value.realName : maskRealName(userInfo.value.realName)
    }
    
    // Get masked phone
    const getMaskedPhone = () => {
      return showPhone.value ? userInfo.value.phone : maskPhone(userInfo.value.phone)
    }
    
    // Get masked email
    const getMaskedEmail = () => {
      return showEmail.value ? userInfo.value.email : maskEmail(userInfo.value.email)
    }

    const formatDateTime = (dateTime) => {
      if (!dateTime) return '-'
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    const getStatusClass = (status) => {
      return status === 1 ? 'status-success' : 'status-warning'
    }

    const getStatusText = (status) => {
      return status === 1 ? 'Active' : 'Inactive'
    }

    // Countdown timer
    let countdownTimer = null

    const startCountdown = () => {
      countdown.value = 60
      if (countdownTimer) clearInterval(countdownTimer)
      countdownTimer = setInterval(() => {
        if (countdown.value > 0) {
          countdown.value--
        } else {
          clearInterval(countdownTimer)
        }
      }, 1000)
    }

    const startEmailCountdown = () => {
      emailCountdown.value = 60
      if (countdownTimer) clearInterval(countdownTimer)
      countdownTimer = setInterval(() => {
        if (emailCountdown.value > 0) {
          emailCountdown.value--
        } else {
          clearInterval(countdownTimer)
        }
      }, 1000)
    }

    const startPasswordCountdown = () => {
      passwordCountdown.value = 60
      if (countdownTimer) clearInterval(countdownTimer)
      countdownTimer = setInterval(() => {
        if (passwordCountdown.value > 0) {
          passwordCountdown.value--
        } else {
          clearInterval(countdownTimer)
        }
      }, 1000)
    }

    const loadUserInfo = async () => {
      try {
        // Get user info from Vuex
        await store.dispatch('fetchUserInfo')
        const user = store.state.user
        if (user) {
          userInfo.value = {
            id: user.id,
            username: user.username,
            email: user.email,
            phone: user.phone,
            realName: user.realName,
            status: user.status,
            emailVerified: user.emailVerified,
            phoneVerified: user.phoneVerified,
            createdAt: user.createdAt
          }
          // Sync edit form
          editForm.username = user.username || ''
          editForm.phone = user.phone || ''
          editForm.realName = user.realName || ''
          // Sync verification form
          verifyPhoneForm.phone = user.phone || ''
          verifyEmailForm.email = user.email || ''
        }
      } catch (error) {
        console.error('Load user info error:', error)
      }
    }

    const loadAccounts = async () => {
      try {
        const response = await accountApi.getAccounts()
        const { code, data } = response.data
        if (code === 1 || code === 200) {
          accounts.value = data || []
        }
      } catch (error) {
        console.error('Load accounts error:', error)
      }
    }

    const toggleEdit = () => {
      if (isEditing.value) {
        saveProfile()
      } else {
        isEditing.value = !isEditing.value
      }
    }

    // Save profile - call backend /users/profile API
    const saveProfile = async () => {
      // Validate required fields
      if (!editForm.username) {
        proxy.$message.warning('Username cannot be empty')
        return
      }

      try {
        // Use userApi.updateProfile to call backend API
        // UpdateProfileReq: {username, phone, realName}
        const response = await userApi.updateProfile({
          username: editForm.username,
          phone: editForm.phone,
          realName: editForm.realName
        })
        const { code, data, message } = response.data
        if (code === 1 || code === 200) {
          // Update local user info
          userInfo.value.username = editForm.username
          userInfo.value.phone = editForm.phone
          userInfo.value.realName = editForm.realName
          // Update user info in Vuex
          store.commit('SET_USER', data)
          proxy.$message.success('Profile saved successfully!')
        } else {
          proxy.$message.error(message || 'Save failed')
        }
      } catch (error) {
        console.error('Save profile error:', error)
        proxy.$message.error(error.response?.data?.message || 'Save failed, please try again')
      } finally {
        isEditing.value = false
      }
    }

    // Send phone verification code
    const sendPhoneCode = async () => {
      if (!verifyPhoneForm.phone) {
        proxy.$message.warning('Please enter phone number')
        return
      }
      try {
        const response = await userApi.sendCode('phone', verifyPhoneForm.phone)
        const { code, message } = response.data
        if (code === 1 || code === 200) {
          proxy.$message.success('Verification code sent')
          startCountdown()
        } else {
          proxy.$message.error(message || 'Send failed')
        }
      } catch (error) {
        console.error('Send phone code error:', error)
        proxy.$message.error(error.response?.data?.message || 'Failed to send verification code')
      }
    }

    // Send email verification code
    const sendEmailCode = async () => {
      if (!verifyEmailForm.email) {
        proxy.$message.warning('Please enter email address')
        return
      }
      try {
        const response = await userApi.sendCode('email', null, verifyEmailForm.email)
        const { code, message } = response.data
        if (code === 1 || code === 200) {
          proxy.$message.success('Verification code sent to email')
          startEmailCountdown()
        } else {
          proxy.$message.error(message || 'Send failed')
        }
      } catch (error) {
        console.error('Send email code error:', error)
        proxy.$message.error(error.response?.data?.message || 'Failed to send verification code')
      }
    }

    // Submit phone verification
    const submitPhoneVerify = async () => {
      if (!verifyPhoneForm.phone || !verifyPhoneForm.code) {
        proxy.$message.warning('Please fill in all fields')
        return
      }
      verifyPhoneLoading.value = true
      try {
        // Backend API requires verification code parameter
        const response = await userApi.sendCode('phone', verifyPhoneForm.phone)
        // Note: This requires backend API to verify the code
        // Temporarily call send code API as example, should call verification API in production
        proxy.$message.success('Phone verification feature pending, please contact administrator')
        showVerifyPhone.value = false
      } catch (error) {
        console.error('Phone verify error:', error)
        proxy.$message.error(error.response?.data?.message || 'Verification failed')
      } finally {
        verifyPhoneLoading.value = false
      }
    }

    // Submit email verification
    const submitEmailVerify = async () => {
      if (!verifyEmailForm.email || !verifyEmailForm.code) {
        proxy.$message.warning('Please fill in all fields')
        return
      }
      verifyEmailLoading.value = true
      try {
        const response = await userApi.sendCode('email', null, verifyEmailForm.email)
        proxy.$message.success('Email verification feature pending, please contact administrator')
        showVerifyEmail.value = false
      } catch (error) {
        console.error('Email verify error:', error)
        proxy.$message.error(error.response?.data?.message || 'Verification failed')
      } finally {
        verifyEmailLoading.value = false
      }
    }

    // Send change password verification code
    const sendPasswordCode = async () => {
      try {
        const response = await userApi.sendResetPasswordCode()
        const { code, message } = response.data
        if (code === 1 || code === 200) {
          proxy.$message.success('Verification code sent to your registered email')
          startPasswordCountdown()
        } else {
          proxy.$message.error(message || 'Send failed')
        }
      } catch (error) {
        console.error('Send password code error:', error)
        proxy.$message.error(error.response?.data?.message || 'Failed to send verification code')
      }
    }

    // Change password - call backend /users/reset-password API
    const changePassword = async () => {
      if (!passwordForm.verifyCode) {
        proxy.$message.warning('Please enter verification code')
        return
      }
      if (!passwordForm.newPassword || !passwordForm.confirmPassword) {
        proxy.$message.warning('Please fill in all password fields')
        return
      }
      if (passwordForm.newPassword !== passwordForm.confirmPassword) {
        proxy.$message.error('New passwords do not match')
        return
      }
      if (passwordForm.newPassword.length < 6) {
        proxy.$message.warning('Password must be at least 6 characters')
        return
      }

      changePasswordLoading.value = true
      try {
        // Change password after login requires verification code
        const response = await userApi.resetPassword({
          newPassword: passwordForm.newPassword
        }, passwordForm.verifyCode)
        const { code, message } = response.data
        if (code === 1 || code === 200) {
          proxy.$message.success('Password changed successfully!')
          showChangePassword.value = false
          passwordForm.verifyCode = ''
          passwordForm.newPassword = ''
          passwordForm.confirmPassword = ''
        } else {
          proxy.$message.error(message || 'Change failed')
        }
      } catch (error) {
        console.error('Change password error:', error)
        proxy.$message.error(error.response?.data?.message || 'Change failed, please check if verification code is correct')
      } finally {
        changePasswordLoading.value = false
      }
    }

    // Load payment password status
    const loadPayPasswordStatus = async () => {
      try {
        const response = await userApi.getPayPasswordStatus()
        const { code, data } = response.data
        if (code === 1 || code === 200) {
          hasPayPassword.value = data || false
        }
      } catch (error) {
        console.error('Load pay password status error:', error)
      }
    }

    // Open payment password modal
    const openPayPasswordModal = () => {
      // Reset form
      payPasswordForm.oldPayPassword = ''
      payPasswordForm.payPassword = ''
      payPasswordForm.confirmPassword = ''
      showPayPassword.value = true
    }

    // Submit payment password
    const submitPayPassword = async () => {
      // Validate input
      if (hasPayPassword.value && !payPasswordForm.oldPayPassword) {
        proxy.$message.warning('Please enter old payment password')
        return
      }
      if (!payPasswordForm.payPassword) {
        proxy.$message.warning('Please enter payment password')
        return
      }
      if (!/^\d{6}$/.test(payPasswordForm.payPassword)) {
        proxy.$message.warning('Payment password must be 6 digits')
        return
      }
      if (payPasswordForm.payPassword !== payPasswordForm.confirmPassword) {
        proxy.$message.error('Payment passwords do not match')
        return
      }

      payPasswordLoading.value = true
      try {
        let response
        if (hasPayPassword.value) {
          // Change payment password
          response = await userApi.updatePayPassword(
            payPasswordForm.oldPayPassword,
            payPasswordForm.payPassword
          )
        } else {
          // Set payment password
          response = await userApi.setPayPassword(payPasswordForm.payPassword)
        }

        const { code, message } = response.data
        if (code === 1 || code === 200) {
          proxy.$message.success(hasPayPassword.value ? 'Payment password changed successfully!' : 'Payment password set successfully!')
          showPayPassword.value = false
          hasPayPassword.value = true
          // Reset form
          payPasswordForm.oldPayPassword = ''
          payPasswordForm.payPassword = ''
          payPasswordForm.confirmPassword = ''
        } else {
          proxy.$message.error(message || 'Operation failed')
        }
      } catch (error) {
        console.error('Submit pay password error:', error)
        proxy.$message.error(error.response?.data?.message || 'Operation failed, please try again')
      } finally {
        payPasswordLoading.value = false
      }
    }

    onMounted(() => {
      loadUserInfo()
      loadAccounts()
      loadPayPasswordStatus()
    })

    // Clean up timer
    onMounted(() => {
      return () => {
        if (countdownTimer) clearInterval(countdownTimer)
      }
    })

    return {
      isEditing,
      showChangePassword,
      showVerifyPhone,
      showVerifyEmail,
      changePasswordLoading,
      verifyPhoneLoading,
      verifyEmailLoading,
      countdown,
      emailCountdown,
      userInfo,
      accounts,
      editForm,
      passwordForm,
      verifyPhoneForm,
      verifyEmailForm,
      userAvatar,
      totalAssets,
      formatNumber,
      formatPhone,
      formatDateTime,
      getStatusClass,
      getStatusText,
      showRealName,
      showPhone,
      showEmail,
      getMaskedRealName,
      getMaskedPhone,
      getMaskedEmail,
      toggleEdit,
      saveProfile,
      passwordCountdown,
      sendPhoneCode,
      sendEmailCode,
      sendPasswordCode,
      submitPhoneVerify,
      submitEmailVerify,
      changePassword,
      // Payment password related
      showPayPassword,
      hasPayPassword,
      payPasswordLoading,
      payPasswordForm,
      openPayPasswordModal,
      submitPayPassword
    }
  }
}
</script>

<style scoped>
.profile {
  max-width: 1000px;
  margin: 0 auto;
}

.profile-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-3xl);
  border: 1px solid var(--glass-border);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
  color: var(--color-white);
  font-weight: 600;
}

.card-body {
  padding: 20px;
}

/* Avatar Section */
.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--glass-bg-hover);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-white);
  font-size: 32px;
  font-weight: 600;
}

.user-info h3 {
  margin: 0 0 8px 0;
  color: var(--color-white);
  font-weight: 600;
}

.user-id {
  margin: 0;
  color: var(--text-on-gradient-muted);
  font-size: 14px;
}

.user-level {
  display: inline-block;
  margin-top: 8px;
  padding: 4px 12px;
  background: var(--status-success-bg);
  color: var(--status-success);
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.user-level.warning {
  background: var(--status-warning-bg);
  color: var(--status-warning);
}

/* Info Grid */
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item label {
  font-size: 14px;
  color: var(--text-on-gradient-secondary);
  font-weight: 500;
}

/* Label row - contains label and eye button */
.info-label-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 22px;
}

.info-label-row label {
  margin: 0;
  min-width: 85px;
}

.info-value {
  font-size: 16px;
  color: var(--color-white);
  font-weight: 500;
}

/* Eye Button Styles */
.eye-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  border-radius: 4px;
  transition: all var(--transition-normal);
  opacity: 0.7;
}

.eye-btn:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.1);
  transform: scale(1.1);
}

.status-success {
  color: var(--status-success);
  font-weight: 600;
}

.status-warning {
  color: var(--status-warning);
  font-weight: 600;
}

.edit-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--input-border);
  border-radius: var(--radius-xl);
  font-size: 14px;
  margin-top: 4px;
  background: var(--input-bg);
  color: var(--color-white);
  box-sizing: border-box;
}

.edit-input:focus {
  outline: none;
  border-color: var(--input-border-focus);
  background: var(--input-bg-focus);
}

.edit-btn {
  padding: 10px 20px;
  background: var(--glass-bg-hover);
  color: var(--color-white);
  border: 1px solid var(--glass-border-hover);
  border-radius: var(--radius-xl);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all var(--transition-normal);
}

.edit-btn:hover {
  background: var(--glass-bg-active);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

/* Security Settings */
.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.security-item:last-child {
  border-bottom: none;
}

.security-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.security-icon {
  font-size: 24px;
}

.security-info h4 {
  margin: 0 0 4px 0;
  color: var(--color-white);
  font-weight: 500;
}

.security-info p {
  margin: 0;
  color: var(--text-on-gradient-muted);
  font-size: 13px;
}

.action-btn {
  padding: 8px 16px;
  background: var(--glass-bg-hover);
  border: 1px solid var(--glass-border-hover);
  color: var(--color-white);
  border-radius: var(--radius-xl);
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all var(--transition-normal);
}

.action-btn:hover {
  background: var(--glass-bg-active);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.verified-badge {
  padding: 8px 16px;
  background: var(--status-success-bg);
  color: var(--status-success);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

/* Account Overview */
.view-all {
  color: var(--text-on-gradient-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color var(--transition-normal);
}

.view-all:hover {
  color: var(--color-white);
}

.account-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
}

.summary-item {
  text-align: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-xl);
}

.summary-label {
  font-size: 14px;
  color: var(--text-on-gradient-muted);
  margin-bottom: 8px;
}

.summary-value {
  font-size: 20px;
  color: var(--color-white);
  font-weight: 700;
}

.empty-state {
  text-align: center;
  padding: var(--spacing-2xl);
  color: var(--text-on-gradient-muted);
}

/* Quick Actions */
.quick-actions {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-3xl);
  border: 1px solid var(--glass-border);
  padding: 20px;
}

.quick-actions h3 {
  margin: 0 0 16px 0;
  color: var(--color-white);
  font-size: 18px;
  font-weight: 600;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 16px;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-xl);
  text-decoration: none;
  color: var(--color-white);
  transition: all var(--transition-normal);
  border: 1px solid var(--glass-border);
}

.action-card:hover {
  background: var(--glass-bg-hover);
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.action-icon {
  font-size: 32px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(5px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: var(--z-modal);
}

.modal {
  background: var(--gradient-primary);
  border-radius: var(--radius-3xl);
  width: 100%;
  max-width: 400px;
  overflow: hidden;
  border: 1px solid var(--glass-border);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.modal-header h3 {
  margin: 0;
  color: var(--color-white);
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  background: var(--glass-bg-light);
  border: 1px solid var(--glass-border);
  font-size: 20px;
  cursor: pointer;
  color: var(--color-white);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-normal);
}

.close-btn:hover {
  background: var(--glass-border-hover);
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: var(--text-on-gradient);
  font-size: 14px;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid var(--input-border);
  border-radius: var(--radius-xl);
  font-size: 14px;
  box-sizing: border-box;
  background: var(--input-bg);
  color: var(--color-white);
}

.form-group input:focus {
  outline: none;
  border-color: var(--input-border-focus);
  background: var(--input-bg-focus);
}

.form-group input::placeholder {
  color: var(--text-on-gradient-muted);
}

.form-hint {
  margin: 8px 0 0 0;
  font-size: 12px;
  color: var(--text-on-gradient-muted);
}

.code-input-group {
  display: flex;
  gap: 10px;
}

.code-input-group input {
  flex: 1;
}

.send-code-btn {
  padding: 12px 16px;
  background: var(--glass-bg-hover);
  border: 1px solid var(--glass-border-hover);
  color: var(--color-white);
  border-radius: var(--radius-xl);
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  transition: all var(--transition-normal);
}

.send-code-btn:hover:not(:disabled) {
  background: var(--glass-bg-active);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.send-code-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}

.cancel-btn,
.confirm-btn {
  padding: 10px 24px;
  border-radius: var(--radius-xl);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.cancel-btn {
  background: var(--glass-bg-light);
  border: 1px solid var(--glass-border-hover);
  color: var(--color-white);
}

.cancel-btn:hover {
  background: var(--glass-border-hover);
}

.confirm-btn {
  background: var(--glass-bg-hover);
  border: 1px solid var(--glass-border-active);
  color: var(--color-white);
}

.confirm-btn:hover:not(:disabled) {
  background: var(--glass-bg-active);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.confirm-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .account-summary {
    grid-template-columns: 1fr;
  }
  
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
