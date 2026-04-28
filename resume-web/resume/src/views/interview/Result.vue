<template>
  <div class="result-page">
    <header class="header">
      <div class="logo" @click="router.push('/')">技术论坛</div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">论坛</router-link>
        <router-link to="/resume/upload" class="nav-item">简历助手</router-link>
        <router-link to="/reading" class="nav-item">在线阅读</router-link>
        <router-link to="/interview/1" class="nav-item">面试助手</router-link>
        <router-link to="/profile" class="nav-item">个人信息</router-link>
        <router-link to="/history" class="nav-item">查看历史</router-link>
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
      <div class="content-card">
        <div class="page-header">
          <button class="back-btn" @click="goBack">← 返回</button>
          <h1 class="page-title">面试评估结果</h1>
        </div>

        <div v-if="loading" class="loading">
          加载评估结果中...
        </div>
        <div v-else-if="error" class="error-message">
          {{ error }}
        </div>
        <div v-else-if="evaluation" class="result-content">
          <div class="score-section">
            <h3>面试评分</h3>
            <div class="score-value">{{ evaluation.overallScore }}</div>
            <div class="score-breakdown">
              <div v-for="(item, index) in evaluation.categoryScores" :key="index" class="score-item">
                <span>{{ item.category }}:</span>
                <span>{{ item.score }}</span>
              </div>
            </div>
          </div>

          <div class="strengths-section">
            <h3>优势</h3>
            <ul>
              <li v-for="(strength, index) in evaluation.strengths" :key="index">
                {{ strength }}
              </li>
            </ul>
          </div>

          <div class="improvements-section">
            <h3>改进建议</h3>
            <ul>
              <li v-for="(improvement, index) in evaluation.improvements" :key="index">
                {{ improvement }}
              </li>
            </ul>
          </div>

          <div class="answers-section">
            <h3>问题与答案评估</h3>
            <div v-for="(item, index) in evaluation.questionDetails" :key="index" class="answer-item">
              <div class="answer-header">
                <span class="question-number">问题 {{ (item.questionIndex as number) + 1 }}</span>
                <span class="answer-score">得分: {{ item.score }}</span>
              </div>
              <div class="question-text">{{ item.question }}</div>
              <div class="user-answer">
                <strong>你的回答:</strong>
                <p>{{ item.userAnswer }}</p>
              </div>
              <div class="evaluation-text">
                <strong>评估:</strong>
                <p>{{ item.feedback }}</p>
              </div>
            </div>
          </div>

          <div class="actions">
            <button @click="goBack" class="btn btn-secondary">
              返回首页
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
const evaluation = ref<any>(null)

const token = localStorage.getItem('token')
const username = localStorage.getItem('username')

const isLoggedIn = computed(() => !!token)

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userId')
  localStorage.removeItem('role')
  router.push('/login')
}

const resumeId = computed(() => route.params.resumeId as string)

onMounted(async () => {
  await loadEvaluation()
})

const loadEvaluation = async () => {
  try {
    const response = await axios.get(`/api/interview/result/${resumeId.value}`)
    evaluation.value = response.data.evaluation
  } catch (err: any) {
    error.value = err.response?.data?.error || '加载评估结果失败'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/resume/upload')
}


</script>

<style scoped>
.result-page {
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
  padding: 1.5rem 2rem;
}

.content-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  max-width: 800px;
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

.result-content {
  padding: 1.5rem;
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
  flex: 1 1 200px;
}

.strengths-section,
.improvements-section {
  margin-bottom: 2rem;
}

.answers-section {
  margin-bottom: 2rem;
}

h3 {
  margin-bottom: 1rem;
  color: #495057;
}

ul {
  list-style-type: disc;
  padding-left: 1.5rem;
}

li {
  margin-bottom: 0.5rem;
  line-height: 1.5;
}

.answer-item {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.answer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.question-number {
  font-weight: bold;
  color: #007bff;
}

.answer-score {
  font-weight: bold;
  color: #28a745;
}

.question-text {
  margin-bottom: 1rem;
  line-height: 1.5;
}

.user-answer,
.evaluation-text {
  margin-bottom: 1rem;
}

.user-answer p,
.evaluation-text p {
  margin-top: 0.5rem;
  line-height: 1.5;
}

.actions {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

button {
  padding: 0.75rem 1.5rem;
  background-color: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
}

button:hover {
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
}
</style>