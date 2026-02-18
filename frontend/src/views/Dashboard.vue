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
  max-width: 1200px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-right: 16px;
}

.stat-icon.balance {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #666;
  font-weight: normal;
}

.stat-value {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #1e3a5f;
}

.quick-actions {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

.quick-actions h2 {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #1e3a5f;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn:hover {
  background: #1e3a5f;
  color: white;
  transform: translateY(-2px);
}

.action-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.recent-transactions {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

.recent-transactions h2 {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #1e3a5f;
}

.transaction-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.transaction-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background 0.3s;
}

.transaction-item:hover {
  background: #e9ecef;
}

.transaction-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  margin-right: 12px;
}

.transaction-icon.income {
  background: #d4edda;
  color: #28a745;
}

.transaction-icon.expense {
  background: #f8d7da;
  color: #dc3545;
}

.transaction-info {
  flex: 1;
}

.transaction-info h4 {
  margin: 0 0 4px 0;
  font-size: 15px;
  color: #333;
}

.transaction-date {
  margin: 0;
  font-size: 13px;
  color: #999;
}

.transaction-amount {
  font-size: 16px;
  font-weight: bold;
}

.transaction-amount.income {
  color: #28a745;
}

.transaction-amount.expense {
  color: #dc3545;
}

.notices {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.notices h2 {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #1e3a5f;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  gap: 12px;
}

.notice-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.notice-tag.important {
  background: #f8d7da;
  color: #dc3545;
}

.notice-tag.normal {
  background: #d1ecf1;
  color: #0c5460;
}

.notice-tag.activity {
  background: #d4edda;
  color: #28a745;
}

.notice-title {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.notice-date {
  font-size: 13px;
  color: #999;
}
</style>
