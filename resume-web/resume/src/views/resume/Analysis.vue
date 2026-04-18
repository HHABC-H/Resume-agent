<template>
  <div class="analysis-container">
    <h2>简历分析结果</h2>
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

const resumeId = computed(() => route.params.resumeId as string)

onMounted(async () => {
  await loadAnalysis()
})

const loadAnalysis = async () => {
  try {
    const response = await axios.get(`/api/resume/analysis/${resumeId.value}`, {
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
.analysis-container {
  max-width: 1200px;
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
  margin-top: 1rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-layout {
    flex-direction: column;
  }
  
  .resume-section,
  .analysis-section {
    min-width: 100%;
  }
}
</style>