<template>
  <div class="resume-management">
    <div class="page-header">
      <h2>简历管理</h2>
      <p>查看和管理所有用户的简历原文</p>
    </div>

    <div class="card">
      <div class="card-header">
        <h3>所有用户简历列表</h3>
        <div class="header-actions">
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索用户名、显示名称或简历内容..."
            class="search-input"
            @input="handleSearch"
          />
          <button @click="exportResumes" class="btn btn-primary">
            <i>📤</i>
            导出简历数据
          </button>
          <div class="pagination">
            <button @click="prevPage" :disabled="currentPage === 0" class="btn btn-sm">上一页</button>
            <span>{{ currentPage + 1 }} / {{ totalPages || 1 }}</span>
            <button @click="nextPage" :disabled="currentPage >= totalPages - 1" class="btn btn-sm">下一页</button>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="error" class="error-message">{{ error }}</div>
      <div v-else class="table-wrapper">
        <table class="resume-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户ID</th>
              <th>用户名</th>
              <th>显示名称</th>
              <th>邮箱</th>
              <th>简历ID</th>
              <th>岗位类型</th>
              <th>简历评分</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in resumes" :key="item.id" class="resume-row">
              <td>{{ item.id }}</td>
              <td>{{ item.userId || '-' }}</td>
              <td>{{ item.username || '-' }}</td>
              <td>{{ item.displayName || '-' }}</td>
              <td>{{ item.email || '-' }}</td>
              <td class="resume-id">{{ item.resumeId }}</td>
              <td>{{ getPositionTypeText(item.positionType) }}</td>
              <td>{{ item.resumeOverallScore || '-' }}</td>
              <td>{{ formatDate(item.createdAt) }}</td>
              <td>
                <button @click="viewDetail(item)" class="btn btn-sm btn-primary">查看简历</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="resumes.length === 0" class="empty-state">
          <p>暂无简历数据</p>
        </div>
      </div>
    </div>

    <div v-if="showDetail" class="modal-overlay" @click.self="closeDetail">
      <div class="modal">
        <button @click="closeDetail" class="floating-close-btn" aria-label="close">&times;</button>
        <div class="modal-header">
          <h3>简历详情</h3>
          <div class="modal-header-info">
            <span class="info-item">用户: {{ detailData.displayName || detailData.username }}</span>
            <span class="info-item">岗位: {{ getPositionTypeText(detailData.positionType) }}</span>
            <span class="info-item">评分: {{ detailData.resumeOverallScore || '-' }}</span>
          </div>
        </div>
        <div class="modal-body">
          <div class="resume-text-container">
            <pre class="resume-text">{{ detailData.resumeText }}</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

const resumes = ref<any[]>([])
const loading = ref(false)
const error = ref('')
const currentPage = ref(0)
const pageSize = ref(20)
const totalPages = ref(0)
const keyword = ref('')
const searchTimeout = ref<number | null>(null)

const showDetail = ref(false)
const detailData = ref<any>({})

const loadResumes = async (page: number = 0) => {
  loading.value = true
  error.value = ''
  try {
    const token = localStorage.getItem('token')
    const params: any = { page, size: pageSize.value }
    if (keyword.value) {
      params.keyword = keyword.value
    }
    const response = await axios.get('/api/admin/resumes', {
      headers: { Authorization: `Bearer ${token}` },
      params
    })
    resumes.value = response.data.content || []
    totalPages.value = response.data.totalPages || 0
    currentPage.value = response.data.currentPage || 0
  } catch (err: any) {
    error.value = err.response?.data?.error || '加载简历列表失败'
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }
  searchTimeout.value = window.setTimeout(() => {
    loadResumes(0)
  }, 300)
}

const prevPage = () => {
  if (currentPage.value > 0) {
    loadResumes(currentPage.value - 1)
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    loadResumes(currentPage.value + 1)
  }
}

const exportResumes = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/admin/resumes/export', {
      headers: { Authorization: `Bearer ${token}` },
      responseType: 'blob'
    })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'resumes.csv')
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

const viewDetail = (item: any) => {
  detailData.value = item
  showDetail.value = true
}

const closeDetail = () => {
  showDetail.value = false
  detailData.value = {}
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
  loadResumes()
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
  flex-wrap: wrap;
  gap: 1rem;
}

.card-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.2rem;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.search-input {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  min-width: 250px;
}

.search-input:focus {
  outline: none;
  border-color: #3498db;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 0.5rem;
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

.resume-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.resume-table th,
.resume-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.resume-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
  white-space: nowrap;
}

.resume-table tbody tr:hover {
  background-color: #f8f9fa;
}

.resume-id {
  font-family: monospace;
  font-size: 0.85rem;
  color: #666;
}

.empty-state {
  padding: 4rem 0;
  text-align: center;
  color: #666;
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

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.8rem;
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
  flex-wrap: wrap;
  gap: 0.5rem;
}

.modal-header h3 {
  margin: 0;
  color: #123252;
  font-size: 1.45rem;
  letter-spacing: 0.4px;
}

.modal-header-info {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.info-item {
  background: #e8f4fd;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.85rem;
  color: #2c3e50;
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
  max-height: calc(85vh - 80px);
}

.resume-text-container {
  background: #ffffff;
  border: 1px solid #eaf1f8;
  border-radius: 12px;
  overflow: hidden;
}

.resume-text {
  margin: 0;
  padding: 1.5rem;
  font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif;
  font-size: 0.95rem;
  line-height: 1.8;
  color: #2c3e50;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: calc(85vh - 150px);
  overflow: auto;
}

@media (max-width: 768px) {
  .resume-management {
    padding: 1rem;
  }

  .card-header {
    flex-direction: column;
    align-items: stretch;
  }

  .header-actions {
    flex-direction: column;
  }

  .search-input {
    min-width: auto;
    width: 100%;
  }

  .btn {
    justify-content: center;
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