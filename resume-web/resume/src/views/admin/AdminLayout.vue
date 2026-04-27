<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="admin-sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <h2>管理后台</h2>
          <span class="version">v1.0</span>
        </div>
      </div>
      
      <nav class="sidebar-nav">
        <router-link to="/admin/dashboard" class="nav-item">
          <i class="nav-icon">📊</i>
          <span>仪表盘</span>
        </router-link>
        <router-link to="/admin/users" class="nav-item">
          <i class="nav-icon">👥</i>
          <span>用户管理</span>
        </router-link>
        <router-link to="/admin/resume-history" class="nav-item">
          <i class="nav-icon">📄</i>
          <span>简历管理</span>
        </router-link>
        <router-link to="/admin/interview-history" class="nav-item">
          <i class="nav-icon">🎯</i>
          <span>面试管理</span>
        </router-link>
        <router-link to="/admin/permissions" class="nav-item">
          <i class="nav-icon">🔐</i>
          <span>权限管理</span>
        </router-link>
        <router-link to="/admin/article-manage" class="nav-item">
          <i class="nav-icon">📝</i>
          <span>文章管理</span>
        </router-link>
      </nav>
      
      <div class="sidebar-footer">
        <div class="user-info">
          <span class="username">{{ username }}</span>
          <button @click="logout" class="logout-btn">
            <i>🚪</i>
            <span>退出</span>
          </button>
        </div>
      </div>
    </aside>
    
    <!-- 主内容区 -->
    <main class="admin-main">
      <header class="main-header">
        <div class="header-title">
          <h1>{{ pageTitle }}</h1>
        </div>
        <div class="header-actions">
          <span class="current-date">{{ currentDate }}</span>
        </div>
      </header>
      
      <div class="main-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = localStorage.getItem('username') || '管理员'

const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

const pageTitle = computed(() => {
  const path = router.currentRoute.value.path
  const titles: Record<string, string> = {
    '/admin/dashboard': '仪表盘',
    '/admin/users': '用户管理',
    '/admin/resume-history': '简历管理',
    '/admin/interview-history': '面试管理',
    '/admin/system-config': '系统配置',
    '/admin/permissions': '权限管理',
    '/admin/article-manage': '文章管理',
    '/admin/article-edit': '文章编辑'
  }
  return titles[path] || '管理后台'
})

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('userId')
  localStorage.removeItem('role')
  router.push('/login')
}

onMounted(() => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  if (!token || role !== 'ADMIN') {
    router.push('/login')
  }
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 侧边栏 */
.admin-sidebar {
  width: 260px;
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  color: white;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
  position: fixed;
  height: 100vh;
  overflow-y: auto;
}

.sidebar-header {
  padding: 2rem 1.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #ffffff;
}

.logo .version {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 0.25rem;
  display: block;
}

.sidebar-nav {
  flex: 1;
  padding: 1rem 0;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-left-color: #3498db;
}

.nav-item.router-link-active {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border-left-color: #3498db;
  font-weight: 500;
}

.nav-icon {
  font-size: 1.2rem;
  margin-right: 1rem;
  width: 24px;
  text-align: center;
}

.sidebar-footer {
  padding: 1.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  margin-top: auto;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.username {
  font-weight: 500;
  color: white;
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 6px;
  color: white;
  cursor: pointer;
  transition: background 0.3s ease;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 主内容区 */
.admin-main {
  flex: 1;
  margin-left: 260px;
  min-height: 100vh;
}

.main-header {
  background: white;
  padding: 1.5rem 2rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-title h1 {
  margin: 0;
  font-size: 1.5rem;
  color: #2c3e50;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.current-date {
  color: #666;
  font-size: 0.9rem;
}

.main-content {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-sidebar {
    width: 240px;
  }
  
  .admin-main {
    margin-left: 240px;
  }
  
  .main-content {
    padding: 1rem;
  }
}
</style>