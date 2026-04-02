import axios from 'axios'
import router from '@/router'
import store from '@/store'

const instance = axios.create({
    baseURL: 'http://ipv6.maretu.top:8080',
    timeout: 30000,
    withCredentials: true,
})

// 标记是否正在刷新 token
let isRefreshing = false
// 存储 token 刷新期间的请求队列
let refreshSubscribers = []

/**
 * 订阅 token 刷新事件
 * @param {Function} cb - token 刷新后的回调函数
 */
function subscribeTokenRefresh(cb) {
    refreshSubscribers.push(cb)
}

/**
 * 执行 token 刷新后的回调队列
 * @param {string} token - 新的 token
 */
function onRefreshed(token) {
    refreshSubscribers.forEach(cb => cb(token))
    refreshSubscribers = []
}

/**
 * 处理刷新 token
 * @returns {Promise<string|null>} - 新的 token 或 null
 */
async function handleRefreshToken() {
    try {
        const response = await instance.post('/users/refresh')
        
        const { code, data } = response.data
        if (code === 1 || code === 200) {
            const newToken = data
            // 更新 localStorage
            localStorage.setItem('token', newToken)
            // 更新 Vuex
            store.commit('SET_TOKEN', newToken)
            return newToken
        }
        return null
    } catch (error) {
        console.error('Refresh token failed:', error)
        return null
    }
}

// 请求拦截器
instance.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        const userInfo = localStorage.getItem('user-info')
        
        if (token) {
            config.headers['Maretu'] = token
        }
        // 添加 user-info 请求头（用于获取当前用户信息）
        if (userInfo) {
            config.headers['user-info'] = userInfo
        }
        return config
    },
    error => {
        console.error('Request error:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
instance.interceptors.response.use(
    response => {
        return response
    },
    async error => {
        const { config, response } = error
        
        // 处理 401 token 过期/无效
        if (response?.status === 401) {
            // 如果是 refresh 请求本身失败，清除 token 并跳转登录
            if (config.url.includes('/users/refresh')) {
                store.commit('CLEAR_USER')
                if (router.currentRoute.value.path !== '/login') {
                    router.push('/login')
                }
                return Promise.reject(error)
            }
            
            // 如果正在刷新 token，将请求加入队列
            if (isRefreshing) {
                return new Promise((resolve) => {
                    subscribeTokenRefresh((token) => {
                        config.headers['Maretu'] = token
                        resolve(instance(config))
                    })
                })
            }
            
            // 开始刷新 token
            isRefreshing = true
            
            try {
                const newToken = await handleRefreshToken()
                
                if (newToken) {
                    // 执行队列中的请求
                    onRefreshed(newToken)
                    
                    // 重试原请求
                    config.headers['Maretu'] = newToken
                    return instance(config)
                } else {
                    // 刷新失败，清除登录状态并跳转登录页
                    store.commit('CLEAR_USER')
                    if (router.currentRoute.value.path !== '/login') {
                        router.push('/login')
                    }
                    return Promise.reject(error)
                }
            } catch (refreshError) {
                // 刷新失败，清除登录状态并跳转登录页
                store.commit('CLEAR_USER')
                if (router.currentRoute.value.path !== '/login') {
                    router.push('/login')
                }
                return Promise.reject(refreshError)
            } finally {
                isRefreshing = false
            }
        }
        
        // 其他错误处理
        const message = response?.data?.message || error.message || '请求失败，请重试'
        console.error('Response error:', message)
        return Promise.reject(error)
    }
)

export default instance
