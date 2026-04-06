<template>
  <div class="transfers">
    <div class="page-header">
      <h2>Transfer Service</h2>
    </div>

    <div class="transfer-container">
      <!-- Transfer Form -->
      <div class="transfer-form-section">
        <div class="form-card">
          <h3>New Transfer</h3>
          <form @submit.prevent="submitTransfer">
            <div class="form-group">
              <label>From Account <span class="required">*</span></label>
              <select v-model="transfer.fromAccount" required>
                <option value="">Please select an account</option>
                <option v-for="account in accounts" :key="account.id" :value="account.id">
                  {{ account.accountName || 'Account' }} (****{{ formatAccountLastFour(account.accountNumber) }}) - Balance: ¥{{ formatNumber(account.balance) }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>Transfer Type <span class="required">*</span></label>
              <div class="transfer-type-selector">
                <button 
                  type="button"
                  class="type-btn" 
                  :class="{ active: transferType === 'internal' }"
                  @click="transferType = 'internal'"
                >
                  My Account
                </button>
                <button 
                  type="button"
                  class="type-btn" 
                  :class="{ active: transferType === 'external' }"
                  @click="transferType = 'external'"
                >
                  Others' Account
                </button>
              </div>
            </div>

            <!-- Internal Transfer -->
            <div class="form-group" v-if="transferType === 'internal'">
              <label>To Account <span class="required">*</span></label>
              <select v-model="selectedToAccount" required>
                <option value="">Please select an account</option>
                <option v-for="account in availableToAccounts" :key="account.id" :value="account">
                  {{ account.accountName || 'Account' }} ({{ account.accountNumber }}) - Balance: ¥{{ formatNumber(account.balance) }}
                </option>
              </select>
            </div>

            <!-- External Transfer -->
            <div class="form-group" v-if="transferType === 'external'">
              <label>Recipient Account Number <span class="required">*</span></label>
              <input 
                type="text" 
                v-model="transfer.toAccountNumber" 
                placeholder="Please enter recipient account number"
              />
            </div>

            <div class="form-group" v-if="transferType === 'external'">
              <label>Recipient Name <span class="required">*</span></label>
              <input 
                type="text" 
                v-model="transfer.toAccountName" 
                placeholder="Please enter recipient name"
              />
            </div>

            <div class="form-group" v-if="transferType === 'external'">
              <label>Recipient Bank</label>
              <input 
                type="text" 
                v-model="transfer.toBankName" 
                placeholder="Please enter bank name (optional)"
              />
            </div>

            <div class="form-group">
              <label>Amount <span class="required">*</span></label>
              <div class="amount-input">
                <span class="currency">¥</span>
                <input 
                  type="number" 
                  v-model="transfer.amount" 
                  placeholder="Please enter amount"
                  step="0.01"
                  min="0.01"
                  required
                />
              </div>
              <span class="error-msg" v-if="insufficientBalance">Insufficient balance</span>
            </div>

            <div class="form-group">
              <label>Remark</label>
              <textarea 
                v-model="transfer.remark" 
                placeholder="Please enter remark (optional)"
                rows="3"
              ></textarea>
            </div>

            <div class="form-actions">
              <button type="button" class="btn reset" @click="resetForm">Reset</button>
              <button type="submit" class="btn primary" :disabled="!canSubmit || loading">
                {{ loading ? 'Processing...' : 'Confirm Transfer' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- Right Sidebar -->
      <div class="payees-section">
        <!-- Transfer History -->
        <div class="history-card">
          <div class="card-header">
            <h3>Recent Transfers</h3>
          </div>
          <div class="history-list" v-if="recentTransfers.length > 0">
            <div class="history-item" v-for="item in recentTransfers" :key="item.id">
              <div class="history-info">
                <div class="history-name">{{ item.toAccountName || 'Recipient' }}</div>
                <div class="history-time">{{ formatDateTime(item.createdAt) }}</div>
              </div>
              <div class="history-amount" :class="getStatusClass(item.status)">
                -¥{{ formatNumber(item.amount) }}
              </div>
            </div>
          </div>
          <div class="empty-state" v-else>
            <p>No transfer records</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Confirmation Modal -->
    <div class="modal-overlay" v-if="showConfirm" @click="showConfirm = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>Confirm Transfer</h3>
          <button class="close-btn" @click="showConfirm = false">×</button>
        </div>
        <div class="modal-body">
          <div class="confirm-info">
            <div class="info-row">
              <span class="label">From Account</span>
              <span class="value">{{ selectedAccountName }}</span>
            </div>
            <div class="info-row">
              <span class="label">Recipient</span>
              <span class="value">{{ transfer.toAccountName }}</span>
            </div>
            <div class="info-row">
              <span class="label">Account Number</span>
              <span class="value">{{ transfer.toAccountNumber }}</span>
            </div>
            <div class="info-row">
              <span class="label">Bank</span>
              <span class="value">{{ transfer.toBankName || '-' }}</span>
            </div>
            <div class="info-row highlight">
              <span class="label">Amount</span>
              <span class="value amount">¥{{ formatNumber(transfer.amount) }}</span>
            </div>
            <div class="info-row">
              <span class="label">Remark</span>
              <span class="value">{{ transfer.remark || 'None' }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn cancel" @click="showConfirm = false">Back</button>
          <button class="btn primary" @click="showPasswordInput" :disabled="confirmLoading">
            Next
          </button>
        </div>
      </div>
    </div>

    <!-- Password Input Modal -->
    <div class="modal-overlay" v-if="showPasswordModal" @click="showPasswordModal = false">
      <div class="modal password-modal" @click.stop>
        <div class="modal-header">
          <h3>Enter Payment Password</h3>
          <button class="close-btn" @click="closePasswordModal">×</button>
        </div>
        <div class="modal-body">
          <div class="password-info">
            <p>Please verify your identity to complete the transfer</p>
            <div class="amount-display">
              <span class="label">Amount</span>
              <span class="value">¥{{ formatNumber(transfer.amount) }}</span>
            </div>
          </div>
          <div class="password-input-group">
            <input 
              type="password" 
              v-model="transfer.payPassword" 
              placeholder="Please enter payment password"
              maxlength="6"
              ref="passwordInput"
              @keyup.enter="confirmTransfer"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn cancel" @click="closePasswordModal">Back</button>
          <button class="btn primary" @click="confirmTransfer" :disabled="!transfer.payPassword || confirmLoading">
            {{ confirmLoading ? 'Processing...' : 'Confirm Payment' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Success Modal -->
    <div class="modal-overlay" v-if="showSuccess" @click="showSuccess = false">
      <div class="modal success-modal" @click.stop>
        <div class="success-icon">✓</div>
        <h3>Transfer Successful</h3>
        <p>Transferred ¥{{ formatNumber(transfer.amount) }} to {{ transfer.toAccountName }}</p>
        <div class="modal-footer">
          <button class="btn primary" @click="showSuccess = false; resetForm()">Done</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, getCurrentInstance, watch } from 'vue'
import { useRoute } from 'vue-router'
import { accountApi, transferApi } from '@/api/api'

export default {
  name: 'Transfers',
  setup() {
    const route = useRoute()
    const { proxy } = getCurrentInstance()
    const showConfirm = ref(false)
    const showSuccess = ref(false)
    const loading = ref(false)
    const confirmLoading = ref(false)

    const accounts = ref([])
    const recentTransfers = ref([])

    const transferType = ref('internal') // 'internal' or 'external'
    const selectedToAccount = ref(null) // selected recipient account object

    const transfer = reactive({
      fromAccount: '',
      toAccountNumber: '',
      toAccountName: '',
      toBankName: '',
      amount: '',
      payPassword: '',
      remark: ''
    })

    const showPasswordModal = ref(false)

    const selectedAccount = computed(() => {
      return accounts.value.find(a => a.id === transfer.fromAccount)
    })

    const selectedAccountName = computed(() => {
      return selectedAccount.value ? (selectedAccount.value.accountName || 'Account') : ''
    })

    // Available recipient accounts (excluding current from account)
    const availableToAccounts = computed(() => {
      return accounts.value.filter(a => a.id !== transfer.fromAccount)
    })

    const insufficientBalance = computed(() => {
      if (!transfer.amount || !selectedAccount.value) return false
      return parseFloat(transfer.amount) > selectedAccount.value.balance
    })

    const canSubmit = computed(() => {
      if (!transfer.fromAccount || !transfer.amount || insufficientBalance.value) {
        return false
      }
      
      if (transferType.value === 'internal') {
        return selectedToAccount.value !== null
      } else {
        return transfer.toAccountNumber && transfer.toAccountName
      }
    })

    const formatNumber = (num) => {
      if (!num) return '0.00'
      return Number(num).toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })
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

    const formatAccountLastFour = (accountNumber) => {
      if (!accountNumber) return '****'
      const cleanNumber = accountNumber.replace(/\s/g, '')
      return cleanNumber.slice(-4)
    }

    const getStatusClass = (status) => {
      const statusMap = {
        0: 'failed',
        1: 'success',
        2: 'pending'
      }
      return statusMap[status] || 'pending'
    }

    const loadAccounts = async () => {
      try {
        const response = await accountApi.getAccounts()
        const { code, data } = response.data
        if (code === 1 || code === 200) {
          accounts.value = data || []
          // If URL has accountId parameter, select it by default
          if (route.query.accountId) {
            transfer.fromAccount = parseInt(route.query.accountId)
          } else if (data && data.length > 0) {
            transfer.fromAccount = data[0].id
          }
        }
      } catch (error) {
        console.error('Load accounts error:', error)
      }
    }

    const loadRecentTransfers = async () => {
      if (!transfer.fromAccount) return
      try {
        const response = await transferApi.getTransfers({ accountId: transfer.fromAccount, page: 1, size: 5 })
        const { code, data } = response.data
        if (code === 1 || code === 200) {
          recentTransfers.value = data || []
        }
      } catch (error) {
        console.error('Load transfers error:', error)
      }
    }

    const resetForm = () => {
      transferType.value = 'internal'
      transfer.fromAccount = ''
      transfer.toAccountNumber = ''
      transfer.toAccountName = ''
      transfer.toBankName = ''
      transfer.amount = ''
      transfer.payPassword = ''
      transfer.remark = ''
      selectedToAccount.value = null
    }

    // Watch transfer type changes, sync form data
    watch(() => transferType.value, (newType) => {
      if (newType === 'internal') {
        // Switch to internal transfer, clear external data
        transfer.toAccountNumber = ''
        transfer.toAccountName = ''
        transfer.toBankName = ''
      } else {
        // Switch to external transfer, clear internal selection
        selectedToAccount.value = null
      }
    })

    // Watch selected recipient account, sync to transfer object
    watch(() => selectedToAccount.value, (newAccount) => {
      if (newAccount) {
        transfer.toAccountNumber = newAccount.accountNumber
        transfer.toAccountName = newAccount.accountName || 'My Account'
        transfer.toBankName = ''
      }
    })

    const submitTransfer = () => {
      if (!canSubmit.value) return
      showConfirm.value = true
    }

    const showPasswordInput = () => {
      showConfirm.value = false
      showPasswordModal.value = true
      // Auto focus password input
      setTimeout(() => {
        if (passwordInput.value) {
          passwordInput.value.focus()
        }
      }, 100)
    }

    const closePasswordModal = () => {
      showPasswordModal.value = false
      transfer.payPassword = ''
    }

    const confirmTransfer = async () => {
      if (!selectedAccount.value) return
      if (!transfer.payPassword) {
        proxy.$message.error('Please enter payment password')
        return
      }
      
      confirmLoading.value = true
      try {
        const transferData = {
          fromAccountId: transfer.fromAccount,
          toAccountNumber: transfer.toAccountNumber,
          toAccountName: transfer.toAccountName,
          toBankName: transfer.toBankName || '',
          amount: parseFloat(transfer.amount),
          remark: transfer.remark || '',
          payPassword: transfer.payPassword
        }

        const response = await transferApi.executeTransfer(transferData)
        const { code, data, message } = response.data
        
        if (code === 1 || code === 200) {
          showPasswordModal.value = false
          showSuccess.value = true
          // Refresh account list and transfer records
          await loadAccounts()
          await loadRecentTransfers()
        } else {
          proxy.$message.error(message || 'Transfer failed')
        }
      } catch (error) {
        console.error('Transfer error:', error)
        const message = error.response?.data?.message || 'Transfer failed, please check if password is correct'
        proxy.$message.error(message)
      } finally {
        confirmLoading.value = false
      }
    }

    onMounted(() => {
      loadAccounts()
    })

    // Watch account changes, load transfer records
    watch(() => transfer.fromAccount, () => {
      loadRecentTransfers()
    })

    const passwordInput = ref(null)

    return {
      accounts,
      recentTransfers,
      transfer,
      transferType,
      selectedToAccount,
      availableToAccounts,
      showConfirm,
      showSuccess,
      showPasswordModal,
      loading,
      confirmLoading,
      selectedAccountName,
      insufficientBalance,
      canSubmit,
      formatNumber,
      formatDateTime,
      formatAccountLastFour,
      getStatusClass,
      resetForm,
      submitTransfer,
      showPasswordInput,
      closePasswordModal,
      confirmTransfer,
      passwordInput
    }
  }
}
</script>

<style scoped>
.transfers {
  max-width: var(--container-max-width);
  margin: 0 auto;
}

.page-header {
  margin-bottom: var(--spacing-3xl);
}

.page-header h2 {
  margin: 0;
  color: var(--color-white);
  font-size: var(--font-size-5xl);
  font-weight: var(--font-weight-semibold);
}

.transfer-container {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: var(--spacing-2xl);
}

.form-card, .history-card {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-3xl);
  padding: var(--spacing-2xl);
  border: 1px solid var(--glass-border);
}

.form-card h3 {
  margin: 0 0 var(--spacing-2xl) 0;
  color: var(--color-white);
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-semibold);
}

.form-group {
  margin-bottom: var(--spacing-2xl);
}

.form-group label {
  display: block;
  margin-bottom: var(--spacing-sm);
  color: var(--text-on-gradient);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
}

.required {
  color: var(--status-danger);
}

.form-group select,
.form-group input,
.form-group textarea {
  width: 100%;
  padding: var(--spacing-lg) var(--spacing-2xl);
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

.form-group select:focus,
.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--input-border-focus);
  background: var(--input-bg-focus);
}

.error-msg {
  color: var(--status-danger);
  font-size: var(--font-size-xs);
  margin-top: var(--spacing-xs);
  display: block;
}

.amount-input {
  position: relative;
  display: flex;
  align-items: center;
}

.currency {
  position: absolute;
  left: var(--spacing-lg);
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-lg);
}

.amount-input input {
  padding-left: 30px;
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-semibold);
}

.form-actions {
  display: flex;
  gap: var(--spacing-lg);
  justify-content: flex-end;
  margin-top: var(--spacing-2xl);
}

.btn {
  padding: var(--spacing-lg) var(--spacing-3xl);
  border: none;
  border-radius: var(--radius-xl);
  cursor: pointer;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-normal);
}

.btn.reset {
  background: var(--glass-bg-light);
  color: var(--color-white);
  border: 1px solid var(--glass-border-hover);
}

.btn.reset:hover {
  background: var(--glass-border-hover);
}

.btn.primary {
  background: var(--glass-bg-hover);
  color: var(--color-white);
  border: 1px solid var(--glass-border-active);
}

.btn.primary:hover:not(:disabled) {
  background: var(--glass-bg-active);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Transfer Type Selector */
.transfer-type-selector {
  display: flex;
  gap: var(--spacing-md);
}

.type-btn {
  flex: 1;
  padding: var(--spacing-lg) var(--spacing-xl);
  background: var(--glass-bg-light);
  color: var(--color-white);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  cursor: pointer;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-normal);
}

.type-btn:hover {
  background: var(--glass-bg-hover);
  border-color: var(--glass-border-hover);
}

.type-btn.active {
  background: var(--glass-bg-hover);
  border-color: var(--glass-border-active);
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.3);
}

/* History */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.card-header h3 {
  margin: 0;
  color: var(--color-white);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.history-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.history-name {
  font-weight: var(--font-weight-semibold);
  color: var(--color-white);
}

.history-time {
  font-size: var(--font-size-xs);
  color: var(--text-on-gradient-muted);
}

.history-amount {
  font-weight: var(--font-weight-bold);
}

.history-amount.success {
  color: var(--status-success);
}

.history-amount.pending {
  color: var(--status-warning);
}

.history-amount.failed {
  color: var(--status-danger);
}

.empty-state {
  text-align: center;
  padding: var(--spacing-2xl);
  color: var(--text-on-gradient-muted);
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
  max-width: 450px;
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

.confirm-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-row .label {
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-md);
}

.info-row .value {
  color: var(--color-white);
  font-weight: var(--font-weight-medium);
}

.info-row.highlight .value,
.info-row .value.amount {
  font-size: var(--font-size-4xl);
  color: var(--status-warning);
  font-weight: var(--font-weight-bold);
}

/* Payment Password Modal */
.password-modal {
  max-width: 400px;
}

.password-info {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
}

.password-info p {
  color: var(--text-on-gradient-muted);
  margin: 0 0 var(--spacing-lg) 0;
}

.amount-display {
  background: var(--glass-bg-light);
  padding: var(--spacing-xl);
  border-radius: var(--radius-xl);
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-2xl);
}

.amount-display .label {
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-md);
}

.amount-display .value {
  color: var(--status-warning);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
}

.password-input-group {
  margin-bottom: var(--spacing-lg);
}

.password-input-group input {
  width: 100%;
  padding: var(--spacing-xl) var(--spacing-2xl);
  font-size: var(--font-size-2xl);
  text-align: center;
  letter-spacing: 8px;
}

.modal-footer {
  display: flex;
  gap: var(--spacing-lg);
  justify-content: flex-end;
  margin-top: var(--spacing-2xl);
  padding-top: var(--spacing-lg);
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}

.btn.cancel {
  background: var(--glass-bg-light);
  color: var(--color-white);
  border: 1px solid var(--glass-border-hover);
}

.btn.cancel:hover {
  background: var(--glass-border-hover);
}

/* Success Modal */
.success-modal {
  text-align: center;
}

.success-icon {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-full);
  background: var(--gradient-income);
  color: var(--color-white);
  font-size: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--spacing-2xl);
  box-shadow: 0 10px 30px rgba(56, 239, 125, 0.3);
}

.success-modal h3 {
  margin: 0 0 var(--spacing-lg) 0;
  color: var(--color-white);
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-semibold);
}

.success-modal p {
  color: var(--text-on-gradient-secondary);
  margin: 0 0 var(--spacing-2xl) 0;
}

.success-modal .modal-footer {
  justify-content: center;
  border-top: none;
  margin-top: 0;
  padding-top: 0;
}

/* Responsive */
@media (max-width: 992px) {
  .transfer-container {
    grid-template-columns: 1fr;
  }
}
</style>
