<template>
  <div class="post-management">
    <div class="page-header">
      <h2>帖子管理</h2>
      <div class="header-actions">
        <button @click="loadPosts" class="btn-refresh">刷新</button>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card">
        <span class="stat-value">{{ stats.totalPosts }}</span>
        <span class="stat-label">总帖子数</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ stats.totalComments }}</span>
        <span class="stat-label">总评论数</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ stats.essenceCount }}</span>
        <span class="stat-label">精华帖</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ stats.topCount }}</span>
        <span class="stat-label">置顶帖</span>
      </div>
    </div>

    <div class="filter-bar">
      <input v-model="searchKeyword" type="text" placeholder="搜索标题或内容..." class="search-input" @keyup.enter="searchPosts" />
      <select v-model="statusFilter" class="filter-select">
        <option value="">全部状态</option>
        <option value="essence">精华</option>
        <option value="top">置顶</option>
      </select>
      <button @click="searchPosts" class="btn-search">搜索</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="posts.length === 0" class="empty">暂无帖子</div>
    <div v-else class="post-table">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>作者</th>
            <th>分类</th>
            <th>状态</th>
            <th>浏览</th>
            <th>点赞</th>
            <th>评论</th>
            <th>发布时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="post in posts" :key="post.id">
            <td>{{ post.id }}</td>
            <td class="post-title">{{ post.title }}</td>
            <td>{{ post.authorName }}</td>
            <td>{{ post.categoryName || '-' }}</td>
            <td>
              <span v-if="post.isTop" class="status-badge status-top">置顶</span>
              <span v-if="post.isEssence" class="status-badge status-essence">精华</span>
              <span v-if="!post.isTop && !post.isEssence" class="status-badge status-normal">普通</span>
            </td>
            <td>{{ post.viewCount }}</td>
            <td>{{ post.likeCount }}</td>
            <td>{{ post.commentCount }}</td>
            <td>{{ formatDate(post.createdAt) }}</td>
            <td class="actions">
              <button @click="viewPost(post)" class="btn-action btn-view">查看</button>
              <button v-if="!post.isEssence" @click="setEssence(post.id)" class="btn-action btn-essence">精华</button>
              <button v-else @click="removeEssence(post.id)" class="btn-action btn-remove-essence">取消精华</button>
              <button v-if="!post.isTop" @click="setTop(post.id)" class="btn-action btn-top">置顶</button>
              <button v-else @click="removeTop(post.id)" class="btn-action btn-remove-top">取消置顶</button>
              <button @click="deletePost(post.id)" class="btn-action btn-delete">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="totalPages > 1" class="pagination">
      <button @click="prevPage" :disabled="currentPage === 0" class="page-btn">上一页</button>
      <span class="page-info">{{ currentPage + 1 }} / {{ totalPages }}</span>
      <button @click="nextPage" :disabled="currentPage >= totalPages - 1" class="page-btn">下一页</button>
    </div>

    <div v-if="showDetailModal" class="modal" @click.self="closeDetailModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>帖子详情</h3>
          <button @click="closeDetailModal" class="btn-close">×</button>
        </div>
        <div class="modal-body" v-if="selectedPost">
          <div class="detail-grid">
            <div class="detail-item full-width">
              <label>标题</label>
              <span>{{ selectedPost.title }}</span>
            </div>
            <div class="detail-item">
              <label>作者</label>
              <span>{{ selectedPost.authorName }}</span>
            </div>
            <div class="detail-item">
              <label>分类</label>
              <span>{{ selectedPost.categoryName || '-' }}</span>
            </div>
            <div class="detail-item">
              <label>状态</label>
              <span>
                <span v-if="selectedPost.isTop" class="status-badge status-top">置顶</span>
                <span v-if="selectedPost.isEssence" class="status-badge status-essence">精华</span>
                <span v-if="!selectedPost.isTop && !selectedPost.isEssence" class="status-badge status-normal">普通</span>
              </span>
            </div>
            <div class="detail-item">
              <label>浏览量</label>
              <span>{{ selectedPost.viewCount }}</span>
            </div>
            <div class="detail-item">
              <label>点赞</label>
              <span>{{ selectedPost.likeCount }}</span>
            </div>
            <div class="detail-item">
              <label>评论</label>
              <span>{{ selectedPost.commentCount }}</span>
            </div>
            <div class="detail-item">
              <label>发布时间</label>
              <span>{{ formatDate(selectedPost.createdAt) }}</span>
            </div>
          </div>
          <div class="detail-content">
            <label>帖子内容</label>
            <div class="content-text">{{ selectedPost.content }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const token = localStorage.getItem('token')

const posts = ref([])
const stats = ref({
  totalPosts: 0,
  totalComments: 0,
  essenceCount: 0,
  topCount: 0
})
const loading = ref(false)
const error = ref('')
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = 15
const statusFilter = ref('')
const searchKeyword = ref('')
const showDetailModal = ref(false)
const selectedPost = ref(null)

const loadPosts = async (page = 0) => {
  loading.value = true
  error.value = ''
  try {
    const params = { page, size: pageSize }
    if (statusFilter.value) {
      params.filter = statusFilter.value
    }
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }
    const response = await axios.get('/api/admin/posts', {
      params,
      headers: { Authorization: `Bearer ${token}` }
    })
    if (response.data.content) {
      posts.value = response.data.content
      totalPages.value = response.data.totalPages
      currentPage.value = page
    }
  } catch (e) {
    error.value = '加载帖子失败'
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await axios.get('/api/admin/forum-stats', {
      headers: { Authorization: `Bearer ${token}` }
    })
    stats.value = response.data
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const searchPosts = () => {
  loadPosts(0)
}

const getStatusText = (status) => {
  const map = { 0: '普通', 1: '精华', 2: '置顶' }
  return map[status] || '普通'
}

const getStatusClass = (status) => {
  const map = { 0: 'status-normal', 1: 'status-essence', 2: 'status-top' }
  return map[status] || 'status-normal'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const viewPost = async (post) => {
  try {
    const response = await axios.get(`/api/admin/posts/${post.id}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    selectedPost.value = response.data
    showDetailModal.value = true
  } catch (e) {
    alert('加载帖子详情失败')
  }
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedPost.value = null
}

const setEssence = async (id) => {
  try {
    await axios.post(`/api/admin/posts/${id}/essence`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    })
    alert('设置精华成功')
    loadPosts(currentPage.value)
    loadStats()
  } catch (e) {
    alert('操作失败')
  }
}

const removeEssence = async (id) => {
  try {
    await axios.delete(`/api/admin/posts/${id}/essence`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    alert('取消精华成功')
    loadPosts(currentPage.value)
    loadStats()
  } catch (e) {
    alert('操作失败')
  }
}

const setTop = async (id) => {
  try {
    await axios.post(`/api/admin/posts/${id}/top`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    })
    alert('置顶成功')
    loadPosts(currentPage.value)
    loadStats()
  } catch (e) {
    alert('操作失败')
  }
}

const removeTop = async (id) => {
  try {
    await axios.delete(`/api/admin/posts/${id}/top`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    alert('取消置顶成功')
    loadPosts(currentPage.value)
    loadStats()
  } catch (e) {
    alert('操作失败')
  }
}

const deletePost = async (id) => {
  if (!confirm('确定要删除这篇帖子吗？')) return
  try {
    await axios.delete(`/api/admin/posts/${id}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    alert('删除成功')
    loadPosts(currentPage.value)
    loadStats()
  } catch (e) {
    alert('删除失败')
  }
}

const prevPage = () => {
  if (currentPage.value > 0) {
    loadPosts(currentPage.value - 1)
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    loadPosts(currentPage.value + 1)
  }
}

onMounted(() => {
  loadPosts()
  loadStats()
})
</script>

<style scoped>
.post-management {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.page-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #2c3e50;
}

.btn-refresh {
  padding: 0.5rem 1rem;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-card {
  background: #fff;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 2rem;
  font-weight: bold;
  color: #007bff;
}

.stat-label {
  display: block;
  margin-top: 0.5rem;
  color: #666;
  font-size: 0.9rem;
}

.filter-bar {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1rem;
  background: #fff;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  align-items: center;
}

.search-input {
  width: 300px;
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.filter-select {
  width: 140px;
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: #fff;
}

.btn-search {
  padding: 0.5rem 1.5rem;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.loading, .error, .empty {
  text-align: center;
  padding: 3rem;
  background: #fff;
  border-radius: 8px;
  color: #666;
}

.error {
  color: #dc3545;
}

.post-table {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: #f8f9fa;
}

th, td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

td {
  font-size: 14px;
  color: #666;
}

.post-title {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 12px;
}

.status-normal {
  background: #e9ecef;
  color: #6c757d;
}

.status-essence {
  background: #ffc107;
  color: #856404;
}

.status-top {
  background: #dc3545;
  color: #fff;
}

.actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.btn-action {
  padding: 0.3rem 0.6rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-view {
  background: #007bff;
  color: #fff;
}

.btn-essence {
  background: #ffc107;
  color: #333;
}

.btn-remove-essence {
  background: #6c757d;
  color: #fff;
}

.btn-top {
  background: #dc3545;
  color: #fff;
}

.btn-remove-top {
  background: #6c757d;
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
  margin-top: 1rem;
  padding: 1rem;
  background: #fff;
  border-radius: 8px;
}

.page-btn {
  padding: 0.5rem 1rem;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.page-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.page-info {
  color: #666;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.modal-content {
  background: #fff;
  border-radius: 12px;
  width: 95%;
  max-width: 900px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.3rem;
  font-weight: 600;
}

.btn-close {
  background: rgba(255,255,255,0.2);
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #fff;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.btn-close:hover {
  background: rgba(255,255,255,0.3);
  transform: scale(1.1);
}

.modal-body {
  padding: 2rem;
  overflow-y: auto;
  flex: 1;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-item label {
  font-size: 13px;
  font-weight: 600;
  color: #8898aa;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-item span {
  color: #333;
  font-size: 15px;
  line-height: 1.5;
}

.detail-content {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #eef1f5;
}

.detail-content label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #8898aa;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 0.75rem;
}

.content-text {
  background: #f8f9fa;
  padding: 1.25rem;
  border-radius: 8px;
  white-space: pre-wrap;
  line-height: 1.8;
  max-height: 450px;
  overflow-y: auto;
  word-break: break-word;
  font-size: 14px;
  border: 1px solid #eef1f5;
}
</style>