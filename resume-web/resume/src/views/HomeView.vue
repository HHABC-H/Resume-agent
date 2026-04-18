<template>
  <div class="home-container">
    <header class="header">
      <div class="logo">Resume Agent</div>
      <div class="header-content">
        <nav class="nav-menu">
          <router-link to="/resume/upload" class="nav-item">上传简历</router-link>
          <router-link to="/history" class="nav-item">查看历史</router-link>
          <router-link to="/profile" class="nav-item">个人资料</router-link>
          <template v-if="isAdmin">
            <router-link to="/admin" class="nav-item">管理后台</router-link>
          </template>
        </nav>
        <nav class="user-nav">
          <template v-if="isLoggedIn">
            <span class="user-info">{{ username }}</span>
            <button @click="logout" class="btn btn-text">退出</button>
          </template>
          <template v-else>
            <router-link to="/login" class="btn btn-text">登录</router-link>
            <router-link to="/register" class="btn btn-primary">注册</router-link>
          </template>
        </nav>
      </div>
    </header>

    <main class="main-content">
      <div class="hero-section">
        <h1>智能简历分析与模拟面试平台</h1>
        <p>帮助您提升简历质量，掌握面试技巧</p>
        <div class="hero-actions">
          <template v-if="isLoggedIn">
            <router-link to="/resume/upload" class="btn btn-primary btn-lg">
              上传简历
            </router-link>
            <router-link to="/history" class="btn btn-secondary btn-lg">
              查看历史
            </router-link>
          </template>
          <template v-else>
            <router-link to="/login" class="btn btn-primary btn-lg">
              开始使用
            </router-link>
          </template>
        </div>
      </div>

      <div class="features-section">
        <h2>核心功能</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">📄</div>
            <h3>简历分析</h3>
            <p>智能分析简历内容，提供详细的评分和改进建议</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">💬</div>
            <h3>模拟面试</h3>
            <p>根据简历和目标职位生成个性化面试问题</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📊</div>
            <h3>面试评估</h3>
            <p>对回答进行专业评估，提供详细的反馈和建议</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📝</div>
            <h3>历史记录</h3>
            <p>保存面试历史，方便查看和对比不同面试表现</p>
          </div>
        </div>
      </div>
    </main>

    <footer class="footer">
      <p>&copy; 2024 Resume Agent. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const token = localStorage.getItem('token')
const username = localStorage.getItem('username')
const role = localStorage.getItem('role')

const isLoggedIn = computed(() => !!token)
const isAdmin = computed(() => role === 'ADMIN')

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userId')
  router.push('/')
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: #007bff;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.nav-item {
  color: #495057;
  text-decoration: none;
  font-weight: 500;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  transition: background-color 0.2s, color 0.2s;
}

.nav-item:hover {
  background-color: #f8f9fa;
  color: #007bff;
}

.user-nav {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-info {
  color: #495057;
  font-weight: 500;
}

.main-content {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.hero-section {
  text-align: center;
  padding: 3rem 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
  margin-bottom: 3rem;
}

.hero-section h1 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.hero-section p {
  font-size: 1.25rem;
  margin-bottom: 2rem;
  opacity: 0.9;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.features-section h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.feature-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: transform 0.2s, box-shadow 0.2s;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 12px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  margin-bottom: 0.5rem;
  color: #333;
}

.feature-card p {
  color: #6c757d;
  line-height: 1.5;
}

.footer {
  text-align: center;
  padding: 1.5rem;
  background-color: #f8f9fa;
  color: #6c757d;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  text-decoration: none;
  display: inline-block;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover {
  background-color: #0069d9;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #5a6268;
}

.btn-text {
  background: none;
  color: #007bff;
}

.btn-text:hover {
  text-decoration: underline;
}

.btn-lg {
  padding: 0.75rem 1.5rem;
  font-size: 1.1rem;
}

@media (max-width: 768px) {
  .hero-section h1 {
    font-size: 1.75rem;
  }

  .hero-actions {
    flex-direction: column;
  }
}
</style>