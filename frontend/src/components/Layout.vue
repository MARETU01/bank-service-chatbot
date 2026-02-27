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

    const pageTitle = computed(() => {
      const titles = {
        '/dashboard': '仪表盘',
        '/accounts': '账户管理',
        '/transactions': '交易记录',
        '/transfers': '转账服务',
        '/chatbot': '智能客服',
        '/profile': '个人中心'
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
        router.push('/login')
      }
    }

    return {
      pageTitle,
      currentUser,
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.sidebar {
  width: var(--sidebar-width);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  transition: width var(--transition-normal);
  position: fixed;
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
  padding: 15px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  display: flex;
  justify-content: flex-end;
}

.sidebar-toggle {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-size: 20px;
  cursor: pointer;
  color: white;
  padding: 8px 12px;
  border-radius: 10px;
  transition: all 0.3s;
}

.sidebar-toggle:hover {
  background: rgba(255, 255, 255, 0.2);
}

.sidebar.collapsed .sidebar-toggle-container {
  justify-content: center;
}

.sidebar.collapsed .menu-item span:last-child {
  display: none;
}

.sidebar.collapsed .menu-item {
  justify-content: center;
  padding: 15px 0;
}

.sidebar.collapsed .menu-item .icon {
  margin-right: 0;
}

.logo-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.header-left h1 {
  font-size: 24px;
  margin: 0;
  color: white;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
}

.menu {
  padding: 15px 0;
  flex: 1;
  overflow-y: auto;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: all 0.3s;
  margin: 5px 10px;
  border-radius: 12px;
  overflow: hidden;
}

.menu-item:hover,
.menu-item.router-link-active {
  background: rgba(255, 255, 255, 0.15);
  color: white;
}

.menu-item:hover:not(.router-link-active) {
  transform: translateX(5px);
}

.menu-item .icon {
  font-size: 22px;
  margin-right: 12px;
  width: 24px;
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
  padding: 20px 40px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-left h1 {
  font-size: 24px;
  margin: 0;
  color: white;
  font-weight: 600;
}


.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.avatar {
  font-size: 24px;
}

.logout-btn {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  font-weight: 500;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.page-content {
  padding: 30px 40px;
}

@media (max-width: 768px) {
  .sidebar {
    width: var(--sidebar-collapsed-width);
  }
  
  .main-content {
    margin-left: var(--sidebar-collapsed-width);
  }
  
  .sidebar-toggle-container {
    padding: 15px 10px;
  }
  
  .sidebar.collapsed .menu-item span:last-child {
    display: none;
  }
  
  .header {
    padding: 15px 20px;
  }
  
  .header-left h1 {
    font-size: 18px;
  }
  
  .user-info span:last-child {
    display: none;
  }
}
</style>
