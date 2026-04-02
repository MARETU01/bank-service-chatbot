<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-toggle-container">
        <button class="sidebar-toggle" @click="toggleSidebar">☰</button>
      </div>
      <nav class="menu">
        <router-link to="/dashboard" class="menu-item">
          <span class="icon">📊</span>
          <span>仪表盘</span>
        </router-link>
        <router-link to="/accounts" class="menu-item">
          <span class="icon">💳</span>
          <span>账户管理</span>
        </router-link>
        <router-link to="/transactions" class="menu-item">
          <span class="icon">📝</span>
          <span>交易记录</span>
        </router-link>
        <router-link to="/transfers" class="menu-item">
          <span class="icon">💸</span>
          <span>转账服务</span>
        </router-link>
        <router-link to="/chatbot" class="menu-item">
          <span class="icon">🤖</span>
          <span>智能客服</span>
        </router-link>
        <router-link to="/profile" class="menu-item">
          <span class="icon">👤</span>
          <span>个人中心</span>
        </router-link>

        <!-- 管理员菜单 -->
        <template v-if="isAdmin">
          <div class="menu-divider">
            <span class="divider-text">管理功能</span>
          </div>
          <router-link to="/admin/knowledge" class="menu-item admin-item">
            <span class="icon">📚</span>
            <span>知识库管理</span>
          </router-link>
          <router-link to="/admin/users" class="menu-item admin-item">
            <span class="icon">👥</span>
            <span>用户管理</span>
          </router-link>
        </template>
      </nav>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- 顶部导航 -->
      <header class="header">
        <div class="header-left">
          <h1><span class="logo-icon">🏦</span> 银行服务机器人</h1>
        </div>
        <div class="header-right">
          <span class="user-info">
            <span class="avatar">👤</span>
            <span>{{ currentUser.name }}</span>
          </span>
          <button class="logout-btn" @click="handleLogout">退出</button>
        </div>
      </header>

      <!-- 页面内容 -->
      <div class="page-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'

export default {
  name: 'Layout',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const store = useStore()

    // 是否是管理员
    const isAdmin = computed(() => store.getters.isAdmin)

    const pageTitle = computed(() => {
      const titles = {
        '/dashboard': '仪表盘',
        '/accounts': '账户管理',
        '/transactions': '交易记录',
        '/transfers': '转账服务',
        '/chatbot': '智能客服',
        '/profile': '个人中心',
        '/admin/knowledge': '知识库管理',
        '/admin/users': '用户管理'
      }
      return titles[route.path] || '银行服务'
    })

    // 从 Vuex 获取用户信息
    const currentUser = computed(() => {
      const user = store.state.user
      if (user) {
        return {
          name: user.username || user.email || '用户',
          id: user.userId
        }
      }
      // 如果还没有用户信息，显示默认值
      return {
        name: '用户',
        id: ''
      }
    })

    const toggleSidebar = () => {
      document.querySelector('.sidebar').classList.toggle('collapsed')
    }

    const handleLogout = async () => {
      try {
        await store.dispatch('logout')
      } catch (error) {
        console.error('Logout error:', error)
        router.push('/')
      }
    }

    return {
      pageTitle,
      currentUser,
      isAdmin,
      toggleSidebar,
      handleLogout
    }
  }
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background: var(--gradient-primary);
}

.sidebar {
  width: var(--sidebar-width);
  background: var(--glass-bg-light);
  backdrop-filter: var(--glass-blur);
  border-right: 1px solid var(--glass-border);
  color: var(--color-white);
  transition: width var(--transition-normal);
  position: fixed;
  top: 0;
  height: 100vh;
  overflow-y: auto;
  z-index: var(--z-sticky);
  display: flex;
  flex-direction: column;
}

.sidebar.collapsed {
  width: var(--sidebar-collapsed-width);
}

.sidebar-toggle-container {
  padding: var(--spacing-2xl) var(--spacing-2xl);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  display: flex;
  justify-content: flex-end;
  height: var(--header-height);
  align-items: center;
}

.sidebar-toggle {
  background: var(--glass-bg-light);
  border: 1px solid var(--glass-border);
  font-size: var(--font-size-2xl);
  cursor: pointer;
  color: var(--color-white);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-xl);
  transition: all var(--transition-normal);
}

.sidebar-toggle:hover {
  background: var(--glass-border-hover);
}

.sidebar.collapsed .sidebar-toggle-container {
  justify-content: center;
}

.sidebar.collapsed .menu-item span:last-child {
  display: none;
}

.sidebar.collapsed .menu-item {
  justify-content: center;
  padding: var(--spacing-lg) 0;
}

.sidebar.collapsed .menu-item .icon {
  margin-right: 0;
}

.logo-icon {
  font-size: var(--font-size-5xl);
  flex-shrink: 0;
}

.header-left h1 {
  font-size: var(--font-size-4xl);
  margin: 0;
  color: var(--color-white);
  font-weight: var(--font-weight-semibold);
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.menu {
  padding: var(--spacing-lg) 0;
  flex: 1;
  overflow-y: auto;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-lg) var(--spacing-2xl);
  color: var(--text-on-gradient-secondary);
  text-decoration: none;
  transition: all var(--transition-normal);
  margin: var(--spacing-xs) var(--spacing-lg);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.menu-item:hover,
.menu-item.router-link-active {
  background: var(--glass-bg);
  color: var(--color-white);
}

.menu-item:hover:not(.router-link-active) {
  transform: translateX(5px);
}

.menu-item .icon {
  font-size: var(--font-size-2xl);
  margin-right: var(--spacing-lg);
  width: var(--spacing-3xl);
  text-align: center;
  flex-shrink: 0;
}

.menu-item span:last-child {
  white-space: nowrap;
  transition: opacity var(--transition-normal);
}

.main-content {
  flex: 1;
  margin-left: var(--sidebar-width);
  transition: margin-left var(--transition-normal);
}

.sidebar.collapsed + .main-content {
  margin-left: var(--sidebar-collapsed-width);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 var(--spacing-4xl);
  background: var(--glass-bg-light);
  backdrop-filter: var(--glass-blur);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  height: var(--header-height);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-2xl);
}

.header-left h1 {
  font-size: var(--font-size-4xl);
  margin: 0;
  color: var(--color-white);
  font-weight: var(--font-weight-semibold);
}


.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-2xl);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  color: var(--text-on-gradient);
  font-weight: var(--font-weight-medium);
}

.avatar {
  font-size: var(--font-size-4xl);
}

.logout-btn {
  padding: var(--spacing-lg) var(--spacing-2xl);
  background: var(--glass-bg-light);
  color: var(--color-white);
  border: 1px solid var(--glass-border-hover);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-normal);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
}

.logout-btn:hover {
  background: var(--glass-bg-hover);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.page-content {
  padding: var(--spacing-3xl) var(--spacing-4xl);
}

/* 管理员菜单分隔线 */
.menu-divider {
  margin: var(--spacing-lg) var(--spacing-2xl);
  padding: var(--spacing-sm) 0;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
}

.divider-text {
  font-size: var(--font-size-xs);
  color: var(--text-on-gradient-muted);
  text-transform: uppercase;
  letter-spacing: 1px;
  padding: var(--spacing-xs) 0;
  white-space: nowrap;
}

.sidebar.collapsed .divider-text {
  display: none;
}

.sidebar.collapsed .menu-divider {
  margin: var(--spacing-lg) var(--spacing-md);
}

.admin-item {
  position: relative;
}

.admin-item::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  background: linear-gradient(180deg, #f59e0b, #ef4444);
  border-radius: 2px;
  opacity: 0.7;
}

.sidebar.collapsed .admin-item::before {
  display: none;
}

@media (max-width: 768px) {
  .sidebar {
    width: var(--sidebar-collapsed-width);
  }
  
  .main-content {
    margin-left: var(--sidebar-collapsed-width);
  }
  
  .sidebar-toggle-container {
    padding: var(--spacing-lg) var(--spacing-lg);
  }
  
  .sidebar.collapsed .menu-item span:last-child {
    display: none;
  }
  
  .header {
    padding: var(--spacing-lg) var(--spacing-2xl);
  }
  
  .header-left h1 {
    font-size: var(--font-size-2xl);
  }
  
  .user-info span:last-child {
    display: none;
  }
}
</style>
