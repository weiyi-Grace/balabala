<template>
  <div class="max-w-7xl mx-auto animate-fade-in">
    <!-- 顶部导航栏 -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 mb-6">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 bg-primary-100 rounded-xl flex items-center justify-center">
            <el-icon :size="24" class="text-primary-600"><School /></el-icon>
          </div>
          <div>
            <h2 class="text-xl font-bold text-gray-800">班级管理</h2>
            <p class="text-sm text-gray-500 mt-1">共管理 {{ classes.length }} 个班级，{{ totalStudents }} 名学生</p>
          </div>
        </div>
        <div class="flex items-center gap-3">
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon class="mr-1"><Plus /></el-icon>
            创建班级
          </el-button>
        </div>
      </div>
    </div>

    <!-- 班级卡片列表 -->
    <div class="grid grid-cols-3 gap-6">
      <div 
        v-for="cls in classes" 
        :key="cls.id"
        class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 hover:shadow-md transition-all cursor-pointer"
        @click="viewClassDetail(cls)"
      >
        <!-- 班级头部 -->
        <div class="flex items-start justify-between mb-4">
          <div class="flex items-center gap-3">
            <div 
              class="w-14 h-14 rounded-2xl flex items-center justify-center text-xl font-bold"
              :class="getClassColor(cls.id)"
            >
              {{ cls.name.charAt(2) }}
            </div>
            <div>
              <h3 class="font-bold text-lg">{{ cls.name }}</h3>
              <p class="text-sm text-gray-500">{{ cls.grade }}</p>
            </div>
          </div>
          <el-dropdown @command="(cmd) => handleClassCommand(cmd, cls)" @click.stop>
            <el-button circle text>
              <el-icon><More /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">编辑班级</el-dropdown-item>
                <el-dropdown-item command="students">管理学生</el-dropdown-item>
                <el-dropdown-item command="invite">邀请学生</el-dropdown-item>
                <el-dropdown-item divided command="delete">删除班级</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- 统计信息 -->
        <div class="grid grid-cols-3 gap-4 mb-4">
          <div class="text-center p-3 bg-gray-50 rounded-xl">
            <div class="text-2xl font-bold text-gray-800">{{ cls.studentCount }}</div>
            <div class="text-xs text-gray-500">学生</div>
          </div>
          <div class="text-center p-3 bg-gray-50 rounded-xl">
            <div class="text-2xl font-bold text-primary-600">{{ cls.avgScore }}</div>
            <div class="text-xs text-gray-500">平均分</div>
          </div>
          <div class="text-center p-3 bg-gray-50 rounded-xl">
            <div class="text-2xl font-bold text-green-600">{{ cls.completionRate }}%</div>
            <div class="text-xs text-gray-500">作业完成率</div>
          </div>
        </div>

        <!-- 进度条 -->
        <div class="mb-4">
          <div class="flex items-center justify-between text-xs text-gray-500 mb-1">
            <span>近期作业完成情况</span>
            <span>{{ cls.completedHomework }}/{{ cls.totalHomework }}</span>
          </div>
          <el-progress 
            :percentage="Math.round((cls.completedHomework / cls.totalHomework) * 100)" 
            :color="cls.completionRate >= 90 ? '#22c55e' : '#f59e0b'"
            :stroke-width="8"
          />
        </div>

        <!-- 底部信息 -->
        <div class="flex items-center justify-between text-sm">
          <div class="flex items-center gap-2 text-gray-500">
            <el-icon><Clock /></el-icon>
            <span>创建时间: {{ cls.createTime }}</span>
          </div>
          <el-button type="primary" link size="small" @click.stop="viewClassDetail(cls)">
            查看详情<el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>

      <!-- 创建班级卡片 -->
      <div 
        class="bg-gray-50 rounded-2xl p-6 border-2 border-dashed border-gray-200 hover:border-primary-300 hover:bg-primary-50/30 transition-all cursor-pointer flex flex-col items-center justify-center min-h-[280px]"
        @click="showCreateDialog = true"
      >
        <div class="w-16 h-16 rounded-full bg-primary-100 flex items-center justify-center mb-4">
          <el-icon :size="32" class="text-primary-600"><Plus /></el-icon>
        </div>
        <p class="text-gray-600 font-medium">创建新班级</p>
        <p class="text-sm text-gray-400 mt-1">点击创建一个新的教学班级</p>
      </div>
    </div>

    <!-- 创建/编辑班级对话框 -->
    <el-dialog v-model="showCreateDialog" :title="isEditing ? '编辑班级' : '创建班级'" width="500px">
      <el-form :model="classForm" label-position="top">
        <el-form-item label="班级名称" required>
          <el-input v-model="classForm.name" placeholder="如：高二1班" />
        </el-form-item>
        <el-form-item label="年级" required>
          <el-select v-model="classForm.grade" placeholder="选择年级" class="w-full">
            <el-option label="高一" value="高一" />
            <el-option label="高二" value="高二" />
            <el-option label="高三" value="高三" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级描述">
          <el-input 
            v-model="classForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="班级描述（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveClass">
            {{ isEditing ? '保存' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 班级详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="班级详情" width="900px">
      <div v-if="currentClass" class="space-y-6">
        <!-- 班级信息头部 -->
        <div class="flex items-center justify-between pb-4 border-b border-gray-100">
          <div class="flex items-center gap-4">
            <div 
              class="w-16 h-16 rounded-2xl flex items-center justify-center text-2xl font-bold"
              :class="getClassColor(currentClass.id)"
            >
              {{ currentClass.name.charAt(2) }}
            </div>
            <div>
              <h3 class="font-bold text-xl">{{ currentClass.name }}</h3>
              <p class="text-gray-500">{{ currentClass.grade }} | {{ currentClass.studentCount }} 名学生</p>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <el-button type="primary" @click="showInviteDialog = true">
              <el-icon class="mr-1"><UserPlus /></el-icon>
              邀请学生
            </el-button>
            <el-button @click="editClass(currentClass)">
              <el-icon class="mr-1"><Edit /></el-icon>
              编辑
            </el-button>
          </div>
        </div>

        <!-- 统计卡片 -->
        <div class="grid grid-cols-4 gap-4">
          <div class="bg-blue-50 rounded-xl p-4">
            <div class="text-sm text-blue-600 mb-1">学生人数</div>
            <div class="text-2xl font-bold text-blue-700">{{ currentClass.studentCount }}</div>
          </div>
          <div class="bg-green-50 rounded-xl p-4">
            <div class="text-sm text-green-600 mb-1">平均分</div>
            <div class="text-2xl font-bold text-green-700">{{ currentClass.avgScore }}</div>
          </div>
          <div class="bg-orange-50 rounded-xl p-4">
            <div class="text-sm text-orange-600 mb-1">作业完成率</div>
            <div class="text-2xl font-bold text-orange-700">{{ currentClass.completionRate }}%</div>
          </div>
          <div class="bg-purple-50 rounded-xl p-4">
            <div class="text-sm text-purple-600 mb-1">待批改作业</div>
            <div class="text-2xl font-bold text-purple-700">{{ currentClass.pendingCorrection }}</div>
          </div>
        </div>

        <!-- 学生列表 -->
        <div>
          <div class="flex items-center justify-between mb-4">
            <h4 class="font-bold text-lg">学生列表</h4>
            <el-input 
              v-model="studentSearchQuery" 
              placeholder="搜索学生..." 
              clearable 
              style="width: 200px"
              size="small"
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
          
          <el-table :data="paginatedStudents" style="width: 100%">
            <el-table-column label="学生" min-width="150">
              <template #default="{ row }">
                <div class="flex items-center gap-2">
                  <el-avatar :size="32" class="bg-primary-100 text-primary-600">
                    {{ row.name.charAt(0) }}
                  </el-avatar>
                  <span class="font-medium">{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="studentNo" label="学号" width="120" />
            <el-table-column label="平均分" width="100" align="center">
              <template #default="{ row }">
                <span class="font-bold" :class="row.avgScore >= 85 ? 'text-green-600' : 'text-orange-600'">
                  {{ row.avgScore }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="作业完成率" width="120" align="center">
              <template #default="{ row }">
                <span>{{ row.completionRate }}%</span>
              </template>
            </el-table-column>
            <el-table-column label="错题数" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.errorCount > 10 ? 'danger' : 'info'" size="small" effect="light" round>
                  {{ row.errorCount }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" link size="small" @click="removeStudent(row)">
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 分页 -->
          <div class="flex justify-center mt-4">
            <el-pagination 
              v-model:current-page="studentPage" 
              v-model:page-size="studentPageSize"
              :page-sizes="[10, 20, 50]"
              :total="filteredStudents.length" 
              background 
              layout="total, sizes, prev, pager, next"
              @size-change="handleStudentSizeChange"
              @current-change="handleStudentCurrentChange"
            />
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 邀请学生对话框 -->
    <el-dialog v-model="showInviteDialog" title="邀请学生加入班级" width="500px">
      <div class="space-y-4">
        <el-alert type="info" :closable="false">
          <p>学生可以通过以下班级码加入班级，或在注册时直接选择本班级。</p>
        </el-alert>
        
        <div class="bg-gray-50 rounded-xl p-4 text-center">
          <div class="text-sm text-gray-500 mb-2">班级邀请码</div>
          <div class="text-3xl font-bold text-primary-600 tracking-wider">{{ currentClass?.inviteCode }}</div>
          <el-button type="primary" link class="mt-2" @click="copyInviteCode">
            <el-icon class="mr-1"><CopyDocument /></el-icon>
            复制邀请码
          </el-button>
        </div>

        <el-divider>或直接添加学生</el-divider>

        <el-form label-position="top">
          <el-form-item label="学生账号">
            <el-select
              v-model="selectedStudents"
              multiple
              filterable
              remote
              reserve-keyword
              placeholder="输入学生姓名或学号搜索"
              :remote-method="searchStudents"
              :loading="searching"
              class="w-full"
            >
              <el-option
                v-for="student in searchResults"
                :key="student.id"
                :label="`${student.name} (${student.studentNo})`"
                :value="student.id"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="showInviteDialog = false">取消</el-button>
          <el-button type="primary" :loading="adding" @click="addStudents">
            添加选中学生
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  School, 
  Plus, 
  More, 
  Clock, 
  ArrowRight, 
  User, 
  Edit, 
  Search, 
  CopyDocument 
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { classApi, userApi, dashboardApi } from '@/api'
import { useUserStore } from '@/stores'

const router = useRouter()
const userStore = useUserStore()

const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const showInviteDialog = ref(false)
const saving = ref(false)
const adding = ref(false)
const searching = ref(false)
const isEditing = ref(false)
const currentClass = ref<any>(null)
const studentSearchQuery = ref('')
const selectedStudents = ref<number[]>([])
const searchResults = ref<any[]>([])
const studentPage = ref(1)
const studentPageSize = ref(10)

// 班级数据
const classes = ref<any[]>([])
const classStudents = ref<any[]>([])

// 加载班级列表
const loadClasses = async () => {
  if (!userStore.user) return
  try {
    const teacherId = userStore.user.id
    const data = await classApi.getClassList(teacherId)
    
    // 获取每个班级的真实统计数据
    const classesWithStats = await Promise.all(
      data.map(async (cls: any) => {
        try {
          const stats = await dashboardApi.getClassDashboard(cls.id)
          return {
            id: cls.id,
            name: cls.name,
            grade: cls.grade,
            description: cls.description,
            studentCount: stats.totalStudents || 0,
            avgScore: stats.averageScore || 0,
            completionRate: stats.completionRate || 0,
            completedHomework: stats.completedHomework || 0,
            totalHomework: stats.totalHomework || 0,
            pendingCorrection: stats.pendingCorrection || 0,
            inviteCode: cls.inviteCode,
            createTime: cls.createTime
          }
        } catch (error) {
          // 如果统计接口失败，返回基础数据
          return {
            id: cls.id,
            name: cls.name,
            grade: cls.grade,
            description: cls.description,
            studentCount: 0,
            avgScore: 0,
            completionRate: 0,
            completedHomework: 0,
            totalHomework: 0,
            pendingCorrection: 0,
            inviteCode: cls.inviteCode,
            createTime: cls.createTime
          }
        }
      })
    )
    
    classes.value = classesWithStats
  } catch (error: any) {
    ElMessage.error('加载班级失败: ' + error.message)
  }
}

// 班级表单
const classForm = ref({
  id: null as number | null,
  name: '',
  grade: '',
  description: ''
})

// 总学生数
const totalStudents = computed(() => classes.value.reduce((sum, c) => sum + (c.studentCount || 0), 0))

// 筛选后的学生
const filteredStudents = computed(() => {
  if (!studentSearchQuery.value) return classStudents.value
  return classStudents.value.filter(s => 
    s.name.includes(studentSearchQuery.value) || 
    s.studentNo.includes(studentSearchQuery.value)
  )
})

// 分页后的学生列表
const paginatedStudents = computed(() => {
  const start = (studentPage.value - 1) * studentPageSize.value
  const end = start + studentPageSize.value
  return filteredStudents.value.slice(start, end)
})

// 获取班级颜色
const getClassColor = (id: number) => {
  const colors = [
    'bg-blue-100 text-blue-600',
    'bg-green-100 text-green-600',
    'bg-orange-100 text-orange-600',
    'bg-purple-100 text-purple-600',
    'bg-pink-100 text-pink-600'
  ]
  return colors[(id - 1) % colors.length]
}

// 查看班级详情
const viewClassDetail = async (cls: any) => {
  currentClass.value = cls
  showDetailDialog.value = true
  
  // 加载班级学生
  try {
    const students = await classApi.getClassStudents(cls.id)
    classStudents.value = students.map((s: any) => ({
      id: s.id,
      name: s.realName,
      studentNo: s.username,
      avgScore: 85, // 需要后端提供
      completionRate: 90, // 需要后端提供
      errorCount: 0 // 需要后端提供
    }))
    // 更新学生数
    cls.studentCount = students.length
  } catch (error: any) {
    ElMessage.error('加载学生列表失败: ' + error.message)
  }
}

// 编辑班级
const editClass = (cls: any) => {
  classForm.value = {
    id: cls.id,
    name: cls.name,
    grade: cls.grade,
    description: cls.description
  }
  isEditing.value = true
  showCreateDialog.value = true
}

// 保存班级
const saveClass = async () => {
  if (!userStore.user) return
  
  saving.value = true
  try {
    if (isEditing.value && classForm.value.id) {
      await classApi.updateClass(classForm.value.id, {
        name: classForm.value.name,
        grade: classForm.value.grade,
        description: classForm.value.description
      })
      ElMessage.success('班级信息已更新')
    } else {
      await classApi.createClass({
        name: classForm.value.name,
        grade: classForm.value.grade,
        description: classForm.value.description,
        teacherId: userStore.user.id
      })
      ElMessage.success('班级创建成功')
    }
    showCreateDialog.value = false
    isEditing.value = false
    classForm.value = { id: null, name: '', grade: '', description: '' }
    await loadClasses()
  } catch (error: any) {
    ElMessage.error('保存失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

// 处理班级命令
const handleClassCommand = async (command: string, cls: any) => {
  if (command === 'edit') {
    editClass(cls)
  } else if (command === 'students') {
    viewClassDetail(cls)
  } else if (command === 'invite') {
    currentClass.value = cls
    showInviteDialog.value = true
  } else if (command === 'delete') {
    try {
      await ElMessageBox.confirm(`确定要删除 ${cls.name} 吗？`, '提示', { type: 'warning' })
      await classApi.deleteClass(cls.id)
      await loadClasses()
      ElMessage.success('班级已删除')
    } catch {}
  }
}

// 复制邀请码
const copyInviteCode = () => {
  if (currentClass.value?.inviteCode) {
    navigator.clipboard.writeText(currentClass.value.inviteCode)
    ElMessage.success('邀请码已复制')
  }
}

// 搜索学生
const searchStudents = async (query: string) => {
  if (query && currentClass.value) {
    searching.value = true
    try {
      const students = await userApi.searchStudentsNotInClass(query, currentClass.value.id)
      searchResults.value = students.map((s: any) => ({
        id: s.id,
        name: s.realName || s.username,
        studentNo: s.username
      }))
    } catch (error: any) {
      ElMessage.error('搜索学生失败: ' + error.message)
      searchResults.value = []
    } finally {
      searching.value = false
    }
  }
}

// 添加学生
const addStudents = async () => {
  if (selectedStudents.value.length === 0) {
    ElMessage.warning('请选择要添加的学生')
    return
  }
  
  if (!currentClass.value) return
  
  adding.value = true
  try {
    // 逐个添加学生到班级
    for (const studentId of selectedStudents.value) {
      await classApi.joinClass(currentClass.value.inviteCode, studentId)
    }
    ElMessage.success(`成功添加 ${selectedStudents.value.length} 名学生`)
    showInviteDialog.value = false
    selectedStudents.value = []
    await viewClassDetail(currentClass.value)
  } catch (error: any) {
    ElMessage.error('添加失败: ' + error.message)
  } finally {
    adding.value = false
  }
}

// 移除学生
const removeStudent = async (student: any) => {
  if (!currentClass.value) return
  
  try {
    await ElMessageBox.confirm(`确定将 ${student.name} 从班级移除吗？`, '提示', { type: 'warning' })
    await classApi.removeStudent(currentClass.value.id, student.id)
    classStudents.value = classStudents.value.filter(s => s.id !== student.id)
    currentClass.value.studentCount--
    ElMessage.success('学生已移除')
  } catch {}
}

// 页面加载时获取数据
const handleStudentSizeChange = (val: number) => {
  studentPageSize.value = val
  studentPage.value = 1
}

const handleStudentCurrentChange = (val: number) => {
  studentPage.value = val
}

onMounted(() => {
  loadClasses()
})
</script>
