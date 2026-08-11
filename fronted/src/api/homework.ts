import { http } from './request'
import type { 
  PageResult, 
  Homework, 
  HomeworkSubmission,
  CorrectionRecord,
  ScoringRule,
  KnowledgePoint,
  Question
} from '@/types'

// 作业管理 API
export const homeworkApi = {
  // 获取作业列表
  getHomeworkList(params: { 
    page?: number
    size?: number
    courseId?: number
    teacherId?: number
    homeworkType?: number
    status?: number
    keyword?: string
  }) {
    return http.get<PageResult<Homework>>('/homeworks', { params })
  },
  
  // 获取作业详情
  getHomeworkById(id: number) {
    return http.get<Homework>(`/homeworks/${id}`)
  },
  
  // 创建作业 (教师)
  createHomework(data: Partial<Homework>) {
    return http.post<Homework>('/homeworks', data)
  },
  
  // 更新作业
  updateHomework(id: number, data: Partial<Homework>) {
    return http.put<Homework>(`/homeworks/${id}`, data)
  },
  
  // 删除作业
  deleteHomework(id: number) {
    return http.delete(`/homeworks/${id}`)
  },
  
  // 发布作业
  publishHomework(id: number) {
    return http.post(`/homeworks/${id}/publish`)
  },
  
  // 截止作业
  closeHomework(id: number) {
    return http.post(`/homeworks/${id}/close`)
  }
}

// 题目管理 API
export const questionApi = {
  // 获取作业的题目列表
  getQuestionsByHomeworkId(homeworkId: number) {
    return http.get<Question[]>(`/homeworks/${homeworkId}/questions`)
  },
  
  // 添加题目
  addQuestion(data: Partial<Question>) {
    return http.post<Question>('/questions', data)
  },
  
  // 批量添加题目
  batchAddQuestions(homeworkId: number, questions: Partial<Question>[]) {
    return http.post(`/homeworks/${homeworkId}/questions/batch`, questions)
  },
  
  // 更新题目
  updateQuestion(id: number, data: Partial<Question>) {
    return http.put<Question>(`/questions/${id}`, data)
  },
  
  // 删除题目
  deleteQuestion(id: number) {
    return http.delete(`/questions/${id}`)
  }
}

// 作业提交 API (学生)
export const submissionApi = {
  // 获取我的提交列表
  getMySubmissions(params: { homeworkId?: number; status?: number }) {
    return http.get<HomeworkSubmission[]>('/submissions/my', { params })
  },
  
  // 获取作业的所有提交 (教师)
  getSubmissionsByHomework(homeworkId: number, params?: { status?: number }) {
    return http.get<HomeworkSubmission[]>(`/homeworks/${homeworkId}/submissions`, { params })
  },
  
  // 提交作业
  submitHomework(homeworkId: number, data: HomeworkSubmission) {
    return http.post<HomeworkSubmission>(`/homeworks/${homeworkId}/submit`, data)
  },
  
  // 保存草稿
  saveDraft(homeworkId: number, data: Partial<HomeworkSubmission>) {
    return http.post(`/homeworks/${homeworkId}/draft`, data)
  },
  
  // 获取提交详情
  getSubmissionById(id: number) {
    return http.get<HomeworkSubmission>(`/submissions/${id}`)
  }
}

// 批改记录 API
export const correctionApi = {
  // 获取批改记录列表
  getCorrectionList(params: {
    page?: number
    size?: number
    homeworkId?: number
    studentId?: number
    status?: number
    correctionType?: number
  }) {
    return http.get<PageResult<CorrectionRecord>>('/corrections', { params })
  },
  
  // 获取批改详情
  getCorrectionById(id: number) {
    return http.get<CorrectionRecord>(`/corrections/${id}`)
  },
  
  // 获取作业的批改记录
  getCorrectionsByHomework(homeworkId: number) {
    return http.get<CorrectionRecord[]>(`/homeworks/${homeworkId}/corrections`)
  },
  
  // 获取学生的批改记录
  getCorrectionsByStudent(studentId: number, params?: { courseId?: number }) {
    return http.get<CorrectionRecord[]>(`/students/${studentId}/corrections`, { params })
  },
  
  // 触发自动批改
  triggerAutoCorrection(submissionId: number, ruleId?: number) {
    return http.post<CorrectionRecord>(`/submissions/${submissionId}/auto-correct`, { ruleId })
  },
  
  // 人工批改
  manualCorrection(submissionId: number, data: Partial<CorrectionRecord>) {
    return http.post<CorrectionRecord>(`/submissions/${submissionId}/manual-correct`, data)
  },
  
  // 重新批改
  reCorrect(correctionId: number, ruleId?: number) {
    return http.post<CorrectionRecord>(`/corrections/${correctionId}/re-correct`, { ruleId })
  }
}

// 评分规则 API
export const scoringRuleApi = {
  // 获取评分规则列表
  getRuleList(params?: { subjectId?: number; questionType?: number }) {
    return http.get<ScoringRule[]>('/scoring-rules', { params })
  },
  
  // 获取评分规则详情
  getRuleById(id: number) {
    return http.get<ScoringRule>(`/scoring-rules/${id}`)
  },
  
  // 创建评分规则
  createRule(data: Partial<ScoringRule>) {
    return http.post<ScoringRule>('/scoring-rules', data)
  },
  
  // 更新评分规则
  updateRule(id: number, data: Partial<ScoringRule>) {
    return http.put<ScoringRule>(`/scoring-rules/${id}`, data)
  },
  
  // 删除评分规则
  deleteRule(id: number) {
    return http.delete(`/scoring-rules/${id}`)
  }
}

// 知识点 API
export const knowledgePointApi = {
  // 获取知识点列表
  getPointList(params?: { subjectId?: number; parentId?: number; keyword?: string }) {
    return http.get<KnowledgePoint[]>('/knowledge-points', { params })
  },
  
  // 获取知识点详情
  getPointById(id: number) {
    return http.get<KnowledgePoint>(`/knowledge-points/${id}`)
  },
  
  // 获取学科的知识点树
  getPointTree(subjectId: number) {
    return http.get<KnowledgePoint[]>(`/subjects/${subjectId}/knowledge-tree`)
  },
  
  // 创建知识点
  createPoint(data: Partial<KnowledgePoint>) {
    return http.post<KnowledgePoint>('/knowledge-points', data)
  },
  
  // 更新知识点
  updatePoint(id: number, data: Partial<KnowledgePoint>) {
    return http.put<KnowledgePoint>(`/knowledge-points/${id}`, data)
  },
  
  // 删除知识点
  deletePoint(id: number) {
    return http.delete(`/knowledge-points/${id}`)
  }
}
