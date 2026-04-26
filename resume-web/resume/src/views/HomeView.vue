<template>
  <div class="forum-home">
    <!-- 导航栏 -->
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

    <!-- 主内容区 -->
    <main class="main-content">
      <div class="content-wrapper">
        <!-- 左侧主区域 -->
        <div class="main">
          <!-- 发帖入口 + Tab切换 -->
          <div class="post-header">
            <div class="tab-buttons">
              <button :class="{ active: currentTab === 'latest' }" @click="switchTab('latest')">最新</button>
              <button :class="{ active: currentTab === 'hot' }" @click="switchTab('hot')">热门</button>
            </div>
            <router-link to="/forum/publish" class="btn-write">
              <span class="write-icon">✏️</span>
              发帖
            </router-link>
          </div>

          <!-- 帖子列表 -->
          <div class="post-list">
            <div v-if="loading" class="loading">加载中...</div>
            <div v-else-if="error" class="error">{{ error }}</div>
            <div v-else-if="posts.length === 0" class="empty">
              <p>暂无帖子</p>
              <router-link to="/forum/publish" class="btn-publish-empty">成为第一个发帖的人</router-link>
            </div>
            <div v-else>
              <div v-for="post in posts" :key="post.id" class="post-item" @click="router.push(`/forum/post/${post.id}`)">
                <div class="post-left">
                  <div class="post-title">
                    <span v-if="post.status === 1" class="badge essence">精华</span>
                    <span v-if="post.status === 2" class="badge top">置顶</span>
                    <span class="category-tag" v-if="post.categoryName">{{ post.categoryName }}</span>
                    <span class="title-text">{{ post.title }}</span>
                  </div>
                  <div class="post-meta">
                    <span class="author">{{ post.authorName }}</span>
                    <span class="time">{{ formatDate(post.createdAt) }}</span>
                  </div>
                </div>
                <div class="post-right">
                  <div class="stat">
                    <span class="stat-icon">🔥</span>
                    <span>{{ post.viewCount }}</span>
                  </div>
                  <div class="stat">
                    <span class="stat-icon">💬</span>
                    <span>{{ post.commentCount }}</span>
                  </div>
                  <div class="stat">
                    <span class="stat-icon">👍</span>
                    <span>{{ post.likeCount }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div class="pagination" v-if="totalPages > 1">
              <button :disabled="page === 0" @click="loadPosts(page - 1)">上一页</button>
              <span>{{ page + 1 }} / {{ totalPages }}</span>
              <button :disabled="page >= totalPages - 1" @click="loadPosts(page + 1)">下一页</button>
            </div>
          </div>
        </div>

        <!-- 右侧边栏 -->
        <aside class="sidebar">
          <!-- 分类导航 -->
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

          <!-- 全站热榜 -->
          <div class="sidebar-section">
            <h3>🔥 全站热榜</h3>
            <ul class="hot-list">
              <li v-for="(hotPost, index) in hotPosts" :key="hotPost.id" @click="router.push(`/forum/post/${hotPost.id}`)">
                <span class="rank" :class="{ top3: index < 3 }">{{ index + 1 }}</span>
                <span class="hot-title">{{ hotPost.title }}</span>
              </li>
            </ul>
          </div>

          <!-- 热门作者 -->
          <div class="sidebar-section">
            <h3>⭐ 热门作者</h3>
            <ul class="author-list">
              <li v-for="author in hotAuthors" :key="author.id" class="author-item">
                <div class="author-avatar">{{ author.username?.charAt(0).toUpperCase() }}</div>
                <div class="author-info">
                  <span class="author-name">{{ author.username }}</span>
                  <span class="author-posts">{{ author.postCount }} 帖</span>
                </div>
              </li>
            </ul>
          </div>
        </aside>
      </div>
    </main>

    <!-- 页脚 -->
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

const posts = ref([])
const categories = ref([])
const hotPosts = ref([])
const hotAuthors = ref([])
const loading = ref(true)
const error = ref('')
const page = ref(0)
const totalPages = ref(1)
const currentTab = ref('latest')

const token = localStorage.getItem('token')
const username = localStorage.getItem('username')
const role = localStorage.getItem('role')

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

const loadPosts = async (pageNum = 0) => {
  try {
    loading.value = true
    let url = `/forum?page=${pageNum}&size=20`
    if (currentTab.value === 'hot') {
      url = `/forum/hot?page=${pageNum}&size=20`
    }
    const response = await axios.get(url)
    posts.value = response.data.data?.content || response.data?.content || []
    totalPages.value = response.data.data?.totalPages || response.data?.totalPages || 1
    page.value = pageNum
  } catch (e) {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const response = await axios.get('/forum/categories')
    categories.value = response.data?.data || response.data || []
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

const loadHotPosts = async () => {
  try {
    const response = await axios.get('/forum/hot?size=10')
    hotPosts.value = response.data.data?.content || response.data?.content || []
  } catch (e) {
    console.error('加载热榜失败', e)
  }
}

const loadHotAuthors = async () => {
  try {
    const response = await axios.get('/forum/hot-authors')
    hotAuthors.value = response.data?.data || response.data || []
  } catch (e) {
    console.error('加载热门作者失败', e)
  }
}

const switchTab = (tab) => {
  currentTab.value = tab
  loadPosts(0)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadPosts()
  loadCategories()
  loadHotPosts()
  loadHotAuthors()
})
</script>

<style scoped>
.forum-home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

/* 导航栏 */
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

/* 主内容区 */
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

/* 左侧主区域 */
.main {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  overflow: hidden;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #eee;
  background: #fff;
}

.tab-buttons {
  display: flex;
  gap: 0.5rem;
}

.tab-buttons button {
  padding: 0.4rem 1rem;
  border: none;
  background: #f5f5f5;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.2s;
}

.tab-buttons button.active {
  background: #007bff;
  color: #fff;
}

.btn-write {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.4rem 1rem;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  text-decoration: none;
  color: #666;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-write:hover {
  border-color: #007bff;
  color: #007bff;
}

.write-icon {
  font-size: 14px;
}

/* 帖子列表 */
.post-list {
  padding: 0;
}

.post-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.2s;
}

.post-item:hover {
  background: #fafafa;
}

.post-item:last-child {
  border-bottom: none;
}

.post-left {
  flex: 1;
  min-width: 0;
}

.post-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.4rem;
  flex-wrap: wrap;
}

.badge {
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: normal;
  flex-shrink: 0;
}

.badge.essence {
  background: #ffc107;
  color: #000;
}

.badge.top {
  background: #dc3545;
  color: #fff;
}

.category-tag {
  padding: 2px 8px;
  background: #e3f2fd;
  color: #1976d2;
  border-radius: 3px;
  font-size: 12px;
  flex-shrink: 0;
}

.title-text {
  color: #333;
  font-size: 15px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-meta {
  display: flex;
  gap: 1rem;
  color: #999;
  font-size: 13px;
}

.post-right {
  display: flex;
  gap: 1.5rem;
  margin-left: 1rem;
}

.stat {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  color: #999;
  font-size: 13px;
  white-space: nowrap;
}

.stat-icon {
  font-size: 14px;
}

/* 右侧边栏 */
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

/* 分类列表 */
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

/* 热榜列表 */
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

/* 作者列表 */
.author-list {
  list-style: none;
  padding: 0.5rem 0;
  margin: 0;
}

.author-item {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.6rem 1rem;
  cursor: pointer;
  transition: background 0.2s;
}

.author-item:hover {
  background: #f8f9fa;
}

.author-avatar {
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
  flex-shrink: 0;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  color: #333;
  font-size: 13px;
  font-weight: 500;
}

.author-posts {
  color: #999;
  font-size: 12px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  gap: 1rem;
  padding: 1.2rem;
  align-items: center;
}

.pagination button {
  padding: 0.5rem 1rem;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  color: #666;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination button:hover:not(:disabled) {
  border-color: #007bff;
  color: #007bff;
}

.pagination span {
  color: #666;
  font-size: 14px;
}

/* 加载状态 */
.loading, .error, .empty {
  text-align: center;
  padding: 3rem;
  color: #999;
}

.empty p {
  margin-bottom: 1rem;
}

.btn-publish-empty {
  padding: 0.5rem 1rem;
  background: #007bff;
  color: #fff;
  border-radius: 4px;
  text-decoration: none;
  font-size: 14px;
}

.error {
  color: #dc3545;
}

/* 页脚 */
.footer {
  text-align: center;
  padding: 1rem;
  background: #fff;
  color: #999;
  font-size: 13px;
  margin-top: auto;
}

/* 响应式 */
@media (max-width: 900px) {
  .header {
    flex-wrap: wrap;
    gap: 1rem;
    padding: 0.8rem 1rem;
  }

  .nav-menu {
    order: 3;
    width: 100%;
    overflow-x: auto;
    gap: 0.2rem;
  }

  .nav-item {
    padding: 0.4rem 0.7rem;
    font-size: 14px;
    white-space: nowrap;
  }

  .content-wrapper {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }

  .sidebar-section:last-child {
    grid-column: span 2;
  }
}

@media (max-width: 600px) {
  .sidebar {
    grid-template-columns: 1fr;
  }

  .sidebar-section:last-child {
    grid-column: span 1;
  }

  .post-right {
    display: none;
  }
}
</style>
