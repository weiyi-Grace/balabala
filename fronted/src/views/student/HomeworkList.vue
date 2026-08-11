<template>
  <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 animate-fade-in">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-bold">我的作业</h2>
      <div class="flex gap-3">
        <el-input v-model="searchQuery" placeholder="搜索作业..." clearable style="width: 200px">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="filterSubject" placeholder="学科" clearable style="width: 120px">
          <el-option label="全部学科" value="" />
          <el-option label="数学" value="数学" />
          <el-option label="语文" value="语文" />
          <el-option label="英语" value="英语" />
          <el-option label="物理" value="物理" />
          <el-option label="化学" value="化学" />
        </el-select>
        <el-radio-group v-model="filterStatus">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="pending">待完成</el-radio-button>
          <el-radio-button label="correcting">批改中</el-radio-button>
          <el-radio-button label="completed">已完成</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <el-table :data="paginatedList" style="width: 100%" :header-cell-style="{ background: '#f9fafb' }" v-loading="loading" @row-click="handleRowClick">
      <template #empty>
        <div class="flex flex-col items-center justify-center py-12">
          <el-icon class="text-6xl text-gray-300 mb-4"><Document /></el-icon>
          <div class="text-gray-500 text-lg">暂无作业数据</div>
          <div class="text-gray-400 text-sm mt-1">当前班级还没有发布作业</div>
        </div>
      </template>
      <el-table-column label="作业信息" min-width="300">
        <template #default="{ row }">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg flex items-center justify-center text-white font-bold"
                 :style="{ background: row.color }">
              {{ row.subject.charAt(0) }}
            </div>
            <div>
              <div class="font-medium text-gray-800">{{ row.title }}</div>
              <div class="text-xs text-gray-500">{{ row.subject }} | 发布于 {{ row.publishDate }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="截止时间" width="150">
        <template #default="{ row }">
          <div :class="isUrgent(row.deadline) ? 'text-red-500 font-medium' : 'text-gray-600'">
            {{ row.deadline }}
            <el-tag v-if="isUrgent(row.deadline)" type="danger" size="small" class="ml-1">即将截止</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" effect="light" round>
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <!-- 得分列已隐藏 - 作业不需要分数 -->
      <el-table-column label="操作" width="150" align="center">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="primary" size="small" round @click="goSubmit(row.id)">去完成</el-button>
          <el-button v-else-if="row.status === 1" type="warning" size="small" plain round @click="goDetail(row.id)">查看详情</el-button>
          <el-button v-else type="success" size="small" plain round @click="goDetail(row.id)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="flex justify-center mt-6">
      <el-pagination 
        v-model:current-page="currentPage" 
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total" 
        background 
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { homeworkApi, submissionApi, userApi } from '@/api'
import { useUserStore } from '@/stores'
import { correctionWebSocket } from '@/services/websocket'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const searchQuery = ref('')
const filterSubject = ref('')
const filterStatus = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const homeworkList = ref<any[]>([])

// 加载作业列表
const loadHomeworks = async () => {
  // 如果没有班级信息，先获取完整用户信息
  let user = userStore.user
  if (!user?.classInfo && !user?.classId) {
    try {
      const fullUser = await userApi.getUserById(user!.id)
      console.log('获取完整用户:', fullUser)
      userStore.setUser({ ...user, ...fullUser })
      user = userStore.user
    } catch (error) {
      console.error('获取用户信息失败', error)
    }
  }
  
  const classId = user?.classInfo?.id || user?.classId
  console.log('班级ID:', classId)
  if (!classId) {
    console.warn('未获取到班级ID')
    return
  }
  
  loading.value = true
  try {
    // 获取班级作业列表
    const homeworks = await homeworkApi.getClassHomeworks(classId)
    console.log('获取到的作业:', homeworks)
    
    // 获取学生的提交记录
    const submissions = await submissionApi.getSubmissionsByStudent(user!.id)
    console.log('获取到的提交记录:', submissions)
    
    // 合并作业和提交状态
    homeworkList.value = homeworks.map((hw: any) => {
      const submission = submissions.find((s: any) => s.homework?.id === hw.id || s.homeworkId === hw.id)
      return {
        id: hw.id,
        title: hw.title,
        subject: getSubjectName(hw.subject),
        classId: hw.classId,
        publishDate: hw.createdTime?.split('T')[0] || '',
        deadline: hw.deadline?.split('T')[0] || '',
        status: submission?.status ?? 0,
        score: submission?.totalScore,
        color: getSubjectColor(hw.subject)
      }
    })
    
    total.value = homeworkList.value.length
  } catch (error: any) {
    ElMessage.error('加载作业失败: ' + error.message)
  } finally {
    loading.value = false
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

const getSubjectColor = (subject: string) => {
  const map: Record<string, string> = {
    chinese: '#ef4444',
    math: '#22c55e',
    english: '#3b82f6',
    physics: '#a855f7',
    chemistry: '#f59e0b'
  }
  return map[subject] || '#64748b'
}

// 只显示本班作业
const filteredList = computed(() => {
  return homeworkList.value.filter(h => {
    if (searchQuery.value && !h.title.includes(searchQuery.value)) return false
    if (filterSubject.value && h.subject !== filterSubject.value) return false
    if (filterStatus.value === 'all') return true
    if (filterStatus.value === 'pending') return h.status === 0
    if (filterStatus.value === 'correcting') return h.status === 1
    if (filterStatus.value === 'completed') return h.status === 2
    return true
  })
})

// 分页后的列表
const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredList.value.slice(start, end)
})

const isUrgent = (deadline: string) => {
  const days = Math.ceil((new Date(deadline).getTime() - Date.now()) / (1000 * 60 * 60 * 24))
  return days <= 2 && days >= 0
}

const getStatusType = (status: number) => status === 0 ? 'info' : status === 1 ? 'warning' : 'success'
const getStatusText = (status: number) => status === 0 ? '待完成' : status === 1 ? '批改中' : '已完成'

const getScoreClass = (score: number) => {
  if (score >= 90) return 'text-green-600'
  if (score >= 60) return 'text-blue-600'
  return 'text-red-600'
}

const goSubmit = (id: number) => {
  router.push(`/student/homework/submit/${id}`)
}

const goDetail = (homeworkId: number) => {
  // 跳转到批改详情页，传递 homeworkId 和 studentId
  const studentId = userStore.user?.id
  router.push({
    path: '/student/correction-detail',
    query: { homeworkId, studentId }
  })
}

// 处理行点击
const handleRowClick = (row: any) => {
  if (row.status === 0) {
    goSubmit(row.id)
  } else {
    goDetail(row.id)
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
  loadHomeworks()

  // 监听AI批改完成通知，自动刷新作业列表
  correctionWebSocket.on('CORRECTION_COMPLETE', (data: any) => {
    console.log('学生端收到批改完成通知:', data)
    ElMessage.success('作业批改完成，自动刷新列表')
    loadHomeworks()
  })

  // 监听学生专用的批改完成通知
  correctionWebSocket.on('CORRECTION_COMPLETE_STUDENT', (data: any) => {
    console.log('学生端收到批改完成通知(CORRECTION_COMPLETE_STUDENT):', data)
    ElMessage.success('作业批改完成，自动刷新列表')
    loadHomeworks()
  })
})

onUnmounted(() => {
  // 移除监听器
  correctionWebSocket.off('CORRECTION_COMPLETE', () => {})
})
</script>
