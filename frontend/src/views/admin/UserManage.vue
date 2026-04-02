<template>
  <div class="user-manage">
    <div class="page-header">
      <h2>👥 用户管理</h2>
      <p class="page-desc">管理系统用户，查看用户信息、分配角色等</p>
    </div>

    <!-- 操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <input
          v-model="searchKeyword"
          type="text"
          class="search-input"
          placeholder="搜索用户名或邮箱..."
          @keyup.enter="handleSearch"
        />
        <button class="btn btn-primary" @click="handleSearch">搜索</button>
        <select v-model="filterRole" class="filter-select" @change="handleFilter">
          <option value="">全部角色</option>
          <option value="USER">普通用户</option>
          <option value="ADMIN">管理员</option>
          <option value="CUSTOMER_SERVICE">客服</option>
        </select>
      </div>
      <div class="toolbar-right">
        <div class="user-count">
          共 <strong>{{ userList.length }}</strong> 位用户
        </div>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="user-list">
      <div class="list-header">
        <span class="col-id">ID</span>
        <span class="col-username">用户名</span>
        <span class="col-email">邮箱</span>
        <span class="col-roles">角色</span>
        <span class="col-status">状态</span>
        <span class="col-time">注册时间</span>
        <span class="col-action">操作</span>
      </div>

      <div v-if="loading" class="loading-state">
        <p>加载中...</p>
      </div>

      <div v-else-if="filteredUsers.length === 0" class="empty-state">
        <p>📭 暂无匹配的用户</p>
      </div>

      <div v-else>
        <div
          class="list-item"
          v-for="user in filteredUsers"
          :key="user.id"
        >
          <span class="col-id">{{ user.id }}</span>
          <span class="col-username">
            <span class="user-avatar">{{ user.username.charAt(0).toUpperCase() }}</span>
            {{ user.username }}
          </span>
          <span class="col-email">{{ user.email }}</span>
          <span class="col-roles">
            <span
              class="role-tag"
              v-for="role in user.roles"
              :key="role"
              :class="getRoleClass(role)"
            >
              {{ getRoleName(role) }}
            </span>
          </span>
          <span class="col-status">
            <span class="status-dot" :class="user.active ? 'online' : 'offline'"></span>
            {{ user.active ? '正常' : '禁用' }}
          </span>
          <span class="col-time">{{ user.createdAt }}</span>
          <span class="col-action">
            <button class="btn-sm btn-role" @click="showRoleDialog(user)">角色</button>
            <button class="btn-sm btn-toggle" @click="handleToggleStatus(user)">
              {{ user.active ? '禁用' : '启用' }}
            </button>
          </span>
        </div>
      </div>
    </div>

    <!-- 角色分配弹窗 -->
    <div class="modal-overlay" v-if="roleDialogVisible" @click.self="closeRoleDialog">
      <div class="modal">
        <div class="modal-header">
          <h3>分配角色 - {{ selectedUser?.username }}</h3>
          <button class="modal-close" @click="closeRoleDialog">✕</button>
        </div>
        <div class="modal-body">
          <p class="modal-hint">选择要分配给该用户的角色（可多选）：</p>
          <div class="role-options">
            <label
              class="role-option"
              v-for="role in allRoles"
              :key="role.code"
              :class="{ selected: selectedRoles.includes(role.code) }"
            >
              <input
                type="checkbox"
                :value="role.code"
                v-model="selectedRoles"
                class="role-checkbox"
              />
              <div class="role-option-content">
                <span class="role-option-icon">{{ role.icon }}</span>
                <div>
                  <div class="role-option-name">{{ role.name }}</div>
                  <div class="role-option-desc">{{ role.description }}</div>
                </div>
              </div>
            </label>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-cancel" @click="closeRoleDialog">取消</button>
          <button class="btn btn-primary" @click="handleSaveRoles">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, getCurrentInstance } from 'vue'

export default {
  name: 'UserManage',
  setup() {
    const { proxy } = getCurrentInstance()

    const searchKeyword = ref('')
    const filterRole = ref('')
    const loading = ref(false)
    const roleDialogVisible = ref(false)
    const selectedUser = ref(null)
    const selectedRoles = ref([])

    // 所有可用角色
    const allRoles = ref([
      { code: 'USER', name: '普通用户', icon: '👤', description: '基本的银行服务功能' },
      { code: 'ADMIN', name: '管理员', icon: '🛡️', description: '系统管理、用户管理、知识库管理' },
      { code: 'CUSTOMER_SERVICE', name: '客服', icon: '🎧', description: '处理客户咨询和投诉' }
    ])

    // 模拟用户数据（接口待实现）
    const userList = ref([
      { id: 1, username: 'admin', email: 'admin@bank.com', roles: ['USER', 'ADMIN'], active: true, createdAt: '2024-01-01 08:00' },
      { id: 2, username: 'zhangsan', email: 'zhangsan@example.com', roles: ['USER'], active: true, createdAt: '2024-01-05 10:30' },
      { id: 3, username: 'lisi', email: 'lisi@example.com', roles: ['USER'], active: true, createdAt: '2024-01-08 14:20' },
      { id: 4, username: 'wangwu', email: 'wangwu@example.com', roles: ['USER', 'CUSTOMER_SERVICE'], active: true, createdAt: '2024-01-10 09:15' },
      { id: 5, username: 'zhaoliu', email: 'zhaoliu@example.com', roles: ['USER'], active: false, createdAt: '2024-01-12 16:45' },
    ])

    // 过滤后的用户列表
    const filteredUsers = computed(() => {
      let list = userList.value
      if (searchKeyword.value) {
        const keyword = searchKeyword.value.toLowerCase()
        list = list.filter(u =>
          u.username.toLowerCase().includes(keyword) ||
          u.email.toLowerCase().includes(keyword)
        )
      }
      if (filterRole.value) {
        list = list.filter(u => u.roles.includes(filterRole.value))
      }
      return list
    })

    const getRoleName = (code) => {
      const map = { USER: '用户', ADMIN: '管理员', CUSTOMER_SERVICE: '客服' }
      return map[code] || code
    }

    const getRoleClass = (code) => {
      const map = { USER: 'role-user', ADMIN: 'role-admin', CUSTOMER_SERVICE: 'role-cs' }
      return map[code] || ''
    }

    const handleSearch = () => {
      // TODO: 调用搜索接口
      proxy.$message.info('搜索功能接口待实现，当前为前端过滤')
    }

    const handleFilter = () => {
      // 前端过滤，无需调用接口
    }

    const handleToggleStatus = (user) => {
      // TODO: 调用切换状态接口
      user.active = !user.active
      proxy.$message.success(`已${user.active ? '启用' : '禁用'}用户 ${user.username}`)
    }

    const showRoleDialog = (user) => {
      selectedUser.value = user
      selectedRoles.value = [...user.roles]
      roleDialogVisible.value = true
    }

    const closeRoleDialog = () => {
      roleDialogVisible.value = false
      selectedUser.value = null
    }

    const handleSaveRoles = () => {
      if (selectedRoles.value.length === 0) {
        proxy.$message.warning('至少需要分配一个角色')
        return
      }
      // TODO: 调用角色分配接口
      if (selectedUser.value) {
        selectedUser.value.roles = [...selectedRoles.value]
        proxy.$message.success(`已更新 ${selectedUser.value.username} 的角色`)
      }
      closeRoleDialog()
    }

    return {
      searchKeyword,
      filterRole,
      loading,
      userList,
      filteredUsers,
      allRoles,
      roleDialogVisible,
      selectedUser,
      selectedRoles,
      getRoleName,
      getRoleClass,
      handleSearch,
      handleFilter,
      handleToggleStatus,
      showRoleDialog,
      closeRoleDialog,
      handleSaveRoles
    }
  }
}
</script>

<style scoped>
.user-manage {
  max-width: var(--container-max-width-lg);
  margin: 0 auto;
}

.page-header {
  margin-bottom: var(--spacing-3xl);
}

.page-header h2 {
  font-size: var(--font-size-5xl);
  color: var(--color-white);
  margin: 0 0 var(--spacing-sm) 0;
}

.page-desc {
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-lg);
  margin: 0;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-2xl);
  gap: var(--spacing-lg);
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
}

.search-input {
  padding: var(--spacing-md) var(--spacing-xl);
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  color: var(--color-white);
  font-size: var(--font-size-lg);
  width: 260px;
  outline: none;
  transition: all var(--transition-normal);
}

.search-input::placeholder {
  color: var(--text-on-gradient-muted);
}

.search-input:focus {
  border-color: var(--glass-border-active);
  background: var(--glass-bg-hover);
}

.filter-select {
  padding: var(--spacing-md) var(--spacing-xl);
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  color: var(--color-white);
  font-size: var(--font-size-lg);
  outline: none;
  cursor: pointer;
}

.filter-select option {
  background: #1e293b;
  color: var(--color-white);
}

.user-count {
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-lg);
}

.user-count strong {
  color: var(--color-white);
}

/* 按钮 */
.btn {
  padding: var(--spacing-md) var(--spacing-xl);
  border: none;
  border-radius: var(--radius-xl);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all var(--transition-normal);
  white-space: nowrap;
}

.btn-primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: var(--color-white);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(59, 130, 246, 0.4);
}

.btn-cancel {
  background: var(--glass-bg);
  color: var(--color-white);
  border: 1px solid var(--glass-border);
}

.btn-cancel:hover {
  background: var(--glass-bg-hover);
}

/* 用户列表 */
.user-list {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-3xl);
  border: 1px solid var(--glass-border);
  overflow: hidden;
}

.list-header {
  display: grid;
  grid-template-columns: 50px 150px 1fr 180px 80px 150px 140px;
  padding: var(--spacing-lg) var(--spacing-2xl);
  background: var(--glass-bg-light);
  border-bottom: 1px solid var(--glass-border);
  font-weight: var(--font-weight-semibold);
  color: var(--text-on-gradient);
  font-size: var(--font-size-md);
}

.list-item {
  display: grid;
  grid-template-columns: 50px 150px 1fr 180px 80px 150px 140px;
  padding: var(--spacing-lg) var(--spacing-2xl);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  align-items: center;
  color: var(--color-white);
  font-size: var(--font-size-md);
  transition: all var(--transition-normal);
}

.list-item:hover {
  background: var(--glass-bg-light);
}

.list-item:last-child {
  border-bottom: none;
}

/* 用户头像 */
.col-username {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  flex-shrink: 0;
}

.col-email {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-on-gradient-muted);
}

/* 角色标签 */
.col-roles {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.role-tag {
  padding: 2px var(--spacing-md);
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
}

.role-user {
  background: rgba(59, 130, 246, 0.3);
  color: #93c5fd;
}

.role-admin {
  background: rgba(239, 68, 68, 0.3);
  color: #fca5a5;
}

.role-cs {
  background: rgba(16, 185, 129, 0.3);
  color: #6ee7b7;
}

/* 状态 */
.col-status {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
}

.status-dot.online {
  background: #10b981;
  box-shadow: 0 0 6px rgba(16, 185, 129, 0.6);
}

.status-dot.offline {
  background: #6b7280;
}

.col-action {
  display: flex;
  gap: var(--spacing-sm);
}

.btn-sm {
  padding: var(--spacing-xs) var(--spacing-md);
  border: none;
  border-radius: var(--radius-lg);
  font-size: var(--font-size-xs);
  cursor: pointer;
  transition: all var(--transition-normal);
  color: var(--color-white);
}

.btn-role {
  background: rgba(99, 102, 241, 0.4);
}

.btn-role:hover {
  background: rgba(99, 102, 241, 0.6);
}

.btn-toggle {
  background: rgba(245, 158, 11, 0.4);
}

.btn-toggle:hover {
  background: rgba(245, 158, 11, 0.6);
}

/* 空状态和加载状态 */
.empty-state, .loading-state {
  text-align: center;
  padding: var(--spacing-4xl);
  color: var(--text-on-gradient-muted);
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal {
  background: linear-gradient(135deg, #1e293b, #0f172a);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-3xl);
  width: 500px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-2xl);
  border-bottom: 1px solid var(--glass-border);
}

.modal-header h3 {
  margin: 0;
  color: var(--color-white);
  font-size: var(--font-size-3xl);
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-2xl);
  cursor: pointer;
  padding: var(--spacing-sm);
  transition: color var(--transition-normal);
}

.modal-close:hover {
  color: var(--color-white);
}

.modal-body {
  padding: var(--spacing-2xl);
}

.modal-hint {
  color: var(--text-on-gradient-muted);
  margin: 0 0 var(--spacing-xl) 0;
  font-size: var(--font-size-lg);
}

.role-options {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.role-option {
  display: flex;
  align-items: center;
  padding: var(--spacing-lg) var(--spacing-xl);
  background: var(--glass-bg);
  border: 2px solid var(--glass-border);
  border-radius: var(--radius-2xl);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.role-option:hover {
  background: var(--glass-bg-hover);
  border-color: var(--glass-border-hover);
}

.role-option.selected {
  border-color: #6366f1;
  background: rgba(99, 102, 241, 0.15);
}

.role-checkbox {
  display: none;
}

.role-option-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.role-option-icon {
  font-size: var(--font-size-4xl);
}

.role-option-name {
  color: var(--color-white);
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-lg);
  margin-bottom: 2px;
}

.role-option-desc {
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-sm);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
  padding: var(--spacing-2xl);
  border-top: 1px solid var(--glass-border);
}

/* 响应式 */
@media (max-width: 992px) {
  .list-header, .list-item {
    grid-template-columns: 40px 120px 1fr 150px 70px 120px 120px;
    font-size: var(--font-size-sm);
  }
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-left {
    flex-wrap: wrap;
  }

  .search-input {
    width: 100%;
  }

  .list-header {
    display: none;
  }

  .list-item {
    display: flex;
    flex-wrap: wrap;
    gap: var(--spacing-sm);
    padding: var(--spacing-lg);
  }

  .col-id, .col-time {
    display: none;
  }
}
</style>
