import http from '@/http'

// API 服务层 - 对接后端真实接口

// 账户相关 API
export const accountApi = {
  /**
   * 获取当前用户的所有账户
   */
  getAccounts: async () => {
    return await http.get('/accounts')
  },

  /**
   * 获取账户详情
   */
  getAccountDetail: async (id) => {
    return await http.get(`/accounts/${id}`)
  },

  /**
   * 获取仪表盘统计数据
   */
  getDashboardStats: async () => {
    return await http.get('/accounts/dashboard/stats')
  },

  /**
   * 创建新账户
   * @param {object} data - 账户数据 {accountName, currency, dailyLimit}
   */
  createAccount: async (data) => {
    return await http.post('/accounts', data)
  },

  /**
   * 更新账户信息
   * @param {number} id - 账户 ID
   * @param {object} data - 更新数据 {accountName, dailyLimit}
   */
  updateAccount: async (id, data) => {
    return await http.put(`/accounts/${id}`, data)
  },

  /**
   * 更新账户状态（冻结/解冻）
   * @param {number} id - 账户 ID
   * @param {number} status - 状态: 0-冻结, 1-正常
   */
  updateStatus: async (id, status) => {
    return await http.put(`/accounts/${id}/status`, { status })
  }
}

// 交易记录相关 API
export const transactionApi = {
  /**
   * 获取交易记录
   * @param {object} params - 查询参数 {accountId, type, status, startDate, endDate, page, size, allAccounts}
   */
  getTransactions: async (params = {}) => {
    return await http.get('/transactions', { params })
  }
}

// 转账相关 API
export const transferApi = {
  /**
   * 获取转账记录
   * @param {object} params - 查询参数 {accountId, page, size}
   */
  getTransfers: async (params = {}) => {
    return await http.get('/transfers', { params })
  },

  /**
   * 发起转账
   * @param {object} data - 转账数据 {fromAccountId, toAccountNumber, toAccountName, toBankName, amount, remark, payPassword}
   */
  executeTransfer: async (data) => {
    return await http.post('/transfers', data)
  }
}

// 用户相关 API（通过 user-service）
export const userApi = {
  /**
   * 获取当前用户信息
   */
  getUserInfo: async () => {
    return await http.get('/users/me')
  },

  /**
   * 更新用户资料
   * @param {object} data - 更新数据 {username, phone, realName}
   */
  updateProfile: async (data) => {
    return await http.put('/users/profile', data)
  },

  /**
   * 发送验证码
   * @param {string} type - 验证码类型：'register', 'reset-password'
   * @param {string} phone - 手机号（可选）
   * @param {string} email - 邮箱（可选）
   */
  sendCode: async (type, phone = null, email = null) => {
    const body = {}
    if (phone) body.phone = phone
    if (email) body.email = email
    return await http.post('/users/code', body, { params: { type } })
  },

  /**
   * 发送重置密码验证码（登录后）
   * 使用当前登录用户的邮箱发送验证码
   */
  sendResetPasswordCode: async () => {
    return await http.post('/users/code', {}, { params: { type: 'reset-password' } })
  },

  /**
   * 重置密码（需要验证码）
   * @param {object} data - 重置数据 {oldPassword, newPassword}
   * @param {string} verifyCode - 验证码
   */
  resetPassword: async (data, verifyCode) => {
    return await http.post('/users/reset-password', data, { params: { verifyCode } })
  },

  /**
   * 修改密码（登录后）
   * @param {object} data - 修改数据 {oldPassword, newPassword}
   */
  changePassword: async (data) => {
    return await http.post('/users/reset-password', data)
  },

  /**
   * 刷新 Token
   */
  refreshToken: async () => {
    return await http.post('/users/refresh', {})
  },

  /**
   * 设置支付密码
   * @param {string} payPassword - 6位数字支付密码
   */
  setPayPassword: async (payPassword) => {
    return await http.post('/users/pay-password', { payPassword })
  },

  /**
   * 修改支付密码
   * @param {string} oldPayPassword - 原支付密码
   * @param {string} payPassword - 新支付密码
   */
  updatePayPassword: async (oldPayPassword, payPassword) => {
    return await http.put('/users/pay-password', { oldPayPassword, payPassword })
  },

  /**
   * 验证支付密码
   * @param {string} payPassword - 支付密码
   */
  verifyPayPassword: async (payPassword) => {
    return await http.post('/users/pay-password/verify', { payPassword })
  },

  /**
   * 检查是否已设置支付密码
   */
  getPayPasswordStatus: async () => {
    return await http.get('/users/pay-password/status')
  },

  /**
   * 获取当前用户的角色列表
   * @returns {Promise} - 返回角色代码数组，如 ['USER', 'ADMIN']
   */
  getUserRoles: async () => {
    return await http.get('/users/roles')
  }
}

// 聊天相关 API（通过 chat-service）
export const chatApi = {
  /**
   * 获取用户的聊天会话列表
   */
  getSessions: async () => {
    return await http.get('/chat/session')
  },

  /**
   * 创建新的聊天会话
   */
  createSession: async () => {
    return await http.post('/chat/session')
  },

  /**
   * 删除聊天会话
   * @param {string} sessionId - 会话 UUID
   */
  deleteSession: async (sessionId) => {
    return await http.delete(`/chat/session/${sessionId}`)
  },

  /**
   * 重命名聊天会话
   * @param {string} sessionId - 会话 UUID
   * @param {string} title - 新标题
   */
  renameSession: async (sessionId, title) => {
    return await http.put(`/chat/session/${sessionId}`, { title })
  },

  /**
   * 获取指定会话的聊天记录
   * @param {string} sessionId - 会话 UUID
   */
  getMessages: async (sessionId) => {
    return await http.get(`/chat/message/${sessionId}`)
  },

  /**
   * 发送消息并获取 AI 流式回复
   * @param {object} data - 聊天数据 {sessionId, content}
   * @returns {ReadableStream} 流式响应
   */
  sendMessage: async (data) => {
    // 使用 fetch API 实现真正的流式响应（axios 不支持流式）
    const token = localStorage.getItem('token')
    const userInfo = localStorage.getItem('user-info')
    
    const response = await fetch('http://ipv6.maretu.top:8080/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/html',
        'Maretu': token,
        'user-info': userInfo
      },
      body: JSON.stringify(data)
    })
    
    if (!response.ok) {
      const error = await response.text()
      throw new Error(error || '发送消息失败')
    }
    
    // 返回原始响应对象，包含可读流
    return response
  }
}

// 管理员用户管理相关 API
export const adminApi = {
  /**
   * 获取用户列表（管理员）
   * @param {object} params - 查询参数 {keyword, role, page, size}
   * @returns {Promise} - 返回分页用户数据 {total, pages, list}
   */
  getUserList: async (params = {}) => {
    return await http.get('/users/list', { params })
  },

  /**
   * 切换用户启用/禁用状态（管理员）
   * @param {number} userId - 用户 ID
   * @returns {Promise} - 返回操作结果
   */
  toggleUserStatus: async (userId) => {
    return await http.put(`/users/status/${userId}`)
  },

  /**
   * 分配用户角色（管理员）
   * @param {number} userId - 用户 ID
   * @param {string[]} roles - 角色代码数组，如 ['USER', 'ADMIN']
   * @returns {Promise} - 返回操作结果
   */
  assignRoles: async (userId, roles) => {
    return await http.put(`/users/roles/${userId}`, { roles })
  }
}

// 知识库管理相关 API
export const knowledgeApi = {
  /**
   * 上传文档到知识库
   * 支持 PDF 和 TXT 文件
   * 注意：PDF 文件会按每一页进行切片（pagesPerDocument=1），
   *       务必使用 multipart/form-data 格式提交，参数名为 "files"，
   *       不要修改此提交格式，否则会导致后端知识库切片异常
   * @param {File[]} files - 文件数组
   * @param {Function} onProgress - 上传进度回调 (progressEvent) => {}
   * @returns {Promise} - 返回每个文件的入库结果
   */
  uploadDocuments: async (files, onProgress = null) => {
    const formData = new FormData()
    // 必须使用 "files" 作为参数名，与后端 @RequestParam("files") 对应
    files.forEach(file => {
      formData.append('files', file)
    })
    return await http.post('/chat/knowledge', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      // 文档上传+向量化可能耗时较长，设置 5 分钟超时
      timeout: 300000,
      // 上传进度回调
      ...(onProgress ? { onUploadProgress: onProgress } : {})
    })
  },

  /**
   * 清空知识库
   * 警告：此操作会删除所有已入库的向量数据，不可恢复
   * @returns {Promise}
   */
  clearKnowledge: async () => {
    return await http.delete('/chat/knowledge')
  }
}

export default {
  accountApi,
  transactionApi,
  transferApi,
  userApi,
  chatApi,
  knowledgeApi,
  adminApi
}