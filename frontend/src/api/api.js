// API 服务层 - 目前使用模拟数据，后续可替换为真实的 HTTP 请求

// 模拟用户数据
const mockUser = {
  id: 1,
  username: 'zhangsan',
  email: 'zhangsan@example.com',
  phone: '138****8888',
  avatar: 'https://ui-avatars.com/api/?name=Zhang+San&background=409EFF&color=fff'
}

// 模拟账户数据
const mockAccounts = [
  { id: 1, accountNumber: '6222 **** **** 1234', type: '储蓄卡', balance: 50000.00, currency: 'CNY', status: '正常' },
  { id: 2, accountNumber: '6222 **** **** 5678', type: '信用卡', balance: -5000.00, currency: 'CNY', status: '正常' },
  { id: 3, accountNumber: '6222 **** **** 9012', type: '储蓄卡', balance: 120000.00, currency: 'CNY', status: '冻结' }
]

// 模拟交易记录
const mockTransactions = [
  { id: 1, date: '2024-01-15', type: '收入', amount: 15000.00, description: '工资发放', status: '完成' },
  { id: 2, date: '2024-01-14', type: '支出', amount: -500.00, description: '网购消费', status: '完成' },
  { id: 3, date: '2024-01-13', type: '支出', amount: -2000.00, description: '转账给李四', status: '完成' },
  { id: 4, date: '2024-01-12', type: '收入', amount: 3000.00, description: '理财收益', status: '完成' },
  { id: 5, date: '2024-01-11', type: '支出', amount: -150.00, description: '水电费缴纳', status: '处理中' }
]

// 模拟聊天消息
const mockChatMessages = [
  { id: 1, sender: 'bot', content: '您好！我是银行智能客服助手，请问有什么可以帮您？', time: '09:00' },
  { id: 2, sender: 'user', content: '我想查询我的账户余额', time: '09:01' },
  { id: 3, sender: 'bot', content: '您好，您当前储蓄卡账户余额为 50,000.00 元。请问还有其他需要帮助的吗？', time: '09:01' }
]

// 模拟常见问题
const mockFAQs = [
  { id: 1, question: '如何修改登录密码？', answer: '您可以在个人中心页面点击"修改密码"，按照提示完成密码修改。' },
  { id: 2, question: '转账限额是多少？', answer: '单笔转账限额为 5 万元，日累计限额为 20 万元。' },
  { id: 3, question: '如何申请信用卡？', answer: '您可以在"信用卡服务"页面在线申请，审核通过后我们将为您寄送卡片。' },
  { id: 4, question: '理财产品有哪些？', answer: '我们提供多种理财产品，包括定期理财、活期理财、基金等，您可以在"理财服务"页面查看详情。' }
]

// API 方法
export const api = {
  // 用户相关
  getUserInfo: () => {
    return Promise.resolve({ code: 200, data: mockUser, message: 'success' })
  },
  
  updateUserInfo: (data) => {
    return Promise.resolve({ code: 200, data: { ...mockUser, ...data }, message: '更新成功' })
  },
  
  // 账户相关
  getAccounts: () => {
    return Promise.resolve({ code: 200, data: mockAccounts, message: 'success' })
  },
  
  getAccountDetail: (id) => {
    const account = mockAccounts.find(a => a.id === id)
    return Promise.resolve({ code: 200, data: account || {}, message: 'success' })
  },
  
  // 交易相关
  getTransactions: (params = {}) => {
    let result = [...mockTransactions]
    if (params.type) {
      result = result.filter(t => t.type === params.type)
    }
    if (params.status) {
      result = result.filter(t => t.status === params.status)
    }
    return Promise.resolve({ code: 200, data: result, message: 'success' })
  },
  
  // 转账相关
  transfer: (data) => {
    return Promise.resolve({ code: 200, data: { transactionId: Date.now() }, message: '转账成功' })
  },
  
  // 聊天机器人相关
  getChatMessages: () => {
    return Promise.resolve({ code: 200, data: mockChatMessages, message: 'success' })
  },
  
  sendMessage: (content) => {
    const newMessage = {
      id: mockChatMessages.length + 1,
      sender: 'user',
      content: content,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
    mockChatMessages.push(newMessage)
    
    // 模拟机器人回复
    setTimeout(() => {
      const botReply = {
        id: mockChatMessages.length + 1,
        sender: 'bot',
        content: getBotReply(content),
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      }
      mockChatMessages.push(botReply)
    }, 1000)
    
    return Promise.resolve({ code: 200, data: newMessage, message: 'success' })
  },
  
  getFAQs: () => {
    return Promise.resolve({ code: 200, data: mockFAQs, message: 'success' })
  },
  
  // 仪表盘统计
  getDashboardStats: () => {
    const totalBalance = mockAccounts.reduce((sum, acc) => sum + acc.balance, 0)
    const income = mockTransactions.filter(t => t.type === '收入').reduce((sum, t) => sum + t.amount, 0)
    const expense = mockTransactions.filter(t => t.type === '支出').reduce((sum, t) => sum + Math.abs(t.amount), 0)
    
    return Promise.resolve({
      code: 200,
      data: {
        totalBalance,
        income,
        expense,
        accountCount: mockAccounts.length,
        transactionCount: mockTransactions.length
      },
      message: 'success'
    })
  }
}

// 简单的机器人回复逻辑
function getBotReply(content) {
  const lowerContent = content.toLowerCase()
  if (lowerContent.includes('余额') || lowerContent.includes('钱')) {
    return '您当前储蓄卡账户余额为 50,000.00 元。'
  } else if (lowerContent.includes('密码')) {
    return '您可以在个人中心页面点击"修改密码"，按照提示完成密码修改。'
  } else if (lowerContent.includes('转账')) {
    return '您可以通过"转账服务"页面进行转账操作，单笔限额 5 万元。'
  } else if (lowerContent.includes('信用卡')) {
    return '您可以在"信用卡服务"页面在线申请信用卡。'
  } else if (lowerContent.includes('理财')) {
    return '我们提供多种理财产品，预期年化收益率 3%-5% 不等。'
  } else {
    return '抱歉，我没有理解您的问题。您可以拨打客服热线 95588 获取人工服务。'
  }
}

export default api
