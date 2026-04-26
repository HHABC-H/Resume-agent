<template>
  <div class="interview-page">
    <header class="header">
      <div class="logo" @click="router.push('/')">技术论坛</div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">论坛</router-link>
        <router-link to="/resume/upload" class="nav-item">简历助手</router-link>
        <router-link to="/reading" class="nav-item">在线阅读</router-link>
        <router-link to="/interview/1" class="nav-item active">面试助手</router-link>
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
          <h1 class="page-title">模拟面试</h1>
        </div>

        <div v-if="loading" class="loading">
          {{ loadingText }}
        </div>
        <div v-else-if="error" class="error-message">
          {{ error }}
        </div>
        <div v-else-if="!questions" class="question-count-selector">
          <h3>选择题目数量</h3>
          <div class="count-options">
            <button @click="generateQuestionsWithCount(5)" class="btn btn-primary">5题</button>
            <button @click="generateQuestionsWithCount(10)" class="btn btn-primary">10题</button>
            <button @click="generateQuestionsWithCount(15)" class="btn btn-primary">15题</button>
          </div>
        </div>
        <div v-else class="interview-content">
          <div class="questions-section">
            <h3>面试问题</h3>
            <div class="question-item">
              <div class="question-header">
                <span class="question-number">问题 {{ currentQuestionIndex + 1 }} / {{ questions.questions.length }}</span>
              </div>
              <div class="question-text">{{ currentQuestion.question }}</div>
              <div class="answer-section">
                <textarea
                  v-model="answers[currentQuestionIndex]"
                  placeholder="请输入你的回答..."
                  class="answer-input"
                  @input="resetFollowUp"
                ></textarea>
              </div>
              <div v-if="followUpQuestions[currentQuestionIndex]" class="follow-up-section">
                <div class="follow-up-header">
                  <span class="follow-up-label">追问：</span>
                </div>
                <div class="follow-up-text">{{ followUpQuestions[currentQuestionIndex] }}</div>
                <div class="follow-up-answer-section">
                  <textarea
                    v-model="followUpAnswers[currentQuestionIndex]"
                    placeholder="请输入你的回答..."
                    class="answer-input"
                  ></textarea>
                </div>
              </div>
            </div>
          </div>

          <div class="question-navigation">
            <button
              @click="prevQuestion"
              class="btn btn-secondary"
              :disabled="currentQuestionIndex === 0"
            >
              上一题
            </button>
            <div class="question-indicators">
              <div
                v-for="(_, index) in questions.questions"
                :key="index"
                :class="['question-indicator', { active: index === currentQuestionIndex }]"
                @click="goToQuestion(Number(index))"
              ></div>
            </div>
            <button
              @click="nextQuestion"
              class="btn btn-secondary"
              :disabled="currentQuestionIndex === questions.questions.length - 1"
            >
              下一题
            </button>
          </div>

          <div class="actions">
            <button @click="goBack" class="btn btn-secondary">
              返回
            </button>
            <button
              v-if="!followUpQuestions[currentQuestionIndex]"
              @click="generateFollowUp"
              class="btn btn-secondary"
              :disabled="!answers[currentQuestionIndex]"
            >
              追问
            </button>
            <button
              v-if="currentQuestionIndex === questions.questions.length - 1"
              @click="submitAnswers"
              class="btn btn-primary"
            >
              提交答案并评估
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
const loading = ref(false)
const loadingText = ref('生成问题中...')
const error = ref('')
const questions = ref<any>(null)
const answers = ref<Record<number, string>>({})
const followUpQuestions = ref<Record<number, string>>({})
const followUpAnswers = ref<Record<number, string>>({})
const currentQuestionIndex = ref(0)

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

const currentQuestion = computed(() => {
  if (!questions.value || !questions.value.questions) return { question: '' }
  return questions.value.questions[currentQuestionIndex.value] || { question: '' }
})

onMounted(() => {
})

const generateQuestions = async (questionCount: number = 10) => {
  try {
    const response = await axios.post(`/interview/${resumeId.value}/questions`, { questionCount }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    questions.value = response.data
    if (response.data.questions && response.data.questions.length > 0) {
      response.data.questions.forEach((_: any, index: number) => {
        answers.value[index] = ''
        followUpQuestions.value[index] = ''
        followUpAnswers.value[index] = ''
      })
    } else {
      error.value = '生成问题失败：AI未能生成任何面试问题'
    }
  } catch (err: any) {
    const errMsg = err.response?.data?.error || err.response?.data?.message || '生成问题失败'
    if (errMsg.includes('简历') || errMsg.includes('未找到') || err.response?.status === 404) {
      error.value = '请先上传简历后再使用面试助手'
    } else {
      error.value = errMsg
    }
  } finally {
    loading.value = false
  }
}

const generateQuestionsWithCount = async (count: number) => {
  loading.value = true
  loadingText.value = `生成${count}个问题中...`
  error.value = ''
  await generateQuestions(count)
}

const generateFollowUp = async () => {
  if (!answers.value[currentQuestionIndex.value]) return

  loadingText.value = '生成追问中...'
  loading.value = true
  error.value = ''

  try {
    const response = await axios.post(`/interview/${resumeId.value}/follow-up`, {
      questionIndex: currentQuestionIndex.value,
      answer: answers.value[currentQuestionIndex.value]
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })

    if (response.data && response.data.followUpQuestion) {
      followUpQuestions.value[currentQuestionIndex.value] = response.data.followUpQuestion
    }
  } catch (err: any) {
    error.value = err.response?.data?.error || '生成追问失败'
  } finally {
    loading.value = false
  }
}

const resetFollowUp = () => {
  followUpQuestions.value[currentQuestionIndex.value] = ''
  followUpAnswers.value[currentQuestionIndex.value] = ''
}

const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

const nextQuestion = () => {
  if (questions.value && currentQuestionIndex.value < questions.value.questions.length - 1) {
    currentQuestionIndex.value++
  }
}

const goToQuestion = (index: number) => {
  currentQuestionIndex.value = index
}

const submitAnswers = async () => {
  loadingText.value = '评估中...'
  loading.value = true
  error.value = ''

  try {
    const response = await axios.post(`/interview/${resumeId.value}/submit`, {
      answers: answers.value,
      followUpAnswers: followUpAnswers.value
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })

    if (response.data) {
      router.push(`/interview/result/${resumeId.value}`)
    }
  } catch (err: any) {
    error.value = err.response?.data?.error || '评估失败'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/history')
}


</script>

<style scoped>
.interview-page {
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

.interview-content,
.question-count-selector {
  padding: 1.5rem;
}

.question-count-selector h3 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #333;
}

.count-options {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.questions-section {
  margin-bottom: 2rem;
}

h3 {
  margin-bottom: 1.5rem;
  color: #495057;
}

.question-item {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.question-number {
  font-weight: bold;
  color: #007bff;
}

.question-text {
  margin-bottom: 1rem;
  line-height: 1.5;
}

.answer-input {
  width: 100%;
  min-height: 150px;
  padding: 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
  font-family: inherit;
  font-size: 1rem;
}

.follow-up-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #dee2e6;
}

.follow-up-header {
  margin-bottom: 0.5rem;
}

.follow-up-label {
  font-weight: bold;
  color: #007bff;
}

.follow-up-text {
  margin-bottom: 1rem;
  line-height: 1.5;
  background-color: #e3f2fd;
  padding: 1rem;
  border-radius: 4px;
}

.follow-up-answer-section {
  margin-top: 1rem;
}

.question-navigation {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 2rem 0;
  gap: 1rem;
}

.question-indicators {
  display: flex;
  gap: 0.5rem;
}

.question-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: #dee2e6;
  cursor: pointer;
  transition: all 0.2s;
}

.question-indicator:hover {
  background-color: #adb5bd;
}

.question-indicator.active {
  background-color: #007bff;
  width: 16px;
  height: 16px;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

button {
  padding: 0.75rem 1.5rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

button:hover {
  background-color: #0069d9;
}

button:disabled {
  background-color: #adb5bd;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #6c757d;
}

.btn-secondary:hover:not(:disabled) {
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