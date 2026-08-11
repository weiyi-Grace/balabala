<template>
  <div class="max-w-5xl mx-auto animate-fade-in">
    <!-- 顶部信息栏 -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 mb-6">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <el-button circle text @click="router.back()">
            <el-icon :size="20"><ArrowLeft /></el-icon>
          </el-button>
          <div>
            <h2 class="text-xl font-bold text-gray-800">{{ homeworkTitle }}</h2>
            <p class="text-sm text-gray-500 mt-1">{{ homeworkSubject }} | 截止时间：{{ homeworkDeadline }}</p>
          </div>
        </div>
        <div class="flex items-center gap-4">
          <div class="text-center px-4 py-2 bg-orange-50 rounded-xl">
            <div class="text-xs text-orange-500">已用时间</div>
            <div class="text-lg font-bold text-orange-600 font-mono">{{ elapsedTime }}</div>
          </div>
          <el-button type="primary" :loading="submitting" @click="submit">提交作业</el-button>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-6">
      <!-- 左侧题目导航 -->
      <div class="col-span-2">
        <div class="bg-white rounded-2xl p-4 shadow-sm border border-gray-100 sticky top-4">
          <h3 class="font-bold text-sm mb-4">题目导航</h3>
          <div class="grid grid-cols-3 gap-2">
            <div v-for="(q, idx) in questions" :key="q.id"
                 class="w-10 h-10 rounded-lg flex items-center justify-center text-sm font-bold cursor-pointer transition-all"
                 :class="getQuestionStatusClass(idx)"
                 @click="scrollToQuestion(idx)">
              {{ idx + 1 }}
            </div>
          </div>
          <div class="mt-4 pt-4 border-t border-gray-100">
            <div class="flex items-center gap-2 text-xs mb-2">
              <div class="w-3 h-3 rounded bg-primary-500"></div>
              <span class="text-gray-600">已完成</span>
            </div>
            <div class="flex items-center gap-2 text-xs mb-2">
              <div class="w-3 h-3 rounded bg-gray-200"></div>
              <span class="text-gray-600">未完成</span>
            </div>
            <div class="flex items-center gap-2 text-xs">
              <div class="w-3 h-3 rounded bg-orange-100 border border-orange-300"></div>
              <span class="text-gray-600">当前</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间题目区域 -->
      <div class="col-span-7 space-y-6">
        <div v-for="(q, idx) in questions" :key="q.id" 
             :id="`question-${idx}`"
             class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 scroll-mt-4"
             :class="{ 'border-primary-300 ring-2 ring-primary-100': currentQuestion === idx }">
          <div class="flex items-center gap-3 mb-4">
            <span class="w-10 h-10 rounded-xl bg-primary-100 text-primary-700 flex items-center justify-center font-bold text-lg">
              {{ idx + 1 }}
            </span>
            <span class="text-sm text-gray-500">{{ 
            q.type === 'single_choice' ? '单选题' : 
            q.type === 'multiple_choice' ? '多选题' : 
            q.type === 'fill_blank' ? '填空题' : 
            q.type === 'true_false' ? '判断题' : 
            q.type === 'reading_comprehension' ? '阅读理解' : '简答题'
          }} | {{ q.score }}分</span>
            <el-tag v-if="q.type === 'fill_blank' ? (Array.isArray(q.blankAnswers) && q.blankAnswers.some((a: any) => a && String(a).trim())) : !!q.answer" type="success" size="small" effect="light" round class="ml-auto">已完成</el-tag>
            <el-tag v-else type="info" size="small" effect="light" round class="ml-auto">未完成</el-tag>
          </div>
          
          <p class="text-gray-800 font-medium mb-4 text-lg leading-relaxed">{{ q.content }}</p>
          
          <!-- 单选题 -->
          <template v-if="q.type === 'single_choice'">
            <el-radio-group v-model="q.answer" class="flex flex-col gap-3 w-full">
              <el-radio v-for="(opt, optIdx) in q.options" :key="optIdx" :label="String(optIdx)" 
                style="width: 100%;"
                class="!h-auto !min-h-[48px] !py-3 !px-4 !rounded-xl !border !border-gray-200 hover:!border-primary-300 hover:!bg-primary-50 transition-all cursor-pointer"
                :class="{ '!border-primary-500 !bg-primary-50': q.answer === String(optIdx) }">
                <div class="flex items-start gap-3 w-full min-w-0">
                  <span class="w-8 h-8 rounded-lg bg-gray-100 text-gray-700 flex items-center justify-center font-bold text-sm shrink-0 mt-0.5"
                    :class="{ 'bg-primary-500 text-white': q.answer === String(optIdx) }">
                    {{ ['A', 'B', 'C', 'D', 'E', 'F'][optIdx] }}
                  </span>
                  <span class="text-gray-700 leading-relaxed flex-1 break-all">{{ opt }}</span>
                </div>
              </el-radio>
            </el-radio-group>
          </template>
          
          <!-- 多选题 -->
          <template v-else-if="q.type === 'multiple_choice'">
            <el-checkbox-group v-model="q.answer" class="flex flex-col gap-3 w-full">
              <el-checkbox v-for="(opt, optIdx) in q.options" :key="optIdx" :label="String(optIdx)" 
                style="width: 100%;"
                class="!h-auto !min-h-[48px] !py-3 !px-4 !rounded-xl !border !border-gray-200 hover:!border-primary-300 hover:!bg-primary-50 transition-all cursor-pointer"
                :class="{ '!border-primary-500 !bg-primary-50': q.answer?.includes(String(optIdx)) }">
                <div class="flex items-start gap-3 w-full min-w-0">
                  <span class="w-8 h-8 rounded-lg bg-gray-100 text-gray-700 flex items-center justify-center font-bold text-sm shrink-0 mt-0.5"
                    :class="{ 'bg-primary-500 text-white': q.answer?.includes(String(optIdx)) }">
                    {{ ['A', 'B', 'C', 'D', 'E', 'F'][optIdx] }}
                  </span>
                  <span class="text-gray-700 leading-relaxed flex-1 break-all">{{ opt }}</span>
                </div>
              </el-checkbox>
            </el-checkbox-group>
            <p class="text-xs text-gray-400 mt-3 flex items-center gap-1">
              <el-icon><InfoFilled /></el-icon>
              此题为多选题，请选择所有正确答案
            </p>
          </template>
          
          <!-- 填空题 -->
          <template v-else-if="q.type === 'fill_blank'">
            <div class="space-y-3">
              <div v-for="bIdx in (q.blankCount || 1)" :key="bIdx" class="flex items-center gap-3">
                <span class="text-gray-500 font-medium">({{ bIdx }})</span>
                <el-input v-model="q.blankAnswers[bIdx-1]" placeholder="请输入答案" class="flex-1" />
              </div>
            </div>
          </template>
          
          <!-- 判断题 -->
          <template v-else-if="q.type === 'true_false'">
            <el-radio-group v-model="q.answer" class="grid grid-cols-2 gap-3">
              <el-radio label="true" 
                class="!h-auto !min-h-[48px] !py-3 !px-4 !rounded-xl !border !border-gray-200 hover:!border-primary-300 hover:!bg-primary-50 transition-all cursor-pointer"
                :class="{ '!border-primary-500 !bg-primary-50': q.answer === 'true' }">
                <div class="flex items-center gap-3">
                  <span class="w-8 h-8 rounded-lg bg-gray-100 text-gray-700 flex items-center justify-center font-bold text-sm shrink-0"
                    :class="{ 'bg-green-500 text-white': q.answer === 'true' }">
                    A
                  </span>
                  <span class="text-gray-700">正确</span>
                </div>
              </el-radio>
              <el-radio label="false" 
                class="!h-auto !min-h-[48px] !py-3 !px-4 !rounded-xl !border !border-gray-200 hover:!border-primary-300 hover:!bg-primary-50 transition-all cursor-pointer"
                :class="{ '!border-primary-500 !bg-primary-50': q.answer === 'false' }">
                <div class="flex items-center gap-3">
                  <span class="w-8 h-8 rounded-lg bg-gray-100 text-gray-700 flex items-center justify-center font-bold text-sm shrink-0"
                    :class="{ 'bg-red-500 text-white': q.answer === 'false' }">
                    B
                  </span>
                  <span class="text-gray-700">错误</span>
                </div>
              </el-radio>
            </el-radio-group>
          </template>
          
          <!-- 阅读理解题 -->
          <template v-else-if="q.type === 'reading_comprehension'">
            <div class="bg-gray-50 rounded-xl p-4 mb-4">
              <h4 class="font-medium text-gray-700 mb-2">阅读材料</h4>
              <p class="text-gray-600 leading-relaxed">{{ q.readingMaterial }}</p>
            </div>
            <div v-for="(subQ, sqIdx) in (q.subQuestions || [])" :key="sqIdx" class="border-t border-gray-100 pt-4 mt-4">
              <p class="text-gray-700 mb-3">{{ sqIdx + 1 }}. {{ (subQ as any).questionContent || (subQ as any).content }}</p>
              <template v-if="(subQ as any).questionType === 'single_choice' || (subQ as any).type === 'single_choice'">
                <el-radio-group v-model="(subQ as any).answer" class="flex flex-col gap-2">
                  <el-radio v-for="(opt, optIdx) in ((subQ as any).options || [])" :key="optIdx" :label="String(optIdx)">
                    <span class="font-bold mr-1">{{ ['A', 'B', 'C', 'D'][optIdx as number] }}.</span> {{ opt }}
                  </el-radio>
                </el-radio-group>
              </template>
              <template v-else>
                <el-input v-model="(subQ as any).answer" type="textarea" :rows="3" placeholder="请输入答案" />
              </template>
            </div>
          </template>
          
          <!-- 简答题（主观题） -->
          <template v-else>
            <el-input 
              v-model="q.answer" 
              type="textarea" 
              :rows="6" 
              placeholder="请输入你的答案...（支持 LaTeX 公式）"
              class="!font-mono"
              resize="vertical"
            />
            <div class="mt-2 text-xs text-gray-400 flex items-center gap-2">
              <el-icon><InfoFilled /></el-icon>
              <span>支持 Markdown 格式，可输入数学公式如：$x^2 + 2x + 1 = 0$</span>
            </div>
          </template>
        </div>

        <!-- 文件上传 -->
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h3 class="font-bold mb-4 flex items-center gap-2">
            <el-icon><Upload /></el-icon>
            附件上传
          </h3>
          <el-upload
            drag
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :file-list="fileList"
            multiple
            class="w-full"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 jpg/png/pdf/word 格式，单个文件不超过 10MB
              </div>
            </template>
          </el-upload>
        </div>
      </div>

      <!-- 右侧操作区 -->
      <div class="col-span-3">
        <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 sticky top-4">
          <h3 class="font-bold text-lg mb-4">答题进度</h3>
          
          <div class="mb-4">
            <div class="flex items-center gap-3 mb-2">
              <el-progress :percentage="progressPercentage" :stroke-width="10" :color="progressColor" class="flex-1" />
              <span class="text-lg font-bold text-gray-700 w-12 text-right">{{ progressPercentage }}%</span>
            </div>
            <div class="text-center text-sm text-gray-600">
              已完成 {{ completedCount }} / {{ questions.length }} 题
            </div>
          </div>
          
          <el-divider class="my-4" />
          
          <div class="space-y-3 w-full">
            <el-button type="success" style="width: 100%; height: 44px; margin: 0;" :loading="submitting" @click="submit">
              <el-icon class="mr-1"><Check /></el-icon>
              <span class="text-base">提交作业</span>
            </el-button>
            
            <el-button style="width: 100%; height: 44px; margin: 0;" @click="saveDraft">
              <el-icon class="mr-1"><Document /></el-icon>
              <span class="text-base">保存草稿</span>
            </el-button>
            
            <el-button style="width: 100%; height: 44px; margin: 0;" text @click="preview">
              <el-icon class="mr-1"><View /></el-icon>
              <span>预览答案</span>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Timer, Document, Upload, Picture, Check } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { homeworkApi, submissionApi, userApi } from '@/api'
import { useUserStore } from '@/stores'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const homeworkId = computed(() => Number(route.params.id))

const submitting = ref(false)
const currentQuestion = ref(0)
const elapsedTime = ref('00:00:00')
const timerInterval = ref<number | null>(null)
const homeworkTitle = ref('')
const homeworkDeadline = ref('')
const homeworkSubject = ref('')
const homeworkAiEnabled = ref(true) // 作业AI批改设置

let seconds = 0

const startTimer = () => {
  timerInterval.value = window.setInterval(() => {
    seconds++
    const h = Math.floor(seconds / 3600).toString().padStart(2, '0')
    const m = Math.floor((seconds % 3600) / 60).toString().padStart(2, '0')
    const s = (seconds % 60).toString().padStart(2, '0')
    elapsedTime.value = `${h}:${m}:${s}`
  }, 1000)
}

const stopTimer = () => {
  if (timerInterval.value) {
    clearInterval(timerInterval.value)
    timerInterval.value = null
  }
}

// 题目数据
const questions = ref<any[]>([])

// 加载作业详情
const loadHomework = async () => {
  try {
    const homework = await homeworkApi.getHomeworkById(homeworkId.value)
    console.log('作业数据:', homework)
    homeworkTitle.value = homework.title
    homeworkDeadline.value = homework.deadline?.replace('T', ' ')
    homeworkSubject.value = homework.subject
    homeworkAiEnabled.value = homework.aiEnabled !== false // 默认为true
    
    // 如果有草稿数据，跳过初始化
    const draft = localStorage.getItem(`homework_draft_${homeworkId.value}`)
    if (draft) {
      console.log('有草稿数据，跳过初始化')
      // 即使有草稿，也需要根据题干重新计算 blankCount，避免旧草稿导致输入框数量不对
      try {
        const saved = JSON.parse(draft)
        if (Array.isArray(saved)) {
          const byId = new Map<number, any>()
          for (const q of homework.questions || []) {
            if (q && q.id != null) byId.set(Number(q.id), q)
          }

          questions.value = saved.map((q: any) => {
            const serverQ = byId.get(Number(q.id))
            const contentStr = String((serverQ?.content ?? q.content) || '')

            let blankCount = q.blankCount ?? serverQ?.blankCount
            if (!blankCount && q.type === 'fill_blank') {
              const multiMatches = contentStr.match(/_{2,}/g)
              const multiFullWidthUnderscore = contentStr.match(/＿{2,}/g)
              const multiEmDash = contentStr.match(/—{2,}/g)
              const multiHyphen = contentStr.match(/-{2,}/g)
              const groups = [
                ...(multiMatches || []),
                ...(multiFullWidthUnderscore || []),
                ...(multiEmDash || []),
                ...(multiHyphen || [])
              ]
              if (groups.length > 0) {
                blankCount = groups.length
              } else {
                const singleMatches = contentStr.match(/[_＿]/g)
                blankCount = singleMatches ? singleMatches.length : 1
              }
            }

            let blankAnswers = q.blankAnswers
            if (q.type === 'fill_blank') {
              if (!Array.isArray(blankAnswers)) blankAnswers = []
              const bc = Number(blankCount || 1)
              if (blankAnswers.length < bc) {
                blankAnswers = [...blankAnswers, ...new Array(bc - blankAnswers.length).fill('')]
              } else if (blankAnswers.length > bc) {
                blankAnswers = blankAnswers.slice(0, bc)
              }
            } else {
              blankAnswers = undefined
            }

            return {
              ...(serverQ || {}),
              ...q,
              content: serverQ?.content ?? q.content,
              blankCount,
              blankAnswers
            }
          })
        }
      } catch {}
      return
    }
    
    // 初始化题目数据
    questions.value = (homework.questions || []).map((q: any) => {
      let blankCount = q.blankCount
      // 如果没有blankCount，从content中解析空的数量
      if (!blankCount && q.type === 'fill_blank') {
        const contentStr = String(q.content || '')
        // 优先匹配连续下划线（"__"及以上），视为一个空
        const multiMatches = contentStr.match(/_{2,}/g)
        // 支持全角下划线（＿）与中文破折号（—）连续出现表示空
        const multiFullWidthUnderscore = contentStr.match(/＿{2,}/g)
        const multiEmDash = contentStr.match(/—{2,}/g)
        const multiHyphen = contentStr.match(/-{2,}/g)

        const groups = [
          ...(multiMatches || []),
          ...(multiFullWidthUnderscore || []),
          ...(multiEmDash || []),
          ...(multiHyphen || [])
        ]

        if (groups.length > 0) {
          blankCount = groups.length
        } else {
          // 兜底：如果题干用单个下划线表示空，则按单下划线数量
          const singleMatches = contentStr.match(/[_＿]/g)
          blankCount = singleMatches ? singleMatches.length : 1
        }
      }
      return {
        ...q,
        blankCount,
        answer: q.type === 'multiple_choice' ? [] : (q.answer || ''),
        blankAnswers: q.type === 'fill_blank' ? new Array(blankCount || 1).fill('') : undefined
      }
    })
    console.log('题目列表:', questions.value)
  } catch (error: any) {
    ElMessage.error('加载作业失败: ' + error.message)
  }
}

// 进度计算
const completedCount = computed(() => questions.value.filter(q => {
  if (q.type === 'multiple_choice') {
    return q.answer && q.answer.length > 0
  }
  if (q.type === 'fill_blank') {
    return Array.isArray(q.blankAnswers) && q.blankAnswers.some((a: string) => a && a.trim())
  }
  return q.answer && q.answer.trim()
}).length)
const progressPercentage = computed(() => Math.round((completedCount.value / questions.value.length) * 100))
const progressColor = computed(() => {
  if (progressPercentage.value < 30) return '#ef4444'
  if (progressPercentage.value < 70) return '#f59e0b'
  return '#22c55e'
})

const getQuestionStatusClass = (idx: number) => {
  const q = questions.value[idx]
  if (!q) return 'bg-gray-100 text-gray-600 hover:bg-gray-200'
  
  // 先检查是否完成（优先级高于当前题目高亮）
  let isCompleted = false
  if (q.type === 'multiple_choice') {
    isCompleted = q.answer && q.answer.length > 0
  } else if (q.type === 'fill_blank') {
    isCompleted = Array.isArray(q.blankAnswers) && q.blankAnswers.some((a: string) => a && a.trim())
  } else {
    isCompleted = q.answer && q.answer.trim()
  }
  
  // 已完成显示绿色，未完成再检查是否是当前题目
  if (isCompleted) return 'bg-primary-500 text-white'
  if (currentQuestion.value === idx) return 'bg-orange-100 text-orange-700 border-2 border-orange-300'
  return 'bg-gray-100 text-gray-600 hover:bg-gray-200'
}

const scrollToQuestion = (idx: number) => {
  currentQuestion.value = idx
  const el = document.getElementById(`question-${idx}`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

const handleFileChange = (file: any) => {
  ElMessage.success(`文件 ${file.name} 已添加`)
}

const saveDraft = () => {
  localStorage.setItem(`homework_draft_${homeworkId.value}`, JSON.stringify(questions.value))
  ElMessage.success('草稿已保存')
}

const preview = () => {
  ElMessageBox.alert(
    questions.value.map((q, idx) => `
      <div style="margin-bottom: 16px;">
        <strong>第${idx + 1}题：</strong>${q.answer || '未作答'}
      </div>
    `).join(''),
    '答案预览',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

const submit = async () => {
  if (completedCount.value < questions.value.length) {
    try {
      await ElMessageBox.confirm(
        `还有 ${questions.value.length - completedCount.value} 道题未完成，确定要提交吗？`,
        '确认提交',
        {
          confirmButtonText: '确定提交',
          cancelButtonText: '继续答题',
          type: 'warning'
        }
      )
    } catch {
      return
    }
  }
  
  if (!userStore.user?.id) {
    ElMessage.error('用户信息不完整，请重新登录')
    return
  }
  
  submitting.value = true
  stopTimer()
  
  try {
    // 构建提交数据
    const answers = questions.value.map(q => {
      let answerContent = ''
      if (q.type === 'single_choice') {
        // 单选题：数字索引转字母
        const idx = parseInt(q.answer)
        if (!isNaN(idx) && idx >= 0 && idx < 26) {
          answerContent = String.fromCharCode(65 + idx) // A, B, C...
        } else {
          answerContent = q.answer || ''
        }
      } else if (q.type === 'multiple_choice') {
        // 多选题：数字索引数组转字母数组
        if (Array.isArray(q.answer)) {
          answerContent = JSON.stringify(q.answer.map((i: string) => {
            const idx = parseInt(i)
            return !isNaN(idx) && idx >= 0 && idx < 26 ? String.fromCharCode(65 + idx) : i
          }))
        } else {
          answerContent = JSON.stringify(q.answer)
        }
      } else if (q.type === 'fill_blank') {
        answerContent = JSON.stringify(q.blankAnswers || [])
      } else {
        answerContent = q.answer || ''
      }
      return {
        questionId: q.id,
        content: answerContent,
        answerTime: seconds
      }
    })
    
    await submissionApi.submitHomework({
      homeworkId: homeworkId.value,
      studentId: userStore.user.id,
      answers,
      totalTime: seconds
    })
    
    // 清除草稿
    localStorage.removeItem(`homework_draft_${homeworkId.value}`)
    
    // 根据AI批改设置显示不同提示
    if (homeworkAiEnabled.value) {
      ElMessage.success('作业提交成功！AI 正在后台批改中，请稍后查看结果...')
    } else {
      ElMessage.success('作业提交成功！请等待教师批改...')
    }
    router.push('/student/homework')
  } catch (error: any) {
    ElMessage.error('提交失败: ' + error.message)
    submitting.value = false
  }
}

// 恢复草稿
onMounted(() => {
  loadHomework()
  startTimer()
  const draft = localStorage.getItem(`homework_draft_${homeworkId.value}`)
  if (draft) {
    try {
      const saved = JSON.parse(draft)
      questions.value = saved
    } catch {}
  }
})

onUnmounted(() => {
  stopTimer()
})
</script>
