import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores'
import { ElMessage } from 'element-plus'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/login',
    children: [
      // 学生端路由
      {
        path: 'student/dashboard',
        name: 'StudentDashboard',
        component: () => import('@/views/student/Dashboard.vue'),
        meta: { title: '学习首页', role: 'student' }
      },
      {
        path: 'student/homework',
        name: 'StudentHomework',
        component: () => import('@/views/student/HomeworkList.vue'),
        meta: { title: '我的作业', role: 'student' }
      },
      {
        path: 'student/homework/submit/:id',
        name: 'SubmitHomework',
        component: () => import('@/views/student/SubmitHomework.vue'),
        meta: { title: '提交作业', role: 'student' }
      },
      {
        path: 'student/homework/detail/:id',
        name: 'CorrectionDetail',
        component: () => import('@/views/student/CorrectionDetail.vue'),
        meta: { title: '批改详情', role: 'student' }
      },
      {
        path: 'student/correction-detail',
        name: 'CorrectionDetailQuery',
        component: () => import('@/views/student/CorrectionDetail.vue'),
        meta: { title: '批改详情', role: 'student' }
      },
      {
        path: 'student/errors',
        name: 'ErrorBook',
        component: () => import('@/views/student/ErrorBook.vue'),
        meta: { title: '错题本', role: 'student' }
      },
      {
        path: 'student/analytics',
        name: 'StudentAnalytics',
        component: () => import('@/views/student/Analytics.vue'),
        meta: { title: '学情分析', role: 'student' }
      },
      // 教师端路由
      {
        path: 'teacher/dashboard',
        name: 'TeacherDashboard',
        component: () => import('@/views/teacher/Dashboard.vue'),
        meta: { title: '教学概览', role: 'teacher' }
      },
      {
        path: 'teacher/homework/create',
        name: 'CreateHomework',
        component: () => import('@/views/teacher/CreateHomework.vue'),
        meta: { title: '发布作业', role: 'teacher' }
      },
      {
        path: 'teacher/homework/list',
        name: 'TeacherHomeworkList',
        component: () => import('@/views/teacher/HomeworkList.vue'),
        meta: { title: '作业管理', role: 'teacher' }
      },
      {
        path: 'teacher/correction',
        name: 'Correction',
        component: () => import('@/views/teacher/Correction.vue'),
        meta: { title: '作业批改', role: 'teacher' }
      },
      {
        path: 'teacher/review',
        name: 'Review',
        component: () => import('@/views/teacher/Review.vue'),
        meta: { title: 'AI复核', role: 'teacher' }
      },
      {
        path: 'teacher/questions',
        name: 'Questions',
        component: () => import('@/views/teacher/Questions.vue'),
        meta: { title: '题库管理', role: 'teacher' }
      },
      {
        path: 'teacher/class',
        name: 'ClassManagement',
        component: () => import('@/views/teacher/ClassManagement.vue'),
        meta: { title: '班级管理', role: 'teacher' }
      },
      {
        path: 'teacher/class-transfer',
        name: 'ClassTransferApproval',
        component: () => import('@/views/teacher/ClassTransferApproval.vue'),
        meta: { title: '转班审批', role: 'teacher' }
      },
      // 系统设置页面暂不开放
      // {
      //   path: 'teacher/settings',
      //   name: 'Settings',
      //   component: () => import('@/views/Settings.vue'),
      //   meta: { title: '系统设置', role: 'teacher' }
      // },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', public: true }
      },
      {
        path: 'teacher/analytics',
        name: 'TeacherAnalytics',
        component: () => import('@/views/teacher/Analytics.vue'),
        meta: { title: '班级分析', role: 'teacher' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', public: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫：权限控制
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 智能作业批改系统` : '智能作业批改系统'
  
  // 获取用户状态
  const userStore = useUserStore()
  const isLoggedIn = userStore.isLoggedIn
  const isTeacher = userStore.isTeacher
  const isStudent = userStore.isStudent
  
  // 公开页面直接放行
  if (to.meta.public) {
    next()
    return
  }
  
  // 未登录跳转到登录页
  if (!isLoggedIn) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }
  
  // 角色权限检查
  const requiredRole = to.meta.role as string | undefined
  
  if (requiredRole === 'teacher' && !isTeacher) {
    ElMessage.error('无权访问教师页面')
    next(isStudent ? '/student/dashboard' : '/login')
    return
  }
  
  if (requiredRole === 'student' && !isStudent) {
    ElMessage.error('无权访问学生页面')
    next(isTeacher ? '/teacher/dashboard' : '/login')
    return
  }
  
  next()
})

export default router
