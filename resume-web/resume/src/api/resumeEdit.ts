import axios from 'axios'

const API_BASE = '/api'

const getHeaders = () => ({
  headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
})

export const resumeEditApi = {
  saveDraft: (data: any) => axios.post(`${API_BASE}/resume/edit/draft`, data, getHeaders()),
  updateDraft: (data: any) => axios.put(`${API_BASE}/resume/edit/draft`, data, getHeaders()),
  getCurrentDraft: () => axios.get(`${API_BASE}/resume/edit/draft`, getHeaders()),
  saveAsVersion: (data: any) => axios.post(`${API_BASE}/resume/edit/save`, data, getHeaders()),
  getHistory: (page = 0, size = 10) => axios.get(`${API_BASE}/resume/edit/history?page=${page}&size=${size}`, getHeaders()),
  getHistoryDetail: (id: number) => axios.get(`${API_BASE}/resume/edit/history/${id}`, getHeaders()),
  deleteHistory: (id: number) => axios.delete(`${API_BASE}/resume/edit/history/${id}`, getHeaders())
}