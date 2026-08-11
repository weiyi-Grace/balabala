<template>
  <div class="space-y-6 animate-fade-in">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-4 gap-4">
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-sm text-gray-500">总错题数</div>
            <div class="text-2xl font-bold text-gray-800">{{ stats.total }}</div>
          </div>
          <div class="w-12 h-12 bg-red-100 rounded-xl flex items-center justify-center">
            <el-icon :size="24" class="text-red-500"><DocumentDelete /></el-icon>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-sm text-gray-500">已掌握</div>
            <div class="text-2xl font-bold text-green-600">{{ stats.mastered }}</div>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center">
            <el-icon :size="24" class="text-green-500"><CircleCheck /></el-icon>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-sm text-gray-500">复习中</div>
            <div class="text-2xl font-bold text-orange-600">{{ stats.learning }}</div>
          </div>
          <div class="w-12 h-12 bg-orange-100 rounded-xl flex items-center justify-center">
            <el-icon :size="24" class="text-orange-500"><Reading /></el-icon>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-sm text-gray-500">掌握率</div>
            <div class="text-2xl font-bold text-primary-600">{{ stats.masteryRate }}%</div>
          </div>
          <div class="w-12 h-12 bg-primary-100 rounded-xl flex items-center justify-center">
            <el-icon :size="24" class="text-primary-500"><TrendCharts /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-6 items-start">
      <!-- 左侧筛选 -->
      <div class="col-span-3">
        <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 sticky top-4">
          <h3 class="font-bold text-lg mb-4">筛选条件</h3>
          
          <div class="space-y-5">
            <div>
              <label class="text-sm text-gray-500 mb-2 block font-medium">掌握状态</label>
              <el-radio-group v-model="filterMastery" class="flex flex-col gap-2 items-start">
                <el-radio label="all">全部</el-radio>
                <el-radio label="mastered">已掌握</el-radio>
                <el-radio label="learning">复习中</el-radio>
                <el-radio label="weak">待掌握</el-radio>
              </el-radio-group>
            </div>
            
            <div>
              <label class="text-sm text-gray-500 mb-2 block font-medium">学科</label>
              <div class="text-sm text-gray-700 py-1">语文</div>
            </div>
            
            <div>
              <label class="text-sm text-gray-500 mb-2 block font-medium">知识点</label>
              <el-select v-model="filterKnowledge" placeholder="选择知识点" clearable class="w-full">
                <el-option label="全部知识点" value="" />
                <el-option v-for="kp in knowledgePoints" :key="kp" :label="kp" :value="kp" />
              </el-select>
            </div>
          </div>
          
          <el-divider class="my-4" />
          
          <el-button type="primary" class="w-full" @click="startReview">
            <el-icon class="mr-1"><VideoPlay /></el-icon>
            开始复习模式
          </el-button>
        </div>
      </div>

      <!-- 右侧错题列表 -->
      <div class="col-span-9">
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-xl font-bold">错题列表</h2>
            <div class="flex gap-2">
              <el-input v-model="searchQuery" placeholder="搜索错题..." clearable style="width: 200px">
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
              <el-dropdown @command="handleBatchAction">
                <el-button>
                  批量操作<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="master">标记已掌握</el-dropdown-item>
                    <el-dropdown-item command="review">加入复习计划</el-dropdown-item>
                    <el-dropdown-item command="export">导出错题</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <div class="space-y-4">
            <!-- 空状态 -->
            <div v-if="filteredErrors.length === 0" class="flex flex-col items-center justify-center py-16">
              <el-icon class="text-6xl text-gray-300 mb-4"><DocumentDelete /></el-icon>
              <div class="text-gray-500 text-lg">暂无错题记录</div>
              <div class="text-gray-400 text-sm mt-1">继续努力，减少错题数量</div>
              <el-button type="primary" class="mt-4" @click="$router.push('/student/homework')">
                <el-icon class="mr-1"><EditPen /></el-icon>去做作业
              </el-button>
            </div>
            
            <div v-for="err in paginatedErrors" :key="err.id" 
                 class="p-5 border border-gray-100 rounded-xl hover:border-primary-200 hover:shadow-md transition-all"
                 :class="{ 'bg-primary-50/50': selectedErrors.includes(err.id) }">
              <div class="flex items-start gap-4">
                <el-checkbox v-model="selectedErrors" :label="err.id" class="mt-1">&nbsp;</el-checkbox>
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-3">
                    <el-tag :type="getMasteryType(err.mastery)" effect="light" round>
                      {{ getMasteryText(err.mastery) }}
                    </el-tag>
                    <el-tag type="info" effect="light" round size="small">{{ err.subject }}</el-tag>
                    <el-tag type="primary" effect="light" round size="small">{{ getQuestionTypeLabel(err.questionType) }}</el-tag>
                    <el-tag v-if="err.knowledgePoint" type="warning" effect="light" round size="small">{{ err.knowledgePoint }}</el-tag>
                    <el-tag v-if="err.difficulty" :type="err.difficulty === 'easy' ? 'success' : err.difficulty === 'hard' ? 'danger' : 'warning'" effect="light" round size="small">{{ getDifficultyLabel(err.difficulty) }}</el-tag>
                    <span class="text-xs text-gray-400 ml-auto">{{ err.date }}</span>
                  </div>
                  
                  <p class="font-medium text-gray-800 mb-4">{{ err.question }}</p>
                  
                  <!-- 题目选项（选择题） -->
                  <div v-if="err.options && err.options.length > 0 && (err.questionType === 'single_choice' || err.questionType === 'multiple_choice')" class="mb-4 space-y-2">
                    <div v-for="(option, idx) in err.options" :key="idx" 
                         class="flex items-center gap-2 p-2 rounded-lg"
                         :class="{
                           'bg-green-100 border border-green-300': getOptionLetter(Number(idx)) === err.correctAnswer,
                           'bg-red-100 border border-red-300': getOptionLetter(Number(idx)) === err.myAnswer && err.myAnswer !== err.correctAnswer,
                           'bg-gray-50 border border-gray-200': getOptionLetter(Number(idx)) !== err.correctAnswer && getOptionLetter(Number(idx)) !== err.myAnswer
                         }">
                      <span class="w-6 h-6 rounded-full flex items-center justify-center text-sm font-bold"
                            :class="{
                              'bg-green-500 text-white': getOptionLetter(Number(idx)) === err.correctAnswer,
                              'bg-red-500 text-white': getOptionLetter(Number(idx)) === err.myAnswer && err.myAnswer !== err.correctAnswer,
                              'bg-gray-300 text-gray-600': getOptionLetter(Number(idx)) !== err.correctAnswer && getOptionLetter(Number(idx)) !== err.myAnswer
                            }">
                        {{ getOptionLetter(Number(idx)) }}
                      </span>
                      <span class="text-sm" :class="{
                        'text-green-700': getOptionLetter(Number(idx)) === err.correctAnswer,
                        'text-red-700': getOptionLetter(Number(idx)) === err.myAnswer && err.myAnswer !== err.correctAnswer
                      }">{{ option }}</span>
                      <el-icon v-if="getOptionLetter(Number(idx)) === err.correctAnswer" class="ml-auto text-green-500"><Check /></el-icon>
                      <el-icon v-if="getOptionLetter(Number(idx)) === err.myAnswer && err.myAnswer !== err.correctAnswer" class="ml-auto text-red-500"><Close /></el-icon>
                    </div>
                  </div>
                  
                  <!-- 判断题选项 -->
                  <div v-if="err.questionType === 'true_false'" class="mb-4 flex gap-4">
                    <div class="flex items-center gap-2 px-4 py-2 rounded-lg border"
                         :class="err.correctAnswer === '正确' ? 'bg-green-100 border-green-300' : 'bg-gray-50 border-gray-200'">
                      <span class="text-sm font-medium" :class="err.correctAnswer === '正确' ? 'text-green-700' : ''">正确</span>
                      <el-icon v-if="err.correctAnswer === '正确'" class="text-green-500"><Check /></el-icon>
                    </div>
                    <div class="flex items-center gap-2 px-4 py-2 rounded-lg border"
                         :class="err.correctAnswer === '错误' ? 'bg-green-100 border-green-300' : 'bg-gray-50 border-gray-200'">
                      <span class="text-sm font-medium" :class="err.correctAnswer === '错误' ? 'text-green-700' : ''">错误</span>
                      <el-icon v-if="err.correctAnswer === '错误'" class="text-green-500"><Check /></el-icon>
                    </div>
                  </div>
                  
                  <!-- 错误分析 -->
                  <div class="grid grid-cols-2 gap-4 mb-4">
                    <div class="bg-red-50 rounded-xl p-4 border border-red-100">
                      <div class="text-xs text-red-500 mb-2 flex items-center gap-1">
                        <el-icon><Close /></el-icon>我的答案
                      </div>
                      <div class="text-sm text-gray-700">{{ err.myAnswer }}</div>
                      <div v-if="err.errorReason" class="mt-2 text-xs text-red-600 bg-red-100 rounded px-2 py-1 inline-block">
                        错误原因：{{ err.errorReason }}
                      </div>
                    </div>
                    <div class="bg-green-50 rounded-xl p-4 border border-green-100">
                      <div class="text-xs text-green-600 mb-2 flex items-center gap-1">
                        <el-icon><Check /></el-icon>正确答案
                      </div>
                      <div class="text-sm text-gray-700">{{ err.correctAnswer }}</div>
                    </div>
                  </div>
                  
                  <!-- AI 分析 -->
                  <div v-if="err.aiAnalysis" class="mb-4 p-3 bg-blue-50 rounded-lg border border-blue-100">
                    <div class="flex items-center gap-2 text-xs text-blue-600 mb-1">
                      <el-icon><Cpu /></el-icon>
                      <span>BYG AI 分析</span>
                    </div>
                    <p class="text-sm text-gray-600">{{ err.aiAnalysis }}</p>
                  </div>
                  
                  <div class="flex gap-2">
                    <el-button type="primary" size="small" plain round @click="openAIChat(err)">
                      <el-icon class="mr-1"><ChatDotRound /></el-icon>AI答疑
                    </el-button>
                    <el-button type="primary" size="small" plain round @click="reviewError(err)">
                      <el-icon class="mr-1"><Reading /></el-icon>复习
                    </el-button>
                    <el-button v-if="err.mastery !== 2" type="success" size="small" plain round @click="markMastered(err.id)">
                      <el-icon class="mr-1"><CircleCheck /></el-icon>标记掌握
                    </el-button>
                    <el-button type="danger" size="small" text @click="deleteError(err.id)">
                      <el-icon><Delete /></el-icon>删除
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div class="flex justify-center mt-6">
            <el-pagination 
              v-model:current-page="currentPage" 
              v-model:page-size="pageSize"
              :page-sizes="[10, 20, 50]"
              :total="filteredErrors.length" 
              background 
              layout="total, sizes, prev, pager, next"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- AI答疑对话框 -->
  <el-dialog v-model="aiChatVisible" title="AI智能答疑" width="600px" :close-on-click-modal="false">
    <div v-if="currentError" class="mb-4 p-4 bg-gray-50 rounded-lg">
      <div class="text-sm text-gray-500 mb-1">题目</div>
      <div class="font-medium text-gray-800">{{ currentError.question }}</div>
      <div class="mt-2 flex gap-4 text-sm">
        <span class="text-red-500">我的答案: {{ currentError.myAnswer }}</span>
        <span class="text-green-600">正确答案: {{ currentError.correctAnswer }}</span>
      </div>
    </div>
    
    <!-- 聊天记录 -->
    <div class="h-80 overflow-y-auto border rounded-lg p-4 mb-4 bg-white">
      <div v-if="chatMessages.length === 0" class="text-center text-gray-400 mt-20">
        <el-icon :size="48" class="mb-2"><Cpu /></el-icon>
        <p>点击发送开始AI答疑</p>
      </div>
      <div v-for="(msg, idx) in chatMessages" :key="idx" class="mb-4">
        <div v-if="msg.role === 'user'" class="flex justify-end">
          <div class="max-w-[80%] bg-primary-100 rounded-lg p-3 text-sm">
            {{ msg.content }}
          </div>
        </div>
        <div v-else class="flex justify-start">
          <div class="max-w-[80%] bg-gray-100 rounded-lg p-3 text-sm">
            <div class="flex items-center gap-1 text-blue-600 mb-1">
              <el-icon><Cpu /></el-icon>
              <span class="text-xs">BYG AI</span>
            </div>
            {{ msg.content }}
          </div>
        </div>
      </div>
    </div>
    
    <!-- 输入框 -->
    <div class="flex gap-2">
      <el-input v-model="chatInput" placeholder="输入你的问题..." @keyup.enter="sendChatMessage" :disabled="chatLoading" />
      <el-button type="primary" :loading="chatLoading" :disabled="!chatInput.trim()" @click="sendChatMessage">
        发送
      </el-button>
    </div>
    
    <!-- 快捷问题 -->
    <div class="mt-3 flex flex-wrap gap-2">
      <el-tag v-for="q in quickQuestions" :key="q" size="small" class="cursor-pointer hover:bg-primary-100" @click="chatInput = q">
        {{ q }}
      </el-tag>
    </div>
  </el-dialog>

  <!-- 复习模式对话框 -->
  <el-dialog 
    v-model="reviewMode" 
    title="错题复习模式" 
    width="800px" 
    :close-on-click-modal="false"
    :show-close="false"
    :close-on-press-escape="false"
    class="review-dialog"
  >
    <div v-if="currentReviewError" class="review-content">
      <!-- 进度条 -->
      <div class="flex items-center justify-between mb-4">
        <span class="text-sm text-gray-500">进度: {{ currentReviewIndex + 1 }} / {{ reviewList.length }}</span>
        <el-progress :percentage="Math.round(((currentReviewIndex + 1) / reviewList.length) * 100)" style="width: 200px" />
        <el-button size="small" text @click="exitReviewMode">
          <el-icon><Close /></el-icon>退出
        </el-button>
      </div>

      <!-- 题目信息 -->
      <div class="mb-4">
        <div class="flex items-center gap-2 mb-2">
          <el-tag type="info" effect="light" round size="small">{{ currentReviewError.subject }}</el-tag>
          <el-tag type="primary" effect="light" round size="small">{{ getQuestionTypeLabel(currentReviewError.questionType) }}</el-tag>
          <el-tag v-if="currentReviewError.knowledgePoint" type="warning" effect="light" round size="small">{{ currentReviewError.knowledgePoint }}</el-tag>
        </div>
        <p class="font-medium text-lg text-gray-800 mb-4">{{ currentReviewError.question }}</p>
        
        <!-- 选择题选项 -->
        <div v-if="currentReviewError.options && currentReviewError.options.length > 0 && 
                    (currentReviewError.questionType === 'single_choice' || currentReviewError.questionType === 'multiple_choice')" 
             class="space-y-2 mb-4">
          <div v-for="(option, idx) in currentReviewError.options" :key="idx" 
               class="flex items-center gap-3 p-3 rounded-lg border cursor-pointer transition-all"
               :class="{
                 'border-primary-500 bg-primary-50': reviewAnswer === getOptionLetter(Number(idx)),
                 'border-gray-200 hover:border-primary-300': reviewAnswer !== getOptionLetter(Number(idx)),
                 'border-green-500 bg-green-50': showReviewResult && getOptionLetter(Number(idx)) === currentReviewError.correctAnswer,
                 'border-red-500 bg-red-50': showReviewResult && reviewAnswer === getOptionLetter(Number(idx)) && getOptionLetter(Number(idx)) !== currentReviewError.correctAnswer
               }"
               @click="!showReviewResult && (reviewAnswer = getOptionLetter(Number(idx)))">
            <span class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold"
                  :class="{
                    'bg-primary-500 text-white': reviewAnswer === getOptionLetter(Number(idx)) && !showReviewResult,
                    'bg-gray-200 text-gray-700': reviewAnswer !== getOptionLetter(Number(idx)) && !showReviewResult,
                    'bg-green-500 text-white': showReviewResult && getOptionLetter(Number(idx)) === currentReviewError.correctAnswer,
                    'bg-red-500 text-white': showReviewResult && reviewAnswer === getOptionLetter(Number(idx)) && getOptionLetter(Number(idx)) !== currentReviewError.correctAnswer,
                    'bg-gray-200 text-gray-700': showReviewResult && reviewAnswer !== getOptionLetter(Number(idx)) && getOptionLetter(Number(idx)) !== currentReviewError.correctAnswer
                  }">
              {{ getOptionLetter(Number(idx)) }}
            </span>
            <span class="flex-1" :class="{
              'text-green-700': showReviewResult && getOptionLetter(Number(idx)) === currentReviewError.correctAnswer,
              'text-red-700': showReviewResult && reviewAnswer === getOptionLetter(Number(idx)) && getOptionLetter(Number(idx)) !== currentReviewError.correctAnswer
            }">{{ option }}</span>
            <el-icon v-if="showReviewResult && getOptionLetter(Number(idx)) === currentReviewError.correctAnswer" class="text-green-500"><Check /></el-icon>
            <el-icon v-if="showReviewResult && reviewAnswer === getOptionLetter(Number(idx)) && getOptionLetter(Number(idx)) !== currentReviewError.correctAnswer" class="text-red-500"><Close /></el-icon>
          </div>
        </div>

        <!-- 判断题 -->
        <div v-if="currentReviewError.questionType === 'true_false'" class="flex gap-4 mb-4">
          <div class="flex-1 p-4 rounded-lg border text-center cursor-pointer transition-all"
               :class="{
                 'border-primary-500 bg-primary-50': reviewAnswer === '正确',
                 'border-gray-200 hover:border-primary-300': reviewAnswer !== '正确',
                 'border-green-500 bg-green-50': showReviewResult && currentReviewError.correctAnswer === '正确',
                 'border-red-500 bg-red-50': showReviewResult && reviewAnswer === '正确' && currentReviewError.correctAnswer !== '正确'
               }"
               @click="!showReviewResult && (reviewAnswer = '正确')">
            <span class="font-medium">正确</span>
          </div>
          <div class="flex-1 p-4 rounded-lg border text-center cursor-pointer transition-all"
               :class="{
                 'border-primary-500 bg-primary-50': reviewAnswer === '错误',
                 'border-gray-200 hover:border-primary-300': reviewAnswer !== '错误',
                 'border-green-500 bg-green-50': showReviewResult && currentReviewError.correctAnswer === '错误',
                 'border-red-500 bg-red-50': showReviewResult && reviewAnswer === '错误' && currentReviewError.correctAnswer !== '错误'
               }"
               @click="!showReviewResult && (reviewAnswer = '错误')">
            <span class="font-medium">错误</span>
          </div>
        </div>

        <!-- 填空题/简答题输入 -->
        <div v-if="currentReviewError.questionType === 'fill_blank' || currentReviewError.questionType === 'short_answer'" class="mb-4">
          <el-input 
            v-model="reviewAnswer" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入你的答案..."
            :disabled="showReviewResult"
          />
        </div>
      </div>

      <!-- 答案和解析（提交后显示） -->
      <div v-if="showReviewResult" class="space-y-4">
        <div class="bg-green-50 rounded-xl p-4 border border-green-100">
          <div class="flex items-center gap-2 text-green-700 font-medium mb-2">
            <el-icon><Check /></el-icon>
            正确答案
          </div>
          <div class="text-gray-800">{{ currentReviewError.correctAnswer }}</div>
        </div>
        
        <div class="bg-blue-50 rounded-xl p-4 border border-blue-100">
          <div class="flex items-center gap-2 text-blue-700 font-medium mb-2">
            <el-icon><Cpu /></el-icon>
            解析
          </div>
          <div class="text-gray-700 text-sm leading-relaxed">{{ currentReviewError.aiAnalysis || '暂无解析' }}</div>
        </div>

        <div class="flex items-center gap-2 text-sm text-gray-500">
          <span>上次答案:</span>
          <span class="text-red-500">{{ currentReviewError.myAnswer }}</span>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="flex justify-between mt-6">
        <el-button v-if="showReviewResult" type="success" @click="markCurrentMastered">
          <el-icon class="mr-1"><CircleCheck /></el-icon>
          已掌握，下一题
        </el-button>
        <div v-else></div>
        
        <div class="flex gap-2">
          <el-button v-if="!showReviewResult" type="primary" :disabled="!reviewAnswer" @click="submitReviewAnswer">
            提交答案
          </el-button>
          <el-button v-else type="primary" @click="nextReviewQuestion">
            {{ currentReviewIndex < reviewList.length - 1 ? '下一题' : '完成复习' }}
            <el-icon class="ml-1"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { DocumentDelete, CircleCheck, Reading, TrendCharts, VideoPlay, Search, ArrowDown, ArrowRight, Close, Check, Cpu, Delete, EditPen, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { errorBookApi, aiApi } from '@/api'
import { useUserStore } from '@/stores'

const userStore = useUserStore()
const searchQuery = ref('')
const filterMastery = ref('all')
const filterSubjects = ref<string[]>([])
const filterKnowledge = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const selectedErrors = ref<number[]>([])
const errorList = ref<any[]>([])
const knowledgePoints = ref<string[]>([])

// AI答疑对话框
const aiChatVisible = ref(false)
const currentError = ref<any>(null)
const chatMessages = ref<{role: 'user' | 'ai', content: string, isTyping?: boolean}[]>([])
const chatInput = ref('')
const chatLoading = ref(false)
const currentTypingMessage = ref('')

// 打字机效果 - 逐字显示
const typewriterEffect = (messageIndex: number, fullText: string, speed: number = 30) => {
  let currentIndex = 0
  currentTypingMessage.value = ''
  
  const typeNextChar = () => {
    if (currentIndex < fullText.length) {
      currentTypingMessage.value += fullText[currentIndex]
      chatMessages.value[messageIndex].content = currentTypingMessage.value
      currentIndex++
      
      // 使用requestAnimationFrame或setTimeout控制速度
      const delay = fullText[currentIndex - 1] === '。' || fullText[currentIndex - 1] === '！' || fullText[currentIndex - 1] === '？' ? speed * 3 : speed
      setTimeout(typeNextChar, delay)
    } else {
      chatMessages.value[messageIndex].isTyping = false
    }
  }
  
  chatMessages.value[messageIndex].isTyping = true
  typeNextChar()
}

// 加载错题本
const loadErrorBook = async () => {
  if (!userStore.user) return
  
  try {
    const errors = await errorBookApi.getStudentErrors(userStore.user.id)
    errorList.value = errors.map((e: any) => ({
      id: e.id,
      subject: getSubjectName(e.subject || 'chinese'),
      knowledgePoint: e.knowledgePoint,
      question: e.questionContent || '',
      questionType: e.questionType || 'short_answer',
      options: e.options || [],
      score: e.score || 0,
      difficulty: e.difficulty || 'medium',
      myAnswer: e.wrongAnswer,
      correctAnswer: e.correctAnswer,
      mastery: e.masteryStatus ?? 0,
      date: e.createTime?.split('T')[0] || '',
      errorReason: e.notes || '',
      aiAnalysis: e.analysis || ''
    }))
  } catch (error: any) {
    ElMessage.error('加载错题本失败: ' + error.message)
  }
}

const getSubjectName = (subject: string) => {
  const map: Record<string, string> = {
    chinese: '语文',
    math: '数学',
    english: '英语',
    physics: '物理',
    chemistry: '化学'
  }
  return map[subject] || subject
}

const stats = computed(() => {
  const total = errorList.value.length
  const mastered = errorList.value.filter(e => e.mastery === 2).length
  const learning = errorList.value.filter(e => e.mastery === 1).length
  const masteryRate = total > 0 ? Math.round((mastered / total) * 100) : 0
  return { total, mastered, learning, masteryRate }
})

const filteredErrors = computed(() => {
  return errorList.value.filter(err => {
    if (searchQuery.value && !err.question.includes(searchQuery.value)) return false
    if (filterMastery.value !== 'all') {
      const masteryMap: Record<string, number> = { mastered: 2, learning: 1, weak: 0 }
      if (err.mastery !== masteryMap[filterMastery.value]) return false
    }
    if (filterSubjects.value.length > 0 && !filterSubjects.value.includes(err.subject)) return false
    if (filterKnowledge.value && err.knowledgePoint !== filterKnowledge.value) return false
    return true
  })
})

// 分页后的错题列表
const paginatedErrors = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredErrors.value.slice(start, end)
})

const getQuestionTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    single_choice: '单选题',
    multiple_choice: '多选题',
    true_false: '判断题',
    fill_blank: '填空题',
    short_answer: '简答题',
    reading_comprehension: '阅读理解'
  }
  return map[type] || type
}

const getDifficultyLabel = (difficulty: string) => {
  const map: Record<string, string> = {
    easy: '简单',
    medium: '中等',
    hard: '困难'
  }
  return map[difficulty] || difficulty
}

const getMasteryType = (mastery: number) => {
  return mastery === 2 ? 'success' : mastery === 1 ? 'warning' : 'danger'
}

const getMasteryText = (mastery: number) => {
  return mastery === 2 ? '已掌握' : mastery === 1 ? '复习中' : '未掌握'
}

// 获取选项字母 (A, B, C, D...)
const getOptionLetter = (index: number): string => {
  return String.fromCharCode(65 + index)
}

const markMastered = async (id: number) => {
  try {
    await errorBookApi.updateMastery(id, 2)
    const err = errorList.value.find(e => e.id === id)
    if (err) err.mastery = 2
    ElMessage.success('已标记为掌握')
  } catch (error: any) {
    ElMessage.error('操作失败: ' + error.message)
  }
}

const deleteError = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这道错题吗？', '提示', { type: 'warning' })
    await errorBookApi.deleteError(id)
    errorList.value = errorList.value.filter(e => e.id !== id)
    ElMessage.success('删除成功')
  } catch {}
}

const reviewError = async (err: any) => {
  try {
    await errorBookApi.reviewError(err.id, '复习备注')
    ElMessage.info(`开始复习：${err.question.substring(0, 20)}...`)
  } catch (error: any) {
    ElMessage.error('复习记录失败: ' + error.message)
  }
}

// 复习模式
const reviewMode = ref(false)
const reviewList = ref<any[]>([])
const currentReviewIndex = ref(0)
const reviewAnswer = ref('')
const showReviewResult = ref(false)

// 当前复习的题目
const currentReviewError = computed(() => {
  if (reviewList.value.length === 0) return null
  return reviewList.value[currentReviewIndex.value]
})

// 开始复习模式
const startReview = () => {
  const weakErrors = errorList.value.filter(e => e.mastery !== 2)
  if (weakErrors.length === 0) {
    ElMessage.success('恭喜！你已掌握所有错题')
    return
  }
  reviewList.value = weakErrors
  currentReviewIndex.value = 0
  reviewAnswer.value = ''
  showReviewResult.value = false
  reviewMode.value = true
  ElMessage.success(`进入复习模式，共 ${weakErrors.length} 道错题`)
}

// 提交复习答案
const submitReviewAnswer = () => {
  showReviewResult.value = true
}

// 下一题
const nextReviewQuestion = async () => {
  // 记录复习
  if (currentReviewError.value) {
    try {
      await errorBookApi.reviewError(currentReviewError.value.id, '复习完成')
    } catch {}
  }
  
  if (currentReviewIndex.value < reviewList.value.length - 1) {
    currentReviewIndex.value++
    reviewAnswer.value = ''
    showReviewResult.value = false
  } else {
    // 复习完成
    ElMessage.success('恭喜！已完成本次复习')
    reviewMode.value = false
    loadErrorBook()
  }
}

// 标记当前题掌握
const markCurrentMastered = async () => {
  if (!currentReviewError.value) return
  try {
    await errorBookApi.updateMastery(currentReviewError.value.id, 2)
    currentReviewError.value.mastery = 2
    ElMessage.success('已标记为掌握')
    nextReviewQuestion()
  } catch (error: any) {
    ElMessage.error('操作失败: ' + error.message)
  }
}

// 退出复习模式
const exitReviewMode = () => {
  reviewMode.value = false
  reviewList.value = []
  currentReviewIndex.value = 0
}

const handleBatchAction = async (command: string) => {
  if (selectedErrors.value.length === 0) {
    ElMessage.warning('请先选择错题')
    return
  }
  
  try {
    if (command === 'master') {
      for (const id of selectedErrors.value) {
        await errorBookApi.updateMastery(id, 2)
        const err = errorList.value.find(e => e.id === id)
        if (err) err.mastery = 2
      }
      ElMessage.success(`已标记 ${selectedErrors.value.length} 道题为已掌握`)
      selectedErrors.value = []
    } else if (command === 'review') {
      // 批量加入复习计划
      for (const id of selectedErrors.value) {
        await errorBookApi.reviewError(id, '加入批量复习计划')
        const err = errorList.value.find(e => e.id === id)
        if (err && err.mastery === 0) err.mastery = 1
      }
      ElMessage.success(`已将 ${selectedErrors.value.length} 道题加入复习计划`)
      selectedErrors.value = []
      loadErrorBook()
    } else if (command === 'export') {
      ElMessage.success('错题导出成功')
    }
  } catch (error: any) {
    ElMessage.error('操作失败: ' + error.message)
  }
}

// 快捷问题
const quickQuestions = [
  '为什么我的答案是错的？',
  '这道题考察什么知识点？',
  '类似的题目应该怎么解？',
  '你有什么好的学习方法吗？',
  '能给我一个类似的练习题吗？'
]

// 打开AI答疑对话框
const openAIChat = (err: any) => {
  currentError.value = err
  chatMessages.value = []
  chatInput.value = ''
  currentTypingMessage.value = ''
  aiChatVisible.value = true
  
  // 自动发送欢迎消息，使用打字机效果
  const welcomeMessage = `我是BYG AI，可以帮你分析这道错题。\n\n题目：${err.question}\n\n你的答案：${err.myAnswer}\n正确答案：${err.correctAnswer}\n\n${err.aiAnalysis ? `初步分析：${err.aiAnalysis}\n\n` : ''}有什么疑问尽管问我！`
  
  chatMessages.value.push({ role: 'ai', content: '', isTyping: true })
  typewriterEffect(0, welcomeMessage, 20)
}

// 发送消息
const sendChatMessage = async () => {
  if (!chatInput.value.trim() || !currentError.value) return
  
  const question = chatInput.value.trim()
  chatMessages.value.push({ role: 'user', content: question, isTyping: false })
  chatInput.value = ''
  chatLoading.value = true
  
  try {
    // 构建完整题目上下文（包含选项）
    let fullQuestion = currentError.value.question
    
    // 如果有选项，格式化为 ABCD 并附加到题目
    if (currentError.value.options && currentError.value.options.length > 0) {
      const optionsText = currentError.value.options.map((opt: string, idx: number) => {
        return `${getOptionLetter(idx)}. ${opt}`
      }).join('\n')
      fullQuestion = `${fullQuestion}\n\n选项：\n${optionsText}`
    }
    
    // 调用AI聊天答疑接口
    const response = await aiApi.chat({
      question: fullQuestion,
      studentAnswer: currentError.value.myAnswer,
      correctAnswer: currentError.value.correctAnswer,
      chatMessage: question,
      knowledgePoint: currentError.value.knowledgePoint || 'general'
    })
    
    // 添加AI消息占位符
    const aiMessageIndex = chatMessages.value.length
    chatMessages.value.push({ role: 'ai', content: '', isTyping: true })
    
    // 启动打字机效果
    const aiResponse = response || '抱歉，AI暂无回复。'
    typewriterEffect(aiMessageIndex, aiResponse, 25)
  } catch (error: any) {
    console.error('AI答疑失败:', error)
    ElMessage.error('AI回复失败: ' + error.message)
    chatMessages.value.push({
      role: 'ai',
      content: '抱歉，AI服务暂时不可用，请稍后重试。',
      isTyping: false
    })
  } finally {
    chatLoading.value = false
  }
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

onMounted(() => {
  loadErrorBook()
})
</script>
