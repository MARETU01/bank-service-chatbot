<template>
  <div class="chatbot">
    <div class="chatbot-container">
      <!-- 聊天窗口 -->
      <div class="chat-window" ref="chatWindow">
        <div class="chat-messages" ref="chatMessages">
          <!-- 欢迎消息 -->
          <div class="message bot-message" v-for="(msg, index) in messages" :key="index" :class="msg.type">
            <div class="message-avatar">
              <span v-if="msg.type === 'bot'">🤖</span>
              <span v-else>👤</span>
            </div>
            <div class="message-content">
              <div class="message-bubble markdown-content" v-html="renderMarkdown(msg.text)"></div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>

          <!-- 正在输入 -->
          <div class="message bot-message" v-if="isTyping && messages.length > 0 && messages[messages.length - 1].text === ''">
            <div class="message-avatar">🤖</div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷问题 -->
      <div class="quick-questions">
        <span class="quick-label">快捷问题：</span>
        <button 
          class="quick-btn" 
          v-for="(q, index) in quickQuestions" 
          :key="index"
          @click="sendQuickQuestion(q)"
          :disabled="isTyping"
        >
          {{ q }}
        </button>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <div class="input-wrapper">
          <input 
            type="text" 
            v-model="inputMessage" 
            placeholder="请输入您的问题..."
            @keyup.enter="sendMessage"
            :disabled="isTyping"
          />
          <button class="send-btn" @click="sendMessage" :disabled="!inputMessage.trim() || isTyping">
            <span>📤</span> 发送
          </button>
        </div>
      </div>
    </div>

    <!-- 侧边栏 - 聊天记录 -->
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <h3>💬 会话列表</h3>
        <button class="new-chat-btn" @click="createNewSession" title="新建会话">➕</button>
      </div>
      <div class="session-list">
        <div 
          class="session-item" 
          v-for="session in sessions" 
          :key="session.sessionId"
          :class="{ active: currentSessionId === session.sessionId }"
          @click="selectSession(session.sessionId)"
        >
          <div class="session-icon">💬</div>
          <div class="session-content">
            <div class="session-title">{{ session.title || '新会话' }}</div>
            <div class="session-time">{{ formatSessionTime(session.updatedAt) }}</div>
          </div>
          <button class="delete-btn" @click.stop="deleteSession(session.sessionId)" title="删除会话">🗑️</button>
        </div>
      </div>

      <div class="sidebar-header contact-info">
        <h3>📞 联系方式</h3>
      </div>
      <div class="contact-list">
        <div class="contact-item">
          <span class="contact-icon">☎️</span>
          <div>
            <div class="contact-label">客服热线</div>
            <div class="contact-value">95588</div>
          </div>
        </div>
        <div class="contact-item">
          <span class="contact-icon">📧</span>
          <div>
            <div class="contact-label">在线客服</div>
            <div class="contact-value">9:00 - 18:00</div>
          </div>
        </div>
        <div class="contact-item">
          <span class="contact-icon">🏦</span>
          <div>
            <div class="contact-label">网点查询</div>
            <div class="contact-value">查看附近网点</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { chatApi } from '@/api/api'
import Message from '@/utils/message'
import { marked } from 'marked'

// 配置 marked 选项
marked.setOptions({
  breaks: true,
  gfm: true
})

export default {
  name: 'Chatbot',
  setup() {
    const chatWindow = ref(null)
    const chatMessages = ref(null)
    const inputMessage = ref('')
    const isTyping = ref(false)
    const currentSessionId = ref(null)
    const sessions = ref([])

    const messages = ref([])

    const quickQuestions = [
      '如何查询余额？',
      '如何转账？',
      '如何修改密码？',
      '如何挂失银行卡？'
    ]

    // 获取当前时间
    function getCurrentTime() {
      const now = new Date()
      return now.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    }

    // 渲染 Markdown
    function renderMarkdown(text) {
      if (!text) return ''
      return marked.parse(text)
    }

    // 格式化会话时间
    function formatSessionTime(timeStr) {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
      
      return date.toLocaleDateString('zh-CN', { 
        month: '2-digit', 
        day: '2-digit' 
      })
    }

    // 滚动到底部
    function scrollToBottom() {
      nextTick(() => {
        if (chatMessages.value) {
          chatMessages.value.scrollTop = chatMessages.value.scrollHeight
        }
      })
    }

    // 获取会话列表
    async function loadSessions() {
      try {
        const res = await chatApi.getSessions()
        if (res.data.code === 1 || res.data.code === 200) {
          sessions.value = res.data.data || []
          // 如果有会话且当前没有选中，选择第一个
          if (sessions.value.length > 0 && !currentSessionId.value) {
            selectSession(sessions.value[0].sessionId)
          }
        }
      } catch (error) {
        console.error('加载会话列表失败:', error)
      }
    }

    // 创建新会话
    async function createNewSession() {
      try {
        const res = await chatApi.createSession()
        if (res.data.code === 1 || res.data.code === 200) {
          const newSession = res.data.data
          sessions.value.unshift(newSession)
          // 直接设置当前会话ID，不调用 selectSession（避免异步加载消息覆盖 messages）
          currentSessionId.value = newSession.sessionId
          // 清空消息，显示欢迎语
          messages.value = [{
            type: 'bot',
            text: '您好！我是您的智能银行助手，请问有什么可以帮助您的？',
            time: getCurrentTime()
          }]
          Message.success('新建会话成功')
        }
      } catch (error) {
        console.error('创建会话失败:', error)
        Message.error('创建会话失败')
      }
    }

    // 选择会话
    async function selectSession(sessionId) {
      currentSessionId.value = sessionId
      messages.value = []
      isTyping.value = false
      
      try {
        const res = await chatApi.getMessages(sessionId)
        if (res.data.code === 1 || res.data.code === 200) {
          const msgList = res.data.data || []
          messages.value = msgList.map(msg => ({
            type: msg.senderType === 1 ? 'user' : 'bot',
            text: msg.content,
            time: msg.createdAt ? new Date(msg.createdAt).toLocaleTimeString('zh-CN', { 
              hour: '2-digit', 
              minute: '2-digit' 
            }) : getCurrentTime()
          }))
          scrollToBottom()
        }
      } catch (error) {
        console.error('加载消息失败:', error)
        Message.error('加载消息失败')
      }
    }

    // 删除会话
    async function deleteSession(sessionId) {
      try {
        const res = await chatApi.deleteSession(sessionId)
        if (res.data.code === 1 || res.data.code === 200) {
          sessions.value = sessions.value.filter(s => s.sessionId !== sessionId)
          if (currentSessionId.value === sessionId) {
            currentSessionId.value = null
            messages.value = [{
              type: 'bot',
              text: '您好！我是您的智能银行助手，请问有什么可以帮助您的？',
              time: getCurrentTime()
            }]
          }
          Message.success('删除会话成功')
        }
      } catch (error) {
        console.error('删除会话失败:', error)
        Message.error('删除会话失败')
      }
    }

    // 发送消息
    async function sendMessage() {
      const text = inputMessage.value.trim()
      if (!text || isTyping.value) return

      // 如果没有当前会话，先创建
      if (!currentSessionId.value) {
        await createNewSession()
        if (!currentSessionId.value) return
      }

      // 添加用户消息
      messages.value.push({
        type: 'user',
        text: text,
        time: getCurrentTime()
      })

      inputMessage.value = ''
      scrollToBottom()
      isTyping.value = true

      try {
        // 发送消息到后端（使用 fetch 实现流式响应）
        const response = await chatApi.sendMessage({
          sessionId: currentSessionId.value,
          content: text
        })

        // 处理流式响应
        const reader = response.body.pipeThrough(new TextDecoderStream('utf-8')).getReader()
        let botResponse = ''
        const botMessage = {
          type: 'bot',
          text: '',
          time: getCurrentTime()
        }
        messages.value.push(botMessage)

        while (true) {
          const { done, value } = await reader.read()
          if (done) break
          
          botResponse += value
          messages.value[messages.value.length - 1].text = botResponse
          scrollToBottom()
        }

        isTyping.value = false
      } catch (error) {
        console.error('发送消息失败:', error)
        isTyping.value = false
        Message.error('发送消息失败，请重试')
        
        // 移除失败的消息
        messages.value.pop()
      }
    }

    // 发送快捷问题
    function sendQuickQuestion(question) {
      inputMessage.value = question
      sendMessage()
    }

    // 组件挂载时加载会话列表
    onMounted(() => {
      loadSessions()
    })

    return {
      chatWindow,
      chatMessages,
      inputMessage,
      isTyping,
      messages,
      quickQuestions,
      sessions,
      currentSessionId,
      getCurrentTime,
      formatSessionTime,
      scrollToBottom,
      loadSessions,
      createNewSession,
      selectSession,
      deleteSession,
      sendMessage,
      sendQuickQuestion,
      renderMarkdown
    }
  }
}
</script>

<style scoped>
.chatbot {
  display: flex;
  gap: var(--spacing-2xl);
  height: calc(100vh - 140px);
  max-width: var(--container-max-width-lg);
}

.chatbot-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-3xl);
  border: 1px solid var(--glass-border);
  overflow: hidden;
}

.chat-window {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-2xl);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.message {
  display: flex;
  gap: var(--spacing-lg);
  max-width: 80%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  background: var(--glass-border-hover);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-2xl);
  flex-shrink: 0;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
  align-items: flex-start;
}

.message-bubble {
  background: var(--glass-bg);
  padding: var(--spacing-lg) var(--spacing-2xl);
  border-radius: var(--radius-xl);
  border-top-left-radius: var(--radius-sm);
  border: 1px solid var(--glass-border);
  text-align: left;
}

.message-bubble p {
  margin: 0;
  white-space: pre-wrap;
  line-height: var(--line-height-normal);
  color: var(--color-white);
}

.message.user .message-bubble {
  background: var(--glass-bg-hover);
  color: var(--color-white);
  border-radius: var(--radius-xl);
  border-top-right-radius: var(--radius-sm);
}

.message-time {
  font-size: var(--font-size-xs);
  color: var(--text-on-gradient-muted);
  padding: 0 var(--spacing-xs);
  text-align: left;
}

.message.user .message-time {
  text-align: right;
}

.message.user .message-bubble {
  text-align: right;
}

.message.user .message-content {
  align-items: flex-end;
}

/* 输入指示器 */
.typing-indicator {
  display: flex;
  gap: var(--spacing-xs);
  padding: var(--spacing-lg) var(--spacing-2xl);
}

.typing-indicator span {
  width: var(--spacing-sm);
  height: var(--spacing-sm);
  border-radius: var(--radius-full);
  background: var(--text-on-gradient-placeholder);
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-8px);
  }
}

/* 快捷问题 */
.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  padding: var(--spacing-lg) var(--spacing-2xl);
  background: var(--glass-bg-light);
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.quick-label {
  font-size: var(--font-size-xs);
  color: var(--text-on-gradient-secondary);
  display: flex;
  align-items: center;
}

.quick-btn {
  padding: var(--spacing-xs) var(--spacing-md);
  background: var(--glass-bg);
  border: 1px solid var(--glass-border-hover);
  color: var(--color-white);
  border-radius: var(--radius-2xl);
  cursor: pointer;
  font-size: var(--font-size-xs);
  transition: all var(--transition-normal);
}

.quick-btn:hover:not(:disabled) {
  background: var(--glass-bg-hover);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.quick-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 输入区域 */
.chat-input-area {
  padding: var(--spacing-lg) var(--spacing-2xl);
  background: var(--glass-bg-light);
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}

.input-wrapper {
  display: flex;
  gap: var(--spacing-lg);
}

.input-wrapper input {
  flex: 1;
  padding: var(--spacing-lg) var(--spacing-2xl);
  border: 1px solid var(--input-border);
  border-radius: var(--radius-full);
  font-size: var(--font-size-md);
  background: var(--input-bg);
  color: var(--color-white);
  transition: all var(--transition-normal);
}

.input-wrapper input:focus {
  outline: none;
  border-color: var(--input-border-focus);
  background: var(--input-bg-focus);
}

.input-wrapper input::placeholder {
  color: var(--input-placeholder);
}

.send-btn {
  padding: var(--spacing-lg) var(--spacing-2xl);
  background: var(--btn-glass-bg);
  color: var(--color-white);
  border: 1px solid var(--btn-glass-border);
  border-radius: var(--radius-full);
  cursor: pointer;
  font-size: var(--font-size-md);
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  transition: all var(--transition-normal);
}

.send-btn:hover:not(:disabled) {
  background: var(--btn-glass-hover);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 侧边栏 */
.chat-sidebar {
  width: 300px;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-3xl);
  border: 1px solid var(--glass-border);
  overflow-y: auto;
  padding: var(--spacing-2xl);
}

.sidebar-header {
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-lg);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  color: var(--color-white);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
}

.new-chat-btn {
  background: var(--glass-bg-hover);
  border: 1px solid var(--glass-border-hover);
  color: var(--color-white);
  border-radius: var(--radius-lg);
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-normal);
  font-size: var(--font-size-lg);
}

.new-chat-btn:hover {
  background: var(--btn-glass-hover);
  transform: scale(1.1);
}

.session-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-2xl);
}

.session-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--glass-bg-light);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-normal);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.session-item:hover {
  background: var(--glass-border-hover);
  transform: translateX(-4px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.session-item.active {
  background: var(--btn-glass-bg);
  border-color: var(--btn-glass-border);
}

.session-icon {
  font-size: var(--font-size-xl);
}

.session-content {
  flex: 1;
  min-width: 0;
}

.session-title {
  font-size: var(--font-size-sm);
  color: var(--color-white);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: var(--font-size-xs);
  color: var(--text-on-gradient-muted);
  margin-top: 2px;
}

.delete-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: var(--font-size-lg);
  opacity: 0;
  transition: all var(--transition-normal);
  padding: 4px;
}

.session-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  transform: scale(1.2);
}

.contact-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.contact-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-lg);
  background: var(--glass-bg-light);
  border-radius: var(--radius-xl);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.contact-icon {
  font-size: var(--font-size-2xl);
}

.contact-label {
  font-size: var(--font-size-xs);
  color: var(--text-on-gradient-muted);
}

.contact-value {
  font-size: var(--font-size-md);
  color: var(--color-white);
  font-weight: var(--font-weight-medium);
}

.contact-info {
  margin-top: var(--spacing-2xl);
}

/* Markdown 内容样式 */
.markdown-content {
  color: var(--color-white);
  line-height: var(--line-height-normal);
}

.markdown-content :deep(p) {
  margin: 0 0 var(--spacing-md) 0;
  line-height: 1.6;
}

.markdown-content :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin: var(--spacing-lg) 0 var(--spacing-sm) 0;
  font-weight: var(--font-weight-semibold);
  line-height: 1.3;
}

.markdown-content :deep(h1) { font-size: var(--font-size-2xl); }
.markdown-content :deep(h2) { font-size: var(--font-size-xl); }
.markdown-content :deep(h3) { font-size: var(--font-size-lg); }

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: var(--spacing-sm) 0;
  padding-left: var(--spacing-xl);
}

.markdown-content :deep(li) {
  margin: var(--spacing-xs) 0;
}

.markdown-content :deep(code) {
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 6px;
  border-radius: var(--radius-md);
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 0.9em;
}

.markdown-content :deep(pre) {
  background: rgba(0, 0, 0, 0.3);
  padding: var(--spacing-md);
  border-radius: var(--radius-lg);
  overflow-x: auto;
  margin: var(--spacing-md) 0;
}

.markdown-content :deep(pre code) {
  background: transparent;
  padding: 0;
}

.markdown-content :deep(blockquote) {
  border-left: 3px solid var(--glass-border-hover);
  padding-left: var(--spacing-md);
  margin: var(--spacing-md) 0;
  color: var(--text-on-gradient-secondary);
}

.markdown-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: var(--spacing-md) 0;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  border: 1px solid var(--glass-border);
  padding: var(--spacing-sm);
  text-align: left;
}

.markdown-content :deep(th) {
  background: rgba(255, 255, 255, 0.1);
  font-weight: var(--font-weight-semibold);
}

.markdown-content :deep(a) {
  color: var(--btn-glass-bg);
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  text-decoration: underline;
}

.markdown-content :deep(hr) {
  border: none;
  border-top: 1px solid var(--glass-border);
  margin: var(--spacing-lg) 0;
}

/* 数学公式样式 */
.markdown-content :deep(.math) {
  font-family: 'Times New Roman', serif;
  font-style: italic;
}

.markdown-content :deep(.math-inline) {
  display: inline;
  background: rgba(255, 255, 255, 0.05);
  padding: 2px 4px;
  border-radius: var(--radius-sm);
}

.markdown-content :deep(.math-display) {
  display: block;
  text-align: center;
  padding: var(--spacing-md);
  background: rgba(0, 0, 0, 0.2);
  border-radius: var(--radius-lg);
  margin: var(--spacing-md) 0;
}
</style>
