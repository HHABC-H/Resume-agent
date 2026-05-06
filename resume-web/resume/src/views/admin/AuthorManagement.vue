<template>
  <div class="author-management">
    <div class="page-header">
      <h2>热门作者管理</h2>
      <div class="header-actions">
        <button @click="loadAuthors" class="btn-refresh">刷新</button>
      </div>
    </div>

    <div class="stats-cards">
      <div class="stat-card">
        <span class="stat-value">{{ stats.totalAuthors }}</span>
        <span class="stat-label">作者总数</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ stats.hotAuthors }}</span>
        <span class="stat-label">热门作者</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ stats.totalPosts }}</span>
        <span class="stat-label">总帖子数</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ stats.totalLikes }}</span>
        <span class="stat-label">总点赞数</span>
      </div>
    </div>

    <div class="filter-bar">
      <input v-model="searchKeyword" type="text" placeholder="搜索用户名..." class="search-input" @keyup.enter="searchAuthors" />
      <button @click="searchAuthors" class="btn-search">搜索</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="authors.length === 0" class="empty">暂无作者</div>
    <div v-else class="author-table">
      <table>
        <thead>
          <tr>
            <th>排名</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>帖子数</th>
            <th>点赞数</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(author, index) in authors" :key="author.id">
            <td>{{ page * pageSize + index + 1 }}</td>
            <td class="author-name">
              <div class="author-avatar">{{ author.username?.charAt(0).toUpperCase() }}</div>
              <span>{{ author.username }}</span>
            </td>
            <td>{{ author.email || '-' }}</td>
            <td>{{ author.postCount || 0 }}</td>
            <td>{{ author.likeCount || 0 }}</td>
            <td>{{ formatDate(author.createdAt) }}</td>
            <td class="actions">
              <button @click="viewAuthor(author)" class="btn-action btn-view">查看</button>
              <button @click="viewAuthorPosts(author)" class="btn-action btn-posts">查看帖子</button>
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
          <h3>作者详情</h3>
          <button @click="closeDetailModal" class="btn-close">×</button>
        </div>
        <div class="modal-body" v-if="selectedAuthor">
          <div class="detail-grid">
            <div class="detail-item full-width">
              <label>用户名</label>
              <span>{{ selectedAuthor.username }}</span>
            </div>
            <div class="detail-item">
              <label>邮箱</label>
              <span>{{ selectedAuthor.email || '-' }}</span>
            </div>
            <div class="detail-item">
              <label>发帖数</label>
              <span>{{ selectedAuthor.postCount || 0 }}</span>
            </div>
            <div class="detail-item">
              <label>点赞数</label>
              <span>{{ selectedAuthor.likeCount || 0 }}</span>
            </div>
            <div class="detail-item">
              <label>评论数</label>
              <span>{{ selectedAuthor.commentCount || 0 }}</span>
            </div>
            <div class="detail-item">
              <label>注册时间</label>
              <span>{{ formatDate(selectedAuthor.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showPostsModal" class="modal" @click.self="closePostsModal">
      <div class="modal-content modal-large">
        <div class="modal-header">
          <h3>{{ selectedAuthorName }} 的帖子</h3>
          <button @click="closePostsModal" class="btn-close">×</button>
        </div>
        <div class="modal-body">
          <div v-if="authorPosts.length === 0" class="empty">暂无帖子</div>
          <div v-else class="post-list">
            <div v-for="post in authorPosts" :key="post.id" class="post-item">
              <div class="post-info">
                <span class="post-title">{{ post.title }}</span>
                <span class="post-meta">
                  <span class="status-badge" :class="post.isEssence ? 'status-essence' : 'status-normal'">
                    {{ post.isEssence ? '精华' : '普通' }}
                  </span>
                  <span class="post-stats">浏览 {{ post.viewCount }} | 点赞 {{ post.likeCount }} | 评论 {{ post.commentCount }}</span>
                </span>
              </div>
              <div class="post-actions">
                <button @click="viewPost(post)" class="btn-action btn-view">查看</button>
              </div>
            </div>
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

const authors = ref([])
const stats = ref({
  totalAuthors: 0,
  hotAuthors: 0,
  totalPosts: 0,
  totalLikes: 0
})
const loading = ref(false)
const error = ref('')
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = 15
const searchKeyword = ref('')
const showDetailModal = ref(false)
const selectedAuthor = ref(null)
const showPostsModal = ref(false)
const authorPosts = ref([])
const selectedAuthorName = ref('')

const loadAuthors = async (page = 0) => {
  loading.value = true
  error.value = ''
  try {
    const params = { page, size: pageSize }
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }
    const response = await axios.get('/api/admin/hot-authors', {
      params,
      headers: { Authorization: `Bearer ${token}` }
    })
    if (response.data.content) {
      authors.value = response.data.content
      totalPages.value = response.data.totalPages
      currentPage.value = page
    }
  } catch (e) {
    error.value = '加载作者失败'
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const response = await axios.get('/api/admin/author-stats', {
      headers: { Authorization: `Bearer ${token}` }
    })
    stats.value = response.data
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const searchAuthors = () => {
  loadAuthors(0)
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const viewAuthor = (author) => {
  selectedAuthor.value = author
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedAuthor.value = null
}

const viewAuthorPosts = async (author) => {
  try {
    selectedAuthorName.value = author.username
    const response = await axios.get(`/api/admin/authors/${author.id}/posts`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    authorPosts.value = response.data || []
    showPostsModal.value = true
  } catch (e) {
    alert('加载帖子失败')
  }
}

const closePostsModal = () => {
  showPostsModal.value = false
  authorPosts.value = []
  selectedAuthorName.value = ''
}

const viewPost = (post) => {
  window.open(`/forum/post/${post.id}`, '_blank')
}

const prevPage = () => {
  if (currentPage.value > 0) {
    loadAuthors(currentPage.value - 1)
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    loadAuthors(currentPage.value + 1)
  }
}

onMounted(() => {
  loadAuthors()
  loadStats()
})
</script>

<style scoped>
.author-management {
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

.author-table {
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
  background: #e3f2fd;
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

.author-name {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.author-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  flex-shrink: 0;
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

.status-hot {
  background: linear-gradient(135deg, #007bff, #0056b3);
  color: #fff;
}

.status-essence {
  background: #007bff;
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

.btn-hot {
  background: linear-gradient(135deg, #007bff, #0056b3);
  color: #fff;
}

.btn-remove-hot {
  background: #6c757d;
  color: #fff;
}

.btn-posts {
  background: #17a2b8;
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
  max-width: 600px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  display: flex;
  flex-direction: column;
}

.modal-large {
  max-width: 900px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 2rem;
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
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

.post-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.post-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  gap: 1rem;
}

.post-info {
  flex: 1;
  min-width: 0;
}

.post-title {
  display: block;
  font-weight: 500;
  color: #333;
  margin-bottom: 0.5rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 12px;
  color: #999;
}

.post-stats {
  white-space: nowrap;
}

.post-actions {
  flex-shrink: 0;
}
</style>