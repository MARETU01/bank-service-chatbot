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
  }
}

// 交易记录相关 API
export const transactionApi = {
  /**
   * 获取交易记录
   * @param {number} accountId - 账户 ID
   * @param {object} params - 查询参数 {type, status, startDate, endDate, page, size}
   */
  getTransactions: async (accountId, params = {}) => {
    return await http.get(`/accounts/${accountId}/transactions`, { params })
  }
}

// 转账相关 API
export const transferApi = {
  /**
   * 获取转账记录
   * @param {number} accountId - 账户 ID
   * @param {object} params - 查询参数 {page, size}
   */
  getTransfers: async (accountId, params = {}) => {
    return await http.get(`/accounts/${accountId}/transfers`, { params })
  },

  /**
   * 发起转账
   * @param {object} data - 转账数据 {fromAccountId, toAccountNumber, toAccountName, toBankName, amount, remark, payPassword}
   */
  executeTransfer: async (data) => {
    return await http.post('/accounts/transfer', data)
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
   * 更新用户信息
   */
  updateUserInfo: async (data) => {
    return await http.put('/users/me', data)
  }
}

export default {
  accountApi,
  transactionApi,
  transferApi,
  userApi
}
