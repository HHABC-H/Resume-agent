<template>
  <div class="admin-container">
    <header class="header">
      <div class="logo">Resume Agent - 管理后台</div>
      <nav class="nav">
        <router-link to="/admin" class="nav-link">用户管理</router-link>
        <router-link to="/admin/resume-history" class="nav-link">简历管理</router-link>
        <router-link to="/admin/interview-history" class="nav-link">面试管理</router-link>
        <router-link to="/admin/system-config" class="nav-link">系统配置</router-link>
        <router-link to="/admin/permissions" class="nav-link">权限管理</router-link>
        <span class="user-info">{{ username }}</span>
        <button @click="logout" class="btn btn-text">退出登录</button>
      </nav>
    </header>

    <main class="main-content">
      <!-- 用户管理 -->
      <div v-if="currentPath === '/admin'">
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
      </div>

      <!-- 简历管理 -->
      <div v-else-if="currentPath === '/admin/resume-history'">
        <div class="page-header">
          <h1>简历管理</h1>
        </div>
        <div class="card">
          <div class="card-header">
            <h2>所有用户简历分析历史</h2>
            <button @click="exportResumeHistory" class="btn btn-primary">导出简历分析数据</button>
          </div>
          <div v-if="resumeHistoryLoading" class="loading">加载中...</div>
          <div v-else-if="resumeHistoryError" class="error-message">{{ resumeHistoryError }}</div>
          <div v-else class="resume-history-table-container">
            <table class="users-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户ID</th>
                  <th>简历ID</th>
                  <th>职位类型</th>
                  <th>状态</th>
                  <th>简历评分</th>
                  <th>面试评分</th>
                  <th>问题数量</th>
                  <th>创建时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in resumeHistory" :key="item.id">
                  <td>{{ item.id }}</td>
                  <td>{{ item.userId || '-' }}</td>
                  <td>{{ item.resumeId }}</td>
                  <td>{{ item.positionType }}</td>
                  <td>{{ item.status }}</td>
                  <td>{{ item.resumeOverallScore || '-' }}</td>
                  <td>{{ item.evaluationOverallScore || '-' }}</td>
                  <td>{{ item.questionCount || 0 }}</td>
                  <td>{{ formatDate(item.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 面试管理 -->
      <div v-else-if="currentPath === '/admin/interview-history'">
        <div class="page-header">
          <h1>面试管理</h1>
        </div>
        <div class="card">
          <div class="card-header">
            <h2>所有用户面试历史</h2>
          </div>
          <div v-if="interviewHistoryLoading" class="loading">加载中...</div>
          <div v-else-if="interviewHistoryError" class="error-message">{{ interviewHistoryError }}</div>
          <div v-else class="interview-history-table-container">
            <table class="users-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户ID</th>
                  <th>简历ID</th>
                  <th>职位类型</th>
                  <th>状态</th>
                  <th>面试评分</th>
                  <th>问题数量</th>
                  <th>创建时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in interviewHistory" :key="item.id">
                  <td>{{ item.id }}</td>
                  <td>{{ item.userId || '-' }}</td>
                  <td>{{ item.resumeId }}</td>
                  <td>{{ item.positionType }}</td>
                  <td>{{ item.status }}</td>
                  <td>{{ item.evaluationOverallScore || '-' }}</td>
                  <td>{{ item.questionCount || 0 }}</td>
                  <td>{{ formatDate(item.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="card">
          <div class="card-header">
            <h2>面试模板和问题库</h2>
          </div>
          <div v-if="interviewTemplatesLoading" class="loading">加载中...</div>
          <div v-else-if="interviewTemplatesError" class="error-message">{{ interviewTemplatesError }}</div>
          <div v-else class="interview-templates-container">
            <div v-if="interviewTemplates.message" class="message">{{ interviewTemplates.message }}</div>
            <div v-else>
              <!-- 这里可以添加面试模板的具体内容 -->
            </div>
          </div>
        </div>
      </div>

      <!-- 系统配置 -->
      <div v-else-if="currentPath === '/admin/system-config'">
        <div class="page-header">
          <h1>系统配置</h1>
        </div>
        <div class="card">
          <div class="card-header">
            <h2>AI模型参数</h2>
            <div>
              <button v-if="!aiConfigEditing" @click="aiConfigEditing = true" class="btn btn-sm btn-primary">编辑</button>
              <div v-else>
                <button @click="updateAIConfig" class="btn btn-sm btn-success">保存</button>
                <button @click="aiConfigEditing = false" class="btn btn-sm btn-secondary">取消</button>
              </div>
            </div>
          </div>
          <div v-if="aiConfigLoading" class="loading">加载中...</div>
          <div v-else-if="aiConfigError" class="error-message">{{ aiConfigError }}</div>
          <div v-else class="ai-config-form">
            <div class="form-group">
              <label>模型</label>
              <input v-if="aiConfigEditing" type="text" v-model="aiConfig.model" class="form-control">
              <span v-else>{{ aiConfig.model }}</span>
            </div>
            <div class="form-group">
              <label>API Key</label>
              <input v-if="aiConfigEditing" type="text" v-model="aiConfig.apiKey" class="form-control">
              <span v-else>{{ aiConfig.apiKey }}</span>
            </div>
            <div class="form-group">
              <label>最大重试次数</label>
              <input v-if="aiConfigEditing" type="number" v-model="aiConfig.retryMaxAttempts" class="form-control">
              <span v-else>{{ aiConfig.retryMaxAttempts }}</span>
            </div>
            <div class="form-group">
              <label>重试间隔(ms)</label>
              <input v-if="aiConfigEditing" type="number" v-model="aiConfig.retryBackoffMs" class="form-control">
              <span v-else>{{ aiConfig.retryBackoffMs }}</span>
            </div>
          </div>
        </div>
        <div class="card">
          <div class="card-header">
            <h2>提示词模板</h2>
          </div>
          <div v-if="promptTemplatesLoading" class="loading">加载中...</div>
          <div v-else-if="promptTemplatesError" class="error-message">{{ promptTemplatesError }}</div>
          <div v-else class="prompt-templates-container">
            <div v-if="promptTemplates.message" class="message">{{ promptTemplates.message }}</div>
            <div v-else>
              <!-- 这里可以添加提示词模板的具体内容 -->
            </div>
          </div>
        </div>
        <div class="card">
          <div class="card-header">
            <h2>系统限制</h2>
            <div>
              <button v-if="!systemLimitsEditing" @click="systemLimitsEditing = true" class="btn btn-sm btn-primary">编辑</button>
              <div v-else>
                <button @click="updateSystemLimits" class="btn btn-sm btn-success">保存</button>
                <button @click="systemLimitsEditing = false" class="btn btn-sm btn-secondary">取消</button>
              </div>
            </div>
          </div>
          <div v-if="systemLimitsLoading" class="loading">加载中...</div>
          <div v-else-if="systemLimitsError" class="error-message">{{ systemLimitsError }}</div>
          <div v-else class="system-limits-form">
            <div class="form-group">
              <label>简历上传大小</label>
              <input v-if="systemLimitsEditing" type="text" v-model="systemLimits.resumeUploadSize" class="form-control">
              <span v-else>{{ systemLimits.resumeUploadSize }}</span>
            </div>
            <div class="form-group">
              <label>面试问题数量</label>
              <input v-if="systemLimitsEditing" type="number" v-model="systemLimits.interviewQuestionCount" class="form-control">
              <span v-else>{{ systemLimits.interviewQuestionCount }}</span>
            </div>
            <div class="form-group">
              <label>最大用户数</label>
              <input v-if="systemLimitsEditing" type="number" v-model="systemLimits.maxUsers" class="form-control">
              <span v-else>{{ systemLimits.maxUsers }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 权限管理 -->
      <div v-else-if="currentPath === '/admin/permissions'">
        <div class="page-header">
          <h1>权限管理</h1>
        </div>
        <div class="card">
          <div class="card-header">
            <h2>角色和权限</h2>
          </div>
          <div v-if="rolesLoading" class="loading">加载中...</div>
          <div v-else-if="rolesError" class="error-message">{{ rolesError }}</div>
          <div v-else class="roles-list">
            <table class="users-table">
              <thead>
                <tr>
                  <th>角色名称</th>
                  <th>描述</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="role in roles" :key="role.name">
                  <td>{{ role.name }}</td>
                  <td>{{ role.description }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="card">
          <div class="card-header">
            <h2>分配管理员权限</h2>
          </div>
          <div v-if="loading" class="loading">加载中...</div>
          <div v-else-if="error" class="error-message">{{ error }}</div>
          <div v-else class="user-role-assignment">
            <div class="form-group">
              <label>选择用户</label>
              <select v-model="selectedUser" class="form-control">
                <option value="">请选择用户</option>
                <option v-for="user in users" :key="user.id" :value="user">{{ user.username }} ({{ user.displayName || '无显示名称' }})</option>
              </select>
            </div>
            <div class="form-group" v-if="selectedUser">
              <label>选择角色</label>
              <select v-model="selectedRole" class="form-control">
                <option value="USER">普通用户</option>
                <option value="ADMIN">管理员</option>
              </select>
            </div>
            <div class="form-actions" v-if="selectedUser">
              <button @click="updateUserRole" class="btn btn-primary">更新角色</button>
            </div>
          </div>
        </div>
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
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

const currentPath = computed(() => route.path)

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

// 简历历史相关
const resumeHistory = ref<any[]>([])
const resumeHistoryLoading = ref(false)
const resumeHistoryError = ref('')

// 面试历史相关
const interviewHistory = ref<any[]>([])
const interviewHistoryLoading = ref(false)
const interviewHistoryError = ref('')

// 面试模板相关
const interviewTemplates = ref<any[]>([])
const interviewTemplatesLoading = ref(false)
const interviewTemplatesError = ref('')

// 系统配置相关
const aiConfig = ref<any>({})
const aiConfigLoading = ref(false)
const aiConfigError = ref('')
const aiConfigEditing = ref(false)

const promptTemplates = ref<any[]>([])
const promptTemplatesLoading = ref(false)
const promptTemplatesError = ref('')

const systemLimits = ref<any>({})
const systemLimitsLoading = ref(false)
const systemLimitsError = ref('')
const systemLimitsEditing = ref(false)

// 权限管理相关
const roles = ref<any[]>([])
const rolesLoading = ref(false)
const rolesError = ref('')

const selectedUser = ref<any>(null)
const selectedRole = ref('USER')

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

const loadResumeHistory = async () => {
  resumeHistoryLoading.value = true
  resumeHistoryError.value = ''
  try {
    const response = await axios.get('/api/admin/resume-history', {
      headers: { Authorization: `Bearer ${token}` }
    })
    resumeHistory.value = response.data
  } catch (err: any) {
    resumeHistoryError.value = err.response?.data?.error || '加载简历历史失败'
  } finally {
    resumeHistoryLoading.value = false
  }
}

const exportResumeHistory = async () => {
  try {
    const response = await axios.get('/api/admin/resume-history/export', {
      headers: { Authorization: `Bearer ${token}` },
      responseType: 'blob'
    })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'resume-history.csv')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (err: any) {
    alert('导出失败: ' + (err.response?.data?.error || '未知错误'))
  }
}

const loadInterviewHistory = async () => {
  interviewHistoryLoading.value = true
  interviewHistoryError.value = ''
  try {
    const response = await axios.get('/api/admin/interview-history', {
      headers: { Authorization: `Bearer ${token}` }
    })
    interviewHistory.value = response.data
  } catch (err: any) {
    interviewHistoryError.value = err.response?.data?.error || '加载面试历史失败'
  } finally {
    interviewHistoryLoading.value = false
  }
}

const loadInterviewTemplates = async () => {
  interviewTemplatesLoading.value = true
  interviewTemplatesError.value = ''
  try {
    const response = await axios.get('/api/admin/interview-templates', {
      headers: { Authorization: `Bearer ${token}` }
    })
    interviewTemplates.value = response.data
  } catch (err: any) {
    interviewTemplatesError.value = err.response?.data?.error || '加载面试模板失败'
  } finally {
    interviewTemplatesLoading.value = false
  }
}

const loadAIConfig = async () => {
  aiConfigLoading.value = true
  aiConfigError.value = ''
  try {
    const response = await axios.get('/api/admin/system-config/ai', {
      headers: { Authorization: `Bearer ${token}` }
    })
    aiConfig.value = response.data
  } catch (err: any) {
    aiConfigError.value = err.response?.data?.error || '加载AI配置失败'
  } finally {
    aiConfigLoading.value = false
  }
}

const updateAIConfig = async () => {
  aiConfigLoading.value = true
  aiConfigError.value = ''
  try {
    const response = await axios.put('/api/admin/system-config/ai', aiConfig.value, {
      headers: { Authorization: `Bearer ${token}` }
    })
    alert('AI配置更新成功')
    aiConfigEditing.value = false
  } catch (err: any) {
    aiConfigError.value = err.response?.data?.error || '更新AI配置失败'
  } finally {
    aiConfigLoading.value = false
  }
}

const loadPromptTemplates = async () => {
  promptTemplatesLoading.value = true
  promptTemplatesError.value = ''
  try {
    const response = await axios.get('/api/admin/system-config/prompts', {
      headers: { Authorization: `Bearer ${token}` }
    })
    promptTemplates.value = response.data
  } catch (err: any) {
    promptTemplatesError.value = err.response?.data?.error || '加载提示词模板失败'
  } finally {
    promptTemplatesLoading.value = false
  }
}

const loadSystemLimits = async () => {
  systemLimitsLoading.value = true
  systemLimitsError.value = ''
  try {
    const response = await axios.get('/api/admin/system-config/limits', {
      headers: { Authorization: `Bearer ${token}` }
    })
    systemLimits.value = response.data
  } catch (err: any) {
    systemLimitsError.value = err.response?.data?.error || '加载系统限制失败'
  } finally {
    systemLimitsLoading.value = false
  }
}

const updateSystemLimits = async () => {
  systemLimitsLoading.value = true
  systemLimitsError.value = ''
  try {
    const response = await axios.put('/api/admin/system-config/limits', systemLimits.value, {
      headers: { Authorization: `Bearer ${token}` }
    })
    alert('系统限制更新成功')
    systemLimitsEditing.value = false
  } catch (err: any) {
    systemLimitsError.value = err.response?.data?.error || '更新系统限制失败'
  } finally {
    systemLimitsLoading.value = false
  }
}

const loadRoles = async () => {
  rolesLoading.value = true
  rolesError.value = ''
  try {
    const response = await axios.get('/api/admin/permissions/roles', {
      headers: { Authorization: `Bearer ${token}` }
    })
    roles.value = response.data.roles
  } catch (err: any) {
    rolesError.value = err.response?.data?.error || '加载角色列表失败'
  } finally {
    rolesLoading.value = false
  }
}

const updateUserRole = async () => {
  if (!selectedUser.value) return
  
  try {
    const response = await axios.put(`/api/admin/permissions/users/${selectedUser.value.id}/role`, {
      role: selectedRole.value
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    alert('用户角色更新成功')
    selectedUser.value = null
    selectedRole.value = 'USER'
    loadUsers() // 重新加载用户列表以更新角色显示
  } catch (err: any) {
    alert('更新用户角色失败: ' + (err.response?.data?.error || '未知错误'))
  }
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
  if (currentPath.value === '/admin') {
    loadUsers()
  } else if (currentPath.value === '/admin/resume-history') {
    loadResumeHistory()
  } else if (currentPath.value === '/admin/interview-history') {
    loadInterviewHistory()
    loadInterviewTemplates()
  } else if (currentPath.value === '/admin/system-config') {
    loadAIConfig()
    loadPromptTemplates()
    loadSystemLimits()
  } else if (currentPath.value === '/admin/permissions') {
    loadRoles()
    loadUsers() // 加载用户列表用于角色分配
  }
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

.card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.card h2 {
  color: #333;
  font-size: 1.25rem;
  margin: 0;
}

.card .loading {
  padding: 2rem 0;
  text-align: center;
}

.form-control {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  margin-top: 0.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  font-weight: 500;
  margin-bottom: 0.5rem;
}

.message {
  padding: 1rem;
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  text-align: center;
}
</style>