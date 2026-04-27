<template>
  <div class="forum-home">
    <header class="header">
      <div class="logo" @click="router.push('/')">技术论坛</div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item active">论坛</router-link>
        <router-link to="/resume/upload" class="nav-item">简历助手</router-link>
        <router-link to="/reading" class="nav-item">在线阅读</router-link>
        <router-link :to="interviewLink" class="nav-item">面试助手</router-link>
        <router-link to="/profile" class="nav-item">个人信息</router-link>
        <router-link to="/history" class="nav-item">查看历史</router-link>
        <router-link to="/my-bookmarks" class="nav-item">我的收藏</router-link>
        <router-link to="/forum/essences" class="nav-item">精华帖</router-link>
        <router-link to="/forum/authors" class="nav-item">热门作者</router-link>
      </nav>
      <div class="header-right">
        <router-link to="/forum/publish" class="btn-publish-header">+ 发布帖子</router-link>
        <div class="user-section">
          <template v-if="isLoggedIn">
            <div class="user-info" @click="router.push('/profile')">
              <span class="username">{{ username }}</span>
              <div class="avatar">{{ username?.charAt(0).toUpperCase() }}</div>
            </div>
            <button @click="handleLogout" class="btn-logout">退出</button>
          </template>
          <template v-else>
            <router-link to="/login" class="btn-login">登录</router-link>
            <router-link to="/register" class="btn-register">注册</router-link>
          </template>
        </div>
      </div>
    </header>

    <main class="main-content">
      <div class="content-wrapper">
        <div class="main">
          <h2 class="category-title">{{ categoryName }}</h2>

        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="error" class="error">{{ error }}</div>
        <div v-else>
          <div v-if="posts.length === 0" class="empty">该分类暂无帖子</div>
          <div v-else>
            <div v-for="post in posts" :key="post.id" class="post-item">
              <h2>
                <span v-if="post.status === 1" class="badge essence">精华</span>
                <span v-if="post.status === 2" class="badge top">置顶</span>
                <router-link :to="`/forum/post/${post.id}`">{{ post.title }}</router-link>
              </h2>
              <div class="meta">
                <span>{{ post.authorName }}</span>
                <span>{{ formatDate(post.createdAt) }}</span>
                <span>阅读 {{ post.viewCount }}</span>
                <span>评论 {{ post.commentCount }}</span>
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

      <aside class="sidebar">
        <div class="box">
          <h3>分类</h3>
          <ul>
            <li v-for="cat in categories" :key="cat.id">
              <router-link
                :to="`/forum/category/${cat.id}`"
                :class="{ active: cat.id === categoryId }"
              >
                {{ cat.name }}
                <span class="count">{{ cat.postCount }}</span>
              </router-link>
            </li>
          </ul>
        </div>
      </aside>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const token = localStorage.getItem('token')
const username = localStorage.getItem('username')

const isLoggedIn = computed(() => !!token)

const interviewLink = computed(() => {
  if (!token) return '/login'
  return '/interview/1'
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userId')
  localStorage.removeItem('role')
  router.push('/login')
}

const posts = ref([])
const categories = ref([])
const loading = ref(true)
const error = ref('')
const page = ref(0)
const totalPages = ref(1)
const categoryId = ref(null)
const categoryName = ref('')

const loadPosts = async (pageNum = 0) => {
  try {
    loading.value = true
    const response = await axios.get(`/api/forum/category/${categoryId.value}?page=${pageNum}&size=20`)
    posts.value = response.data.data?.content || []
    totalPages.value = response.data.data?.totalPages || 1
    page.value = pageNum
  } catch (e) {
    error.value = '加载失败: ' + (e.message || '未知错误')
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const response = await axios.get('/api/forum/categories')
    categories.value = response.data?.data || []
    const cat = categories.value.find(c => c.id === categoryId.value)
    if (cat) {
      categoryName.value = cat.name
    }
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const init = () => {
  categoryId.value = parseInt(route.params.id)
  loadPosts()
  loadCategories()
}

watch(() => route.params.id, () => {
  init()
})

onMounted(() => {
  init()
})
</script>

<style scoped>
.forum-home {
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
  gap: 1.5rem;
}

.btn-publish-header {
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
  color: #fff;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-publish-header:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,123,255,0.3);
}

.user-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  padding: 0.3rem 0.5rem;
  border-radius: 4px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f5f5f5;
}

.username {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
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

.badge.top {
  background: #dc3545;
  color: #fff;
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
