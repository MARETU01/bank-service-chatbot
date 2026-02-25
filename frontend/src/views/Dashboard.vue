<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon balance">💰</div>
        <div class="stat-info">
          <h3>总余额</h3>
          <p class="stat-value">¥ 125,680.00</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon income">📈</div>
        <div class="stat-info">
          <h3>本月收入</h3>
          <p class="stat-value">¥ 15,200.00</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon expense">📉</div>
        <div class="stat-info">
          <h3>本月支出</h3>
          <p class="stat-value">¥ 8,450.00</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon transfer">💸</div>
        <div class="stat-info">
          <h3>待处理转账</h3>
          <p class="stat-value">3 笔</p>
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
      <div class="transaction-list">
        <div class="transaction-item" v-for="item in recentTransactions" :key="item.id">
          <div class="transaction-icon" :class="item.type">
            {{ item.type === 'income' ? '↓' : '↑' }}
          </div>
          <div class="transaction-info">
            <h4>{{ item.title }}</h4>
            <p class="transaction-date">{{ item.date }}</p>
          </div>
          <div class="transaction-amount" :class="item.type">
            {{ item.type === 'income' ? '+' : '-' }}¥{{ item.amount }}
          </div>
        </div>
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
import { useRouter } from 'vue-router'

export default {
  name: 'Dashboard',
  setup() {
    const router = useRouter()

    const navigateTo = (path) => {
      router.push(path)
    }

    // 模拟数据
    const recentTransactions = [
      { id: 1, title: '工资收入', date: '2024-01-15 09:30', amount: '15,200.00', type: 'income' },
      { id: 2, title: '支付宝转账', date: '2024-01-14 14:20', amount: '2,500.00', type: 'expense' },
      { id: 3, title: '微信支付', date: '2024-01-13 18:45', amount: '358.00', type: 'expense' },
      { id: 4, title: '理财收益', date: '2024-01-12 10:00', amount: '1,280.00', type: 'income' },
      { id: 5, title: '信用卡还款', date: '2024-01-10 16:30', amount: '5,600.00', type: 'expense' }
    ]

    const notices = [
      { id: 1, type: 'important', typeName: '重要', title: '关于系统升级维护的通知', date: '2024-01-15' },
      { id: 2, type: 'normal', typeName: '公告', title: '2024 年第一季度理财产品推荐', date: '2024-01-14' },
      { id: 3, type: 'normal', typeName: '公告', title: '防范电信诈骗温馨提示', date: '2024-01-12' },
      { id: 4, type: 'activity', typeName: '活动', title: '新用户开户送好礼', date: '2024-01-10' }
    ]

    return {
      recentTransactions,
      notices,
      navigateTo
    }
  }
}
</script>

<style scoped>
.dashboard {
  max-width: var(--container-max-width);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: var(--spacing-2xl);
  margin-bottom: var(--spacing-3xl);
}

.stat-card {
  display: flex;
  align-items: center;
  padding: var(--spacing-2xl);
  background: var(--color-white);
  border-radius: var(--radius-xl);
  box-shadow: var(--card-shadow);
  transition: box-shadow var(--transition-normal);
}

.stat-card:hover {
  box-shadow: var(--card-hover-shadow);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-right: var(--spacing-lg);
}

.stat-icon.balance {
  background: var(--gradient-primary);
}

.stat-icon.income {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-icon.expense {
  background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
}

.stat-icon.transfer {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-info h3 {
  margin: 0 0 var(--spacing-sm) 0;
  font-size: var(--font-size-md);
  color: var(--text-secondary);
  font-weight: var(--font-weight-normal);
}

.stat-value {
  margin: 0;
  font-size: var(--font-size-5xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

.quick-actions {
  background: var(--color-white);
  padding: var(--spacing-2xl);
  border-radius: var(--radius-xl);
  box-shadow: var(--card-shadow);
  margin-bottom: var(--spacing-3xl);
}

.quick-actions h2 {
  margin: 0 0 var(--spacing-2xl) 0;
  font-size: var(--font-size-2xl);
  color: var(--color-primary);
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: var(--spacing-lg);
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-2xl);
  background: var(--color-gray-50);
  border: none;
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-normal);
  color: var(--text-primary);
}

.action-btn:hover {
  background: var(--color-primary);
  color: var(--color-white);
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.action-icon {
  font-size: 32px;
  margin-bottom: var(--spacing-sm);
}

.recent-transactions {
  background: var(--color-white);
  padding: var(--spacing-2xl);
  border-radius: var(--radius-xl);
  box-shadow: var(--card-shadow);
  margin-bottom: var(--spacing-3xl);
}

.recent-transactions h2 {
  margin: 0 0 var(--spacing-2xl) 0;
  font-size: var(--font-size-2xl);
  color: var(--color-primary);
}

.transaction-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.transaction-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-lg);
  background: var(--color-gray-50);
  border-radius: var(--radius-lg);
  transition: background var(--transition-normal);
}

.transaction-item:hover {
  background: var(--color-gray-100);
}

.transaction-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: var(--font-weight-bold);
  margin-right: var(--spacing-md);
}

.transaction-icon.income {
  background: var(--color-success-light);
  color: var(--color-success);
}

.transaction-icon.expense {
  background: var(--color-danger-light);
  color: var(--color-danger);
}

.transaction-info {
  flex: 1;
}

.transaction-info h4 {
  margin: 0 0 var(--spacing-xs) 0;
  font-size: var(--font-size-lg);
  color: var(--text-primary);
}

.transaction-date {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
}

.transaction-amount {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
}

.transaction-amount.income {
  color: var(--color-success);
}

.transaction-amount.expense {
  color: var(--color-danger);
}

.notices {
  background: var(--color-white);
  padding: var(--spacing-2xl);
  border-radius: var(--radius-xl);
  box-shadow: var(--card-shadow);
}

.notices h2 {
  margin: 0 0 var(--spacing-2xl) 0;
  font-size: var(--font-size-2xl);
  color: var(--color-primary);
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.notice-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--color-gray-50);
  border-radius: var(--radius-lg);
  gap: var(--spacing-md);
}

.notice-tag {
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-md);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
}

.notice-tag.important {
  background: var(--color-danger-light);
  color: var(--color-danger);
}

.notice-tag.normal {
  background: var(--color-info-light);
  color: #0c5460;
}

.notice-tag.activity {
  background: var(--color-success-light);
  color: var(--color-success);
}

.notice-title {
  flex: 1;
  font-size: var(--font-size-md);
  color: var(--text-primary);
}

.notice-date {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
}
</style>
