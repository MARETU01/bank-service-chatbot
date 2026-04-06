<template>
  <div class="chat-stats">
    <div class="page-header">
      <h2>📈 Chat Statistics</h2>
      <p class="page-desc">View chat data, AI performance metrics, and security protection statistics</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Loading statistics...</p>
    </div>

    <!-- Error Message -->
    <div v-else-if="error" class="error-container">
      <div class="error-icon">❌</div>
      <p>{{ error }}</p>
      <button class="btn btn-primary" @click="loadStats">Reload</button>
    </div>

    <template v-else>
      <!-- Toolbar: Time Range Filter + Refresh -->
      <div class="toolbar">
        <div class="toolbar-left">
          <div class="date-filter">
            <div class="quick-btns">
              <button v-for="opt in quickOptions" :key="opt.label"
                      class="btn btn-quick" :class="{ active: activeQuick === opt.label }"
                      @click="applyQuickOption(opt)">
                {{ opt.label }}
              </button>
            </div>
            <div class="date-inputs">
              <input type="date" v-model="startDate" class="date-input" placeholder="Start Date" />
              <span class="date-sep">~</span>
              <input type="date" v-model="endDate" class="date-input" placeholder="End Date" />
              <button class="btn btn-primary btn-sm" @click="loadStats" :disabled="refreshing">Query</button>
            </div>
          </div>
          <span class="last-update" v-if="lastUpdateTime">Last Updated: {{ lastUpdateTime }}</span>
        </div>
        <div class="toolbar-right">
          <button class="btn btn-outline" @click="clearDateFilter" v-if="startDate || endDate">Clear Filter</button>
          <button class="btn btn-primary" @click="loadStats" :disabled="refreshing">
            {{ refreshing ? 'Refreshing...' : '🔄 Refresh Data' }}
          </button>
        </div>
      </div>

      <!-- ===== Basic Chat Statistics ===== -->
      <div class="section">
        <h3 class="section-title">💬 Basic Chat Statistics</h3>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon sessions">💬</div>
            <div class="stat-info">
              <h4>Total Sessions</h4>
              <p class="stat-value">{{ formatNumber(stats.totalSessions) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon messages">📨</div>
            <div class="stat-info">
              <h4>Total Messages</h4>
              <p class="stat-value">{{ formatNumber(stats.totalMessages) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon users">👤</div>
            <div class="stat-info">
              <h4>User Messages</h4>
              <p class="stat-value">{{ formatNumber(stats.userMessages) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon ai">🤖</div>
            <div class="stat-info">
              <h4>AI Responses</h4>
              <p class="stat-value">{{ formatNumber(stats.aiMessages) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon active">🧑‍💻</div>
            <div class="stat-info">
              <h4>Active Users</h4>
              <p class="stat-value">{{ formatNumber(stats.activeUsers) }}</p>
            </div>
          </div>
          <div class="stat-card highlight-today">
            <div class="stat-icon today-sessions">📅</div>
            <div class="stat-info">
              <h4>Today's New Sessions</h4>
              <p class="stat-value">{{ formatNumber(stats.todaySessions) }}</p>
            </div>
          </div>
          <div class="stat-card highlight-today">
            <div class="stat-icon today-messages">📬</div>
            <div class="stat-info">
              <h4>Today's Messages</h4>
              <p class="stat-value">{{ formatNumber(stats.todayMessages) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon avg">📊</div>
            <div class="stat-info">
              <h4>Avg Messages/Session</h4>
              <p class="stat-value">{{ stats.avgMessagesPerSession ?? '-' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- ===== AI Performance Statistics ===== -->
      <div class="section">
        <h3 class="section-title">🤖 AI Performance Statistics</h3>
        <div class="model-badge" v-if="stats.modelName">
          <span class="model-label">Current Model</span>
          <span class="model-name">{{ stats.modelName }}</span>
        </div>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon tokens">🪙</div>
            <div class="stat-info">
              <h4>Total Tokens Used</h4>
              <p class="stat-value">{{ formatNumber(stats.totalTokens) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon prompt">📥</div>
            <div class="stat-info">
              <h4>Input Tokens</h4>
              <p class="stat-value">{{ formatNumber(stats.totalPromptTokens) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon completion">📤</div>
            <div class="stat-info">
              <h4>Output Tokens</h4>
              <p class="stat-value">{{ formatNumber(stats.totalCompletionTokens) }}</p>
            </div>
          </div>
          <div class="stat-card highlight-today">
            <div class="stat-icon today-tokens">🔥</div>
            <div class="stat-info">
              <h4>Today's Token Usage</h4>
              <p class="stat-value">{{ formatNumber(stats.todayTokens) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon avg-tokens">⚡</div>
            <div class="stat-info">
              <h4>Avg Tokens/Chat</h4>
              <p class="stat-value">{{ stats.avgTokensPerChat ?? '-' }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon avg-time">⏱️</div>
            <div class="stat-info">
              <h4>Avg Response Time</h4>
              <p class="stat-value">{{ formatMs(stats.avgResponseTimeMs) }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon max-time">🐢</div>
            <div class="stat-info">
              <h4>Max Response Time</h4>
              <p class="stat-value">{{ formatMs(stats.maxResponseTimeMs) }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- ===== Security Protection Statistics ===== -->
      <div class="section">
        <h3 class="section-title">🛡️ Security Protection Statistics</h3>
        <div class="stats-grid stats-grid-security">
          <div class="stat-card security-card">
            <div class="stat-icon blocked">🚫</div>
            <div class="stat-info">
              <h4>Blocked Messages</h4>
              <p class="stat-value">{{ formatNumber(stats.blockedMessages) }}</p>
            </div>
          </div>
          <div class="stat-card security-card">
            <div class="stat-icon rate">📉</div>
            <div class="stat-info">
              <h4>Block Rate</h4>
              <p class="stat-value">{{ stats.blockRate != null ? (stats.blockRate * 100).toFixed(2) + '%' : '-' }}</p>
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

    // Time range filter
    const startDate = ref('')
    const endDate = ref('')
    const activeQuick = ref('All')

    // Quick time options
    const quickOptions = [
      { label: 'All', days: null },
      { label: 'Today', days: 0 },
      { label: 'Last 7 Days', days: 7 },
      { label: 'Last 30 Days', days: 30 },
      { label: 'Last 90 Days', days: 90 }
    ]

    /**
     * Apply quick time option
     */
    const applyQuickOption = (opt) => {
      activeQuick.value = opt.label
      if (opt.days === null) {
        startDate.value = ''
        endDate.value = ''
      } else {
        const today = new Date()
        endDate.value = formatDate(today)
        if (opt.days === 0) {
          startDate.value = formatDate(today)
        } else {
          const start = new Date(today)
          start.setDate(start.getDate() - opt.days + 1)
          startDate.value = formatDate(start)
        }
      }
      loadStats()
    }

    /**
     * Clear date filter
     */
    const clearDateFilter = () => {
      startDate.value = ''
      endDate.value = ''
      activeQuick.value = 'All'
      loadStats()
    }

    /**
     * Format date as yyyy-MM-dd
     */
    const formatDate = (date) => {
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    }

    /**
     * Load statistics
     */
    const loadStats = async () => {
      if (!loading.value) {
        refreshing.value = true
      }
      error.value = null

      try {
        const params = {}
        if (startDate.value) params.startDate = startDate.value
        if (endDate.value) params.endDate = endDate.value

        const response = await chatStatsApi.getChatStats(params)
        const { code, data, message } = response.data
        if (code === 1 || code === 200) {
          stats.value = data
          lastUpdateTime.value = new Date().toLocaleString('zh-CN')
        } else {
          error.value = message || 'Failed to get statistics'
          proxy.$message.error(error.value)
        }
      } catch (err) {
        console.error('Failed to load statistics:', err)
        error.value = 'Failed to load statistics: ' + (err.response?.data?.message || err.message || 'Network error')
        proxy.$message.error(error.value)
      } finally {
        loading.value = false
        refreshing.value = false
      }
    }

    /**
     * Format number (thousands separator)
     */
    const formatNumber = (num) => {
      if (num == null) return '-'
      return Number(num).toLocaleString('zh-CN')
    }

    /**
     * Format milliseconds
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
      startDate,
      endDate,
      activeQuick,
      quickOptions,
      loadStats,
      applyQuickOption,
      clearDateFilter,
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

/* Loading State */
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

/* Error State */
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

/* Toolbar */
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

/* Date Filter */
.date-filter {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.quick-btns {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.btn-quick {
  padding: var(--spacing-xs) var(--spacing-lg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  background: var(--glass-bg);
  color: var(--text-on-gradient);
  font-size: var(--font-size-sm);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.btn-quick:hover {
  background: var(--glass-bg-hover);
  border-color: rgba(59, 130, 246, 0.4);
}

.btn-quick.active {
  background: rgba(59, 130, 246, 0.2);
  border-color: rgba(59, 130, 246, 0.6);
  color: #93c5fd;
  font-weight: var(--font-weight-semibold);
}

.date-inputs {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.date-input {
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.05);
  color: var(--color-white);
  font-size: var(--font-size-md);
  outline: none;
  transition: border-color var(--transition-normal);
}

.date-input:focus {
  border-color: rgba(59, 130, 246, 0.6);
}

.date-input::-webkit-calendar-picker-indicator {
  filter: invert(1);
  cursor: pointer;
}

.date-sep {
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-lg);
}

.btn-sm {
  padding: var(--spacing-sm) var(--spacing-lg);
  font-size: var(--font-size-md);
}

.btn-outline {
  padding: var(--spacing-md) var(--spacing-xl);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  background: transparent;
  color: var(--text-on-gradient);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all var(--transition-normal);
  white-space: nowrap;
}

.btn-outline:hover {
  background: var(--glass-bg-hover);
  border-color: rgba(239, 68, 68, 0.4);
  color: #fca5a5;
}

/* Buttons */
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

/* Section */
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

/* Model Badge */
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

/* Stats Grid */
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

/* Basic Stats Icons */
.stat-icon.sessions { background: rgba(59, 130, 246, 0.2); }
.stat-icon.messages { background: rgba(16, 185, 129, 0.2); }
.stat-icon.users { background: rgba(139, 92, 246, 0.2); }
.stat-icon.ai { background: rgba(236, 72, 153, 0.2); }
.stat-icon.active { background: rgba(245, 158, 11, 0.2); }
.stat-icon.today-sessions { background: rgba(59, 130, 246, 0.3); }
.stat-icon.today-messages { background: rgba(59, 130, 246, 0.3); }
.stat-icon.avg { background: rgba(107, 114, 128, 0.2); }

/* AI Performance Icons */
.stat-icon.tokens { background: rgba(245, 158, 11, 0.2); }
.stat-icon.prompt { background: rgba(59, 130, 246, 0.2); }
.stat-icon.completion { background: rgba(16, 185, 129, 0.2); }
.stat-icon.today-tokens { background: rgba(239, 68, 68, 0.2); }
.stat-icon.avg-tokens { background: rgba(139, 92, 246, 0.2); }
.stat-icon.avg-time { background: rgba(16, 185, 129, 0.2); }
.stat-icon.max-time { background: rgba(245, 158, 11, 0.2); }

/* Security Icons */
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

/* Responsive */
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
