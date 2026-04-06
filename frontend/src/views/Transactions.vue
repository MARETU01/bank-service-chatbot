<template>
  <div class="transactions">
    <div class="page-header">
      <h2>Transaction History</h2>
      <div class="header-actions">
        <button class="export-btn" @click="exportTransactions">
          <span>📥</span> Export Records
        </button>
      </div>
    </div>

    <!-- Filter Section -->
    <div class="filter-section">
      <div class="filter-row">
        <div class="filter-group">
          <label>Account</label>
          <select v-model="filters.accountId" @change="onAccountChange">
            <option value="all">All Accounts</option>
            <option v-for="acc in accounts" :key="acc.id" :value="acc.id">
              {{ acc.accountName || 'Account' }} ({{ acc.accountNumber }})
            </option>
          </select>
        </div>
        <div class="filter-group">
          <label>Transaction Type</label>
          <select v-model="filters.type">
            <option value="">All</option>
            <option value="DEPOSIT">Deposit</option>
            <option value="WITHDRAW">Withdrawal</option>
            <option value="TRANSFER_IN">Transfer In</option>
            <option value="TRANSFER_OUT">Transfer Out</option>
            <option value="PAYMENT">Payment</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Status</label>
          <select v-model="filters.status">
            <option value="">All</option>
            <option value="1">Success</option>
            <option value="0">Failed</option>
            <option value="2">Processing</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Start Date</label>
          <input type="date" v-model="filters.startDate" />
        </div>
        <div class="filter-group">
          <label>End Date</label>
          <input type="date" v-model="filters.endDate" />
        </div>
        <div class="filter-group">
          <label>&nbsp;</label>
          <button class="search-btn" @click="searchTransactions">Search</button>
        </div>
      </div>
    </div>

    <!-- Transaction List -->
    <div class="transactions-table" v-if="transactions.length > 0">
      <table>
        <thead>
          <tr>
            <th>Time</th>
            <th>Type</th>
            <th>Counterparty/Merchant</th>
            <th>Description</th>
            <th>Amount</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in transactions" :key="item.id">
            <td>{{ formatDateTime(item.transactionTime) }}</td>
            <td>
              <span class="type-badge" :class="getTypeClass(item.transactionType)">
                {{ getTypeText(item.transactionType) }}
              </span>
            </td>
            <td>{{ item.counterpartyName || item.counterpartyAccount || '-' }}</td>
            <td>{{ item.description || '-' }}</td>
            <td :class="getAmountClass(item.transactionType)">
              {{ isIncomeType(item.transactionType) ? '+' : '-' }}¥{{ formatNumber(item.amount) }}
            </td>
            <td>
              <span class="status-badge" :class="getStatusClass(item.status)">
                {{ getStatusText(item.status) }}
              </span>
            </td>
            <td>
              <button class="detail-btn" @click="viewDetail(item)">Details</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="empty-state" v-else>
      <p>No transaction records</p>
    </div>

    <!-- Pagination -->
    <div class="pagination" v-if="transactions.length > 0">
      <button :disabled="currentPage === 1" @click="changePage(currentPage - 1)">Previous</button>
      <span class="page-info">Page {{ currentPage }} / {{ totalPages }}</span>
      <button :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">Next</button>
    </div>

    <!-- Transaction Details Modal -->
    <div class="modal-overlay" v-if="selectedTransaction" @click="selectedTransaction = null">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>Transaction Details</h3>
          <button class="close-btn" @click="selectedTransaction = null">×</button>
        </div>
        <div class="modal-body">
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">Transaction ID</span>
              <span class="value">{{ selectedTransaction.id }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Transaction Reference</span>
              <span class="value">{{ selectedTransaction.transactionId }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Transaction Time</span>
              <span class="value">{{ formatDateTime(selectedTransaction.transactionTime) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Transaction Type</span>
              <span class="value">{{ getTypeText(selectedTransaction.transactionType) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Amount</span>
              <span class="value amount" :class="getAmountClass(selectedTransaction.transactionType)">
                {{ isIncomeType(selectedTransaction.transactionType) ? '+' : '-' }}¥{{ formatNumber(selectedTransaction.amount) }}
              </span>
            </div>
            <div class="detail-item">
              <span class="label">Balance After</span>
              <span class="value">¥ {{ formatNumber(selectedTransaction.balanceAfter) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Counterparty Account</span>
              <span class="value">{{ selectedTransaction.counterpartyAccount || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Counterparty Name</span>
              <span class="value">{{ selectedTransaction.counterpartyName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Description</span>
              <span class="value">{{ selectedTransaction.description || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Status</span>
              <span class="value">
                <span class="status-badge" :class="getStatusClass(selectedTransaction.status)">
                  {{ getStatusText(selectedTransaction.status) }}
                </span>
              </span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn cancel" @click="selectedTransaction = null">Close</button>
          <button class="btn primary" @click="printReceipt">Print Receipt</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, getCurrentInstance } from 'vue'
import { accountApi, transactionApi } from '@/api/api'

export default {
  name: 'Transactions',
  setup() {
    const { proxy } = getCurrentInstance()
    const selectedTransaction = ref(null)
    const currentPage = ref(1)
    const pageSize = 10
    const total = ref(0)
    const accounts = ref([])
    const transactions = ref([])
    const loading = ref(false)

    const filters = reactive({
      accountId: '',
      type: '',
      status: '',
      startDate: '',
      endDate: '',
      page: 1,
      size: 10
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

    const getTypeClass = (type) => {
      const typeMap = {
        'DEPOSIT': 'income',
        'WITHDRAW': 'expense',
        'TRANSFER_IN': 'transfer',
        'TRANSFER_OUT': 'transfer',
        'PAYMENT': 'payment'
      }
      return typeMap[type] || 'expense'
    }

    const getTypeText = (type) => {
      const typeMap = {
        'DEPOSIT': 'Deposit',
        'WITHDRAW': 'Withdrawal',
        'TRANSFER_IN': 'Transfer In',
        'TRANSFER_OUT': 'Transfer Out',
        'PAYMENT': 'Payment'
      }
      return typeMap[type] || type
    }

    const isIncomeType = (type) => {
      return type === 'DEPOSIT' || type === 'TRANSFER_IN'
    }

    const getAmountClass = (type) => {
      return isIncomeType(type) ? 'income' : 'expense'
    }

    const getStatusClass = (status) => {
      const statusMap = {
        0: 'failed',
        1: 'success',
        2: 'pending'
      }
      return statusMap[status] || 'pending'
    }

    const getStatusText = (status) => {
      const statusMap = {
        0: 'Failed',
        1: 'Success',
        2: 'Processing'
      }
      return statusMap[status] || 'Unknown'
    }

    const loadAccounts = async () => {
      try {
        const response = await accountApi.getAccounts()
        const { code, data } = response.data
        if (code === 1 || code === 200) {
          accounts.value = data || []
          // If there are accounts, select the first one by default
          if (data && data.length > 0) {
            filters.accountId = data[0].id
            loadTransactions()
          }
        }
      } catch (error) {
        console.error('Load accounts error:', error)
      }
    }

    const loadTransactions = async () => {
      // When accountId is empty, do not load data
      if (!filters.accountId) {
        transactions.value = []
        return
      }

      loading.value = true
      try {
        const params = {
          page: currentPage.value,
          size: pageSize,
          allAccounts: filters.accountId === 'all'
        }
        if (filters.type) params.type = filters.type
        if (filters.status !== '') params.status = filters.status
        if (filters.startDate) params.startDate = filters.startDate
        if (filters.endDate) params.endDate = filters.endDate

        // When querying all accounts, do not pass accountId
        if (filters.accountId !== 'all') {
          params.accountId = filters.accountId
        }
        
        const response = await transactionApi.getTransactions(params)
        const { code, data } = response.data
        if (code === 1 || code === 200) {
          transactions.value = data || []
          total.value = data ? data.length : 0
        }
      } catch (error) {
        console.error('Load transactions error:', error)
        proxy.$message.error('Failed to get transaction records')
      } finally {
        loading.value = false
      }
    }

    const onAccountChange = () => {
      currentPage.value = 1
      loadTransactions()
    }

    const searchTransactions = () => {
      currentPage.value = 1
      loadTransactions()
    }

    const changePage = (page) => {
      if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page
        loadTransactions()
      }
    }

    const viewDetail = (item) => {
      selectedTransaction.value = item
    }

    const exportTransactions = () => {
      proxy.$message.info('Export feature not yet implemented')
    }

    const printReceipt = () => {
      proxy.$message.info('Print receipt feature not yet implemented')
    }

    const totalPages = computed(() => {
      return Math.ceil(total.value / pageSize)
    })

    onMounted(() => {
      loadAccounts()
    })

    return {
      filters,
      accounts,
      transactions,
      currentPage,
      totalPages,
      selectedTransaction,
      formatNumber,
      formatDateTime,
      formatAccountLastFour,
      getTypeClass,
      getTypeText,
      isIncomeType,
      getAmountClass,
      getStatusClass,
      getStatusText,
      onAccountChange,
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

td.expense {
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

@media (max-width: 768px) {
  .filter-row {
    flex-direction: column;
  }

  .filter-group {
    width: 100%;
  }

  table {
    font-size: var(--font-size-sm);
  }

  th, td {
    padding: var(--spacing-md);
  }
}
</style>
