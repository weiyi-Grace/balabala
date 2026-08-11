import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { CorrectionRecord, ErrorBook, StudentAnalytics, ClassAnalytics } from '@/types'
import { correctionApi } from '@/api/homework'
import { errorBookApi, analyticsApi } from '@/api/analytics'

export const useCorrectionStore = defineStore('correction', () => {
  // State
  const corrections = ref<CorrectionRecord[]>([])
  const currentCorrection = ref<CorrectionRecord | null>(null)
  const errorBooks = ref<ErrorBook[]>([])
  const studentAnalytics = ref<StudentAnalytics | null>(null)
  const classAnalytics = ref<ClassAnalytics | null>(null)
  const loading = ref(false)
  
  // Getters
  const pendingCorrections = computed(() => 
    corrections.value.filter(c => c.status === 0)
  )
  const completedCorrections = computed(() => 
    corrections.value.filter(c => c.status === 2)
  )
  const autoCorrections = computed(() => 
    corrections.value.filter(c => c.correctionType === 1)
  )
  const manualCorrections = computed(() => 
    corrections.value.filter(c => c.correctionType === 2)
  )
  
  // Actions
  
  // 获取批改记录列表
  const fetchCorrections = async (params?: any) => {
    loading.value = true
    try {
      const result = await correctionApi.getCorrectionList(params || {})
      corrections.value = result.records
      return result
    } finally {
      loading.value = false
    }
  }
  
  // 获取批改详情
  const fetchCorrectionDetail = async (id: number) => {
    loading.value = true
    try {
      const correction = await correctionApi.getCorrectionById(id)
      currentCorrection.value = correction
      return correction
    } finally {
      loading.value = false
    }
  }
  
  // 触发自动批改
  const autoCorrect = async (submissionId: number, ruleId?: number) => {
    loading.value = true
    try {
      const correction = await correctionApi.triggerAutoCorrection(submissionId, ruleId)
      
      // 更新列表
      const index = corrections.value.findIndex(c => c.id === correction.id)
      if (index !== -1) {
        corrections.value[index] = correction
      } else {
        corrections.value.unshift(correction)
      }
      
      currentCorrection.value = correction
      return correction
    } finally {
      loading.value = false
    }
  }
  
  // 人工批改
  const manualCorrect = async (submissionId: number, data: Partial<CorrectionRecord>) => {
    loading.value = true
    try {
      const correction = await correctionApi.manualCorrection(submissionId, data)
      
      // 更新列表
      const index = corrections.value.findIndex(c => c.id === correction.id)
      if (index !== -1) {
        corrections.value[index] = correction
      }
      
      currentCorrection.value = correction
      return correction
    } finally {
      loading.value = false
    }
  }
  
  // 重新批改
  const reCorrect = async (correctionId: number, ruleId?: number) => {
    loading.value = true
    try {
      const correction = await correctionApi.reCorrect(correctionId, ruleId)
      
      const index = corrections.value.findIndex(c => c.id === correctionId)
      if (index !== -1) {
        corrections.value[index] = correction
      }
      
      if (currentCorrection.value?.id === correctionId) {
        currentCorrection.value = correction
      }
      
      return correction
    } finally {
      loading.value = false
    }
  }
  
  // 获取错题本
  const fetchErrorBooks = async (params?: any) => {
    loading.value = true
    try {
      const result = await errorBookApi.getMyErrorBook(params || {})
      errorBooks.value = result.records
      return result
    } finally {
      loading.value = false
    }
  }
  
  // 更新错题订正状态
  const updateErrorStatus = async (id: number, status: number, record?: string) => {
    const errorBook = await errorBookApi.updateCorrectionStatus(id, status, record)
    
    const index = errorBooks.value.findIndex(e => e.id === id)
    if (index !== -1) {
      errorBooks.value[index] = errorBook
    }
    
    return errorBook
  }
  
  // 获取学生学情分析
  const fetchStudentAnalytics = async (studentId?: number) => {
    loading.value = true
    try {
      const analytics = await analyticsApi.getStudentAnalytics(studentId)
      studentAnalytics.value = analytics
      return analytics
    } finally {
      loading.value = false
    }
  }
  
  // 获取班级学情分析
  const fetchClassAnalytics = async (classId: number, params?: any) => {
    loading.value = true
    try {
      const analytics = await analyticsApi.getClassAnalytics(classId, params)
      classAnalytics.value = analytics
      return analytics
    } finally {
      loading.value = false
    }
  }
  
  // 清空当前数据
  const clearCurrentData = () => {
    currentCorrection.value = null
    studentAnalytics.value = null
    classAnalytics.value = null
  }
  
  return {
    corrections,
    currentCorrection,
    errorBooks,
    studentAnalytics,
    classAnalytics,
    loading,
    pendingCorrections,
    completedCorrections,
    autoCorrections,
    manualCorrections,
    fetchCorrections,
    fetchCorrectionDetail,
    autoCorrect,
    manualCorrect,
    reCorrect,
    fetchErrorBooks,
    updateErrorStatus,
    fetchStudentAnalytics,
    fetchClassAnalytics,
    clearCurrentData
  }
})
