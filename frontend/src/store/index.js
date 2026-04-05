import { createStore } from 'vuex'
import router from '@/router'
import http from '@/http'
import { userApi } from '@/api/api'

export default createStore({
  state: {
    // User info
    user: null,
    // Token
    token: localStorage.getItem('token') || null,
    // Login status
    isLoggedIn: !!localStorage.getItem('token'),
    // Loading state
    loading: false,
    // User roles list
    roles: [],
    // Whether dynamic routes have been added
    dynamicRoutesAdded: false
  },
  
  getters: {
    // Get user ID
    userId: state => state.user?.userId,
    // Get username
    username: state => state.user?.username,
    // Get user email
    email: state => state.user?.email,
    // Get token
    token: state => state.token,
    // Is logged in
    isLoggedIn: state => state.isLoggedIn,
    // Is loading
    loading: state => state.loading,
    // Get user roles
    roles: state => state.roles,
    // Is admin
    isAdmin: state => state.roles.includes('ADMIN'),
  },
  
  mutations: {
    // Set user info
    SET_USER(state, user) {
      state.user = user
    },
    
    // Set token
    SET_TOKEN(state, token) {
      state.token = token
      state.isLoggedIn = !!token
      if (token) {
        localStorage.setItem('token', token)
      } else {
        localStorage.removeItem('token')
      }
    },
    
    // Set login status
    SET_LOGIN_STATUS(state, status) {
      state.isLoggedIn = status
    },
    
    // Set loading state
    SET_LOADING(state, loading) {
      state.loading = loading
    },
    
    // Clear user info (logout)
    CLEAR_USER(state) {
      state.user = null
      state.token = null
      state.isLoggedIn = false
      state.roles = []
      state.dynamicRoutesAdded = false
      localStorage.removeItem('token')
      localStorage.removeItem('roles')
    },

    // Set user roles
    SET_ROLES(state, roles) {
      state.roles = roles
      // Save roles to localStorage for route guards
      localStorage.setItem('roles', JSON.stringify(roles))
    },

    // Set dynamic routes added flag
    SET_DYNAMIC_ROUTES_ADDED(state, value) {
      state.dynamicRoutesAdded = value
    }
  },
  
  actions: {
    // Login action
    async login({ commit }, loginData) {
      commit('SET_LOADING', true)
      try {
        const response = await http.post('/users/login', loginData)
        const { code, data, message } = response.data
        
        if (code === 1 || code === 200) {
          // Save token
          commit('SET_TOKEN', data)
          
          // Fetch user info
          await this.dispatch('fetchUserInfo')
          
          // Fetch user roles and add dynamic routes
          await this.dispatch('fetchUserRoles')
          
          return { success: true, message }
        } else {
          return { success: false, message }
        }
      } catch (error) {
        console.error('Login error:', error)
        const message = error.response?.data?.message || 'Login failed, please try again'
        return { success: false, message }
      } finally {
        commit('SET_LOADING', false)
      }
    },
    
    // Fetch user info
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
        // If fetching user info fails (possibly token expired), clear login status
        if (error.response?.status === 401) {
          commit('CLEAR_USER')
          router.push('/login')
        }
      }
    },
    
    // Refresh token
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
        // Refresh failed, clear login status
        commit('CLEAR_USER')
        router.push('/login')
        return null
      }
    },
    
    // Fetch user roles
    async fetchUserRoles({ commit, state }) {
      if (!state.token) {
        return
      }

      try {
        const response = await userApi.getUserRoles()
        const { code, data } = response.data

        if (code === 1 || code === 200) {
          commit('SET_ROLES', data || [])
        }
      } catch (error) {
        console.error('Fetch user roles error:', error)
        // Role fetch failure doesn't affect normal use, default to regular user
        commit('SET_ROLES', ['USER'])
      }
    },

    // Logout action
    async logout({ commit, state }) {
      commit('CLEAR_USER')
      router.push('/')
    },
    
    // Initialize user state (called when app starts)
    async initAuth({ commit, dispatch }) {
      const token = localStorage.getItem('token')
      if (token) {
        commit('SET_TOKEN', token)
        await dispatch('fetchUserInfo')
        await dispatch('fetchUserRoles')
      }
    }
  },
  
  modules: {
  }
})
