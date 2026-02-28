<template>
  <div class="accounts">
    <!-- 账户列表 -->
    <div class="accounts-header">
      <h2>我的账户</h2>
      <button class="add-btn" @click="showAddModal = true">
        <span>+</span> 添加账户
      </button>
    </div>

    <div class="accounts-grid">
      <div class="account-card" v-for="account in accounts" :key="account.id">
        <div class="account-header">
          <div class="account-type">
            <span class="type-icon">{{ account.typeIcon }}</span>
            <span class="type-name">{{ account.typeName }}</span>
          </div>
          <span class="account-status" :class="account.status">{{ account.statusText }}</span>
        </div>
        <div class="account-number">
          <span>**** **** **** {{ account.lastFourDigits }}</span>
        </div>
        <div class="account-balance">
          <span class="balance-label">可用余额</span>
          <span class="balance-amount">¥ {{ formatNumber(account.balance) }}</span>
        </div>
        <div class="account-actions">
          <button class="action-btn primary" @click="viewDetail(account)">详情</button>
          <button class="action-btn secondary" @click="transfer(account)">转账</button>
        </div>
      </div>
    </div>

    <!-- 账户详情 -->
    <div class="account-detail-section" v-if="selectedAccount">
      <h2>账户详情</h2>
      <div class="detail-card">
        <div class="detail-row">
          <span class="detail-label">账户类型</span>
          <span class="detail-value">{{ selectedAccount.typeName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">账户号码</span>
          <span class="detail-value">{{ selectedAccount.accountNumber }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">开户日期</span>
          <span class="detail-value">{{ selectedAccount.openDate }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">币种</span>
          <span class="detail-value">{{ selectedAccount.currency }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">可用余额</span>
          <span class="detail-value amount">¥ {{ formatNumber(selectedAccount.balance) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">冻结金额</span>
          <span class="detail-value">¥ {{ formatNumber(selectedAccount.frozenAmount || 0) }}</span>
        </div>
      </div>
    </div>

    <!-- 添加账户弹窗 -->
    <div class="modal-overlay" v-if="showAddModal" @click="showAddModal = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>添加新账户</h3>
          <button class="close-btn" @click="showAddModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>账户类型</label>
            <select v-model="newAccount.type">
              <option value="savings">储蓄账户</option>
              <option value="checking">支票账户</option>
              <option value="time">定期存款</option>
              <option value="investment">理财账户</option>
            </select>
          </div>
          <div class="form-group">
            <label>初始存款金额</label>
            <input type="number" v-model="newAccount.initialDeposit" placeholder="请输入金额" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn cancel" @click="showAddModal = false">取消</button>
          <button class="btn primary" @click="addAccount">确认添加</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'Accounts',
  setup() {
    const { proxy } = getCurrentInstance()
    const router = useRouter()
    const showAddModal = ref(false)
    const selectedAccount = ref(null)

    // 模拟账户数据
    const accounts = ref([
      {
        id: 1,
        type: 'savings',
        typeName: '储蓄账户',
        typeIcon: '💰',
        accountNumber: '6222 0212 3456 7890',
        lastFourDigits: '7890',
        balance: 125680.00,
        frozenAmount: 0,
        status: 'active',
        statusText: '正常',
        openDate: '2020-05-15',
        currency: 'CNY'
      },
      {
        id: 2,
        type: 'checking',
        typeName: '支票账户',
        typeIcon: '📋',
        accountNumber: '6222 0212 3456 1234',
        lastFourDigits: '1234',
        balance: 45200.50,
        frozenAmount: 5000,
        status: 'active',
        statusText: '正常',
        openDate: '2021-08-20',
        currency: 'CNY'
      },
      {
        id: 3,
        type: 'time',
        typeName: '定期存款',
        typeIcon: '📅',
        accountNumber: '6222 0212 3456 5678',
        lastFourDigits: '5678',
        balance: 100000.00,
        frozenAmount: 100000,
        status: 'frozen',
        statusText: '定期中',
        openDate: '2023-01-10',
        currency: 'CNY'
      },
      {
        id: 4,
        type: 'investment',
        typeName: '理财账户',
        typeIcon: '📈',
        accountNumber: '6222 0212 3456 9012',
        lastFourDigits: '9012',
        balance: 78500.00,
        frozenAmount: 0,
        status: 'active',
        statusText: '正常',
        openDate: '2022-03-25',
        currency: 'CNY'
      }
    ])

    const newAccount = reactive({
      type: 'savings',
      initialDeposit: ''
    })

    const formatNumber = (num) => {
      return Number(num).toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })
    }

    const viewDetail = (account) => {
      selectedAccount.value = account
    }

    const transfer = (account) => {
      router.push('/transfers?accountId=' + account.id)
    }

    const addAccount = () => {
      proxy.$message.info('添加账户功能待实现')
      showAddModal.value = false
    }

    return {
      accounts,
      showAddModal,
      selectedAccount,
      newAccount,
      formatNumber,
      viewDetail,
      transfer,
      addAccount
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
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
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

.account-number {
  font-size: var(--font-size-xl);
  color: var(--text-on-gradient-secondary);
  margin-bottom: var(--spacing-lg);
  letter-spacing: 2px;
}

.account-balance {
  margin-bottom: var(--spacing-2xl);
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

.account-detail-section {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-3xl);
  padding: var(--spacing-2xl);
  border: 1px solid var(--glass-border);
}

.account-detail-section h2 {
  margin: 0 0 var(--spacing-2xl) 0;
  color: var(--color-white);
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-semibold);
}

.detail-card {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-lg);
}

.detail-row {
  display: flex;
  flex-direction: column;
  padding: var(--spacing-lg);
  background: var(--glass-bg-light);
  border-radius: var(--radius-xl);
}

.detail-label {
  font-size: var(--font-size-xs);
  color: var(--text-on-gradient-muted);
  margin-bottom: var(--spacing-sm);
}

.detail-value {
  font-size: var(--font-size-xl);
  color: var(--color-white);
  font-weight: var(--font-weight-medium);
}

.detail-value.amount {
  font-size: var(--font-size-3xl);
  color: var(--status-warning);
  font-weight: var(--font-weight-bold);
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
  max-width: 400px;
  padding: var(--spacing-2xl);
  border: 1px solid var(--glass-border);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-2xl);
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

.modal-body {
  margin-bottom: var(--spacing-2xl);
}

.form-group {
  margin-bottom: var(--spacing-lg);
}

.form-group label {
  display: block;
  margin-bottom: var(--spacing-sm);
  color: var(--text-on-gradient);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
}

.form-group select,
.form-group input {
  width: 100%;
  padding: var(--spacing-lg) var(--spacing-2xl);
  border: 1px solid var(--input-border);
  border-radius: var(--radius-xl);
  font-size: var(--font-size-md);
  box-sizing: border-box;
  background: var(--input-bg);
  color: var(--color-white);
}

.form-group select option {
  background: #667eea;
  color: var(--color-white);
}

.form-group select:focus,
.form-group input:focus {
  outline: none;
  border-color: var(--input-border-focus);
  background: var(--input-bg-focus);
}

.form-group input::placeholder {
  color: var(--input-placeholder);
}

.modal-footer {
  display: flex;
  gap: var(--spacing-lg);
  justify-content: flex-end;
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
</style>
