<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon balance">💰</div>
        <div class="stat-info">
          <h3>总余额</h3>
          <p class="stat-value">¥ {{ formatNumber(stats.totalBalance) }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon income">📈</div>
        <div class="stat-info">
          <h3>总收入</h3>
          <p class="stat-value">¥ {{ formatNumber(stats.income) }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon expense">📉</div>
        <div class="stat-info">
          <h3>总支出</h3>
          <p class="stat-value">¥ {{ formatNumber(stats.expense) }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon transfer">💸</div>
        <div class="stat-info">
          <h3>账户数量</h3>
          <p class="stat-value">{{ stats.accountCount }} 个</p>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h2>快捷服务</h2>
      <div class="actions-grid">
        <button class="action-btn" @click="navigateTo('/transfers')">
          <span class="action-icon">💸</span>
          <span>转账汇款</span>
        </button>
        <button class="action-btn" @click="navigateTo('/transactions')">
          <span class="action-icon">📝</span>
          <span>交易查询</span>
        </button>
        <button class="action-btn" @click="navigateTo('/accounts')">
          <span class="action-icon">💳</span>
          <span>账户管理</span>
        </button>
        <button class="action-btn" @click="navigateTo('/chatbot')">
          <span class="action-icon">🤖</span>
          <span>智能客服</span>
        </button>
      </div>
    </div>

    <!-- 最近交易 -->
    <div class="recent-transactions">
      <h2>最近交易</h2>
      <div class="transaction-list" v-if="recentTransactions.length > 0">
        <div class="transaction-item" v-for="item in recentTransactions" :key="item.id">
          <div class="transaction-icon" :class="item.type">
            {{ item.type === 'income' ? '↓' : '↑' }}
          </div>
          <div class="transaction-info">
            <h4>{{ item.description }}</h4>
            <p class="transaction-date">{{ formatDateTime(item.transactionTime) }}</p>
          </div>
          <div class="transaction-amount" :class="item.type">
            {{ item.type === 'income' ? '+' : '-' }}¥{{ formatNumber(item.amount) }}
          </div>
        </div>
      </div>
      <div class="empty-state" v-else>
        <p>暂无交易记录</p>
      </div>
    </div>

    <!-- 通知公告 -->
    <div class="notices">
      <h2>通知公告</h2>
      <div class="notice-list">
        <div class="notice-item" v-for="notice in notices" :key="notice.id">
          <span class="notice-tag" :class="notice.type">{{ notice.typeName }}</span>
          <span class="notice-title">{{ notice.title }}</span>
          <span class="notice-date">{{ notice.date }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { accountApi, transactionApi } from '@/api/api'

export default {
  name: 'Dashboard',
  setup() {
    const router = useRouter()
    const { proxy } = getCurrentInstance()

    const stats = ref({
      totalBalance: 0,
      income: 0,
      expense: 0,
      accountCount: 0,
      transactionCount: 0
    })

    const recentTransactions = ref([])
    const loading = ref(false)

    const notices = ref([
      { id: 1, type: 'important', typeName: '重要', title: '关于系统升级维护的通知', date: '2024-01-15' },
      { id: 2, type: 'normal', typeName: '公告', title: '2024 年第一季度理财产品推荐', date: '2024-01-14' },
      { id: 3, type: 'normal', typeName: '公告', title: '防范电信诈骗温馨提示', date: '2024-01-12' },
      { id: 4, type: 'activity', typeName: '活动', title: '新用户开户送好礼', date: '2024-01-10' }
    ])

    const formatNumber = (num) => {
      if (!num) return '0.00'
      return Number(num).toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })
    }

    const formatDateTime = (dateTime) => {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    const navigateTo = (path) => {
      router.push(path)
    }

    const loadDashboardStats = async () => {
      loading.value = true
      try {
        const response = await accountApi.getDashboardStats()
        const { code, data, message } = response.data
        if (code === 1 || code === 200) {
          stats.value = data
        } else {
          proxy.$message.error(message || '获取统计数据失败')
        }
      } catch (error) {
        console.error('Load dashboard stats error:', error)
        proxy.$message.error('获取统计数据失败')
      } finally {
        loading.value = false
      }
    }

    const loadRecentTransactions = async () => {
      try {
        // 获取第一个账户的最近交易
        const accountsResponse = await accountApi.getAccounts()
        const { code, data } = accountsResponse.data
        if (code === 1 || code === 200) {
          const accounts = data
          if (accounts && accounts.length > 0) {
            const firstAccountId = accounts[0].id
            const response = await transactionApi.getTransactions({ accountId: firstAccountId, page: 1, size: 5 })
            const { code: txCode, data: txData } = response.data
            if (txCode === 1 || txCode === 200) {
              recentTransactions.value = txData.map(tx => ({
                ...tx,
                type: tx.transactionType === 'DEPOSIT' || tx.transactionType === 'TRANSFER_IN' || tx.transactionType === 'INCOME' ? 'income' : 'expense'
              }))
            }
          }
        }
      } catch (error) {
        console.error('Load recent transactions error:', error)
      }
    }

    onMounted(() => {
      loadDashboardStats()
      loadRecentTransactions()
    })

    return {
      stats,
      recentTransactions,
      notices,
      formatNumber,
      formatDateTime,
      navigateTo
    }
  }
}
</script>

<style scoped>
.dashboard {
  max-width: var(--container-max-width-lg);
  margin: 0 auto;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: var(--spacing-2xl);
  margin-bottom: var(--spacing-3xl);
}

.stat-card {
  display: flex;
  align-items: center;
  padding: var(--spacing-2xl);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-3xl);
  border: 1px solid var(--glass-border);
  transition: all var(--transition-normal);
}

.stat-card:hover {
  transform: translateY(-5px);
  background: var(--glass-bg-hover);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2);
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: var(--radius-3xl);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-6xl);
  margin-right: var(--spacing-2xl);
  flex-shrink: 0;
}

.stat-icon.balance {
  background: var(--gradient-balance);
}

.stat-icon.income {
  background: var(--gradient-income);
}

.stat-icon.expense {
  background: var(--gradient-expense);
}

.stat-icon.transfer {
  background: var(--gradient-transfer);
}

.stat-info h3 {
  margin: 0 0 var(--spacing-sm) 0;
  font-size: var(--font-size-xl);
  color: var(--text-on-gradient);
  font-weight: var(--font-weight-medium);
}

.stat-value {
  margin: 0;
  font-size: var(--font-size-6xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-white);
}

/* 快捷操作 */
.quick-actions {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-3xl);
  padding: var(--spacing-2xl);
  border: 1px solid var(--glass-border);
  margin-bottom: var(--spacing-3xl);
}

.quick-actions h2 {
  margin: 0 0 var(--spacing-2xl) 0;
  font-size: var(--font-size-4xl);
  color: var(--color-white);
  font-weight: var(--font-weight-semibold);
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: var(--spacing-2xl);
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 25px var(--spacing-2xl);
  background: var(--glass-bg);
  border: 1px solid var(--glass-border-hover);
  border-radius: var(--radius-3xl);
  cursor: pointer;
  transition: all var(--transition-normal);
  color: var(--color-white);
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-medium);
}

.action-btn:hover {
  background: var(--glass-bg-hover);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
  border-color: var(--glass-border-active);
}

.action-icon {
  font-size: 40px;
  margin-bottom: var(--spacing-lg);
}

/* 最近交易 */
.recent-transactions {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-3xl);
  padding: var(--spacing-2xl);
  border: 1px solid var(--glass-border);
  margin-bottom: var(--spacing-3xl);
}

.recent-transactions h2 {
  margin: 0 0 var(--spacing-2xl) 0;
  font-size: var(--font-size-4xl);
  color: var(--color-white);
  font-weight: var(--font-weight-semibold);
}

.transaction-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.transaction-item {
  display: flex;
  align-items: center;
  padding: 18px var(--spacing-2xl);
  background: var(--glass-bg-light);
  border-radius: var(--radius-2xl);
  transition: all var(--transition-normal);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.transaction-item:hover {
  background: var(--glass-bg);
  transform: translateX(5px);
  border-color: var(--glass-border);
}

.transaction-icon {
  width: 45px;
  height: 45px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  margin-right: var(--spacing-lg);
  flex-shrink: 0;
}

.transaction-icon.income {
  background: var(--transaction-income-bg);
  color: var(--transaction-income);
}

.transaction-icon.expense {
  background: var(--transaction-expense-bg);
  color: var(--transaction-expense);
}

.transaction-info {
  flex: 1;
}

.transaction-info h4 {
  margin: 0 0 var(--spacing-xs) 0;
  font-size: var(--font-size-xl);
  color: var(--color-white);
  font-weight: var(--font-weight-medium);
}

.transaction-date {
  margin: 0;
  font-size: var(--font-size-xs);
  color: var(--text-on-gradient-muted);
}

.transaction-amount {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  flex-shrink: 0;
}

.transaction-amount.income {
  color: var(--transaction-income);
}

.transaction-amount.expense {
  color: var(--transaction-expense);
}

.empty-state {
  text-align: center;
  padding: var(--spacing-3xl);
  color: var(--text-on-gradient-muted);
}

/* 通知公告 */
.notices {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-3xl);
  padding: var(--spacing-2xl);
  border: 1px solid var(--glass-border);
}

.notices h2 {
  margin: 0 0 var(--spacing-2xl) 0;
  font-size: var(--font-size-4xl);
  color: var(--color-white);
  font-weight: var(--font-weight-semibold);
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.notice-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-lg) var(--spacing-2xl);
  background: var(--glass-bg-light);
  border-radius: var(--radius-xl);
  gap: var(--spacing-lg);
  transition: all var(--transition-normal);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.notice-item:hover {
  background: var(--glass-bg);
  transform: translateX(5px);
  border-color: var(--glass-border);
}

.notice-tag {
  padding: var(--spacing-xs) var(--spacing-md);
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  flex-shrink: 0;
}

.notice-tag.important {
  background: var(--gradient-important);
  color: var(--color-white);
}

.notice-tag.normal {
  background: var(--glass-border-hover);
  color: var(--color-white);
}

.notice-tag.activity {
  background: var(--gradient-activity);
  color: var(--color-white);
}

.notice-title {
  flex: 1;
  font-size: var(--font-size-lg);
  color: var(--color-white);
}

.notice-date {
  font-size: var(--font-size-xs);
  color: var(--text-on-gradient-muted);
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .dashboard {
    padding: 0 var(--spacing-2xl);
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-value {
    font-size: var(--font-size-4xl);
  }
}

@media (max-width: 576px) {
  .dashboard {
    padding: 0 var(--spacing-lg);
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .quick-actions h2,
  .recent-transactions h2,
  .notices h2 {
    font-size: var(--font-size-3xl);
  }
}
</style>
