<template>
  <div class="article-detail-page">
    <header class="header">
      <div class="logo" @click="router.push('/')">技术论坛</div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">论坛</router-link>
        <router-link to="/resume/upload" class="nav-item">简历助手</router-link>
        <router-link to="/my-resumes" class="nav-item">我的简历</router-link>
        <router-link to="/reading" class="nav-item active">在线阅读</router-link>
      </nav>
      <div class="header-right">
        <button @click="handleToggleBookmark" :class="['btn-bookmark', { active: isBookmarked }]">
          {{ isBookmarked ? '已收藏' : '收藏' }}
        </button>
        <button @click="router.push('/reading')" class="btn-back">返回列表</button>
      </div>
    </header>

    <main class="main-content">
      <div class="content-card" v-if="article">
        <div class="article-header">
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="article-meta">
            <span class="category">{{ article.category }}</span>
            <span class="author">作者：{{ article.authorName }}</span>
            <span class="read-count">阅读：{{ article.readCount }}</span>
            <span class="date">{{ formatDate(article.createdAt) }}</span>
          </div>
        </div>

        <div class="article-tags" v-if="article.tags">
          <span v-for="tag in article.tags.split(',')" :key="tag" class="tag">{{ tag.trim() }}</span>
        </div>

        <div class="article-content" v-html="article.content"></div>
      </div>

      <div v-else class="loading">加载中...</div>
    </main>

    <footer class="footer">
      <p>&copy; 2024 Resume Agent. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { articleApi } from '../api/article'

const router = useRouter()
const route = useRoute()

const article = ref(null)
const isBookmarked = ref(false)
const isLoggedIn = ref(!!localStorage.getItem('token'))

const loadArticle = async () => {
  try {
    const response = await articleApi.getArticleDetail(route.params.id)
    if (response.data.code === 200) {
      article.value = response.data.data
      isBookmarked.value = response.data.data.isBookmarked || false
    }
  } catch (e) {
    console.error('加载文章失败', e)
  }
}

const handleToggleBookmark = async () => {
  if (!isLoggedIn.value) {
    alert('请先登录')
    router.push('/login')
    return
  }
  try {
    await articleApi.toggleBookmark(route.params.id)
    isBookmarked.value = !isBookmarked.value
  } catch (e) {
    alert('操作失败')
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

onMounted(() => {
  loadArticle()
})
</script>

<style scoped>
.article-detail-page {
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

.btn-bookmark {
  padding: 0.5rem 1rem;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
}

.btn-bookmark.active {
  background: #ffc107;
  border-color: #ffc107;
  color: #333;
}

.btn-back {
  padding: 0.5rem 1rem;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
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
  padding: 2rem;
}

.article-header {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.article-title {
  margin: 0 0 1rem 0;
  font-size: 24px;
  color: #333;
  line-height: 1.4;
}

.article-meta {
  display: flex;
  gap: 1.5rem;
  font-size: 14px;
  color: #666;
}

.category {
  color: #007bff;
  font-weight: 500;
}

.article-tags {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-bottom: 1.5rem;
}

.tag {
  padding: 0.3rem 0.8rem;
  background: #f0f7ff;
  color: #007bff;
  border-radius: 15px;
  font-size: 13px;
}

.article-content {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
}

.loading {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.footer {
  text-align: center;
  padding: 1rem;
  background: #fff;
  color: #999;
  font-size: 13px;
}
</style>