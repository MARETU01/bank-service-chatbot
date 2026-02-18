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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'Accounts',
  setup() {
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
      alert('添加账户功能待实现')
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
}

.accounts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.accounts-header h2 {
  margin: 0;
  color: #1e3a5f;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #1e3a5f;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.add-btn:hover {
  background: #2d5a87;
}

.accounts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.account-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
}

.account-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12);
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
  font-size: 24px;
}

.type-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.account-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.account-status.active {
  background: #d4edda;
  color: #28a745;
}

.account-status.frozen {
  background: #fff3cd;
  color: #856404;
}

.account-number {
  font-size: 18px;
  color: #666;
  margin-bottom: 16px;
  letter-spacing: 2px;
}

.account-balance {
  margin-bottom: 20px;
}

.balance-label {
  display: block;
  font-size: 13px;
  color: #999;
  margin-bottom: 4px;
}

.balance-amount {
  font-size: 28px;
  font-weight: bold;
  color: #1e3a5f;
}

.account-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.action-btn.primary {
  background: #1e3a5f;
  color: white;
}

.action-btn.primary:hover {
  background: #2d5a87;
}

.action-btn.secondary {
  background: #f0f0f0;
  color: #333;
}

.action-btn.secondary:hover {
  background: #e0e0e0;
}

.account-detail-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.account-detail-section h2 {
  margin: 0 0 20px 0;
  color: #1e3a5f;
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
  background: #f8f9fa;
  border-radius: 8px;
}

.detail-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.detail-value {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.detail-value.amount {
  font-size: 20px;
  color: #1e3a5f;
  font-weight: bold;
}

/* 弹窗样式 */
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
  padding: 24px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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
  margin-bottom: 20px;
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

.form-group select,
.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group select:focus,
.form-group input:focus {
  outline: none;
  border-color: #1e3a5f;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.btn.cancel {
  background: #f0f0f0;
  color: #333;
}

.btn.primary {
  background: #1e3a5f;
  color: white;
}
</style>
