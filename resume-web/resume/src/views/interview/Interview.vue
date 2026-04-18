<template>
  <div class="interview-container">
    <h2>模拟面试</h2>
    <div v-if="loading" class="loading">
      生成问题中...
    </div>
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>
    <div v-else-if="questions" class="interview-content">
      <div class="questions-section">
        <h3>面试问题</h3>
        <div v-for="(question, index) in questions.questions" :key="index" class="question-item">
          <div class="question-header">
            <span class="question-number">问题 {{ (index as number) + 1 }}</span>
          </div>
          <div class="question-text">{{ question.question }}</div>
          <div class="answer-section">
            <textarea 
              v-model="answers[index as number]" 
              placeholder="请输入你的回答..."
              class="answer-input"
            ></textarea>
          </div>
        </div>
      </div>
      
      <div class="actions">
        <button @click="submitAnswers" class="btn btn-primary">
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
const loading = ref(true)
const error = ref('')
const questions = ref<any>(null)
const answers = ref<Record<number, string>>({})

const resumeId = computed(() => route.params.resumeId as string)

onMounted(async () => {
  await generateQuestions()
})

const generateQuestions = async () => {
  try {
    const response = await axios.get(`/api/interview/questions/${resumeId.value}`)
    questions.value = response.data
    // 初始化答案对象
    response.data.questions.forEach((_: any, index: number) => {
      answers.value[index] = ''
    })
  } catch (err: any) {
    error.value = err.response?.data?.message || '生成问题失败'
  } finally {
    loading.value = false
  }
}

const submitAnswers = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await axios.post(`/api/interview/evaluate/${resumeId.value}`, {
      answers: answers.value
    })
    
    if (response.data) {
      router.push(`/interview/result/${resumeId.value}`)
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || '评估失败'
  } finally {
    loading.value = false
  }
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

h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
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

.actions {
  display: flex;
  justify-content: center;
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
}

button:hover {
  background-color: #0069d9;
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