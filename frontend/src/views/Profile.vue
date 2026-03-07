<template>
  <div class="profile">
    <div class="profile-container">
      <!-- 个人信息卡片 -->
      <div class="profile-card">
        <div class="card-header">
          <h2>👤 个人信息</h2>
          <button class="edit-btn" @click="toggleEdit">
            {{ isEditing ? '💾 保存' : '✏️ 编辑' }}
          </button>
        </div>
        <div class="card-body">
          <div class="avatar-section">
            <div class="avatar">
              <span>{{ userAvatar }}</span>
            </div>
            <div class="user-info">
              <h3>{{ userInfo.realName || userInfo.username || '用户' }}</h3>
              <p class="user-id">用户 ID: {{ userInfo.id }}</p>
              <span class="user-level" v-if="userInfo.status === 1">✓ 正常</span>
              <span class="user-level warning" v-else>⚠ 异常</span>
            </div>
          </div>
          
          <div class="info-grid">
            <div class="info-item">
              <label>📱 手机号码</label>
              <div class="info-value">
                <span v-if="!isEditing">{{ formatPhone(userInfo.phone) }}</span>
                <input v-if="isEditing" v-model="editForm.phone" type="text" class="edit-input" />
              </div>
            </div>
            <div class="info-item">
              <label>📧 电子邮箱</label>
              <div class="info-value">
                <span v-if="!isEditing">{{ userInfo.email }}</span>
                <input v-if="isEditing" v-model="editForm.email" type="email" class="edit-input" />
              </div>
            </div>
            <div class="info-item">
              <label>📅 注册时间</label>
              <div class="info-value">
                <span>{{ formatDateTime(userInfo.createdAt) }}</span>
              </div>
            </div>
            <div class="info-item">
              <label>🔐 账号状态</label>
              <div class="info-value">
                <span :class="getStatusClass(userInfo.status)">{{ getStatusText(userInfo.status) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 安全设置 -->
      <div class="profile-card">
        <div class="card-header">
          <h2>🔒 安全设置</h2>
        </div>
        <div class="card-body">
          <div class="security-item">
            <div class="security-info">
              <span class="security-icon">🔑</span>
              <div>
                <h4>登录密码</h4>
                <p>定期修改密码可提高账户安全性</p>
              </div>
            </div>
            <button class="action-btn" @click="showChangePassword = true">修改</button>
          </div>
          <div class="security-item">
            <div class="security-info">
              <span class="security-icon">📱</span>
              <div>
                <h4>手机验证</h4>
                <p>{{ userInfo.phoneVerified ? '已验证：' + formatPhone(userInfo.phone) : '未验证' }}</p>
              </div>
            </div>
            <button class="action-btn" v-if="!userInfo.phoneVerified" @click="verifyPhone">验证</button>
            <span class="verified-badge" v-else>✓ 已验证</span>
          </div>
          <div class="security-item">
            <div class="security-info">
              <span class="security-icon">📧</span>
              <div>
                <h4>邮箱验证</h4>
                <p>{{ userInfo.emailVerified ? '已验证：' + userInfo.email : '未验证' }}</p>
              </div>
            </div>
            <button class="action-btn" v-if="!userInfo.emailVerified" @click="verifyEmail">验证</button>
            <span class="verified-badge" v-else>✓ 已验证</span>
          </div>
        </div>
      </div>

      <!-- 账户概览 -->
      <div class="profile-card">
        <div class="card-header">
          <h2>💳 我的账户</h2>
          <router-link to="/accounts" class="view-all">查看全部</router-link>
        </div>
        <div class="card-body">
          <div class="account-summary" v-if="accounts.length > 0">
            <div class="summary-item">
              <div class="summary-label">总资产</div>
              <div class="summary-value">¥ {{ totalAssets }}</div>
            </div>
            <div class="summary-item" v-for="account in accounts" :key="account.id">
              <div class="summary-label">{{ account.accountName || '账户' }}</div>
              <div class="summary-value">¥ {{ formatNumber(account.balance) }}</div>
            </div>
          </div>
          <div class="empty-state" v-else>
            <p>暂无账户信息</p>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <h3>⚡ 快捷操作</h3>
        <div class="action-grid">
          <router-link to="/transfers" class="action-card">
            <span class="action-icon">💸</span>
            <span>转账汇款</span>
          </router-link>
          <router-link to="/transactions" class="action-card">
            <span class="action-icon">📋</span>
            <span>交易记录</span>
          </router-link>
          <router-link to="/chatbot" class="action-card">
            <span class="action-icon">🤖</span>
            <span>在线客服</span>
          </router-link>
          <router-link to="/accounts" class="action-card">
            <span class="action-icon">📄</span>
            <span>账户管理</span>
          </router-link>
        </div>
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <div class="modal-overlay" v-if="showChangePassword" @click="showChangePassword = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>🔑 修改密码</h3>
          <button class="close-btn" @click="showChangePassword = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>当前密码</label>
            <input type="password" v-model="passwordForm.oldPassword" placeholder="请输入当前密码" />
          </div>
          <div class="form-group">
            <label>新密码</label>
            <input type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码" />
          </div>
          <div class="form-group">
            <label>确认新密码</label>
            <input type="password" v-model="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="showChangePassword = false">取消</button>
          <button class="confirm-btn" @click="changePassword" :disabled="changePasswordLoading">
            {{ changePasswordLoading ? '修改中...' : '确认修改' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, getCurrentInstance } from 'vue'
import { useStore } from 'vuex'
import { accountApi } from '@/api/api'
import http from '@/http'

export default {
  name: 'Profile',
  setup() {
    const { proxy } = getCurrentInstance()
    const store = useStore()
    const isEditing = ref(false)
    const showChangePassword = ref(false)
    const changePasswordLoading = ref(false)

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

    const editForm = reactive({
      phone: '',
      email: ''
    })

    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    const userAvatar = computed(() => {
      const name = userInfo.value.realName || userInfo.value.username || '用'
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
      // 隐藏中间 4 位
      if (phone.length >= 7) {
        return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
      }
      return phone
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
      return status === 1 ? '正常' : '异常'
    }

    const loadUserInfo = async () => {
      try {
        // 从 Vuex 获取用户信息
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
          // 同步编辑表单
          editForm.phone = user.phone || ''
          editForm.email = user.email || ''
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

    const saveProfile = async () => {
      try {
        const response = await http.put('/users/profile', {
          phone: editForm.phone,
          email: editForm.email
        })
        const { code, data, message } = response.data
        if (code === 1 || code === 200) {
          userInfo.value.phone = editForm.phone
          userInfo.value.email = editForm.email
          proxy.$message.success('个人信息保存成功！')
        } else {
          proxy.$message.error(message || '保存失败')
        }
      } catch (error) {
        console.error('Save profile error:', error)
        proxy.$message.error('保存失败，请重试')
      } finally {
        isEditing.value = false
      }
    }

    const verifyPhone = async () => {
      try {
        const response = await http.post('/users/code', { type: 'phone' })
        const { code, message } = response.data
        if (code === 1 || code === 200) {
          proxy.$message.success('验证码已发送')
        } else {
          proxy.$message.error(message || '发送失败')
        }
      } catch (error) {
        console.error('Verify phone error:', error)
        proxy.$message.error('发送验证码失败')
      }
    }

    const verifyEmail = async () => {
      try {
        const response = await http.post('/users/code', { type: 'email' })
        const { code, message } = response.data
        if (code === 1 || code === 200) {
          proxy.$message.success('验证码已发送到邮箱')
        } else {
          proxy.$message.error(message || '发送失败')
        }
      } catch (error) {
        console.error('Verify email error:', error)
        proxy.$message.error('发送验证码失败')
      }
    }

    const changePassword = async () => {
      if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
        proxy.$message.warning('请填写所有密码字段')
        return
      }
      if (passwordForm.newPassword !== passwordForm.confirmPassword) {
        proxy.$message.error('两次输入的新密码不一致')
        return
      }
      if (passwordForm.newPassword.length < 6) {
        proxy.$message.warning('密码长度不能少于 6 位')
        return
      }

      changePasswordLoading.value = true
      try {
        const response = await http.post('/users/reset-password', {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        const { code, message } = response.data
        if (code === 1 || code === 200) {
          proxy.$message.success('密码修改成功！')
          showChangePassword.value = false
          passwordForm.oldPassword = ''
          passwordForm.newPassword = ''
          passwordForm.confirmPassword = ''
        } else {
          proxy.$message.error(message || '修改失败')
        }
      } catch (error) {
        console.error('Change password error:', error)
        proxy.$message.error('修改失败，请检查当前密码是否正确')
      } finally {
        changePasswordLoading.value = false
      }
    }

    onMounted(() => {
      loadUserInfo()
      loadAccounts()
    })

    return {
      isEditing,
      showChangePassword,
      changePasswordLoading,
      userInfo,
      accounts,
      editForm,
      passwordForm,
      userAvatar,
      totalAssets,
      formatNumber,
      formatPhone,
      formatDateTime,
      getStatusClass,
      getStatusText,
      toggleEdit,
      saveProfile,
      verifyPhone,
      verifyEmail,
      changePassword
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

/* 头像区域 */
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

/* 信息网格 */
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

.info-value {
  font-size: 16px;
  color: var(--color-white);
  font-weight: 500;
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

/* 安全设置 */
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

/* 账户概览 */
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

/* 快捷操作 */
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

/* 弹窗 */
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

/* 响应式设计 */
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
