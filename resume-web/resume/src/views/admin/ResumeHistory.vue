<template>
  <div class="resume-management">
    <div class="page-header">
      <h2>简历管理</h2>
      <p>查看和管理所有用户的简历分析历史</p>
    </div>
    
    <div class="card">
      <div class="card-header">
        <h3>所有用户简历分析历史</h3>
        <button @click="exportResumeHistory" class="btn btn-primary">
          <i>📤</i>
          导出简历分析数据
        </button>
      </div>
      
      <div v-if="resumeHistoryLoading" class="loading">加载中...</div>
      <div v-else-if="resumeHistoryError" class="error-message">{{ resumeHistoryError }}</div>
      <div v-else class="table-wrapper">
        <table class="resume-history-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户ID</th>
              <th>简历ID</th>
              <th>职位类型</th>
              <th>状态</th>
              <th>简历评分</th>
              <th>面试评分</th>
              <th>问题数量</th>
              <th>创建时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in resumeHistory" :key="item.id" class="resume-row">
              <td>{{ item.id }}</td>
              <td>{{ item.userId || '-' }}</td>
              <td>{{ item.resumeId }}</td>
              <td>{{ item.positionType }}</td>
              <td>
                <span :class="['status-badge', getStatusClass(item.status)]">
                  {{ item.status }}
                </span>
              </td>
              <td>{{ item.resumeOverallScore || '-' }}</td>
              <td>{{ item.evaluationOverallScore || '-' }}</td>
              <td>{{ item.questionCount || 0 }}</td>
              <td>{{ formatDate(item.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
        
        <div v-if="resumeHistory.length === 0" class="empty-state">
          <p>暂无简历分析历史数据</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

const resumeHistory = ref<any[]>([])
const resumeHistoryLoading = ref(false)
const resumeHistoryError = ref('')

const loadResumeHistory = async () => {
  resumeHistoryLoading.value = true
  resumeHistoryError.value = ''
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/admin/resume-history', {
      headers: { Authorization: `Bearer ${token}` }
    })
    resumeHistory.value = response.data
  } catch (err: any) {
    resumeHistoryError.value = err.response?.data?.error || '加载简历历史失败'
  } finally {
    resumeHistoryLoading.value = false
  }
}

const exportResumeHistory = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/admin/resume-history/export', {
      headers: { Authorization: `Bearer ${token}` },
      responseType: 'blob'
    })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'resume-history.csv')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (err: any) {
    alert('导出失败: ' + (err.response?.data?.error || '未知错误'))
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
  loadResumeHistory()
})
</script>

<style scoped>
.resume-management {
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

.btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background-color: #3498db;
  color: white;
}

.btn-primary:hover {
  background-color: #2980b9;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(52, 152, 219, 0.2);
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

.resume-history-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.resume-history-table th,
.resume-history-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.resume-history-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
  white-space: nowrap;
}

.resume-history-table tbody tr:hover {
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

/* 响应式设计 */
@media (max-width: 768px) {
  .resume-management {
    padding: 1rem;
  }
  
  .card-header {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .btn {
    justify-content: center;
  }
}
</style>