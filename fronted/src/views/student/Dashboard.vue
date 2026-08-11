<template>
  <div class="space-y-6 animate-fade-in">
    <!-- 欢迎栏 -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 hover-card">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <el-avatar :size="64" :src="userStore.user?.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=Student'" class="border-2 border-primary-200" />
          <div>
            <h2 class="text-xl font-bold text-gray-800">下午好，{{ userStore.user?.realName || '同学' }}！ 👋</h2>
            <p class="text-sm text-gray-500 mt-1">今天是你坚持学习的第 <span class="text-primary-600 font-bold">{{ stats.studyDays }}</span> 天</p>
            <div class="flex gap-2 mt-2">
              <el-tag type="success" effect="light" round>{{ classInfo }}</el-tag>
              <el-tag v-if="stats.pendingHomework > 0" type="warning" effect="light" round>待完成作业 {{ stats.pendingHomework }}</el-tag>
              <el-tag v-if="stats.checkedInToday" type="primary" effect="light" round>今日已打卡 ✓</el-tag>
            </div>
          </div>
        </div>
        <div class="flex gap-4 text-center">
          <div class="px-4 py-2 bg-primary-50 rounded-xl">
            <div class="text-2xl font-bold text-primary-600">{{ stats.totalHomework }}</div>
            <div class="text-xs text-gray-500">累计作业</div>
          </div>
          <div class="px-4 py-2 bg-blue-50 rounded-xl">
            <div class="text-2xl font-bold text-blue-600">{{ stats.avgCorrectRate }}%</div>
            <div class="text-xs text-gray-500">平均正确率</div>
          </div>
          <div class="px-4 py-2 bg-purple-50 rounded-xl">
            <div class="text-2xl font-bold text-purple-600">{{ stats.streakDays }}</div>
            <div class="text-xs text-gray-500">连续打卡</div>
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-6">
      <!-- 作业列表 -->
      <div class="col-span-8 space-y-6">
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-lg font-bold flex items-center gap-2">
              <el-icon class="text-primary-500"><Notebook /></el-icon>
              近期作业
            </h3>
            <div class="flex gap-2">
              <el-radio-group v-model="filter" size="small">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="pending">待完成</el-radio-button>
                <el-radio-button label="completed">已完成</el-radio-button>
              </el-radio-group>
              <el-button type="primary" text @click="router.push('/student/homework')">查看更多</el-button>
            </div>
          </div>

          <div class="space-y-3">
            <!-- 未加入班级提示 -->
            <div v-if="!userStore.user?.classInfo?.id" class="p-4 rounded-xl border border-orange-200 bg-orange-50">
              <div class="flex items-center gap-3">
                <el-icon class="text-orange-500" :size="20"><Warning /></el-icon>
                <div class="flex-1">
                  <p class="text-sm text-orange-700 font-medium">您还未加入班级</p>
                  <p class="text-xs text-orange-600 mt-1">加入班级后才能查看和完成作业</p>
                </div>
              </div>
              <div class="flex gap-2 mt-3">
                <el-input 
                  v-model="inviteCode" 
                  placeholder="请输入班级邀请码" 
                  size="small"
                  maxlength="20"
                  clearable
                />
                <el-button type="primary" size="small" :loading="joining" @click="handleJoinClass">
                  加入班级
                </el-button>
              </div>
            </div>
            
            <div v-if="filteredHomework.length === 0 && userStore.user?.classInfo?.id" class="text-center py-8 text-gray-400">
              <el-icon :size="48" class="mb-2"><Notebook /></el-icon>
              <p>暂无作业</p>
              <p v-if="!userStore.user?.classInfo?.id" class="text-sm mt-1">请加入班级后查看作业</p>
            </div>
            <div v-for="hw in filteredHomework" :key="hw.id" 
                 class="group p-4 rounded-xl border border-gray-100 hover:border-primary-200 hover:shadow-md transition-all cursor-pointer"
                 @click="handleHomeworkClick(hw)">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-4">
                  <div class="w-12 h-12 rounded-xl flex items-center justify-center text-white font-bold text-lg"
                       :style="{ background: hw.color }">
                    {{ hw.subject?.charAt(0) || '?' }}
                  </div>
                  <div>
                    <h4 class="font-semibold text-gray-800 group-hover:text-primary-600 transition-colors">{{ hw.title }}</h4>
                    <div class="flex items-center gap-2 mt-1 text-xs text-gray-500">
                      <span>{{ hw.subject }}</span>
                      <span class="w-1 h-1 rounded-full bg-gray-300"></span>
                      <span>截止: {{ hw.deadline }}</span>
                    </div>
                  </div>
                </div>
                <div class="flex items-center gap-3">
                  <el-tag :type="getStatusType(hw.status)" effect="light" round>
                    {{ getStatusText(hw.status) }}
                  </el-tag>
                  <el-button v-if="hw.status === 0" type="primary" size="small" round>去完成</el-button>
                  <el-button v-else-if="hw.status === 2" type="success" size="small" plain round>查看批改</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 侧边栏 -->
      <div class="col-span-4 space-y-6">
        <!-- 能力雷达图 -->
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h3 class="font-bold mb-4 flex items-center gap-2">
            <el-icon class="text-blue-500"><DataAnalysis /></el-icon>
            能力分析
          </h3>
          <div ref="radarChart" class="h-48"></div>
          <div class="mt-4 grid grid-cols-2 gap-3">
            <div class="p-3 rounded-lg" :class="{
              'bg-red-50': abilityData.weakPoint.level === 'danger',
              'bg-orange-50': abilityData.weakPoint.level === 'warning',
              'bg-gray-50': abilityData.weakPoint.level === 'normal'
            }">
              <div class="text-xs" :class="{
                'text-red-400': abilityData.weakPoint.level === 'danger',
                'text-orange-400': abilityData.weakPoint.level === 'warning',
                'text-gray-400': abilityData.weakPoint.level === 'normal'
              }">薄弱点</div>
              <div class="text-sm font-bold" :class="{
                'text-red-600': abilityData.weakPoint.level === 'danger',
                'text-orange-600': abilityData.weakPoint.level === 'warning',
                'text-gray-600': abilityData.weakPoint.level === 'normal'
              }">{{ abilityData.weakPoint.name }}</div>
            </div>
            <div class="p-3 rounded-lg" :class="{
              'bg-green-50': abilityData.strongPoint.level === 'success',
              'bg-blue-50': abilityData.strongPoint.level === 'warning',
              'bg-gray-50': abilityData.strongPoint.level === 'normal'
            }">
              <div class="text-xs" :class="{
                'text-green-400': abilityData.strongPoint.level === 'success',
                'text-blue-400': abilityData.strongPoint.level === 'warning',
                'text-gray-400': abilityData.strongPoint.level === 'normal'
              }">优势项</div>
              <div class="text-sm font-bold" :class="{
                'text-green-600': abilityData.strongPoint.level === 'success',
                'text-blue-600': abilityData.strongPoint.level === 'warning',
                'text-gray-600': abilityData.strongPoint.level === 'normal'
              }">{{ abilityData.strongPoint.name }}</div>
            </div>
          </div>
        </div>

        <!-- 学情简报 -->
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h3 class="font-bold mb-4">学情简报</h3>
          <div class="space-y-3">
            <div v-for="(brief, idx) in briefings" :key="idx" 
                 class="flex gap-3 p-3 rounded-lg"
                 :class="brief.type === 'warning' ? 'bg-orange-50' : 'bg-green-50'">
              <el-icon class="mt-0.5" :class="brief.type === 'warning' ? 'text-orange-500' : 'text-green-500'">
                <component :is="brief.icon" />
              </el-icon>
              <p class="text-sm text-gray-600">{{ brief.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Notebook, DataAnalysis, Warning, CircleCheck } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { dashboardApi, homeworkApi, submissionApi, userApi, checkInApi, errorBookApi, classApi } from '@/api'
import { useUserStore } from '@/stores'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const radarChart = ref<HTMLDivElement>()

const filter = ref('all')
const homeworkList = ref<any[]>([])

// 统计数据
const stats = ref({
  totalHomework: 0,
  completedHomework: 0,
  pendingHomework: 0,
  avgCorrectRate: 0,
  streakDays: 0,
  studyDays: 0,  // 学习天数（从首次打卡到今天）
  checkedInToday: false  // 今日是否已打卡
})

// 用户信息和班级信息
const classInfo = ref('未加入班级')

// 能力分析数据
const abilityData = ref({
  current: [70, 70, 70, 70, 70, 70],
  weakPoint: { name: '暂无数据', level: 'normal' },
  strongPoint: { name: '暂无数据', level: 'normal' }
})

// 学情简报
const briefings = ref<any[]>([])

const getSubjectColor = (subject: string) => {
  const colors: Record<string, string> = {
    'chinese': 'linear-gradient(135deg, #ef4444, #dc2626)',
    'math': 'linear-gradient(135deg, #22c55e, #16a34a)',
    'english': 'linear-gradient(135deg, #3b82f6, #2563eb)',
    'physics': 'linear-gradient(135deg, #a855f7, #9333ea)',
    'chemistry': 'linear-gradient(135deg, #f59e0b, #d97706)'
  }
  return colors[subject] || 'linear-gradient(135deg, #64748b, #475569)'
}

const filteredHomework = computed(() => {
  if (filter.value === 'all') return homeworkList.value
  if (filter.value === 'pending') return homeworkList.value.filter(h => h.status === 0)
  return homeworkList.value.filter(h => h.status === 2)
})

const getStatusType = (status: number) => {
  return status === 0 ? 'info' : status === 1 ? 'warning' : 'success'
}

const getStatusText = (status: number) => {
  return status === 0 ? '待完成' : status === 1 ? '批改中' : '已完成'
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

const handleHomeworkClick = (hw: any) => {
  const studentId = userStore.user?.id
  if (hw.status === 0) {
    router.push(`/student/homework/submit/${hw.id}`)
  } else {
    router.push({
      path: '/student/correction-detail',
      query: { homeworkId: hw.id, studentId }
    })
  }
}

// 加载数据
const loadData = async () => {
  if (!userStore.user) return
  
  try {
    const userId = userStore.user?.id
    
    // 并行加载数据
    const [homeworks, _dashboardData, userStats, checkInStats, submissions, errors] = await Promise.all([
      userStore.user.classInfo?.id 
        ? homeworkApi.getClassHomeworks(userStore.user.classInfo.id)
        : Promise.resolve([]),
      dashboardApi.getStudentDashboard(userId),
      userApi.getUserStats(userId),
      checkInApi.getStats(userId),
      submissionApi.getSubmissionsByStudent(userId),
      errorBookApi.getStudentErrors(userId)
    ])
    
    // 处理作业列表 - 合并作业和提交状态
    homeworkList.value = homeworks.slice(0, 4).map((hw: any) => {
      const submission = submissions?.find((s: any) => s.homework?.id === hw.id || s.homeworkId === hw.id)
      return {
        id: hw.id,
        title: hw.title,
        subject: getSubjectName(hw.subject),
        deadline: hw.deadline?.split('T')[0],
        status: submission?.status ?? 0,
        color: getSubjectColor(hw.subject)
      }
    })
    
    // 更新统计（使用真实数据）
    stats.value = {
      totalHomework: submissions?.length || 0,
      completedHomework: submissions?.filter((s: any) => s.status === 2)?.length || 0,
      pendingHomework: submissions?.filter((s: any) => s.status === 0)?.length || 0,
      avgCorrectRate: Math.round(userStats.avgScore || 0),
      streakDays: checkInStats.currentStreak || 0,
      studyDays: checkInStats.studyDays || 0,
      checkedInToday: checkInStats.checkedInToday || false
    }
    
    // 班级名称
    classInfo.value = userStore.user.classInfo?.name || '未加入班级'
    
    // 处理能力分析数据（基于错题本）
    const kpMap = new Map()
    errors?.forEach((err: any) => {
      if (err.knowledgePoint) {
        const kp = err.knowledgePoint
        if (!kpMap.has(kp)) {
          kpMap.set(kp, { total: 0, mastered: 0 })
        }
        kpMap.get(kp).total++
        if (err.masteryStatus === 2) {
          kpMap.get(kp).mastered++
        }
      }
    })
    
    // 计算知识点掌握度并找出薄弱点和优势项
    const kpList = Array.from(kpMap.entries())
      .map(([name, data]: [string, any]) => ({
        name,
        mastery: Math.round((data.mastered / data.total) * 100)
      }))
      .sort((a: any, b: any) => b.mastery - a.mastery)
    
    if (kpList.length > 0) {
      // 薄弱点（掌握度最低）
      const weak = kpList[kpList.length - 1]
      abilityData.value.weakPoint = { 
        name: weak.name, 
        level: weak.mastery < 60 ? 'danger' : weak.mastery < 80 ? 'warning' : 'normal'
      }
      
      // 优势项（掌握度最高）
      const strong = kpList[0]
      abilityData.value.strongPoint = { 
        name: strong.name, 
        level: strong.mastery >= 90 ? 'success' : strong.mastery >= 70 ? 'warning' : 'normal'
      }
      
      // 更新能力雷达图数据（基于知识点掌握度）
      const avgMastery = kpList.reduce((sum: number, kp: any) => sum + kp.mastery, 0) / kpList.length
      abilityData.value.current = [
        Math.min(100, avgMastery + Math.random() * 10 - 5),
        Math.min(100, avgMastery + Math.random() * 10 - 5),
        Math.min(100, avgMastery + Math.random() * 10 - 5),
        Math.min(100, avgMastery + Math.random() * 10 - 5),
        Math.min(100, avgMastery + Math.random() * 10 - 5),
        Math.min(100, avgMastery + Math.random() * 10 - 5)
      ].map(v => Math.round(v))
    }
    
    // 生成学情简报
    briefings.value = []
    
    // 薄弱知识点提醒
    if (abilityData.value.weakPoint.level !== 'normal') {
      briefings.value.push({
        type: 'warning',
        icon: Warning,
        content: `「${abilityData.value.weakPoint.name}」掌握度较低，建议重点复习`
      })
    }
    
    // 连续打卡鼓励
    if (stats.value.streakDays >= 7) {
      briefings.value.push({
        type: 'success',
        icon: CircleCheck,
        content: `已连续打卡 ${stats.value.streakDays} 天，学习状态很棒！`
      })
    }
    
    // 正确率提醒
    if (stats.value.avgCorrectRate >= 90) {
      briefings.value.push({
        type: 'success',
        icon: CircleCheck,
        content: `作业正确率达到 ${stats.value.avgCorrectRate}%，表现优异！`
      })
    } else if (stats.value.avgCorrectRate < 60) {
      briefings.value.push({
        type: 'warning',
        icon: Warning,
        content: `作业正确率较低 (${stats.value.avgCorrectRate}%)，建议多复习错题`
      })
    }
    
    // 如果没有简报，显示默认鼓励
    if (briefings.value.length === 0) {
      briefings.value.push({
        type: 'success',
        icon: CircleCheck,
        content: '坚持学习，每天进步一点点！'
      })
    }
  } catch (error: any) {
    ElMessage.error('加载数据失败: ' + error.message)
  }
}

// 班级相关
const inviteCode = ref('')
const joining = ref(false)

const handleJoinClass = async () => {
  if (!inviteCode.value.trim()) {
    ElMessage.warning('请输入班级邀请码')
    return
  }
  
  joining.value = true
  try {
    await classApi.joinClass(inviteCode.value.trim(), userStore.user!.id)
    ElMessage.success('加入班级成功！')
    // 刷新用户信息
    const userData = await userApi.getUserById(userStore.user!.id)
    userStore.setUser(userData)
    // 重新加载数据
    loadData()
    inviteCode.value = ''
  } catch (error: any) {
    ElMessage.error(error.message || '加入班级失败，请检查邀请码是否正确')
  } finally {
    joining.value = false
  }
}
onMounted(() => {
  loadData()
  
  if (radarChart.value) {
    const chart = echarts.init(radarChart.value)
    const option = {
      radar: {
        indicator: [
          { name: '逻辑推理', max: 100 },
          { name: '计算能力', max: 100 },
          { name: '阅读理解', max: 100 },
          { name: '写作表达', max: 100 },
          { name: '创新思维', max: 100 },
          { name: '知识掌握', max: 100 }
        ],
        radius: '65%',
        axisName: {
          color: '#666',
          fontSize: 10
        }
      },
      series: [{
        type: 'radar',
        data: [{
          value: abilityData.value.current,
          name: '能力分布',
          areaStyle: {
            color: 'rgba(34, 197, 94, 0.2)'
          },
          lineStyle: {
            color: '#22c55e'
          },
          itemStyle: {
            color: '#22c55e'
          }
        }]
      }]
    }
    chart.setOption(option)
    
    // 监听数据变化，更新图表
    watch(() => abilityData.value.current, (newVal) => {
      chart.setOption({
        series: [{
          data: [{
            value: newVal,
            name: '能力分布',
            areaStyle: { color: 'rgba(34, 197, 94, 0.2)' },
            lineStyle: { color: '#22c55e' },
            itemStyle: { color: '#22c55e' }
          }]
        }]
      })
    }, { deep: true })
  }
})
</script>
