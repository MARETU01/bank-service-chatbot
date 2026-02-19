import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../components/Layout.vue'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: '/login',
        name: 'Login',
        component: () => import('../views/LoginView.vue'),
        meta: { title: '登录页面', icon: '📊' }
      },
      {
        path: '/register',
        name: 'Register',
        component: () => import('../views/RegisterView.vue'),
        meta: { title: '注册页面', icon: '📊' }
      },
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: '📊' }
      },
      {
        path: '/accounts',
        name: 'Accounts',
        component: () => import('../views/Accounts.vue'),
        meta: { title: '我的账户', icon: '💳' }
      },
      {
        path: '/transactions',
        name: 'Transactions',
        component: () => import('../views/Transactions.vue'),
        meta: { title: '交易记录', icon: '📋' }
      },
      {
        path: '/transfers',
        name: 'Transfers',
        component: () => import('../views/Transfers.vue'),
        meta: { title: '转账服务', icon: '💸' }
      },
      {
        path: '/chatbot',
        name: 'Chatbot',
        component: () => import('../views/Chatbot.vue'),
        meta: { title: '在线客服', icon: '🤖' }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心', icon: '👤' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
