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
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in resumeHistory" :key="item.id" class="resume-row">
              <td>{{ item.id }}</td>
              <td>{{ item.userId || '-' }}</td>
              <td>{{ item.resumeId }}</td>
              <td>{{ getPositionTypeText(item.positionType) }}</td>
              <td>
                <span :class="['status-badge', getStatusClass(item.status)]">
                  {{ getStatusText(item.status) }}
                </span>
              </td>
              <td>{{ item.resumeOverallScore || '-' }}</td>
              <td>{{ item.evaluationOverallScore || '-' }}</td>
              <td>{{ item.questionCount || 0 }}</td>
              <td>{{ formatDate(item.createdAt) }}</td>
              <td>
                <button @click="viewDetail(item.resumeId)" class="btn btn-sm btn-primary">查看详情</button>
              </td>
            </tr>
          </tbody>
        </table>
        
        <div v-if="resumeHistory.length === 0" class="empty-state">
          <p>暂无简历分析历史数据</p>
        </div>
      </div>
    </div>

    <div v-if="showDetail" class="modal-overlay" @click.self="closeDetail">
      <div class="modal">
        <button @click="closeDetail" class="floating-close-btn" aria-label="close">&times;</button>
        <div class="modal-header">
          <h3>简历详情</h3>
        </div>
        <div class="modal-body">
          <div class="detail-item">
            <label>简历ID:</label>
            <span>{{ detailData.resumeId }}</span>
          </div>
          <div class="detail-item">
            <label>用户ID:</label>
            <span>{{ detailData.userId }}</span>
          </div>
          <div class="detail-item">
            <label>职位类型:</label>
            <span>{{ getPositionTypeText(detailData.positionType) }}</span>
          </div>
          <div class="detail-item">
            <label>状态:</label>
            <span :class="['status-badge', getStatusClass(detailData.status)]">
              {{ getStatusText(detailData.status) }}
            </span>
          </div>
          <div class="detail-item">
            <label>简历评分:</label>
            <span>{{ detailData.resumeOverallScore || '-' }}</span>
          </div>
          <div class="detail-item">
            <label>面试评分:</label>
            <span>{{ detailData.evaluationOverallScore || '-' }}</span>
          </div>
          <div class="detail-item">
            <label>创建时间:</label>
            <span>{{ formatDate(detailData.createdAt) }}</span>
          </div>

          <div v-if="detailData.scoreResult" class="detail-section">
            <h4>简历评分详情</h4>
            <div class="detail-item">
              <label>综合评分:</label>
              <span>{{ detailData.scoreResult.overallScore || '-' }}</span>
            </div>
            <div v-if="detailData.scoreResult.scoreDetail" class="sub-section">
              <h5>评分明细</h5>
              <div class="score-grid">
                <div class="score-card">
                  <div class="score-title">项目评分</div>
                  <div class="score-value">{{ detailData.scoreResult.scoreDetail.projectScore ?? '-' }}</div>
                </div>
                <div class="score-card">
                  <div class="score-title">技能匹配</div>
                  <div class="score-value">{{ detailData.scoreResult.scoreDetail.skillMatchScore ?? '-' }}</div>
                </div>
                <div class="score-card">
                  <div class="score-title">内容质量</div>
                  <div class="score-value">{{ detailData.scoreResult.scoreDetail.contentScore ?? '-' }}</div>
                </div>
                <div class="score-card">
                  <div class="score-title">结构评分</div>
                  <div class="score-value">{{ detailData.scoreResult.scoreDetail.structureScore ?? '-' }}</div>
                </div>
                <div class="score-card">
                  <div class="score-title">表达评分</div>
                  <div class="score-value">{{ detailData.scoreResult.scoreDetail.expressionScore ?? '-' }}</div>
                </div>
              </div>
            </div>
            <div v-if="detailData.scoreResult.summary" class="detail-item">
              <label>简历摘要:</label>
              <span class="multiline">{{ detailData.scoreResult.summary }}</span>
            </div>
            <div v-if="detailData.scoreResult.strengths?.length" class="sub-section">
              <h5>优势</h5>
              <ul class="strength-list">
                <li v-for="(strength, index) in detailData.scoreResult.strengths" :key="index">{{ strength }}</li>
              </ul>
            </div>
            <div v-if="detailData.scoreResult.suggestions?.length" class="sub-section">
              <h5>改进建议</h5>
              <div v-for="(suggestion, index) in detailData.scoreResult.suggestions" :key="index" class="suggestion-item">
                <div class="suggestion-category">{{ suggestion.category }} - {{ suggestion.priority }}</div>
                <div class="suggestion-issue">{{ suggestion.issue }}</div>
                <div class="suggestion-recommendation">{{ suggestion.recommendation }}</div>
              </div>
            </div>
          </div>

          <div v-if="detailData.resumeText" class="detail-section">
            <h4>简历原文</h4>
            <div class="resume-text">{{ detailData.resumeText }}</div>
          </div>
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
const showDetail = ref(false)
const detailData = ref<any>({})

const loadResumeHistory = async () => {
  resumeHistoryLoading.value = true
  resumeHistoryError.value = ''
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/admin/resume-history', {
      headers: { Authorization: `Bearer ${token}` }
    })
    const payload = response.data
    resumeHistory.value = Array.isArray(payload)
      ? payload
      : (Array.isArray(payload?.content) ? payload.content : [])
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
    let message = '未知错误'
    const responseData = err?.response?.data
    if (responseData instanceof Blob) {
      try {
        const text = await responseData.text()
        const parsed = JSON.parse(text)
        message = parsed?.error || text || message
      } catch {
        message = err?.message || message
      }
    } else {
      message = responseData?.error || err?.message || message
    }
    alert(`导出失败: ${message}`)
  }
}

const viewDetail = async (resumeId: string) => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get(`/api/admin/resume-history/${resumeId}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    detailData.value = response.data.data || response.data
    showDetail.value = true
  } catch (err: any) {
    alert('加载详情失败: ' + (err.response?.data?.error || '未知错误'))
  }
}

const closeDetail = () => {
  showDetail.value = false
}

const getStatusClass = (status: string) => {
  const statusMap: Record<string, string> = {
    'ANALYZED': 'analyzed',
    'QUESTIONS_READY': 'questions-ready',
    'EVALUATED': 'evaluated'
  }
  return statusMap[status] || 'default'
}

const getPositionTypeText = (positionType: string) => {
  const textMap: Record<string, string> = {
    'BACKEND_JAVA': '\u540e\u7aef Java',
    'FRONTEND': '\u524d\u7aef',
    'ALGORITHM': '\u7b97\u6cd5'
  }
  return textMap[positionType] || positionType || '-'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'ANALYZED': '\u5df2\u5206\u6790',
    'QUESTIONS_READY': '\u5df2\u751f\u6210\u95ee\u9898',
    'EVALUATED': '\u5df2\u8bc4\u4f30'
  }
  return textMap[status] || status || '-'
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

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.8rem;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(10, 20, 32, 0.52);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal {
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border-radius: 16px;
  border: 1px solid #dce7f2;
  width: 920px;
  max-width: 92%;
  max-height: 85vh;
  overflow: hidden;
  position: relative;
  display: flex;
  flex-direction: column;
  box-shadow: 0 24px 56px rgba(11, 36, 63, 0.25);
}

.modal-header {
  padding: 1.1rem 1.5rem;
  border-bottom: 1px solid #e7eef6;
  background: linear-gradient(90deg, #ffffff 0%, #eef5ff 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 2;
}

.modal-header h3 {
  margin: 0;
  color: #123252;
  font-size: 1.45rem;
  letter-spacing: 0.4px;
}

.floating-close-btn {
  position: absolute;
  top: 0.9rem;
  right: 0.9rem;
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 50%;
  background: #ffffff;
  color: #37526f;
  font-size: 1.3rem;
  line-height: 1;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(25, 61, 98, 0.24);
  z-index: 4;
  transition: all 0.2s ease;
}

.floating-close-btn:hover {
  background: #f1f6ff;
  color: #0e2740;
  transform: translateY(-1px);
}

.modal-body {
  padding: 1.25rem 1.5rem 1.5rem;
  overflow: auto;
  max-height: calc(85vh - 74px);
}

.detail-item {
  display: flex;
  padding: 0.8rem 0.9rem;
  margin-bottom: 0.55rem;
  border: 1px solid #eaf1f8;
  border-radius: 10px;
  background: #ffffff;
}

.detail-item label {
  width: 128px;
  color: #4d6782;
  font-weight: 500;
  flex-shrink: 0;
}

.detail-item span {
  flex: 1;
  color: #2c3e50;
}

.multiline {
  white-space: pre-wrap;
}

.detail-section {
  margin-top: 1.3rem;
  padding: 1rem;
  border-radius: 12px;
  background: #f2f7fd;
  border: 1px solid #ddeaf8;
}

.detail-section h4 {
  margin: 0 0 0.9rem 0;
  color: #13385c;
  font-size: 1.12rem;
}

.sub-section {
  margin-top: 1.05rem;
  padding-top: 0.2rem;
}

.sub-section h5 {
  margin: 0 0 0.6rem 0;
  color: #2a4f73;
}

.score-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 0.6rem;
}

.score-card {
  background: #f8f9fa;
  border: 1px solid #edf0f3;
  border-radius: 8px;
  padding: 0.6rem 0.7rem;
}

.score-title {
  color: #666;
  font-size: 0.82rem;
}

.score-value {
  color: #2c3e50;
  font-size: 1.05rem;
  font-weight: 600;
  margin-top: 0.2rem;
}

.strength-list {
  margin: 0;
  padding-left: 1.5rem;
  color: #2c3e50;
}

.strength-list li {
  margin-bottom: 0.5rem;
}

.suggestion-item {
  background: #ffffff;
  border: 1px solid #eaf1f8;
  border-radius: 8px;
  padding: 0.75rem;
  margin-bottom: 0.75rem;
}

.suggestion-category {
  font-weight: 600;
  color: #2a4f73;
  margin-bottom: 0.3rem;
}

.suggestion-issue {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 0.3rem;
}

.suggestion-recommendation {
  color: #2c3e50;
  font-size: 0.9rem;
}

.resume-text {
  background: #ffffff;
  border: 1px solid #eaf1f8;
  border-radius: 8px;
  padding: 1rem;
  white-space: pre-wrap;
  max-height: 300px;
  overflow: auto;
  color: #2c3e50;
  line-height: 1.6;
}
</style>
