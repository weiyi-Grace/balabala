<template>
  <div class="space-y-6 animate-fade-in">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-4 gap-4">
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 bg-orange-100 rounded-xl flex items-center justify-center">
            <el-icon :size="24" class="text-orange-500"><Clock /></el-icon>
          </div>
          <div>
            <div class="text-sm text-gray-500">待复核</div>
            <div class="text-2xl font-bold text-orange-600">{{ stats.pending }}</div>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center">
            <el-icon :size="24" class="text-green-500"><CircleCheck /></el-icon>
          </div>
          <div>
            <div class="text-sm text-gray-500">AI已批</div>
            <div class="text-2xl font-bold text-green-600">{{ stats.aiDone }}</div>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center">
            <el-icon :size="24" class="text-blue-500"><Checked /></el-icon>
          </div>
          <div>
            <div class="text-sm text-gray-500">已完成</div>
            <div class="text-2xl font-bold text-blue-600">{{ stats.done }}</div>
          </div>
        </div>
      </div>
      <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 bg-purple-100 rounded-xl flex items-center justify-center">
            <el-icon :size="24" class="text-purple-500"><TrendCharts /></el-icon>
          </div>
          <div>
            <div class="text-sm text-gray-500">平均用时</div>
            <div class="text-2xl font-bold text-purple-600">3.2min</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 批改列表 -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center gap-4">
          <h2 class="text-xl font-bold">批改中心</h2>
          <el-radio-group v-model="filter" size="small">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="pending">待复核</el-radio-button>
            <el-radio-button label="ai">AI预批</el-radio-button>
            <el-radio-button label="done">已完成</el-radio-button>
          </el-radio-group>
        </div>
        <div class="flex gap-3">
          <el-input v-model="searchQuery" placeholder="搜索学生或作业..." clearable style="width: 200px">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-dropdown @command="handleBatchAction" v-if="selectedRows.length > 0">
            <el-button type="primary">
              批量操作 ({{ selectedRows.length }})<el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="ai">AI批量批改</el-dropdown-item>
                <el-dropdown-item command="approve">批量通过</el-dropdown-item>
                <el-dropdown-item command="export">导入选中</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <el-table 
        :data="paginatedSubmissions" 
        style="width: 100%" 
        :header-cell-style="{ background: '#f9fafb' }"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="学生" width="150">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-avatar :size="32" :src="row.avatar" />
              <div>
                <div class="font-medium">{{ row.student }}</div>
                <div class="text-xs text-gray-400">{{ row.class }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="homework" label="作业" min-width="200">
          <template #default="{ row }">
            <div class="font-medium">{{ row.homework }}</div>
            <div class="text-xs text-gray-400">{{ row.questionCount }} 题 | 提交于 {{ row.submitTime }}</div>
          </template>
        </el-table-column>
        <el-table-column label="AI评分" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.aiScore" class="text-lg font-bold" :class="getScoreClass(row.aiScore)">{{ row.aiScore }}</span>
            <span v-else class="text-gray-400">-</span>
          </template>
        </el-table-column>
        <el-table-column label="置信度" width="120" align="center">
          <template #default="{ row }">
            <div v-if="row.aiConfidence">
              <el-progress :percentage="row.aiConfidence" :color="getConfidenceColor(row.aiConfidence)" :show-text="false" :stroke-width="6" />
              <span class="text-xs" :class="getConfidenceTextClass(row.aiConfidence)">{{ row.aiConfidence }}%</span>
            </div>
            <span v-else class="text-gray-400 text-sm">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light" round>
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === '待复核'" type="primary" size="small" round @click="review(row)">复核</el-button>
            <el-button v-else-if="row.status === 'AI已批'" type="success" size="small" plain round @click="approve(row)">确认</el-button>
            <el-button v-else type="info" size="small" text @click="viewDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="flex justify-center mt-6">
        <el-pagination 
          v-model:current-page="currentPage" 
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="filteredSubmissions.length" 
          background 
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- AI 批改设置对话框 -->
    <el-dialog v-model="aiDialogVisible" title="AI 批量批改设置" width="500px">
      <el-form label-position="top">
        <el-form-item label="批改严格程度">
          <el-slider v-model="aiSettings.strictness" :max="100" show-stops :marks="{0: '宽松', 50: '标准', 100: '严格'}" />
        </el-form-item>
        <el-form-item label="评分维度">
          <el-checkbox-group v-model="aiSettings.dimensions" class="flex flex-col gap-2">
            <el-checkbox label="accuracy">答案准确性</el-checkbox>
            <el-checkbox label="logic">逻辑推导</el-checkbox>
            <el-checkbox label="process">解题过程</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="aiSettings.generateComment">生成 AI 评语</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="aiDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="aiProcessing" @click="startAIBatch">开始 AI 批改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Clock, CircleCheck, Checked, TrendCharts, Search, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const filter = ref('all')
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)
const selectedRows = ref<any[]>([])
const aiDialogVisible = ref(false)
const aiProcessing = ref(false)

const aiSettings = ref({
  strictness: 70,
  dimensions: ['accuracy', 'logic', 'process'],
  generateComment: true
})

const submissions = ref([
  { id: 1, student: '张三', class: '高二1班', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=1', homework: '一元二次方程专项练习', questionCount: 5, submitTime: '2026-02-27 14:20', aiScore: 85, aiConfidence: 92, status: '待复核' },
  { id: 2, student: '李四', class: '高二1班', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=2', homework: '一元二次方程专项练习', questionCount: 5, submitTime: '2026-02-27 15:30', aiScore: 92, aiConfidence: 95, status: 'AI已批' },
  { id: 3, student: '王五', class: '高二3班', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=3', homework: '函数导数应用题', questionCount: 3, submitTime: '2026-02-27 16:10', aiScore: null, aiConfidence: null, status: '待批改' },
  { id: 4, student: '赵六', class: '高二3班', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=4', homework: '概率统计基础', questionCount: 8, submitTime: '2026-02-27 17:00', aiScore: 78, aiConfidence: 75, status: '待复核' },
  { id: 5, student: '孙七', class: '高二5班', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=5', homework: '一元二次方程专项练习', questionCount: 5, submitTime: '2026-02-27 18:30', aiScore: 88, aiConfidence: 88, status: '已完成' }
])

const stats = computed(() => {
  const pending = submissions.value.filter(s => s.status === '待复核').length
  const aiDone = submissions.value.filter(s => s.status === 'AI已批').length
  const done = submissions.value.filter(s => s.status === '已完成').length
  return { pending, aiDone, done }
})

const filteredSubmissions = computed(() => {
  return submissions.value.filter(s => {
    if (searchQuery.value && !s.student.includes(searchQuery.value) && !s.homework.includes(searchQuery.value)) return false
    if (filter.value === 'all') return true
    if (filter.value === 'pending') return s.status === '待复核'
    if (filter.value === 'ai') return s.status === 'AI已批'
    if (filter.value === 'done') return s.status === '已完成'
    return true
  })
})

// 分页后的提交列表
const paginatedSubmissions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredSubmissions.value.slice(start, end)
})

const getScoreClass = (score: number) => {
  if (score >= 90) return 'text-green-600'
  if (score >= 70) return 'text-blue-600'
  return 'text-orange-600'
}

const getConfidenceColor = (confidence: number) => {
  if (confidence >= 90) return '#22c55e'
  if (confidence >= 70) return '#f59e0b'
  return '#ef4444'
}

const getConfidenceTextClass = (confidence: number) => {
  if (confidence >= 90) return 'text-green-600'
  if (confidence >= 70) return 'text-orange-600'
  return 'text-red-600'
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = { '待复核': 'warning', 'AI已批': 'success', '待批改': 'info', '已完成': '' }
  return map[status] || 'info'
}

const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

const handleBatchAction = (command: string) => {
  if (command === 'ai') {
    aiDialogVisible.value = true
  } else if (command === 'approve') {
    ElMessage.success(`已批量通过 ${selectedRows.value.length} 份作业`)
  } else if (command === 'export') {
    ElMessage.success('已导出选中的作业')
  }
}

const startAIBatch = () => {
  aiProcessing.value = true
  setTimeout(() => {
    aiProcessing.value = false
    aiDialogVisible.value = false
    ElMessage.success(`AI 已完成 ${selectedRows.value.length} 份作业批改`)
  }, 2000)
}

const review = (row: any) => {
  ElMessage.info(`正在复核 ${row.student} 的作业`)
}

const approve = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确认通过 ${row.student} 的 AI 评分 ${row.aiScore} 分？`,
      '确认通过',
      { confirmButtonText: '确认', cancelButtonText: '再检查', type: 'info' }
    )
    row.status = '已完成'
    ElMessage.success('已确认通过')
  } catch {}
}

const viewDetail = (row: any) => {
  ElMessage.info(`查看 ${row.student} 的作业详情`)
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
}

onMounted(() => {
  loading.value = true
  setTimeout(() => loading.value = false, 500)
})
</script>
