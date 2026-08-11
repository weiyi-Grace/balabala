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
            <h2 class="text-xl font-bold text-gray-800">AI 复核中心</h2>
            <p class="text-sm text-gray-500 mt-1">智能批改结果人工复核与修正</p>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <el-button type="primary" :loading="batchApproving" @click="approveBatch">
            <el-icon class="mr-1"><Check /></el-icon>
            批量通过
          </el-button>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-6">
      <!-- 左侧筛选 -->
      <div class="col-span-3">
        <div class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 sticky top-4">
          <h3 class="font-bold text-lg mb-4">筛选条件</h3>
          
          <div class="space-y-4">
            <div>
              <label class="text-sm text-gray-500 mb-2 block font-medium">复核状态</label>
              <el-radio-group v-model="filterStatus" class="flex flex-col gap-2 items-start">
                <el-radio label="all">全部</el-radio>
                <el-radio label="pending">待复核</el-radio>
                <el-radio label="approved">已通过</el-radio>
                <el-radio label="rejected">已驳回</el-radio>
              </el-radio-group>
            </div>
            
            <el-divider />
            
            <div>
              <label class="text-sm text-gray-500 mb-2 block font-medium">置信度</label>
              <el-checkbox-group v-model="filterConfidence" class="flex flex-col gap-2 items-start">
                <el-checkbox label="high">高置信度 (≥90%)</el-checkbox>
                <el-checkbox label="medium">中置信度 (70-90%)</el-checkbox>
                <el-checkbox label="low">低置信度 (<70%)</el-checkbox>
              </el-checkbox-group>
            </div>
            
            <el-divider />
            
            <div>
              <label class="text-sm text-gray-500 mb-2 block font-medium">题型</label>
              <el-checkbox-group v-model="filterTypes" class="flex flex-col gap-2 items-start">
                <el-checkbox label="objective">客观题</el-checkbox>
                <el-checkbox label="subjective">主观题</el-checkbox>
              </el-checkbox-group>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧复核列表 -->
      <div class="col-span-9">
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <div class="flex items-center justify-between mb-6">
            <div class="flex items-center gap-4">
              <h3 class="text-lg font-bold">待复核列表</h3>
              <el-tag type="warning" effect="light" round>12 待复核</el-tag>
            </div>
            <div class="flex items-center gap-3">
              <el-input v-model="searchQuery" placeholder="搜索学生或题目..." clearable style="width: 220px">
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
              <el-checkbox v-model="autoReview">自动高置信度通过</el-checkbox>
            </div>
          </div>

          <!-- 复核卡片列表 -->
          <div class="space-y-4">
            <div 
              v-for="item in filteredItems" 
              :key="item.id"
              class="border border-gray-100 rounded-xl p-5 hover:border-primary-200 hover:shadow-md transition-all"
              :class="{ 'ring-2 ring-primary-100 bg-primary-50/30': selectedItems.includes(item.id) }"
            >
              <!-- 头部信息 -->
              <div class="flex items-center justify-between mb-4">
                <div class="flex items-center gap-3">
                  <el-checkbox v-model="selectedItems" :label="item.id" class="mr-2" />
                  <el-avatar :size="32" class="bg-primary-100 text-primary-600 text-sm">
                    {{ item.studentName.charAt(0) }}
                  </el-avatar>
                  <div>
                    <span class="font-medium">{{ item.studentName }}</span>
                    <span class="text-gray-400 mx-2">|</span>
                    <span class="text-sm text-gray-500">{{ item.homeworkTitle }}</span>
                  </div>
                </div>
                <div class="flex items-center gap-2">
                  <el-tag 
                    :type="getConfidenceType(item.confidence)" 
                    effect="light"
                    round
                  >
                    置信度 {{ item.confidence }}%
                  </el-tag>
                  <el-tag 
                    :type="getStatusType(item.status)" 
                    effect="light"
                    round
                  >
                    {{ getStatusText(item.status) }}
                  </el-tag>
                </div>
              </div>

              <!-- 题目内容 -->
              <div class="bg-gray-50 rounded-lg p-4 mb-4">
                <div class="flex items-center gap-2 mb-2">
                  <span class="text-xs font-bold text-gray-500">题目 {{ item.questionNo }}</span>
                  <el-tag size="small" effect="light">{{ item.questionType }}</el-tag>
                </div>
                <p class="text-sm text-gray-800">{{ item.question }}</p>
              </div>

              <!-- 答案对比 -->
              <div class="grid grid-cols-2 gap-4 mb-4">
                <div class="bg-orange-50 rounded-lg p-4 border border-orange-100">
                  <div class="text-xs text-orange-600 mb-2 flex items-center gap-1">
                    <el-icon><User /></el-icon>
                    学生答案
                  </div>
                  <p class="text-sm text-gray-800">{{ item.studentAnswer }}</p>
                </div>
                <div class="bg-green-50 rounded-lg p-4 border border-green-100">
                  <div class="text-xs text-green-600 mb-2 flex items-center gap-1">
                    <el-icon><Check /></el-icon>
                    参考答案
                  </div>
                  <p class="text-sm text-gray-800">{{ item.correctAnswer }}</p>
                </div>
              </div>

              <!-- AI 评分详情 -->
              <div class="bg-blue-50 rounded-lg p-4 mb-4 border border-blue-100">
                <div class="flex items-center justify-between mb-3">
                  <div class="text-xs text-blue-600 flex items-center gap-1">
                    <el-icon><Cpu /></el-icon>
                    AI 评分详情
                  </div>
                  <div class="flex items-center gap-2">
                    <span class="text-sm text-gray-600">AI评分:</span>
                    <span class="text-lg font-bold" :class="item.aiScore >= item.fullScore * 0.6 ? 'text-green-600' : 'text-orange-600'">
                      {{ item.aiScore }}/{{ item.fullScore }}
                    </span>
                  </div>
                </div>
                <p class="text-sm text-gray-700 mb-3">{{ item.aiAnalysis }}</p>
                <div class="flex items-center gap-4 text-xs">
                  <div class="flex items-center gap-1">
                    <el-icon class="text-green-500"><CircleCheck /></el-icon>
                    <span>准确性: {{ item.dimensions.accuracy }}%</span>
                  </div>
                  <div class="flex items-center gap-1">
                    <el-icon class="text-blue-500"><TrendCharts /></el-icon>
                    <span>逻辑性: {{ item.dimensions.logic }}%</span>
                  </div>
                  <div class="flex items-center gap-1">
                    <el-icon class="text-purple-500"><Document /></el-icon>
                    <span>完整性: {{ item.dimensions.completeness }}%</span>
                  </div>
                </div>
              </div>

              <!-- 人工修正 -->
              <div class="flex items-center justify-between pt-4 border-t border-gray-100">
                <div class="flex items-center gap-4">
                  <div class="flex items-center gap-2">
                    <span class="text-sm text-gray-600">人工评分:</span>
                    <el-input-number 
                      v-model="item.humanScore" 
                      :min="0" 
                      :max="item.fullScore"
                      size="small"
                      style="width: 100px"
                    />
                    <span class="text-sm text-gray-400">/ {{ item.fullScore }}</span>
                  </div>
                  <el-input 
                    v-model="item.humanComment" 
                    placeholder="复核意见（可选）"
                    size="small"
                    style="width: 200px"
                  />
                </div>
                <div class="flex items-center gap-2">
                  <el-button size="small" type="danger" @click="reject(item)">
                    <el-icon class="mr-1"><Close /></el-icon>
                    驳回
                  </el-button>
                  <el-button size="small" type="success" @click="approve(item)">
                    <el-icon class="mr-1"><Check /></el-icon>
                    通过
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div class="flex justify-center mt-6">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ArrowLeft, 
  Check, 
  Close, 
  Search, 
  Cpu, 
  User, 
  CircleCheck, 
  TrendCharts, 
  Document 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { submissionApi, aiApi } from '@/api'
import { useUserStore } from '@/stores'

const router = useRouter()
const userStore = useUserStore()
const filterStatus = ref('all')
const filterConfidence = ref<string[]>(['high', 'medium', 'low'])
const filterTypes = ref<string[]>(['objective', 'subjective'])
const searchQuery = ref('')
const autoReview = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedItems = ref<number[]>([])
const batchApproving = ref(false)
const loading = ref(false)

// 复核数据
const reviewItems = ref<any[]>([])

// 筛选
const filteredItems = computed(() => {
  return reviewItems.value.filter(item => {
    if (filterStatus.value !== 'all' && item.status !== filterStatus.value) return false
    
    const confLevel = item.confidence >= 90 ? 'high' : item.confidence >= 70 ? 'medium' : 'low'
    if (!filterConfidence.value.includes(confLevel)) return false
    
    if (searchQuery.value && !item.studentName.includes(searchQuery.value) && !item.question.includes(searchQuery.value)) return false
    
    return true
  })
})

// 获取置信度类型
const getConfidenceType = (confidence: number) => {
  if (confidence >= 90) return 'success'
  if (confidence >= 70) return 'warning'
  return 'danger'
}

// 获取状态类型
const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    pending: '待复核',
    approved: '已通过',
    rejected: '已驳回'
  }
  return map[status] || status
}

// 加载待复核列表
const loadReviewItems = async () => {
  try {
    loading.value = true
    // 获取待复核的AI批改结果
    const data = await submissionApi.getPendingReviews({
      status: filterStatus.value,
      page: currentPage.value,
      size: pageSize.value
    })
    reviewItems.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    console.error('加载复核列表失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadReviewItems()
})
const approve = (item: any) => {
  item.status = 'approved'
  if (item.humanScore === null) {
    item.humanScore = item.aiScore
  }
  ElMessage.success('已通过 AI 评分')
}

// 驳回
const reject = (item: any) => {
  item.status = 'rejected'
  ElMessage.warning('已驳回，请人工评分')
}

// 批量通过
const approveBatch = async () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择要复核的项目')
    return
  }
  
  batchApproving.value = true
  setTimeout(() => {
    selectedItems.value.forEach(id => {
      const item = reviewItems.value.find(i => i.id === id)
      if (item) {
        item.status = 'approved'
        if (item.humanScore === null) {
          item.humanScore = item.aiScore
        }
      }
    })
    batchApproving.value = false
    selectedItems.value = []
    ElMessage.success('批量通过成功！')
  }, 1000)
}
</script>
