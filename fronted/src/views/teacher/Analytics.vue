<template>
  <div class="space-y-6 animate-fade-in">
    <!-- 顶部标题栏 - 隐藏
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 flex items-center justify-between">
      <div>
        <h2 class="text-xl font-bold text-gray-800">班级学情分析</h2>
        <p class="text-sm text-gray-500 mt-1">数据实时同步自 DeepSeek AI | 更新时间：2026-02-27 20:00</p>
      </div>
      <div class="flex gap-3">
        <el-select v-model="selectedClass" placeholder="选择班级" style="width: 150px">
          <el-option label="高二1班" value="class1" />
          <el-option label="高二3班" value="class3" />
          <el-option label="高二5班" value="class5" />
        </el-select>
        <el-select v-model="timeRange" placeholder="时间范围" style="width: 120px">
          <el-option label="本周" value="week" />
          <el-option label="本月" value="month" />
          <el-option label="本学期" value="semester" />
        </el-select>
        <el-button type="primary" @click="exportReport">
          <el-icon class="mr-1"><Download /></el-icon>导出报表
        </el-button>
      </div>
    </div>
    -->

    <!-- 顶部班级选择器 -->
    <div class="bg-white rounded-2xl p-4 shadow-sm border border-gray-100 flex items-center justify-between mb-4">
      <div class="flex items-center gap-2">
        <el-select v-model="selectedClass" placeholder="选择班级" style="width: 200px" @change="loadClassData">
          <el-option 
            v-for="cls in classList" 
            :key="cls.id" 
            :label="cls.name" 
            :value="cls.id" 
          />
        </el-select>
        <span v-if="classList.length === 0" class="text-red-500 text-sm">暂无班级数据</span>
      </div>
      <div class="text-sm text-gray-500">
        数据实时同步分析由后台算法进行
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-4 gap-4">
      <div v-for="card in summaryCards" :key="card.title" 
           class="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 text-center hover:shadow-md transition-shadow">
        <div class="text-sm text-gray-500 mb-2">{{ card.title }}</div>
        <div class="text-3xl font-bold" :class="card.color">{{ card.value }}</div>
        <div v-if="card.change" class="text-xs mt-1" :class="card.changeType === 'up' ? 'text-green-500' : 'text-red-500'">
          {{ card.changeType === 'up' ? '↑' : '↓' }} {{ card.change }}
        </div>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-6">
      <!-- 分数段分布 -->
      <div class="col-span-6 bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
        <h3 class="font-bold mb-4 flex items-center gap-2">
          <el-icon class="text-blue-500"><Histogram /></el-icon>
          分数段分布
        </h3>
        <div ref="scoreChart" class="h-72"></div>
      </div>

      <!-- 薄弱知识点 -->
      <div class="col-span-6 bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
        <h3 class="font-bold mb-4 flex items-center gap-2">
          <el-icon class="text-red-500"><Warning /></el-icon>
          薄弱知识点 Top 5
        </h3>
        <div class="space-y-4">
          <div v-for="(kp, idx) in weakPoints" :key="kp.name" class="p-3 bg-red-50 rounded-xl">
            <div class="flex justify-between items-center mb-2">
              <div class="flex items-center gap-2">
                <span class="w-6 h-6 rounded-full bg-red-500 text-white text-xs flex items-center justify-center font-bold">{{ idx + 1 }}</span>
                <span class="font-medium">{{ kp.name }}</span>
              </div>
              <span class="text-red-600 font-bold">{{ kp.rate }}% 错误率</span>
            </div>
            <el-progress :percentage="kp.rate" color="#ef4444" :show-text="false" :stroke-width="10" />
            <p class="text-xs text-gray-500 mt-2">{{ kp.suggestion }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 学生成绩分布与知识点掌握 -->
    <div class="grid grid-cols-12 gap-6">
      <!-- 学生成绩排行 -->
      <div class="col-span-7 bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
        <h3 class="font-bold mb-4 flex items-center gap-2">
          <el-icon class="text-green-500"><Trophy /></el-icon>
          学生成绩排行
        </h3>
        <div ref="rankChart" class="h-80"></div>
      </div>

      <!-- 知识点掌握度 -->
      <div class="col-span-5 bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
        <h3 class="font-bold mb-4 flex items-center gap-2">
          <el-icon class="text-purple-500"><PieChart /></el-icon>
          知识点掌握度
        </h3>
        <div ref="knowledgeChart" class="h-72"></div>
      </div>
    </div>

    <!-- 作业完成趋势 -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
      <h3 class="font-bold mb-4 flex items-center gap-2">
        <el-icon class="text-orange-500"><TrendCharts /></el-icon>
        作业完成趋势
      </h3>
      <div ref="trendChart" class="h-72"></div>
    </div>

    <!-- 学生详情表格 -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
      <div class="flex items-center justify-between mb-4">
        <h3 class="font-bold flex items-center gap-2">
          <el-icon class="text-blue-500"><UserFilled /></el-icon>
          学生详细成绩
        </h3>
        <div class="flex gap-2">
          <el-input v-model="searchStudent" placeholder="搜索学生..." clearable style="width: 200px">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" text @click="showAllStudents">查看全部</el-button>
        </div>
      </div>
      <el-table :data="studentDetails" style="width: 100%" :header-cell-style="{ background: '#f9fafb' }">
        <template #empty>
          <div class="flex flex-col items-center justify-center py-12">
            <el-icon class="text-6xl text-gray-300 mb-4"><UserFilled /></el-icon>
            <div class="text-gray-500 text-lg">暂无学生数据</div>
            <div class="text-gray-400 text-sm mt-1">当前班级还没有学生作业记录</div>
          </div>
        </template>
        <el-table-column label="排名" width="80" align="center">
          <template #default="{ $index }">
            <div v-if="$index < 3" class="w-8 h-8 rounded-full mx-auto flex items-center justify-center text-white font-bold"
                 :class="$index === 0 ? 'bg-yellow-500' : $index === 1 ? 'bg-gray-400' : 'bg-orange-600'">
              {{ $index + 1 }}
            </div>
            <span v-else>{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="学生" width="150">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-avatar :size="32" :src="row.avatar" />
              <span class="font-medium">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="总分" width="100" align="center">
          <template #default="{ row }">
            <span class="font-bold" :class="getScoreClass(row.score)">{{ row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="correctRate" label="正确率" width="120" align="center">
          <template #default="{ row }">
            <el-progress :percentage="row.correctRate" :color="getProgressColor(row.correctRate)" />
          </template>
        </el-table-column>
        <el-table-column prop="time" label="用时" width="100" align="center" />
        <el-table-column prop="submitTime" label="提交时间" width="150" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '已完成' ? 'success' : row.status === '待批改' ? 'warning' : 'info'" size="small" effect="light" round>
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewStudentDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Download, Histogram, Warning, Trophy, PieChart, TrendCharts, UserFilled, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { dashboardApi } from '@/api'
import { useUserStore } from '@/stores'

const userStore = useUserStore()
const selectedClass = ref<number | null>(null)
const classList = ref<any[]>([])
const timeRange = ref('week')
const searchStudent = ref('')
const loading = ref(false)
const scoreChart = ref<HTMLDivElement>()
const rankChart = ref<HTMLDivElement>()
const knowledgeChart = ref<HTMLDivElement>()
const trendChart = ref<HTMLDivElement>()

// 真实数据
const summaryCards = ref([
  { title: '应交/实交', value: '- / -', color: 'text-blue-600', change: '-%', changeType: 'up' },
  { title: '班级平均分', value: '-', color: 'text-green-600', change: '-', changeType: 'up' },
  { title: 'AI批改作业', value: '-', color: 'text-purple-600', change: '-%', changeType: 'up' },
  { title: '平均响应时间', value: '-', color: 'text-orange-600', change: '-', changeType: 'down' }
])

const weakPoints = ref<any[]>([])
const studentDetails = ref<any[]>([])
const scoreDistribution = ref({ ranges: ['<60', '60-70', '70-80', '80-90', '90-100'], counts: [0, 0, 0, 0, 0] })
const knowledgeMastery = ref<any[]>([])
const classTrendData = ref<any[]>([])

const getScoreClass = (score: number) => {
  if (score >= 90) return 'text-green-600'
  if (score >= 80) return 'text-blue-600'
  if (score >= 70) return 'text-orange-600'
  return 'text-red-600'
}

const getProgressColor = (rate: number) => {
  if (rate >= 90) return '#22c55e'
  if (rate >= 70) return '#3b82f6'
  return '#ef4444'
}

// 加载班级列表
const loadClassList = async () => {
  try {
    const data = await dashboardApi.getTeacherDashboard(userStore.user?.id || 0)
    console.log('教师仪表盘数据:', data)
    if (data?.classes) {
      classList.value = data.classes
      if (classList.value.length > 0 && !selectedClass.value) {
        selectedClass.value = classList.value[0].id
        console.log('设置默认班级:', selectedClass.value)
      }
    }
  } catch (error) {
    console.error('加载班级列表失败', error)
  }
}

// 加载班级统计数据
const loadClassData = async () => {
  if (!selectedClass.value) {
    console.log('未选择班级，跳过加载')
    return
  }
  
  console.log('开始加载班级数据:', selectedClass.value)
  loading.value = true
  try {
    // 并行加载所有数据
    const [analytics, scoreDist, weakPointsData, ranking, trendData] = await Promise.all([
      dashboardApi.getClassAnalytics(selectedClass.value),
      dashboardApi.getClassScoreDistribution(selectedClass.value),
      dashboardApi.getClassWeakPoints(selectedClass.value),
      dashboardApi.getClassStudentRanking(selectedClass.value),
      dashboardApi.getClassTrend(selectedClass.value)
    ])
    
    console.log('班级分析数据:', analytics)
    console.log('成绩分布数据:', scoreDist)
    console.log('薄弱知识点数据:', weakPointsData)
    console.log('学生排行数据:', ranking)
    console.log('趋势数据:', trendData)
    
    // 存储趋势数据供图表使用
    classTrendData.value = trendData || []
    
    // 更新统计卡片
    summaryCards.value = [
      { 
        title: '班级人数', 
        value: String(analytics?.totalStudents || 0), 
        color: 'text-blue-600', 
        change: '人', 
        changeType: 'up' 
      },
      { 
        title: '班级平均分', 
        value: String(scoreDist?.average || 0), 
        color: 'text-green-600', 
        change: '分', 
        changeType: 'up' 
      },
      { 
        title: '薄弱知识点', 
        value: String(weakPointsData?.length || 0), 
        color: 'text-purple-600', 
        change: '个', 
        changeType: 'down' 
      },
      { 
        title: '已掌握知识点', 
        value: String((analytics?.knowledgePoints || []).filter((kp: any) => kp.mastery >= 80).length), 
        color: 'text-orange-600', 
        change: '个', 
        changeType: 'up' 
      }
    ]
    
    // 更新薄弱知识点
    weakPoints.value = (weakPointsData || []).slice(0, 5).map((wp: any) => ({
      name: wp.name,
      rate: wp.errorRate || 0,
      suggestion: wp.suggestion || '建议加强练习'
    }))
    
    // 更新成绩分布
    scoreDistribution.value = {
      ranges: scoreDist?.ranges || ['<60', '60-70', '70-80', '80-90', '90-100'],
      counts: scoreDist?.counts || [0, 0, 0, 0, 0]
    }
    
    // 更新知识点掌握度
    knowledgeMastery.value = (analytics?.knowledgePoints || []).map((kp: any) => ({
      value: kp.total || 0,
      name: kp.name,
      mastery: kp.mastery || 0
    }))
    
    // 更新学生排行
    studentDetails.value = (ranking || []).map((r: any, index: number) => ({
      name: r.studentName || '未知',
      avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${r.studentId || index}`,
      score: Math.round(r.averageScore || 0),
      correctRate: r.completionRate || 0,
      time: `${r.completedCount || 0}/${r.totalHomework || 0}作业`,
      submitTime: `已完成${r.completedCount || 0}次`,
      status: r.completedCount >= r.totalHomework ? '已完成' : '进行中'
    }))
    
    console.log('数据处理完成，准备刷新图表')
    // 刷新图表
    updateCharts()
  } catch (error: any) {
    console.error('加载班级数据失败:', error)
    ElMessage.error('加载班级数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 更新所有图表
const updateCharts = () => {
  initScoreChart()
  initRankChart()
  initKnowledgeChart()
  initTrendChart()
}

// 初始化分数段分布图
const initScoreChart = () => {
  if (!scoreChart.value) return
  const chart = echarts.init(scoreChart.value)
  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: scoreDistribution.value.ranges
    },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: scoreDistribution.value.counts,
      itemStyle: {
        color: (params: any) => {
          const colors = ['#22c55e', '#84cc16', '#f59e0b', '#ef4444', '#dc2626']
          return colors[params.dataIndex]
        },
        borderRadius: [4, 4, 0, 0]
      },
      barWidth: '50%'
    }]
  }
  chart.setOption(option)
}

// 初始化学生排行图
const initRankChart = () => {
  if (!rankChart.value) return
  const chart = echarts.init(rankChart.value)
  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
    xAxis: {
      type: 'value',
      min: 0,
      max: 100
    },
    yAxis: {
      type: 'category',
      data: studentDetails.value.map(s => s.name).reverse()
    },
    series: [{
      type: 'bar',
      data: studentDetails.value.map(s => s.score).reverse(),
      itemStyle: {
        color: (params: any) => {
          const score = params.value
          if (score >= 90) return '#22c55e'
          if (score >= 80) return '#3b82f6'
          if (score >= 70) return '#f59e0b'
          return '#ef4444'
        },
        borderRadius: [0, 4, 4, 0]
      },
      label: {
        show: true,
        position: 'right',
        formatter: '{c}分'
      }
    }]
  }
  chart.setOption(option)
}

// 初始化知识点掌握度图
const initKnowledgeChart = () => {
  if (!knowledgeChart.value) return
  const chart = echarts.init(knowledgeChart.value)
  
  // 按掌握度分组统计
  const mastered = knowledgeMastery.value.filter(kp => (kp.mastery || 0) >= 80).length
  const consolidating = knowledgeMastery.value.filter(kp => {
    const m = kp.mastery || 0
    return m >= 50 && m < 80
  }).length
  const weak = knowledgeMastery.value.filter(kp => (kp.mastery || 0) < 50).length
  
  const option = {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left', top: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: false,
      label: { show: false },
      data: [
        { value: mastered || 1, name: '已掌握', itemStyle: { color: '#22c55e' } },
        { value: consolidating || 1, name: '待巩固', itemStyle: { color: '#f59e0b' } },
        { value: weak || 1, name: '需加强', itemStyle: { color: '#ef4444' } }
      ]
    }]
  }
  chart.setOption(option)
}

// 初始化趋势图
const initTrendChart = () => {
  if (!trendChart.value) return
  
  // 使用后端返回的真实数据，如果没有则使用默认值
  const trendData = classTrendData.value || []
  
  // 处理数据
  const dates = trendData.length > 0 
    ? trendData.map((item: any) => item.weekDay || item.date)
    : ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  
  const submissionCounts = trendData.length > 0
    ? trendData.map((item: any) => item.submissionCount || 0)
    : [0, 0, 0, 0, 0, 0, 0]
  
  const averageScores = trendData.length > 0
    ? trendData.map((item: any) => item.averageScore || 0)
    : [0, 0, 0, 0, 0, 0, 0]
  
  const chart = echarts.init(trendChart.value)
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['提交人数', '平均分'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: [
      { type: 'value', name: '人数', min: 0 },
      { type: 'value', name: '分数', min: 0, max: 100 }
    ],
    series: [
      {
        name: '提交人数',
        type: 'bar',
        data: submissionCounts,
        itemStyle: { color: '#3b82f6', borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '平均分',
        type: 'line',
        yAxisIndex: 1,
        data: averageScores,
        smooth: true,
        itemStyle: { color: '#22c55e' }
      }
    ]
  }
  chart.setOption(option)
}

const exportReport = () => {
  ElMessage.success('报表导出成功')
}

const showAllStudents = () => {
  ElMessage.info('显示全部学生')
}

const viewStudentDetail = (row: any) => {
  ElMessage.info(`查看 ${row.name} 的详细成绩`)
}

onMounted(() => {
  loadClassList().then(() => {
    loadClassData()
  })
})

// 监听班级变化
watch(selectedClass, () => {
  loadClassData()
})
</script>
