import { createApp, h } from 'vue'

// 消息类型对应的样式类
const typeClassMap = {
  success: 'message-success',
  error: 'message-error',
  warning: 'message-warning',
  info: 'message-info'
}

// 消息类型对应的图标
const typeIconMap = {
  success: '✓',
  error: '✕',
  warning: '⚠',
  info: 'ℹ'
}

// 创建消息组件
const createMessage = ({ message, type = 'info', duration = 3000 }) => {
  // 创建容器
  const container = document.createElement('div')
  container.className = 'message-container'
  document.body.appendChild(container)

  // 用于防止重复关闭的标志
  let isClosing = false

  // 创建消息元素
  const messageApp = createApp({
    data() {
      return {
        visible: false
      }
    },
    mounted() {
      // 延迟显示以触发动画
      setTimeout(() => {
        this.visible = true
      }, 10)

      // 自动关闭
      if (duration > 0) {
        setTimeout(() => {
          this.close()
        }, duration)
      }
    },
    methods: {
      close() {
        // 防止重复关闭
        if (isClosing) return
        isClosing = true

        this.visible = false
        setTimeout(() => {
          messageApp.unmount()
          // 使用 remove() 方法更安全，即使节点已不在 DOM 中也不会报错
          if (container.parentNode) {
            container.parentNode.removeChild(container)
          }
        }, 300)
      }
    },
    render() {
      return h('div', {
        class: ['message-wrapper', typeClassMap[type] || 'message-info', { 'message-visible': this.visible }],
        onClick: this.close
      }, [
        h('span', { class: 'message-icon' }, typeIconMap[type] || 'ℹ'),
        h('span', { class: 'message-content' }, message)
      ])
    }
  })

  messageApp.mount(container)
  
  return {
    close: () => {
      const instance = messageApp._instance
      if (instance && instance.proxy) {
        instance.proxy.close()
      }
    }
  }
}

// 定义 Message 函数
const Message = (options) => {
  if (typeof options === 'string') {
    options = { message: options }
  }
  return createMessage(options)
}

// 定义快捷方法
Message.success = (message, duration) => {
  return Message({ message, type: 'success', duration })
}

Message.error = (message, duration) => {
  return Message({ message, type: 'error', duration })
}

Message.warning = (message, duration) => {
  return Message({ message, type: 'warning', duration })
}

Message.info = (message, duration) => {
  return Message({ message, type: 'info', duration })
}

// 安装插件
Message.install = (app) => {
  // 注册全局属性
  app.config.globalProperties.$message = Message
  
  // 提供 inject/provide 方式
  app.provide('message', Message)
}

export default Message
