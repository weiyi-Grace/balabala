<template>
  <div class="space-y-6 animate-fade-in">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-4 gap-4">
      <div v-for="stat in stats" :key="stat.label" 
           class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 rounded-xl flex items-center justify-center" :class="stat.bg">
            <el-icon :size="24" :class="stat.iconColor"><component :is="stat.icon"/></el-icon>
          </div>
          <div>
            <div class="text-sm text-gray-500">{{ stat.label }}</div>
            <div class="text-2xl font-bold text-gray-800">{{ stat.value }}</div>
            <div v-if="stat.change" class="text-xs mt-1" :class="stat.changeType === 'up' ? 'text-green-500' : 'text-red-500'">
              {{ stat.changeType === 'up' ? '↑' : '↓' }} {{ stat.change }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-6">
      <!-- 左侧主要内容 -->
      <div class="col-span-8 space-y-6">
        <!-- 班级作业概况 -->
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold flex items-center gap-2">
              <el-icon class="text-primary-500"><School /></el-icon>
              授课班级概况
            </h3>
            <el-button type="primary" text @click="router.push('/teacher/homework/list')">管理作业</el-button>
          </div>
          <el-table :data="classes" style="width: 100%" :header-cell-style="{ background: '#f9fafb' }">
            <el-table-column label="班级" min-width="120">
              <template #default="{ row }">
                <div class="flex items-center gap-2">
                  <el-avatar :size="32" class="bg-primary-100 text-primary-600">{{ row.name.charAt(2) }}</el-avatar>
                  <span class="font-medium">{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="students" label="学生数" width="100" align="center" />
            <el-table-column prop="avgScore" label="平均分" width="100" align="center">
              <template #default="{ row }">
                <span class="font-bold" :class="row.avgScore >= 85 ? 'text-green-600' : row.avgScore >= 70 ? 'text-blue-600' : 'text-orange-600'">
                  {{ row.avgScore }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="作业完成率" width="180" align="center">
              <template #default="{ row }">
                <el-progress :percentage="row.completion" :color="row.completion >= 90 ? '#22c55e' : '#f59e0b'" />
              </template>
            </el-table-column>
            <el-table-column label="待批改" width="120" align="center">
              <template #default="{ row }">
                <div v-if="row.pending > 0" class="flex items-center justify-center">
                  <el-button size="small" type="primary" @click="goCorrection" class="relative !pr-6">
                    去批改
                    <span 
                      class="absolute top-0 right-0 -mt-1 -mr-1 bg-red-500 text-white text-[10px] font-bold rounded-full w-4 h-4 flex items-center justify-center"
                    >
                      {{ row.pending > 9 ? '9+' : row.pending }}
                    </span>
                  </el-button>
                </div>
                <span v-else class="text-gray-400 text-sm">-</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 右侧边栏 -->
      <div class="col-span-4 space-y-6">
        <!-- 快捷操作 -->
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h3 class="font-bold mb-4">快捷操作</h3>
          <div class="grid grid-cols-2 gap-3">
            <div class="p-4 bg-primary-50 rounded-xl text-center cursor-pointer hover:bg-primary-100 transition-colors" @click="router.push('/teacher/homework/create')">
              <el-icon class="text-primary-600 mb-2" :size="24"><EditPen /></el-icon>
              <div class="text-sm text-primary-700 font-medium">发布作业</div>
            </div>
            <div class="p-4 bg-blue-50 rounded-xl text-center cursor-pointer hover:bg-blue-100 transition-colors" @click="router.push('/teacher/correction')">
              <el-icon class="text-blue-600 mb-2" :size="24"><Check /></el-icon>
              <div class="text-sm text-blue-700 font-medium">批改作业</div>
            </div>
            <div class="p-4 bg-purple-50 rounded-xl text-center cursor-pointer hover:bg-purple-100 transition-colors" @click="router.push('/teacher/analytics')">
              <el-icon class="text-purple-600 mb-2" :size="24"><TrendCharts /></el-icon>
              <div class="text-sm text-purple-700 font-medium">班级分析</div>
            </div>
            <div class="p-4 bg-orange-50 rounded-xl text-center cursor-pointer hover:bg-orange-100 transition-colors" @click="openNotifyDialog">
              <el-icon class="text-orange-600 mb-2" :size="24"><Message /></el-icon>
              <div class="text-sm text-orange-700 font-medium">通知学生</div>
            </div>
          </div>
        </div>

        <!-- 最近动态 -->
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h3 class="font-bold mb-4">最近动态</h3>
          <div v-if="activitiesLoading" class="py-4 text-center text-gray-400">
            <el-icon class="animate-spin"><Loading /></el-icon> 加载中...
          </div>
          <div v-else-if="activities.length === 0" class="py-4 text-center text-gray-400">
            暂无动态
          </div>
          <div v-else class="space-y-3">
            <div v-for="(activity, idx) in activities" :key="idx" class="flex gap-3 text-sm">
              <div class="w-2 h-2 rounded-full mt-1.5" :class="activity.color || 'bg-gray-400'"></div>
              <div class="flex-1">
                <p class="text-gray-700">{{ activity.content || activity.text }}</p>
                <span class="text-xs text-gray-400">{{ activity.time }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 通知学生对话框 -->
    <el-dialog v-model="showNotifyDialog" title="通知学生" width="500px">
      <el-form label-position="top">
        <el-form-item label="选择班级">
          <el-select v-model="notifyForm.classId" placeholder="选择要通知的班级" class="w-full">
            <el-option 
              v-for="cls in classes" 
              :key="cls.id" 
              :label="cls.name" 
              :value="cls.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="通知类型">
          <el-radio-group v-model="notifyForm.type">
            <el-radio-button label="HOMEWORK">作业提醒</el-radio-button>
            <el-radio-button label="EXAM">考试提醒</el-radio-button>
            <el-radio-button label="NOTICE">一般通知</el-radio-button>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="通知内容">
          <el-input 
            v-model="notifyForm.content" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入要通知的内容..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showNotifyDialog = false">取消</el-button>
        <el-button type="primary" :loading="notifyLoading" @click="sendNotification">
          发送通知
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Files, CircleCheck, TrendCharts, EditPen, Check, Message, School, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { classApi, homeworkApi, dashboardApi } from '@/api'
import { useUserStore } from '@/stores'

const router = useRouter()
const userStore = useUserStore()

// 统计数据
const stats = ref([
  { label: '授课学生', value: '0', icon: User, bg: 'bg-green-50', iconColor: 'text-green-600', change: '0', changeType: 'up' },
  { label: '本周作业', value: '0', icon: Files, bg: 'bg-blue-50', iconColor: 'text-blue-600', change: '0', changeType: 'up' },
  { label: 'AI批改', value: '0', icon: CircleCheck, bg: 'bg-purple-50', iconColor: 'text-purple-600', change: '0', changeType: 'up' },
  { label: '平均正确率', value: '0%', icon: TrendCharts, bg: 'bg-orange-50', iconColor: 'text-orange-600', change: '0', changeType: 'up' }
])

const classes = ref<any[]>([])
const activities = ref<any[]>([])
const activitiesLoading = ref(false)

// 通知学生
const showNotifyDialog = ref(false)
const notifyLoading = ref(false)
const notifyForm = ref({
  classId: '',
  type: 'HOMEWORK',
  content: ''
})

// 加载数据
const loadData = async () => {
  if (!userStore.user) return
  
  try {
    const teacherId = userStore.user.id
    
    // 并行加载数据
    const [classList, teacherDashboard] = await Promise.all([
      classApi.getClassList(teacherId),
      dashboardApi.getTeacherDashboard(teacherId)
    ])
    
    // 处理班级数据
    classes.value = await Promise.all(classList.map(async (cls: any) => {
      const students = await classApi.getClassStudents(cls.id)
      return {
        id: cls.id,
        name: cls.name,
        students: students.length,
        avgScore: 85,
        completion: 90,
        pending: 0
      }
    }))
    
    // 更新统计数据
    const totalStudents = classes.value.reduce((sum, c) => sum + c.students, 0)
    stats.value[0].value = totalStudents.toString()
    
    // 获取作业列表
    const homeworks = await homeworkApi.getTeacherHomeworks(teacherId)
    stats.value[1].value = homeworks.length.toString()
    
    // 加载最近动态
    await loadActivities(teacherId)
    
  } catch (error: any) {
    ElMessage.error('加载数据失败: ' + error.message)
  }
}

// 加载最近动态
const loadActivities = async (teacherId: number) => {
  activitiesLoading.value = true
  try {
    const res = await dashboardApi.getRecentActivities(teacherId)
    activities.value = res || []
  } catch (error: any) {
    console.error('加载动态失败:', error)
    activities.value = []
  } finally {
    activitiesLoading.value = false
  }
}

// 打开通知对话框
const openNotifyDialog = () => {
  showNotifyDialog.value = true
  notifyForm.value = { classId: '', type: 'HOMEWORK', content: '' }
}

// 发送通知
const sendNotification = async () => {
  if (!notifyForm.value.content.trim()) {
    ElMessage.warning('请输入通知内容')
    return
  }
  if (!notifyForm.value.classId) {
    ElMessage.warning('请选择班级')
    return
  }
  
  notifyLoading.value = true
  try {
    await dashboardApi.sendNotification({
      title: '',
      content: notifyForm.value.content,
      type: notifyForm.value.type,
      senderId: userStore.user?.id,
      classId: notifyForm.value.classId
    })
    
    ElMessage.success('通知已发送')
    showNotifyDialog.value = false
  } catch (error: any) {
    ElMessage.error('发送失败: ' + error.message)
  } finally {
    notifyLoading.value = false
  }
}

const goCorrection = () => {
  router.push('/teacher/correction')
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
:deep(.el-table .el-table__body-wrapper .el-scrollbar__view) {
  overflow: visible !important;
}
:deep(.el-table .el-table__row) {
  position: relative;
}
:deep(.el-table .el-table__cell) {
  overflow: visible !important;
  padding-top: 16px !important;
  padding-bottom: 16px !important;
}
:deep(.el-table) {
  overflow: visible;
}
</style>
