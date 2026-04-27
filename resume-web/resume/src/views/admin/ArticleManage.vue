<template>
  <div class="article-manage-page">
    <div class="page-header">
      <button class="back-btn" @click="router.push('/admin/dashboard')">← 返回</button>
      <h1 class="page-title">文章管理</h1>
      <button @click="router.push('/admin/article-edit')" class="btn-add">+ 新建文章</button>
    </div>

    <div class="filter-bar">
      <input v-model="searchKeyword" type="text" placeholder="搜索文章..." class="search-input" @keyup.enter="handleSearch" />
      <select v-model="filterCategory" class="filter-select" @change="handleSearch">
        <option value="">全部分类</option>
        <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
      </select>
      <button @click="handleSearch" class="search-btn">🔍 搜索文章</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="articles.length === 0" class="empty">
      <p>暂无文章</p>
    </div>
    <div v-else class="article-table">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>分类</th>
            <th>作者</th>
            <th>阅读数</th>
            <th>状态</th>
            <th>时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="article in articles" :key="article.id">
            <td>{{ article.id }}</td>
            <td class="title-cell">{{ article.title }}</td>
            <td>{{ article.category }}</td>
            <td>{{ article.authorName }}</td>
            <td>{{ article.readCount }}</td>
            <td>
              <span :class="['status-tag', article.status.toLowerCase()]">
                {{ article.status === 'PUBLISHED' ? '已发布' : '草稿' }}
              </span>
            </td>
            <td>{{ formatDate(article.createdAt) }}</td>
            <td>
              <button @click="editArticle(article.id)" class="btn-edit">编辑</button>
              <button @click="deleteArticle(article.id)" class="btn-delete">删除</button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '../../api/article'

const router = useRouter()

const articles = ref([])
const categories = ref(['Java', 'Python', 'Frontend', 'DB', '架构', '面试技巧'])
const searchKeyword = ref('')
const filterCategory = ref('')
const loading = ref(false)
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = 20

const loadArticles = async (page = 0) => {
  loading.value = true
  try {
    const params = { page, size: pageSize }
    if (filterCategory.value) {
      params.category = filterCategory.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    const response = await axios.get(`${API_BASE}/article`, { params, ...getHeaders() })
    if (response.data.code === 200) {
      articles.value = response.data.data.content || []
      totalPages.value = response.data.data.totalPages || 0
      currentPage.value = page
    }
  } catch (e) {
    console.error('加载文章失败', e)
  } finally {
    loading.value = false
  }
}

import axios from 'axios'

const API_BASE = '/api'
const getHeaders = () => ({
  headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
})

const loadCategories = async () => {
  try {
    const response = await axios.get(`${API_BASE}/article/categories`, getHeaders())
    if (response.data.code === 200 && response.data.data.length > 0) {
      categories.value = response.data.data
    }
  } catch (e) {
    console.log('使用默认分类')
  }
}

const handleSearch = () => {
  loadArticles(0)
}

const editArticle = (id) => {
  router.push(`/admin/article-edit/${id}`)
}

const deleteArticle = async (id) => {
  if (!confirm('确定要删除这篇文章吗？')) return
  try {
    const response = await axios.delete(`${API_BASE}/article/${id}`, getHeaders())
    if (response.data.code === 200) {
      alert('删除成功')
      loadArticles(currentPage.value)
    }
  } catch (e) {
    alert('删除失败')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const prevPage = () => {
  if (currentPage.value > 0) {
    loadArticles(currentPage.value - 1)
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    loadArticles(currentPage.value + 1)
  }
}

onMounted(() => {
  loadArticles()
  loadCategories()
})
</script>

<style scoped>
.article-manage-page {
  padding: 1.5rem;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.back-btn, .btn-add {
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.back-btn {
  background: #fff;
  border: 1px solid #dcdfe6;
  color: #606266;
}

.back-btn:hover {
  border-color: #409eff;
  color: #409eff;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}

.btn-add {
  background: #409eff;
  color: #fff;
  border: none;
}

.btn-add:hover {
  background: #66b1ff;
}

.filter-bar {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1rem;
  align-items: center;
}

.search-input {
  flex: 3;
  min-width: 200px;
  height: 36px;
  padding: 0 1rem;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
}

.search-input:focus {
  border-color: #409eff;
}

.filter-select {
  flex: 1;
  min-width: 140px;
  height: 36px;
  padding: 0 2rem 0 1rem;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  background: #fff;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%2360666f' d='M2 4l4 4 4-4'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
}

.filter-select:focus {
  border-color: #409eff;
}

.search-btn {
  padding: 0.6rem 1.2rem;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.search-btn:hover {
  background: #66b1ff;
}

.loading, .empty {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.article-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 0.8rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background: #f9f9f9;
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

td {
  font-size: 14px;
  color: #666;
}

.title-cell {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-tag {
  padding: 0.2rem 0.5rem;
  border-radius: 3px;
  font-size: 12px;
}

.status-tag.published {
  background: #d4edda;
  color: #155724;
}

.status-tag.draft {
  background: #fff3cd;
  color: #856404;
}

.btn-edit, .btn-delete {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  margin-right: 0.5rem;
}

.btn-edit {
  background: #409eff;
  color: #fff;
}

.btn-edit:hover {
  background: #66b1ff;
}

.btn-delete {
  background: #f56c6c;
  color: #fff;
}

.btn-delete:hover {
  background: #f78989;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
}

.page-btn {
  padding: 0.5rem 1rem;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.page-btn:hover:not(:disabled) {
  background: #66b1ff;
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