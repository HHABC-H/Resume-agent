<template>
  <div class="forum-container">
    <div class="header">
      <h1>技术论坛</h1>
      <div class="nav">
        <router-link to="/forum">首页</router-link>
        <router-link to="/forum/essences" class="active">精华帖</router-link>
        <router-link to="/forum/publish" class="btn-publish">发布帖子</router-link>
      </div>
    </div>

    <div class="main">
      <h2 class="page-title">⭐ 精华帖</h2>

      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="error" class="error">{{ error }}</div>
      <div v-else>
        <div v-if="posts.length === 0" class="empty">暂无精华帖</div>
        <div v-else>
          <div v-for="post in posts" :key="post.id" class="post-item">
            <h2>
              <span class="badge essence">精华</span>
              <router-link :to="`/forum/post/${post.id}`">{{ post.title }}</router-link>
            </h2>
            <div class="meta">
              <span>{{ post.authorName }}</span>
              <span>{{ post.categoryName }}</span>
              <span>{{ formatDate(post.createdAt) }}</span>
              <span>阅读 {{ post.viewCount }}</span>
              <span>评论 {{ post.commentCount }}</span>
              <span>👍 {{ post.likeCount }}</span>
            </div>
            <div class="preview">{{ post.contentPreview }}</div>
          </div>
        </div>

        <div class="pagination">
          <button :disabled="page === 0" @click="loadPosts(page - 1)">上一页</button>
          <span>{{ page + 1 }} / {{ totalPages }}</span>
          <button :disabled="page >= totalPages - 1" @click="loadPosts(page + 1)">下一页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const posts = ref([])
const loading = ref(true)
const error = ref('')
const page = ref(0)
const totalPages = ref(1)

const loadPosts = async (pageNum = 0) => {
  try {
    loading.value = true
    const response = await axios.get(`/forum/essences?page=${pageNum}&size=20`)
    posts.value = response.data.content || []
    totalPages.value = response.data.totalPages || 1
    page.value = pageNum
  } catch (e) {
    error.value = '加载失败: ' + (e.message || '未知错误')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.forum-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  background: #fff;
  padding: 15px 20px;
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.nav {
  display: flex;
  gap: 20px;
  align-items: center;
}

.nav a {
  color: #007bff;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 4px;
}

.nav a:hover {
  background: #f0f0f0;
}

.nav a.active {
  background: #f0f0f0;
  font-weight: bold;
}

.btn-publish {
  background: #007bff;
  color: #fff !important;
}

.btn-publish:hover {
  background: #0056b3 !important;
}

.main {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.page-title {
  margin-bottom: 20px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
}

.post-item {
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.post-item:last-child {
  border-bottom: none;
}

.post-item h2 {
  font-size: 18px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.post-item h2 a {
  color: #333;
  text-decoration: none;
}

.post-item h2 a:hover {
  color: #007bff;
}

.badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.badge.essence {
  background: #ffc107;
  color: #000;
}

.meta {
  color: #999;
  font-size: 14px;
  margin-bottom: 10px;
}

.meta span {
  margin-right: 15px;
}

.preview {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.pagination {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
  align-items: center;
}

.pagination button {
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading, .error, .empty {
  text-align: center;
  padding: 50px;
  color: #999;
}

.error {
  color: #dc3545;
}
</style>
