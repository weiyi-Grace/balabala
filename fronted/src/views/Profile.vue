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
            <h2 class="text-xl font-bold text-gray-800">个人中心</h2>
            <p class="text-sm text-gray-500 mt-1">管理您的个人信息和账户安全</p>
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-6">
      <!-- 左侧个人信息卡片 -->
      <div class="col-span-4">
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <div class="text-center mb-6">
            <div class="relative inline-block">
              <el-avatar :size="100" class="bg-gradient-to-br from-primary-400 to-primary-600 text-3xl">
                {{ userInfo.name.charAt(0) }}
              </el-avatar>
              <el-button 
                circle 
                size="small" 
                class="absolute -bottom-1 -right-1"
                @click="uploadAvatar"
              >
                <el-icon><Camera /></el-icon>
              </el-button>
            </div>
            <h3 class="font-bold text-lg mt-4">{{ userInfo.name }}</h3>
            <p class="text-sm text-gray-500">{{ userInfo.role }}</p>
          </div>

          <div class="space-y-4 text-sm">
            <div class="flex items-center justify-between py-2 border-b border-gray-100">
              <span class="text-gray-500">学号/工号</span>
              <span class="font-medium">{{ userInfo.userId }}</span>
            </div>
            <div class="flex items-center justify-between py-2 border-b border-gray-100">
              <span class="text-gray-500">班级</span>
              <div class="flex items-center gap-2">
                <span class="font-medium">{{ userInfo.className || '—' }}</span>
                <el-button v-if="userInfo.role === '学生'" link type="primary" size="small" @click="showClassTransferDialog = true">
                  申请转班
                </el-button>
              </div>
            </div>
            <div class="flex items-center justify-between py-2 border-b border-gray-100">
              <span class="text-gray-500">注册时间</span>
              <span class="font-medium">{{ userInfo.registerTime }}</span>
            </div>
            <div class="flex items-center justify-between py-2">
              <span class="text-gray-500">最后登录</span>
              <span class="font-medium">{{ userInfo.lastLogin }}</span>
            </div>
          </div>

          <div class="mt-6 pt-4 border-t border-gray-100">
            <div class="grid grid-cols-3 gap-4 text-center">
              <div>
                <div class="text-2xl font-bold text-primary-600">{{ stats.homeworkCount }}</div>
                <div class="text-xs text-gray-500">作业完成</div>
              </div>
              <div>
                <div class="text-2xl font-bold text-green-600">{{ stats.avgScore }}</div>
                <div class="text-xs text-gray-500">平均分</div>
              </div>
              <div>
                <div class="text-2xl font-bold text-orange-600">{{ stats.errorCount }}</div>
                <div class="text-xs text-gray-500">错题数</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧编辑区域 -->
      <div class="col-span-8 space-y-6">
        <!-- 基本信息 -->
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <div class="flex items-center justify-between mb-6">
            <h3 class="font-bold text-lg flex items-center gap-2">
              <el-icon class="text-primary-500"><User /></el-icon>
              基本信息
            </h3>
            <el-button type="primary" @click="saveBasicInfo" :loading="saving">
              <el-icon class="mr-1"><Check /></el-icon>
              保存修改
            </el-button>
          </div>
          
          <el-form :model="form" label-position="top">
            <div class="grid grid-cols-2 gap-4">
              <el-form-item label="真实姓名">
                <el-input v-model="form.realName" placeholder="请输入真实姓名" />
              </el-form-item>
              
              <el-form-item label="昵称">
                <el-input v-model="form.nickname" placeholder="请输入昵称" />
              </el-form-item>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <el-form-item label="手机号码">
                <el-input v-model="form.phone" placeholder="请输入手机号码" />
              </el-form-item>
              
              <el-form-item label="邮箱">
                <el-input v-model="form.email" placeholder="请输入邮箱地址" />
              </el-form-item>
            </div>

            <el-form-item label="个人简介">
              <el-input 
                v-model="form.bio" 
                type="textarea" 
                :rows="3" 
                placeholder="简单介绍一下自己..." 
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="450px">
      <el-form :model="passwordForm" label-position="top">
        <el-form-item label="当前密码">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            placeholder="请输入当前密码" 
            show-password 
          />
        </el-form-item>
        
        <el-form-item label="新密码">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            placeholder="请输入新密码（6-20位）" 
            show-password 
            @input="checkPasswordStrength"
          />
          <!-- 密码强度 -->
          <div class="mt-2 flex items-center gap-2">
            <span class="text-xs text-gray-500">密码强度：</span>
            <div class="flex gap-1">
              <div class="w-12 h-1 rounded-full" :class="passwordStrength >= 1 ? 'bg-red-500' : 'bg-gray-200'"></div>
              <div class="w-12 h-1 rounded-full" :class="passwordStrength >= 2 ? 'bg-yellow-500' : 'bg-gray-200'"></div>
              <div class="w-12 h-1 rounded-full" :class="passwordStrength >= 3 ? 'bg-green-500' : 'bg-gray-200'"></div>
            </div>
            <span class="text-xs" :class="strengthTextClass">{{ strengthText }}</span>
          </div>
        </el-form-item>
        
        <el-form-item label="确认新密码">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码" 
            show-password 
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="showPasswordDialog = false">取消</el-button>
          <el-button type="primary" :loading="changingPassword" @click="changePassword">确认修改</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 班级申请对话框 -->
    <el-dialog v-model="showClassTransferDialog" title="申请转班" width="500px">
      <div v-if="pendingRequest" class="mb-4 p-3 bg-yellow-50 rounded-lg">
        <p class="text-sm text-yellow-700">
          <el-icon class="mr-1"><Warning /></el-icon>
          您有一条待审批的转班申请，请等待老师处理
        </p>
      </div>
      
      <el-form :model="transferForm" label-position="top">
        <el-form-item label="目标班级" required>
          <el-select v-model="transferForm.targetClassId" placeholder="请选择要转入的班级" class="w-full">
            <el-option 
              v-for="cls in availableClasses" 
              :key="cls.id" 
              :label="cls.name" 
              :value="cls.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="申请理由">
          <el-input 
            v-model="transferForm.reason" 
            type="textarea" 
            :rows="3"
            placeholder="请输入转班理由（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 申请历史 -->
        <el-form-item v-if="transferHistory.length > 0" label="申请历史">
          <div class="space-y-2 max-h-40 overflow-y-auto">
            <div v-for="item in transferHistory" :key="item.id" class="p-2 bg-gray-50 rounded text-sm">
              <div class="flex justify-between">
                <span>{{ item.toClassName }}</span>
                <el-tag :type="getStatusType(item.status)" size="small">{{ getStatusText(item.status) }}</el-tag>
              </div>
              <div class="text-xs text-gray-500 mt-1">{{ item.createTime }}</div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="showClassTransferDialog = false">取消</el-button>
          <el-button type="primary" :loading="submittingTransfer" @click="submitClassTransfer" :disabled="!!pendingRequest">
            提交申请
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { User, Camera, Check, Lock, Message, Phone, Warning, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { userApi, classTransferApi, classApi } from '@/api'
import { useUserStore } from '@/stores'

const userStore = useUserStore()

// 用户信息
const userInfo = ref({
  name: '',
  role: '',
  userId: '',
  className: '',
  registerTime: '',
  lastLogin: ''
})

// 统计数据
const stats = ref({
  homeworkCount: 0,
  avgScore: 0,
  errorCount: 0
})

// 表单数据
const form = reactive({
  realName: '',
  nickname: '',
  phone: '',
  email: '',
  bio: ''
})

const saving = ref(false)
const showPasswordDialog = ref(false)
const changingPassword = ref(false)

// 班级申请相关
const showClassTransferDialog = ref(false)
const submittingTransfer = ref(false)
const availableClasses = ref<any[]>([])
const transferHistory = ref<any[]>([])
const pendingRequest = ref<any>(null)

const transferForm = reactive({
  targetClassId: null as number | null,
  reason: ''
})

// 状态显示
const getStatusType = (status: number) => {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '待审批', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

// 加载班级列表
const loadAvailableClasses = async () => {
  try {
    const data = await classApi.getAllClasses()
    availableClasses.value = data.filter((c: any) => c.name !== userInfo.value.className)
  } catch (error) {
    console.error('加载班级列表失败', error)
  }
}

// 加载申请历史
const loadTransferHistory = async () => {
  if (!userStore.user) return
  try {
    const data = await classTransferApi.getStudentRequests(userStore.user.id)
    transferHistory.value = data
    pendingRequest.value = data.find((item: any) => item.status === 0) || null
  } catch (error) {
    console.error('加载申请历史失败', error)
  }
}

// 提交班级申请
const submitClassTransfer = async () => {
  if (!transferForm.targetClassId) {
    ElMessage.warning('请选择目标班级')
    return
  }
  if (!userStore.user) return
  
  submittingTransfer.value = true
  try {
    await classTransferApi.applyForTransfer({
      studentId: userStore.user.id,
      toClassId: transferForm.targetClassId,
      reason: transferForm.reason
    })
    ElMessage.success('申请提交成功，请等待老师审批')
    showClassTransferDialog.value = false
    await loadTransferHistory()
  } catch (error: any) {
    ElMessage.error(error.message || '申请提交失败')
  } finally {
    submittingTransfer.value = false
  }
}

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordStrength = ref(0)

// 加载用户数据
const loadUserData = async () => {
  if (!userStore.user) return
  
  try {
    const userId = userStore.user.id
    
    // 并行加载用户信息和统计
    const [userData, userStats] = await Promise.all([
      userApi.getUserById(userId),
      userApi.getUserStats(userId)
    ])
    
    console.log('用户数据:', userData)
    console.log('classInfo:', userData.classInfo)
    console.log('classId:', userData.classId)
    
    // 更新用户信息
    userInfo.value = {
      name: userData.realName || userData.username,
      role: userData.role === 2 ? '教师' : '学生',
      userId: userData.username,
      className: userData.classInfo?.name || '',
      registerTime: userData.createTime?.split('T')[0] || '',
      lastLogin: userData.lastLoginTime?.replace('T', ' ') || ''
    }
    
    // 更新统计数据
    stats.value = {
      homeworkCount: userStats.homeworkCount || 0,
      avgScore: userStats.avgScore || 0,
      errorCount: userStats.errorCount || 0
    }
    
    // 更新表单
    form.realName = userData.realName || ''
    form.nickname = userData.nickname || ''
    form.phone = userData.phone || ''
    form.email = userData.email || ''
    form.bio = userData.bio || ''
  } catch (error: any) {
    ElMessage.error('加载用户信息失败: ' + error.message)
  }
}

// 密码强度检测
const checkPasswordStrength = () => {
  const pwd = passwordForm.newPassword
  let strength = 0
  if (pwd.length >= 6) strength++
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) strength++
  if (/\d/.test(pwd) && /[^a-zA-Z0-9]/.test(pwd)) strength++
  passwordStrength.value = strength
}

const strengthText = computed(() => {
  if (passwordStrength.value === 0) return '太短'
  if (passwordStrength.value === 1) return '弱'
  if (passwordStrength.value === 2) return '中'
  return '强'
})

const strengthTextClass = computed(() => {
  if (passwordStrength.value === 0) return 'text-gray-400'
  if (passwordStrength.value === 1) return 'text-red-500'
  if (passwordStrength.value === 2) return 'text-yellow-500'
  return 'text-green-500'
})

// 保存基本信息
const saveBasicInfo = async () => {
  if (!userStore.user) return
  
  saving.value = true
  try {
    await userApi.updateProfile(userStore.user.id, {
      realName: form.realName,
      nickname: form.nickname,
      phone: form.phone,
      email: form.email,
      bio: form.bio
    })
    ElMessage.success('个人信息保存成功！')
    await loadUserData()
  } catch (error: any) {
    ElMessage.error('保存失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

// 修改密码
const changePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次密码输入不一致')
    return
  }
  
  if (!userStore.user) return
  
  changingPassword.value = true
  try {
    await userApi.changePassword(userStore.user.id, {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    showPasswordDialog.value = false
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    ElMessage.success('密码修改成功！')
  } catch (error: any) {
    ElMessage.error('修改失败: ' + error.message)
  } finally {
    changingPassword.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadUserData()
  loadTransferHistory()
})

// 监听对话框打开
watch(showClassTransferDialog, (val) => {
  if (val) {
    loadAvailableClasses()
    loadTransferHistory()
  }
})
</script>
