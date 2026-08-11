import { http } from './request'
import type { 
  StudentAnalytics,
  ClassAnalytics,
  ErrorBook,
  KnowledgePointMastery,
  PageResult
} from '@/types'

// 错题本 API (学生)
export const errorBookApi = {
  // 获取我的错题本
  getMyErrorBook(params?: { 
    knowledgePointId?: number
    correctionStatus?: number
    page?: number
    size?: number 
  }) {
    return http.get<PageResult<ErrorBook>>('/error-books/my', { params })
  },
  
  // 获取错题详情
  getErrorById(id: number) {
    return http.get<ErrorBook>(`/error-books/${id}`)
  },
  
  // 更新订正状态
  updateCorrectionStatus(id: number, status: number, record?: string) {
    return http.patch<ErrorBook>(`/error-books/${id}/status`, { correctionStatus: status, correctionRecord: record })
  },
  
  // 批量导出错题
  exportErrors(params?: { knowledgePointIds?: number[] }) {
    return http.get('/error-books/export', { params, responseType: 'blob' })
  }
}

// 学情分析 API
export const analyticsApi = {
  // 获取学生个人学情分析
  getStudentAnalytics(studentId?: number) {
    const url = studentId ? `/analytics/students/${studentId}` : '/analytics/my'
    return http.get<StudentAnalytics>(url)
  },
  
  // 获取知识点掌握情况
  getKnowledgePointMastery(studentId?: number) {
    const url = studentId ? `/analytics/students/${studentId}/knowledge-mastery` : '/analytics/my/knowledge-mastery'
    return http.get<KnowledgePointMastery[]>(url)
  },
  
  // 获取班级学情分析 (教师)
  getClassAnalytics(classId: number, params?: { courseId?: number; homeworkId?: number }) {
    return http.get<ClassAnalytics>(`/classes/${classId}/analytics`, { params })
  },
  
  // 获取班级成绩分布
  getClassScoreDistribution(classId: number, homeworkId?: number) {
    return http.get(`/classes/${classId}/score-distribution`, { params: { homeworkId } })
  },
  
  // 获取薄弱知识点排行
  getWeakKnowledgePoints(classId?: number) {
    const url = classId ? `/classes/${classId}/weak-points` : '/analytics/my/weak-points'
    return http.get(url)
  },
  
  // 获取近期成绩趋势
  getScoreTrend(days: number = 30, studentId?: number) {
    const url = studentId 
      ? `/analytics/students/${studentId}/score-trend` 
      : '/analytics/my/score-trend'
    return http.get(url, { params: { days } })
  }
}

// AI 大模型 API
export const aiApi = {
  // AI 智能分析作文/主观题
  analyzeSubjective(content: string, questionType: string, knowledgePoints?: string[]) {
    return http.post('/ai/analyze', {
      content,
      questionType,
      knowledgePoints
    })
  },
  
  // 获取 AI 生成的个性化学习建议
  generateStudyAdvice(studentId?: number) {
    const url = studentId ? `/ai/study-advice/${studentId}` : '/ai/my-study-advice'
    return http.get(url)
  },
  
  // AI 相似度检测 (查重)
  checkSimilarity(content1: string, content2: string) {
    return http.post('/ai/similarity-check', { content1, content2 })
  },
  
  // 获取 AI 批改解释
  getAICorrectionExplanation(correctionId: number) {
    return http.get(`/ai/correction-explanation/${correctionId}`)
  }
}
