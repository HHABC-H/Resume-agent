<template>
  <div class="admin-container">
    <header class="header">
      <div class="logo">Resume Agent - 管理后台</div>
      <nav class="nav">
        <router-link to="/admin" class="nav-link">用户管理</router-link>
        <router-link to="/admin/stats" class="nav-link">数据统计</router-link>
        <span class="user-info">{{ username }}</span>
        <button @click="logout" class="btn btn-text">退出登录</button>
      </nav>
    </header>

    <main class="main-content">
      <div class="page-header">
        <h1>用户管理</h1>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="error" class="error-message">{{ error }}</div>
      <div v-else class="users-table-container">
        <table class="users-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>邮箱</th>
              <th>显示名称</th>
              <th>角色</th>
              <th>状态</th>
              <th>注册时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.id }}</td>
              <td>{{ user.username }}</td>
              <td>{{ user.email || '-' }}</td>
              <td>{{ user.displayName || '-' }}</td>
              <td>
                <span :class="['role-badge', user.role.toLowerCase()]">
                  {{ user.role === 'ADMIN' ? '管理员' : '用户' }}
                </span>
              </td>
              <td>
                <span :class="['status-badge', user.status === 1 ? 'active' : 'disabled']">
                  {{ user.status === 1 ? '正常' : '禁用' }}
                </span>
              </td>
              <td>{{ formatDate(user.createdAt) }}</td>
              <td class="actions">
                <button @click="editUser(user)" class="btn btn-sm btn-primary">编辑</button>
                <button 
                  v-if="user.status === 1" 
                  @click="toggleUserStatus(user.id, 0)" 
                  class="btn btn-sm btn-danger"
                >
                  禁用
                </button>
                <button 
                  v-else 
                  @click="toggleUserStatus(user.id, 1)" 
                  class="btn btn-sm btn-success"
                >
                  启用
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </main>

    <div v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
      <div class="modal">
        <h2>编辑用户</h2>
        <form @submit.prevent="saveUser">
          <div class="form-group">
            <label>用户名</label>
            <input type="text" v-model="editForm.username" disabled>
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input type="email" v-model="editForm.email">
          </div>
          <div class="form-group">
            <label>显示名称</label>
            <input type="text" v-model="editForm.displayName">
          </div>
          <div class="form-actions">
            <button type="button" @click="closeEditModal" class="btn btn-secondary">取消</button>
            <button type="submit" class="btn btn-primary">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const token = localStorage.getItem('token')
const username = localStorage.getItem('username')

const users = ref<any[]>([])
const loading = ref(false)
const error = ref('')
const showEditModal = ref(false)
const editForm = ref({
  id: null as number | null,
  username: '',
  email: '',
  displayName: ''
})

const loadUsers = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await axios.get('/api/admin/users', {
      headers: { Authorization: `Bearer ${token}` }
    })
    users.value = response.data
  } catch (err: any) {
    error.value = err.response?.data?.error || '加载用户列表失败'
  } finally {
    loading.value = false
  }
}

const editUser = (user: any) => {
  editForm.value = {
    id: user.id,
    username: user.username,
    email: user.email || '',
    displayName: user.displayName || ''
  }
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
  editForm.value = {
    id: null,
    username: '',
    email: '',
    displayName: ''
  }
}

const saveUser = async () => {
  if (!editForm.value.id) return
  
  try {
    await axios.put(`/api/admin/users/${editForm.value.id}`, {
      email: editForm.value.email,
      displayName: editForm.value.displayName
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    closeEditModal()
    loadUsers()
  } catch (err: any) {
    error.value = err.response?.data?.error || '保存失败'
  }
}

const toggleUserStatus = async (userId: number, status: number) => {
  try {
    await axios.put(`/api/admin/users/${userId}/status`, { status }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    loadUsers()
  } catch (err: any) {
    error.value = err.response?.data?.error || '更新状态失败'
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userId')
  localStorage.removeItem('role')
  router.push('/')
}

onMounted(() => {
  if (!token) {
    router.push('/login')
    return
  }
  const role = localStorage.getItem('role')
  if (role !== 'ADMIN') {
    router.push('/')
    return
  }
  loadUsers()
})
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 1.25rem;
  font-weight: bold;
  color: #007bff;
}

.nav {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.nav-link {
  color: #495057;
  text-decoration: none;
  font-weight: 500;
}

.nav-link:hover {
  color: #007bff;
}

.user-info {
  color: #495057;
  font-weight: 500;
}

.main-content {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 2rem;
}

.page-header h1 {
  color: #333;
}

.loading, .error-message {
  padding: 2rem;
  text-align: center;
}

.error-message {
  background-color: #f8d7da;
  color: #721c24;
  border-radius: 4px;
}

.users-table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th,
.users-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.users-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #495057;
}

.users-table tbody tr:hover {
  background-color: #f8f9fa;
}

.role-badge,
.status-badge {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.875rem;
  font-weight: 500;
}

.role-badge.admin {
  background-color: #d4edda;
  color: #155724;
}

.role-badge.user {
  background-color: #cce5ff;
  color: #004085;
}

.status-badge.active {
  background-color: #d4edda;
  color: #155724;
}

.status-badge.disabled {
  background-color: #f8d7da;
  color: #721c24;
}

.actions {
  display: flex;
  gap: 0.5rem;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  font-size: 0.875rem;
  cursor: pointer;
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.8125rem;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover {
  background-color: #0069d9;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #5a6268;
}

.btn-danger {
  background-color: #dc3545;
  color: white;
}

.btn-danger:hover {
  background-color: #c82333;
}

.btn-success {
  background-color: #28a745;
  color: white;
}

.btn-success:hover {
  background-color: #218838;
}

.btn-text {
  background: none;
  color: #007bff;
  padding: 0.5rem;
}

.btn-text:hover {
  text-decoration: underline;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.modal h2 {
  margin-bottom: 1.5rem;
  color: #333;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group input:disabled {
  background-color: #e9ecef;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
}
</style>