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
  max-width: 1400px;
  margin: 0 auto;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 25px;
  margin-bottom: 30px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2);
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  margin-right: 20px;
  flex-shrink: 0;
}

.stat-icon.balance {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.income {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-icon.expense {
  background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
}

.stat-icon.transfer {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.stat-value {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: white;
}

/* 快捷操作 */
.quick-actions {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 30px;
}

.quick-actions h2 {
  margin: 0 0 25px 0;
  font-size: 24px;
  color: white;
  font-weight: 600;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 20px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 25px 20px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  color: white;
  font-size: 16px;
  font-weight: 500;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
}

.action-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

/* 最近交易 */
.recent-transactions {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 30px;
}

.recent-transactions h2 {
  margin: 0 0 25px 0;
  font-size: 24px;
  color: white;
  font-weight: 600;
}

.transaction-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.transaction-item {
  display: flex;
  align-items: center;
  padding: 18px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  transition: all 0.3s;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.transaction-item:hover {
  background: rgba(255, 255, 255, 0.18);
  transform: translateX(5px);
  border-color: rgba(255, 255, 255, 0.25);
}

.transaction-icon {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  margin-right: 15px;
  flex-shrink: 0;
}

.transaction-icon.income {
  background: rgba(56, 239, 125, 0.2);
  color: #38ef7d;
}

.transaction-icon.expense {
  background: rgba(244, 92, 67, 0.2);
  color: #f45c43;
}

.transaction-info {
  flex: 1;
}

.transaction-info h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: white;
  font-weight: 500;
}

.transaction-date {
  margin: 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.transaction-amount {
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
}

.transaction-amount.income {
  color: #38ef7d;
}

.transaction-amount.expense {
  color: #f45c43;
}

/* 通知公告 */
.notices {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.notices h2 {
  margin: 0 0 25px 0;
  font-size: 24px;
  color: white;
  font-weight: 600;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  gap: 15px;
  transition: all 0.3s;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.notice-item:hover {
  background: rgba(255, 255, 255, 0.18);
  transform: translateX(5px);
  border-color: rgba(255, 255, 255, 0.25);
}

.notice-tag {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.notice-tag.important {
  background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
  color: white;
}

.notice-tag.normal {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.notice-tag.activity {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.notice-title {
  flex: 1;
  font-size: 15px;
  color: white;
}

.notice-date {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .dashboard {
    padding: 0 20px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-value {
    font-size: 24px;
  }
}

@media (max-width: 576px) {
  .dashboard {
    padding: 0 15px;
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
    font-size: 20px;
  }
}
</style>
