<template>
  <div class="forum-home">
    <header class="header">
      <div class="logo" @click="router.push('/')">技术论坛</div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">论坛</router-link>
        <router-link to="/resume/upload" class="nav-item">简历助手</router-link>
        <router-link to="/reading" class="nav-item">在线阅读</router-link>
        <router-link :to="interviewLink" class="nav-item">面试助手</router-link>
        <router-link to="/profile" class="nav-item">个人信息</router-link>
        <router-link to="/history" class="nav-item active">查看历史</router-link>
        <router-link to="/my-bookmarks" class="nav-item">我的收藏</router-link>
        <router-link to="/forum/essences" class="nav-item">精华帖</router-link>
        <router-link to="/forum/authors" class="nav-item">热门作者</router-link>
      </nav>
      <div class="header-right">
        <router-link to="/forum/publish" class="btn-publish-header">+ 发布帖子</router-link>
        <div class="user-section">
          <template v-if="isLoggedIn">
            <div class="user-info" @click="router.push('/profile')">
              <span class="username">{{ username }}</span>
              <div class="avatar">{{ username?.charAt(0).toUpperCase() }}</div>
            </div>
            <button @click="handleLogout" class="btn-logout">退出</button>
          </template>
          <template v-else>
            <router-link to="/login" class="btn-login">登录</router-link>
            <router-link to="/register" class="btn-register">注册</router-link>
          </template>
        </div>
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
                  v-if="item.status === 'ANALYZED' || item.status === 'QUESTIONS_READY' || item.status === 'EVALUATED'"
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

const isLoggedIn = computed(() => !!token)

const interviewLink = computed(() => {
  if (!token) return '/login'
  return '/interview/1'
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userId')
  localStorage.removeItem('role')
  router.push('/login')
}

const history = ref<any[]>([])
const loading = ref(false)
const error = ref('')

// 简历分析历史（已分析或已生成问题）
const resumeAnalysisHistory = computed(() => {
  return history.value.filter(item => 
    item.status === 'ANALYZED' || item.status === 'QUESTIONS_READY' || item.status === 'EVALUATED'
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
    history.value = response.data.data || response.data || []
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

onMounted(() => {
  if (!token) {
    router.push('/login')
    return
  }
  loadHistory()
})
</script>

<style scoped>
.forum-home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem 2rem;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  font-size: 1.4rem;
  font-weight: bold;
  color: #007bff;
  cursor: pointer;
  margin-right: 2rem;
}

.nav-menu {
  display: flex;
  gap: 0.3rem;
  flex: 1;
}

.nav-item {
  color: #666;
  text-decoration: none;
  padding: 0.5rem 0.9rem;
  border-radius: 4px;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.2s;
}

.nav-item:hover {
  background: #f0f7ff;
  color: #007bff;
}

.nav-item.active {
  background: #007bff;
  color: #fff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.btn-publish-header {
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
  color: #fff;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-publish-header:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,123,255,0.3);
}

.user-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  padding: 0.3rem 0.5rem;
  border-radius: 4px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f5f5f5;
}

.username {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.btn-login, .btn-register {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
}

.btn-login {
  color: #007bff;
}

.btn-register {
  background: #007bff;
  color: #fff;
}

.btn-logout {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
  background: #dc3545;
  color: #fff;
  border: none;
  cursor: pointer;
}

.btn-logout:hover {
  background: #c82333;
}

.main-content {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.main-content h1 {
  margin-bottom: 2rem;
  color: #333;
  font-size: 24px;
}

.tabs {
  display: flex;
  margin-bottom: 2rem;
  border-bottom: 1px solid #e0e0e0;
}

.tab-button {
  padding: 1rem 2rem;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
  color: #666;
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
  padding: 4rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.empty-section {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
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
  gap: 1.5rem;
}

.history-item {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: box-shadow 0.2s;
}

.history-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.history-info {
  flex: 1;
}

.history-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.position-type {
  font-weight: 600;
  color: #333;
  font-size: 16px;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 13px;
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
  gap: 2rem;
  margin: 0.75rem 0;
  color: #666;
  font-size: 14px;
}

.history-date {
  color: #999;
  font-size: 13px;
  margin-top: 0.5rem;
}

.history-actions {
  display: flex;
  gap: 1rem;
}

.btn {
  padding: 0.6rem 1.2rem;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.2s;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover {
  background-color: #0069d9;
  transform: translateY(-1px);
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
