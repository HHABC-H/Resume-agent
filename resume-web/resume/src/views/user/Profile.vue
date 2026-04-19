<template>
  <div class="profile-container">
    <header class="header">
      <div class="logo">Resume Agent</div>
      <div class="header-content">
        <nav class="nav-menu">
          <router-link to="/" class="nav-item">首页</router-link>
          <router-link to="/resume/upload" class="nav-item">上传简历</router-link>
          <router-link to="/history" class="nav-item">查看历史</router-link>
          <router-link to="/profile" class="nav-item">个人资料</router-link>
          <template v-if="isAdmin">
            <router-link to="/admin" class="nav-item">管理后台</router-link>
          </template>
        </nav>
        <nav class="user-nav">
          <span class="user-info">{{ username }}</span>
          <button @click="logout" class="btn btn-text">退出</button>
        </nav>
      </div>
    </header>

    <main class="main-content">
      <div class="profile-card">
        <h1>个人资料</h1>
        
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="error" class="error-message">{{ error }}</div>
        <form v-else @submit.prevent="saveProfile">
          <div class="form-group">
            <label>用户名</label>
            <input type="text" v-model="profile.username" disabled>
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input type="email" v-model="profile.email">
          </div>
          <div class="form-group">
            <label>显示名称</label>
            <input type="text" v-model="profile.displayName">
          </div>
          <div class="form-group">
            <label>注册时间</label>
            <input type="text" :value="formatDate(profile.createdAt)" disabled>
          </div>
          
          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? '保存中...' : '保存修改' }}
            </button>
          </div>
          
          <div v-if="successMessage" class="success-message">
            {{ successMessage }}
          </div>
        </form>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const token = localStorage.getItem('token')
const username = localStorage.getItem('username')
const userId = localStorage.getItem('userId')
const role = localStorage.getItem('role')

const isAdmin = computed(() => role === 'ADMIN')

const profile = ref({
  username: '',
  email: '',
  displayName: '',
  createdAt: ''
})

const loading = ref(false)
const saving = ref(false)
const error = ref('')
const successMessage = ref('')

const loadProfile = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await axios.get('/auth/me', {
      headers: { Authorization: `Bearer ${token}` }
    })
    profile.value = {
      username: response.data.username,
      email: response.data.email || '',
      displayName: response.data.displayName || '',
      createdAt: response.data.createdAt
    }
  } catch (err: any) {
    error.value = err.response?.data?.error || '加载个人资料失败'
  } finally {
    loading.value = false
  }
}

const saveProfile = async () => {
  saving.value = true
  error.value = ''
  successMessage.value = ''
  try {
    await axios.put(`/user/profile`, {
      email: profile.value.email,
      displayName: profile.value.displayName
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    successMessage.value = '个人资料保存成功！'
    if (profile.value.displayName) {
      localStorage.setItem('username', profile.value.displayName)
    }
  } catch (err: any) {
    error.value = err.response?.data?.error || '保存失败'
  } finally {
    saving.value = false
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
  loadProfile()
})
</script>

<style scoped>
.profile-container {
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

.header-content {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.nav-item {
  color: #495057;
  text-decoration: none;
  font-weight: 500;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.2s, color 0.2s;
}

.nav-item:hover {
  background-color: #f8f9fa;
  color: #007bff;
}

.user-nav {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-info {
  color: #495057;
  font-weight: 500;
}

.main-content {
  padding: 2rem;
  max-width: 600px;
  margin: 0 auto;
}

.profile-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 2rem;
}

.profile-card h1 {
  margin-bottom: 2rem;
  color: #333;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #495057;
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
  color: #6c757d;
}

.form-actions {
  margin-top: 2rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  width: 100%;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #0069d9;
}

.btn-primary:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.btn-text {
  background: none;
  color: #007bff;
  padding: 0.5rem;
  width: auto;
}

.btn-text:hover {
  text-decoration: underline;
}

.loading, .error-message, .success-message {
  padding: 1rem;
  border-radius: 4px;
  text-align: center;
  margin-top: 1rem;
}

.error-message {
  background-color: #f8d7da;
  color: #721c24;
}

.success-message {
  background-color: #d4edda;
  color: #155724;
  margin-top: 1rem;
}
</style>