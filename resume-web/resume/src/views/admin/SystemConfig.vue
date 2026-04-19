<template>
  <div class="system-config">
    <div class="page-header">
      <h2>系统配置</h2>
      <p>配置AI模型参数、管理提示词模板和系统限制</p>
    </div>
    
    <!-- AI模型参数 -->
    <div class="card">
      <div class="card-header">
        <h3>AI模型参数</h3>
        <div>
          <button v-if="!aiConfigEditing" @click="aiConfigEditing = true" class="btn btn-sm btn-primary">编辑</button>
          <div v-else>
            <button @click="updateAIConfig" class="btn btn-sm btn-success">保存</button>
            <button @click="aiConfigEditing = false" class="btn btn-sm btn-secondary">取消</button>
          </div>
        </div>
      </div>
      
      <div v-if="aiConfigLoading" class="loading">加载中...</div>
      <div v-else-if="aiConfigError" class="error-message">{{ aiConfigError }}</div>
      <div v-else class="ai-config-form">
        <div class="form-group">
          <label>模型</label>
          <input v-if="aiConfigEditing" type="text" v-model="aiConfig.model" class="form-control">
          <span v-else>{{ aiConfig.model }}</span>
        </div>
        <div class="form-group">
          <label>API Key</label>
          <input v-if="aiConfigEditing" type="text" v-model="aiConfig.apiKey" class="form-control">
          <span v-else>{{ aiConfig.apiKey }}</span>
        </div>
        <div class="form-group">
          <label>最大重试次数</label>
          <input v-if="aiConfigEditing" type="number" v-model="aiConfig.retryMaxAttempts" class="form-control">
          <span v-else>{{ aiConfig.retryMaxAttempts }}</span>
        </div>
        <div class="form-group">
          <label>重试延迟(秒)</label>
          <input v-if="aiConfigEditing" type="number" v-model="aiConfig.retryDelaySeconds" class="form-control">
          <span v-else>{{ aiConfig.retryDelaySeconds }}</span>
        </div>
        <div class="form-group">
          <label>API基础URL</label>
          <input v-if="aiConfigEditing" type="text" v-model="aiConfig.apiBaseUrl" class="form-control">
          <span v-else>{{ aiConfig.apiBaseUrl }}</span>
        </div>
      </div>
    </div>
    
    <!-- 系统限制 -->
    <div class="card mt-4">
      <div class="card-header">
        <h3>系统限制</h3>
        <div>
          <button v-if="!systemLimitsEditing" @click="systemLimitsEditing = true" class="btn btn-sm btn-primary">编辑</button>
          <div v-else>
            <button @click="updateSystemLimits" class="btn btn-sm btn-success">保存</button>
            <button @click="systemLimitsEditing = false" class="btn btn-sm btn-secondary">取消</button>
          </div>
        </div>
      </div>
      
      <div v-if="systemLimitsLoading" class="loading">加载中...</div>
      <div v-else-if="systemLimitsError" class="error-message">{{ systemLimitsError }}</div>
      <div v-else class="system-limits-form">
        <div class="form-group">
          <label>简历上传大小限制(MB)</label>
          <input v-if="systemLimitsEditing" type="number" v-model="systemLimits.resumeUploadSizeLimit" class="form-control">
          <span v-else>{{ systemLimits.resumeUploadSizeLimit }} MB</span>
        </div>
        <div class="form-group">
          <label>面试问题数量</label>
          <input v-if="systemLimitsEditing" type="number" v-model="systemLimits.interviewQuestionCount" class="form-control">
          <span v-else>{{ systemLimits.interviewQuestionCount }}</span>
        </div>
        <div class="form-group">
          <label>最大并发请求数</label>
          <input v-if="systemLimitsEditing" type="number" v-model="systemLimits.maxConcurrentRequests" class="form-control">
          <span v-else>{{ systemLimits.maxConcurrentRequests }}</span>
        </div>
      </div>
    </div>
    
    <!-- 提示词模板 -->
    <div class="card mt-4">
      <div class="card-header">
        <h3>提示词模板</h3>
      </div>
      
      <div v-if="promptTemplatesLoading" class="loading">加载中...</div>
      <div v-else-if="promptTemplatesError" class="error-message">{{ promptTemplatesError }}</div>
      <div v-else class="prompt-templates-content">
        <div v-if="promptTemplates.message" class="message">
          {{ promptTemplates.message }}
        </div>
        <div v-else>
          <div class="templates-list">
            <!-- 这里可以添加提示词模板的具体内容 -->
            <div class="template-item">
              <h4>简历分析提示词</h4>
              <p>用于分析简历的AI提示词模板</p>
              <button class="btn btn-sm btn-primary">编辑</button>
            </div>
            <div class="template-item">
              <h4>面试问题生成提示词</h4>
              <p>用于生成面试问题的AI提示词模板</p>
              <button class="btn btn-sm btn-primary">编辑</button>
            </div>
            <div class="template-item">
              <h4>面试评估提示词</h4>
              <p>用于评估面试回答的AI提示词模板</p>
              <button class="btn btn-sm btn-primary">编辑</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

const aiConfig = ref({
  model: '',
  apiKey: '',
  retryMaxAttempts: 3,
  retryDelaySeconds: 2,
  apiBaseUrl: ''
})
const aiConfigLoading = ref(false)
const aiConfigError = ref('')
const aiConfigEditing = ref(false)

const systemLimits = ref({
  resumeUploadSizeLimit: 10,
  interviewQuestionCount: 5,
  maxConcurrentRequests: 10
})
const systemLimitsLoading = ref(false)
const systemLimitsError = ref('')
const systemLimitsEditing = ref(false)

const promptTemplates = ref<any[]>([])
const promptTemplatesLoading = ref(false)
const promptTemplatesError = ref('')

const loadAIConfig = async () => {
  aiConfigLoading.value = true
  aiConfigError.value = ''
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/admin/ai-config', {
      headers: { Authorization: `Bearer ${token}` }
    })
    aiConfig.value = response.data
  } catch (err: any) {
    aiConfigError.value = err.response?.data?.error || '加载AI配置失败'
  } finally {
    aiConfigLoading.value = false
  }
}

const updateAIConfig = async () => {
  try {
    const token = localStorage.getItem('token')
    await axios.put('/admin/ai-config', aiConfig.value, {
      headers: { Authorization: `Bearer ${token}` }
    })
    aiConfigEditing.value = false
  } catch (err: any) {
    aiConfigError.value = err.response?.data?.error || '更新AI配置失败'
  }
}

const loadSystemLimits = async () => {
  systemLimitsLoading.value = true
  systemLimitsError.value = ''
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/admin/system-limits', {
      headers: { Authorization: `Bearer ${token}` }
    })
    systemLimits.value = response.data
  } catch (err: any) {
    systemLimitsError.value = err.response?.data?.error || '加载系统限制失败'
  } finally {
    systemLimitsLoading.value = false
  }
}

const updateSystemLimits = async () => {
  try {
    const token = localStorage.getItem('token')
    await axios.put('/admin/system-limits', systemLimits.value, {
      headers: { Authorization: `Bearer ${token}` }
    })
    systemLimitsEditing.value = false
  } catch (err: any) {
    systemLimitsError.value = err.response?.data?.error || '更新系统限制失败'
  }
}

const loadPromptTemplates = async () => {
  promptTemplatesLoading.value = true
  promptTemplatesError.value = ''
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/admin/prompt-templates', {
      headers: { Authorization: `Bearer ${token}` }
    })
    promptTemplates.value = response.data
  } catch (err: any) {
    promptTemplatesError.value = err.response?.data?.error || '加载提示词模板失败'
  } finally {
    promptTemplatesLoading.value = false
  }
}

onMounted(() => {
  loadAIConfig()
  loadSystemLimits()
  loadPromptTemplates()
})
</script>

<style scoped>
.system-config {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.page-header {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #f0f0f0;
}

.page-header h2 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1.8rem;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 1rem;
}

.card {
  background: #f9f9f9;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 1.5rem;
}

.card.mt-4 {
  margin-top: 2rem;
}

.card-header {
  background: white;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.2rem;
  font-weight: 600;
}

.loading, .error-message {
  padding: 4rem 0;
  text-align: center;
}

.error-message {
  background-color: #f8d7da;
  color: #721c24;
  padding: 1rem;
  border-radius: 6px;
  margin: 1rem;
}

.ai-config-form,
.system-limits-form {
  padding: 2rem;
  background: white;
}

.form-group {
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 2rem;
}

.form-group label {
  flex: 0 0 200px;
  font-weight: 500;
  color: #2c3e50;
  white-space: nowrap;
}

.form-control {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-control:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.75rem;
}

.btn-primary {
  background-color: #3498db;
  color: white;
}

.btn-primary:hover {
  background-color: #2980b9;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #5a6268;
}

.btn-success {
  background-color: #28a745;
  color: white;
}

.btn-success:hover {
  background-color: #218838;
}

.prompt-templates-content {
  padding: 2rem;
  background: white;
}

.message {
  padding: 2rem;
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  text-align: center;
  color: #666;
}

.templates-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.template-item {
  background: #f8f9fa;
  padding: 1.5rem;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.template-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.template-item h4 {
  margin: 0 0 0.5rem 0;
  color: #2c3e50;
  font-size: 1.1rem;
  font-weight: 600;
}

.template-item p {
  margin: 0 0 1rem 0;
  color: #666;
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .system-config {
    padding: 1rem;
  }
  
  .form-group {
    flex-direction: column;
    align-items: stretch;
    gap: 0.5rem;
  }
  
  .form-group label {
    flex: none;
  }
  
  .templates-list {
    grid-template-columns: 1fr;
  }
}
</style>