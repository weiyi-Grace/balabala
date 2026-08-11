// 用户相关类型
export interface User {
  id: number
  username: string
  realName: string
  role: 1 | 2 | 3 // 1-管理员, 2-教师, 3-学生
  subjectId?: number
  classId?: number
  classInfo?: {
    id: number
    name: string
  }
  phone?: string
  createTime: string
  status: 0 | 1
  avatar?: string
}

export interface LoginForm {
  username: string
  password: string
  role?: number
}

export interface RegisterForm {
  username: string
  password: string
  confirmPassword: string
  realName: string
  role: number
  subjectId?: number
  classId?: number
  phone?: string
}

// 学科/班级类型
export interface Subject {
  id: number
  subjectName: string
  description?: string
  status: 0 | 1
}

export interface Class {
  id: number
  className: string
  grade: string
  description?: string
  status: 0 | 1
}

// 课程类型
export interface Course {
  id: number
  courseName: string
  subjectId: number
  subjectName?: string
  teacherId: number
  teacherName?: string
  classId: number
  className?: string
  semester: string
  status: 0 | 1
}

// 作业类型
export type HomeworkType = 1 | 2 | 3 // 1-客观题, 2-主观题, 3-混合题
export type HomeworkStatus = 0 | 1 | 2 // 0-未发布, 1-已发布, 2-已截止

export interface Homework {
  id: number
  homeworkTitle: string
  courseId: number
  courseName?: string
  teacherId: number
  teacherName?: string
  releaseTime: string
  deadlineTime: string
  homeworkType: HomeworkType
  totalScore: number
  content: string
  status: HomeworkStatus
  questions?: Question[]
  createTime?: string
}

// 题目类型
export type QuestionType = 'single_choice' | 'multiple_choice' | 'fill_blank' | 'true_false' | 'short_answer' | 'reading_comprehension'

export interface Question {
  id: number
  homeworkId: number
  questionType: QuestionType
  questionContent: string
  options?: string[] // 选择题选项
  correctAnswer?: string | string[] // 正确答案（多选为数组）
  score: number // 分值
  knowledgePointIds?: number[] // 关联知识点
  orderNum: number // 题号
  // 阅读理解专用
  readingMaterial?: string // 阅读材料
  subQuestions?: Question[] // 子题目
  // 填空题专用
  blankCount?: number // 填空数量
  blankAnswers?: string[] // 填空答案
}

// 作业提交类型
export interface HomeworkSubmission {
  id: number
  homeworkId: number
  studentId: number
  studentName?: string
  submitTime: string
  answers: Answer[]
  attachmentUrl?: string
  status: 0 | 1 // 0-草稿, 1-已提交
}

export interface Answer {
  questionId: number
  answerContent: string
  attachmentUrl?: string
}

// 批改记录类型
export type CorrectionType = 1 | 2 // 1-自动批改, 2-人工批改

export interface CorrectionRecord {
  id: number
  homeworkId: number
  homeworkTitle?: string
  studentId: number
  studentName?: string
  teacherId?: number
  teacherName?: string
  score: number
  correctionTime: string
  errorPoints: number[] // 错误知识点ID数组
  feedback: string
  correctionType: CorrectionType
  ruleId?: number
  questionCorrections?: QuestionCorrection[]
  status: 0 | 1 | 2 // 0-待批改, 1-批改中, 2-已完成
}

export interface QuestionCorrection {
  questionId: number
  score: number
  feedback: string
  errorPoints?: number[]
  modelAnalysis?: string // AI分析结果
}

// 评分规则类型
export interface ScoringRule {
  id: number
  ruleName: string
  subjectId: number
  subjectName?: string
  questionType: HomeworkType
  objectiveConfig?: ObjectiveScoringConfig
  subjectiveConfig?: SubjectiveScoringConfig
  status: 0 | 1
  createTime?: string
}

export interface ObjectiveScoringConfig {
  correctScore: number // 答对得分
  wrongScore: number // 答错得分
  partialScore?: number // 部分得分
}

export interface SubjectiveScoringConfig {
  logicWeight: number // 逻辑权重
  completenessWeight: number // 完整性权重
  accuracyWeight: number // 准确性权重
  minScorePerStep: number // 最小步骤分
}

// 知识点类型
export interface KnowledgePoint {
  id: number
  pointName: string
  subjectId: number
  subjectName?: string
  parentId?: number // 父级知识点
  description?: string
  difficulty: 1 | 2 | 3 // 1-基础, 2-进阶, 3-拔高
  importance: 1 | 2 | 3 // 1-一般, 2-重要, 3-核心
  status: 0 | 1
}

// 错题本类型
export interface ErrorBook {
  id: number
  studentId: number
  homeworkId: number
  homeworkTitle?: string
  questionId: number
  knowledgePointId: number
  knowledgePointName?: string
  errorCount: number
  lastErrorTime: string
  correctionStatus: 0 | 1 | 2 // 0-未订正, 1-已订正, 2-已掌握
  correctionRecord?: string
}

// 学情分析类型
export interface StudentAnalytics {
  studentId: number
  studentName?: string
  totalHomeworks: number
  completedHomeworks: number
  averageScore: number
  knowledgePointMastery: KnowledgePointMastery[]
  recentTrend: ScoreTrend[]
  errorDistribution: ErrorDistribution[]
}

export interface KnowledgePointMastery {
  knowledgePointId: number
  knowledgePointName: string
  masteryRate: number // 掌握率 0-100
  totalQuestions: number
  correctQuestions: number
}

export interface ScoreTrend {
  date: string
  score: number
  averageScore: number
}

export interface ErrorDistribution {
  knowledgePointId: number
  knowledgePointName: string
  errorCount: number
  percentage: number
}

// 班级学情分析
export interface ClassAnalytics {
  classId: number
  className: string
  totalStudents: number
  homeworkCompletionRate: number
  averageScore: number
  scoreDistribution: { range: string; count: number }[]
  weakKnowledgePoints: KnowledgePointWeakness[]
  studentRankings: StudentRanking[]
}

export interface KnowledgePointWeakness {
  knowledgePointId: number
  knowledgePointName: string
  classMasteryRate: number
  errorRate: number
}

export interface StudentRanking {
  studentId: number
  studentName: string
  averageScore: number
  completionRate: number
  rank: number
}

// 通用响应类型
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
