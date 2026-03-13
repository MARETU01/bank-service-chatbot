<template>
  <div class="transfers">
    <div class="page-header">
      <h2>转账服务</h2>
    </div>

    <div class="transfer-container">
      <!-- 转账表单 -->
      <div class="transfer-form-section">
        <div class="form-card">
          <h3>新建转账</h3>
          <form @submit.prevent="submitTransfer">
            <div class="form-group">
              <label>付款账户 <span class="required">*</span></label>
              <select v-model="transfer.fromAccount" required>
                <option value="">请选择付款账户</option>
                <option v-for="account in accounts" :key="account.id" :value="account.id">
                  {{ account.accountName || '账户' }} (****{{ formatAccountLastFour(account.accountNumber) }}) - 余额：¥{{ formatNumber(account.balance) }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>转账类型 <span class="required">*</span></label>
              <div class="transfer-type-selector">
                <button 
                  type="button"
                  class="type-btn" 
                  :class="{ active: transferType === 'internal' }"
                  @click="transferType = 'internal'"
                >
                  本人账户
                </button>
                <button 
                  type="button"
                  class="type-btn" 
                  :class="{ active: transferType === 'external' }"
                  @click="transferType = 'external'"
                >
                  他人账户
                </button>
              </div>
            </div>

            <!-- 本人账户转账 -->
            <div class="form-group" v-if="transferType === 'internal'">
              <label>收款账户 <span class="required">*</span></label>
              <select v-model="selectedToAccount" required>
                <option value="">请选择收款账户</option>
                <option v-for="account in availableToAccounts" :key="account.id" :value="account">
                  {{ account.accountName || '账户' }} ({{ account.accountNumber }}) - 余额：¥{{ formatNumber(account.balance) }}
                </option>
              </select>
            </div>

            <!-- 他人账户转账 -->
            <div class="form-group" v-if="transferType === 'external'">
              <label>收款人账号 <span class="required">*</span></label>
              <input 
                type="text" 
                v-model="transfer.toAccountNumber" 
                placeholder="请输入收款人账号"
              />
            </div>

            <div class="form-group" v-if="transferType === 'external'">
              <label>收款人姓名 <span class="required">*</span></label>
              <input 
                type="text" 
                v-model="transfer.toAccountName" 
                placeholder="请输入收款人姓名"
              />
            </div>

            <div class="form-group" v-if="transferType === 'external'">
              <label>收款银行</label>
              <input 
                type="text" 
                v-model="transfer.toBankName" 
                placeholder="请输入开户行名称（选填）"
              />
            </div>

            <div class="form-group">
              <label>转账金额 <span class="required">*</span></label>
              <div class="amount-input">
                <span class="currency">¥</span>
                <input 
                  type="number" 
                  v-model="transfer.amount" 
                  placeholder="请输入金额"
                  step="0.01"
                  min="0.01"
                  required
                />
              </div>
              <span class="error-msg" v-if="insufficientBalance">余额不足</span>
            </div>

            <div class="form-group">
              <label>备注</label>
              <textarea 
                v-model="transfer.remark" 
                placeholder="请输入备注（选填）"
                rows="3"
              ></textarea>
            </div>

            <div class="form-actions">
              <button type="button" class="btn reset" @click="resetForm">重置</button>
              <button type="submit" class="btn primary" :disabled="!canSubmit || loading">
                {{ loading ? '处理中...' : '确认转账' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- 右侧区域 -->
      <div class="payees-section">
        <!-- 转账记录 -->
        <div class="history-card">
          <div class="card-header">
            <h3>最近转账记录</h3>
          </div>
          <div class="history-list" v-if="recentTransfers.length > 0">
            <div class="history-item" v-for="item in recentTransfers" :key="item.id">
              <div class="history-info">
                <div class="history-name">{{ item.toAccountName || '收款人' }}</div>
                <div class="history-time">{{ formatDateTime(item.createdAt) }}</div>
              </div>
              <div class="history-amount" :class="getStatusClass(item.status)">
                -¥{{ formatNumber(item.amount) }}
              </div>
            </div>
          </div>
          <div class="empty-state" v-else>
            <p>暂无转账记录</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 确认弹窗 -->
    <div class="modal-overlay" v-if="showConfirm" @click="showConfirm = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>确认转账信息</h3>
          <button class="close-btn" @click="showConfirm = false">×</button>
        </div>
        <div class="modal-body">
          <div class="confirm-info">
            <div class="info-row">
              <span class="label">付款账户</span>
              <span class="value">{{ selectedAccountName }}</span>
            </div>
            <div class="info-row">
              <span class="label">收款人</span>
              <span class="value">{{ transfer.toAccountName }}</span>
            </div>
            <div class="info-row">
              <span class="label">收款账号</span>
              <span class="value">{{ transfer.toAccountNumber }}</span>
            </div>
            <div class="info-row">
              <span class="label">收款银行</span>
              <span class="value">{{ transfer.toBankName || '-' }}</span>
            </div>
            <div class="info-row highlight">
              <span class="label">转账金额</span>
              <span class="value amount">¥{{ formatNumber(transfer.amount) }}</span>
            </div>
            <div class="info-row">
              <span class="label">备注</span>
              <span class="value">{{ transfer.remark || '无' }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn cancel" @click="showConfirm = false">返回修改</button>
          <button class="btn primary" @click="showPasswordInput" :disabled="confirmLoading">
            下一步
          </button>
        </div>
      </div>
    </div>

    <!-- 支付密码输入弹窗 -->
    <div class="modal-overlay" v-if="showPasswordModal" @click="showPasswordModal = false">
      <div class="modal password-modal" @click.stop>
        <div class="modal-header">
          <h3>输入支付密码</h3>
          <button class="close-btn" @click="closePasswordModal">×</button>
        </div>
        <div class="modal-body">
          <div class="password-info">
            <p>请验证您的身份以完成转账</p>
            <div class="amount-display">
              <span class="label">转账金额</span>
              <span class="value">¥{{ formatNumber(transfer.amount) }}</span>
            </div>
          </div>
          <div class="password-input-group">
            <input 
              type="password" 
              v-model="transfer.payPassword" 
              placeholder="请输入支付密码"
              maxlength="6"
              ref="passwordInput"
              @keyup.enter="confirmTransfer"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn cancel" @click="closePasswordModal">返回</button>
          <button class="btn primary" @click="confirmTransfer" :disabled="!transfer.payPassword || confirmLoading">
            {{ confirmLoading ? '处理中...' : '确认支付' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 转账成功弹窗 -->
    <div class="modal-overlay" v-if="showSuccess" @click="showSuccess = false">
      <div class="modal success-modal" @click.stop>
        <div class="success-icon">✓</div>
        <h3>转账成功</h3>
        <p>已向 {{ transfer.toAccountName }} 转账 ¥{{ formatNumber(transfer.amount) }}</p>
        <div class="modal-footer">
          <button class="btn primary" @click="showSuccess = false; resetForm()">完成</button>
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

    const transferType = ref('internal') // 'internal' 或 'external'
    const selectedToAccount = ref(null) // 选中的目标账户对象

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
      return selectedAccount.value ? (selectedAccount.value.accountName || '账户') : ''
    })

    // 可用的目标账户列表（排除当前选中的付款账户）
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
          // 如果 URL 中有 accountId 参数，默认选中
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

    // 监听转账类型变化，同步更新表单数据
    watch(() => transferType.value, (newType) => {
      if (newType === 'internal') {
        // 切换到本人账户，清空外部转账数据
        transfer.toAccountNumber = ''
        transfer.toAccountName = ''
        transfer.toBankName = ''
      } else {
        // 切换到外部账户，清空内部选择
        selectedToAccount.value = null
      }
    })

    // 监听选中的目标账户，同步到 transfer 对象
    watch(() => selectedToAccount.value, (newAccount) => {
      if (newAccount) {
        transfer.toAccountNumber = newAccount.accountNumber
        transfer.toAccountName = newAccount.accountName || '本人账户'
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
      // 自动聚焦到密码输入框
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
        proxy.$message.error('请输入支付密码')
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
          // 刷新转账记录
          await loadRecentTransfers()
        } else {
          proxy.$message.error(message || '转账失败')
        }
      } catch (error) {
        console.error('Transfer error:', error)
        const message = error.response?.data?.message || '转账失败，请检查密码是否正确'
        proxy.$message.error(message)
      } finally {
        confirmLoading.value = false
      }
    }

    onMounted(() => {
      loadAccounts()
    })

    // 监听账户变化，加载转账记录
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

/* 转账类型选择器 */
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

/* 历史记录 */
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

/* 支付密码弹窗样式 */
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

/* 成功弹窗 */
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

/* 响应式设计 */
@media (max-width: 992px) {
  .transfer-container {
    grid-template-columns: 1fr;
  }
}
</style>
