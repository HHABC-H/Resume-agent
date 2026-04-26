<template>
  <div class="my-resumes-page">
    <header class="header">
      <div class="logo" @click="router.push('/')">技术论坛</div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">论坛</router-link>
        <router-link to="/resume/upload" class="nav-item">简历助手</router-link>
        <router-link to="/resume/edit" class="nav-item active">我的简历</router-link>
        <router-link to="/reading" class="nav-item">在线阅读</router-link>
      </nav>
      <div class="header-right">
        <button @click="router.push('/resume/edit')" class="btn-primary">新建简历</button>
        <button @click="router.go(-1)" class="btn-back">返回</button>
      </div>
    </header>

    <main class="main-content">
      <div class="content-card">
        <div class="page-header">
          <button class="back-btn" @click="router.go(-1)">← 返回</button>
          <h1 class="page-title">我的简历</h1>
        </div>

        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="resumes.length === 0" class="empty">
          <p>暂无简历记录</p>
          <button @click="router.push('/resume/edit')" class="btn-primary">开始编辑</button>
        </div>
        <div v-else class="resume-list">
          <div v-for="(resume, index) in resumes" :key="resume.id" class="resume-item">
            <div class="resume-info">
              <span class="version">V{{ resumes.length - index }}</span>
              <span class="date">{{ formatDate(resume.createdAt) }}</span>
              <span class="status" :class="resume.status.toLowerCase()">{{ resume.status === 'DRAFT' ? '草稿' : '正式' }}</span>
            </div>
            <div class="resume-actions">
              <button @click="viewResume(resume.id)" class="btn-view">查看</button>
              <button @click="editResume(resume.id)" class="btn-edit">编辑</button>
              <button @click="deleteResume(resume.id)" class="btn-delete">删除</button>
            </div>
          </div>
        </div>

        <div v-if="totalPages > 1" class="pagination">
          <button @click="prevPage" :disabled="currentPage === 0" class="page-btn">上一页</button>
          <span class="page-info">{{ currentPage + 1 }} / {{ totalPages }}</span>
          <button @click="nextPage" :disabled="currentPage >= totalPages - 1" class="page-btn">下一页</button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { resumeEditApi } from '../api/resumeEdit'

const router = useRouter()

const resumes = ref([])
const loading = ref(false)
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = 10

const loadResumes = async (page = 0) => {
  loading.value = true
  try {
    const response = await resumeEditApi.getHistory(page, pageSize)
    if (response.data.code === 200) {
      resumes.value = response.data.data.content || []
      totalPages.value = response.data.data.totalPages || 0
      currentPage.value = page
    }
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const viewResume = (id) => {
  router.push(`/resume/edit/${id}`)
}

const editResume = (id) => {
  router.push(`/resume/edit?from=${id}`)
}

const deleteResume = async (id) => {
  if (!confirm('确定要删除这份简历吗？')) return
  try {
    const response = await resumeEditApi.deleteHistory(id)
    if (response.data.code === 200) {
      alert('删除成功')
      loadResumes(currentPage.value)
    }
  } catch (e) {
    alert('删除失败')
  }
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

onMounted(() => {
  loadResumes()
})
</script>

<style scoped>
.my-resumes-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem 2rem;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  font-size: 1.4rem;
  font-weight: bold;
  color: #007bff;
  cursor: pointer;
  margin-right: 2rem;
}

.nav-menu {
  display: flex;
  gap: 0.3rem;
  flex: 1;
}

.nav-item {
  color: #666;
  text-decoration: none;
  padding: 0.5rem 0.9rem;
  border-radius: 4px;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.2s;
}

.nav-item:hover {
  background: #f0f7ff;
  color: #007bff;
}

.nav-item.active {
  background: #007bff;
  color: #fff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.btn-primary {
  padding: 0.5rem 1rem;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-back {
  padding: 0.5rem 1rem;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
}

.main-content {
  flex: 1;
  padding: 1.5rem 2rem;
  display: flex;
  justify-content: center;
}

.content-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  width: 100%;
  max-width: 800px;
}

.page-header {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #eee;
}

.back-btn {
  padding: 0.5rem 1rem;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  margin-right: 1rem;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.loading, .empty {
  padding: 3rem;
  text-align: center;
  color: #666;
}

.resume-list {
  padding: 1rem 1.5rem;
}

.resume-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 0.5rem;
}

.resume-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.version {
  font-weight: bold;
  color: #007bff;
  font-size: 15px;
}

.date {
  color: #666;
  font-size: 14px;
}

.status {
  padding: 0.2rem 0.5rem;
  border-radius: 3px;
  font-size: 12px;
}

.status.draft {
  background: #fff3cd;
  color: #856404;
}

.status.published {
  background: #d4edda;
  color: #155724;
}

.resume-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-view, .btn-edit, .btn-delete {
  padding: 0.4rem 0.8rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}

.btn-view {
  background: #007bff;
  color: #fff;
}

.btn-edit {
  background: #28a745;
  color: #fff;
}

.btn-delete {
  background: #dc3545;
  color: #fff;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border-top: 1px solid #eee;
}

.page-btn {
  padding: 0.5rem 1rem;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.page-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 14px;
}
</style>