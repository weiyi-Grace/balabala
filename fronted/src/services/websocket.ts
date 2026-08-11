import { ElNotification } from 'element-plus'
import type { Notification } from '@/stores/notification'

/**
 * WebSocket连接管理
 */
export class CorrectionWebSocket {
  private ws: WebSocket | null = null
  private userId: string = ''
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectDelay = 3000
  private listeners = new Map<string, ((data: any) => void)[]>()
  private notificationCallback: ((notification: Omit<Notification, 'id' | 'createdAt' | 'read'>) => void) | null = null

  private getWebSocketUrl(userId: string) {
    const env = (import.meta as any).env || {}
    const apiBase = (env.VITE_API_BASE as string | undefined)?.trim()
    if (apiBase) {
      const wsBase = apiBase.replace(/^http:/, 'ws:').replace(/^https:/, 'wss:').replace(/\/$/, '')
      return `${wsBase}/api/ws/correction?userId=${encodeURIComponent(userId)}`
    }

    const wsBase = `ws://localhost:8080`
    return `${wsBase}/api/ws/correction?userId=${encodeURIComponent(userId)}`
  }

  /**
   * 连接WebSocket
   */
  connect(userId: string) {
    if (!userId) {
      console.warn('用户ID为空，无法连接WebSocket')
      return
    }

    this.userId = userId
    const wsUrl = this.getWebSocketUrl(userId)
    console.log('WebSocket尝试连接到:', wsUrl)

    try {
      this.ws = new WebSocket(wsUrl)

      this.ws.onopen = () => {
        console.log('WebSocket连接成功')
        this.reconnectAttempts = 0
      }

      this.ws.onmessage = (event) => {
        console.log('WebSocket收到原始消息:', event.data)
        try {
          const data = JSON.parse(event.data)
          console.log('WebSocket解析后的消息:', data)
          this.handleMessage(data)
        } catch (error) {
          console.error('WebSocket消息解析失败:', error)
        }
      }

      this.ws.onclose = (event) => {
        console.log('WebSocket连接关闭, code:', event.code, 'reason:', event.reason)
        this.attemptReconnect()
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error)
      }
    } catch (error) {
      console.error('WebSocket连接失败:', error)
      this.attemptReconnect()
    }
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  /**
   * 尝试重连
   */
  private attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`WebSocket重连尝试 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`)
      setTimeout(() => {
        this.connect(this.userId)
      }, this.reconnectDelay)
    } else {
      console.error('WebSocket重连失败，已达到最大重试次数')
    }
  }

  /**
   * 处理消息
   */
  private handleMessage(data: any) {
    const { type } = data

    switch (type) {
      case 'CORRECTION_COMPLETE':
      case 'CORRECTION_COMPLETE_STUDENT':
        this.showCorrectionCompleteNotification(data, 'student')
        this.handleCorrectionComplete(data, 'student')
        break
      case 'CORRECTION_COMPLETE_TEACHER':
        this.showCorrectionCompleteNotification(data, 'teacher')
        this.handleCorrectionComplete(data, 'teacher')
        break
      case 'HOMEWORK_SUBMITTED':
        this.showHomeworkSubmittedNotification(data)
        this.handleHomeworkSubmitted(data)
        break
      case 'HOMEWORK_PUBLISHED':
        this.showHomeworkPublishedNotification(data)
        this.handleHomeworkPublished(data)
        break
      case 'STUDENT_JOINED_CLASS':
        this.showStudentJoinedClassNotification(data)
        this.handleStudentJoinedClass(data)
        break
      case 'NOTIFICATION':
        this.showNotification(data)
        this.handleNotification(data)
        break
      case 'CLASS_TRANSFER_REQUEST':
        this.showClassTransferRequestNotification(data)
        this.handleClassTransferRequest(data)
        break
      default:
        console.log('收到未知类型消息:', data)
    }

    // 触发注册的监听器
    const listeners = this.listeners.get(type) || []
    listeners.forEach(callback => callback(data))
  }

  /**
   * 处理批改完成消息 - 添加到铃铛通知
   */
  private handleCorrectionComplete(data: any, userType: 'student' | 'teacher' = 'student') {
    if (this.notificationCallback) {
      const title = userType === 'teacher' ? '学生作业批改完成' : '作业批改完成'
      this.notificationCallback({
        type: userType === 'teacher' ? 'CORRECTION_COMPLETE_TEACHER' : 'CORRECTION_COMPLETE',
        title: title,
        message: data.message || (userType === 'teacher'
          ? `学生作业《${data.title}》已完成AI批改`
          : `作业《${data.title}》已完成批改`),
        homeworkId: data.homeworkId,
        studentId: data.studentId,
        data: data
      })
    }
  }

  /**
   * 处理作业提交消息
   */
  private handleHomeworkSubmitted(data: any) {
    if (this.notificationCallback) {
      this.notificationCallback({
        type: 'HOMEWORK_SUBMITTED',
        title: '学生提交作业',
        message: data.message || `学生${data.studentName || ''}提交了作业《${data.title}》`,
        homeworkId: data.homeworkId,
        studentId: data.studentId,
        data: data
      })
    }
  }

  /**
   * 显示作业提交通知（给老师）
   */
  private showHomeworkSubmittedNotification(data: any) {
    ElNotification({
      title: '学生提交作业',
      message: `${data.message || `学生${data.studentName || ''}提交了作业《${data.title}》`}`,
      type: 'info',
      duration: 0,
      showClose: true,
      onClick: () => {
        if (data.homeworkId) {
          window.location.href = `/teacher/correction?homeworkId=${data.homeworkId}`
        }
      }
    })
  }

  /**
   * 处理作业发布消息
   */
  private handleHomeworkPublished(data: any) {
    if (this.notificationCallback) {
      this.notificationCallback({
        type: 'HOMEWORK_PUBLISHED',
        title: '新作业发布',
        message: data.message || `新作业《${data.title}》已发布，请及时完成`,
        homeworkId: data.homeworkId,
        data: data
      })
    }
  }

  /**
   * 显示作业发布通知（给学生）
   */
  private showHomeworkPublishedNotification(data: any) {
    ElNotification({
      title: '新作业发布',
      message: `${data.message || `新作业《${data.title}》已发布，请及时完成`}`,
      type: 'warning',
      duration: 0,
      showClose: true,
      onClick: () => {
        if (data.homeworkId) {
          window.location.href = `/student/homework/${data.homeworkId}`
        }
      }
    })
  }

  /**
   * 处理学生加入班级消息
   */
  private handleStudentJoinedClass(data: any) {
    if (this.notificationCallback) {
      this.notificationCallback({
        type: 'STUDENT_JOINED_CLASS',
        title: '学生加入班级',
        message: data.message || `学生${data.studentName || ''}加入了班级《${data.className || ''}》`,
        studentId: data.studentId,
        data: data
      })
    }
  }

  /**
   * 显示学生加入班级通知（给老师）
   */
  private showStudentJoinedClassNotification(data: any) {
    ElNotification({
      title: '学生加入班级',
      message: `${data.message || `学生${data.studentName || ''}加入了班级《${data.className || ''}》`}`,
      type: 'success',
      duration: 0,
      showClose: true,
      onClick: () => {
        // 跳转到班级学生管理页面
        window.location.href = '/teacher/class'
      }
    })
  }

  /**
   * 处理教师发送的通知消息
   */
  private handleNotification(data: any) {
    if (this.notificationCallback) {
      this.notificationCallback({
        type: 'NOTIFICATION',
        title: data.title || '新通知',
        message: data.content || '',
        homeworkId: data.homeworkId,
        data: data
      })
    }
  }

  /**
   * 显示教师发送的通知（给学生）
   */
  private showNotification(data: any) {
    ElNotification({
      title: data.title || '新通知',
      message: data.content || '',
      type: data.notificationType === 'HOMEWORK' ? 'warning' : data.notificationType === 'EXAM' ? 'error' : 'info',
      duration: 0,
      showClose: true,
      onClick: () => {
        if (data.homeworkId) {
          window.location.href = `/student/homework/${data.homeworkId}`
        }
      }
    })
  }

  /**
   * 处理转班申请消息
   */
  private handleClassTransferRequest(data: any) {
    if (this.notificationCallback) {
      this.notificationCallback({
        type: 'CLASS_TRANSFER_REQUEST',
        title: '转班申请',
        message: data.message || `学生${data.studentName || ''}申请加入班级《${data.className || ''}》`,
        studentId: data.studentId,
        data: data
      })
    }
  }

  /**
   * 显示转班申请通知（给老师）
   */
  private showClassTransferRequestNotification(data: any) {
    ElNotification({
      title: '转班申请',
      message: `${data.message || `学生${data.studentName || ''}申请加入班级《${data.className || ''}》`}`,
      type: 'warning',
      duration: 0,
      showClose: true,
      onClick: () => {
        // 跳转到转班审批页面
        window.location.href = '/teacher/class-transfer'
      }
    })
  }

  /**
   * 设置通知回调
   */
  setNotificationCallback(callback: (notification: Omit<Notification, 'id' | 'createdAt' | 'read'>) => void) {
    this.notificationCallback = callback
  }

  /**
   * 显示批改完成通知
   */
  private showCorrectionCompleteNotification(data: any, userType: 'student' | 'teacher' = 'student') {
    const title = userType === 'teacher' ? '学生作业批改完成' : '作业批改完成'
    const defaultMsg = userType === 'teacher'
      ? `学生作业《${data.title}》已完成AI批改，请查看详情`
      : '您的作业已完成批改，点击查看详情'

    ElNotification({
      title: title,
      message: `${data.message || defaultMsg}`,
      type: 'success',
      duration: 0,
      showClose: true,
      onClick: () => {
        // 跳转到批改详情页
        if (userType === 'teacher') {
          if (data.homeworkId) {
            window.location.href = `/teacher/correction?homeworkId=${data.homeworkId}`
          }
        } else {
          if (data.homeworkId && data.studentId) {
            window.location.href = `/student/correction-detail?homeworkId=${data.homeworkId}&studentId=${data.studentId}`
          }
        }
      }
    })
  }

  /**
   * 添加消息监听器
   */
  on(type: string, callback: (data: any) => void) {
    if (!this.listeners.has(type)) {
      this.listeners.set(type, [])
    }
    this.listeners.get(type)!.push(callback)
  }

  /**
   * 移除消息监听器
   */
  off(type: string, callback: (data: any) => void) {
    const listeners = this.listeners.get(type) || []
    const index = listeners.indexOf(callback)
    if (index > -1) {
      listeners.splice(index, 1)
    }
  }
}

// 单例导出
export const correctionWebSocket = new CorrectionWebSocket()
