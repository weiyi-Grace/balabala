<template>
  <div class="max-w-7xl mx-auto animate-fade-in">
    <!-- 顶部导航栏 -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 mb-6">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <el-button circle text @click="router.back()">
            <el-icon :size="20"><ArrowLeft /></el-icon>
          </el-button>
          <div>
            <h2 class="text-xl font-bold text-gray-800">作业批改</h2>
            <p class="text-sm text-gray-500 mt-1">语文阅读理解专项练习 | 高二1班</p>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <!-- 待批改/已批改统计 -->
          <div class="text-center px-4 py-2 bg-blue-50 rounded-xl">
            <div class="text-xs text-blue-500">待批改</div>
            <div class="text-lg font-bold text-blue-600">{{ pendingCount }}<span class="text-sm font-normal">份</span></div>
          </div>
          <div class="text-center px-4 py-2 bg-green-50 rounded-xl">
            <div class="text-xs text-green-500">已批改</div>
            <div class="text-lg font-bold text-green-600">{{ correctedCount }}<span class="text-sm font-normal">份</span></div>
          </div>
          <el-divider direction="vertical" class="mx-2" />
          <el-button type="primary" :loading="batchSaving" @click="saveBatch">
            <el-icon class="mr-1"><Check /></el-icon>
            批量保存
          </el-button>
        </div>
      </div>
    </div>

    <div v-if="!homeworkId" class="bg-white rounded-2xl p-12 shadow-sm border border-gray-100 text-center">
      <el-icon :size="64" class="text-gray-300 mb-4"><Document /></el-icon>
      <p class="text-gray-500 mb-4">请先选择要批改的作业</p>
      <el-button type="primary" @click="router.push('/teacher/homework')">去作业列表</el-button>
    </div>

    <div v-else class="grid grid-cols-12 gap-6">
      <!-- 左侧学生列表 -->
      <div class="col-span-3">
        <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 sticky top-4">
          <div class="flex items-center justify-between mb-4">
            <h3 class="font-bold text-lg">学生列表</h3>
            <el-tag type="primary" effect="light" round>{{ pendingCount }} 待批</el-tag>
          </div>
          
          <!-- 筛选 -->
          <div class="flex gap-2 mb-4">
            <el-button 
              size="small" 
              :type="filterStatus === 'all' ? 'primary' : 'default'"
              @click="filterStatus = 'all'"
            >全部</el-button>
            <el-button 
              size="small" 
              :type="filterStatus === 'submitted' ? 'primary' : 'default'"
              @click="filterStatus = 'submitted'"
            >待批改</el-button>
            <el-button 
              size="small" 
              :type="filterStatus === 'corrected' ? 'primary' : 'default'"
              @click="filterStatus = 'corrected'"
            >已批改</el-button>
          </div>

          <!-- 学生列表 -->
          <div class="space-y-2 max-h-[calc(100vh-300px)] overflow-y-auto">
            <div 
              v-for="student in filteredStudents" 
              :key="student.id"
              class="flex items-center gap-3 p-3 rounded-xl cursor-pointer transition-all"
              :class="{ 
                'bg-primary-50 border-2 border-primary-200': currentStudent?.id === student.id,
                'hover:bg-gray-50 border border-transparent': currentStudent?.id !== student.id,
                'opacity-60': student.status === 'corrected'
              }"
              @click="selectStudent(student)"
            >
              <el-avatar :size="36" class="bg-primary-100 text-primary-600 text-sm">
                {{ student.name.charAt(0) }}
              </el-avatar>
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2">
                  <span class="font-medium text-sm truncate">{{ student.name }}</span>
                  <el-tag 
                    v-if="student.status === 'corrected'" 
                    type="success" 
                    effect="light" 
                    size="small"
                    round
                  >已批改</el-tag>
                  <el-tag 
                    v-else 
                    type="warning" 
                    effect="light" 
                    size="small"
                    round
                  >待批改</el-tag>
                </div>
                <div class="flex items-center gap-3 mt-1">
                  <span class="text-xs text-gray-400">{{ student.submitTime }}</span>
                </div>
              </div>
              <el-icon v-if="student.status === 'corrected'" class="text-green-500"><CircleCheck /></el-icon>
              <el-icon v-else class="text-orange-400"><Clock /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间批改区域 -->
      <div class="col-span-6 space-y-6">
        <div v-if="currentStudent" class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <!-- 学生信息头部 -->
          <div class="flex items-center justify-between mb-6 pb-4 border-b border-gray-100">
            <div class="flex items-center gap-3">
              <el-avatar :size="48" class="bg-primary-100 text-primary-600">
                {{ currentStudent.name.charAt(0) }}
              </el-avatar>
              <div>
                <h3 class="font-bold text-lg">{{ currentStudent.name }}</h3>
                <p class="text-sm text-gray-500">提交时间: {{ currentStudent.submitTime }}</p>
              </div>
            </div>
            <div class="flex items-center gap-4">
              <!-- 当前得分已隐藏 - 改为展示正确率 -->
              <div class="text-right">
                <div class="text-sm text-gray-500">正确率</div>
                <div class="text-2xl font-bold text-primary-600">
                  {{ calculateCorrectRate }}<span class="text-base text-gray-400">%</span>
                </div>
              </div>
              <el-button type="primary" :loading="saving" @click="saveCorrection">
                <el-icon class="mr-1"><Check /></el-icon>
                保存批改
              </el-button>
            </div>
          </div>

          <!-- 题目批改列表 -->
          <div class="space-y-6">
            <div 
              v-for="(question, idx) in questions" 
              :key="question.id"
              class="border border-gray-100 rounded-xl p-5 cursor-pointer"
              :class="{ 
                'ring-2 ring-primary-300 bg-primary-50/30': currentQuestion === idx,
                'hover:bg-gray-50': currentQuestion !== idx
              }"
              @click="currentQuestion = idx"
            >
              <!-- 题目头部 -->
              <div class="flex items-center gap-3 mb-4">
                <span class="w-8 h-8 rounded-lg bg-primary-100 text-primary-700 flex items-center justify-center font-bold text-sm">
                  {{ idx + 1 }}
                </span>
                <span class="text-sm text-gray-500">
                  {{ getQuestionTypeName(question.type) }}
                </span>
                <el-tag 
                  v-if="getCorrectionStatus(question)" 
                  :type="getCorrectionStatus(question) === 'correct' ? 'success' : 'danger'"
                  effect="light"
                  size="small"
                  round
                  class="ml-auto"
                >
                  {{ getCorrectionStatus(question) === 'correct' ? '正确' : '错误' }}
                </el-tag>
              </div>

              <!-- 题目内容 -->
              <p class="text-gray-800 font-medium mb-4">{{ question.content }}</p>

              <!-- 选择题/判断题选项 -->
              <div v-if="question.type === 'single_choice' || question.type === 'multiple_choice' || question.type === 'true_false'" 
                   class="space-y-2 mb-4">
                <div v-for="(opt, optIdx) in question.options" :key="optIdx" 
                     class="flex items-start gap-3 p-3 rounded-xl border"
                     :class="{
                       'border-green-500 bg-green-50 ring-2 ring-green-300': (isCorrectOption(question, Number(optIdx)) && isStudentSelectedOption(question, Number(optIdx))) || (isStudentSelectedOption(question, Number(optIdx)) && !isCorrectOption(question, Number(optIdx)) && !isWrongOption(question, Number(optIdx)) && question.type === 'true_false'),
                       'border-green-500 bg-green-50': isCorrectOption(question, Number(optIdx)) && !isStudentSelectedOption(question, Number(optIdx)),
                       'border-red-500 bg-red-50 ring-2 ring-red-300': isWrongOption(question, Number(optIdx)),
                       'border-blue-500 bg-blue-50 ring-2 ring-blue-300': isStudentSelectedOption(question, Number(optIdx)) && !isCorrectOption(question, Number(optIdx)) && !isWrongOption(question, Number(optIdx)) && question.type !== 'true_false',
                       'border-gray-200 bg-white': !isCorrectOption(question, Number(optIdx)) && !isWrongOption(question, Number(optIdx)) && !isStudentSelectedOption(question, Number(optIdx))
                     }">
                  <span class="w-8 h-8 rounded-lg flex items-center justify-center font-bold text-sm shrink-0"
                        :class="{
                          'bg-green-500 text-white': isCorrectOption(question, Number(optIdx)) || (isStudentSelectedOption(question, Number(optIdx)) && !isCorrectOption(question, Number(optIdx)) && !isWrongOption(question, Number(optIdx)) && question.type === 'true_false'),
                          'bg-red-500 text-white': isWrongOption(question, Number(optIdx)),
                          'bg-blue-500 text-white': isStudentSelectedOption(question, Number(optIdx)) && !isCorrectOption(question, Number(optIdx)) && !isWrongOption(question, Number(optIdx)) && question.type !== 'true_false',
                          'bg-gray-100 text-gray-600': !isCorrectOption(question, Number(optIdx)) && !isWrongOption(question, Number(optIdx)) && !isStudentSelectedOption(question, Number(optIdx))
                        }">
                    {{ ['A', 'B', 'C', 'D', 'E', 'F'][Number(optIdx)] }}
                  </span>
                  <span class="text-gray-700 flex-1">{{ opt }}</span>
                  <el-icon v-if="isCorrectOption(question, Number(optIdx))" class="text-green-500"><CircleCheck /></el-icon>
                  <el-icon v-if="isWrongOption(question, Number(optIdx))" class="text-red-500"><CircleClose /></el-icon>
                  <el-icon v-if="isStudentSelectedOption(question, Number(optIdx)) && !isCorrectOption(question, Number(optIdx)) && !isWrongOption(question, Number(optIdx))" class="text-blue-500"><CircleCheck /></el-icon>
                </div>
                
                <!-- 判断题固定选项 -->
                <div v-if="question.type === 'true_false' && (!question.options || question.options.length === 0)" class="grid grid-cols-2 gap-3">
                  <div class="p-3 rounded-xl border text-center"
                       :class="question.correctAnswer === '正确' || question.correctAnswer === 'true' ? 'border-green-500 bg-green-50' : (question.studentAnswer === '正确' || question.studentAnswer === 'true' ? 'border-red-500 bg-red-50' : 'border-gray-200 bg-white')">
                    <span class="font-bold" :class="question.correctAnswer === '正确' || question.correctAnswer === 'true' ? 'text-green-600' : (question.studentAnswer === '正确' || question.studentAnswer === 'true' ? 'text-red-600' : 'text-gray-600')">正确</span>
                    <el-icon v-if="question.correctAnswer === '正确' || question.correctAnswer === 'true'" class="text-green-500 ml-2"><CircleCheck /></el-icon>
                    <el-icon v-if="(question.studentAnswer === '正确' || question.studentAnswer === 'true') && (question.correctAnswer !== '正确' && question.correctAnswer !== 'true')" class="text-red-500 ml-2"><CircleClose /></el-icon>
                  </div>
                  <div class="p-3 rounded-xl border text-center"
                       :class="question.correctAnswer === '错误' || question.correctAnswer === 'false' ? 'border-green-500 bg-green-50' : (question.studentAnswer === '错误' || question.studentAnswer === 'false' ? 'border-red-500 bg-red-50' : 'border-gray-200 bg-white')">
                    <span class="font-bold" :class="question.correctAnswer === '错误' || question.correctAnswer === 'false' ? 'text-green-600' : (question.studentAnswer === '错误' || question.studentAnswer === 'false' ? 'text-red-600' : 'text-gray-600')">错误</span>
                    <el-icon v-if="question.correctAnswer === '错误' || question.correctAnswer === 'false'" class="text-green-500 ml-2"><CircleCheck /></el-icon>
                    <el-icon v-if="(question.studentAnswer === '错误' || question.studentAnswer === 'false') && (question.correctAnswer !== '错误' && question.correctAnswer !== 'false')" class="text-red-500 ml-2"><CircleClose /></el-icon>
                  </div>
                </div>
              </div>

              <!-- 学生答案 -->
              <div class="bg-gray-50 rounded-xl p-4 mb-4">
                <div class="text-xs text-gray-500 mb-2 flex items-center gap-1">
                  <el-icon><User /></el-icon>
                  学生答案
                </div>
                <div class="text-sm text-gray-700">{{ formatAnswer(question.studentAnswer, question.type) }}</div>
              </div>

              <!-- 参考答案 -->
              <div class="bg-green-50 rounded-xl p-4 mb-4 border border-green-100">
                <div class="text-xs text-green-600 mb-2 flex items-center gap-1">
                  <el-icon><Check /></el-icon>
                  参考答案
                </div>
                <div class="text-sm text-gray-700">{{ question.correctAnswer }}</div>
              </div>

              <!-- AI 分析 - 仅在作业启用AI批改时显示 -->
              <div v-if="homeworkInfo?.aiEnabled && question.aiAnalysis" class="bg-blue-50 rounded-xl p-4 mb-4">
                <div class="flex items-center gap-2 mb-2 text-blue-700 font-bold">
                  <el-icon><Cpu /></el-icon>
                  BYG AI 分析
                </div>
                <p class="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">{{ question.aiAnalysis }}</p>
              </div>
              <div v-else-if="homeworkInfo?.aiEnabled && currentStudent?.status === 'submitted'" class="bg-orange-50 rounded-xl p-4 mb-4">
                <div class="flex items-center gap-2 mb-2 text-orange-600">
                  <el-icon><Clock /></el-icon>
                  <span class="font-medium">AI批改中</span>
                </div>
                <p class="text-sm text-gray-600">AI正在批改中，请稍后再查看或手动触发批改</p>
                <div class="mt-3 text-center">
                  <el-button type="primary" size="small" :icon="MagicStick" @click="autoCorrectWithAI">
                    立即AI批改
                  </el-button>
                </div>
              </div>
              <div v-else-if="homeworkInfo?.aiEnabled" class="bg-gray-50 rounded-xl p-4 mb-4 text-center">
                <el-button type="primary" :icon="MagicStick" @click="autoCorrectWithAI">AI自动批改</el-button>
              </div>

              <!-- 评分区域 - 改为正确/错误判定 -->
              <div class="flex items-center gap-4 pt-4 border-t border-gray-100">
                <div class="flex items-center gap-2">
                  <span class="text-sm text-gray-600">判定:</span>
                  <el-radio-group v-model="question.isCorrect" size="small">
                    <el-radio-button :label="true">正确</el-radio-button>
                    <el-radio-button :label="false">错误</el-radio-button>
                  </el-radio-group>
                </div>
                <div class="flex-1">
                  <el-input 
                    v-model="question.comment" 
                    placeholder="评语（可选）"
                    size="small"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- 总评 -->
          <div class="mt-6 pt-6 border-t border-gray-100">
            <h4 class="font-bold mb-3">总评评语</h4>
            <el-input 
              v-model="overallComment" 
              type="textarea" 
              :rows="3"
              placeholder="请输入对学生本次作业的整体评价..."
            />
          </div>
        </div>

        <div v-else class="bg-white rounded-2xl p-12 shadow-sm border border-gray-100 text-center">
          <el-icon :size="64" class="text-gray-300 mb-4"><Document /></el-icon>
          <p class="text-gray-500">请从左侧选择一个学生进行批改</p>
        </div>
      </div>

      <!-- 右侧工具栏 -->
      <div class="col-span-3">
        <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 sticky top-4 space-y-6">
          <!-- 快速判定 -->
          <div>
            <h4 class="font-bold mb-3 flex items-center gap-2">
              <el-icon class="text-primary-500"><Star /></el-icon>
              快速判定
            </h4>
            <div class="grid grid-cols-2 gap-2">
              <el-button size="small" type="danger" @click="quickCorrect(false)">错误</el-button>
              <el-button size="small" type="success" @click="quickCorrect(true)">正确</el-button>
            </div>
          </div>

          <el-divider />

          <!-- AI自动批改 - 仅在作业启用AI批改时显示 -->
          <div v-if="homeworkInfo?.aiEnabled">
            <h4 class="font-bold mb-3 flex items-center gap-2">
              <el-icon class="text-blue-500"><Cpu /></el-icon>
              AI自动批改
            </h4>
            <el-button type="primary" size="small" @click="autoCorrectWithAI" :disabled="!currentStudent">
              <el-icon class="mr-1"><MagicStick /></el-icon>
              一键AI批改
            </el-button>
          </div>

          <el-divider v-if="homeworkInfo?.aiEnabled" />

          <!-- 常用评语 -->
          <div>
            <h4 class="font-bold mb-3 flex items-center gap-2">
              <el-icon class="text-green-500"><ChatDotRound /></el-icon>
              常用评语
            </h4>
            <div class="space-y-2">
              <div 
                v-for="comment in commonComments" 
                :key="comment"
                class="text-sm text-gray-600 p-2 bg-gray-50 rounded-lg cursor-pointer hover:bg-gray-100 transition-colors"
                @click="addComment(comment)"
              >
                {{ comment }}
              </div>
            </div>
          </div>

          <el-divider />

          <!-- 批改统计 -->
          <div>
            <h4 class="font-bold mb-3">批改统计</h4>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-gray-500">题目数</span>
                <span class="font-bold">{{ questions.length }} 题</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">正确</span>
                <span class="font-bold text-green-600">{{ correctCount }} 题</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">错误</span>
                <span class="font-bold text-red-500">{{ wrongCount }} 题</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">正确率</span>
                <span class="font-bold text-primary-600">{{ calculateCorrectRate }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  ArrowLeft, 
  Check, 
  Clock, 
  User, 
  EditPen, 
  Star,
  TrendCharts,
  Warning,
  Document,
  DocumentChecked,
  MagicStick,
  CircleCheck,
  CircleClose,
  ChatDotRound,
  Cpu
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { submissionApi, aiApi, homeworkApi } from '@/api'
import { useUserStore } from '@/stores'
import { correctionWebSocket } from '@/services/websocket'

const route = useRoute()
const router = useRouter()
const homeworkId = computed(() => Number(route.query.homeworkId))

const loading = ref(false)
const saving = ref(false)
const batchSaving = ref(false)
const currentStudent = ref<any>(null)
const currentQuestion = ref(0)
const filterStatus = ref('all')
const overallComment = ref('')  // 添加总评评语变量

const homeworkInfo = ref<any>(null)

// 加载作业信息
const loadHomeworkInfo = async () => {
  if (!homeworkId.value) return
  try {
    const res = await homeworkApi.getHomeworkById(homeworkId.value)
    homeworkInfo.value = res
  } catch (error) {
    console.error('加载作业信息失败', error)
  }
}

// 学生列表
const students = ref<any[]>([])

// 加载学生提交列表
const loadSubmissions = async () => {
  if (!homeworkId.value) return
  
  loading.value = true
  try {
    const submissions = await submissionApi.getSubmissionsByHomework(homeworkId.value)
    students.value = submissions.map((s: any) => ({
      id: s.studentId,
      name: s.studentName || `学生${s.studentId}`,
      status: s.status === 2 ? 'corrected' : s.status === 1 ? 'submitted' : 'pending',
      submitTime: s.submitTime?.replace('T', ' ') || '',
      score: s.totalScore,
      avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${s.studentId}`
    }))
  } catch (error: any) {
    ElMessage.error('加载提交列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 题目数据
const questions = ref<any[]>([])

// 加载学生答题详情
const loadSubmissionDetail = async (studentId: number) => {
  if (!homeworkId.value) return
  
  try {
    const submission = await submissionApi.getSubmissionByHomeworkAndStudent(homeworkId.value, studentId)
    console.log('Submission data:', submission)
    questions.value = submission.answers?.map((a: any) => ({
      id: a.questionId,
      type: a.questionType,
      content: a.questionContent,
      score: a.fullScore,
      studentAnswer: a.studentAnswer,
      correctAnswer: a.correctAnswer,
      aiAnalysis: a.aiAnalysis,
      gotScore: a.score,
      isCorrect: a.score === a.fullScore ? true : a.score === 0 ? false : null,
      comment: a.comment || '',
      options: a.questionOptions || []
    })) || []
    console.log('Questions with options:', questions.value)
  } catch (error: any) {
    ElMessage.error('加载答题详情失败: ' + error.message)
  }
}

// 常用评语
const commonComments = [
  '回答准确，思路清晰！',
  '基本正确，但细节不够完整。',
  '理解到位，表达流畅。',
  '需要加强对知识点的理解。',
  '注意审题，理解题意有误。',
  '书写工整，值得表扬！'
]

// 待批改数量
const pendingCount = computed(() => students.value.filter(s => s.status !== 'corrected').length)

// 已批改数量
const correctedCount = computed(() => students.value.filter(s => s.status === 'corrected').length)

// 计算正确率
const calculateCorrectRate = computed(() => {
  const total = questions.value.length
  if (total === 0) return 0
  const correct = questions.value.filter(q => q.isCorrect === true).length
  return Math.round((correct / total) * 100)
})

// 正确题数
const correctCount = computed(() => questions.value.filter(q => q.isCorrect === true).length)

// 错误题数
const wrongCount = computed(() => questions.value.filter(q => q.isCorrect === false).length)

// 已判定数量
const scoredCount = computed(() => questions.value.filter(q => q.isCorrect !== null && q.isCorrect !== undefined).length)

// 筛选后的学生列表
const filteredStudents = computed(() => {
  if (filterStatus.value === 'all') return students.value
  return students.value.filter(s => s.status === filterStatus.value)
})

// 未判定数量
const unscoredCount = computed(() => questions.value.filter(q => q.isCorrect === null || q.isCorrect === undefined).length)

// 计算总得分
const calculateTotalScore = computed(() => {
  const total = questions.value.reduce((sum, q) => sum + (q.gotScore || 0), 0)
  return total
})

// 获取题型名称
const getQuestionTypeName = (type: string) => {
  const map: Record<string, string> = {
    single_choice: '单选题',
    multiple_choice: '多选题',
    fill_blank: '填空题',
    true_false: '判断题',
    short_answer: '简答题',
    reading_comprehension: '阅读理解'
  }
  return map[type] || '简答题'
}

// 获取批改状态
const getCorrectionStatus = (question: any) => {
  if (question.gotScore === null) return null
  if (question.gotScore === question.score) return 'correct'
  return 'wrong'
}

// 判断是否为正确选项
const isCorrectOption = (question: any, optIdx: number) => {
  if (!question.correctAnswer) return false
  
  // 判断题特殊处理：选项0=正确，1=错误
  if (question.type === 'true_false') {
    const correctAnswer = question.correctAnswer.toLowerCase()
    if (optIdx === 0) {
      // 第一个选项是"正确"
      return correctAnswer === 'true' || correctAnswer === '正确'
    } else if (optIdx === 1) {
      // 第二个选项是"错误"
      return correctAnswer === 'false' || correctAnswer === '错误'
    }
    return false
  }
  
  // 单选题和多选题的原有逻辑
  const correctIdx = parseInt(question.correctAnswer)
  if (!isNaN(correctIdx) && correctIdx === optIdx) return true
  // 多选题的情况
  try {
    const correctArr = JSON.parse(question.correctAnswer || '[]')
    return Array.isArray(correctArr) && correctArr.includes(String(optIdx))
  } catch {
    // 检查是否是字母格式 (A, B, C...)
    const letter = String.fromCharCode(65 + optIdx)
    if (question.correctAnswer === letter) return true
    if (question.correctAnswer?.includes(letter)) return true
    return false
  }
}

// 判断是否为学生选错的选项
const isWrongOption = (question: any, optIdx: number) => {
  // 判断题特殊处理
  if (question.type === 'true_false') {
    return isStudentSelectedOption(question, optIdx) && !isCorrectOption(question, optIdx)
  }
  
  // 单选题和多选题的原有逻辑
  if (!question.studentAnswer) return false
  const studentIdx = parseInt(question.studentAnswer)
  if (!isNaN(studentIdx) && studentIdx === optIdx && !isCorrectOption(question, optIdx)) return true
  // 多选题的情况
  try {
    const studentArr = JSON.parse(question.studentAnswer || '[]')
    if (Array.isArray(studentArr) && studentArr.includes(String(optIdx))) {
      return !isCorrectOption(question, optIdx)
    }
    // 检查是否是字母格式
    const letter = String.fromCharCode(65 + optIdx)
    if (question.studentAnswer?.includes(letter) && !isCorrectOption(question, optIdx)) return true
    return false
  } catch {
    return false
  }
}

// 判断是否为学生选中的选项（无论对错）
const isStudentSelectedOption = (question: any, optIdx: number) => {
  if (!question.studentAnswer) return false
  
  // 判断题特殊处理：选项0=正确，1=错误
  if (question.type === 'true_false') {
    const studentAnswer = question.studentAnswer.toLowerCase()
    if (optIdx === 0) {
      // 第一个选项是"正确"
      return studentAnswer === 'true' || studentAnswer === '正确'
    } else if (optIdx === 1) {
      // 第二个选项是"错误"
      return studentAnswer === 'false' || studentAnswer === '错误'
    }
    return false
  }
  
  // 单选题和多选题的原有逻辑
  const studentIdx = parseInt(question.studentAnswer)
  if (!isNaN(studentIdx) && studentIdx === optIdx) return true
  // 多选题的情况
  try {
    const studentArr = JSON.parse(question.studentAnswer || '[]')
    if (Array.isArray(studentArr) && studentArr.includes(String(optIdx))) return true
    // 检查是否是字母格式
    const letter = String.fromCharCode(65 + optIdx)
    if (question.studentAnswer?.includes(letter)) return true
    return false
  } catch {
    return false
  }
}

// 格式化答案显示
const formatAnswer = (answer: string, type: string) => {
  if (!answer) return '未作答'
  
  // 判断题转换
  if (type === 'true_false') {
    const lowerAnswer = answer.toLowerCase()
    if (lowerAnswer === 'true') return '正确'
    if (lowerAnswer === 'false') return '错误'
    return answer
  }
  
  // 单选题：数字转字母
  if (type === 'single_choice') {
    const idx = parseInt(answer)
    if (!isNaN(idx) && idx >= 0 && idx < 26) {
      return String.fromCharCode(65 + idx)
    }
    return answer
  }
  
  // 多选题：[0,2] 转 A,C
  if (type === 'multiple_choice') {
    try {
      const arr = JSON.parse(answer)
      if (Array.isArray(arr)) {
        return arr.map((idx: string | number) => {
          const numIdx = parseInt(String(idx))
          if (!isNaN(numIdx) && numIdx >= 0 && numIdx < 26) {
            return String.fromCharCode(65 + numIdx)
          }
          return idx
        }).join(', ')
      }
    } catch {
      // 解析失败返回原值
    }
    return answer
  }
  
  return answer
}

// 选择学生
const selectStudent = async (student: any) => {
  currentStudent.value = student
  await loadSubmissionDetail(student.id)
}

// 快速判定正确/错误
const quickCorrect = (isCorrect: boolean) => {
  const currentQ = questions.value[currentQuestion.value]
  if (currentQ) {
    currentQ.isCorrect = isCorrect
    currentQ.gotScore = isCorrect ? currentQ.score : 0
    ElMessage.success(`第${currentQuestion.value + 1}题已判定为${isCorrect ? '正确' : '错误'}`)
  }
}

// 添加评语
const addComment = (comment: string) => {
  const currentQ = questions.value[currentQuestion.value]
  if (currentQ) {
    currentQ.comment = currentQ.comment ? currentQ.comment + '；' + comment : comment
  }
}

// 保存批改
const saveCorrection = async () => {
  if (scoredCount.value < questions.value.length) {
    ElMessage.warning(`还有 ${questions.value.length - scoredCount.value} 道题未评分`)
    return
  }
  
  if (!currentStudent.value) return
  
  saving.value = true
  try {
    // 获取当前学生的提交ID
    const submissions = await submissionApi.getSubmissionsByHomework(homeworkId.value)
    console.log('All submissions:', submissions)
    console.log('Current student id:', currentStudent.value.id)
    
    const currentSubmission = submissions.find((s: any) => {
      // 支持字符串和数字类型匹配
      const sid = String(s.studentId)
      const cid = String(currentStudent.value.id)
      console.log('Comparing:', sid, '===', cid, '=>', sid === cid)
      return sid === cid
    })
    console.log('Found submission:', currentSubmission)
    
    if (!currentSubmission || !currentSubmission.id) {
      ElMessage.error('未找到该学生的提交记录或提交ID为空')
      return
    }
    
    const corrections = questions.value.map(q => {
      // 根据isCorrect计算分数（如果gotScore未设置）
      let score = q.gotScore
      if (score === null || score === undefined) {
        score = q.isCorrect === true ? q.score : q.isCorrect === false ? 0 : 0
      }
      return {
        answerId: q.id,
        score: score,
        comment: q.comment || ''
      }
    })
    
    console.log('Calling correctSubmission with id:', currentSubmission.id)
    await submissionApi.correctSubmission(currentSubmission.id, corrections)
    
    currentStudent.value.status = 'corrected'
    currentStudent.value.score = calculateTotalScore.value
    ElMessage.success('批改保存成功！')
  } catch (error: any) {
    ElMessage.error('保存失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

// 批量保存
const saveBatch = async () => {
  batchSaving.value = true
  try {
    // 批量保存逻辑
    ElMessage.success('批量保存成功！')
  } catch (error: any) {
    ElMessage.error('批量保存失败: ' + error.message)
  } finally {
    batchSaving.value = false
  }
}

// AI自动批改
const autoCorrectWithAI = async () => {
  if (!currentStudent.value) {
    ElMessage.warning('请先选择学生')
    return
  }
  
  try {
    ElMessage.info('AI正在分析...')
    
    // 对每个题目调用AI批改
    for (const question of questions.value) {
      if (question.gotScore !== null) continue // 跳过已批改的题目
      
      const result = await aiApi.correct({
        question: question.content,
        studentAnswer: question.studentAnswer,
        correctAnswer: question.correctAnswer,
        questionType: question.type,
        fullScore: question.score
      })
      
    if (result) {
        question.isCorrect = result.score === question.score ? true : result.score === 0 ? false : null
        question.comment = result.analysis || ''
        question.aiAnalysis = result.analysis || ''
      }
    }
    
    ElMessage.success('AI自动批改完成！')
  } catch (error: any) {
    ElMessage.error('AI批改失败: ' + error.message)
  }
}

// 页面加载
onMounted(() => {
  loadHomeworkInfo()
  loadSubmissions()
  
  // 监听AI批改完成通知，自动刷新
  if (homeworkId.value) {
    correctionWebSocket.on('CORRECTION_COMPLETE_TEACHER', (data: any) => {
      if (data.homeworkId === homeworkId.value) {
        console.log('收到AI批改完成通知，自动刷新:', data)
        ElMessage.success('学生作业AI批改完成，自动刷新数据')
        // 刷新学生列表
        loadSubmissions()
        // 如果当前有选中的学生，刷新该学生的详情
        if (currentStudent.value) {
          loadSubmissionDetail(currentStudent.value.id)
        }
      }
    })
  }
})
</script>
