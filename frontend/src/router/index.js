import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/Layout'
import Message from '@/utils/message'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView'),
    meta: { title: 'Home', requiresAuth: false }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView'),
    meta: { title: 'Login', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView'),
    meta: { title: 'Register', requiresAuth: false }
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('@/views/ResetPasswordView'),
    meta: { title: 'Reset Password', requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard'),
        meta: { title: 'Dashboard', icon: '📊', requiresAuth: true }
      },
      {
        path: 'accounts',
        name: 'Accounts',
        component: () => import('@/views/Accounts'),
        meta: { title: 'My Accounts', icon: '💳', requiresAuth: true }
      },
      {
        path: 'transactions',
        name: 'Transactions',
        component: () => import('@/views/Transactions'),
        meta: { title: 'Transactions', icon: '📋', requiresAuth: true }
      },
      {
        path: 'transfers',
        name: 'Transfers',
        component: () => import('@/views/Transfers'),
        meta: { title: 'Transfer Service', icon: '💸', requiresAuth: true }
      },
      {
        path: 'chatbot',
        name: 'Chatbot',
        component: () => import('@/views/Chatbot'),
        meta: { title: 'Online Support', icon: '🤖', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile'),
        meta: { title: 'Profile', icon: '👤', requiresAuth: true }
      },
      {
        path: 'admin/knowledge',
        name: 'KnowledgeManage',
        component: () => import('@/views/admin/KnowledgeManage'),
        meta: { title: 'Knowledge Base', icon: '📚', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'admin/chat-stats',
        name: 'ChatStats',
        component: () => import('@/views/admin/ChatStats'),
        meta: { title: 'Chat Statistics', icon: '📈', requiresAuth: true, requiresAdmin: true }
      },
      {
        path: 'admin/users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage'),
        meta: { title: 'User Management', icon: '👥', requiresAuth: true, requiresAdmin: true }
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

// Global before guard
router.beforeEach((to, from, next) => {
  // Set page title
  document.title = to.meta.title || 'Bank Service Chatbot'
  
  // Get token
  const token = localStorage.getItem('token')
  const requiresAuth = to.meta.requiresAuth !== false
  
  // Check if login is required
  if (requiresAuth && !token) {
    // Not logged in, redirect to login page
    next({
      path: '/login',
      query: { redirect: to.fullPath } // Save original target path
    })
    return
  }
  
  // Check if it's an admin-only page
  if (to.meta.requiresAdmin) {
    const roles = JSON.parse(localStorage.getItem('roles') || '[]')
    const isAdmin = roles.includes('ADMIN')
    
    if (!isAdmin) {
      // Regular user trying to access admin page, show error and redirect to home
      Message.error('Access denied: Admin access required')
      next('/dashboard')
      return
    }
  }
  
  // Check if logged-in user accesses login page
  if (to.path === '/login' && token) {
    // Logged-in user accessing login page, redirect to home
    next('/')
    return
  }
  
  // Allow access
  next()
})

// Global after guard
router.afterEach((to, from) => {
  // Can perform page analysis here, etc.
  console.log(`Route changed from ${from.path} to ${to.path}`)
})

export default router
