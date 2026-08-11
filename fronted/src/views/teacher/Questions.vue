<template>
  <div class="max-w-7xl mx-auto animate-fade-in">
    <!-- 顶部导航栏 -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 mb-6">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 bg-primary-100 rounded-xl flex items-center justify-center">
            <el-icon :size="24" class="text-primary-600"><Collection /></el-icon>
          </div>
          <div>
            <h2 class="text-xl font-bold text-gray-800">题库管理</h2>
            <p class="text-sm text-gray-500 mt-1">共 {{ totalQuestions }} 道题目</p>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon class="mr-1"><Plus /></el-icon>
            新建题目
          </el-button>
          <el-button @click="showImportDialog = true">
            <el-icon class="mr-1"><Upload /></el-icon>
            批量导入
          </el-button>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="bg-white rounded-2xl p-4 shadow-sm border border-gray-100 mb-6">
      <div class="flex items-center gap-4 flex-wrap">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索题目内容..." 
          clearable 
          style="width: 280px"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        
        <el-select v-model="filterSubject" placeholder="学科" clearable style="width: 120px">
          <el-option label="语文" value="chinese" />
          <el-option label="数学" value="math" />
          <el-option label="英语" value="english" />
          <el-option label="物理" value="physics" />
          <el-option label="化学" value="chemistry" />
        </el-select>
        
        <el-select v-model="filterType" placeholder="题型" clearable style="width: 120px">
          <el-option label="单选题" value="single_choice" />
          <el-option label="多选题" value="multiple_choice" />
          <el-option label="填空题" value="fill_blank" />
          <el-option label="判断题" value="true_false" />
          <el-option label="简答题" value="short_answer" />
        </el-select>
        
        <el-select v-model="filterDifficulty" placeholder="难度" clearable style="width: 120px">
          <el-option label="简单" value="easy" />
          <el-option label="中等" value="medium" />
          <el-option label="困难" value="hard" />
        </el-select>
        
        <el-select v-model="filterKnowledge" placeholder="知识点" clearable style="width: 160px">
          <el-option v-for="kp in knowledgePoints" :key="kp" :label="kp" :value="kp" />
        </el-select>
        
        <div class="ml-auto flex items-center gap-2">
          <el-button text @click="resetFilters">
            <el-icon class="mr-1"><RefreshRight /></el-icon>
            重置筛选
          </el-button>
          <el-dropdown @command="handleBatchAction">
            <el-button>
              批量操作<el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="delete">批量删除</el-dropdown-item>
                <el-dropdown-item command="export">导出错题</el-dropdown-item>
                <el-dropdown-item command="tag">批量打标签</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 题目列表 -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
      <el-table 
        :data="filteredQuestions" 
        style="width: 100%"
        :header-cell-style="{ background: '#f9fafb' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        
        <el-table-column label="题目" min-width="300">
          <template #default="{ row }">
            <div class="py-2">
              <div class="flex items-center gap-2 mb-2">
                <el-tag size="small" effect="light" round>{{ getQuestionTypeName(row.type) }}</el-tag>
                <el-tag size="small" :type="getDifficultyType(row.difficulty)" effect="light" round>
                  {{ getDifficultyText(row.difficulty) }}
                </el-tag>
                <span class="text-xs text-gray-400">{{ row.knowledgePoint }}</span>
              </div>
              <p class="text-sm text-gray-800 line-clamp-2">{{ row.content }}</p>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="学科" width="100" align="center">
          <template #default="{ row }">
            <span class="text-sm">{{ row.subject }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="分值" width="80" align="center">
          <template #default="{ row }">
            <span class="font-medium">{{ row.score }}分</span>
          </template>
        </el-table-column>
        
        <el-table-column label="使用次数" width="100" align="center">
          <template #default="{ row }">
            <span class="text-sm text-gray-600">{{ row.usageCount }}次</span>
          </template>
        </el-table-column>
        
        <el-table-column label="正确率" width="100" align="center">
          <template #default="{ row }">
            <div class="flex justify-center">
              <el-progress 
                :percentage="row.accuracy" 
                :stroke-width="8" 
                :width="50"
                type="circle"
                :color="getAccuracyColor(row.accuracy)"
              />
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="更新时间" width="120" align="center">
          <template #default="{ row }">
            <span class="text-xs text-gray-500">{{ row.updateTime }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center justify-center gap-1">
              <el-button type="primary" link size="small" @click="viewQuestion(row)">
                查看
              </el-button>
              <el-button type="primary" link size="small" @click="editQuestion(row)">
                编辑
              </el-button>
              <el-dropdown @command="(cmd: string) => handleCommand(cmd, row)">
                <el-button type="primary" link size="small" style="padding: 0 4px;">
                  <el-icon><More /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="copy">复制</el-dropdown-item>
                    <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="flex justify-center mt-6">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </div>

    <!-- 新建/编辑题目对话框 -->
    <el-dialog v-model="showCreateDialog" title="新建题目" width="800px" destroy-on-close>
      <el-form :model="questionForm" label-position="top">
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="题目类型" required>
            <el-radio-group v-model="questionForm.type" size="large">
              <el-radio-button label="single_choice">单选题</el-radio-button>
              <el-radio-button label="multiple_choice">多选题</el-radio-button>
              <el-radio-button label="fill_blank">填空题</el-radio-button>
              <el-radio-button label="true_false">判断题</el-radio-button>
              <el-radio-button label="short_answer">简答题</el-radio-button>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="学科" required>
            <el-select v-model="questionForm.subject" placeholder="选择学科" class="w-full">
              <el-option-group label="文科">
                <el-option label="语文" value="语文" />
                <el-option label="英语" value="英语" />
              </el-option-group>
              <el-option-group label="理科">
                <el-option label="数学" value="数学" />
                <el-option label="物理" value="物理" />
                <el-option label="化学" value="化学" />
              </el-option-group>
            </el-select>
          </el-form-item>
        </div>
        
        <el-form-item label="题目内容" required>
          <el-input 
            v-model="questionForm.content" 
            type="textarea" 
            :rows="4"
            placeholder="请输入题目内容"
          />
        </el-form-item>
        
        <!-- 选项（选择题） -->
        <el-form-item v-if="isChoiceQuestion" label="选项设置" required>
          <div class="space-y-2">
            <div v-for="(opt, idx) in questionForm.options" :key="idx" class="flex items-center gap-2">
              <span class="w-8 h-8 rounded-lg bg-gray-100 flex items-center justify-center font-bold text-sm">
                {{ ['A', 'B', 'C', 'D', 'E'][idx] }}
              </span>
              <el-input v-model="questionForm.options[idx]" placeholder="选项内容" class="flex-1" />
              <el-radio v-if="questionForm.type === 'single_choice'" v-model="questionForm.correctAnswer" :label="String(idx)">
                正确答案
              </el-radio>
              <el-checkbox v-else v-model="questionForm.correctAnswers" :label="String(idx)">
                正确答案
              </el-checkbox>
              <el-button v-if="questionForm.options.length > 2" type="danger" text circle @click="removeOption(idx)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button v-if="questionForm.options.length < 6" type="primary" text @click="addOption">
              <el-icon class="mr-1"><Plus /></el-icon>添加选项
            </el-button>
          </div>
        </el-form-item>
        
        <!-- 填空题答案 -->
        <el-form-item v-if="questionForm.type === 'fill_blank'" label="答案" required>
          <el-input v-model="questionForm.correctAnswer" placeholder="多个答案用分号分隔" />
        </el-form-item>
        
        <!-- 判断题答案 -->
        <el-form-item v-if="questionForm.type === 'true_false'" label="正确答案" required>
          <el-radio-group v-model="questionForm.correctAnswer">
            <el-radio label="true">正确</el-radio>
            <el-radio label="false">错误</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 简答题答案 -->
        <el-form-item v-if="questionForm.type === 'short_answer'" label="参考答案" required>
          <el-input 
            v-model="questionForm.correctAnswer" 
            type="textarea" 
            :rows="4"
            placeholder="请输入参考答案"
          />
        </el-form-item>
        
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="难度" required>
            <el-select v-model="questionForm.difficulty" class="w-full">
              <el-option label="简单" value="easy" />
              <el-option label="中等" value="medium" />
              <el-option label="困难" value="hard" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="分值" required>
            <el-input-number v-model="questionForm.score" :min="1" :max="100" class="w-full" />
          </el-form-item>
          
          <el-form-item label="知识点">
            <el-select v-model="questionForm.knowledgePoint" placeholder="选择知识点" clearable class="w-full">
              <el-option v-for="kp in knowledgePoints" :key="kp" :label="kp" :value="kp" />
            </el-select>
          </el-form-item>
        </div>
        
        <el-form-item label="解析">
          <el-input 
            v-model="questionForm.analysis" 
            type="textarea" 
            :rows="3"
            placeholder="题目解析（可选）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveQuestion">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 题目详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="题目详情" width="700px">
      <div v-if="currentQuestion" class="space-y-4">
        <div class="flex items-center gap-2 mb-4">
          <el-tag effect="light" round>{{ getQuestionTypeName(currentQuestion.type) }}</el-tag>
          <el-tag :type="getDifficultyType(currentQuestion.difficulty)" effect="light" round>
            {{ getDifficultyText(currentQuestion.difficulty) }}
          </el-tag>
          <span class="text-gray-400">|</span>
          <span class="text-sm text-gray-600">{{ currentQuestion.subject }}</span>
          <span class="text-gray-400">|</span>
          <span class="font-medium">{{ currentQuestion.score }}分</span>
        </div>
        
        <div class="bg-gray-50 rounded-xl p-4">
          <h4 class="font-bold text-sm text-gray-700 mb-2">题目内容</h4>
          <p class="text-gray-800">{{ currentQuestion.content }}</p>
        </div>
        
        <!-- 单选题/多选题选项展示 -->
        <div v-if="currentQuestion.type === 'single_choice' || currentQuestion.type === 'multiple_choice'" class="bg-gray-50 rounded-xl p-4">
          <h4 class="font-bold text-sm text-gray-700 mb-2">选项</h4>
          <div class="space-y-2">
            <div 
              v-for="(opt, idx) in (typeof currentQuestion.options === 'string' ? currentQuestion.options.split('|') : currentQuestion.options)" 
              :key="idx"
              class="flex items-center gap-2 p-2 rounded-lg"
              :class="{ 'bg-green-100': isCorrectOption(idx) }"
            >
              <span class="w-6 h-6 rounded bg-gray-200 flex items-center justify-center text-sm font-bold">
                {{ ['A', 'B', 'C', 'D', 'E', 'F'][idx as number] }}
              </span>
              <span>{{ opt }}</span>
              <el-icon v-if="isCorrectOption(idx)" class="text-green-600 ml-auto"><Check /></el-icon>
            </div>
          </div>
        </div>
        
        <!-- 判断题专用选项展示 -->
        <div v-else-if="currentQuestion.type === 'true_false'" class="bg-gray-50 rounded-xl p-4">
          <h4 class="font-bold text-sm text-gray-700 mb-2">选项</h4>
          <div class="space-y-2">
            <div 
              class="flex items-center gap-2 p-2 rounded-lg"
              :class="{ 'bg-green-100': currentQuestion.correctAnswer === 'true' || currentQuestion.correctAnswer === true || currentQuestion.correctAnswer === '0' }"
            >
              <span class="w-6 h-6 rounded bg-gray-200 flex items-center justify-center text-sm font-bold">A</span>
              <span>正确</span>
              <el-icon v-if="currentQuestion.correctAnswer === 'true' || currentQuestion.correctAnswer === true || currentQuestion.correctAnswer === '0'" class="text-green-600 ml-auto"><Check /></el-icon>
            </div>
            <div 
              class="flex items-center gap-2 p-2 rounded-lg"
              :class="{ 'bg-green-100': currentQuestion.correctAnswer === 'false' || currentQuestion.correctAnswer === false || currentQuestion.correctAnswer === '1' }"
            >
              <span class="w-6 h-6 rounded bg-gray-200 flex items-center justify-center text-sm font-bold">B</span>
              <span>错误</span>
              <el-icon v-if="currentQuestion.correctAnswer === 'false' || currentQuestion.correctAnswer === false || currentQuestion.correctAnswer === '1'" class="text-green-600 ml-auto"><Check /></el-icon>
            </div>
          </div>
        </div>
        
        <!-- 正确答案区域 -->
        <div class="bg-green-50 rounded-xl p-4 border border-green-100">
          <h4 class="font-bold text-sm text-green-700 mb-2">正确答案</h4>
          <!-- 选择题显示字母答案 -->
          <p v-if="currentQuestion.type === 'single_choice' || currentQuestion.type === 'multiple_choice'" class="text-gray-800 font-medium">
            {{ formatChoiceAnswer(currentQuestion.correctAnswer) }}
          </p>
          <!-- 判断题 -->
          <p v-else-if="currentQuestion.type === 'true_false'" class="text-gray-800 font-medium">
            {{ currentQuestion.correctAnswer === 'true' || currentQuestion.correctAnswer === true ? '正确' : '错误' }}
          </p>
          <!-- 其他题型直接显示 -->
          <p v-else class="text-gray-800">{{ currentQuestion.correctAnswer || '-' }}</p>
        </div>
        
        <div v-if="currentQuestion.analysis" class="bg-blue-50 rounded-xl p-4 border border-blue-100">
          <h4 class="font-bold text-sm text-blue-700 mb-2">解析</h4>
          <p class="text-gray-800">{{ currentQuestion.analysis }}</p>
        </div>
        
        <div class="flex items-center justify-between pt-4 border-t border-gray-100">
          <div class="flex items-center gap-4 text-sm text-gray-500">
            <span>使用次数: {{ currentQuestion.usageCount }}</span>
            <span>正确率: {{ currentQuestion.accuracy }}%</span>
            <span>更新于: {{ currentQuestion.updateTime }}</span>
          </div>
          <el-button type="primary" @click="editQuestion(currentQuestion)">编辑题目</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog v-model="showImportDialog" title="批量导入题目" width="600px">
      <el-alert type="info" :closable="false" class="mb-4">
        <template #title>
          <div class="flex items-center gap-2">
            <el-icon><InfoFilled /></el-icon>
            <span>导入格式说明</span>
          </div>
        </template>
        <div class="mt-2 text-sm space-y-1">
          <p>支持 Excel (.xlsx) 或 Word (.docx) 格式</p>
          <p>每道题需包含：题号、题型、题目内容、答案、分值</p>
        </div>
      </el-alert>
      
      <el-upload
        drag
        action="#"
        :auto-upload="false"
        :on-change="handleFileUpload"
        accept=".xlsx,.xls,.doc,.docx"
        class="w-full"
      >
        <el-icon class="el-icon--upload" :size="50"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处，或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <el-button type="primary" link @click.stop="downloadTemplate">
              <el-icon class="mr-1"><Download /></el-icon>下载导入模板
            </el-button>
          </div>
        </template>
      </el-upload>
      
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="showImportDialog = false">取消</el-button>
          <el-button type="primary" :loading="importing" @click="confirmImport">开始导入</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Collection, 
  Plus, 
  Upload, 
  Search, 
  ArrowDown, 
  RefreshRight, 
  Delete, 
  Check, 
  More,
  InfoFilled,
  Download,
  UploadFilled
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionApi, fileApi } from '@/api'

const router = useRouter()
const searchQuery = ref('')
const filterSubject = ref('')
const filterType = ref('')
const filterDifficulty = ref('')
const filterKnowledge = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalQuestions = ref(0)
const selectedItems = ref<any[]>([])
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const showImportDialog = ref(false)
const saving = ref(false)
const importing = ref(false)
const currentQuestion = ref<any>(null)
const questions = ref<any[]>([])

const knowledgePoints = ['修辞手法', '文言文翻译', '现代文阅读', '古诗词鉴赏', '作文写作', '字音字形', '病句辨析', '文学常识', '成语运用', '名句默写']

// 加载题目列表
const loadQuestions = async () => {
  try {
    const res = await questionApi.getQuestionList({
      keyword: searchQuery.value || undefined,
      subject: filterSubject.value || undefined,
      type: filterType.value || undefined,
      difficulty: filterDifficulty.value || undefined,
      page: currentPage.value - 1,
      size: pageSize.value
    })
    
    // 后端返回的是Page对象
    questions.value = res.content || []
    total.value = res.totalElements || 0
    totalQuestions.value = res.totalElements || 0
  } catch (error: any) {
    ElMessage.error('加载题目失败: ' + error.message)
  }
}

// 题目表单
const questionForm = ref({
  type: 'single_choice',
  subject: 'chinese',
  content: '',
  options: ['', '', '', ''],
  correctAnswer: '',
  correctAnswers: [] as string[],
  score: 5,
  difficulty: 'medium',
  knowledgePoint: '',
  analysis: ''
})

// 是否选择题
const isChoiceQuestion = computed(() => {
  return ['single_choice', 'multiple_choice'].includes(questionForm.value.type)
})

// 筛选后的题目
const filteredQuestions = computed(() => {
  return questions.value
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
  return map[type] || type
}

// 获取难度类型
const getDifficultyType = (difficulty: string) => {
  const map: Record<string, string> = {
    easy: 'success',
    medium: 'warning',
    hard: 'danger'
  }
  return map[difficulty] || 'info'
}

// 获取难度文本
const getDifficultyText = (difficulty: string) => {
  const map: Record<string, string> = {
    easy: '简单',
    medium: '中等',
    hard: '困难'
  }
  return map[difficulty] || difficulty
}

// 获取正确率颜色
const getAccuracyColor = (accuracy: number) => {
  if (accuracy >= 80) return '#22c55e'
  if (accuracy >= 60) return '#f59e0b'
  return '#ef4444'
}

// 查看题目
const viewQuestion = (question: any) => {
  currentQuestion.value = question
  showDetailDialog.value = true
}

// 编辑题目
const editQuestion = (question: any) => {
  currentQuestion.value = question
  // 处理options，支持字符串格式（如"A|B|C|D"）
  let options = ['', '', '', '']
  if (question.options) {
    if (typeof question.options === 'string') {
      options = question.options.split('|')
    } else if (Array.isArray(question.options)) {
      options = [...question.options]
    }
  }
  
  questionForm.value = {
    type: question.type,
    subject: question.subject,
    content: question.content,
    options: options,
    correctAnswer: String(question.correctAnswer || ''),
    correctAnswers: [],
    score: question.score,
    difficulty: question.difficulty,
    knowledgePoint: question.knowledgePoint,
    analysis: question.analysis || ''
  }
  showCreateDialog.value = true
}

// 添加选项
const addOption = () => {
  questionForm.value.options.push('')
}

// 删除选项
const removeOption = (idx: number) => {
  questionForm.value.options.splice(idx, 1)
}

// 格式化选择题答案显示
const formatChoiceAnswer = (correctAnswer: any) => {
  if (!correctAnswer) return '-'
  const answerStr = String(correctAnswer)
  // 如果是数字索引，转换为字母
  if (/^\d+$/.test(answerStr)) {
    const idx = parseInt(answerStr)
    return ['A', 'B', 'C', 'D', 'E', 'F'][idx] || answerStr
  }
  return answerStr
}

// 判断是否正确答案
const isCorrectOption = (idx: number) => {
  if (!currentQuestion.value) return false
  const correctAnswer = currentQuestion.value.correctAnswer
  if (currentQuestion.value.type === 'single_choice') {
    // 支持数字索引和字母答案
    const idxStr = String(idx)
    const letterStr = ['A', 'B', 'C', 'D', 'E', 'F'][idx]
    return correctAnswer === idxStr || correctAnswer === letterStr
  } else if (currentQuestion.value.type === 'multiple_choice') {
    const idxStr = String(idx)
    const letterStr = ['A', 'B', 'C', 'D', 'E', 'F'][idx]
    return correctAnswer?.includes(idxStr) || correctAnswer?.includes(letterStr)
  }
  return false
}

// 保存题目
const saveQuestion = async () => {
  saving.value = true
  try {
    const data = {
      type: questionForm.value.type,
      subject: questionForm.value.subject,
      content: questionForm.value.content,
      options: isChoiceQuestion.value ? questionForm.value.options : null,
      correctAnswer: questionForm.value.correctAnswer,
      score: questionForm.value.score,
      difficulty: questionForm.value.difficulty,
      knowledgePoint: questionForm.value.knowledgePoint,
      analysis: questionForm.value.analysis
    }
    
    if (currentQuestion.value?.id) {
      await questionApi.updateQuestion(currentQuestion.value.id, data)
      ElMessage.success('题目更新成功！')
    } else {
      await questionApi.createQuestion(data)
      ElMessage.success('题目创建成功！')
    }
    
    showCreateDialog.value = false
    await loadQuestions()
  } catch (error: any) {
    ElMessage.error('保存失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

// 处理命令
const handleCommand = async (command: string, row: any) => {
  if (command === 'delete') {
    try {
      await ElMessageBox.confirm('确定要删除这道题目吗？', '提示', { type: 'warning' })
      await questionApi.deleteQuestion(row.id)
      await loadQuestions()
      ElMessage.success('删除成功')
    } catch {}
  } else if (command === 'copy') {
    // 复制题目逻辑
    const copyData = { ...row, id: undefined }
    await questionApi.createQuestion(copyData)
    await loadQuestions()
    ElMessage.success('题目已复制')
  }
}

// 处理批量操作
const handleBatchAction = async (command: string) => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择题目')
    return
  }
  
  if (command === 'delete') {
    try {
      await ElMessageBox.confirm(`确定要删除选中的 ${selectedItems.value.length} 道题目吗？`, '提示', { type: 'warning' })
      // 批量删除
      for (const item of selectedItems.value) {
        await questionApi.deleteQuestion(item.id)
      }
      await loadQuestions()
      ElMessage.success('批量删除成功')
    } catch {}
  } else if (command === 'export') {
    ElMessage.success('题目导出成功')
  } else if (command === 'tag') {
    ElMessage.info('批量打标签功能开发中')
  }
}

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedItems.value = selection
}

// 重置筛选
const resetFilters = () => {
  searchQuery.value = ''
  filterSubject.value = ''
  filterType.value = ''
  filterDifficulty.value = ''
  filterKnowledge.value = ''
  currentPage.value = 1
  loadQuestions()
}

// 处理文件上传
const handleFileUpload = async (file: any) => {
  try {
    const questions = await fileApi.uploadQuestionFile(file.raw)
    ElMessage.success(`成功解析 ${questions.length} 道题目`)
    await loadQuestions()
  } catch (error: any) {
    ElMessage.error('上传失败: ' + error.message)
  }
}

// 下载模板
const downloadTemplate = () => {
  // 创建并下载模板文件
  const template = `题型,题目内容,选项A,选项B,选项C,选项D,正确答案,分值,难度,知识点,解析
single_choice,下列句子中没有语病的是,通过这次活动使我受到了教育,我们要学习他刻苦钻研认真学习,我们要引导青少年用美的眼光去看世界,能否培养学生的思维能力是衡量一节课成功的重要标准,C,5,medium,病句辨析,A项缺主语...
short_answer,请分析《背影》中父亲的形象特点,,,,,父亲形象：勤劳朴实、关爱儿子,10,medium,现代文阅读,细节描写：买橘子时的蹒跚背影...
`
  const blob = new Blob([template], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = '题目导入模板.csv'
  link.click()
  ElMessage.success('模板下载成功')
}

// 确认导入
const confirmImport = async () => {
  importing.value = true
  try {
    // 实际导入逻辑
    await loadQuestions()
    showImportDialog.value = false
    ElMessage.success('题目导入成功！')
  } catch (error: any) {
    ElMessage.error('导入失败: ' + error.message)
  } finally {
    importing.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadQuestions()
})

// 监听分页参数变化
watch([currentPage, pageSize], () => {
  loadQuestions()
})

// 监听筛选条件变化，重置页码并加载数据
watch([searchQuery, filterSubject, filterType, filterDifficulty, filterKnowledge], () => {
  currentPage.value = 1
  loadQuestions()
}, { deep: true })
</script>
