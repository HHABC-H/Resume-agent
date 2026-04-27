<template>
  <div class="interview-management">
    <div class="page-header">
      <h2>面试管理</h2>
      <p>查看和管理所有用户的面试历史</p>
    </div>

    <div class="card">
      <div class="card-header">
        <h3>所有用户面试历史</h3>
        <div class="pagination">
          <button @click="prevPage" :disabled="currentPage === 0" class="btn btn-sm">上一页</button>
          <span>{{ currentPage + 1 }} / {{ totalPages || 1 }}</span>
          <button @click="nextPage" :disabled="currentPage >= totalPages - 1" class="btn btn-sm">下一页</button>
        </div>
      </div>

      <div v-if="interviewHistoryLoading" class="loading">加载中...</div>
      <div v-else-if="interviewHistoryError" class="error-message">{{ interviewHistoryError }}</div>
      <div v-else class="table-wrapper">
        <table class="interview-history-table">
          <thead>
            <tr>
              <th>简历ID</th>
              <th>用户ID</th>
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
            <tr v-for="item in interviewHistory" :key="item.resumeId" class="interview-row">
              <td>{{ item.resumeId }}</td>
              <td>{{ item.userId || '-' }}</td>
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

        <div v-if="interviewHistory.length === 0" class="empty-state">
          <p>暂无面试历史数据</p>
        </div>
      </div>
    </div>

    <div v-if="showDetail" class="modal-overlay" @click.self="closeDetail">
      <div class="modal">
        <button @click="closeDetail" class="floating-close-btn" aria-label="close">&times;</button>
        <div class="modal-header">
          <h3>面试详情</h3>
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
            <label>问题数量:</label>
            <span>{{ detailData.questionCount || 0 }}</span>
          </div>
          <div class="detail-item">
            <label>创建时间:</label>
            <span>{{ formatDate(detailData.createdAt) }}</span>
          </div>
          <div class="detail-item">
            <label>更新时间:</label>
            <span>{{ formatDate(detailData.updatedAt) }}</span>
          </div>

          <div class="detail-section" v-if="detailData.evaluation">
            <h4>面试评估结果</h4>
            <div class="detail-item">
              <label>总分:</label>
              <span>{{ detailData.evaluation.overallScore || '-' }}</span>
            </div>
            <div class="detail-item">
              <label>总体反馈:</label>
              <span class="multiline">{{ detailData.evaluation.overallFeedback || '-' }}</span>
            </div>

            <div v-if="detailData.evaluation.categoryScores?.length" class="sub-section">
              <h5>分类得分</h5>
              <div class="score-grid">
                <div v-for="(item, index) in detailData.evaluation.categoryScores" :key="index" class="score-card">
                  <div class="score-title">{{ item.category || '-' }}</div>
                  <div class="score-value">{{ item.score ?? '-' }}</div>
                </div>
              </div>
            </div>

            <div class="sub-section">
              <h5>问题 / 用户答案 / 评语</h5>
              <div
                v-for="(item, index) in sortedQuestionDetails(detailData.evaluation.questionDetails || [])"
                :key="index"
                class="qa-card"
              >
                <div class="qa-header">
                  <span>问题 {{ (item.questionIndex ?? index) + 1 }}</span>
                  <span>得分: {{ item.score ?? '-' }}</span>
                </div>
                <div class="qa-line">
                  <label>问题:</label>
                  <p>{{ item.question || '-' }}</p>
                </div>
                <div class="qa-line">
                  <label>用户答案:</label>
                  <p>{{ item.userAnswer || '-' }}</p>
                </div>
                <div class="qa-line">
                  <label>评语:</label>
                  <p>{{ item.feedback || '-' }}</p>
                </div>
              </div>
              <div v-if="!detailData.evaluation.questionDetails?.length" class="empty-subsection">
                暂无问题明细
              </div>
            </div>
          </div>
          <div v-else class="empty-subsection">该记录暂无面试评估结果</div>
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
const currentPage = ref(0)
const pageSize = ref(20)
const totalPages = ref(0)

const showDetail = ref(false)
const detailData = ref<any>({})

const loadInterviewHistory = async (page: number = 0) => {
  interviewHistoryLoading.value = true
  interviewHistoryError.value = ''
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/admin/interview-history', {
      headers: { Authorization: `Bearer ${token}` },
      params: { page, size: pageSize.value }
    })
    interviewHistory.value = response.data.content || []
    totalPages.value = response.data.totalPages || 0
    currentPage.value = response.data.currentPage || 0
  } catch (err: any) {
    interviewHistoryError.value = err.response?.data?.error || '加载面试历史失败'
  } finally {
    interviewHistoryLoading.value = false
  }
}

const prevPage = () => {
  if (currentPage.value > 0) {
    loadInterviewHistory(currentPage.value - 1)
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    loadInterviewHistory(currentPage.value + 1)
  }
}

const viewDetail = async (resumeId: string) => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get(`/admin/interview-history/${resumeId}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    detailData.value = response.data
    showDetail.value = true
  } catch (err: any) {
    alert('加载详情失败: ' + (err.response?.data?.error || '未知错误'))
  }
}

const closeDetail = () => {
  showDetail.value = false
}

const sortedQuestionDetails = (questionDetails: any[]) => {
  return [...(questionDetails || [])].sort(
    (a, b) => (a?.questionIndex ?? 0) - (b?.questionIndex ?? 0)
  )
}

const getStatusClass = (status: string) => {
  const statusMap: Record<string, string> = {
    ANALYZED: 'analyzed',
    QUESTIONS_READY: 'questions-ready',
    EVALUATED: 'evaluated'
  }
  return statusMap[status] || 'default'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    ANALYZED: '已分析',
    QUESTIONS_READY: '已生成问题',
    EVALUATED: '已评估'
  }
  return textMap[status] || status || '-'
}

const getPositionTypeText = (positionType: string) => {
  const textMap: Record<string, string> = {
    BACKEND_JAVA: '后端 Java',
    FRONTEND: '前端',
    ALGORITHM: '算法'
  }
  return textMap[positionType] || positionType || '-'
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

.pagination {
  display: flex;
  align-items: center;
  gap: 1rem;
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
  font-size: 0.8rem;
}

.btn-primary {
  background-color: #3498db;
  color: white;
}

.btn-primary:hover {
  background-color: #2980b9;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #999;
}

.close-btn:hover {
  color: #333;
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

.qa-card {
  border: 1px solid #dae7f6;
  border-radius: 12px;
  background: #ffffff;
  padding: 0.9rem;
  margin-bottom: 0.75rem;
  box-shadow: 0 4px 14px rgba(23, 68, 111, 0.06);
}

.qa-header {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #2c3e50;
}

.qa-line {
  margin-bottom: 0.45rem;
}

.qa-line:last-child {
  margin-bottom: 0;
}

.qa-line label {
  display: block;
  color: #666;
  font-size: 0.82rem;
  margin-bottom: 0.2rem;
}

.qa-line p {
  margin: 0;
  color: #2c3e50;
  white-space: pre-wrap;
  line-height: 1.5;
}

.empty-subsection {
  color: #666;
  padding: 0.4rem 0;
}

@media (max-width: 768px) {
  .interview-management {
    padding: 1rem;
  }

  .card-header {
    flex-direction: column;
    gap: 1rem;
  }

  .detail-item {
    flex-direction: column;
    gap: 0.3rem;
  }

  .detail-item label {
    width: auto;
  }

  .modal {
    width: 96%;
    max-height: 88vh;
  }

  .floating-close-btn {
    top: 0.7rem;
    right: 0.7rem;
  }
}
</style>
