import { createApp } from 'vue'
import { createPinia } from 'pinia'
import axios from 'axios'

import App from './App.vue'
import router from './router'

// 配置 axios
axios.defaults.baseURL = '/api'
axios.defaults.timeout = 10000 // 设置 10 秒超时

// 请求拦截器
axios.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
axios.interceptors.response.use(
  response => {
    return response
  },
  error => {
    // 处理超时错误
    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      console.error('请求超时，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
