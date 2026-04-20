<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h2>仪表盘</h2>
      <p>欢迎回来，{{ username }}！这里是系统概览</p>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon users-icon">👥</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalUsers }}</div>
          <div class="stat-label">总用户数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon resumes-icon">📄</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalResumes }}</div>
          <div class="stat-label">简历分析</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon interviews-icon">🎯</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalInterviews }}</div>
          <div class="stat-label">面试评估</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon active-icon">💡</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.activeUsers }}</div>
          <div class="stat-label">活跃用户</div>
        </div>
      </div>
    </div>
    
    <!-- 最近活动 -->
    <div class="recent-activity">
      <h3>最近活动</h3>
      <div class="activity-list">
        <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
          <div class="activity-icon">{{ activity.icon }}</div>
          <div class="activity-content">
            <div class="activity-text">{{ activity.text }}</div>
            <div class="activity-time">{{ activity.time }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

const username = localStorage.getItem('username') || '管理员'
const stats = ref({
  totalUsers: 0,
  totalResumes: 0,
  totalInterviews: 0,
  activeUsers: 0
})

const recentActivities = ref<any[]>([])

const loadStats = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/admin/stats', {
      headers: { Authorization: `Bearer ${token}` }
    })
    stats.value = response.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadRecentActivities = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/admin/recent-activities', {
      headers: { Authorization: `Bearer ${token}` }
    })
    recentActivities.value = response.data
  } catch (error) {
    console.error('加载最近活动失败:', error)
  }
}

onMounted(() => {
  loadStats()
  loadRecentActivities()
})
</script>

<style scoped>
.dashboard {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.dashboard-header {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #f0f0f0;
}

.dashboard-header h2 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1.8rem;
  font-weight: 600;
}

.dashboard-header p {
  margin: 0;
  color: #666;
  font-size: 1rem;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 2.5rem;
  margin-right: 1.5rem;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 最近活动 */
.recent-activity {
  margin-top: 2rem;
}

.recent-activity h3 {
  color: #2c3e50;
  font-size: 1.2rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.activity-list {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 1rem;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  padding: 1rem;
  border-bottom: 1px solid #e0e0e0;
  transition: background 0.2s ease;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-item:hover {
  background: rgba(52, 152, 219, 0.05);
  border-radius: 6px;
}

.activity-icon {
  font-size: 1.2rem;
  margin-right: 1rem;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
}

.activity-text {
  color: #2c3e50;
  margin-bottom: 0.25rem;
  font-size: 0.95rem;
}

.activity-time {
  color: #999;
  font-size: 0.8rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    flex-direction: column;
    text-align: center;
  }
  
  .stat-icon {
    margin-right: 0;
    margin-bottom: 1rem;
  }
}
</style>