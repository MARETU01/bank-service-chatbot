<template>
  <div class="profile">
    <div class="profile-container">
      <!-- 个人信息卡片 -->
      <div class="profile-card">
        <div class="card-header">
          <h2>👤 个人信息</h2>
          <button class="edit-btn" @click="isEditing = !isEditing">
            {{ isEditing ? '💾 保存' : '✏️ 编辑' }}
          </button>
        </div>
        <div class="card-body">
          <div class="avatar-section">
            <div class="avatar">
              <span>{{ user.name.charAt(0) }}</span>
            </div>
            <div class="user-info">
              <h3>{{ user.name }}</h3>
              <p class="user-id">用户 ID: {{ user.userId }}</p>
              <span class="user-level">VIP {{ user.level }} 会员</span>
            </div>
          </div>
          
          <div class="info-grid">
            <div class="info-item">
              <label>📱 手机号码</label>
              <div class="info-value">
                <span>{{ user.phone }}</span>
                <input v-if="isEditing" v-model="editForm.phone" type="text" class="edit-input" />
              </div>
            </div>
            <div class="info-item">
              <label>📧 电子邮箱</label>
              <div class="info-value">
                <span>{{ user.email }}</span>
                <input v-if="isEditing" v-model="editForm.email" type="email" class="edit-input" />
              </div>
            </div>
            <div class="info-item">
              <label>🏠 联系地址</label>
              <div class="info-value">
                <span>{{ user.address }}</span>
                <input v-if="isEditing" v-model="editForm.address" type="text" class="edit-input" />
              </div>
            </div>
            <div class="info-item">
              <label>📅 注册时间</label>
              <div class="info-value">
                <span>{{ user.registerDate }}</span>
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
                <p>已绑定：{{ user.phone }}</p>
              </div>
            </div>
            <button class="action-btn">验证</button>
          </div>
          <div class="security-item">
            <div class="security-info">
              <span class="security-icon">🆔</span>
              <div>
                <h4>实名认证</h4>
                <p>已完成实名认证</p>
              </div>
            </div>
            <span class="verified-badge">✓ 已认证</span>
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
          <div class="account-summary">
            <div class="summary-item">
              <div class="summary-label">总资产</div>
              <div class="summary-value">¥ {{ totalAssets }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">活期存款</div>
              <div class="summary-value">¥ {{ user.accounts[0].balance }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">定期存款</div>
              <div class="summary-value">¥ {{ user.accounts[1].balance }}</div>
            </div>
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
          <a href="#" class="action-card">
            <span class="action-icon">📄</span>
            <span>电子账单</span>
          </a>
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
          <button class="confirm-btn" @click="changePassword">确认修改</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue'

export default {
  name: 'Profile',
  setup() {
    const isEditing = ref(false)
    const showChangePassword = ref(false)

    const user = reactive({
      userId: 'U123456789',
      name: '张三',
      phone: '138****5678',
      email: 'zhang***@example.com',
      address: '北京市朝阳区***街道***号',
      registerDate: '2023-01-15',
      level: '黄金',
      accounts: [
        { type: '活期', accountNo: '6222 **** **** 1234', balance: '50,000.00' },
        { type: '定期', accountNo: '6222 **** **** 5678', balance: '100,000.00' }
      ]
    })

    const editForm = reactive({
      phone: user.phone,
      email: user.email,
      address: user.address
    })

    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    const totalAssets = computed(() => {
      const total = 50000 + 100000
      return total.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
    })

    function changePassword() {
      if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
        alert('请填写所有密码字段')
        return
      }
      if (passwordForm.newPassword !== passwordForm.confirmPassword) {
        alert('两次输入的新密码不一致')
        return
      }
      if (passwordForm.newPassword.length < 6) {
        alert('密码长度不能少于 6 位')
        return
      }
      alert('密码修改成功！')
      showChangePassword.value = false
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    }

    function saveProfile() {
      user.phone = editForm.phone
      user.email = editForm.email
      user.address = editForm.address
      isEditing.value = false
      alert('个人信息保存成功！')
    }

    return {
      isEditing,
      showChangePassword,
      user,
      editForm,
      passwordForm,
      totalAssets,
      changePassword,
      saveProfile
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
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #1e3a5f;
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
  border-bottom: 1px solid #e9ecef;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1e3a5f, #2d5a87);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 32px;
  font-weight: bold;
}

.user-info h3 {
  margin: 0 0 8px 0;
  color: #333;
}

.user-id {
  margin: 0;
  color: #999;
  font-size: 14px;
}

.user-level {
  display: inline-block;
  margin-top: 8px;
  padding: 4px 12px;
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  color: #333;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
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
  color: #666;
}

.info-value {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.edit-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  margin-top: 4px;
}

.edit-btn {
  padding: 8px 16px;
  background: #1e3a5f;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.edit-btn:hover {
  background: #2d5a87;
}

/* 安全设置 */
.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #e9ecef;
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
  color: #333;
}

.security-info p {
  margin: 0;
  color: #999;
  font-size: 13px;
}

.action-btn {
  padding: 6px 16px;
  background: white;
  border: 1px solid #1e3a5f;
  color: #1e3a5f;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.action-btn:hover {
  background: #1e3a5f;
  color: white;
}

.verified-badge {
  padding: 6px 16px;
  background: #e8f5e9;
  color: #2e7d32;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}

/* 账户概览 */
.view-all {
  color: #1e3a5f;
  text-decoration: none;
  font-size: 14px;
}

.view-all:hover {
  text-decoration: underline;
}

.account-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.summary-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.summary-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 20px;
  color: #1e3a5f;
  font-weight: bold;
}

/* 快捷操作 */
.quick-actions {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px;
}

.quick-actions h3 {
  margin: 0 0 16px 0;
  color: #1e3a5f;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  text-decoration: none;
  color: #333;
  transition: all 0.3s;
}

.action-card:hover {
  background: #e8f0f8;
  transform: translateY(-2px);
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
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 400px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h3 {
  margin: 0;
  color: #1e3a5f;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
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
  color: #333;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #e9ecef;
}

.cancel-btn,
.confirm-btn {
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.cancel-btn {
  background: white;
  border: 1px solid #ddd;
  color: #666;
}

.confirm-btn {
  background: #1e3a5f;
  border: none;
  color: white;
}
</style>
