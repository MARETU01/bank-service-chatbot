import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import http from './http'
import Message from './utils/message'
import './assets/styles/global.css'

const app = createApp(App)

app.config.globalProperties.$http = http

// 注册全局 message 插件
app.use(Message)

app.use(store).use(router).mount('#app')

// 应用启动时初始化用户认证状态
store.dispatch('initAuth').then(() => {
  console.log('Auth initialization complete')
})
