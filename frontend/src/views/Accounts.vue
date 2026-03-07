<template>
  <div class="accounts">
    <!-- 账户列表 -->
    <div class="accounts-header">
      <h2>我的账户</h2>
    </div>

    <div class="accounts-grid" v-if="accounts.length > 0">
      <div class="account-card" v-for="account in accounts" :key="account.id">
        <div class="account-header">
          <div class="account-type">
            <span class="type-icon">💳</span>
            <span class="type-name">{{ account.accountName || '银行账户' }}</span>
          </div>
          <span class="account-status" :class="getStatusClass(account.status)">{{ getStatusText(account.status) }}</span>
        </div>
        <div class="account-number">
          <span>{{ formatAccountNumber(account.accountNumber) }}</span>
        </div>
        <div class="account-balance">
          <span class="balance-label">可用余额</span>
          <span class="balance-amount">¥ {{ formatNumber(account.balance) }}</span>
        </div>
        <div class="account-info">
          <div class="info-item">
            <span class="info-label">币种</span>
            <span class="info-value">{{ account.currency || 'CNY' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">日限额</span>
            <span class="info-value">¥ {{ formatNumber(account.dailyLimit) }}</span>
          </div>
        </div>
        <div class="account-actions">
          <button class="action-btn primary" @click="viewDetail(account)">详情</button>
          <button class="action-btn secondary" @click="transfer(account)">转账</button>
        </div>
      </div>
    </div>
    <div class="empty-state" v-else>
      <p>暂无账户信息</p>
    </div>

    <!-- 账户详情弹窗 -->
    <div class="modal-overlay" v-if="selectedAccount" @click="selectedAccount = null">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>账户详情</h3>
          <button class="close-btn" @click="selectedAccount = null">×</button>
        </div>
        <div class="modal-body">
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">账户 ID</span>
              <span class="value">{{ selectedAccount.id }}</span>
            </div>
            <div class="detail-item">
              <span class="label">账户号码</span>
              <span class="value">{{ selectedAccount.accountNumber }}</span>
            </div>
            <div class="detail-item">
              <span class="label">账户名称</span>
              <span class="value">{{ selectedAccount.accountName || '银行账户' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">账户状态</span>
              <span class="value">
                <span class="status-badge" :class="getStatusClass(selectedAccount.status)">
                  {{ getStatusText(selectedAccount.status) }}
                </span>
              </span>
            </div>
            <div class="detail-item">
              <span class="label">账户余额</span>
              <span class="value amount">¥ {{ formatNumber(selectedAccount.balance) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">币种</span>
              <span class="value">{{ selectedAccount.currency || 'CNY' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">单日交易限额</span>
              <span class="value">¥ {{ formatNumber(selectedAccount.dailyLimit) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">创建时间</span>
              <span class="value">{{ formatDateTime(selectedAccount.createdAt) }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn cancel" @click="selectedAccount = null">关闭</button>
          <button class="btn primary" @click="transfer(selectedAccount)">转账</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { accountApi } from '@/api/api'

export default {
  name: 'Accounts',
  setup() {
    const router = useRouter()
    const { proxy } = getCurrentInstance()
    const selectedAccount = ref(null)
    const accounts = ref([])
    const loading = ref(false)

    const formatNumber = (num) => {
      if (!num) return '0.00'
      return Number(num).toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })
    }

    const formatAccountNumber = (accountNumber) => {
      if (!accountNumber) return '**** **** **** ****'
      // 将账号格式化为每 4 位一组
      const cleanNumber = accountNumber.replace(/\s/g, '')
      if (cleanNumber.length >= 4) {
        const lastFour = cleanNumber.slice(-4)
        return `**** **** **** ${lastFour}`
      }
      return accountNumber
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
      const statusMap = {
        0: 'frozen',
        1: 'active',
        2: 'closed'
      }
      return statusMap[status] || 'active'
    }

    const getStatusText = (status) => {
      const statusMap = {
        0: '冻结',
        1: '正常',
        2: '关闭'
      }
      return statusMap[status] || '未知'
    }

    const loadAccounts = async () => {
      loading.value = true
      try {
        const response = await accountApi.getAccounts()
        const { code, data, message } = response.data
        if (code === 1 || code === 200) {
          accounts.value = data || []
        } else {
          proxy.$message.error(message || '获取账户列表失败')
        }
      } catch (error) {
        console.error('Load accounts error:', error)
        proxy.$message.error('获取账户列表失败')
      } finally {
        loading.value = false
      }
    }

    const viewDetail = (account) => {
      selectedAccount.value = account
    }

    const transfer = (account) => {
      router.push(`/transfers?accountId=${account.id}`)
    }

    onMounted(() => {
      loadAccounts()
    })

    return {
      accounts,
      selectedAccount,
      formatNumber,
      formatAccountNumber,
      formatDateTime,
      getStatusClass,
      getStatusText,
      viewDetail,
      transfer
    }
  }
}
</script>

<style scoped>
.accounts {
  max-width: var(--container-max-width);
  margin: 0 auto;
}

.accounts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-3xl);
}

.accounts-header h2 {
  margin: 0;
  color: var(--color-white);
  font-size: var(--font-size-5xl);
  font-weight: var(--font-weight-semibold);
}

.add-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg) var(--spacing-2xl);
  background: var(--glass-bg);
  color: var(--color-white);
  border: 1px solid var(--glass-border-hover);
  border-radius: var(--radius-full);
  cursor: pointer;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-normal);
}

.add-btn:hover {
  background: var(--glass-bg-hover);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.accounts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--spacing-2xl);
  margin-bottom: var(--spacing-3xl);
}

.account-card {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-3xl);
  padding: var(--spacing-2xl);
  border: 1px solid var(--glass-border);
  transition: all var(--transition-normal);
}

.account-card:hover {
  transform: translateY(-5px);
  background: var(--glass-bg-hover);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

.account-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.account-type {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.type-icon {
  font-size: var(--font-size-5xl);
}

.type-name {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-white);
}

.account-status {
  padding: var(--spacing-xs) var(--spacing-md);
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
}

.account-status.active {
  background: var(--status-success-bg);
  color: var(--status-success);
}

.account-status.frozen {
  background: var(--status-warning-bg);
  color: var(--status-warning);
}

.account-status.closed {
  background: var(--status-danger-bg);
  color: var(--status-danger);
}

.account-number {
  font-size: var(--font-size-xl);
  color: var(--text-on-gradient-secondary);
  margin-bottom: var(--spacing-lg);
  letter-spacing: 2px;
}

.account-balance {
  margin-bottom: var(--spacing-xl);
}

.balance-label {
  display: block;
  font-size: var(--font-size-xs);
  color: var(--text-on-gradient-muted);
  margin-bottom: var(--spacing-xs);
}

.balance-amount {
  font-size: var(--font-size-5xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-white);
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.account-info {
  display: flex;
  gap: var(--spacing-xl);
  margin-bottom: var(--spacing-xl);
  padding: var(--spacing-lg);
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-xl);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.info-label {
  font-size: var(--font-size-xs);
  color: var(--text-on-gradient-muted);
}

.info-value {
  font-size: var(--font-size-md);
  color: var(--color-white);
  font-weight: var(--font-weight-medium);
}

.account-actions {
  display: flex;
  gap: var(--spacing-lg);
}

.action-btn {
  flex: 1;
  padding: var(--spacing-lg);
  border: none;
  border-radius: var(--radius-xl);
  cursor: pointer;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-normal);
}

.action-btn.primary {
  background: var(--btn-glass-bg);
  color: var(--color-white);
  border: 1px solid var(--btn-glass-border);
}

.action-btn.primary:hover {
  background: var(--btn-glass-hover);
  transform: translateY(-2px);
}

.action-btn.secondary {
  background: transparent;
  color: var(--color-white);
  border: 1px solid var(--glass-border-hover);
}

.action-btn.secondary:hover {
  background: var(--glass-bg-light);
  transform: translateY(-2px);
}

.empty-state {
  text-align: center;
  padding: var(--spacing-5xl);
  color: var(--text-on-gradient-muted);
  background: var(--glass-bg);
  border-radius: var(--radius-3xl);
  border: 1px solid var(--glass-border);
}

/* 弹窗样式 */
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
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  padding: var(--spacing-2xl);
  border: 1px solid var(--glass-border);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-2xl);
  padding-bottom: var(--spacing-lg);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.modal-header h3 {
  margin: 0;
  color: var(--color-white);
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-semibold);
}

.close-btn {
  background: var(--glass-bg-light);
  border: 1px solid var(--glass-border);
  font-size: var(--font-size-2xl);
  cursor: pointer;
  color: var(--color-white);
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-normal);
}

.close-btn:hover {
  background: var(--glass-border-hover);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-lg);
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.detail-item .label {
  font-size: var(--font-size-xs);
  color: var(--text-on-gradient-muted);
}

.detail-item .value {
  font-size: var(--font-size-lg);
  color: var(--color-white);
  font-weight: var(--font-weight-medium);
}

.detail-item .value.amount {
  font-size: var(--font-size-3xl);
  color: var(--status-warning);
  font-weight: var(--font-weight-bold);
}

.status-badge {
  display: inline-block;
  padding: var(--spacing-xs) var(--spacing-md);
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
}

.status-badge.active {
  background: var(--status-success-bg);
  color: var(--status-success);
}

.status-badge.frozen {
  background: var(--status-warning-bg);
  color: var(--status-warning);
}

.status-badge.closed {
  background: var(--status-danger-bg);
  color: var(--status-danger);
}

.modal-footer {
  display: flex;
  gap: var(--spacing-lg);
  justify-content: flex-end;
  margin-top: var(--spacing-2xl);
  padding-top: var(--spacing-lg);
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}

.btn {
  padding: var(--spacing-lg) var(--spacing-2xl);
  border: none;
  border-radius: var(--radius-xl);
  cursor: pointer;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-normal);
}

.btn.cancel {
  background: var(--glass-bg-light);
  color: var(--color-white);
  border: 1px solid var(--glass-border-hover);
}

.btn.cancel:hover {
  background: var(--glass-border-hover);
}

.btn.primary {
  background: var(--glass-bg-hover);
  color: var(--color-white);
  border: 1px solid var(--glass-border-active);
}

.btn.primary:hover {
  background: var(--glass-bg-active);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .accounts-grid {
    grid-template-columns: 1fr;
  }

  .account-info {
    flex-direction: column;
    gap: var(--spacing-md);
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
