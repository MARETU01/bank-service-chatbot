import { createStore } from 'vuex'
import router from '@/router'
import http from '@/http'

export default createStore({
  state: {
    // 用户信息
    user: null,
    // Token
    token: localStorage.getItem('token') || null,
    // 是否已登录
    isLoggedIn: !!localStorage.getItem('token'),
    // 加载状态
    loading: false
  },
  
  getters: {
    // 获取用户 ID
    userId: state => state.user?.userId,
    // 获取用户名
    username: state => state.user?.username,
    // 获取用户邮箱
    email: state => state.user?.email,
    // 获取 token
    token: state => state.token,
    // 是否已登录
    isLoggedIn: state => state.isLoggedIn,
    // 是否正在加载
    loading: state => state.loading
  },
  
  mutations: {
    // 设置用户信息
    SET_USER(state, user) {
      state.user = user
    },
    
    // 设置 token
    SET_TOKEN(state, token) {
      state.token = token
      state.isLoggedIn = !!token
      if (token) {
        localStorage.setItem('token', token)
      } else {
        localStorage.removeItem('token')
      }
    },
    
    // 设置登录状态
    SET_LOGIN_STATUS(state, status) {
      state.isLoggedIn = status
    },
    
    // 设置加载状态
    SET_LOADING(state, loading) {
      state.loading = loading
    },
    
    // 清除用户信息（登出）
    CLEAR_USER(state) {
      state.user = null
      state.token = null
      state.isLoggedIn = false
      localStorage.removeItem('token')
    }
  },
  
  actions: {
    // 登录动作
    async login({ commit }, loginData) {
      commit('SET_LOADING', true)
      try {
        const response = await http.post('/users/login', loginData)
        const { code, data, message } = response.data
        
        if (code === 1 || code === 200) {
          // 保存 token
          commit('SET_TOKEN', data)
          
          // 获取用户信息
          await this.dispatch('fetchUserInfo')
          
          return { success: true, message }
        } else {
          return { success: false, message }
        }
      } catch (error) {
        console.error('Login error:', error)
        const message = error.response?.data?.message || '登录失败，请重试'
        return { success: false, message }
      } finally {
        commit('SET_LOADING', false)
      }
    },
    
    // 获取用户信息
    async fetchUserInfo({ commit, state }) {
      if (!state.token) {
        return
      }
      
      try {
        const response = await http.get('/users/me')
        const { code, data } = response.data
        
        if (code === 1 || code === 200) {
          commit('SET_USER', data)
        }
      } catch (error) {
        console.error('Fetch user info error:', error)
        // 如果获取用户信息失败（可能是 token 过期），清除登录状态
        if (error.response?.status === 401) {
          commit('CLEAR_USER')
          router.push('/login')
        }
      }
    },
    
    // 刷新 token
    async refreshToken({ commit, state }) {
      if (!state.token) {
        return null
      }
      
      try {
        const response = await http.post('/users/refresh', {})
        const { code, data } = response.data
        
        if (code === 1 || code === 200) {
          commit('SET_TOKEN', data)
          return data
        }
        return null
      } catch (error) {
        console.error('Refresh token error:', error)
        // 刷新失败，清除登录状态
        commit('CLEAR_USER')
        router.push('/login')
        return null
      }
    },
    
    // 登出动作
    async logout({ commit }) {
      commit('CLEAR_USER')
      router.push('/')
    },
    
    // 初始化用户状态（应用启动时调用）
    async initAuth({ commit, dispatch }) {
      const token = localStorage.getItem('token')
      if (token) {
        commit('SET_TOKEN', token)
        await dispatch('fetchUserInfo')
      }
    }
  },
  
  modules: {
  }
})
