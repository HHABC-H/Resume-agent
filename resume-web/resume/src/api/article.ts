import axios from 'axios'

const API_BASE = '/api'

const getHeaders = () => ({
  headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
})

export const articleApi = {
  getArticles: (params: { category?: string; keyword?: string; page?: number; size?: number }) =>
    axios.get(`${API_BASE}/article`, { params, ...getHeaders() }),
  getArticleDetail: (id: number) => axios.get(`${API_BASE}/article/${id}`, getHeaders()),
  getCategories: () => axios.get(`${API_BASE}/article/categories`),
  getBookmarks: (page = 0, size = 20) =>
    axios.get(`${API_BASE}/article/bookmark?page=${page}&size=${size}`, getHeaders()),
  toggleBookmark: (id: number) => axios.post(`${API_BASE}/article/${id}/bookmark`, {}, getHeaders()),
  createArticle: (data: any) => axios.post(`${API_BASE}/article`, data, getHeaders()),
  updateArticle: (id: number, data: any) => axios.put(`${API_BASE}/article/${id}`, data, getHeaders()),
  deleteArticle: (id: number) => axios.delete(`${API_BASE}/article/${id}`, getHeaders())
}