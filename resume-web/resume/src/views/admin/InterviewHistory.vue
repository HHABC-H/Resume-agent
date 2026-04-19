<template>
  <div class="interview-management">
    <div class="page-header">
      <h2>面试管理</h2>
      <p>查看和管理所有用户的面试历史</p>
    </div>
    
    <div class="card">
      <div class="card-header">
        <h3>所有用户面试历史</h3>
      </div>
      
      <div v-if="interviewHistoryLoading" class="loading">加载中...</div>
      <div v-else-if="interviewHistoryError" class="error-message">{{ interviewHistoryError }}</div>
      <div v-else class="table-wrapper">
        <table class="interview-history-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户ID</th>
              <th>简历ID</th>
              <th>职位类型</th>
              <th>状态</th>
              <th>面试评分</th>
              <th>问题数量</th>
              <th>创建时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in interviewHistory" :key="item.id" class="interview-row">
              <td>{{ item.id }}</td>
              <td>{{ item.userId || '-' }}</td>
              <td>{{ item.resumeId }}</td>
              <td>{{ item.positionType }}</td>
              <td>
                <span :class="['status-badge', getStatusClass(item.status)]">
                  {{ item.status }}
                </span>
              </td>
              <td>{{ item.evaluationOverallScore || '-' }}</td>
              <td>{{ item.questionCount || 0 }}</td>
              <td>{{ formatDate(item.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
        
        <div v-if="interviewHistory.length === 0" class="empty-state">
          <p>暂无面试历史数据</p>
        </div>
      </div>
    </div>
    
    <div class="card mt-4">
      <div class="card-header">
        <h3>面试模板和问题库</h3>
      </div>
      
      <div v-if="interviewTemplatesLoading" class="loading">加载中...</div>
      <div v-else-if="interviewTemplatesError" class="error-message">{{ interviewTemplatesError }}</div>
      <div v-else class="templates-content">
        <div v-if="interviewTemplates.message" class="message">
          {{ interviewTemplates.message }}
        </div>
        <div v-else>
          <div class="templates-list">
            <!-- 这里可以添加面试模板的具体内容 -->
            <div class="template-item">
              <h4>Java后端开发面试模板</h4>
              <p>包含Java基础、Spring框架、数据库等相关问题</p>
              <button class="btn btn-sm btn-primary">编辑</button>
            </div>
            <div class="template-item">
              <h4>前端开发面试模板</h4>
              <p>包含HTML、CSS、JavaScript、框架等相关问题</p>
              <button class="btn btn-sm btn-primary">编辑</button>
            </div>
            <div class="template-item">
              <h4>算法工程师面试模板</h4>
              <p>包含算法、数据结构、机器学习等相关问题</p>
              <button class="btn btn-sm btn-primary">编辑</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

const interviewHistory = ref<any[]>([])
const interviewHistoryLoading = ref(false)
const interviewHistoryError = ref('')

const interviewTemplates = ref<any[]>([])
const interviewTemplatesLoading = ref(false)
const interviewTemplatesError = ref('')

const loadInterviewHistory = async () => {
  interviewHistoryLoading.value = true
  interviewHistoryError.value = ''
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/admin/interview-history', {
      headers: { Authorization: `Bearer ${token}` }
    })
    interviewHistory.value = response.data
  } catch (err: any) {
    interviewHistoryError.value = err.response?.data?.error || '加载面试历史失败'
  } finally {
    interviewHistoryLoading.value = false
  }
}

const loadInterviewTemplates = async () => {
  interviewTemplatesLoading.value = true
  interviewTemplatesError.value = ''
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/admin/interview-templates', {
      headers: { Authorization: `Bearer ${token}` }
    })
    interviewTemplates.value = response.data
  } catch (err: any) {
    interviewTemplatesError.value = err.response?.data?.error || '加载面试模板失败'
  } finally {
    interviewTemplatesLoading.value = false
  }
}

const getStatusClass = (status: string) => {
  const statusMap: Record<string, string> = {
    'ANALYZED': 'analyzed',
    'QUESTIONS_READY': 'questions-ready',
    'EVALUATED': 'evaluated'
  }
  return statusMap[status] || 'default'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadInterviewHistory()
  loadInterviewTemplates()
})
</script>

<style scoped>
.interview-management {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.page-header {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #f0f0f0;
}

.page-header h2 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1.8rem;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 1rem;
}

.card {
  background: #f9f9f9;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 1.5rem;
}

.card.mt-4 {
  margin-top: 2rem;
}

.card-header {
  background: white;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.2rem;
  font-weight: 600;
}

.loading, .error-message {
  padding: 4rem 0;
  text-align: center;
}

.error-message {
  background-color: #f8d7da;
  color: #721c24;
  padding: 1rem;
  border-radius: 6px;
  margin: 1rem;
}

.table-wrapper {
  overflow-x: auto;
  background: white;
}

.interview-history-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.interview-history-table th,
.interview-history-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.interview-history-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
  white-space: nowrap;
}

.interview-history-table tbody tr:hover {
  background-color: #f8f9fa;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-badge.analyzed {
  background-color: #e3f2fd;
  color: #1976d2;
}

.status-badge.questions-ready {
  background-color: #fff3e0;
  color: #e65100;
}

.status-badge.evaluated {
  background-color: #e8f5e8;
  color: #2e7d32;
}

.status-badge.default {
  background-color: #f5f5f5;
  color: #666;
}

.empty-state {
  padding: 4rem 0;
  text-align: center;
  color: #666;
}

.templates-content {
  padding: 2rem;
  background: white;
}

.message {
  padding: 2rem;
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  text-align: center;
  color: #666;
}

.templates-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.template-item {
  background: #f8f9fa;
  padding: 1.5rem;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.template-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.template-item h4 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1.1rem;
  font-weight: 600;
}

.template-item p {
  margin: 0 0 1rem 0;
  color: #666;
  font-size: 0.9rem;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.75rem;
}

.btn-primary {
  background-color: #3498db;
  color: white;
}

.btn-primary:hover {
  background-color: #2980b9;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .interview-management {
    padding: 1rem;
  }
  
  .templates-list {
    grid-template-columns: 1fr;
  }
}
</style>