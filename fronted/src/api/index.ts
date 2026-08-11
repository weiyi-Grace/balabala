import { http } from './request'

// 认证相关 API
export const authApi = {
  // 登录
  login(data: { username: string; password: string; role?: number }) {
    return http.post<{ token: string; user: any }>('/auth/login', data)
  },
  
  // 注册
  register(data: { 
    username: string; 
    password: string; 
    realName: string; 
    role: number;
    phone?: string;
    inviteCode?: string 
  }) {
    return http.post<any>('/auth/register', data)
  }
}

// 用户相关 API
export const userApi = {
  // 获取用户信息
  getUserById(id: number) {
    return http.get<any>(`/user/${id}`)
  },
  
  // 更新用户信息
  updateUser(id: number, data: any) {
    return http.put<any>(`/user/${id}`, data)
  },
  
  // 更新个人资料
  updateProfile(id: number, data: {
    realName?: string;
    nickname?: string;
    phone?: string;
    email?: string;
    bio?: string;
    avatar?: string;
  }) {
    return http.put<any>(`/user/${id}/profile`, data)
  },
  
  // 修改密码
  changePassword(id: number, data: { oldPassword: string; newPassword: string }) {
    return http.post<void>(`/user/${id}/change-password`, data)
  },
  
  // 获取用户统计
  getUserStats(id: number) {
    return http.get<any>(`/user/${id}/stats`)
  },
  
  // 获取学生列表(教师用)
  getStudentsByClass(classId: number) {
    return http.get<any[]>(`/user/class/${classId}`)
  },

  // 搜索未加入班级的学生（用于邀请学生）
  searchStudentsNotInClass(keyword: string, excludeClassId: number) {
    return http.get<any[]>(`/user/search/students`, {
      params: { keyword, excludeClassId }
    })
  }
}

// 班级相关 API
export const classApi = {
  // 获取所有班级列表
  getAllClasses() {
    return http.get<any[]>('/class/all')
  },
  
  // 获取老师的班级列表
  getClassList(teacherId?: number) {
    return http.get<any[]>(`/class/list?teacherId=${teacherId || ''}`)
  },
  
  // 获取班级详情
  getClassById(id: number) {
    return http.get<any>(`/class/${id}`)
  },
  
  // 创建班级
  createClass(data: any) {
    return http.post<any>('/class', data)
  },
  
  // 更新班级
  updateClass(id: number, data: any) {
    return http.put<any>(`/class/${id}`, data)
  },
  
  // 删除班级
  deleteClass(id: number) {
    return http.delete(`/class/${id}`)
  },
  
  // 获取班级学生
  getClassStudents(id: number) {
    return http.get<any[]>(`/class/${id}/students`)
  },
  
  // 加入班级
  joinClass(inviteCode: string, studentId: number) {
    return http.post<void>('/class/join', null, { 
      params: { inviteCode, studentId } 
    })
  },
  
  // 移除学生
  removeStudent(classId: number, studentId: number) {
    return http.delete(`/class/${classId}/students/${studentId}`)
  }
}

// 题目相关 API
export const questionApi = {
  // 获取题目列表
  getQuestionList(params: {
    keyword?: string;
    subject?: string;
    type?: string;
    difficulty?: string;
    page?: number;
    size?: number;
  }) {
    return http.get<any>('/question/list', { params })
  },
  
  // 获取题目详情
  getQuestionById(id: number) {
    return http.get<any>(`/question/${id}`)
  },
  
  // 创建题目
  createQuestion(data: any) {
    return http.post<any>('/question', data)
  },
  
  // 更新题目
  updateQuestion(id: number, data: any) {
    return http.put<any>(`/question/${id}`, data)
  },
  
  // 删除题目
  deleteQuestion(id: number) {
    return http.delete(`/question/${id}`)
  },
  
  // 根据知识点查询
  getByKnowledgePoint(knowledgePoint: string) {
    return http.get<any[]>(`/question/knowledge/${knowledgePoint}`)
  },
  
  // 批量导入
  batchImport(questions: any[]) {
    return http.post<any[]>('/question/batch', questions)
  },
  
  // 批量添加作业题目
  batchAddQuestions(homeworkId: number, questions: any[]) {
    return http.post<any>(`/homework/${homeworkId}/questions`, questions)
  }
}

// 作业/考试相关 API
export const homeworkApi = {
  // 获取作业列表(教师)
  getTeacherHomeworks(teacherId: number) {
    return http.get<any[]>(`/homework/teacher/${teacherId}`)
  },
  
  // 获取班级作业列表(学生)
  getClassHomeworks(classId: number) {
    return http.get<any[]>(`/homework/class/${classId}`)
  },
  
  // 获取作业详情
  getHomeworkById(id: number) {
    return http.get<any>(`/homework/${id}`)
  },
  
  // 创建作业
  createHomework(data: any) {
    return http.post<any>('/homework', data)
  },
  
  // 创建考试
  createExam(data: any) {
    return http.post<any>('/homework/exam', data)
  },
  
  // 获取考试设置
  getExamSetting(id: number) {
    return http.get<any>(`/homework/${id}/exam-setting`)
  },
  
  // 检查考试状态
  checkExamStatus(id: number) {
    return http.get<boolean>(`/homework/${id}/exam-status`)
  },
  
  // 更新作业
  updateHomework(id: number, data: any) {
    return http.put<any>(`/homework/${id}`, data)
  },
  
  // 删除作业
  deleteHomework(id: number) {
    return http.delete(`/homework/${id}`)
  },
  
  // 结束作业
  endHomework(id: number) {
    return http.post<void>(`/homework/${id}/end`)
  }
}

// 提交相关 API
export const submissionApi = {
  // 提交作业
  submitHomework(data: any) {
    return http.post<any>('/submission', data)
  },
  
  // 获取作业提交列表
  getSubmissionsByHomework(homeworkId: number) {
    return http.get<any[]>(`/submission/homework/${homeworkId}`)
  },
  
  // 获取学生提交列表
  getSubmissionsByStudent(studentId: number) {
    return http.get<any[]>(`/submission/student/${studentId}`)
  },
  
  // 获取提交详情
  getSubmissionById(id: number) {
    return http.get<any>(`/submission/${id}`)
  },
  
  // 教师批改
  correctSubmission(submissionId: number, corrections: any[]) {
    return http.put<void>(`/submission/${submissionId}/correct`, corrections)
  },

  // 获取指定学生的提交详情（教师端）
  getSubmissionByHomeworkAndStudent(homeworkId: number, studentId: number) {
    return http.get<any>(`/submission/homework/${homeworkId}/student/${studentId}`)
  }
}

// 错题本相关 API
export const errorBookApi = {
  // 获取学生错题本
  getStudentErrors(studentId: number) {
    return http.get<any[]>(`/error-book/student/${studentId}`)
  },
  
  // 根据知识点筛选
  getByKnowledgePoint(studentId: number, knowledgePoint: string) {
    return http.get<any[]>(`/error-book/student/${studentId}/knowledge`, {
      params: { knowledgePoint }
    })
  },
  
  // 添加错题
  addError(data: any) {
    return http.post<any>('/error-book', data)
  },
  
  // 更新掌握状态
  updateMastery(id: number, status: number) {
    return http.put<void>(`/error-book/${id}/mastery`, null, { params: { status } })
  },
  
  // 记录复习
  reviewError(id: number, notes: string) {
    return http.post<void>(`/error-book/${id}/review`, null, { params: { notes } })
  },
  
  // 删除错题
  deleteError(id: number) {
    return http.delete(`/error-book/${id}`)
  }
}

// Dashboard 统计 API
export const dashboardApi = {
  // 学生仪表盘
  getStudentDashboard(studentId: number) {
    return http.get<any>(`/dashboard/student/${studentId}`)
  },
  
  // 教师仪表盘
  getTeacherDashboard(teacherId: number) {
    return http.get<any>(`/dashboard/teacher/${teacherId}`)
  },
  
  // 班级统计
  getClassDashboard(classId: number) {
    return http.get<any>(`/dashboard/class/${classId}`)
  },
  
  // 班级学情分析（详细）
  getClassAnalytics(classId: number) {
    return http.get<any>(`/dashboard/class/${classId}/analytics`)
  },
  
  // 班级成绩分布
  getClassScoreDistribution(classId: number) {
    return http.get<any>(`/dashboard/class/${classId}/score-distribution`)
  },
  
  // 班级薄弱知识点排行
  getClassWeakPoints(classId: number) {
    return http.get<any>(`/dashboard/class/${classId}/weak-points`)
  },
  
  // 班级学生成绩排行
  getClassStudentRanking(classId: number) {
    return http.get<any>(`/dashboard/class/${classId}/student-ranking`)
  },
  
  // 班级作业完成趋势（最近7天）
  getClassTrend(classId: number) {
    return http.get<any>(`/dashboard/class/${classId}/trend`)
  },

  // 获取最近动态
  getRecentActivities(userId: number) {
    return http.get<any[]>(`/dashboard/activities/${userId}`)
  },

  // 发送通知给学生
  sendNotification(data: {
    title?: string;
    content: string;
    type: string;
    senderId?: number;
    classId?: string | number;
    receiverId?: number;
    homeworkId?: number;
  }) {
    return http.post<void>('/dashboard/notification/send', data)
  },

  // 获取学生通知列表
  getStudentNotifications(studentId: number) {
    return http.get<any[]>(`/dashboard/notification/student/${studentId}`)
  },

  // 标记通知为已读
  markNotificationAsRead(notificationId: number) {
    return http.put<void>(`/dashboard/notification/${notificationId}/read`)
  }
}

// AI 相关 API
export const aiApi = {
  // AI 基础批改
  correct(data: {
    question: string;
    studentAnswer: string;
    correctAnswer: string;
    questionType: string;
    fullScore: number;
  }) {
    return http.post<any>('/ai/correct', data)
  },
  
  // RAG批改
  correctWithRAG(data: {
    question: string;
    studentAnswer: string;
    knowledgePoint: string;
  }) {
    return http.post<any>('/ai/correct/rag', data)
  },
  
  // Agent批改
  correctWithAgent(data: {
    question: string;
    studentAnswer: string;
    correctAnswer: string;
    questionType: string;
    fullScore: number;
  }) {
    return http.post<any>('/ai/correct/agent', data)
  },
  
  // 生成解析
  generateAnalysis(question: string, correctAnswer: string) {
    return http.post<string>('/ai/analysis', { question, correctAnswer })
  },
  
  // AI答疑对话
  chat(data: {
    question: string;
    studentAnswer: string;
    correctAnswer: string;
    chatMessage: string;
    knowledgePoint?: string;
  }) {
    return http.post<string>('/ai/chat', data, { timeout: 60000 }) // AI接口超时60秒
  },
  
  // AI出题
  generateQuestion(data: {
    subject: string;
    knowledgePoint: string;
    difficulty: string;
    questionType: string;
    prompt?: string;
  }) {
    return http.post<any>('/ai/generate-question', data)
  }
}

// 文件相关 API
export const fileApi = {
  // 上传题目文件
  uploadQuestionFile(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return http.post<any[]>('/file/upload/questions', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  
  // 解析文本
  parseText(content: string) {
    return http.post<any[]>('/file/parse/text', content)
  }
}

// 打卡相关 API
export const checkInApi = {
  // 今日打卡
  checkIn(studentId: number) {
    return http.post<any>(`/checkin/${studentId}`)
  },
  
  // 获取打卡统计
  getStats(studentId: number) {
    return http.get<{
      totalCheckIns: number;
      currentStreak: number;
      studyDays: number;
      checkedInToday: boolean;
      monthlyCheckIns: string[];
      checkInDates: string[];
    }>(`/checkin/${studentId}/stats`)
  },
  
  // 获取打卡日历
  getCalendar(studentId: number, year: number, month: number) {
    return http.get<{
      year: number;
      month: number;
      checkInDates: string[];
    }>(`/checkin/${studentId}/calendar`, { params: { year, month } })
  },
  
  // 获取近期打卡记录（最近7天）
  getRecent(studentId: number) {
    return http.get<{
      recentDays: {
        date: string;
        dayOfWeek: number;
        checkedIn: boolean;
        isToday: boolean;
      }[];
    }>(`/checkin/${studentId}/recent`)
  }
}

// 班级申请相关 API
export const classTransferApi = {
  // 申请转班
  applyForTransfer(data: {
    studentId: number;
    toClassId: number;
    reason?: string;
  }) {
    return http.post<void>('/class-transfer/apply', data)
  },
  
  // 获取学生的申请历史
  getStudentRequests(studentId: number) {
    return http.get<any[]>(`/class-transfer/student/${studentId}`)
  },
  
  // 获取老师待审批的申请
  getPendingRequests(teacherId: number) {
    return http.get<any[]>(`/class-transfer/teacher/${teacherId}/pending`)
  },
  
  // 审批申请
  approveRequest(requestId: number, teacherId: number, approved: boolean, remark?: string) {
    return http.post<void>(`/class-transfer/${requestId}/approve?teacherId=${teacherId}&approved=${approved}&remark=${remark || ''}`)
  }
}
