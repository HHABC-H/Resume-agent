<template>
  <div class="history-container">
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
      <h1>历史记录</h1>
      
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="error" class="error-message">{{ error }}</div>
      <div v-else-if="history.length === 0" class="empty-state">
        <p>暂无历史记录</p>
        <router-link to="/resume/upload" class="btn btn-primary">上传简历开始分析</router-link>
      </div>
      <div v-else>
        <!-- 标签页切换 -->
        <div class="tabs">
          <button 
            :class="['tab-button', { active: activeTab === 'resume' }]"
            @click="activeTab = 'resume'"
          >
            简历分析历史
          </button>
          <button 
            :class="['tab-button', { active: activeTab === 'interview' }]"
            @click="activeTab = 'interview'"
          >
            面试分析历史
          </button>
        </div>
        
        <!-- 简历分析历史 -->
        <div v-show="activeTab === 'resume'" class="tab-content">
          <div v-if="resumeAnalysisHistory.length === 0" class="empty-section">
            <p>暂无简历分析记录</p>
          </div>
          <div v-else class="history-list">
            <div v-for="item in resumeAnalysisHistory" :key="item.resumeId" class="history-item">
              <div class="history-info">
                <div class="history-header">
                  <span class="position-type">{{ displayPositionType(item.positionType) }}</span>
                  <span :class="['status-badge', item.status.toLowerCase()]">{{ displayStatus(item.status) }}</span>
                </div>
                <div class="history-scores">
                  <span v-if="item.resumeScore">简历评分: {{ item.resumeScore }}</span>
                </div>
                <div class="history-date">
                  {{ formatDate(item.updatedAt) }}
                </div>
              </div>
              <div class="history-actions">
                <router-link 
                  v-if="item.status === 'ANALYZED'"
                  :to="`/resume/analysis/${item.resumeId}`" 
                  class="btn btn-secondary"
                >
                  查看分析
                </router-link>
                <router-link 
                  v-if="item.status === 'ANALYZED' || item.status === 'QUESTIONS_READY'"
                  :to="`/interview/${item.resumeId}`" 
                  class="btn btn-primary"
                >
                  开始面试
                </router-link>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 面试分析历史 -->
        <div v-show="activeTab === 'interview'" class="tab-content">
          <div v-if="interviewAnalysisHistory.length === 0" class="empty-section">
            <p>暂无面试分析记录</p>
          </div>
          <div v-else class="history-list">
            <div v-for="item in interviewAnalysisHistory" :key="item.resumeId" class="history-item">
              <div class="history-info">
                <div class="history-header">
                  <span class="position-type">{{ displayPositionType(item.positionType) }}</span>
                  <span :class="['status-badge', item.status.toLowerCase()]">{{ displayStatus(item.status) }}</span>
                </div>
                <div class="history-scores">
                  <span v-if="item.resumeScore">简历评分: {{ item.resumeScore }}</span>
                  <span v-if="item.evaluationScore">面试评分: {{ item.evaluationScore }}</span>
                  <span v-if="item.questionCount">问题数: {{ item.questionCount }}</span>
                </div>
                <div class="history-date">
                  {{ formatDate(item.updatedAt) }}
                </div>
              </div>
              <div class="history-actions">
                <router-link 
                  v-if="item.status === 'QUESTIONS_READY'"
                  :to="`/interview/${item.resumeId}`" 
                  class="btn btn-primary"
                >
                  继续面试
                </router-link>
                <router-link 
                  v-if="item.status === 'EVALUATED'"
                  :to="`/interview/result/${item.resumeId}`" 
                  class="btn btn-secondary"
                >
                  查看结果
                </router-link>
              </div>
            </div>
          </div>
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

const history = ref<any[]>([])
const loading = ref(false)
const error = ref('')

// 简历分析历史（已分析或已生成问题）
const resumeAnalysisHistory = computed(() => {
  return history.value.filter(item => 
    item.status === 'ANALYZED' || item.status === 'QUESTIONS_READY'
  )
})

// 面试分析历史（已评估）
const activeTab = ref('resume')

const interviewAnalysisHistory = computed(() => {
  return history.value.filter(item => 
    item.status === 'EVALUATED'
  )
})

const loadHistory = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await axios.get('/api/history/data', {
      headers: { Authorization: `Bearer ${token}` }
    })
    history.value = response.data
  } catch (err: any) {
    error.value = err.response?.data?.error || '加载历史记录失败'
  } finally {
    loading.value = false
  }
}

const displayPositionType = (type: string) => {
  const map: Record<string, string> = {
    'BACKEND_JAVA': '后端 Java',
    'FRONTEND': '前端',
    'ALGORITHM': '算法'
  }
  return map[type] || type
}

const displayStatus = (status: string) => {
  const map: Record<string, string> = {
    'ANALYZED': '已分析',
    'QUESTIONS_READY': '已生成问题',
    'EVALUATED': '已评估'
  }
  return map[status] || status
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
  loadHistory()
})
</script>

<style scoped>
.history-container {
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
  max-width: 800px;
  margin: 0 auto;
}

.main-content h1 {
  margin-bottom: 2rem;
  color: #333;
}

.tabs {
  display: flex;
  margin-bottom: 2rem;
  border-bottom: 1px solid #e0e0e0;
}

.tab-button {
  padding: 0.75rem 1.5rem;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-button:hover {
  color: #007bff;
}

.tab-button.active {
  color: #007bff;
  border-bottom-color: #007bff;
  font-weight: 500;
}

.tab-content {
  min-height: 300px;
}

.loading, .error-message, .empty-state {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.empty-section {
  text-align: center;
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  color: #6c757d;
}

.error-message {
  background-color: #f8d7da;
  color: #721c24;
}

.empty-state p {
  margin-bottom: 1rem;
  color: #6c757d;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.history-item {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-info {
  flex: 1;
}

.history-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.position-type {
  font-weight: 600;
  color: #333;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.875rem;
}

.status-badge.analyzed {
  background-color: #cce5ff;
  color: #004085;
}

.status-badge.questions_ready {
  background-color: #fff3cd;
  color: #856404;
}

.status-badge.evaluated {
  background-color: #d4edda;
  color: #155724;
}

.history-scores {
  display: flex;
  gap: 1.5rem;
  margin: 0.5rem 0;
  color: #6c757d;
  font-size: 0.875rem;
}

.history-date {
  color: #6c757d;
  font-size: 0.875rem;
}

.history-actions {
  display: flex;
  gap: 0.5rem;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  font-size: 0.875rem;
  cursor: pointer;
  text-decoration: none;
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

.btn-text {
  background: none;
  color: #007bff;
  padding: 0.5rem;
}

.btn-text:hover {
  text-decoration: underline;
}
</style>