<template>
  <div class="reading-page">
    <header class="header">
      <div class="logo" @click="router.push('/')">技术论坛</div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">论坛</router-link>
        <router-link to="/resume/upload" class="nav-item">简历助手</router-link>
        <router-link to="/reading" class="nav-item active">在线阅读</router-link>
        <router-link to="/interview/1" class="nav-item">面试助手</router-link>
        <router-link to="/profile" class="nav-item">个人信息</router-link>
        <router-link to="/history" class="nav-item">查看历史</router-link>
        <router-link to="/my-bookmarks" class="nav-item">我的收藏</router-link>
        <router-link to="/forum/essences" class="nav-item">精华帖</router-link>
        <router-link to="/forum/authors" class="nav-item">热门作者</router-link>
      </nav>
      <div class="header-right">
        <template v-if="isLoggedIn">
          <router-link to="/my-bookmarks" class="nav-item">我的收藏</router-link>
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
    </header>

    <main class="main-content">
      <div class="content-card">
        <div class="page-header">
          <button class="back-btn" @click="router.go(-1)">← 返回</button>
          <h1 class="page-title">在线阅读</h1>
        </div>

        <div class="search-bar">
          <input v-model="searchKeyword" type="text" placeholder="搜索文章..." class="search-input" @keyup.enter="handleSearch" />
          <button @click="handleSearch" class="search-btn">搜索</button>
        </div>

        <div class="category-nav">
          <button
            v-for="cat in categories"
            :key="cat"
            :class="['cat-btn', { active: selectedCategory === cat }]"
            @click="selectCategory(cat)"
          >
            {{ cat }}
          </button>
        </div>

        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="articles.length === 0" class="empty">
          <p>暂无文章</p>
        </div>
        <div v-else class="article-list">
          <div v-for="article in articles" :key="article.id" class="article-item" @click="viewArticle(article.id)">
            <div class="article-info">
              <h3 class="article-title">{{ article.title }}</h3>
              <div class="article-meta">
                <span class="category">{{ article.category }}</span>
                <span class="author">作者：{{ article.authorName }}</span>
                <span class="read-count">阅读：{{ article.readCount }}</span>
              </div>
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

    <footer class="footer">
      <p>&copy; 2024 Resume Agent. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { articleApi } from '../api/article'

const router = useRouter()

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

const articles = ref([])
const categories = ref(['全部', 'Java', 'Python', 'Frontend', 'DB', '架构', '面试技巧'])
const selectedCategory = ref('全部')
const searchKeyword = ref('')
const loading = ref(false)
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = 20

const loadArticles = async (page = 0) => {
  loading.value = true
  try {
    const params = { page, size: pageSize }
    if (selectedCategory.value !== '全部') {
      params.category = selectedCategory.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    const response = await articleApi.getArticles(params)
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

const loadCategories = async () => {
  try {
    const response = await articleApi.getCategories()
    if (response.data.code === 200 && response.data.data.length > 0) {
      categories.value = ['全部', ...response.data.data]
    }
  } catch (e) {
    console.log('使用默认分类')
  }
}

const selectCategory = (cat) => {
  selectedCategory.value = cat
  searchKeyword.value = ''
  loadArticles(0)
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    selectedCategory.value = '全部'
    loadArticles(0)
  }
}

const viewArticle = (id) => {
  router.push(`/article/${id}`)
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
.reading-page {
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

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  padding: 0.3rem 0.5rem;
  border-radius: 4px;
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

.content-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
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

.search-bar {
  display: flex;
  padding: 1rem 1.5rem;
  gap: 0.5rem;
}

.search-input {
  flex: 1;
  padding: 0.6rem 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.search-btn {
  padding: 0.6rem 1.5rem;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.category-nav {
  display: flex;
  gap: 0.5rem;
  padding: 0 1.5rem 1rem;
  flex-wrap: wrap;
}

.cat-btn {
  padding: 0.4rem 1rem;
  background: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.2s;
}

.cat-btn:hover {
  border-color: #007bff;
  color: #007bff;
}

.cat-btn.active {
  background: #007bff;
  border-color: #007bff;
  color: #fff;
}

.loading, .empty {
  padding: 3rem;
  text-align: center;
  color: #666;
}

.article-list {
  padding: 0 1.5rem 1rem;
}

.article-item {
  padding: 1rem;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
}

.article-item:hover {
  border-color: #007bff;
  background: #f9f9f9;
}

.article-title {
  margin: 0 0 0.5rem 0;
  font-size: 16px;
  color: #333;
}

.article-meta {
  display: flex;
  gap: 1rem;
  font-size: 13px;
  color: #666;
}

.category {
  color: #007bff;
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

.footer {
  text-align: center;
  padding: 1rem;
  background: #fff;
  color: #999;
  font-size: 13px;
}
</style>