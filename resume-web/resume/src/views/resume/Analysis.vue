<template>
  <div class="analysis-page">
    <header class="header">
      <div class="logo" @click="router.push('/')">技术论坛</div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">论坛</router-link>
        <router-link to="/resume/upload" class="nav-item">简历助手</router-link>
        <router-link to="/reading" class="nav-item">在线阅读</router-link>
        <router-link to="/interview/1" class="nav-item">面试助手</router-link>
        <router-link to="/profile" class="nav-item">个人信息</router-link>
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
            <button @click="router.go(-1)" class="btn-primary">返回</button>
          </template>
          <template v-else>
            <router-link to="/login" class="btn-login">登录</router-link>
            <router-link to="/register" class="btn-register">注册</router-link>
          </template>
        </div>
      </div>
    </header>

    <main class="main-content">
      <div class="content-card">
        <div class="page-header">
          <button class="back-btn" @click="goBack">← 返回</button>
          <h1 class="page-title">简历分析结果</h1>
        </div>

        <div v-if="loading" class="loading">
          加载中...
        </div>
        <div v-else-if="error" class="error-message">
          {{ error }}
        </div>
        <div v-else-if="analysis" class="analysis-content">
          <div class="main-layout">
            <div class="resume-section">
              <h3>简历内容</h3>
              <div class="resume-content">
                {{ resumeText }}
              </div>
            </div>
            <div class="analysis-section">
              <div class="score-section">
                <h3>综合评分</h3>
                <div class="score-value">{{ analysis.overallScore }}</div>
                <div class="score-breakdown">
                  <div class="score-item">
                    <span>项目评分:</span>
                    <span>{{ analysis.scoreDetail.projectScore }}</span>
                  </div>
                  <div class="score-item">
                    <span>技能匹配:</span>
                    <span>{{ analysis.scoreDetail.skillMatchScore }}</span>
                  </div>
                  <div class="score-item">
                    <span>内容质量:</span>
                    <span>{{ analysis.scoreDetail.contentScore }}</span>
                  </div>
                  <div class="score-item">
                    <span>结构评分:</span>
                    <span>{{ analysis.scoreDetail.structureScore }}</span>
                  </div>
                  <div class="score-item">
                    <span>表达评分:</span>
                    <span>{{ analysis.scoreDetail.expressionScore }}</span>
                  </div>
                </div>
              </div>

              <div class="strengths-section">
                <h3>优势</h3>
                <ul>
                  <li v-for="(strength, index) in analysis.strengths" :key="index">
                    {{ strength }}
                  </li>
                </ul>
              </div>

              <div class="suggestions-section">
                <h3>改进建议</h3>
                <ul>
                  <li v-for="(suggestion, index) in analysis.suggestions" :key="index">
                    <div class="suggestion-item">
                      <div class="suggestion-category">{{ suggestion.category }} - {{ suggestion.priority }}</div>
                      <div class="suggestion-issue">{{ suggestion.issue }}</div>
                      <div class="suggestion-recommendation">{{ suggestion.recommendation }}</div>
                    </div>
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <div class="actions">
            <button @click="startInterview" class="btn btn-primary">
              开始模拟面试
            </button>
            <button @click="goBack" class="btn btn-secondary">
              返回上传
            </button>
          </div>
        </div>
      </div>
    </main>

    <footer class="footer">
      <p>&copy; 2024 Resume Agent. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const error = ref('')
const analysis = ref<any>(null)
const resumeText = ref('')

const token = localStorage.getItem('token')
const username = localStorage.getItem('username')

const isLoggedIn = computed(() => !!token)

const resumeId = computed(() => route.params.resumeId as string)

onMounted(async () => {
  await loadAnalysis()
})

const loadAnalysis = async () => {
  try {
    const response = await axios.get(`/resume/analysis/${resumeId.value}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    analysis.value = response.data.scoreResult
    resumeText.value = response.data.resumeText
  } catch (err: any) {
    error.value = err.response?.data?.error || '加载分析结果失败'
  } finally {
    loading.value = false
  }
}

const startInterview = () => {
  router.push(`/interview/${resumeId.value}`)
}

const goBack = () => {
  router.push('/resume/upload')
}


</script>

<style scoped>
.analysis-page {
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

.btn-primary {
  padding: 0.5rem 1rem;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary:hover {
  background: #0069d9;
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

.main-content {
  flex: 1;
  padding: 1.5rem 2rem;
}

.content-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #eee;
}

.back-btn {
  padding: 0.5rem 1rem;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  margin-right: 1rem;
}

.back-btn:hover {
  border-color: #007bff;
  color: #007bff;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.analysis-content {
  padding: 1.5rem;
}

.main-layout {
  display: flex;
  gap: 2rem;
  margin-bottom: 2rem;
}

.resume-section {
  flex: 1;
  min-width: 400px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

.resume-section h3 {
  margin: 0;
  padding: 1rem;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
  color: #495057;
}

.resume-content {
  padding: 1rem;
  max-height: 600px;
  overflow-y: auto;
  white-space: pre-wrap;
  line-height: 1.6;
  font-family: 'Courier New', Courier, monospace;
  background-color: #fafafa;
}

.analysis-section {
  flex: 1;
  min-width: 400px;
}

.score-section {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.score-value {
  font-size: 2.5rem;
  font-weight: bold;
  text-align: center;
  margin: 1rem 0;
  color: #007bff;
}

.score-breakdown {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-top: 1rem;
}

.score-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem;
  background-color: white;
  border-radius: 4px;
  flex: 1 1 150px;
}

.strengths-section,
.suggestions-section {
  margin-bottom: 2rem;
}

h3 {
  margin-bottom: 1rem;
  color: #495057;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  margin-bottom: 1rem;
  line-height: 1.5;
}

.suggestion-item {
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #007bff;
}

.suggestion-category {
  font-weight: bold;
  color: #007bff;
  margin-bottom: 0.5rem;
}

.suggestion-issue {
  margin-bottom: 0.5rem;
  color: #dc3545;
}

.suggestion-recommendation {
  color: #28a745;
}

.actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

button {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
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

.loading {
  text-align: center;
  padding: 2rem;
  color: #6c757d;
}

.error-message {
  padding: 1rem;
  background-color: #f8d7da;
  color: #721c24;
  border-radius: 4px;
  margin: 1rem;
}

.footer {
  text-align: center;
  padding: 1rem;
  background: #fff;
  color: #999;
  font-size: 13px;
}

@media (max-width: 900px) {
  .header {
    flex-wrap: wrap;
    gap: 1rem;
    padding: 0.8rem 1rem;
  }

  .nav-menu {
    order: 3;
    width: 100%;
    overflow-x: auto;
  }

  .main-layout {
    flex-direction: column;
  }

  .resume-section,
  .analysis-section {
    min-width: 100%;
  }
}
</style>