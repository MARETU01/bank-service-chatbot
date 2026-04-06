<template>
  <div class="user-manage">
    <div class="page-header">
      <h2>👥 User Management</h2>
      <p class="page-desc">Manage system users, view user information, assign roles, etc.</p>
    </div>

    <!-- Toolbar -->
    <div class="toolbar">
      <div class="toolbar-left">
        <input
          v-model="searchKeyword"
          type="text"
          class="search-input"
          placeholder="Search by username or email..."
          @keyup.enter="handleSearch"
        />
        <button class="btn btn-primary" @click="handleSearch">Search</button>
        <select v-model="filterRole" class="filter-select" @change="handleFilter">
          <option value="">All Roles</option>
          <option value="USER">User</option>
          <option value="ADMIN">Admin</option>
          <option value="CUSTOMER_SERVICE">Customer Service</option>
        </select>
      </div>
      <div class="toolbar-right">
        <div class="user-count">
          Total <strong>{{ total }}</strong> user(s)
        </div>
      </div>
    </div>

    <!-- User List -->
    <div class="user-list">
      <div class="list-header">
        <span class="col-id">ID</span>
        <span class="col-username">Username</span>
        <span class="col-email">Email</span>
        <span class="col-roles">Roles</span>
        <span class="col-status">Status</span>
        <span class="col-time">Registration Date</span>
        <span class="col-action">Actions</span>
      </div>

      <div v-if="loading" class="loading-state">
        <p>Loading...</p>
      </div>

      <div v-else-if="userList.length === 0" class="empty-state">
        <p>📭 No matching users found</p>
      </div>

      <div v-else>
        <div
          class="list-item"
          v-for="user in userList"
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
              v-for="role in sortRoles(user.roles)"
              :key="role"
              :class="getRoleClass(role)"
            >
              {{ getRoleName(role) }}
            </span>
          </span>
          <span class="col-status">
            <span class="status-dot" :class="user.active ? 'online' : 'offline'"></span>
            {{ user.active ? 'Active' : 'Disabled' }}
          </span>
          <span class="col-time">{{ formatTime(user.createdAt) }}</span>
          <span class="col-action">
            <button class="btn-sm btn-role" @click="showRoleDialog(user)">Roles</button>
            <button class="btn-sm btn-toggle" @click="handleToggleStatus(user)">
              {{ user.active ? 'Disable' : 'Enable' }}
            </button>
          </span>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="pagination" v-if="totalPage > 1">
      <button
        class="page-btn"
        :disabled="currentPage <= 1"
        @click="changePage(currentPage - 1)"
      >Previous</button>
      <button
        class="page-btn"
        v-for="p in totalPage"
        :key="p"
        :class="{ active: p === currentPage }"
        @click="changePage(p)"
      >{{ p }}</button>
      <button
        class="page-btn"
        :disabled="currentPage >= totalPage"
        @click="changePage(currentPage + 1)"
      >Next</button>
    </div>

    <!-- Role Assignment Modal -->
    <div class="modal-overlay" v-if="roleDialogVisible" @click.self="closeRoleDialog">
      <div class="modal">
        <div class="modal-header">
          <h3>Assign Roles - {{ selectedUser?.username }}</h3>
          <button class="modal-close" @click="closeRoleDialog">✕</button>
        </div>
        <div class="modal-body">
          <p class="modal-hint">Select roles to assign to this user (multiple selection allowed):</p>
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
          <button class="btn btn-cancel" @click="closeRoleDialog">Cancel</button>
          <button class="btn btn-primary" @click="handleSaveRoles">Save</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, getCurrentInstance } from 'vue'
import { adminApi } from '@/api/api'

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

    // Pagination related
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const totalPage = ref(0)

    // User list (from backend)
    const userList = ref([])

    // Available roles
    const allRoles = ref([
      { code: 'USER', name: 'User', icon: '👤', description: 'Basic banking services' },
      { code: 'ADMIN', name: 'Admin', icon: '🛡️', description: 'System management, user management, knowledge base management' },
      { code: 'CUSTOMER_SERVICE', name: 'Customer Service', icon: '🎧', description: 'Handle customer inquiries and complaints' }
    ])

    /**
     * Load user list
     */
    const fetchUserList = async () => {
      loading.value = true
      try {
        const res = await adminApi.getUserList({
          keyword: searchKeyword.value || undefined,
          role: filterRole.value || undefined,
          page: currentPage.value,
          size: pageSize.value
        })
        const { code, data, message } = res.data
        if (code === 1) {
          userList.value = data.data || []
          total.value = data.total || 0
          totalPage.value = data.totalPage || 0
        } else {
          proxy.$message.error(message || 'Failed to get user list')
        }
      } catch (e) {
        proxy.$message.error('Failed to get user list: ' + (e.message || 'Network error'))
      } finally {
        loading.value = false
      }
    }

    // Fetch data on page load
    onMounted(() => {
      fetchUserList()
    })

    const getRoleName = (code) => {
      const map = { USER: 'User', ADMIN: 'Admin', CUSTOMER_SERVICE: 'CS' }
      return map[code] || code
    }

    const getRoleClass = (code) => {
      const map = { USER: 'role-user', ADMIN: 'role-admin', CUSTOMER_SERVICE: 'role-cs' }
      return map[code] || ''
    }

    /**
     * Role sorting priority: USER > ADMIN > CUSTOMER_SERVICE
     * @param {string[]} roles - Role array
     * @returns {string[]} Sorted role array
     */
    const sortRoles = (roles) => {
      if (!roles || !Array.isArray(roles)) return []
      const priority = { USER: 0, ADMIN: 1, CUSTOMER_SERVICE: 2 }
      return [...roles].sort((a, b) => {
        const priorityA = priority[a] !== undefined ? priority[a] : 999
        const priorityB = priority[b] !== undefined ? priority[b] : 999
        return priorityA - priorityB
      })
    }

    /**
     * Format time
     * @param {string} timeStr - ISO format time string
     * @returns {string} Formatted time string
     */
    const formatTime = (timeStr) => {
      if (!timeStr) return '-'
      try {
        const date = new Date(timeStr)
        const year = date.getFullYear()
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        const seconds = String(date.getSeconds()).padStart(2, '0')
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
      } catch (e) {
        return timeStr
      }
    }

    /**
     * Search - Reset to first page and reload
     */
    const handleSearch = () => {
      currentPage.value = 1
      fetchUserList()
    }

    /**
     * Role filter - Reset to first page and reload
     */
    const handleFilter = () => {
      currentPage.value = 1
      fetchUserList()
    }

    /**
     * Change page
     */
    const changePage = (page) => {
      if (page < 1 || page > totalPage.value) return
      currentPage.value = page
      fetchUserList()
    }

    /**
     * Toggle user enable/disable status
     */
    const handleToggleStatus = async (user) => {
      try {
        const res = await adminApi.toggleUserStatus(user.id)
        const { code, message } = res.data
        if (code === 1) {
          user.active = !user.active
          proxy.$message.success(`${user.active ? 'Enabled' : 'Disabled'} user ${user.username}`)
        } else {
          proxy.$message.error(message || 'Operation failed')
        }
      } catch (e) {
        proxy.$message.error('Operation failed: ' + (e.message || 'Network error'))
      }
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

    /**
     * Save role assignment
     */
    const handleSaveRoles = async () => {
      if (selectedRoles.value.length === 0) {
        proxy.$message.warning('At least one role must be assigned')
        return
      }
      if (!selectedUser.value) return
      try {
        const res = await adminApi.assignRoles(selectedUser.value.id, selectedRoles.value)
        const { code, message } = res.data
        if (code === 1) {
          selectedUser.value.roles = [...selectedRoles.value]
          proxy.$message.success(`Updated roles for ${selectedUser.value.username}`)
          closeRoleDialog()
        } else {
          proxy.$message.error(message || 'Failed to assign roles')
        }
      } catch (e) {
        proxy.$message.error('Failed to assign roles: ' + (e.message || 'Network error'))
      }
    }

    return {
      searchKeyword,
      filterRole,
      loading,
      userList,
      allRoles,
      roleDialogVisible,
      selectedUser,
      selectedRoles,
      currentPage,
      total,
      totalPage,
      getRoleName,
      getRoleClass,
      sortRoles,
      formatTime,
      handleSearch,
      handleFilter,
      changePage,
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

/* Toolbar */
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

/* Buttons */
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

/* User List */
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

/* User Avatar */
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

/* Role Tags */
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

/* Status */
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

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-2xl) 0;
}

.page-btn {
  padding: var(--spacing-sm) var(--spacing-lg);
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  color: var(--color-white);
  font-size: var(--font-size-md);
  cursor: pointer;
  transition: all var(--transition-normal);
  min-width: 36px;
  text-align: center;
}

.page-btn:hover:not(:disabled) {
  background: var(--glass-bg-hover);
  border-color: var(--glass-border-hover);
}

.page-btn.active {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border-color: #3b82f6;
  font-weight: var(--font-weight-semibold);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
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

/* Empty State and Loading State */
.empty-state, .loading-state {
  text-align: center;
  padding: var(--spacing-4xl);
  color: var(--text-on-gradient-muted);
}

/* Modal */
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

/* Responsive */
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
