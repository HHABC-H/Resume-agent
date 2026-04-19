<template>
  <div class="upload-page">
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
      <div class="upload-container">
        <h2>上传简历</h2>
        <div class="upload-form">
          <div class="form-group">
            <label for="resumeFile">选择简历文件</label>
            <input type="file" id="resumeFile" @change="handleFileChange" accept=".txt,.pdf,.doc,.docx">
          </div>
          <div class="form-group">
            <label for="positionType">目标职位类型</label>
            <select id="positionType" v-model="positionType">
              <option value="BACKEND_JAVA">后端 Java</option>
              <option value="FRONTEND">前端</option>
              <option value="ALGORITHM">算法</option>
            </select>
          </div>
          <button @click="uploadResume" class="btn btn-primary" :disabled="!selectedFile || loading">
            {{ loading ? '分析中...' : '上传并分析' }}
          </button>
        </div>
        <div v-if="error" class="error-message">
          {{ error }}
        </div>
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
const role = localStorage.getItem('role')

const isAdmin = computed(() => role === 'ADMIN')

const selectedFile = ref<File | null>(null)
const positionType = ref('BACKEND_JAVA')
const loading = ref(false)
const error = ref('')

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    selectedFile.value = target.files[0]
  }
}

const uploadResume = async () => {
  if (!selectedFile.value) return

  loading.value = true
  error.value = ''

  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('positionType', positionType.value)

    const response = await axios.post('/resume/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token}`
      }
    })

    if (response.data.resumeId) {
      router.push(`/resume/analysis/${response.data.resumeId}`)
    }
  } catch (err: any) {
    error.value = err.response?.data?.error || err.response?.data?.message || '上传失败，请稍后重试'
  } finally {
    loading.value = false
  }
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
  }
})
</script>

<style scoped>
.upload-page {
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
}

.upload-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #333;
}

.upload-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

label {
  font-weight: 500;
}

input[type="file"] {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

select {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

button {
  padding: 0.75rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  margin-top: 1rem;
}

button:hover:not(:disabled) {
  background-color: #0069d9;
}

button:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.btn-text {
  background: none;
  color: #007bff;
  padding: 0.5rem;
}

.btn-text:hover {
  text-decoration: underline;
}

.error-message {
  margin-top: 1rem;
  padding: 0.75rem;
  background-color: #f8d7da;
  color: #721c24;
  border-radius: 4px;
}
</style>