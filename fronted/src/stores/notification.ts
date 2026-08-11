import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElNotification } from 'element-plus'

export interface Notification {
  id: string
  type: 'CORRECTION_COMPLETE' | 'CORRECTION_COMPLETE_TEACHER' | 'HOMEWORK_SUBMITTED' | 'HOMEWORK_PUBLISHED' | 'STUDENT_JOINED_CLASS' | 'SYSTEM' | 'HOMEWORK' | 'NOTIFICATION' | 'CLASS_TRANSFER_REQUEST'
  title: string
  message: string
  homeworkId?: number
  studentId?: number
  read: boolean
  createdAt: string
  data?: any
}

export const useNotificationStore = defineStore('notification', () => {
  // 通知列表
  const notifications = ref<Notification[]>([])
  
  // 未读数量
  const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)
  
  // 未读通知
  const unreadNotifications = computed(() => 
    notifications.value.filter(n => !n.read).sort((a, b) => 
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )
  )
  
  // 所有通知（按时间倒序）
  const allNotifications = computed(() => 
    notifications.value.sort((a, b) => 
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    )
  )

  /**
   * 添加通知
   */
  function addNotification(notification: Omit<Notification, 'id' | 'createdAt' | 'read'>) {
    const newNotification: Notification = {
      ...notification,
      id: Date.now().toString(),
      createdAt: new Date().toISOString(),
      read: false
    }
    notifications.value.unshift(newNotification)
    
    // 限制通知数量，最多保留50条
    if (notifications.value.length > 50) {
      notifications.value = notifications.value.slice(0, 50)
    }
    
    // 显示桌面通知
    let notifyType: 'success' | 'info' | 'warning' = 'info'
    if (notification.type === 'CORRECTION_COMPLETE' || notification.type === 'CORRECTION_COMPLETE_TEACHER') {
      notifyType = 'success'
    } else if (notification.type === 'HOMEWORK_PUBLISHED') {
      notifyType = 'warning'
    } else if (notification.type === 'HOMEWORK_SUBMITTED') {
      notifyType = 'info'
    } else if (notification.type === 'STUDENT_JOINED_CLASS' || notification.type === 'NOTIFICATION' || notification.type === 'CLASS_TRANSFER_REQUEST') {
      notifyType = 'success'
    }

    ElNotification({
      title: notification.title,
      message: notification.message,
      type: notifyType,
      duration: 5000,
      showClose: true
    })
    
    return newNotification.id
  }

  /**
   * 标记为已读
   */
  function markAsRead(id: string) {
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.read = true
    }
  }

  /**
   * 标记所有为已读
   */
  function markAllAsRead() {
    notifications.value.forEach(n => n.read = true)
  }

  /**
   * 删除通知
   */
  function removeNotification(id: string) {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }

  /**
   * 清空所有通知
   */
  function clearAll() {
    notifications.value = []
  }

  /**
   * 处理WebSocket消息
   */
  function handleWebSocketMessage(data: any) {
    if (data.type === 'CORRECTION_COMPLETE' || data.type === 'CORRECTION_COMPLETE_STUDENT') {
      addNotification({
        type: 'CORRECTION_COMPLETE',
        title: '作业批改完成',
        message: data.message || `作业《${data.title}》已完成批改`,
        homeworkId: data.homeworkId,
        studentId: data.studentId,
        data: data
      })
    } else if (data.type === 'CORRECTION_COMPLETE_TEACHER') {
      addNotification({
        type: 'CORRECTION_COMPLETE_TEACHER',
        title: '学生作业批改完成',
        message: data.message || `学生作业《${data.title}》已完成AI批改`,
        homeworkId: data.homeworkId,
        studentId: data.studentId,
        data: data
      })
    } else if (data.type === 'HOMEWORK_SUBMITTED') {
      addNotification({
        type: 'HOMEWORK_SUBMITTED',
        title: '学生提交作业',
        message: data.message || `学生${data.studentName || ''}提交了作业《${data.title}》`,
        homeworkId: data.homeworkId,
        studentId: data.studentId,
        data: data
      })
    } else if (data.type === 'HOMEWORK_PUBLISHED') {
      addNotification({
        type: 'HOMEWORK_PUBLISHED',
        title: '新作业发布',
        message: data.message || `新作业《${data.title}》已发布，请及时完成`,
        homeworkId: data.homeworkId,
        data: data
      })
    } else if (data.type === 'STUDENT_JOINED_CLASS') {
      addNotification({
        type: 'STUDENT_JOINED_CLASS',
        title: '学生加入班级',
        message: data.message || `学生${data.studentName || ''}加入了班级《${data.className || ''}》`,
        studentId: data.studentId,
        data: data
      })
    } else if (data.type === 'NOTIFICATION') {
      addNotification({
        type: 'NOTIFICATION',
        title: data.title || '新通知',
        message: data.content || data.message || '',
        homeworkId: data.homeworkId,
        data: data
      })
    } else if (data.type === 'CLASS_TRANSFER_REQUEST') {
      addNotification({
        type: 'CLASS_TRANSFER_REQUEST',
        title: '转班申请',
        message: data.message || `学生${data.studentName || ''}申请加入班级《${data.className || ''}》`,
        studentId: data.studentId,
        data: data
      })
    }
  }

  return {
    notifications,
    unreadCount,
    unreadNotifications,
    allNotifications,
    addNotification,
    markAsRead,
    markAllAsRead,
    removeNotification,
    clearAll,
    handleWebSocketMessage
  }
})
