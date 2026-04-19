<template>
  <div class="interview-container">
    <div class="header">
      <button @click="goBack" class="btn btn-secondary">
        返回
      </button>
      <h2>模拟面试</h2>
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
          <!-- 追问部分 -->
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
      
      <!-- 问题导航 -->
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
            @click="goToQuestion(index)"
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
      
      <!-- 操作按钮 -->
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

const resumeId = computed(() => route.params.resumeId as string)

// 当前问题
const currentQuestion = computed(() => {
  if (!questions.value || !questions.value.questions) return { question: '' }
  return questions.value.questions[currentQuestionIndex.value] || { question: '' }
})

onMounted(() => {
  // 不自动生成问题，让用户选择题目数量后再生成
})

const token = localStorage.getItem('token')

const generateQuestions = async (questionCount: number = 10) => {
  console.log('开始生成问题，resumeId:', resumeId.value, '题目数量:', questionCount)
  console.log('Token:', token)
  try {
    console.log('发送请求到:', `/interview/${resumeId.value}/questions`)
    const response = await axios.post(`/interview/${resumeId.value}/questions`, { questionCount }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    console.log('请求成功，响应:', response.data)
    questions.value = response.data
    // 初始化答案对象
    if (response.data.questions && response.data.questions.length > 0) {
      response.data.questions.forEach((_: any, index: number) => {
        answers.value[index] = ''
        followUpQuestions.value[index] = ''
        followUpAnswers.value[index] = ''
      })
    } else {
      console.error('生成的问题数组为空')
      error.value = '生成问题失败：AI未能生成任何面试问题'
    }
  } catch (err: any) {
    console.error('生成问题失败:', err)
    error.value = err.response?.data?.error || '生成问题失败'
  } finally {
    console.log('生成问题完成，loading 设置为 false')
    loading.value = false
  }
}

const generateQuestionsWithCount = async (count: number) => {
  loading.value = true
  loadingText.value = `生成${count}个问题中...`
  error.value = ''
  await generateQuestions(count)
}

// 生成追问
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
    console.error('生成追问失败:', err)
    error.value = err.response?.data?.error || '生成追问失败'
  } finally {
    loading.value = false
  }
}

// 重置追问
const resetFollowUp = () => {
  followUpQuestions.value[currentQuestionIndex.value] = ''
  followUpAnswers.value[currentQuestionIndex.value] = ''
}

// 上一题
const prevQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

// 下一题
const nextQuestion = () => {
  if (questions.value && currentQuestionIndex.value < questions.value.questions.length - 1) {
    currentQuestionIndex.value++
  }
}

// 跳转到指定问题
const goToQuestion = (index: number) => {
  currentQuestionIndex.value = index
}

// 提交答案
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
.interview-container {
  max-width: 800px;
  margin: 2rem auto;
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header {
  display: flex;
  align-items: center;
  margin-bottom: 2rem;
}

.header h2 {
  flex: 1;
  text-align: center;
  margin: 0;
  color: #333;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #5a6268;
}

.btn-secondary:disabled {
  background-color: #adb5bd;
  cursor: not-allowed;
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

/* 追问部分 */
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

/* 问题导航 */
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

/* 操作按钮 */
.actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 2rem;
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
  margin-top: 1rem;
}
</style>