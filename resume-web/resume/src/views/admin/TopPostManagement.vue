<template>
  <div class="top-management">
    <div class="page-header">
      <h2>置顶管理</h2>
      <div class="header-actions">
        <button @click="loadTopPosts" class="btn-refresh">刷新</button>
      </div>
    </div>

    <div class="stats-info">
      <span>当前置顶帖子数：{{ topCount }}</span>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="topPosts.length === 0" class="empty">暂无置顶帖子</div>
    <div v-else class="top-table">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>作者</th>
            <th>分类</th>
            <th>浏览</th>
            <th>点赞</th>
            <th>评论</th>
            <th>置顶时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="post in topPosts" :key="post.id">
            <td>{{ post.id }}</td>
            <td class="post-title">{{ post.title }}</td>
            <td>{{ post.authorName }}</td>
            <td>{{ post.categoryName || '-' }}</td>
            <td>{{ post.viewCount }}</td>
            <td>{{ post.likeCount }}</td>
            <td>{{ post.commentCount }}</td>
            <td>{{ formatDate(post.updatedAt) }}</td>
            <td class="actions">
              <button @click="viewPost(post)" class="btn-action btn-view">查看</button>
              <button @click="removeTop(post.id)" class="btn-action btn-remove">取消置顶</button>
            </td>
          </tr>
        </tbody>
      </table>
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

const topPosts = ref([])
const topCount = ref(0)
const loading = ref(false)
const error = ref('')
const showDetailModal = ref(false)
const selectedPost = ref(null)

const loadTopPosts = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await axios.get('/api/admin/posts', {
      params: { filter: 'top', page: 0, size: 100 },
      headers: { Authorization: `Bearer ${token}` }
    })
    if (response.data.content) {
      topPosts.value = response.data.content
      topCount.value = response.data.totalElements
    }
  } catch (e) {
    error.value = '加载置顶帖子失败'
    console.error(e)
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const getStatusText = (status) => {
  const map = { 0: '普通', 1: '精华', 2: '置顶' }
  return map[status] || '普通'
}

const getStatusClass = (status) => {
  const map = { 0: 'status-normal', 1: 'status-essence', 2: 'status-top' }
  return map[status] || 'status-normal'
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

const removeTop = async (id) => {
  if (!confirm('确定要取消置顶吗？')) return
  try {
    await axios.delete(`/api/admin/posts/${id}/top`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    alert('取消置顶成功')
    loadTopPosts()
  } catch (e) {
    alert('操作失败')
  }
}

onMounted(() => {
  loadTopPosts()
})
</script>

<style scoped>
.top-management {
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

.stats-info {
  margin-bottom: 1rem;
  padding: 1rem;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  font-size: 14px;
  color: #666;
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

.top-table {
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

.actions {
  display: flex;
  gap: 0.5rem;
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

.btn-remove {
  background: #dc3545;
  color: #fff;
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

.modal-content {
  background: #fff;
  border-radius: 8px;
  width: 90%;
  max-width: 800px;
  max-height: 85vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  border-bottom: 1px solid #eee;
  background: #f8f9fa;
  border-radius: 8px 8px 0 0;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  color: #2c3e50;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 1.5rem 2rem;
}

.detail-item {
  display: flex;
  padding: 0.75rem 0;
  border-bottom: 1px solid #eee;
}

.detail-item label {
  width: 100px;
  font-weight: 600;
  color: #333;
  flex-shrink: 0;
}

.detail-item span {
  flex: 1;
  color: #666;
  word-break: break-all;
}

.detail-content {
  margin-top: 1rem;
}

.detail-content label {
  display: block;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
  padding-top: 0.5rem;
}

.content-text {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 4px;
  white-space: pre-wrap;
  line-height: 1.8;
  max-height: 400px;
  overflow-y: auto;
  word-break: break-word;
  font-size: 14px;
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
</style>