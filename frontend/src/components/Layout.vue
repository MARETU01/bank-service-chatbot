<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="logo">
        <h2>🏦 银行服务机器人</h2>
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
          <button class="menu-toggle" @click="toggleSidebar">☰</button>
          <h1>{{ pageTitle }}</h1>
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

export default {
  name: 'Layout',
  setup() {
    const route = useRoute()
    const router = useRouter()

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

    const currentUser = {
      name: '张三',
      id: '10001'
    }

    const toggleSidebar = () => {
      document.querySelector('.sidebar').classList.toggle('collapsed')
    }

    const handleLogout = () => {
      alert('退出登录')
      router.push('/login')
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
  background-color: #f5f7fa;
}

.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #1e3a5f 0%, #2d5a87 100%);
  color: white;
  transition: width 0.3s;
  position: fixed;
  height: 100vh;
  overflow-y: auto;
}

.sidebar.collapsed {
  width: 60px;
}

.sidebar.collapsed .logo h2,
.sidebar.collapsed .menu-item span:last-child {
  display: none;
}

.logo {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo h2 {
  font-size: 18px;
  margin: 0;
  white-space: nowrap;
}

.menu {
  padding: 10px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: all 0.3s;
}

.menu-item:hover,
.menu-item.router-link-active {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.menu-item .icon {
  font-size: 20px;
  margin-right: 12px;
  width: 24px;
  text-align: center;
}

.main-content {
  flex: 1;
  margin-left: 240px;
  transition: margin-left 0.3s;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 30px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-left h1 {
  font-size: 20px;
  margin: 0;
  color: #1e3a5f;
}

.menu-toggle {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #1e3a5f;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.avatar {
  font-size: 24px;
}

.logout-btn {
  padding: 8px 16px;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

.logout-btn:hover {
  background: #c0392b;
}

.page-content {
  padding: 30px;
}
</style>
