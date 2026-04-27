<template>
  <div class="forum-home">
    <header class="header">
      <div class="logo" @click="router.push('/')">技术论坛</div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">论坛</router-link>
<router-link to="/resume/upload" class="nav-item active">简历助手</router-link>
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
            <h1 class="page-title">上传简历</h1>
          </div>

          <div class="upload-content">
            <div class="upload-form">
              <div class="form-group">
                <label for="resumeFile">选择简历文件</label>
                <input type="file" id="resumeFile" @change="handleFileChange" accept=".txt,.pdf,.doc,.docx">
              </div>
              <div class="form-group">
                <label for="positionType">目标职位类型</label>
                <select id="positionType" v-model="positionType">
                  <option value="BACKEND_JAVA">后端 Java</option>
                  <option value="FRONTEND">前端</option>
                  <option value="ALGORITHM">算法</option>
                </select>
              </div>
              <button @click="uploadResume" class="btn btn-primary" :disabled="!selectedFile || loading">
                {{ loading ? '分析中...' : '上传并分析' }}
              </button>
            </div>
            <div v-if="error" class="error-message">
              {{ error }}
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

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const token = localStorage.getItem('token')
const username = localStorage.getItem('username')
const role = localStorage.getItem('role')

const isLoggedIn = computed(() => !!token)

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userId')
  localStorage.removeItem('role')
  router.push('/login')
}

const selectedFile = ref<File | null>(null)
const positionType = ref('BACKEND_JAVA')
const loading = ref(false)
const error = ref('')

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
    hotPosts.value = response.data.content || []
  } catch (e) {
    console.error('加载热榜失败', e)
  }
}

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    selectedFile.value = target.files[0]
  }
}

const uploadResume = async () => {
  if (!selectedFile.value) return

  loading.value = true
  error.value = ''

  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('positionType', positionType.value)

    const response = await axios.post('/api/resume/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': `Bearer ${token}`
      }
    })

    if (response.data.resumeId) {
      router.push(`/resume/analysis/${response.data.resumeId}`)
    }
  } catch (err: any) {
    error.value = err.response?.data?.error || err.response?.data?.message || '上传失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!token) {
    router.push('/login')
    return
  }
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

.upload-content {
  padding: 1.5rem;
}

.upload-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 500px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  color: #495757;
}

.form-group input[type="file"] {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.form-group select {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  margin-top: 1rem;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #0069d9;
}

.btn-primary:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.error-message {
  margin-top: 1rem;
  padding: 0.75rem;
  background-color: #f8d7da;
  color: #721c24;
  border-radius: 4px;
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