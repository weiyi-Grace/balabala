<template>
  <div class="min-h-screen bg-gradient-to-br from-primary-50 to-gray-100">
    <header class="sticky top-0 z-50 bg-white/80 backdrop-blur border-b border-gray-200 shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16 gap-4">
          <!-- Logo区域 - 防止被压缩 -->
          <div class="flex items-center gap-3 cursor-pointer flex-shrink-0" @click="goHome">
            <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center flex-shrink-0">
              <img src="/logo.png" alt="Logo" class="w-7 h-7 object-contain" />
            </div>
            <div class="flex-shrink-0">
              <h1 class="text-lg font-bold text-gray-800 whitespace-nowrap">智能作业批改系统</h1>
              <p class="text-xs text-gray-500 whitespace-nowrap">DeepSeek 驱动</p>
            </div>
          </div>

          <!-- 导航菜单 - 占据中间空间 -->
          <nav class="hidden md:flex items-center gap-1 flex-1 justify-center">
            <el-button v-for="item in menuItems" :key="item.path" :type="isActive(item.path) ? 'primary' : ''" text
              :class="isActive(item.path) ? 'bg-primary-50 text-primary-600' : 'text-gray-600'"
              @click="router.push(item.path)">
              <el-icon class="mr-1"><component :is="item.icon" /></el-icon>
              {{ item.name }}
            </el-button>
          </nav>

          <!-- 右侧功能区 - 防止被压缩 -->
          <div class="flex items-center gap-3 flex-shrink-0">
            <!-- 通知铃铛 -->
            <el-popover
              v-model:visible="showNotifications"
              placement="bottom-end"
              :width="380"
              popper-class="notification-popover"
            >
              <template #reference>
                <el-badge :value="notificationStore.unreadCount" :hidden="notificationStore.unreadCount === 0" class="mr-2">
                  <el-button circle text @click.stop="showNotifications = !showNotifications">
                    <el-icon :size="20"><Bell /></el-icon>
                  </el-button>
                </el-badge>
              </template>
              
              <div class="notification-panel" @click.stop>
                <div class="notification-header">
                  <span class="font-bold">通知中心</span>
                  <div class="header-actions">
                    <el-button v-if="notificationStore.unreadCount > 0" link type="primary" size="small" @click="markAllRead">
                      全部已读
                    </el-button>
                    <el-button link type="info" size="small" @click="clearAll">
                      清空
                    </el-button>
                  </div>
                </div>
                
                <div class="notification-list" @click.stop>
                  <div v-if="notificationStore.allNotifications.length === 0" class="empty-state">
                    <el-empty description="暂无通知" :image-size="60" />
                  </div>
                  
                  <div
                    v-for="item in notificationStore.allNotifications"
                    :key="item.id"
                    class="notification-item"
                    :class="{ unread: !item.read }"
                    @click.stop="handleNotificationClick(item)"
                  >
                    <div class="notification-icon" :class="item.type.toLowerCase()">
                      <el-icon :size="18">
                        <CircleCheck v-if="item.type === 'CORRECTION_COMPLETE'" />
                        <InfoFilled v-else />
                      </el-icon>
                    </div>
                    <div class="notification-content">
                      <div class="notification-title">{{ item.title }}</div>
                      <div class="notification-message">{{ item.message }}</div>
                      <div class="notification-time">{{ formatTime(item.createdAt) }}</div>
                      <div class="mt-2">
                        <el-button
                          v-if="item.homeworkId || item.type === 'STUDENT_JOINED_CLASS'"
                          type="primary"
                          size="small"
                          text
                          @click.stop="handleViewDetail(item)"
                        >
                          查看详情
                        </el-button>
                      </div>
                    </div>
                    <div class="notification-actions">
                      <el-button
                        v-if="!item.read"
                        circle
                        text
                        size="small"
                        @click.stop="notificationStore.markAsRead(item.id)"
                      >
                        <el-icon><Check /></el-icon>
                      </el-button>
                      <el-button
                        circle
                        text
                        size="small"
                        @click.stop="notificationStore.removeNotification(item.id)"
                      >
                        <el-icon><Close /></el-icon>
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </el-popover>
            
            <el-dropdown>
              <div class="flex items-center gap-2 cursor-pointer p-1 rounded hover:bg-gray-50">
                <el-avatar :size="36" src="https://api.dicebear.com/7.x/avataaars/svg?seed=user" />
                <span class="hidden sm:block text-sm">{{ userName }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
                  <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </header>

    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
      <router-view />
    </main>

    <footer class="bg-white border-t border-gray-200 mt-auto">
      <div class="max-w-7xl mx-auto px-4 py-4 text-center text-sm text-gray-500">
        © 2026 教育智能化转型课题组 | DeepSeek V4.0 驱动
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { HomeFilled, Notebook, DataAnalysis, Management, Bell, Collection, School, CircleCheck, Check, Close, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { correctionWebSocket } from '@/services/websocket'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const notificationStore = useNotificationStore()
const userName = computed(() => userStore.user?.realName || userStore.user?.username || '用户')

const isStudent = computed(() => route.path.startsWith('/student') || userStore.user?.role === 3)
const isTeacher = computed(() => route.path.startsWith('/teacher') || userStore.user?.role === 2 || userStore.user?.role === 1)

const menuItems = computed(() => {
  if (isTeacher.value) {
    return [
      { name: '教学概览', path: '/teacher/dashboard', icon: HomeFilled },
      { name: '班级管理', path: '/teacher/class', icon: School },
      { name: '转班审批', path: '/teacher/class-transfer', icon: Management },
      { name: '作业管理', path: '/teacher/homework/list', icon: Management },
      { name: '题库管理', path: '/teacher/questions', icon: Collection },
      { name: '班级分析', path: '/teacher/analytics', icon: DataAnalysis }
    ]
  }
  return [
    { name: '学习首页', path: '/student/dashboard', icon: HomeFilled },
    { name: '我的作业', path: '/student/homework', icon: Notebook },
    { name: '错题本', path: '/student/errors', icon: Management },
    { name: '学情分析', path: '/student/analytics', icon: DataAnalysis }
  ]
})

const isActive = (path: string) => route.path === path
const goHome = () => router.push(isTeacher.value ? '/teacher/dashboard' : '/student/dashboard')

// 通知中心
const showNotifications = ref(false)
const markAllRead = () => notificationStore.markAllAsRead()
const clearAll = () => {
  notificationStore.clearAll()
  showNotifications.value = false
}
const handleNotificationClick = (item: any) => {
  notificationStore.markAsRead(item.id)
  // 点击通知项只标记已读，不自动跳转，面板保持打开方便查看多条通知
}
// 查看通知详情并跳转
const handleViewDetail = (item: any) => {
  notificationStore.markAsRead(item.id)
  
  // 根据通知类型和角色跳转
  if (item.type === 'HOMEWORK_PUBLISHED' && item.homeworkId) {
    // 学生收到新作业发布，跳转到提交作业页面
    router.push(`/student/homework/submit/${item.homeworkId}`)
  } else if (item.type === 'HOMEWORK_SUBMITTED' && item.homeworkId) {
    // 老师收到学生提交，跳转到批改页面
    router.push(`/teacher/correction?homeworkId=${item.homeworkId}`)
  } else if (item.type === 'STUDENT_JOINED_CLASS') {
    // 老师收到学生加入班级，跳转到班级管理
    router.push('/teacher/class')
  } else if (item.type === 'CORRECTION_COMPLETE' || item.type === 'CORRECTION_COMPLETE_TEACHER' || item.type === 'CORRECTION_COMPLETE_STUDENT') {
    // 批改完成通知
    if (isTeacher.value && item.homeworkId) {
      router.push(`/teacher/correction?homeworkId=${item.homeworkId}`)
    } else if (item.homeworkId && item.studentId) {
      router.push(`/student/correction-detail?homeworkId=${item.homeworkId}&studentId=${item.studentId}`)
    }
  }
  
  // 跳转后关闭面板
  showNotifications.value = false
}
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  return date.toLocaleDateString()
}

const logout = () => {
  userStore.logout()
  correctionWebSocket.disconnect()
  router.push('/login')
}

// 初始化WebSocket连接
onMounted(() => {
  const userId = userStore.user?.id
  console.log('MainLayout onMounted - userId:', userId)
  console.log('MainLayout onMounted - user:', userStore.user)
  if (userId) {
    // 设置通知回调，将WebSocket消息添加到铃铛通知
    correctionWebSocket.setNotificationCallback((notification) => {
      console.log('收到WebSocket通知:', notification)
      notificationStore.addNotification(notification)
    })
    correctionWebSocket.connect(String(userId))
  } else {
    console.warn('用户未登录，无法连接WebSocket')
  }
})

onUnmounted(() => {
  // 移除监听器
  correctionWebSocket.off('CORRECTION_COMPLETE', () => {})
  correctionWebSocket.off('CORRECTION_COMPLETE_STUDENT', () => {})
})
</script>

<style scoped>
.nav-link {
  @apply px-4 py-2 rounded-lg text-gray-600 hover:bg-gray-100 hover:text-green-600 transition-all duration-200 flex items-center gap-2;
}
.nav-link.active {
  @apply bg-green-50 text-green-600 font-medium;
}

/* 通知面板样式 */
:deep(.notification-popover) {
  padding: 0 !important;
}

.notification-panel {
  max-height: 400px;
  overflow: hidden;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.notification-list {
  max-height: 340px;
  overflow-y: auto;
}

.empty-state {
  padding: 40px 0;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: background 0.2s;
}

.notification-item:hover {
  background: #f9fafb;
}

.notification-item.unread {
  background: #eff6ff;
}

.notification-item.unread:hover {
  background: #dbeafe;
}

.notification-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notification-icon.correction_complete {
  background: #dcfce7;
  color: #16a34a;
}

.notification-icon.system {
  background: #e0e7ff;
  color: #4f46e5;
}

.notification-icon.homework {
  background: #fef3c7;
  color: #d97706;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.notification-message {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notification-time {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}

.notification-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.notification-item:hover .notification-actions {
  opacity: 1;
}
</style>
