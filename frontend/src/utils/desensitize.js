/**
 * 脱敏工具类（前端实现）
 * 对敏感信息进行部分隐藏处理
 */

/**
 * 真实姓名脱敏
 * @param {string} realName 真实姓名
 * @returns {string} 脱敏后的姓名
 * - 长度=1：返回 "*"
 * - 长度=2：保留首字，如 "张*"
 * - 长度>=3：保留首尾，如 "张*三"
 */
export function maskRealName(realName) {
  if (!realName || !realName.trim()) {
    return realName
  }
  const name = realName.trim()
  const len = name.length
  if (len === 1) {
    return '*'
  }
  if (len === 2) {
    return name.charAt(0) + '*'
  }
  return name.charAt(0) + '*'.repeat(len - 2) + name.charAt(len - 1)
}

/**
 * 手机号脱敏
 * @param {string} phone 手机号
 * @returns {string} 脱敏后的手机号
 * - 11位手机号：显示前3后4，如 "138****1234"
 * - 其他情况：保留首1末1，中间掩码
 */
export function maskPhone(phone) {
  if (!phone || !phone.trim()) {
    return phone
  }
  const raw = phone.trim()
  // 只取数字
  const digits = raw.replace(/\D/g, '')
  if (digits.length === 11) {
    return digits.substring(0, 3) + '****' + digits.substring(7)
  }
  // 其他情况保留首尾
  if (raw.length <= 2) {
    return '*'.repeat(raw.length)
  }
  return raw.charAt(0) + '*'.repeat(raw.length - 2) + raw.charAt(raw.length - 1)
}

/**
 * 邮箱脱敏
 * @param {string} email 邮箱地址
 * @returns {string} 脱敏后的邮箱
 * - 例：alice@example.com -> a***e@example.com
 */
export function maskEmail(email) {
  if (!email || !email.trim()) {
    return email
  }
  const raw = email.trim()
  const at = raw.indexOf('@')
  if (at <= 0 || at === raw.length - 1) {
    // 非法邮箱，退化处理
    if (raw.length <= 2) {
      return '*'.repeat(raw.length)
    }
    return raw.charAt(0) + '*'.repeat(raw.length - 2) + raw.charAt(raw.length - 1)
  }
  const local = raw.substring(0, at)
  const domain = raw.substring(at)

  if (local.length <= 1) {
    return '*' + domain
  }
  if (local.length === 2) {
    return local.charAt(0) + '*' + domain
  }
  return local.charAt(0) + '***' + local.charAt(local.length - 1) + domain
}

/**
 * 脱敏对象，包含原始值和脱敏值
 */
export class DesensitizedValue {
  constructor(originalValue, maskedValue) {
    this.original = originalValue
    this.masked = maskedValue
  }

  /**
   * 获取当前显示值
   * @param {boolean} showOriginal 是否显示原始值
   * @returns {string}
   */
  getDisplayValue(showOriginal) {
    return showOriginal ? this.original : this.masked
  }
}

/**
 * 创建脱敏值对象
 * @param {string} value 原始值
 * @param {Function} maskFn 脱敏函数
 * @returns {DesensitizedValue}
 */
export function createDesensitizedValue(value, maskFn) {
  return new DesensitizedValue(value, maskFn(value))
}

/**
 * 银行卡号脱敏
 * @param {string} accountNumber 银行卡号
 * @returns {string} 脱敏后的卡号
 * - 例：6222021234567890 -> **** **** **** 7890
 */
export function maskAccountNumber(accountNumber) {
  if (!accountNumber || !accountNumber.trim()) {
    return accountNumber
  }
  const raw = accountNumber.trim().replace(/\s/g, '')
  if (raw.length <= 4) {
    return '*'.repeat(raw.length)
  }
  // 显示后4位
  const lastFour = raw.slice(-4)
  return `**** **** **** ${lastFour}`
}

/**
 * 余额脱敏（隐藏实际金额）
 * @param {number|string} balance 余额
 * @returns {string} 脱敏后的余额
 * - 例：125680.00 -> ***，***
 */
export function maskBalance(balance) {
  if (balance === null || balance === undefined || balance === '') {
    return '****'
  }
  // 将余额转换为字符串并隐藏
  const str = String(balance)
  // 返回星号，保持大致长度感
  return '***'
}
