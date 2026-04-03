
<template>
  <div class="chat-stats">
    <div class="page-header">
      <h2>📈 对话统计概览</h2>
      <p class="page-desc">查看智能客服的对话数据、AI 性能指标和安全防护统计</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>正在加载统计数据...</p>
    </div>

    <!-- 错误提示 -->
    <div v-else-if="error" class="error-container">
      <div class="error-icon">❌</div>
      <p>{{ error }}</p>
      <button class="btn btn-primary" @click="loadStats">重新加载</button>
    </div>

    <template v-else>
      <!-- 刷新按钮 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <span class="last-update" v-if="lastUpdateTime">最后更新：{{ lastUpdateTime }}</span>
        </div>
        <div class="toolbar-right">
          <button class="btn btn-primary" @click="loadStats" :disabled="refreshing">
            {{ refreshing ? '刷新中...' : '🔄 刷新数据' }}
          </button>
        </div>
      </div>

      <!-- ===== 基础对话统计 ===== -->
      <div class="section">
        <h3 class="section-title">💬 基础对话统计</h3>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon sessions">💬</div>
            <div class="stat-info">
              <h4>总会话数</h4>
              <p class="stat-value">{{ formatNumber(stats.totalSessions) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon messages">📨</div>
            <div class="stat-info">
              <h4>总消息数</h4>
              <p class="stat-value">{{ formatNumber(stats.totalMessages) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon users">👤</div>
            <div class="stat-info">
              <h4>用户消息</h4>
              <p class="stat-value">{{ formatNumber(stats.userMessages) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon ai">🤖</div>
            <div class="stat-info">
              <h4>AI 回复</h4>
              <p class="stat-value">{{ formatNumber(stats.aiMessages) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon active">🧑‍💻</div>
            <div class="stat-info">
              <h4>活跃用户数</h4>
              <p class="stat-value">{{ formatNumber(stats.activeUsers) }}</p>
            </div>
          </div>
          <div class="stat-card highlight-today">
            <div class="stat-icon today-sessions">📅</div>
            <div class="stat-info">
              <h4>今日新增会话</h4>
              <p class="stat-value">{{ formatNumber(stats.todaySessions) }}</p>
            </div>
          </div>
          <div class="stat-card highlight-today">
            <div class="stat-icon today-messages">📬</div>
            <div class="stat-info">
              <h4>今日消息数</h4>
              <p class="stat-value">{{ formatNumber(stats.todayMessages) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon avg">📊</div>
            <div class="stat-info">
              <h4>平均每会话消息</h4>
              <p class="stat-value">{{ stats.avgMessagesPerSession ?? '-' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- ===== AI 性能统计 ===== -->
      <div class="section">
        <h3 class="section-title">🤖 AI 性能统计</h3>
        <div class="model-badge" v-if="stats.modelName">
          <span class="model-label">当前模型</span>
          <span class="model-name">{{ stats.modelName }}</span>
        </div>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon tokens">🪙</div>
            <div class="stat-info">
              <h4>总 Token 消耗</h4>
              <p class="stat-value">{{ formatNumber(stats.totalTokens) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon prompt">📥</div>
            <div class="stat-info">
              <h4>输入 Token</h4>
              <p class="stat-value">{{ formatNumber(stats.totalPromptTokens) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon completion">📤</div>
            <div class="stat-info">
              <h4>输出 Token</h4>
              <p class="stat-value">{{ formatNumber(stats.totalCompletionTokens) }}</p>
            </div>
          </div>
          <div class="stat-card highlight-today">
            <div class="stat-icon today-tokens">🔥</div>
            <div class="stat-info">
              <h4>今日 Token 消耗</h4>
              <p class="stat-value">{{ formatNumber(stats.todayTokens) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon avg-tokens">⚡</div>
            <div class="stat-info">
              <h4>平均每次 Token</h4>
              <p class="stat-value">{{ stats.avgTokensPerChat ?? '-' }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon avg-time">⏱️</div>
            <div class="stat-info">
              <h4>平均响应时间</h4>
              <p class="stat-value">{{ formatMs(stats.avgResponseTimeMs) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon max-time">🐢</div>
            <div class="stat-info">
              <h4>最大响应时间</h4>
              <p class="stat-value">{{ formatMs(stats.maxResponseTimeMs) }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- ===== 安全防护统计 ===== -->
      <div class="section">
        <h3 class="section-title">🛡️ 安全防护统计</h3>
        <div class="stats-grid stats-grid-security">
          <div class="stat-card security-card">
            <div class="stat-icon blocked">🚫</div>
            <div class="stat-info">
              <h4>被拦截消息数</h4>
              <p class="stat-value">{{ formatNumber(stats.blockedMessages) }}</p>
            </div>
          </div>
          <div class="stat-card security-card">
            <div class="stat-icon rate">📉</div>
            <div class="stat-info">
              <h4>拦截率</h4>
              <p class="stat-value">{{ stats.blockRate != null ? stats.blockRate + '%' : '-' }}</p>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
import { ref, onMounted, getCurrentInstance } from 'vue'
import { chatStatsApi } from '@/api/api'

export default {
  name: 'ChatStats',
  setup() {
    const { proxy } = getCurrentInstance()

    const stats = ref({})
    const loading = ref(true)
    const refreshing = ref(false)
    const error = ref(null)
    const lastUpdateTime = ref('')

    /**
     * 加载统计数据
     */
    const loadStats = async () => {
      if (!loading.value) {
        refreshing.value = true
      }
      error.value = null

      try {
        const response = await chatStatsApi.getChatStats()
        const { code, data, message } = response.data
        if (code === 1 || code === 200) {
          stats.value = data
          lastUpdateTime.value = new Date().toLocaleString('zh-CN')
        } else {
          error.value = message || '获取统计数据失败'
          proxy.$message.error(error.value)
        }
      } catch (err) {
        console.error('加载统计数据失败:', err)
        error.value = '加载统计数据失败：' + (err.response?.data?.message || err.message || '网络错误')
        proxy.$message.error(error.value)
      } finally {
        loading.value = false
        refreshing.value = false
      }
    }

    /**
     * 格式化数字（千分位）
     */
    const formatNumber = (num) => {
      if (num == null) return '-'
      return Number(num).toLocaleString('zh-CN')
    }

    /**
     * 格式化毫秒时间
     */
    const formatMs = (ms) => {
      if (ms == null) return '-'
      if (ms >= 1000) {
        return (ms / 1000).toFixed(2) + ' s'
      }
      return ms + ' ms'
    }

    onMounted(() => {
      loadStats()
    })

    return {
      stats,
      loading,
      refreshing,
      error,
      lastUpdateTime,
      loadStats,
      formatNumber,
      formatMs
    }
  }
}
</script>

<style scoped>
.chat-stats {
  max-width: var(--container-max-width-lg);
  margin: 0 auto;
}

.page-header {
  margin-bottom: var(--spacing-2xl);
}

.page-header h2 {
  font-size: var(--font-size-5xl);
  color: var(--color-white);
  margin: 0 0 var(--spacing-sm) 0;
}

.page-desc {
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-lg);
  margin: 0;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-4xl);
  color: var(--text-on-gradient);
}

.loading-container p {
  margin-top: var(--spacing-lg);
  font-size: var(--font-size-xl);
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 255, 255, 0.2);
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 错误状态 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-4xl);
  color: var(--text-on-gradient);
}

.error-icon {
  font-size: 48px;
  margin-bottom: var(--spacing-lg);
}

.error-container p {
  font-size: var(--font-size-xl);
  margin-bottom: var(--spacing-2xl);
  color: #f87171;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-2xl);
  flex-wrap: wrap;
  gap: var(--spacing-lg);
}

.last-update {
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-md);
}

/* 按钮 */
.btn {
  padding: var(--spacing-md) var(--spacing-xl);
  border: none;
  border-radius: var(--radius-xl);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all var(--transition-normal);
  white-space: nowrap;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
}

.btn-primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: var(--color-white);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(59, 130, 246, 0.4);
}

/* 区块 */
.section {
  margin-bottom: var(--spacing-3xl);
}

.section-title {
  font-size: var(--font-size-3xl);
  color: var(--color-white);
  margin: 0 0 var(--spacing-xl) 0;
  font-weight: var(--font-weight-semibold);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--glass-border);
}

/* 模型标识 */
.model-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm) var(--spacing-xl);
  background: rgba(139, 92, 246, 0.15);
  border: 1px solid rgba(139, 92, 246, 0.3);
  border-radius: var(--radius-full);
  margin-bottom: var(--spacing-xl);
}

.model-label {
  font-size: var(--font-size-sm);
  color: var(--text-on-gradient-muted);
}

.model-name {
  font-size: var(--font-size-lg);
  color: #c4b5fd;
  font-weight: var(--font-weight-semibold);
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: var(--spacing-xl);
}

.stats-grid-security {
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
}

.stat-card {
  display: flex;
  align-items: center;
  padding: var(--spacing-xl) var(--spacing-2xl);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-3xl);
  border: 1px solid var(--glass-border);
  transition: all var(--transition-normal);
}

.stat-card:hover {
  transform: translateY(-4px);
  background: var(--glass-bg-hover);
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.2);
}

.stat-card.highlight-today {
  border-color: rgba(59, 130, 246, 0.3);
  background: rgba(59, 130, 246, 0.05);
}

.stat-card.highlight-today:hover {
  border-color: rgba(59, 130, 246, 0.5);
  background: rgba(59, 130, 246, 0.1);
}

.stat-card.security-card {
  border-color: rgba(245, 158, 11, 0.3);
  background: rgba(245, 158, 11, 0.05);
}

.stat-card.security-card:hover {
  border-color: rgba(245, 158, 11, 0.5);
  background: rgba(245, 158, 11, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-2xl);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-4xl);
  margin-right: var(--spacing-xl);
  flex-shrink: 0;
}

/* 基础统计图标背景 */
.stat-icon.sessions { background: rgba(59, 130, 246, 0.2); }
.stat-icon.messages { background: rgba(16, 185, 129, 0.2); }
.stat-icon.users { background: rgba(139, 92, 246, 0.2); }
.stat-icon.ai { background: rgba(236, 72, 153, 0.2); }
.stat-icon.active { background: rgba(245, 158, 11, 0.2); }
.stat-icon.today-sessions { background: rgba(59, 130, 246, 0.3); }
.stat-icon.today-messages { background: rgba(59, 130, 246, 0.3); }
.stat-icon.avg { background: rgba(107, 114, 128, 0.2); }

/* AI 性能图标背景 */
.stat-icon.tokens { background: rgba(245, 158, 11, 0.2); }
.stat-icon.prompt { background: rgba(59, 130, 246, 0.2); }
.stat-icon.completion { background: rgba(16, 185, 129, 0.2); }
.stat-icon.today-tokens { background: rgba(239, 68, 68, 0.2); }
.stat-icon.avg-tokens { background: rgba(139, 92, 246, 0.2); }
.stat-icon.avg-time { background: rgba(16, 185, 129, 0.2); }
.stat-icon.max-time { background: rgba(245, 158, 11, 0.2); }

/* 安全防护图标背景 */
.stat-icon.blocked { background: rgba(239, 68, 68, 0.2); }
.stat-icon.rate { background: rgba(245, 158, 11, 0.2); }

.stat-info h4 {
  margin: 0 0 var(--spacing-xs) 0;
  font-size: var(--font-size-md);
  color: var(--text-on-gradient);
  font-weight: var(--font-weight-medium);
}

.stat-value {
  margin: 0;
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-white);
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  }

  .stat-card {
    padding: var(--spacing-lg) var(--spacing-xl);
  }

  .stat-icon {
    width: 44px;
    height: 44px;
    font-size: var(--font-size-2xl);
    margin-right: var(--spacing-lg);
  }

  .stat-value {
    font-size: var(--font-size-2xl);
  }

  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-right {
    display: flex;
    justify-content: center;
  }
}
</style>
