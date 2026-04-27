<template>
  <div class="permissions-management">
    <div class="page-header">
      <h2>权限管理</h2>
      <p>管理角色和权限，分配管理员权限</p>
    </div>
    
    <div class="card">
      <div class="card-header">
        <h3>角色和权限</h3>
      </div>

      <div v-if="rolesLoading" class="loading">加载中...</div>
      <div v-else-if="rolesError" class="error-message">{{ rolesError }}</div>
      <div v-else class="roles-content">
        <div class="roles-list">
          <div v-for="role in roles" :key="role.name" class="role-item">
            <h4>{{ role.name === 'ADMIN' ? '管理员 (ADMIN)' : '普通用户 (USER)' }}</h4>
            <p>{{ role.description }}</p>
            <ul class="permissions-list">
              <li v-for="(perm, index) in role.permissions" :key="index">{{ perm }}</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
    
    <div class="card mt-4">
      <div class="card-header">
        <h3>分配管理员权限</h3>
      </div>
      
      <div v-if="usersLoading" class="loading">加载中...</div>
      <div v-else-if="usersError" class="error-message">{{ usersError }}</div>
      <div v-else class="assign-permissions">
        <div class="form-group">
          <label>选择用户</label>
          <select v-model="selectedUserId" class="form-control">
            <option value="">请选择用户</option>
            <option v-for="user in users" :key="user.id" :value="user.id">
              {{ user.username }} ({{ user.email || '无邮箱' }})
            </option>
          </select>
        </div>
        <div class="form-group">
          <label>分配角色</label>
          <select v-model="selectedRole" class="form-control">
            <option value="ADMIN">管理员 (ADMIN)</option>
            <option value="USER">普通用户 (USER)</option>
          </select>
        </div>
        <button 
          @click="assignRole" 
          class="btn btn-primary"
          :disabled="!selectedUserId"
        >
          分配角色
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

const roles = ref<any[]>([])
const rolesLoading = ref(false)
const rolesError = ref('')

const users = ref<any[]>([])
const usersLoading = ref(false)
const usersError = ref('')

const selectedUserId = ref('')
const selectedRole = ref('ADMIN')

const loadRoles = async () => {
  rolesLoading.value = true
  rolesError.value = ''
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/admin/roles', {
      headers: { Authorization: `Bearer ${token}` }
    })
    roles.value = response.data.roles || []
  } catch (err: any) {
    rolesError.value = err.response?.data?.error || '加载角色失败: ' + err.message
  } finally {
    rolesLoading.value = false
  }
}

const loadUsers = async () => {
  usersLoading.value = true
  usersError.value = ''
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/admin/users', {
      headers: { Authorization: `Bearer ${token}` }
    })
    users.value = response.data || []
  } catch (err: any) {
    usersError.value = err.response?.data?.error || '加载用户列表失败: ' + err.message
  } finally {
    usersLoading.value = false
  }
}

const assignRole = async () => {
  if (!selectedUserId.value) return
  
  try {
    const token = localStorage.getItem('token')
    await axios.put(`/admin/users/${selectedUserId.value}/role`, {
      role: selectedRole.value
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    alert('角色分配成功！')
    selectedUserId.value = ''
    loadUsers()
  } catch (err: any) {
    usersError.value = err.response?.data?.error || '分配角色失败'
  }
}

onMounted(() => {
  loadRoles()
  loadUsers()
})
</script>

<style scoped>
.permissions-management {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.page-header {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #f0f0f0;
}

.page-header h2 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1.8rem;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 1rem;
}

.card {
  background: #f9f9f9;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 1.5rem;
}

.card.mt-4 {
  margin-top: 2rem;
}

.card-header {
  background: white;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.2rem;
  font-weight: 600;
}

.loading, .error-message {
  padding: 4rem 0;
  text-align: center;
}

.error-message {
  background-color: #f8d7da;
  color: #721c24;
  padding: 1rem;
  border-radius: 6px;
  margin: 1rem;
}

.roles-content {
  padding: 2rem;
  background: white;
}

.message {
  padding: 2rem;
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  text-align: center;
  color: #666;
}

.roles-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.role-item {
  background: #f8f9fa;
  padding: 1.5rem;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.role-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.role-item h4 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1.1rem;
  font-weight: 600;
}

.role-item p {
  margin: 0 0 1rem 0;
  color: #666;
  font-size: 0.9rem;
}

.permissions-list {
  margin: 0;
  padding-left: 1.5rem;
  color: #666;
  font-size: 0.9rem;
}

.permissions-list li {
  margin-bottom: 0.25rem;
}

.assign-permissions {
  padding: 2rem;
  background: white;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #2c3e50;
}

.form-control {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-control:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background-color: #3498db;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #2980b9;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(52, 152, 219, 0.2);
}

.btn-primary:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .permissions-management {
    padding: 1rem;
  }
  
  .roles-list {
    grid-template-columns: 1fr;
  }
}
</style>