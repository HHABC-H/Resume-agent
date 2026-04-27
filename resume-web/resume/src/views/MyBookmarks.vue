<template>
  <div class="forum-home">
    <header class="header">
      <div class="logo" @click="router.push('/')">技术论坛</div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">论坛</router-link>
        <router-link to="/resume/upload" class="nav-item">简历助手</router-link>
        <router-link to="/reading" class="nav-item">在线阅读</router-link>
        <router-link to="/interview/1" class="nav-item">面试助手</router-link>
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
          <div class="page-header">
            <button class="back-btn" @click="router.go(-1)">← 返回</button>
            <h1 class="page-title">我的收藏</h1>
          </div>

          <div class="bookmarks-container">
            <div v-if="loading" class="loading">加载中...</div>
            <div v-else-if="error" class="error">{{ error }}</div>
            <div v-else-if="bookmarks.length === 0" class="empty">
              <p>暂无收藏内容</p>
              <p class="hint">浏览帖子时点击收藏按钮来收藏感兴趣的内容</p>
            </div>
            <div v-else class="bookmark-list">
              <div
                v-for="bookmark in bookmarks"
                :key="bookmark.id"
                class="bookmark-item"
                @click="router.push(`/forum/post/${bookmark.id}`)"
              >
                <h3 class="bookmark-title">{{ bookmark.title }}</h3>
                <div class="bookmark-meta">
                  <span class="author">作者：{{ bookmark.authorName }}</span>
                  <span class="category">{{ bookmark.categoryName }}</span>
                  <span class="time">{{ formatDate(bookmark.createdAt) }}</span>
                </div>
                <div class="bookmark-preview">{{ bookmark.contentPreview }}</div>
              </div>
            </div>

            <div v-if="totalPages > 1" class="pagination">
              <button @click="loadBookmarks(currentPage - 1)" :disabled="currentPage === 0" class="page-btn">上一页</button>
              <span class="page-info">{{ currentPage + 1 }} / {{ totalPages }}</span>
              <button @click="loadBookmarks(currentPage + 1)" :disabled="currentPage >= totalPages - 1" class="page-btn">下一页</button>
            </div>
          </div>
        </div>

        <aside class="sidebar">
          <div class="sidebar-section">
            <h3>分类导航</h3>
            <ul class="category-list">
              <li v-for="cat in categories" :key="cat.id">
                <router-link :to="`/forum/category/${cat.id}`">
                  <span class="cat-name">{{ cat.name }}</span>
                  <span class="cat-count">{{ cat.postCount }}</span>
                </router-link>
              </li>
            </ul>
          </div>

          <div class="sidebar-section">
            <h3>🔥 全站热榜</h3>
            <ul class="hot-list">
              <li v-for="(hotPost, index) in hotPosts" :key="hotPost.id" @click="router.push(`/forum/post/${hotPost.id}`)">
                <span class="rank" :class="{ top3: index < 3 }">{{ index + 1 }}</span>
                <span class="hot-title">{{ hotPost.title }}</span>
              </li>
            </ul>
          </div>
        </aside>
      </div>
    </main>

    <footer class="footer">
      <p>&copy; 2024 Resume Agent. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const bookmarks = ref([])
const categories = ref([])
const hotPosts = ref([])
const loading = ref(true)
const error = ref('')
const currentPage = ref(0)
const totalPages = ref(1)

const token = localStorage.getItem('token')
const username = localStorage.getItem('username')

const isLoggedIn = computed(() => !!token)

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userId')
  localStorage.removeItem('role')
  router.push('/login')
}

const loadBookmarks = async (page = 0) => {
  try {
    loading.value = true
    error.value = ''
    const response = await axios.get(`/article/bookmark?page=${page}&size=20`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (response.data.code === 200) {
      bookmarks.value = response.data.data.content || []
      totalPages.value = response.data.data.totalPages || 1
    } else {
      bookmarks.value = response.data.content || []
      totalPages.value = response.data.totalPages || 1
    }
    currentPage.value = page
  } catch (e) {
    error.value = '加载收藏失败'
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const response = await axios.get('/api/forum/categories')
    categories.value = response.data?.data || response.data || []
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

const loadHotPosts = async () => {
  try {
    const response = await axios.get('/api/forum/hot?size=10')
    hotPosts.value = response.data.data?.content || response.data?.content || []
  } catch (e) {
    console.error('加载热榜失败', e)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadBookmarks()
  loadCategories()
  loadHotPosts()
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

.btn-primary {
  padding: 0.5rem 1rem;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary:hover {
  background: #0069d9;
}

.btn-login, .btn-register {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
}

.btn-login {
  color: #007bff;
}

.btn-register {
  background: #007bff;
  color: #fff;
}

.btn-logout {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
  background: #dc3545;
  color: #fff;
  border: none;
  cursor: pointer;
}

.btn-logout:hover {
  background: #c82333;
}

.main-content {
  flex: 1;
  padding: 1.5rem 2rem;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 20px;
}

.main {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  overflow: hidden;
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

.back-btn:hover {
  border-color: #007bff;
  color: #007bff;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.bookmarks-container {
  padding: 1rem;
}

.loading, .error, .empty {
  text-align: center;
  padding: 3rem;
  color: #999;
}

.error {
  color: #dc3545;
}

.empty .hint {
  font-size: 14px;
  color: #aaa;
  margin-top: 0.5rem;
}

.bookmark-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.bookmark-item {
  padding: 1rem;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.bookmark-item:hover {
  background: #f0f7ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.bookmark-title {
  margin: 0 0 0.5rem 0;
  font-size: 16px;
  color: #333;
}

.bookmark-meta {
  display: flex;
  gap: 1rem;
  font-size: 13px;
  color: #666;
  margin-bottom: 0.5rem;
}

.bookmark-meta .category {
  color: #007bff;
}

.bookmark-preview {
  font-size: 14px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 1rem;
  padding-top: 1rem;
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

.sidebar {
  width: 280px;
  flex-shrink: 0;
}

.sidebar-section {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  margin-bottom: 15px;
  overflow: hidden;
}

.sidebar-section h3 {
  padding: 0.8rem 1rem;
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #f5f5f5;
  background: #fff;
}

.category-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.category-list li a {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.7rem 1rem;
  color: #666;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s;
}

.category-list li a:hover {
  background: #f8f9fa;
  color: #007bff;
}

.cat-count {
  color: #999;
  font-size: 12px;
}

.hot-list {
  list-style: none;
  padding: 0.5rem 0;
  margin: 0;
}

.hot-list li {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.6rem 1rem;
  cursor: pointer;
  transition: background 0.2s;
}

.hot-list li:hover {
  background: #f8f9fa;
}

.rank {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  border-radius: 4px;
  font-size: 12px;
  color: #999;
  font-weight: 600;
  flex-shrink: 0;
}

.rank.top3 {
  background: linear-gradient(135deg, #ff6b6b, #ffa500);
  color: #fff;
}

.hot-title {
  color: #333;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.footer {
  text-align: center;
  padding: 1rem;
  background: #fff;
  color: #999;
  font-size: 13px;
  margin-top: auto;
}

@media (max-width: 900px) {
  .content-wrapper {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
}

@media (max-width: 600px) {
  .sidebar {
    grid-template-columns: 1fr;
  }

  .header {
    flex-wrap: wrap;
    gap: 1rem;
    padding: 0.8rem 1rem;
  }

  .nav-menu {
    order: 3;
    width: 100%;
    overflow-x: auto;
  }
}
</style>