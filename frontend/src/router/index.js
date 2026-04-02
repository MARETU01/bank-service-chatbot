import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../components/Layout.vue'
import Message from '../utils/message'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomeView.vue'),
    meta: { title: '首页', requiresAuth: false }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录页面', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue'),
    meta: { title: '注册页面', requiresAuth: false }
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('../views/ResetPasswordView.vue'),
    meta: { title: '重设密码', requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: '📊', requiresAuth: true }
      },
      {
        path: 'accounts',
        name: 'Accounts',
        component: () => import('../views/Accounts.vue'),
        meta: { title: '我的账户', icon: '💳', requiresAuth: true }
      },
      {
        path: 'transactions',
        name: 'Transactions',
        component: () => import('../views/Transactions.vue'),
        meta: { title: '交易记录', icon: '📋', requiresAuth: true }
      },
      {
        path: 'transfers',
        name: 'Transfers',
        component: () => import('../views/Transfers.vue'),
        meta: { title: '转账服务', icon: '💸', requiresAuth: true }
      },
      {
        path: 'chatbot',
        name: 'Chatbot',
        component: () => import('../views/Chatbot.vue'),
        meta: { title: '在线客服', icon: '🤖', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', icon: '👤', requiresAuth: true }
      },
      {
        path: 'admin/knowledge',
        name: 'KnowledgeManage',
        component: () => import('../views/admin/KnowledgeManage.vue'),
        meta: { title: '知识库管理', icon: '📚', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'admin/users',
        name: 'UserManage',
        component: () => import('../views/admin/UserManage.vue'),
        meta: { title: '用户管理', icon: '👥', requiresAuth: true, requiresAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 滚动到顶部
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || '银行服务机器人'
  
  // 获取 token
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false
  
  // 检查是否需要登录
  if (requiresAuth && !token) {
    // 未登录，重定向到登录页
    next({
      path: '/login',
      query: { redirect: to.fullPath } // 保存原始目标路径
    })
    return
  }
  
  // 检查是否是管理员专属页面
  if (to.meta.requiresAdmin) {
    const roles = JSON.parse(localStorage.getItem('roles') || '[]')
    const isAdmin = roles.includes('ADMIN')
    
    if (!isAdmin) {
      // 普通用户尝试访问管理员页面，显示错误提示并重定向到首页
      Message.error('无权访问管理员页面')
      next('/dashboard')
      return
    }
  }
  
  // 检查已登录用户访问登录页
  if (to.path === '/login' && token) {
    // 已登录用户访问登录页，重定向到首页
    next('/')
    return
  }
  
  // 允许访问
  next()
})

// 全局后置守卫
router.afterEach((to, from) => {
  // 可以在这里进行页面分析等操作
  console.log(`路由从 ${from.path} 切换到 ${to.path}`)
})

export default router
