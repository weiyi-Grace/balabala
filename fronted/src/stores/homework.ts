import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Homework, Question, HomeworkSubmission, ScoringRule, KnowledgePoint } from '@/types'
import { homeworkApi, questionApi, submissionApi, scoringRuleApi, knowledgePointApi } from '@/api/homework'

export const useHomeworkStore = defineStore('homework', () => {
  // State
  const homeworks = ref<Homework[]>([])
  const currentHomework = ref<Homework | null>(null)
  const questions = ref<Question[]>([])
  const submissions = ref<HomeworkSubmission[]>([])
  const currentSubmission = ref<HomeworkSubmission | null>(null)
  const scoringRules = ref<ScoringRule[]>([])
  const knowledgePoints = ref<KnowledgePoint[]>([])
  const loading = ref(false)
  
  // Getters
  const homeworkList = computed(() => homeworks.value)
  const activeHomeworks = computed(() => homeworks.value.filter(h => h.status === 1))
  const expiredHomeworks = computed(() => homeworks.value.filter(h => h.status === 2))
  
  // Actions
  
  // 获取作业列表
  const fetchHomeworks = async (params?: any) => {
    loading.value = true
    try {
      const result = await homeworkApi.getHomeworkList(params || {})
      homeworks.value = result.records
      return result
    } finally {
      loading.value = false
    }
  }
  
  // 获取作业详情
  const fetchHomeworkDetail = async (id: number) => {
    loading.value = true
    try {
      const homework = await homeworkApi.getHomeworkById(id)
      currentHomework.value = homework
      return homework
    } finally {
      loading.value = false
    }
  }
  
  // 创建作业
  const createHomework = async (data: Partial<Homework>) => {
    const homework = await homeworkApi.createHomework(data)
    homeworks.value.unshift(homework)
    return homework
  }
  
  // 更新作业
  const updateHomework = async (id: number, data: Partial<Homework>) => {
    const homework = await homeworkApi.updateHomework(id, data)
    const index = homeworks.value.findIndex(h => h.id === id)
    if (index !== -1) {
      homeworks.value[index] = homework
    }
    if (currentHomework.value?.id === id) {
      currentHomework.value = homework
    }
    return homework
  }
  
  // 删除作业
  const deleteHomework = async (id: number) => {
    await homeworkApi.deleteHomework(id)
    homeworks.value = homeworks.value.filter(h => h.id !== id)
  }
  
  // 发布作业
  const publishHomework = async (id: number) => {
    await homeworkApi.publishHomework(id)
    const index = homeworks.value.findIndex(h => h.id === id)
    if (index !== -1) {
      homeworks.value[index].status = 1
    }
  }
  
  // 获取题目列表
  const fetchQuestions = async (homeworkId: number) => {
    const result = await questionApi.getQuestionsByHomeworkId(homeworkId)
    questions.value = result
    return result
  }
  
  // 提交作业
  const submitHomework = async (homeworkId: number, data: HomeworkSubmission) => {
    const submission = await submissionApi.submitHomework(homeworkId, data)
    currentSubmission.value = submission
    return submission
  }
  
  // 获取评分规则
  const fetchScoringRules = async (params?: any) => {
    const result = await scoringRuleApi.getRuleList(params)
    scoringRules.value = result
    return result
  }
  
  // 获取知识点列表
  const fetchKnowledgePoints = async (params?: any) => {
    const result = await knowledgePointApi.getPointList(params)
    knowledgePoints.value = result
    return result
  }
  
  // 清空当前数据
  const clearCurrentData = () => {
    currentHomework.value = null
    currentSubmission.value = null
    questions.value = []
  }
  
  return {
    homeworks,
    currentHomework,
    questions,
    submissions,
    currentSubmission,
    scoringRules,
    knowledgePoints,
    loading,
    homeworkList,
    activeHomeworks,
    expiredHomeworks,
    fetchHomeworks,
    fetchHomeworkDetail,
    createHomework,
    updateHomework,
    deleteHomework,
    publishHomework,
    fetchQuestions,
    submitHomework,
    fetchScoringRules,
    fetchKnowledgePoints,
    clearCurrentData
  }
})
