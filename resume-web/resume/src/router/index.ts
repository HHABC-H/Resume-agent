import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue')
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/auth/Login.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/auth/Register.vue')
    },
    {
      path: '/resume/upload',
      name: 'resumeUpload',
      component: () => import('../views/resume/Upload.vue')
    },
    {
      path: '/resume/analysis/:resumeId',
      name: 'resumeAnalysis',
      component: () => import('../views/resume/Analysis.vue')
    },
    {
      path: '/interview/:resumeId',
      name: 'interview',
      component: () => import('../views/interview/Interview.vue')
    },
    {
      path: '/interview/result/:resumeId',
      name: 'interviewResult',
      component: () => import('../views/interview/Result.vue')
    },
    {
      path: '/history',
      name: 'history',
      component: () => import('../views/History.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/user/Profile.vue')
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/admin/AdminDashboard.vue')
    }
  ],
})

export default router