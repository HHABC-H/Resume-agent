<template>
  <div class="publish-container">
    <div class="header">
      <router-link to="/forum">&larr; 返回论坛</router-link>
    </div>

    <div class="form">
      <h2>发布新帖子</h2>
      <div class="form-group">
        <label>标题</label>
        <input v-model="title" type="text" placeholder="请输入帖子标题" maxlength="200">
        <div class="error" v-if="titleError">{{ titleError }}</div>
      </div>
      <div class="form-group">
        <label>分类</label>
        <select v-model="categoryId">
          <option value="">请选择分类</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
        </select>
      </div>
      <div class="form-group">
        <label>内容</label>
        <textarea v-model="content" placeholder="请输入帖子内容" rows="15"></textarea>
        <div class="error" v-if="contentError">{{ contentError }}</div>
      </div>
      <button @click="submitPost" :disabled="submitting">
        {{ submitting ? '发布中...' : '发布帖子' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const title = ref('')
const content = ref('')
const categoryId = ref('')
const categories = ref([])
const titleError = ref('')
const contentError = ref('')
const submitting = ref(false)

const loadCategories = async () => {
  try {
    const response = await axios.get('/forum/categories')
    categories.value = response.data || []
  } catch (e) {
    console.error('加载分类失败', e)
  }
}

const submitPost = async () => {
  titleError.value = ''
  contentError.value = ''

  if (!title.value.trim()) {
    titleError.value = '标题不能为空'
    return
  }
  if (!content.value.trim()) {
    contentError.value = '内容不能为空'
    return
  }

  try {
    submitting.value = true
    const response = await axios.post('/forum/post', {
      title: title.value.trim(),
      content: content.value.trim(),
      categoryId: categoryId.value ? parseInt(categoryId.value) : null
    })
    if (response.data.id) {
      router.push(`/forum/post/${response.data.id}`)
    }
  } catch (e) {
    alert('发布失败: ' + (e.response?.data?.error || e.message))
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.publish-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.header a {
  color: #007bff;
  text-decoration: none;
}

.form {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.form h2 {
  margin-bottom: 20px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-group select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  min-height: 200px;
}

.error {
  color: #dc3545;
  font-size: 14px;
  margin-top: 5px;
}

.form button {
  padding: 12px 30px;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.form button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.form button:hover:not(:disabled) {
  background: #0056b3;
}
</style>
