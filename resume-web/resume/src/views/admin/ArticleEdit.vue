<template>
  <div class="article-edit-page">
    <div class="page-header">
      <button class="back-btn" @click="router.push('/admin/article-manage')">← 返回</button>
      <h1 class="page-title">{{ isEdit ? '编辑文章' : '新建文章' }}</h1>
    </div>

    <div class="form-container">
      <div class="form-group">
        <label>标题</label>
        <input v-model="formData.title" type="text" placeholder="请输入文章标题" class="form-input" />
      </div>

      <div class="form-group">
        <label>分类</label>
        <select v-model="formData.category" class="form-select">
          <option value="">请选择分类</option>
          <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
        </select>
      </div>

      <div class="form-group">
        <label>标签（用逗号分隔）</label>
        <input v-model="formData.tags" type="text" placeholder="例如：Java,Spring,后端" class="form-input" />
      </div>

      <div class="form-group">
        <label>内容</label>
        <textarea v-model="formData.content" placeholder="请输入文章内容" class="form-textarea" rows="20"></textarea>
      </div>

      <div class="form-actions">
        <button @click="handleSave" :disabled="saving" class="btn-save">
          {{ saving ? '保存中...' : '保存' }}
        </button>
        <button @click="router.push('/admin/article-manage')" class="btn-cancel">取消</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

const API_BASE = '/api'
const isEdit = ref(false)
const saving = ref(false)
const articleId = ref(null)

const categories = ref(['Java', 'Python', 'Frontend', 'DB', '架构', '面试技巧'])

const formData = reactive({
  title: '',
  category: '',
  tags: '',
  content: ''
})

const getHeaders = () => ({
  headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
})

const loadArticle = async () => {
  try {
    const response = await axios.get(`${API_BASE}/article/${articleId.value}`, getHeaders())
    if (response.data.code === 200) {
      const article = response.data.data
      formData.title = article.title
      formData.category = article.category
      formData.tags = article.tags || ''
      formData.content = article.content
    }
  } catch (e) {
    alert('加载文章失败')
  }
}

const handleSave = async () => {
  if (!formData.title.trim()) {
    alert('请输入标题')
    return
  }
  if (!formData.category) {
    alert('请选择分类')
    return
  }
  if (!formData.content.trim()) {
    alert('请输入内容')
    return
  }

  saving.value = true
  try {
    const data = {
      title: formData.title,
      category: formData.category,
      tags: formData.tags,
      content: formData.content
    }

    let response
    if (isEdit.value) {
      response = await axios.put(`${API_BASE}/article/${articleId.value}`, data, getHeaders())
    } else {
      response = await axios.post(`${API_BASE}/article`, data, getHeaders())
    }

    if (response.data.code === 200) {
      alert('保存成功')
      router.push('/admin/article-manage')
    }
  } catch (e) {
    alert('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  if (route.params.id) {
    isEdit.value = true
    articleId.value = route.params.id
    loadArticle()
  }
})
</script>

<style scoped>
.article-edit-page {
  padding: 1.5rem;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.back-btn {
  padding: 0.5rem 1rem;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.form-container {
  background: #fff;
  border-radius: 8px;
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #333;
}

.form-input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-select {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-textarea {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.btn-save {
  padding: 0.8rem 2rem;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-save:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-cancel {
  padding: 0.8rem 2rem;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
}
</style>