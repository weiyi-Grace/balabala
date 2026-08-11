<template>
  <div class="max-w-7xl mx-auto animate-fade-in">
    <!-- 顶部导航栏 -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 mb-6">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 bg-primary-100 rounded-xl flex items-center justify-center">
            <el-icon :size="24" class="text-primary-600"><User /></el-icon>
          </div>
          <div>
            <h2 class="text-xl font-bold text-gray-800">班级申请审批</h2>
            <p class="text-sm text-gray-500 mt-1">管理学生转入班级的申请</p>
          </div>
        </div>
        <el-tag type="warning" size="large" v-if="pendingCount > 0">
          待审批 {{ pendingCount }} 条
        </el-tag>
      </div>
    </div>

    <!-- 申请列表 -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
      <el-table :data="requests" v-loading="loading" style="width: 100%">
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="fromClassName" label="原班级" width="150">
          <template #default="{ row }">
            <span v-if="row.fromClassName">{{ row.fromClassName }}</span>
            <el-tag v-else size="small" type="info">未分配</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="toClassName" label="目标班级" width="150" />
        <el-table-column prop="reason" label="申请理由" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div v-if="row.status === 0" class="flex gap-2">
              <el-button type="success" size="small" @click="handleApprove(row, true)">
                同意
              </el-button>
              <el-button type="danger" size="small" @click="handleApprove(row, false)">
                拒绝
              </el-button>
            </div>
            <span v-else class="text-gray-400 text-sm">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty v-if="requests.length === 0 && !loading" description="暂无转班申请" />
    </div>

    <!-- 审批备注对话框 -->
    <el-dialog v-model="showRemarkDialog" title="审批备注" width="400px">
      <el-input
        v-model="approvalRemark"
        type="textarea"
        :rows="3"
        placeholder="请输入审批备注（可选）"
      />
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="showRemarkDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmApprove">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { classTransferApi } from '@/api'
import { useUserStore } from '@/stores'

const userStore = useUserStore()
const loading = ref(false)
const requests = ref<any[]>([])
const showRemarkDialog = ref(false)
const approvalRemark = ref('')
const currentRequest = ref<any>(null)
const isApproved = ref(false)

const pendingCount = computed(() => {
  return requests.value.filter(r => r.status === 0).length
})

// 加载申请列表
const loadRequests = async () => {
  if (!userStore.user) return
  loading.value = true
  try {
    const data = await classTransferApi.getPendingRequests(userStore.user.id)
    requests.value = data
  } catch (error: any) {
    ElMessage.error('加载申请列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 状态显示
const getStatusType = (status: number) => {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '待审批', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 处理审批
const handleApprove = (row: any, approved: boolean) => {
  currentRequest.value = row
  isApproved.value = approved
  approvalRemark.value = ''
  showRemarkDialog.value = true
}

// 确认审批
const confirmApprove = async () => {
  if (!currentRequest.value || !userStore.user) return

  try {
    await classTransferApi.approveRequest(
      currentRequest.value.id,
      userStore.user.id,
      isApproved.value,
      approvalRemark.value
    )
    ElMessage.success(isApproved.value ? '已同意申请' : '已拒绝申请')
    showRemarkDialog.value = false
    await loadRequests()
  } catch (error: any) {
    ElMessage.error('审批失败: ' + error.message)
  }
}

onMounted(() => {
  loadRequests()
})
</script>
