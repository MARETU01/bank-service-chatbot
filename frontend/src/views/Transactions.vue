<template>
  <div class="transactions">
    <div class="page-header">
      <h2>交易记录</h2>
      <div class="header-actions">
        <button class="export-btn" @click="exportTransactions">
          <span>📥</span> 导出记录
        </button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-group">
          <label>交易类型</label>
          <select v-model="filters.type">
            <option value="">全部</option>
            <option value="income">收入</option>
            <option value="expense">支出</option>
            <option value="transfer">转账</option>
            <option value="payment">支付</option>
          </select>
        </div>
        <div class="filter-group">
          <label>账户</label>
          <select v-model="filters.account">
            <option value="">全部账户</option>
            <option value="1">储蓄账户 (7890)</option>
            <option value="2">支票账户 (1234)</option>
            <option value="3">定期存款 (5678)</option>
            <option value="4">理财账户 (9012)</option>
          </select>
        </div>
        <div class="filter-group">
          <label>开始日期</label>
          <input type="date" v-model="filters.startDate" />
        </div>
        <div class="filter-group">
          <label>结束日期</label>
          <input type="date" v-model="filters.endDate" />
        </div>
        <div class="filter-group">
          <label>&nbsp;</label>
          <button class="search-btn" @click="searchTransactions">搜索</button>
        </div>
      </div>
    </div>

    <!-- 交易列表 -->
    <div class="transactions-table">
      <table>
        <thead>
          <tr>
            <th>交易时间</th>
            <th>交易类型</th>
            <th>对方账户/商户</th>
            <th>摘要</th>
            <th>金额</th>
            <th>账户</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filteredTransactions" :key="item.id" :class="item.type">
            <td>{{ item.time }}</td>
            <td>
              <span class="type-badge" :class="item.type">
                {{ item.typeName }}
              </span>
            </td>
            <td>{{ item.counterparty }}</td>
            <td>{{ item.description }}</td>
            <td :class="item.type">
              {{ item.type === 'income' ? '+' : '-' }}¥{{ formatNumber(item.amount) }}
            </td>
            <td>{{ item.accountName }}</td>
            <td>
              <span class="status-badge" :class="item.status">
                {{ item.statusText }}
              </span>
            </td>
            <td>
              <button class="detail-btn" @click="viewDetail(item)">详情</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <button :disabled="currentPage === 1" @click="changePage(currentPage - 1)">上一页</button>
      <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
      <button :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">下一页</button>
    </div>

    <!-- 交易详情弹窗 -->
    <div class="modal-overlay" v-if="selectedTransaction" @click="selectedTransaction = null">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>交易详情</h3>
          <button class="close-btn" @click="selectedTransaction = null">×</button>
        </div>
        <div class="modal-body">
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">交易流水号</span>
              <span class="value">{{ selectedTransaction.transactionId }}</span>
            </div>
            <div class="detail-item">
              <span class="label">交易时间</span>
              <span class="value">{{ selectedTransaction.time }}</span>
            </div>
            <div class="detail-item">
              <span class="label">交易类型</span>
              <span class="value">{{ selectedTransaction.typeName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">交易金额</span>
              <span class="value amount" :class="selectedTransaction.type">
                {{ selectedTransaction.type === 'income' ? '+' : '-' }}¥{{ formatNumber(selectedTransaction.amount) }}
              </span>
            </div>
            <div class="detail-item">
              <span class="label">对方账户/商户</span>
              <span class="value">{{ selectedTransaction.counterparty }}</span>
            </div>
            <div class="detail-item">
              <span class="label">对方账号</span>
              <span class="value">{{ selectedTransaction.counterpartyAccount }}</span>
            </div>
            <div class="detail-item">
              <span class="label">摘要</span>
              <span class="value">{{ selectedTransaction.description }}</span>
            </div>
            <div class="detail-item">
              <span class="label">交易账户</span>
              <span class="value">{{ selectedTransaction.accountName }} (****{{ selectedTransaction.accountLastFour }})</span>
            </div>
            <div class="detail-item">
              <span class="label">交易状态</span>
              <span class="value">
                <span class="status-badge" :class="selectedTransaction.status">
                  {{ selectedTransaction.statusText }}
                </span>
              </span>
            </div>
            <div class="detail-item full-width">
              <span class="label">备注</span>
              <span class="value">{{ selectedTransaction.remark || '无' }}</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn cancel" @click="selectedTransaction = null">关闭</button>
          <button class="btn primary" @click="printReceipt">打印回单</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, getCurrentInstance } from 'vue'

export default {
  name: 'Transactions',
  setup() {
    const { proxy } = getCurrentInstance()
    const selectedTransaction = ref(null)
    const currentPage = ref(1)
    const pageSize = 10

    const filters = reactive({
      type: '',
      account: '',
      startDate: '',
      endDate: ''
    })

    // 模拟交易数据
    const allTransactions = ref([
      {
        id: 1,
        transactionId: 'TRX202401150930001',
        time: '2024-01-15 09:30:25',
        type: 'income',
        typeName: '工资收入',
        counterparty: 'XX 科技有限公司',
        counterpartyAccount: '对公账户',
        description: '2024 年 1 月工资',
        amount: 15200.00,
        accountName: '储蓄账户',
        accountLastFour: '7890',
        status: 'success',
        statusText: '交易成功',
        remark: ''
      },
      {
        id: 2,
        transactionId: 'TRX202401141420002',
        time: '2024-01-14 14:20:15',
        type: 'transfer',
        typeName: '转账支出',
        counterparty: '张三',
        counterpartyAccount: '6222 **** **** 5678',
        description: '借款归还',
        amount: 2500.00,
        accountName: '储蓄账户',
        accountLastFour: '7890',
        status: 'success',
        statusText: '交易成功',
        remark: '新年快乐'
      },
      {
        id: 3,
        transactionId: 'TRX202401131845003',
        time: '2024-01-13 18:45:30',
        type: 'payment',
        typeName: '微信支付',
        counterparty: 'XX 餐厅',
        counterpartyAccount: '商户',
        description: '餐饮消费',
        amount: 358.00,
        accountName: '储蓄账户',
        accountLastFour: '7890',
        status: 'success',
        statusText: '交易成功',
        remark: ''
      },
      {
        id: 4,
        transactionId: 'TRX202401121000004',
        time: '2024-01-12 10:00:00',
        type: 'income',
        typeName: '理财收益',
        counterparty: 'XX 理财',
        counterpartyAccount: '理财产品',
        description: '理财到期收益',
        amount: 1280.00,
        accountName: '理财账户',
        accountLastFour: '9012',
        status: 'success',
        statusText: '交易成功',
        remark: ''
      },
      {
        id: 5,
        transactionId: 'TRX202401101630005',
        time: '2024-01-10 16:30:45',
        type: 'payment',
        typeName: '信用卡还款',
        counterparty: 'XX 银行',
        counterpartyAccount: '信用卡',
        description: '信用卡自动还款',
        amount: 5600.00,
        accountName: '储蓄账户',
        accountLastFour: '7890',
        status: 'success',
        statusText: '交易成功',
        remark: ''
      },
      {
        id: 6,
        transactionId: 'TRX202401091200006',
        time: '2024-01-09 12:15:20',
        type: 'expense',
        typeName: 'ATM 取款',
        counterparty: 'XX 银行 ATM',
        counterpartyAccount: 'ATM001234',
        description: 'ATM 现金取款',
        amount: 2000.00,
        accountName: '储蓄账户',
        accountLastFour: '7890',
        status: 'success',
        statusText: '交易成功',
        remark: ''
      },
      {
        id: 7,
        transactionId: 'TRX202401080930007',
        time: '2024-01-08 09:30:00',
        type: 'income',
        typeName: '转账收入',
        counterparty: '李四',
        counterpartyAccount: '6222 **** **** 9876',
        description: '货款',
        amount: 8000.00,
        accountName: '支票账户',
        accountLastFour: '1234',
        status: 'success',
        statusText: '交易成功',
        remark: '12 月货款'
      },
      {
        id: 8,
        transactionId: 'TRX202401071545008',
        time: '2024-01-07 15:45:10',
        type: 'payment',
        typeName: '支付宝',
        counterparty: 'XX 电商平台',
        counterpartyAccount: '商户',
        description: '网上购物',
        amount: 1599.00,
        accountName: '储蓄账户',
        accountLastFour: '7890',
        status: 'success',
        statusText: '交易成功',
        remark: ''
      },
      {
        id: 9,
        transactionId: 'TRX202401061000009',
        time: '2024-01-06 10:00:00',
        type: 'transfer',
        typeName: '定期存入',
        counterparty: '本人定期账户',
        counterpartyAccount: '6222 **** **** 5678',
        description: '定期存款',
        amount: 50000.00,
        accountName: '储蓄账户',
        accountLastFour: '7890',
        status: 'pending',
        statusText: '处理中',
        remark: '1 年期定期'
      },
      {
        id: 10,
        transactionId: 'TRX202401051820010',
        time: '2024-01-05 18:20:30',
        type: 'payment',
        typeName: '银联支付',
        counterparty: 'XX 超市',
        counterpartyAccount: '商户',
        description: '日常购物',
        amount: 456.80,
        accountName: '储蓄账户',
        accountLastFour: '7890',
        status: 'success',
        statusText: '交易成功',
        remark: ''
      },
      {
        id: 11,
        transactionId: 'TRX202401041130011',
        time: '2024-01-04 11:30:00',
        type: 'expense',
        typeName: '手续费',
        counterparty: 'XX 银行',
        counterpartyAccount: '银行',
        description: '跨行转账手续费',
        amount: 5.00,
        accountName: '储蓄账户',
        accountLastFour: '7890',
        status: 'success',
        statusText: '交易成功',
        remark: ''
      },
      {
        id: 12,
        transactionId: 'TRX202401030900012',
        time: '2024-01-03 09:00:00',
        type: 'income',
        typeName: '利息收入',
        counterparty: 'XX 银行',
        counterpartyAccount: '银行',
        description: '季度利息',
        amount: 320.50,
        accountName: '储蓄账户',
        accountLastFour: '7890',
        status: 'success',
        statusText: '交易成功',
        remark: ''
      }
    ])

    const filteredTransactions = computed(() => {
      let result = allTransactions.value

      if (filters.type) {
        result = result.filter(item => item.type === filters.type)
      }

      if (filters.account) {
        result = result.filter(item => item.accountLastFour === filters.account)
      }

      if (filters.startDate) {
        result = result.filter(item => item.time >= filters.startDate)
      }

      if (filters.endDate) {
        result = result.filter(item => item.time <= filters.endDate + ' 23:59:59')
      }

      return result
    })

    const totalPages = computed(() => {
      return Math.ceil(filteredTransactions.value.length / pageSize)
    })

    const formatNumber = (num) => {
      return Number(num).toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })
    }

    const searchTransactions = () => {
      currentPage.value = 1
    }

    const changePage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
      }
    }

    const viewDetail = (item) => {
      selectedTransaction.value = item
    }

    const exportTransactions = () => {
      proxy.$message.info('导出功能待实现')
    }

    const printReceipt = () => {
      proxy.$message.info('打印回单功能待实现')
    }

    return {
      filters,
      filteredTransactions,
      currentPage,
      totalPages,
      selectedTransaction,
      formatNumber,
      searchTransactions,
      changePage,
      viewDetail,
      exportTransactions,
      printReceipt
    }
  }
}
</script>

<style scoped>
.transactions {
  max-width: var(--container-max-width-lg);
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-3xl);
}

.page-header h2 {
  margin: 0;
  color: var(--color-white);
  font-size: var(--font-size-5xl);
  font-weight: var(--font-weight-semibold);
}

.header-actions {
  display: flex;
  gap: var(--spacing-lg);
}

.export-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg) var(--spacing-2xl);
  background: var(--glass-bg);
  color: var(--color-white);
  border: 1px solid var(--glass-border-hover);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-normal);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
}

.export-btn:hover {
  background: var(--glass-bg-hover);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.filter-section {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  padding: var(--spacing-2xl);
  border-radius: var(--radius-3xl);
  border: 1px solid var(--glass-border);
  margin-bottom: var(--spacing-2xl);
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-lg);
}

.filter-group {
  display: flex;
  flex-direction: column;
  min-width: 150px;
}

.filter-group label {
  font-size: var(--font-size-xs);
  color: var(--text-on-gradient-secondary);
  margin-bottom: var(--spacing-xs);
  font-weight: var(--font-weight-medium);
}

.filter-group select,
.filter-group input {
  padding: var(--spacing-lg) var(--spacing-lg);
  border: 1px solid var(--input-border);
  border-radius: var(--radius-xl);
  font-size: var(--font-size-md);
  background: var(--input-bg);
  color: var(--color-white);
}

.filter-group select option {
  background: #667eea;
  color: var(--color-white);
}

.filter-group select:focus,
.filter-group input:focus {
  outline: none;
  border-color: var(--input-border-focus);
  background: var(--input-bg-focus);
}

.filter-group input[type="date"]::-webkit-calendar-picker-indicator {
  filter: invert(1);
  cursor: pointer;
}

.search-btn {
  padding: var(--spacing-lg) var(--spacing-2xl);
  background: var(--btn-glass-bg);
  color: var(--color-white);
  border: 1px solid var(--btn-glass-border);
  border-radius: var(--radius-xl);
  cursor: pointer;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  align-self: flex-end;
  transition: all var(--transition-normal);
}

.search-btn:hover {
  background: var(--btn-glass-hover);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.transactions-table {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-3xl);
  border: 1px solid var(--glass-border);
  overflow: hidden;
  margin-bottom: var(--spacing-2xl);
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: var(--glass-bg-light);
}

th {
  padding: 18px var(--spacing-lg);
  text-align: left;
  font-size: var(--font-size-md);
  color: var(--text-on-gradient-secondary);
  font-weight: var(--font-weight-semibold);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

td {
  padding: var(--spacing-lg);
  font-size: var(--font-size-md);
  color: var(--color-white);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

tbody tr:hover {
  background: var(--glass-bg-light);
}

.type-badge {
  display: inline-block;
  padding: var(--spacing-xs) var(--spacing-md);
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
}

.type-badge.income {
  background: var(--transaction-income-bg);
  color: var(--transaction-income);
}

.type-badge.expense {
  background: var(--transaction-expense-bg);
  color: var(--transaction-expense);
}

.type-badge.transfer {
  background: rgba(102, 126, 234, 0.2);
  color: #a8b5ff;
}

.type-badge.payment {
  background: var(--status-warning-bg);
  color: var(--status-warning);
}

td.income {
  color: var(--transaction-income);
  font-weight: var(--font-weight-bold);
}

td.expense,
td.transfer,
td.payment {
  color: var(--transaction-expense);
  font-weight: var(--font-weight-bold);
}

.status-badge {
  display: inline-block;
  padding: var(--spacing-xs) var(--spacing-md);
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
}

.status-badge.success {
  background: var(--status-success-bg);
  color: var(--status-success);
}

.status-badge.pending {
  background: var(--status-warning-bg);
  color: var(--status-warning);
}

.status-badge.failed {
  background: var(--status-danger-bg);
  color: var(--status-danger);
}

.detail-btn {
  padding: var(--spacing-xs) var(--spacing-lg);
  background: var(--btn-glass-bg);
  color: var(--color-white);
  border: 1px solid var(--btn-glass-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-normal);
}

.detail-btn:hover {
  background: var(--btn-glass-hover);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-2xl);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-3xl);
  border: 1px solid var(--glass-border);
}

.pagination button {
  padding: var(--spacing-lg) var(--spacing-2xl);
  background: var(--btn-glass-bg);
  color: var(--color-white);
  border: 1px solid var(--btn-glass-border);
  border-radius: var(--radius-xl);
  cursor: pointer;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-normal);
}

.pagination button:hover:not(:disabled) {
  background: var(--btn-glass-hover);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.pagination button:disabled {
  background: rgba(255, 255, 255, 0.05);
  cursor: not-allowed;
  opacity: 0.5;
}

.page-info {
  font-size: var(--font-size-md);
  color: var(--text-on-gradient-secondary);
  font-weight: var(--font-weight-medium);
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
  max-width: 600px;
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

.detail-item.full-width {
  grid-column: 1 / -1;
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
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-bold);
}

.detail-item .value.amount.income {
  color: var(--transaction-income);
}

.detail-item .value.amount:not(.income) {
  color: var(--transaction-expense);
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
</style>
