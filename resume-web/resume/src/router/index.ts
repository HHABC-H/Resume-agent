import { createRouter, createWebHistory } from 'vue-router'

const routes = [
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
    path: '/forum',
    name: 'forumIndex',
    component: () => import('../views/forum/Index.vue')
  },
  {
    path: '/forum/post/:id',
    name: 'forumPost',
    component: () => import('../views/forum/PostDetail.vue')
  },
  {
    path: '/forum/publish',
    name: 'forumPublish',
    component: () => import('../views/forum/Publish.vue')
  },
  {
    path: '/forum/category/:id',
    name: 'forumCategory',
    component: () => import('../views/forum/Category.vue')
  },
  {
    path: '/forum/essences',
    name: 'forumEssences',
    component: () => import('../views/forum/Essences.vue')
  },
  {
    path: '/admin',
    name: 'admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'adminDashboard',
        component: () => import('../views/admin/Dashboard.vue')
      },
      {
        path: 'users',
        name: 'adminUsers',
        component: () => import('../views/admin/Users.vue')
      },
      {
        path: 'resume-history',
        name: 'adminResumeHistory',
        component: () => import('../views/admin/ResumeHistory.vue')
      },
      {
        path: 'interview-history',
        name: 'adminInterviewHistory',
        component: () => import('../views/admin/InterviewHistory.vue')
      },
      {
        path: 'permissions',
        name: 'adminPermissions',
        component: () => import('../views/admin/Permissions.vue')
      },
      {
        path: '',
        redirect: '/admin/dashboard'
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  // 公开路由（不需要登录）- 精确匹配
  const publicRoutes = ['/', '/login', '/register', '/forum', '/forum/essences']
  // 公开路由 - 前缀匹配
  const publicPrefixRoutes = ['/forum/post/', '/forum/category/']

  // 管理员路由（以/admin开头）
  const isAdminRoute = to.path.startsWith('/admin')

  // 检查是否访问公开路由
  const isPublicRoute = publicRoutes.includes(to.path) ||
    publicPrefixRoutes.some(prefix => to.path.startsWith(prefix)) ||
    to.path === '/forum/publish'  // 发布页需要登录

  if (isPublicRoute) {
    return true
  }

  // 需要登录的路由检查
  if (!token) {
    return '/login'
  }

  // 管理员路由检查
  if (isAdminRoute) {
    if (role === 'ADMIN') {
      return true
    } else {
      return '/'
    }
  }

  // 普通用户路由检查
  if (role === 'ADMIN') {
    return '/admin'
  }

  return true
})

  // 检查是否已登录
  if (!token) {
    return '/login'
  }

  // 检查管理员访问权限
  if (isAdminRoute) {
    if (role === 'ADMIN') {
      return true
    } else {
      // 非管理员不能访问管理后台
      return '/'
    }
  }

  // 检查普通用户路由访问权限
  if (isUserRoute) {
    if (role === 'ADMIN') {
      // 管理员不能访问普通用户路由，重定向到管理后台
      return '/admin'
    } else {
      return true
    }
  }

  // 默认情况
  return true
})

export default router