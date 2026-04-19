<template>
  <div class="login-container">
    <div class="login-form">
      <h2>登录</h2>
      <form @submit.prevent="login">
        <div class="form-group">
          <label for="username">用户名</label>
          <input type="text" id="username" v-model="form.username" required>
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input type="password" id="password" v-model="form.password" required>
        </div>
        <button type="submit" class="btn btn-primary" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
        <p class="register-link">
          还没有账号？<router-link to="/register">注册</router-link>
        </p>
      </form>
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const form = ref({
  username: '',
  password: ''
})
const error = ref('')
const loading = ref(false)

const login = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await axios.post('/auth/login', form.value)
    if (response.data.token) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('username', response.data.username)
      localStorage.setItem('userId', response.data.userId)
      
      const userResponse = await axios.get('/auth/me', {
        headers: { Authorization: `Bearer ${response.data.token}` }
      })
      const role = userResponse.data.role || 'USER'
      localStorage.setItem('role', role)
      
      // 根据角色重定向
      if (role === 'ADMIN') {
        router.push('/admin')
      } else {
        router.push('/')
      }
    }
  } catch (err: any) {
    error.value = err.response?.data?.error || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.login-form {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #333;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

button {
  width: 100%;
  padding: 0.75rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  margin-top: 1rem;
}

button:hover:not(:disabled) {
  background-color: #0069d9;
}

button:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.register-link {
  text-align: center;
  margin-top: 1rem;
}

.error-message {
  margin-top: 1rem;
  padding: 0.75rem;
  background-color: #f8d7da;
  color: #721c24;
  border-radius: 4px;
}
</style>