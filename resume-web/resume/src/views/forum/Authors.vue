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
        <router-link to="/forum/essences" class="nav-item">精华帖</router-link>
        <router-link to="/forum/authors" class="nav-item active">热门作者</router-link>
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
            <h1 class="page-title">热门作者</h1>
          </div>

          <div class="author-list-container">
            <div v-if="loading" class="loading">加载中...</div>
            <div v-else-if="error" class="error">{{ error }}</div>
            <div v-else-if="authors.length === 0" class="empty">
              <p>暂无热门作者</p>
            </div>
            <div v-else class="author-grid">
              <div
                v-for="author in authors"
                :key="author.id"
                class="author-card"
                @click="router.push(`/forum/author/${author.id}`)"
              >
                <div class="author-avatar">{{ author.username?.charAt(0).toUpperCase() }}</div>
                <div class="author-info">
                  <div class="author-name">{{ author.displayName || author.username }}</div>
                  <div class="author-posts">{{ author.postCount }} 帖</div>
                </div>
              </div>
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

const authors = ref([])
const categories = ref([])
const hotPosts = ref([])
const loading = ref(true)
const error = ref('')

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

const loadAuthors = async () => {
  try {
    loading.value = true
    const response = await axios.get('/api/forum/hot-authors?limit=50')
    authors.value = response.data?.data || response.data || []
  } catch (e) {
    error.value = '加载失败'
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

onMounted(() => {
  loadAuthors()
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

.author-list-container {
  padding: 1rem;
}

.author-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.author-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.author-card:hover {
  background: #f0f7ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 18px;
  flex-shrink: 0;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.author-name {
  color: #333;
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.author-posts {
  color: #999;
  font-size: 12px;
}

.loading, .error, .empty {
  text-align: center;
  padding: 3rem;
  color: #999;
}

.error {
  color: #dc3545;
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

  .author-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
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