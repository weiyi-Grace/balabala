<template>
  <div class="space-y-6 animate-fade-in">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-4 gap-4">
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center">
            <el-icon :size="24" class="text-blue-500"><Document /></el-icon>
          </div>
          <div>
            <div class="text-sm text-gray-500">总发布</div>
            <div class="text-2xl font-bold text-blue-600">{{ stats.total }}</div>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center">
            <el-icon :size="24" class="text-green-500"><Timer /></el-icon>
          </div>
          <div>
            <div class="text-sm text-gray-500">进行中</div>
            <div class="text-2xl font-bold text-green-600">{{ stats.active }}</div>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 bg-purple-100 rounded-xl flex items-center justify-center">
            <el-icon :size="24" class="text-purple-500"><CircleCheck /></el-icon>
          </div>
          <div>
            <div class="text-sm text-gray-500">待批改</div>
            <div class="text-2xl font-bold text-purple-600">{{ stats.pending }}</div>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 bg-orange-100 rounded-xl flex items-center justify-center">
            <el-icon :size="24" class="text-orange-500"><Calendar /></el-icon>
          </div>
          <div>
            <div class="text-sm text-gray-500">已截止</div>
            <div class="text-2xl font-bold text-orange-600">{{ stats.ended }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 作业列表 -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center gap-4">
          <h2 class="text-xl font-bold">作业管理</h2>
        </div>
        <el-button type="primary" @click="router.push('/teacher/homework/create')">
          <el-icon class="mr-1"><Plus /></el-icon>发布新任务
        </el-button>
      </div>

      <!-- 筛选栏 -->
      <div class="flex gap-4 mb-4">
        <el-input v-model="searchQuery" placeholder="搜索标题..." clearable style="width: 250px">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px">
          <el-option label="进行中" value="active" />
          <el-option label="未开始" value="pending" />
          <el-option label="已截止" value="ended" />
          <el-option label="待批改" value="correcting" />
        </el-select>
        <el-select v-model="filterClass" placeholder="班级" clearable style="width: 120px">
          <el-option label="高二1班" value="高二1班" />
          <el-option label="高二3班" value="高二3班" />
          <el-option label="高二5班" value="高二5班" />
        </el-select>
        <el-button type="primary" text @click="resetFilters">
          <el-icon><Refresh /></el-icon>重置
        </el-button>
      </div>

      <el-table :data="paginatedList" style="width: 100%" :header-cell-style="{ background: '#f9fafb' }" v-loading="loading">
        <el-table-column label="标题" min-width="300">
          <template #default="{ row }">
            <div class="font-medium">{{ row.title }}</div>
            <div class="text-xs text-gray-400 mt-1">
              截止：{{ row.deadline }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="class" label="班级" width="100" align="center" />
        <el-table-column label="提交进度" width="180" align="center">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-progress 
                :percentage="row.totalStudents > 0 ? Math.round((row.submittedCount / row.totalStudents) * 100) : 0" 
                :color="row.submittedCount === row.totalStudents ? '#22c55e' : '#3b82f6'"
                class="flex-1"
              />
              <span class="text-xs text-gray-500">{{ row.submittedCount }}/{{ row.totalStudents }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light" round>
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
            <el-button type="success" link size="small" @click="viewSubmissions(row)">批改</el-button>
            <el-dropdown @command="(cmd: string) => handleCommand(cmd, row)">
              <el-button type="primary" text><el-icon><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">编辑</el-dropdown-item>
                  <el-dropdown-item command="copy">复制</el-dropdown-item>
                  <el-dropdown-item command="export">导出成绩</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="flex justify-center mt-6">
        <el-pagination 
          v-model:current-page="currentPage" 
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="filteredList.length" 
          background 
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 作业详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="作业详情" width="800px">
      <div v-if="currentHomework" class="space-y-6">
        <!-- 基本信息 -->
        <div class="bg-gray-50 rounded-xl p-4">
          <h3 class="font-bold text-lg mb-2">{{ currentHomework.title }}</h3>
          <div class="grid grid-cols-3 gap-4 text-sm">
            <div>
              <span class="text-gray-500">班级：</span>
              <span class="font-medium">{{ currentHomework.className || '未知班级' }}</span>
            </div>
            <div>
              <span class="text-gray-500">学科：</span>
              <span class="font-medium">{{ currentHomework.subject }}</span>
            </div>
            <div>
              <span class="text-gray-500">截止时间：</span>
              <span class="font-medium">{{ currentHomework.deadline }}</span>
            </div>
            <div>
              <span class="text-gray-500">状态：</span>
              <el-tag size="small" :type="getStatusType(getHomeworkStatus(currentHomework))">
                {{ getHomeworkStatus(currentHomework) }}
              </el-tag>
            </div>
            <div>
              <span class="text-gray-500">AI批改：</span>
              <el-tag size="small" :type="currentHomework.aiEnabled ? 'success' : 'info'">
                {{ currentHomework.aiEnabled ? '启用' : '关闭' }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 题目列表 -->
        <div>
          <h4 class="font-bold mb-3 flex items-center gap-2">
            <el-icon><Document /></el-icon>
            题目列表 ({{ currentHomework.questions?.length || 0 }} 题)
          </h4>
          <div class="space-y-3">
            <div 
              v-for="(q, idx) in currentHomework.questions || []" 
              :key="q.id"
              class="border border-gray-200 rounded-lg p-4"
            >
              <div class="flex items-start gap-3">
                <span class="w-8 h-8 rounded-lg bg-primary-100 text-primary-700 flex items-center justify-center font-bold text-sm shrink-0">
                  {{ idx + 1 }}
                </span>
                <div class="flex-1">
                  <div class="flex items-center gap-2 mb-2">
                    <el-tag size="small">{{ getQuestionTypeName(q.type) }}</el-tag>
                    <el-tag size="small" type="warning">{{ q.score }}分</el-tag>
                    <el-tag size="small" type="info">{{ q.difficulty }}</el-tag>
                  </div>
                  <p class="text-gray-800 mb-2">{{ q.content }}</p>
                  <!-- 选项 -->
                  <div v-if="q.options && q.options.length > 0" class="space-y-1 mb-2">
                    <div v-for="(opt, optIdx) in (typeof q.options === 'string' ? q.options.split('|') : q.options)" :key="optIdx" class="text-sm text-gray-600">
                      {{ ['A', 'B', 'C', 'D', 'E', 'F'][optIdx as number] }}. {{ cleanOptionText(opt) }}
                    </div>
                  </div>
                  <!-- 正确答案 -->
                  <div class="text-sm">
                    <span class="text-gray-500">正确答案：</span>
                    <span class="text-green-600 font-medium">{{ formatAnswer(q.correctAnswer, q.type) || '-' }}</span>
                  </div>
                  <!-- 解析 -->
                  <div v-if="q.analysis" class="mt-2 p-2 bg-blue-50 rounded text-sm text-blue-700">
                    <span class="font-medium">解析：</span>{{ q.analysis }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Timer, CircleCheck, Calendar, Plus, Search, Refresh, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { homeworkApi, classApi } from '@/api'
import { useUserStore } from '@/stores'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const searchQuery = ref('')
const filterStatus = ref('')
const filterClass = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const classList = ref<any[]>([])

const homeworkList = ref<any[]>([])

// 加载作业列表
const loadHomeworkList = async () => {
  try {
    loading.value = true
    const data = await homeworkApi.getTeacherHomeworks(userStore.user?.id || 1)
    if (data) {
      homeworkList.value = data.map((h: any) => ({
        id: h.id,
        type: h.type === 'exam' ? 'exam' : 'homework',
        title: h.title,
        class: h.className || '未知班级',
        startTime: h.startTime || '',
        deadline: h.deadline,
        submittedCount: h.submittedCount || 0,
        totalStudents: h.totalStudents || 0,
        pendingCorrection: h.pendingCorrection || 0,
        status: getHomeworkStatus(h),
        avgScore: h.avgScore || null
      }))
    }
  } catch (error) {
    console.error('加载作业列表失败', error)
  } finally {
    loading.value = false
  }
}

// 获取作业状态
const getHomeworkStatus = (h: any) => {
  const now = new Date()
  const deadline = new Date(h.deadline)
  const submitted = h.submittedCount || 0
  const pending = h.pendingCorrection || 0
  
  // 已截止
  if (deadline < now) return '已截止'
  
  // 有待批改
  if (pending > 0) {
    return '待批改'
  }
  
  // 有提交且全部批改完成
  if (submitted > 0 && pending === 0) {
    return '已完成'
  }
  
  // 进行中（无提交）
  return '进行中'
}

const stats = computed(() => {
  const total = homeworkList.value.length
  const active = homeworkList.value.filter(h => h.status === '进行中').length
  const pending = homeworkList.value.filter(h => h.pendingCorrection > 0).length
  const ended = homeworkList.value.filter(h => h.status === '已截止').length
  return { total, active, pending, ended }
})

const filteredList = computed(() => {
  return homeworkList.value.filter(h => {
    if (searchQuery.value && !h.title.includes(searchQuery.value)) return false
    if (filterStatus.value) {
      const statusMap: Record<string, string> = { active: '进行中', pending: '未开始', ended: '已截止' }
      if (filterStatus.value === 'correcting') {
        return h.pendingCorrection > 0
      }
      if (h.status !== statusMap[filterStatus.value]) return false
    }
    if (filterClass.value && h.class !== filterClass.value) return false
    return true
  })
})

// 分页后的列表
const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    '进行中': 'success',
    '待批改': 'warning',
    '已完成': 'primary',
    '已截止': 'info'
  }
  return map[status] || 'info'
}

const getQuestionTypeName = (type: string) => {
  const map: Record<string, string> = {
    'single_choice': '单选题',
    'multiple_choice': '多选题',
    'fill_blank': '填空题',
    'true_false': '判断题',
    'short_answer': '简答题',
    'reading_comprehension': '阅读理解'
  }
  return map[type] || '简答题'
}

// 格式化答案显示
const formatAnswer = (correctAnswer: any, type: string) => {
  if (!correctAnswer) return '-'
  const answerStr = String(correctAnswer)
  
  // 判断题
  if (type === 'true_false') {
    return answerStr === 'true' || answerStr === '1' ? '正确' : '错误'
  }
  
  // 选择题，数字索引转字母
  if ((type === 'single_choice' || type === 'multiple_choice') && /^\d+$/.test(answerStr)) {
    const idx = parseInt(answerStr)
    return ['A', 'B', 'C', 'D', 'E', 'F'][idx] || answerStr
  }
  
  return answerStr
}

// 清理选项文本（去除 A. B. C. D. 前缀）
const cleanOptionText = (opt: string) => {
  if (!opt) return ''
  // 去除开头的 A. B. C. D. 等前缀
  return opt.replace(/^[A-Fa-f][.．、]\s*/, '').trim()
}

const getScoreClass = (score: number) => {
  if (score >= 90) return 'text-green-600'
  if (score >= 80) return 'text-blue-600'
  if (score >= 70) return 'text-orange-600'
  return 'text-red-600'
}

// 删除作业
const deleteHomework = async (id: number) => {
  try {
    await homeworkApi.deleteHomework(id)
    homeworkList.value = homeworkList.value.filter(h => h.id !== id)
    ElMessage.success('删除成功')
  } catch (error) {
    console.error('删除作业失败', error)
  }
}

// 导出成绩
const exportScores = async (row: any) => {
  try {
    const res = await homeworkApi.exportScores(row.id)
    // 创建下载链接
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${row.title}_成绩.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('成绩导出成功')
  } catch (error) {
    console.error('导出成绩失败', error)
  }
}

const viewDetail = async (row: any) => {
  try {
    const detail = await homeworkApi.getHomeworkById(row.id)
    currentHomework.value = detail
    showDetailDialog.value = true
  } catch (error: any) {
    ElMessage.error('加载详情失败: ' + error.message)
  }
}

const currentHomework = ref<any>(null)
const showDetailDialog = ref(false)

const viewSubmissions = (row: any) => {
  router.push(`/teacher/correction?homeworkId=${row.id}`)
}

const handleCommand = async (cmd: string, row: any) => {
  if (cmd === 'edit') {
    ElMessage.info(`编辑 ${row.title}`)
  } else if (cmd === 'copy') {
    ElMessage.success(`已复制 ${row.title}`)
  } else if (cmd === 'export') {
    ElMessage.success('成绩导出成功')
  } else if (cmd === 'delete') {
    try {
      await ElMessageBox.confirm(`确定要删除 "${row.title}" 吗？`, '确认删除', { type: 'warning' })
      await deleteHomework(row.id)
    } catch {}
  }
}

// 加载班级列表
const loadClassList = async () => {
  try {
    const data = await classApi.getClassList(userStore.user?.id || 1)
    if (data) {
      classList.value = data.map((c: any) => ({
        label: c.name,
        value: c.name
      }))
    }
  } catch (error) {
    console.error('加载班级列表失败', error)
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
  loadHomeworkList()
  loadClassList()
})
</script>
