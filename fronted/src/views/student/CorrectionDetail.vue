<template>
  <div class="max-w-6xl mx-auto space-y-6 animate-fade-in">
    <!-- 顶部成绩栏 -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 flex items-center justify-between">
      <div class="flex items-center gap-4">
        <el-button circle text @click="router.back()">
          <el-icon :size="20"><ArrowLeft /></el-icon>
        </el-button>
        <div>
          <h2 class="text-xl font-bold text-gray-800">批改详情：{{ homeworkTitle }}</h2>
          <p class="text-sm text-gray-500 mt-1">
            <el-tag :type="submissionData?.status === 1 ? 'warning' : 'success'" size="small" effect="light" round>{{ correctionType }}</el-tag>
            <span class="mx-2">|</span>
            <span>提交时间：{{ submitTime }}</span>
            <span class="mx-2">|</span>
            <span>用时：{{ duration }}</span>
          </p>
        </div>
      </div>
      <div class="flex items-center gap-4">
        <!-- 最终得分已隐藏 - 作业不需要分数 -->
      </div>
    </div>

    <div class="grid grid-cols-12 gap-6">
      <!-- 左侧题目详情 -->
      <div class="col-span-8 space-y-4">
        <div v-for="(q, idx) in questions" :key="q.id" 
             class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
          <div class="flex items-center gap-3 mb-4">
            <span class="w-10 h-10 rounded-xl bg-primary-100 text-primary-700 flex items-center justify-center font-bold text-lg">{{idx+1}}</span>
            <div class="flex-1">
              <span class="text-sm text-gray-500">{{q.type}} | {{q.knowledgePoint}}</span>
            </div>
            <!-- 题目得分已隐藏 - 作业不需要分数 -->
          </div>
          
          <p class="text-gray-800 font-medium mb-4 text-lg">{{q.content}}</p>
          
          <!-- 选择题/判断题选项 -->
          <div v-if="q.questionType === 'single_choice' || q.questionType === 'multiple_choice'" class="space-y-2 mb-4">
            <div v-for="(opt, optIdx) in q.options" :key="optIdx" 
                 class="flex items-start gap-3 p-3 rounded-xl border transition-all"
                 :class="{
                   'border-green-500 bg-green-50': isCorrectOption(q, optIdx),
                   'border-red-500 bg-red-50': isWrongOption(q, optIdx),
                   'border-gray-200 bg-white': !isCorrectOption(q, optIdx) && !isWrongOption(q, optIdx)
                 }">
              <span class="w-8 h-8 rounded-lg flex items-center justify-center font-bold text-sm shrink-0"
                    :class="{
                      'bg-green-500 text-white': isCorrectOption(q, optIdx),
                      'bg-red-500 text-white': isWrongOption(q, optIdx),
                      'bg-gray-100 text-gray-600': !isCorrectOption(q, optIdx) && !isWrongOption(q, optIdx)
                    }">
                {{ ['A', 'B', 'C', 'D', 'E', 'F'][optIdx] }}
              </span>
              <span class="text-gray-700 flex-1">{{ opt }}</span>
              <el-icon v-if="isCorrectOption(q, optIdx)" class="text-green-500"><CircleCheck /></el-icon>
              <el-icon v-if="isWrongOption(q, optIdx)" class="text-red-500"><CircleClose /></el-icon>
            </div>
          </div>
          
          <!-- 判断题选项 -->
          <div v-if="q.questionType === 'true_false'" class="grid grid-cols-2 gap-3 mb-4">
            <div class="p-3 rounded-xl border text-center transition-all"
                 :class="{
                   'border-green-500 bg-green-50': q.rawCorrectAnswer === 'true' || q.rawCorrectAnswer === '正确',
                   'border-red-500 bg-red-50': (q.rawAnswer === 'true' || q.rawAnswer === '正确') && !(q.rawCorrectAnswer === 'true' || q.rawCorrectAnswer === '正确'),
                   'border-gray-200 bg-white': !(q.rawCorrectAnswer === 'true' || q.rawCorrectAnswer === '正确') && !(q.rawAnswer === 'true' || q.rawAnswer === '正确')
                 }">
              <span class="font-bold" :class="(q.rawCorrectAnswer === 'true' || q.rawCorrectAnswer === '正确') ? 'text-green-600' : ((q.rawAnswer === 'true' || q.rawAnswer === '正确') ? 'text-red-600' : 'text-gray-600')">正确</span>
              <el-icon v-if="q.rawCorrectAnswer === 'true' || q.rawCorrectAnswer === '正确'" class="text-green-500 ml-2"><CircleCheck /></el-icon>
              <el-icon v-if="(q.rawAnswer === 'true' || q.rawAnswer === '正确') && !(q.rawCorrectAnswer === 'true' || q.rawCorrectAnswer === '正确')" class="text-red-500 ml-2"><CircleClose /></el-icon>
            </div>
            <div class="p-3 rounded-xl border text-center transition-all"
                 :class="{
                   'border-green-500 bg-green-50': q.rawCorrectAnswer === 'false' || q.rawCorrectAnswer === '错误',
                   'border-red-500 bg-red-50': (q.rawAnswer === 'false' || q.rawAnswer === '错误') && !(q.rawCorrectAnswer === 'false' || q.rawCorrectAnswer === '错误'),
                   'border-gray-200 bg-white': !(q.rawCorrectAnswer === 'false' || q.rawCorrectAnswer === '错误') && !(q.rawAnswer === 'false' || q.rawAnswer === '错误')
                 }">
              <span class="font-bold" :class="(q.rawCorrectAnswer === 'false' || q.rawCorrectAnswer === '错误') ? 'text-green-600' : ((q.rawAnswer === 'false' || q.rawAnswer === '错误') ? 'text-red-600' : 'text-gray-600')">错误</span>
              <el-icon v-if="q.rawCorrectAnswer === 'false' || q.rawCorrectAnswer === '错误'" class="text-green-500 ml-2"><CircleCheck /></el-icon>
              <el-icon v-if="(q.rawAnswer === 'false' || q.rawAnswer === '错误') && !(q.rawCorrectAnswer === 'false' || q.rawCorrectAnswer === '错误')" class="text-red-500 ml-2"><CircleClose /></el-icon>
            </div>
          </div>
          
          <!-- 答案对比 -->
          <div class="grid grid-cols-2 gap-4 mb-4">
            <div class="bg-gray-50 rounded-xl p-4">
              <div class="text-xs text-gray-500 mb-2 flex items-center gap-1">
                <el-icon><User /></el-icon>我的答案
              </div>
              <pre class="text-sm text-gray-700 whitespace-pre-wrap">{{q.answer || '未作答'}}</pre>
            </div>
            <div class="bg-green-50 rounded-xl p-4 border border-green-100">
              <div class="text-xs text-green-600 mb-2 flex items-center gap-1">
                <el-icon><CircleCheck /></el-icon>参考答案
              </div>
              <pre class="text-sm text-gray-700 whitespace-pre-wrap">{{q.correctAnswer}}</pre>
            </div>
          </div>
          
          <!-- AI智能分析 - 仅在AI批改时显示 -->
          <div v-if="submissionData?.homework?.aiEnabled" class="bg-gradient-to-r from-blue-50 to-green-50 rounded-xl p-4 border border-blue-100">
            <div class="flex items-center gap-2 mb-3 text-blue-700 font-bold">
              <el-icon :size="20"><Cpu /></el-icon>
              <span>BYG AI 智能分析</span>
            </div>
            
            <!-- AI分析内容 -->
            <div v-if="q.aiAnalysis" class="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">{{ q.aiAnalysis }}</div>
            <div v-else class="text-sm text-gray-500 italic">AI正在分析中，请稍后查看...</div>
          </div>
        </div>
      </div>

      <!-- 右侧能力分析 -->
      <div class="col-span-4 space-y-6">
        <!-- 能力维度 -->
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h3 class="font-bold mb-4 flex items-center gap-2">
            <el-icon class="text-blue-500"><DataAnalysis /></el-icon>
            能力维度分析
          </h3>
          <div ref="abilityChart" class="h-48 mb-4"></div>
          <div v-for="dim in dimensions" :key="dim.name" class="mb-3">
            <div class="flex justify-between text-sm mb-1">
              <span class="text-gray-600">{{dim.name}}</span>
              <span class="font-bold" :class="dim.value>=80?'text-green-600':dim.value>=60?'text-blue-600':'text-orange-600'">{{dim.value}}%</span>
            </div>
            <el-progress :percentage="dim.value" :color="dim.color" :show-text="false" :stroke-width="8" />
          </div>
        </div>
        
        <!-- DeepSeek评语 -->
        <div class="bg-gradient-to-br from-primary-500 to-primary-700 rounded-2xl p-6 text-white relative overflow-hidden">
          <div class="relative z-10">
            <div class="flex items-center gap-2 mb-3">
              <img src="/logo.png" alt="AI" class="w-6 h-6" />
              <h3 class="font-bold text-lg">BYG 评语</h3>
            </div>
            <p class="text-sm text-green-50 leading-relaxed italic">"{{ overallComment }}"</p>
          </div>
          <el-icon class="absolute -right-4 -bottom-4 text-white/10" :size="100"><ChatDotRound /></el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Cpu, CircleCheck, Warning, CircleClose, User, DataAnalysis, ChatDotRound } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { submissionApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const abilityChart = ref<HTMLDivElement>()

// 加载状态
const loading = ref(false)
const submissionData = ref<any>(null)
// 批改中提示状态
const showCorrectingAlert = ref(false)
const correctingMessage = ref('')

// 计算属性：作业ID和学生ID
const homeworkId = computed(() => Number(route.query.homeworkId))
const studentId = computed(() => Number(route.query.studentId))
const submissionId = computed(() => Number(route.params.id))

// 计算属性：顶部信息
const homeworkTitle = computed(() => submissionData.value?.homework?.title || '作业批改详情')
const submitTime = computed(() => {
  const time = submissionData.value?.submitTime
  return time ? time.replace('T', ' ') : '-'
})
const duration = computed(() => {
  // 计算实际用时
  const startTime = submissionData.value?.startTime
  const endTime = submissionData.value?.submitTime
  if (!startTime || !endTime) return '-'
  
  const start = new Date(startTime).getTime()
  const end = new Date(endTime).getTime()
  const diffMinutes = Math.round((end - start) / (1000 * 60))
  
  if (diffMinutes < 60) {
    return `${diffMinutes}分钟`
  } else {
    const hours = Math.floor(diffMinutes / 60)
    const mins = diffMinutes % 60
    return `${hours}小时${mins}分钟`
  }
})
const correctionType = computed(() => {
  // 优先使用作业的aiEnabled字段判断
  const aiEnabled = submissionData.value?.homework?.aiEnabled
  if (aiEnabled === true || aiEnabled === 1) {
    return 'AI智能批改'
  }
  // 如果没有aiEnabled字段，回退到根据状态判断
  if (submissionData.value?.status === 1) {
    return 'AI智能批改'
  }
  return '人工批改'
})
const finalScore = computed(() => submissionData.value?.totalScore || 0)
const fullScore = computed(() => {
  const answers = submissionData.value?.answers || []
  return answers.reduce((sum: number, a: any) => sum + (a.fullScore || 0), 0)
})

// 题目列表
const questions = computed(() => {
  const answers = submissionData.value?.answers || []
  return answers.map((a: any, idx: number) => ({
    id: a.questionId,
    questionType: a.questionType,
    type: getQuestionTypeName(a.questionType),
    knowledgePoint: '知识点' + (idx + 1),
    full: a.fullScore || 0,
    score: a.score || a.aiScore || 0,
    content: a.questionContent || '题目内容',
    options: a.questionOptions || [],
    answer: formatAnswer(a.studentAnswer, a.questionType),
    rawAnswer: a.studentAnswer,
    correctAnswer: formatAnswer(a.correctAnswer, a.questionType),
    rawCorrectAnswer: a.correctAnswer,
    aiAnalysis: a.aiAnalysis,
    feedback: parseFeedback(a.aiAnalysis),
    suggestion: extractSuggestion(a.aiAnalysis)
  }))
})

// 格式化答案显示
const formatAnswer = (answer: string, type?: string) => {
  if (!answer || answer === '未作答') return '未作答'
  
  // 单选题：索引转字母
  if (type === 'single_choice') {
    const idx = parseInt(answer)
    if (!isNaN(idx) && idx >= 0 && idx < 26) {
      return String.fromCharCode(65 + idx) // A, B, C...
    }
    return answer
  }
  
  // 多选题：索引数组转字母
  if (type === 'multiple_choice') {
    try {
      const arr = JSON.parse(answer)
      if (Array.isArray(arr)) {
        return arr.map((i: string) => {
          const idx = parseInt(i)
          return !isNaN(idx) && idx >= 0 && idx < 26 ? String.fromCharCode(65 + idx) : i
        }).join(', ')
      }
    } catch {
      // 不是JSON数组，返回原值
    }
    return answer
  }
  
  // 判断题
  if (type === 'true_false') {
    if (answer === 'true') return '正确'
    if (answer === 'false') return '错误'
    return answer
  }
  
  // 填空题：去掉数组括号
  if (type === 'fill_blank') {
    try {
      const arr = JSON.parse(answer)
      if (Array.isArray(arr)) {
        return arr.join('、')
      }
    } catch {
      // 不是JSON数组，返回原值
    }
    return answer
  }
  
  return answer
}

// 判断是否为正确选项
const isCorrectOption = (q: any, optIdx: number) => {
  if (!q.rawCorrectAnswer) return false
  
  // 判断题特殊处理：选项0=正确，1=错误
  if (q.questionType === 'true_false') {
    const correctAnswer = q.rawCorrectAnswer.toLowerCase()
    if (optIdx === 0) {
      return correctAnswer === 'true' || correctAnswer === '正确'
    } else if (optIdx === 1) {
      return correctAnswer === 'false' || correctAnswer === '错误'
    }
    return false
  }
  
  // 单选题和多选题的原有逻辑
  const correctIdx = parseInt(q.rawCorrectAnswer)
  if (!isNaN(correctIdx) && correctIdx === optIdx) return true
  // 多选题的情况
  try {
    const correctArr = JSON.parse(q.rawCorrectAnswer || '[]')
    return Array.isArray(correctArr) && correctArr.includes(String(optIdx))
  } catch {
    // 检查是否是字母格式 (A, B, C...)
    const letter = String.fromCharCode(65 + optIdx)
    if (q.rawCorrectAnswer === letter) return true
    if (q.rawCorrectAnswer?.includes(letter)) return true
    return false
  }
}

// 判断是否为学生选错的选项
const isWrongOption = (q: any, optIdx: number) => {
  // 判断题特殊处理
  if (q.questionType === 'true_false') {
    // 学生选中此选项但不是正确答案
    const studentAnswer = q.rawAnswer?.toLowerCase()
    const correctAnswer = q.rawCorrectAnswer?.toLowerCase()
    
    // 检查学生是否选中了这个选项
    let studentSelectedThis = false
    if (optIdx === 0) {
      studentSelectedThis = studentAnswer === 'true' || studentAnswer === '正确'
    } else if (optIdx === 1) {
      studentSelectedThis = studentAnswer === 'false' || studentAnswer === '错误'
    }
    
    // 检查这个选项是否是正确答案
    let isCorrect = false
    if (optIdx === 0) {
      isCorrect = correctAnswer === 'true' || correctAnswer === '正确'
    } else if (optIdx === 1) {
      isCorrect = correctAnswer === 'false' || correctAnswer === '错误'
    }
    
    return studentSelectedThis && !isCorrect
  }
  
  // 单选题和多选题的原有逻辑
  const studentIdx = parseInt(q.rawAnswer)
  if (!isNaN(studentIdx) && studentIdx === optIdx && !isCorrectOption(q, optIdx)) return true
  // 多选题的情况
  try {
    const studentArr = JSON.parse(q.rawAnswer || '[]')
    const correctArr = JSON.parse(q.rawCorrectAnswer || '[]')
    if (Array.isArray(studentArr) && studentArr.includes(String(optIdx))) {
      return !correctArr.includes(String(optIdx))
    }
    return false
  } catch {
    return false
  }
}

// 能力维度分析（基于答题数据计算）
const dimensions = computed(() => {
  const answers = submissionData.value?.answers || []
  if (answers.length === 0) {
    return [
      { name: '知识掌握', value: 0, color: '#22c55e' },
      { name: '逻辑推理', value: 0, color: '#3b82f6' },
      { name: '分析能力', value: 0, color: '#f59e0b' },
      { name: '审题严谨', value: 0, color: '#ef4444' }
    ]
  }
  
  // 计算总正确率
  const correctCount = answers.filter((a: any) => (a.aiScore || a.score || 0) >= (a.fullScore || 0) * 0.6).length
  const totalCount = answers.length
  const baseRate = totalCount > 0 ? Math.round((correctCount / totalCount) * 100) : 0
  
  // 根据题型分布计算各维度能力
  const typeStats: Record<string, { correct: number; total: number }> = {}
  answers.forEach((a: any) => {
    const type = a.questionType || 'subjective'
    if (!typeStats[type]) {
      typeStats[type] = { correct: 0, total: 0 }
    }
    typeStats[type].total++
    if ((a.aiScore || a.score || 0) >= (a.fullScore || 0) * 0.6) {
      typeStats[type].correct++
    }
  })
  
  // 根据题型计算各维度得分
  const getTypeRate = (types: string[]) => {
    let correct = 0
    let total = 0
    types.forEach(t => {
      if (typeStats[t]) {
        correct += typeStats[t].correct
        total += typeStats[t].total
      }
    })
    return total > 0 ? Math.round((correct / total) * 100) : baseRate
  }
  
  return [
    { name: '知识掌握', value: getTypeRate(['single_choice', 'multiple_choice', 'true_false']), color: '#22c55e' },
    { name: '逻辑推理', value: getTypeRate(['multiple_choice', 'reading_comprehension']), color: '#3b82f6' },
    { name: '分析能力', value: getTypeRate(['subjective', 'reading_comprehension']), color: '#f59e0b' },
    { name: '审题严谨', value: Math.max(50, baseRate - 5), color: '#ef4444' }
  ]
})

// AI评语
const overallComment = computed(() => {
  const answers = submissionData.value?.answers || []
  const totalScore = answers.reduce((sum: number, a: any) => sum + (a.aiScore || a.score || 0), 0)
  const fullScore = answers.reduce((sum: number, a: any) => sum + (a.fullScore || 0), 0)
  const rate = fullScore > 0 ? Math.round((totalScore / fullScore) * 100) : 0
  
  if (rate >= 90) return '表现优秀！对知识点的掌握非常扎实，继续保持！'
  if (rate >= 80) return '表现良好，基础知识掌握较好，但还有提升空间。'
  if (rate >= 60) return '基本掌握，但需要针对薄弱知识点加强练习。'
  return '需要加强基础知识的学习，建议多进行针对性练习。'
})

// 辅助函数：解析AI分析为反馈列表
function parseFeedback(aiAnalysis?: string) {
  if (!aiAnalysis) {
    return [{ type: 'warning', text: 'AI正在分析中，请稍后查看...' }]
  }
  const feedback = []
  if (aiAnalysis.includes('正确') || aiAnalysis.includes('准确')) {
    feedback.push({ type: 'success', text: '✓ 答案正确，思路清晰' })
  }
  if (aiAnalysis.includes('步骤') || aiAnalysis.includes('过程')) {
    feedback.push({ type: 'success', text: '✓ 解题步骤完整' })
  }
  if (aiAnalysis.includes('注意') || aiAnalysis.includes('建议')) {
    feedback.push({ type: 'warning', text: '⚠ ' + aiAnalysis.split('。').find((s: string) => s.includes('建议') || s.includes('注意')) || '注意细节' })
  }
  if (feedback.length === 0) {
    feedback.push({ type: 'info', text: aiAnalysis.substring(0, 100) + (aiAnalysis.length > 100 ? '...' : '') })
  }
  return feedback
}

// 辅助函数：提取建议
function extractSuggestion(aiAnalysis?: string) {
  if (!aiAnalysis) return ''
  const match = aiAnalysis.match(/建议[：:]([^。]+)/)
  return match ? match[1] : ''
}

// 辅助函数：题型名称转换
function getQuestionTypeName(type?: string) {
  const typeMap: Record<string, string> = {
    'single_choice': '单选题',
    'multiple_choice': '多选题',
    'fill_blank': '填空题',
    'true_false': '判断题',
    'subjective': '主观题',
    'reading_comprehension': '阅读理解'
  }
  return typeMap[type || ''] || '主观题'
}

// 加载数据
const loadCorrectionDetail = async () => {
  loading.value = true
  try {
    let data
    if (homeworkId.value && studentId.value) {
      // 通过作业ID和学生ID获取
      data = await submissionApi.getSubmissionByHomeworkAndStudent(homeworkId.value, studentId.value)
    } else if (submissionId.value) {
      // 通过提交ID获取
      data = await submissionApi.getSubmissionById(submissionId.value)
    } else {
      ElMessage.error('缺少必要的参数')
      return
    }
    submissionData.value = data
    
    // 检查批改状态
    if (data?.status === 1) {
      // 批改中状态
      showCorrectingAlert.value = true
      correctingMessage.value = '作业正在批改中，请稍后再查看详情...'
      
      ElMessageBox.alert(
        '作业正在AI智能批改中，请稍后再来查看批改结果',
        '批改中',
        {
          confirmButtonText: '知道了',
          type: 'warning',
          showClose: false,
          callback: () => {
            // 可以选择返回上一页或停留在当前页面
            // router.back()
          }
        }
      )
    } else if (data?.status === 0) {
      // 未开始状态
      showCorrectingAlert.value = true
      correctingMessage.value = '作业尚未开始批改...'
    }
    
    console.log('Submission data:', data)
    console.log('Answers:', data?.answers)
    if (data?.answers) {
      data.answers.forEach((a: any, i: number) => {
        console.log(`Question ${i+1}:`, {
          type: a.questionType,
          options: a.questionOptions,
          content: a.questionContent?.substring(0, 30)
        })
      })
    }
  } catch (error: any) {
    ElMessage.error('加载批改详情失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 初始化雷达图
const initChart = () => {
  if (abilityChart.value && dimensions.value.length > 0) {
    const chart = echarts.init(abilityChart.value)
    const option = {
      radar: {
        indicator: dimensions.value.map(d => ({ name: d.name, max: 100 })),
        radius: '65%',
        axisName: { color: '#666', fontSize: 10 }
      },
      series: [{
        type: 'radar',
        data: [{
          value: dimensions.value.map(d => d.value),
          name: '能力分布',
          areaStyle: { color: 'rgba(34, 197, 94, 0.2)' },
          lineStyle: { color: '#22c55e', width: 2 },
          itemStyle: { color: '#22c55e' }
        }]
      }]
    }
    chart.setOption(option)
    window.addEventListener('resize', () => chart.resize())
  }
}

onMounted(() => {
  loadCorrectionDetail()
  initChart()
})
</script>
