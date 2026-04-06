<template>
  <div class="accounts">
    <!-- Account List -->
    <div class="accounts-header">
      <h2>My Accounts</h2>
      <button class="add-btn" @click="showCreateModal = true">
        <span>+</span> New Account
      </button>
    </div>

    <div class="accounts-grid" v-if="accounts.length > 0">
      <div class="account-card" v-for="account in accounts" :key="account.id">
        <div class="account-header">
          <div class="account-type">
            <span class="type-icon">💳</span>
            <span class="type-name">{{ account.accountName || 'Bank Account' }}</span>
          </div>
          <span class="account-status" :class="getStatusClass(account.status)">{{ getStatusText(account.status) }}</span>
        </div>
        <div class="account-number">
          <div class="account-number-row">
            <span>{{ getDisplayAccountNumber(account) }}</span>
            <button class="eye-btn" @click.stop="toggleAccountNumber(account.id)" :title="showAccountNumber[account.id] ? 'Hide' : 'Show'">
              {{ showAccountNumber[account.id] ? '👁️' : '👁️‍🗨️' }}
            </button>
          </div>
        </div>
        <div class="account-balance">
          <div class="balance-row">
            <div class="balance-label-row">
              <span class="balance-label">Available Balance</span>
              <button class="eye-btn" @click.stop="toggleBalance(account.id)" :title="showBalance[account.id] ? 'Hide' : 'Show'">
                {{ showBalance[account.id] ? '👁️' : '👁️‍🗨️' }}
              </button>
            </div>
            <span class="balance-amount">¥ {{ getDisplayBalance(account) }}</span>
          </div>
        </div>
        <div class="account-info">
          <div class="info-item">
            <span class="info-label">Currency</span>
            <span class="info-value">{{ account.currency || 'CNY' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Daily Limit</span>
            <span class="info-value">¥ {{ formatNumber(account.dailyLimit) }}</span>
          </div>
        </div>
        <div class="account-actions">
        <button class="action-btn primary" @click="viewDetail(account)">Details</button>
        <button class="action-btn secondary" @click="transfer(account)">Transfer</button>
        </div>
      </div>
    </div>
    <div class="empty-state" v-else>
      <p>No account information</p>
    </div>

    <!-- Account Details Modal -->
    <div class="modal-overlay" v-if="selectedAccount" @click="selectedAccount = null">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>Account Details</h3>
          <button class="close-btn" @click="selectedAccount = null">×</button>
        </div>
        <div class="modal-body">
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">Account ID</span>
              <span class="value">{{ selectedAccount.id }}</span>
            </div>
            <div class="detail-item">
              <div class="label-row">
                <span class="label">Account Number</span>
                <button class="eye-btn" @click="toggleDetailAccountNumber" :title="showAccountNumber[selectedAccount.id] ? 'Hide' : 'Show'">
                  {{ showAccountNumber[selectedAccount.id] ? '👁️' : '👁️‍🗨️' }}
                </button>
              </div>
              <span class="value">{{ getDetailAccountNumber() }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Account Name</span>
              <span class="value">{{ selectedAccount.accountName || '银行账户' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Account Status</span>
              <span class="value">
                <span class="status-badge" :class="getStatusClass(selectedAccount.status)">
                  {{ getStatusText(selectedAccount.status) }}
                </span>
              </span>
            </div>
            <div class="detail-item">
              <div class="label-row">
                <span class="label">Account Balance</span>
                <button class="eye-btn" @click="toggleDetailBalance" :title="showBalance[selectedAccount.id] ? 'Hide' : 'Show'">
                  {{ showBalance[selectedAccount.id] ? '👁️' : '👁️‍🗨️' }}
                </button>
              </div>
              <span class="value amount">¥ {{ getDetailBalance() }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Currency</span>
              <span class="value">{{ selectedAccount.currency || 'CNY' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Daily Transaction Limit</span>
              <span class="value">¥ {{ formatNumber(selectedAccount.dailyLimit) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Created At</span>
              <span class="value">{{ formatDateTime(selectedAccount.createdAt) }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn cancel" @click="selectedAccount = null">Close</button>
          <button class="btn warning" @click="editAccount(selectedAccount)" v-if="selectedAccount.status === 1">Edit</button>
          <button class="btn danger" @click="freezeAccount(selectedAccount)" v-if="selectedAccount.status === 1">Freeze</button>
          <button class="btn success" @click="unfreezeAccount(selectedAccount)" v-if="selectedAccount.status === 0">Unfreeze</button>
          <button class="btn primary" @click="transfer(selectedAccount)" v-if="selectedAccount.status === 1">Transfer</button>
        </div>
      </div>
    </div>

    <!-- 创建账户弹窗 -->
    <div class="modal-overlay" v-if="showCreateModal" @click="showCreateModal = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>New Account</h3>
          <button class="close-btn" @click="showCreateModal = false">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="createAccount">
            <div class="form-group">
              <label>Account Name <span class="required">*</span></label>
              <input type="text" v-model="newAccount.accountName" placeholder="Enter account name" required />
            </div>
            <div class="form-group">
              <label>Currency</label>
              <select v-model="newAccount.currency">
                <option value="CNY">CNY (Chinese Yuan)</option>
                <option value="USD">USD (US Dollar)</option>
                <option value="EUR">EUR (Euro)</option>
              </select>
            </div>
            <div class="form-group">
              <label>Daily Transaction Limit</label>
              <div class="amount-input">
                <span class="currency">¥</span>
                <input type="number" v-model="newAccount.dailyLimit" placeholder="Enter limit" step="0.01" min="0" />
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button class="btn cancel" @click="showCreateModal = false">Cancel</button>
          <button class="btn primary" @click="createAccount" :disabled="createLoading">
            {{ createLoading ? 'Creating...' : 'Confirm Create' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 编辑账户弹窗 -->
    <div class="modal-overlay" v-if="showEditModal" @click="showEditModal = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>Edit Account</h3>
          <button class="close-btn" @click="showEditModal = false">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="updateAccount">
            <div class="form-group">
              <label>Account Name <span class="required">*</span></label>
              <input type="text" v-model="editAccountData.accountName" placeholder="Enter account name" required />
            </div>
            <div class="form-group">
              <label>Set Balance <span class="test-hint">(For Testing)</span></label>
              <div class="amount-input">
                <span class="currency">¥</span>
                <input type="number" v-model="editAccountData.balance" placeholder="Enter balance" step="0.01" min="0" />
              </div>
            </div>
            <div class="form-group">
              <label>Daily Transaction Limit</label>
              <div class="amount-input">
                <span class="currency">¥</span>
                <input type="number" v-model="editAccountData.dailyLimit" placeholder="Enter limit" step="0.01" min="0" />
              </div>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button class="btn cancel" @click="showEditModal = false">Cancel</button>
          <button class="btn primary" @click="updateAccount" :disabled="editLoading">
            {{ editLoading ? 'Saving...' : 'Save' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { accountApi } from '@/api/api'
import { maskAccountNumber, maskBalance } from '@/utils/desensitize'

export default {
  name: 'Accounts',
  setup() {
    const router = useRouter()
    const { proxy } = getCurrentInstance()
    const selectedAccount = ref(null)
    const accounts = ref([])
    const loading = ref(false)
    
    // 创建账户相关
    const showCreateModal = ref(false)
    const createLoading = ref(false)
    const newAccount = reactive({
      accountName: '',
      currency: 'CNY',
      dailyLimit: 50000
    })
    
    // 编辑账户相关
    const showEditModal = ref(false)
    const editLoading = ref(false)
    const editAccountData = reactive({
      id: null,
      accountName: '',
      dailyLimit: 0,
      balance: 0
    })
    
    // 敏感信息显示状态
    const showAccountNumber = ref({})
    const showBalance = ref({})

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
        0: 'Frozen',
        1: 'Active',
        2: 'Closed'
      }
      return statusMap[status] || 'Unknown'
    }
    
    // 切换账号显示状态
    const toggleAccountNumber = (accountId) => {
      showAccountNumber.value[accountId] = !showAccountNumber.value[accountId]
    }
    
    // 切换余额显示状态
    const toggleBalance = (accountId) => {
      showBalance.value[accountId] = !showBalance.value[accountId]
    }
    
    // 获取显示的账号（脱敏或原始）
    const getDisplayAccountNumber = (account) => {
      const show = showAccountNumber.value[account.id]
      return show ? account.accountNumber : maskAccountNumber(account.accountNumber)
    }
    
    // 获取显示的余额（脱敏或原始）
    const getDisplayBalance = (account) => {
      const show = showBalance.value[account.id]
      return show ? formatNumber(account.balance) : maskBalance(account.balance)
    }
    
    // 切换详情弹窗中的账号显示
    const toggleDetailAccountNumber = () => {
      if (selectedAccount.value) {
        toggleAccountNumber(selectedAccount.value.id)
      }
    }
    
    // 切换详情弹窗中的余额显示
    const toggleDetailBalance = () => {
      if (selectedAccount.value) {
        toggleBalance(selectedAccount.value.id)
      }
    }
    
    // 获取详情弹窗中的账号显示
    const getDetailAccountNumber = () => {
      if (!selectedAccount.value) return ''
      const show = showAccountNumber.value[selectedAccount.value.id]
      return show ? selectedAccount.value.accountNumber : maskAccountNumber(selectedAccount.value.accountNumber)
    }
    
    // 获取详情弹窗中的余额显示
    const getDetailBalance = () => {
      if (!selectedAccount.value) return '0.00'
      const show = showBalance.value[selectedAccount.value.id]
      return show ? formatNumber(selectedAccount.value.balance) : maskBalance(selectedAccount.value.balance)
    }

    const loadAccounts = async () => {
      loading.value = true
      try {
        const response = await accountApi.getAccounts()
        const { code, data, message } = response.data
        if (code === 1 || code === 200) {
          accounts.value = data || []
        } else {
          proxy.$message.error(message || 'Failed to get account list')
        }
      } catch (error) {
        console.error('Load accounts error:', error)
        proxy.$message.error('Failed to get account list')
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
    
    // 创建账户
    const createAccount = async () => {
      if (!newAccount.accountName) {
        proxy.$message.error('Please enter account name')
        return
      }
      
      createLoading.value = true
      try {
        const response = await accountApi.createAccount({
          accountName: newAccount.accountName,
          currency: newAccount.currency,
          dailyLimit: newAccount.dailyLimit
        })
        const { code, data, message } = response.data
        if (code === 1 || code === 200) {
          proxy.$message.success('Account created successfully')
          showCreateModal.value = false
          // 重置表单
          newAccount.accountName = ''
          newAccount.currency = 'CNY'
          newAccount.dailyLimit = 50000
          // 刷新列表
          await loadAccounts()
        } else {
          proxy.$message.error(message || 'Failed to create account')
        }
      } catch (error) {
        console.error('Create account error:', error)
        proxy.$message.error('Failed to create account')
      } finally {
        createLoading.value = false
      }
    }
    
    // 编辑账户
    const editAccount = (account) => {
      editAccountData.id = account.id
      editAccountData.accountName = account.accountName || ''
      editAccountData.dailyLimit = account.dailyLimit || 0
      editAccountData.balance = account.balance || 0
      selectedAccount.value = null
      showEditModal.value = true
    }
    
    // 更新账户
    const updateAccount = async () => {
      if (!editAccountData.accountName) {
        proxy.$message.error('Please enter account name')
        return
      }
      
      editLoading.value = true
      try {
        const response = await accountApi.updateAccount(editAccountData.id, {
          accountName: editAccountData.accountName,
          dailyLimit: editAccountData.dailyLimit,
          balance: editAccountData.balance
        })
        const { code, data, message } = response.data
        if (code === 1 || code === 200) {
          proxy.$message.success('Account updated successfully')
          showEditModal.value = false
          // 刷新列表
          await loadAccounts()
        } else {
          proxy.$message.error(message || 'Failed to update account')
        }
      } catch (error) {
        console.error('Update account error:', error)
        proxy.$message.error('Failed to update account')
      } finally {
        editLoading.value = false
      }
    }
    
    // 冻结账户
    const freezeAccount = async (account) => {
      if (!confirm('Are you sure you want to freeze this account? It will be unable to conduct transactions after freezing.')) {
        return
      }
      
      try {
        const response = await accountApi.updateStatus(account.id, 0)
        const { code, message } = response.data
        if (code === 1 || code === 200) {
          proxy.$message.success('Account has been frozen')
          selectedAccount.value = null
          await loadAccounts()
        } else {
          proxy.$message.error(message || 'Failed to freeze account')
        }
      } catch (error) {
        console.error('Freeze account error:', error)
        proxy.$message.error('Failed to freeze account')
      }
    }
    
    // 解冻账户
    const unfreezeAccount = async (account) => {
      try {
        const response = await accountApi.updateStatus(account.id, 1)
        const { code, message } = response.data
        if (code === 1 || code === 200) {
          proxy.$message.success('Account has been unfrozen')
          selectedAccount.value = null
          await loadAccounts()
        } else {
          proxy.$message.error(message || 'Failed to unfreeze account')
        }
      } catch (error) {
        console.error('Unfreeze account error:', error)
        proxy.$message.error('Failed to unfreeze account')
      }
    }

    onMounted(() => {
      loadAccounts()
    })

    return {
      accounts,
      selectedAccount,
      showCreateModal,
      createLoading,
      newAccount,
      showEditModal,
      editLoading,
      editAccountData,
      showAccountNumber,
      showBalance,
      formatNumber,
      formatAccountNumber,
      formatDateTime,
      getStatusClass,
      getStatusText,
      toggleAccountNumber,
      toggleBalance,
      getDisplayAccountNumber,
      getDisplayBalance,
      toggleDetailAccountNumber,
      toggleDetailBalance,
      getDetailAccountNumber,
      getDetailBalance,
      viewDetail,
      transfer,
      createAccount,
      editAccount,
      updateAccount,
      freezeAccount,
      unfreezeAccount
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

.account-number-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.account-balance {
  margin-bottom: var(--spacing-xl);
}

.balance-row {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.balance-label-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
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

/* label行 - 包含label和眼睛按钮 */
.label-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  min-height: 22px;
}

.label-row .label {
  margin: 0;
}

/* 小眼睛按钮样式 */
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

.btn.warning {
  background: rgba(255, 193, 7, 0.2);
  color: #ffc107;
  border: 1px solid rgba(255, 193, 7, 0.3);
}

.btn.warning:hover {
  background: rgba(255, 193, 7, 0.3);
  transform: translateY(-2px);
}

.btn.danger {
  background: rgba(239, 83, 80, 0.2);
  color: #ef5350;
  border: 1px solid rgba(239, 83, 80, 0.3);
}

.btn.danger:hover {
  background: rgba(239, 83, 80, 0.3);
  transform: translateY(-2px);
}

.btn.success {
  background: rgba(56, 239, 125, 0.2);
  color: #38ef7d;
  border: 1px solid rgba(56, 239, 125, 0.3);
}

.btn.success:hover {
  background: rgba(56, 239, 125, 0.3);
  transform: translateY(-2px);
}

/* 表单样式 */
.form-group {
  margin-bottom: var(--spacing-xl);
}

.form-group label {
  display: block;
  margin-bottom: var(--spacing-sm);
  color: var(--text-on-gradient);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
}

.form-group label .required {
  color: var(--status-danger);
}

.form-group label .test-hint {
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-normal);
}

.form-group input,
.form-group select {
  width: 100%;
  padding: var(--spacing-lg) var(--spacing-xl);
  border: 1px solid var(--input-border);
  border-radius: var(--radius-xl);
  font-size: var(--font-size-md);
  box-sizing: border-box;
  background: var(--input-bg);
  color: var(--color-white);
  transition: all var(--transition-normal);
}

.form-group select option {
  background: #667eea;
  color: var(--color-white);
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: var(--input-border-focus);
  background: var(--input-bg-focus);
}

.amount-input {
  position: relative;
  display: flex;
  align-items: center;
}

.amount-input .currency {
  position: absolute;
  left: var(--spacing-lg);
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-lg);
}

.amount-input input {
  padding-left: 30px;
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
