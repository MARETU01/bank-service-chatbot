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
  max-width: 1200px;
  margin: 0 auto;
}

.accounts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.accounts-header h2 {
  margin: 0;
  color: white;
  font-size: 28px;
  font-weight: 600;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.add-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.accounts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 25px;
  margin-bottom: 30px;
}

.account-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s;
}

.account-card:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

.account-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.account-type {
  display: flex;
  align-items: center;
  gap: 8px;
}

.type-icon {
  font-size: 28px;
}

.type-name {
  font-size: 16px;
  font-weight: 600;
  color: white;
}

.account-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.account-status.active {
  background: rgba(56, 239, 125, 0.2);
  color: #38ef7d;
}

.account-status.frozen {
  background: rgba(255, 215, 0, 0.2);
  color: #ffd700;
}

.account-number {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 16px;
  letter-spacing: 2px;
}

.account-balance {
  margin-bottom: 20px;
}

.balance-label {
  display: block;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 4px;
}

.balance-amount {
  font-size: 28px;
  font-weight: 700;
  color: white;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.account-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.action-btn.primary {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.action-btn.primary:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.action-btn.secondary {
  background: transparent;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.action-btn.secondary:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

.account-detail-section {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.account-detail-section h2 {
  margin: 0 0 20px 0;
  color: white;
  font-size: 24px;
  font-weight: 600;
}

.detail-card {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.detail-row {
  display: flex;
  flex-direction: column;
  padding: 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
}

.detail-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 8px;
}

.detail-value {
  font-size: 16px;
  color: white;
  font-weight: 500;
}

.detail-value.amount {
  font-size: 20px;
  color: #ffd700;
  font-weight: 700;
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
  z-index: 1000;
}

.modal {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  width: 100%;
  max-width: 400px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-header h3 {
  margin: 0;
  color: white;
  font-size: 20px;
  font-weight: 600;
}

.close-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-size: 20px;
  cursor: pointer;
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.modal-body {
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  font-weight: 500;
}

.form-group select,
.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  font-size: 14px;
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.form-group select option {
  background: #667eea;
  color: white;
}

.form-group select:focus,
.form-group input:focus {
  outline: none;
  border-color: rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.15);
}

.form-group input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn {
  padding: 10px 24px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn.cancel {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.btn.cancel:hover {
  background: rgba(255, 255, 255, 0.2);
}

.btn.primary {
  background: rgba(255, 255, 255, 0.25);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.btn.primary:hover {
  background: rgba(255, 255, 255, 0.35);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}
</style>
