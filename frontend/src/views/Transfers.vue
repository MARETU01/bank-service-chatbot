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
                  {{ account.name }} (****{{ account.lastFour }}) - 余额：¥{{ formatNumber(account.balance) }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>收款人姓名 <span class="required">*</span></label>
              <input 
                type="text" 
                v-model="transfer.payeeName" 
                placeholder="请输入收款人姓名"
                required
              />
            </div>

            <div class="form-group">
              <label>收款人账号 <span class="required">*</span></label>
              <input 
                type="text" 
                v-model="transfer.payeeAccount" 
                placeholder="请输入收款人账号"
                required
              />
            </div>

            <div class="form-group">
              <label>确认账号 <span class="required">*</span></label>
              <input 
                type="text" 
                v-model="transfer.confirmAccount" 
                placeholder="请再次输入收款人账号"
                required
              />
              <span class="error-msg" v-if="accountMismatch">两次输入的账号不一致</span>
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
              <label>转账类型</label>
              <div class="transfer-type-options">
                <label class="type-option">
                  <input type="radio" v-model="transfer.transferType" value="normal" />
                  <div class="option-content">
                    <span class="option-name">普通转账</span>
                    <span class="option-desc">2 小时内到账</span>
                  </div>
                </label>
                <label class="type-option">
                  <input type="radio" v-model="transfer.transferType" value="fast" />
                  <div class="option-content">
                    <span class="option-name">快速转账</span>
                    <span class="option-desc">实时到账</span>
                  </div>
                </label>
                <label class="type-option">
                  <input type="radio" v-model="transfer.transferType" value="scheduled" />
                  <div class="option-content">
                    <span class="option-name">预约转账</span>
                    <span class="option-desc">指定时间到账</span>
                  </div>
                </label>
              </div>
            </div>

            <div class="form-group" v-if="transfer.transferType === 'scheduled'">
              <label>预约时间</label>
              <input type="datetime-local" v-model="transfer.scheduledTime" />
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
              <button type="submit" class="btn primary" :disabled="!canSubmit">确认转账</button>
            </div>
          </form>
        </div>
      </div>

      <!-- 常用收款人 -->
      <div class="payees-section">
        <div class="payees-card">
          <div class="card-header">
            <h3>常用收款人</h3>
            <button class="add-payee-btn" @click="showAddPayee = true">
              <span>+</span> 添加
            </button>
          </div>
          <div class="payees-list">
            <div 
              class="payee-item" 
              v-for="payee in payees" 
              :key="payee.id"
              @click="selectPayee(payee)"
            >
              <div class="payee-avatar">{{ payee.name.charAt(0) }}</div>
              <div class="payee-info">
                <div class="payee-name">{{ payee.name }}</div>
                <div class="payee-account">{{ payee.account }}</div>
              </div>
              <div class="payee-bank">{{ payee.bank }}</div>
            </div>
          </div>
        </div>

        <!-- 转账记录 -->
        <div class="history-card">
          <div class="card-header">
            <h3>最近转账记录</h3>
            <button class="view-all-btn" @click="viewAllHistory">查看全部</button>
          </div>
          <div class="history-list">
            <div class="history-item" v-for="item in recentTransfers" :key="item.id">
              <div class="history-info">
                <div class="history-name">{{ item.payeeName }}</div>
                <div class="history-time">{{ item.time }}</div>
              </div>
              <div class="history-amount" :class="item.status">
                -¥{{ formatNumber(item.amount) }}
              </div>
            </div>
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
              <span class="value">{{ transfer.payeeName }}</span>
            </div>
            <div class="info-row">
              <span class="label">收款账号</span>
              <span class="value">{{ transfer.payeeAccount }}</span>
            </div>
            <div class="info-row highlight">
              <span class="label">转账金额</span>
              <span class="value amount">¥{{ formatNumber(transfer.amount) }}</span>
            </div>
            <div class="info-row">
              <span class="label">转账类型</span>
              <span class="value">{{ transferTypeName }}</span>
            </div>
            <div class="info-row">
              <span class="label">手续费</span>
              <span class="value">{{ transferFee === 0 ? '免费' : '¥' + formatNumber(transferFee) }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn cancel" @click="showConfirm = false">返回修改</button>
          <button class="btn primary" @click="confirmTransfer">确认转账</button>
        </div>
      </div>
    </div>

    <!-- 添加收款人弹窗 -->
    <div class="modal-overlay" v-if="showAddPayee" @click="showAddPayee = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>添加收款人</h3>
          <button class="close-btn" @click="showAddPayee = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>姓名</label>
            <input type="text" v-model="newPayee.name" placeholder="请输入姓名" />
          </div>
          <div class="form-group">
            <label>账号</label>
            <input type="text" v-model="newPayee.account" placeholder="请输入账号" />
          </div>
          <div class="form-group">
            <label>银行</label>
            <input type="text" v-model="newPayee.bank" placeholder="请输入开户行" />
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn cancel" @click="showAddPayee = false">取消</button>
          <button class="btn primary" @click="addPayee">确认添加</button>
        </div>
      </div>
    </div>

    <!-- 转账成功弹窗 -->
    <div class="modal-overlay" v-if="showSuccess" @click="showSuccess = false">
      <div class="modal success-modal" @click.stop>
        <div class="success-icon">✓</div>
        <h3>转账成功</h3>
        <p>已向 {{ transfer.payeeName }} 转账 ¥{{ formatNumber(transfer.amount) }}</p>
        <div class="modal-footer">
          <button class="btn primary" @click="showSuccess = false; resetForm()">完成</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue'

export default {
  name: 'Transfers',
  setup() {
    const showConfirm = ref(false)
    const showAddPayee = ref(false)
    const showSuccess = ref(false)

    // 模拟账户数据
    const accounts = ref([
      { id: 1, name: '储蓄账户', lastFour: '7890', balance: 125680.00 },
      { id: 2, name: '支票账户', lastFour: '1234', balance: 45200.50 },
      { id: 4, name: '理财账户', lastFour: '9012', balance: 78500.00 }
    ])

    // 模拟常用收款人
    const payees = ref([
      { id: 1, name: '张三', account: '6222 0212 3456 7891', bank: '工商银行' },
      { id: 2, name: '李四', account: '6222 0212 3456 7892', bank: '建设银行' },
      { id: 3, name: '王五', account: '6222 0212 3456 7893', bank: '农业银行' },
      { id: 4, name: '赵六', account: '6222 0212 3456 7894', bank: '中国银行' }
    ])

    // 模拟最近转账记录
    const allTransfers = ref([
      { id: 1, payeeName: '张三', amount: 1000, time: '2024-01-15 10:30', status: 'success' },
      { id: 2, payeeName: '李四', amount: 2500, time: '2024-01-14 15:20', status: 'success' },
      { id: 3, payeeName: '王五', amount: 500, time: '2024-01-13 09:15', status: 'pending' },
      { id: 4, payeeName: '赵六', amount: 3000, time: '2024-01-12 16:45', status: 'success' }
    ])

    const transfer = reactive({
      fromAccount: '',
      payeeName: '',
      payeeAccount: '',
      confirmAccount: '',
      amount: '',
      transferType: 'normal',
      scheduledTime: '',
      remark: ''
    })

    const newPayee = reactive({
      name: '',
      account: '',
      bank: ''
    })

    const accountMismatch = computed(() => {
      return transfer.confirmAccount && transfer.payeeAccount !== transfer.confirmAccount
    })

    const selectedAccount = computed(() => {
      return accounts.value.find(a => a.id === transfer.fromAccount)
    })

    const selectedAccountName = computed(() => {
      return selectedAccount.value ? selectedAccount.value.name : ''
    })

    const insufficientBalance = computed(() => {
      if (!transfer.amount || !selectedAccount.value) return false
      return parseFloat(transfer.amount) > selectedAccount.value.balance
    })

    const canSubmit = computed(() => {
      return transfer.fromAccount && 
             transfer.payeeName && 
             transfer.payeeAccount && 
             transfer.confirmAccount && 
             transfer.amount &&
             !accountMismatch.value &&
             !insufficientBalance.value
    })

    const transferTypeName = computed(() => {
      const types = {
        normal: '普通转账（2 小时内到账）',
        fast: '快速转账（实时到账）',
        scheduled: '预约转账'
      }
      return types[transfer.transferType] || ''
    })

    const transferFee = computed(() => {
      const amount = parseFloat(transfer.amount) || 0
      if (transfer.transferType === 'normal') return 0
      if (transfer.transferType === 'fast') return amount > 5000 ? amount * 0.001 : 0
      return 0
    })

    const recentTransfers = computed(() => {
      return allTransfers.value.slice(0, 5)
    })

    const formatNumber = (num) => {
      return Number(num).toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })
    }

    const resetForm = () => {
      transfer.fromAccount = ''
      transfer.payeeName = ''
      transfer.payeeAccount = ''
      transfer.confirmAccount = ''
      transfer.amount = ''
      transfer.transferType = 'normal'
      transfer.scheduledTime = ''
      transfer.remark = ''
    }

    const selectPayee = (payee) => {
      transfer.payeeName = payee.name
      transfer.payeeAccount = payee.account.replace(/\s/g, '')
      transfer.confirmAccount = transfer.payeeAccount
    }

    const submitTransfer = () => {
      if (!canSubmit.value) return
      showConfirm.value = true
    }

    const confirmTransfer = () => {
      // 模拟转账操作
      allTransfers.value.unshift({
        id: Date.now(),
        payeeName: transfer.payeeName,
        amount: parseFloat(transfer.amount),
        time: new Date().toLocaleString('zh-CN', { 
          year: 'numeric', 
          month: '2-digit', 
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        }),
        status: 'success'
      })
      showConfirm.value = false
      showSuccess.value = true
    }

    const addPayee = () => {
      if (newPayee.name && newPayee.account && newPayee.bank) {
        payees.value.push({
          id: Date.now(),
          name: newPayee.name,
          account: newPayee.account,
          bank: newPayee.bank
        })
        showAddPayee.value = false
        newPayee.name = ''
        newPayee.account = ''
        newPayee.bank = ''
      }
    }

    const viewAllHistory = () => {
      alert('查看完整历史记录功能待实现')
    }

    return {
      accounts,
      payees,
      recentTransfers,
      transfer,
      newPayee,
      showConfirm,
      showAddPayee,
      showSuccess,
      accountMismatch,
      selectedAccountName,
      insufficientBalance,
      canSubmit,
      transferTypeName,
      transferFee,
      formatNumber,
      resetForm,
      selectPayee,
      submitTransfer,
      confirmTransfer,
      addPayee,
      viewAllHistory
    }
  }
}
</script>

<style scoped>
.transfers {
  max-width: 1200px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  color: #1e3a5f;
}

.transfer-container {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
}

.form-card, .payees-card, .history-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.form-card h3 {
  margin: 0 0 20px 0;
  color: #1e3a5f;
  font-size: 18px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
}

.required {
  color: #dc3545;
}

.form-group select,
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.3s;
}

.form-group select:focus,
.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #1e3a5f;
}

.error-msg {
  color: #dc3545;
  font-size: 12px;
  margin-top: 4px;
  display: block;
}

.amount-input {
  position: relative;
  display: flex;
  align-items: center;
}

.currency {
  position: absolute;
  left: 12px;
  color: #666;
  font-size: 16px;
}

.amount-input input {
  padding-left: 30px;
  font-size: 18px;
  font-weight: 500;
}

.transfer-type-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.type-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.type-option:hover {
  border-color: #1e3a5f;
  background: #f8f9fa;
}

.type-option input[type="radio"]:checked + .option-content {
  color: #1e3a5f;
}

.option-content {
  display: flex;
  flex-direction: column;
}

.option-name {
  font-weight: 500;
  color: #333;
}

.option-desc {
  font-size: 12px;
  color: #999;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}

.btn {
  padding: 12px 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn.reset {
  background: #f0f0f0;
  color: #333;
}

.btn.primary {
  background: #1e3a5f;
  color: white;
}

.btn.primary:hover:not(:disabled) {
  background: #2d5a87;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 收款人列表 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header h3 {
  margin: 0;
  color: #1e3a5f;
  font-size: 16px;
}

.add-payee-btn, .view-all-btn {
  background: none;
  border: none;
  color: #1e3a5f;
  cursor: pointer;
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.3s;
}

.add-payee-btn:hover, .view-all-btn:hover {
  background: #e8f0f8;
}

.payees-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.payee-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.payee-item:hover {
  border-color: #1e3a5f;
  background: #f8f9fa;
}

.payee-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #1e3a5f;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.payee-info {
  flex: 1;
}

.payee-name {
  font-weight: 500;
  color: #333;
}

.payee-account {
  font-size: 13px;
  color: #999;
}

.payee-bank {
  font-size: 12px;
  color: #666;
}

/* 历史记录 */
.history-card {
  margin-top: 20px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #e9ecef;
}

.history-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.history-name {
  font-weight: 500;
  color: #333;
}

.history-time {
  font-size: 12px;
  color: #999;
}

.history-amount {
  font-weight: 600;
}

.history-amount.success {
  color: #28a745;
}

.history-amount.pending {
  color: #856404;
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
  max-width: 450px;
  padding: 24px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
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

.confirm-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-row .label {
  color: #666;
  font-size: 14px;
}

.info-row .value {
  color: #333;
  font-weight: 500;
}

.info-row.highlight .value,
.info-row .value.amount {
  font-size: 24px;
  color: #1e3a5f;
  font-weight: bold;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e9ecef;
}

.btn.cancel {
  background: #f0f0f0;
  color: #333;
}

/* 成功弹窗 */
.success-modal {
  text-align: center;
}

.success-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #28a745;
  color: white;
  font-size: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.success-modal h3 {
  margin: 0 0 12px 0;
  color: #1e3a5f;
}

.success-modal p {
  color: #666;
  margin: 0 0 24px 0;
}

.success-modal .modal-footer {
  justify-content: center;
  border-top: none;
  margin-top: 0;
  padding-top: 0;
}
</style>
