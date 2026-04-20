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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

const interviewHistory = ref<any[]>([])
const interviewHistoryLoading = ref(false)
const interviewHistoryError = ref('')

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

/* 响应式设计 */
@media (max-width: 768px) {
  .interview-management {
    padding: 1rem;
  }
}
</style>