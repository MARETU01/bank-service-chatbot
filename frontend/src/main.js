import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import http from './http'
import './assets/styles/global.css'

const app = createApp(App)

app.config.globalProperties.$http = http

app.use(store).use(router).mount('#app')

// 应用启动时初始化用户认证状态
store.dispatch('initAuth').then(() => {
  console.log('Auth initialization complete')
})
