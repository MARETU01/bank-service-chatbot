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
              <div class="message-bubble">
                <p>{{ msg.text }}</p>
              </div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>

          <!-- 正在输入 -->
          <div class="message bot-message" v-if="isTyping">
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
        <h3>💬 常见问题</h3>
      </div>
      <div class="faq-list">
        <div 
          class="faq-item" 
          v-for="(faq, index) in faqs" 
          :key="index"
          @click="sendQuickQuestion(faq.question)"
        >
          <div class="faq-icon">❓</div>
          <div class="faq-content">
            <div class="faq-question">{{ faq.question }}</div>
          </div>
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
import { ref, reactive, nextTick } from 'vue'

export default {
  name: 'Chatbot',
  setup() {
    const chatWindow = ref(null)
    const chatMessages = ref(null)
    const inputMessage = ref('')
    const isTyping = ref(false)

    const messages = ref([
      {
        type: 'bot',
        text: '您好！我是您的智能银行助手，请问有什么可以帮助您的？',
        time: getCurrentTime()
      }
    ])

    const quickQuestions = [
      '如何查询余额？',
      '如何转账？',
      '如何修改密码？',
      '如何挂失银行卡？'
    ]

    const faqs = ref([
      { question: '如何查询账户余额？' },
      { question: '转账限额是多少？' },
      { question: '如何申请信用卡？' },
      { question: '定期存款利率是多少？' },
      { question: '如何开通网上银行？' },
      { question: '银行卡丢失怎么办？' },
      { question: '如何修改手机号码？' },
      { question: '理财产品有哪些？' }
    ])

    // 模拟自动回复
    const autoReplies = {
      '余额': '您可以通过以下方式查询余额：\n1. 登录网上银行\n2. 使用手机银行 APP\n3. 拨打客服热线 95588\n4. 前往 ATM 机或银行网点查询',
      '转账': '转账服务支持以下类型：\n1. 行内转账：实时到账，免费\n2. 跨行转账：2 小时内到账，手续费 0.5%\n3. 加急转账：实时到账，手续费 1%\n\n您可以在"转账服务"页面进行操作。',
      '密码': '修改密码方式：\n1. 登录网上银行，进入"安全中心"\n2. 使用手机银行 APP\n3. 前往银行网点柜台\n4. 拨打客服热线 95588',
      '挂失': '银行卡挂失流程：\n1. 立即拨打客服热线 95588 进行口头挂失\n2. 携带身份证前往银行网点办理正式挂失\n3. 补办新卡（工本费 10 元）\n\n挂失后原卡即刻失效，请放心。',
      '限额': '转账限额说明：\n- 网上银行：单笔 5 万，日累计 20 万\n- 手机银行：单笔 5 万，日累计 20 万\n- ATM 转账：单笔 5 万，日累计 5 万\n\n如需提高限额，请前往网点办理。',
      '信用卡': '信用卡申请方式：\n1. 网上银行在线申请\n2. 手机银行 APP 申请\n3. 银行网点柜台申请\n4. 拨打客服热线申请\n\n审批时间：3-5 个工作日',
      '利率': '当前定期存款利率：\n- 3 个月：1.35%\n- 6 个月：1.55%\n- 1 年期：1.75%\n- 2 年期：2.25%\n- 3 年期：2.75%\n\n* 利率可能调整，以实际为准',
      '网银': '开通网上银行：\n1. 携带身份证和银行卡前往网点\n2. 柜台工作人员协助开通\n3. 设置登录密码和 U 盾\n4. 下载安全控件后即可使用',
      '手机': '修改手机号码：\n1. 携带身份证和银行卡\n2. 前往银行网点柜台办理\n3. 填写变更申请表\n4. 验证身份后即时生效',
      '理财': '我行理财产品：\n1. 稳健型：年化 3%-4%\n2. 平衡型：年化 4%-5%\n3. 进取型：年化 5%-7%\n\n具体产品请在"理财服务"页面查看。',
      'default': '感谢您的咨询！为了给您提供更准确的帮助，您可以：\n1. 拨打客服热线 95588\n2. 前往附近银行网点\n3. 使用网上银行或手机银行\n\n请问还有其他问题吗？'
    }

    function getCurrentTime() {
      const now = new Date()
      return now.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    }

    function scrollToBottom() {
      nextTick(() => {
        if (chatMessages.value) {
          chatMessages.value.scrollTop = chatMessages.value.scrollHeight
        }
      })
    }

    function getAutoReply(message) {
      for (const [keyword, reply] of Object.entries(autoReplies)) {
        if (keyword !== 'default' && message.includes(keyword)) {
          return reply
        }
      }
      return autoReplies.default
    }

    function sendMessage() {
      const text = inputMessage.value.trim()
      if (!text || isTyping.value) return

      // 添加用户消息
      messages.value.push({
        type: 'user',
        text: text,
        time: getCurrentTime()
      })

      inputMessage.value = ''
      scrollToBottom()

      // 模拟机器人回复
      isTyping.value = true
      setTimeout(() => {
        const reply = getAutoReply(text)
        messages.value.push({
          type: 'bot',
          text: reply,
          time: getCurrentTime()
        })
        isTyping.value = false
        scrollToBottom()
      }, 1000 + Math.random() * 1000)
    }

    function sendQuickQuestion(question) {
      inputMessage.value = question
      sendMessage()
    }

    return {
      chatWindow,
      chatMessages,
      inputMessage,
      isTyping,
      messages,
      quickQuestions,
      faqs,
      sendMessage,
      sendQuickQuestion
    }
  }
}
</script>

<style scoped>
.chatbot {
  display: flex;
  gap: 20px;
  height: calc(100vh - 140px);
  max-width: 1400px;
}

.chatbot-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
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
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-bubble {
  background: rgba(255, 255, 255, 0.15);
  padding: 12px 16px;
  border-radius: 12px;
  border-top-left-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.message-bubble p {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.5;
  color: white;
}

.message.user .message-bubble {
  background: rgba(255, 255, 255, 0.25);
  color: white;
  border-radius: 12px;
  border-top-right-radius: 4px;
}

.message-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  padding: 0 4px;
}

.message.user .message-time {
  text-align: right;
}

/* 输入指示器 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
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
  gap: 8px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.quick-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
}

.quick-btn {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  border-radius: 16px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.quick-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

/* 输入区域 */
.chat-input-area {
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}

.input-wrapper {
  display: flex;
  gap: 12px;
}

.input-wrapper input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 24px;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  transition: all 0.3s;
}

.input-wrapper input:focus {
  outline: none;
  border-color: rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.15);
}

.input-wrapper input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.send-btn {
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 24px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s;
}

.send-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.3);
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
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow-y: auto;
  padding: 20px;
}

.sidebar-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.sidebar-header h3 {
  margin: 0;
  color: white;
  font-size: 16px;
  font-weight: 600;
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 24px;
}

.faq-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.faq-item:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.faq-icon {
  font-size: 18px;
}

.faq-question {
  font-size: 14px;
  color: white;
}

.contact-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.contact-icon {
  font-size: 20px;
}

.contact-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.contact-value {
  font-size: 14px;
  color: white;
  font-weight: 500;
}

.contact-info {
  margin-top: 24px;
}
</style>
