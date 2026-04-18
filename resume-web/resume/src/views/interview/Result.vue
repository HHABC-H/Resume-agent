<template>
  <div class="result-container">
    <h2>面试评估结果</h2>
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
          <div v-for="(score, category) in evaluation.categoryScores" :key="category" class="score-item">
            <span>{{ category }}:</span>
            <span>{{ score }}</span>
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
        <div v-for="(item, index) in evaluation.questionEvaluations" :key="index" class="answer-item">
          <div class="answer-header">
            <span class="question-number">问题 {{ (index as number) + 1 }}</span>
            <span class="answer-score">得分: {{ item.score }}</span>
          </div>
          <div class="question-text">{{ item.question }}</div>
          <div class="user-answer">
            <strong>你的回答:</strong>
            <p>{{ item.answer }}</p>
          </div>
          <div class="evaluation-text">
            <strong>评估:</strong>
            <p>{{ item.evaluation }}</p>
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

const resumeId = computed(() => route.params.resumeId as string)

onMounted(async () => {
  await loadEvaluation()
})

const loadEvaluation = async () => {
  try {
    const response = await axios.get(`/api/interview/result/${resumeId.value}`)
    evaluation.value = response.data
  } catch (err: any) {
    error.value = err.response?.data?.message || '加载评估结果失败'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/resume/upload')
}
</script>

<style scoped>
.result-container {
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
  margin-top: 1rem;
}
</style>