<template>
  <div class="chatbot">
    <div class="chatbot-container">
      <!-- Chat Window -->
      <div class="chat-window" ref="chatWindow">
        <div class="chat-messages" ref="chatMessages">
          <!-- Welcome Message -->
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

          <!-- Typing Indicator -->
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

      <!-- Quick Questions -->
      <div class="quick-questions">
        <span class="quick-label">Quick Questions:</span>
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

      <!-- Input Area -->
      <div class="chat-input-area">
        <div class="input-wrapper">
          <input 
            type="text" 
            v-model="inputMessage" 
            placeholder="Please enter your question..."
            @keyup.enter="sendMessage"
            :disabled="isTyping"
          />
          <button class="send-btn" @click="sendMessage" :disabled="!inputMessage.trim() || isTyping">
            <span>📤</span> Send
          </button>
        </div>
      </div>
    </div>

    <!-- Sidebar - Chat History -->
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <h3>💬 Chat History</h3>
        <button class="new-chat-btn" @click="createNewSession" title="New Chat">➕</button>
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
            <!-- Edit Mode: Show Input -->
            <div v-if="editingSessionId === session.sessionId" class="session-title-edit" @click.stop>
              <input
                ref="renameInput"
                v-model="editingTitle"
                class="rename-input"
                @keyup.enter="confirmRename(session.sessionId)"
                @keyup.esc="cancelRename"
                @blur="confirmRename(session.sessionId)"
                maxlength="50"
                placeholder="Enter session name"
              />
            </div>
            <!-- Normal Mode: Show Title -->
            <template v-else>
              <div class="session-title">{{ session.title || 'New Chat' }}</div>
              <div class="session-time">{{ formatSessionTime(session.updatedAt) }}</div>
            </template>
          </div>
          <button class="rename-btn" @click.stop="startRename(session)" title="Rename" v-if="editingSessionId !== session.sessionId">✏️</button>
          <button class="delete-btn" @click.stop="deleteSession(session.sessionId)" title="Delete Chat">🗑️</button>
        </div>
      </div>

      <div class="sidebar-header contact-info">
        <h3>📞 Contact Us</h3>
      </div>
      <div class="contact-list">
        <div class="contact-item">
          <span class="contact-icon">☎️</span>
          <div>
            <div class="contact-label">Customer Service Hotline</div>
            <div class="contact-value">95588</div>
          </div>
        </div>
        <div class="contact-item">
          <span class="contact-icon">📧</span>
          <div>
            <div class="contact-label">Online Support</div>
            <div class="contact-value">9:00 - 18:00</div>
          </div>
        </div>
        <div class="contact-item">
          <span class="contact-icon">🏦</span>
          <div>
            <div class="contact-label">Branch Locator</div>
            <div class="contact-value">Find nearby branches</div>
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

// Configure marked options
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
    const editingSessionId = ref(null)
    const editingTitle = ref('')
    const renameInput = ref(null)

    const messages = ref([])

    const quickQuestions = [
      'How to check balance?',
      'How to transfer money?',
      'How to change password?',
      'How to report a lost card?'
    ]

    // Get current time
    function getCurrentTime() {
      const now = new Date()
      return now.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    }

    // Render Markdown
    function renderMarkdown(text) {
      if (!text) return ''
      return marked.parse(text)
    }

    // Format session time
    function formatSessionTime(timeStr) {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) return 'Just now'
      if (diff < 3600000) return Math.floor(diff / 60000) + ' min ago'
      if (diff < 86400000) return Math.floor(diff / 3600000) + ' hours ago'
      if (diff < 604800000) return Math.floor(diff / 86400000) + ' days ago'
      
      return date.toLocaleDateString('zh-CN', { 
        month: '2-digit', 
        day: '2-digit' 
      })
    }

    // Scroll to bottom
    function scrollToBottom() {
      nextTick(() => {
        if (chatMessages.value) {
          chatMessages.value.scrollTop = chatMessages.value.scrollHeight
        }
      })
    }

    // Load session list
    async function loadSessions() {
      try {
        const res = await chatApi.getSessions()
        if (res.data.code === 1 || res.data.code === 200) {
          sessions.value = res.data.data || []
          // If there are sessions and none selected, select the first one
          if (sessions.value.length > 0 && !currentSessionId.value) {
            selectSession(sessions.value[0].sessionId)
          }
        }
      } catch (error) {
        console.error('Failed to load session list:', error)
      }
    }

    // Check if current session is empty (only welcome message, no user messages)
    function isCurrentSessionEmpty() {
      return messages.value.every(msg => msg.type === 'bot')
    }

    // Create new session (reuse if current is empty, don't create duplicate)
    async function createNewSession() {
      // If current session exists and is empty (no user messages), reuse it
      if (currentSessionId.value && isCurrentSessionEmpty()) {
        // Already in empty session, no need to create new
        return
      }

      try {
        const res = await chatApi.createSession()
        if (res.data.code === 1 || res.data.code === 200) {
          const newSession = res.data.data
          sessions.value.unshift(newSession)
          // Directly set current session ID, don't call selectSession (avoid async loading overwriting messages)
          currentSessionId.value = newSession.sessionId
          // Clear messages, show welcome message
          messages.value = [{
            type: 'bot',
            text: 'Hello! I am your intelligent banking assistant. How can I help you?',
            time: getCurrentTime()
          }]
          Message.success('New chat created successfully')
        }
      } catch (error) {
        console.error('Failed to create session:', error)
        Message.error('Failed to create session')
      }
    }

    // Select session
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
          // If empty session (no messages), show welcome message
          if (messages.value.length === 0) {
            messages.value = [{
              type: 'bot',
              text: 'Hello! I am your intelligent banking assistant. How can I help you?',
              time: getCurrentTime()
            }]
          }
          scrollToBottom()
        }
      } catch (error) {
        console.error('Failed to load messages:', error)
        Message.error('Failed to load messages')
      }
    }

    // Start renaming
    function startRename(session) {
      editingSessionId.value = session.sessionId
      editingTitle.value = session.title || ''
      // Wait for DOM update then focus input
      nextTick(() => {
        if (renameInput.value) {
          // ref in v-for returns array
          const input = Array.isArray(renameInput.value) ? renameInput.value[0] : renameInput.value
          if (input) {
            input.focus()
            input.select()
          }
        }
      })
    }

    // Confirm rename
    async function confirmRename(sessionId) {
      const newTitle = editingTitle.value.trim()
      if (!newTitle || editingSessionId.value !== sessionId) {
        cancelRename()
        return
      }

      // Check if title has changed
      const session = sessions.value.find(s => s.sessionId === sessionId)
      if (session && session.title === newTitle) {
        cancelRename()
        return
      }

      try {
        const res = await chatApi.renameSession(sessionId, newTitle)
        if (res.data.code === 1 || res.data.code === 200) {
          // Update local data
          if (session) {
            session.title = newTitle
          }
          Message.success('Rename successful')
        }
      } catch (error) {
        console.error('Failed to rename:', error)
        Message.error('Failed to rename')
      } finally {
        cancelRename()
      }
    }

    // Cancel rename
    function cancelRename() {
      editingSessionId.value = null
      editingTitle.value = ''
    }

    // Delete session
    async function deleteSession(sessionId) {
      try {
        const res = await chatApi.deleteSession(sessionId)
        if (res.data.code === 1 || res.data.code === 200) {
          sessions.value = sessions.value.filter(s => s.sessionId !== sessionId)
          if (currentSessionId.value === sessionId) {
            currentSessionId.value = null
            messages.value = [{
              type: 'bot',
              text: 'Hello! I am your intelligent banking assistant. How can I help you?',
              time: getCurrentTime()
            }]
          }
          Message.success('Session deleted successfully')
        }
      } catch (error) {
        console.error('Failed to delete session:', error)
        Message.error('Failed to delete session')
      }
    }

    // Send message
    async function sendMessage() {
      const text = inputMessage.value.trim()
      if (!text || isTyping.value) return

      // If no current session, create one first
      if (!currentSessionId.value) {
        await createNewSession()
        if (!currentSessionId.value) return
      }

      // Add user message
      messages.value.push({
        type: 'user',
        text: text,
        time: getCurrentTime()
      })

      inputMessage.value = ''
      scrollToBottom()
      isTyping.value = true

      try {
        // Send message to backend (use fetch for streaming response)
        const response = await chatApi.sendMessage({
          sessionId: currentSessionId.value,
          content: text
        })

        // Handle streaming response
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
        console.error('Failed to send message:', error)
        isTyping.value = false
        Message.error('Failed to send message, please try again')
        
        // Remove failed message
        messages.value.pop()
      }
    }

    // Send quick question
    function sendQuickQuestion(question) {
      inputMessage.value = question
      sendMessage()
    }

    // Load session list on component mount
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
      editingSessionId,
      editingTitle,
      renameInput,
      getCurrentTime,
      formatSessionTime,
      scrollToBottom,
      loadSessions,
      createNewSession,
      selectSession,
      deleteSession,
      startRename,
      confirmRename,
      cancelRename,
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

/* Typing Indicator */
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

/* Quick Questions */
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

/* Input Area */
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

/* Sidebar */
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

.rename-btn,
.delete-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: var(--font-size-lg);
  opacity: 0;
  transition: all var(--transition-normal);
  padding: 4px;
  flex-shrink: 0;
}

.session-item:hover .rename-btn,
.session-item:hover .delete-btn {
  opacity: 1;
}

.rename-btn:hover,
.delete-btn:hover {
  transform: scale(1.2);
}

/* Rename Input */
.session-title-edit {
  width: 100%;
}

.rename-input {
  width: 100%;
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid var(--glass-border-hover);
  border-radius: var(--radius-md);
  color: var(--color-white);
  font-size: var(--font-size-sm);
  outline: none;
  transition: all var(--transition-normal);
}

.rename-input:focus {
  border-color: var(--input-border-focus);
  background: rgba(255, 255, 255, 0.15);
}

.rename-input::placeholder {
  color: var(--input-placeholder);
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

/* Markdown Content Styles */
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

/* Math Formula Styles */
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
