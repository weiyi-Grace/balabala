<template>
  <div class="min-h-screen bg-system flex items-center justify-center py-8 relative overflow-hidden">
    <!-- 背景装饰 -->
    <div class="absolute top-10 left-10 w-64 h-64 bg-primary-400/10 rounded-full blur-3xl"></div>
    <div class="absolute bottom-10 right-10 w-80 h-80 bg-primary-600/10 rounded-full blur-3xl"></div>
    
    <div class="w-full max-w-lg mx-4 z-10">
      <div class="glass-effect rounded-2xl p-8 shadow-2xl animate-slide-up">
        <div class="text-center mb-6">
          <div class="w-20 h-20 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center shadow-lg shadow-primary-500/30">
            <img src="/logo.png" alt="Logo" class="w-14 h-14 object-contain" />
          </div>
          <h1 class="text-2xl font-bold text-gray-800">创建账号</h1>
          <p class="text-sm text-gray-500">开启智能作业批改之旅</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleRegister">
          <el-form-item prop="realName">
            <el-input v-model="form.realName" placeholder="真实姓名" size="large" :prefix-icon="User" clearable />
          </el-form-item>

          <el-form-item prop="role">
            <el-radio-group v-model="form.role" class="w-full">
              <el-radio-button :label="3" class="flex-1 !w-1/2">
                <el-icon class="mr-1"><User /></el-icon>学生
              </el-radio-button>
              <el-radio-button :label="2" class="flex-1 !w-1/2">
                <el-icon class="mr-1"><UserFilled /></el-icon>教师
              </el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="学号/工号" size="large" :prefix-icon="Postcard" clearable />
          </el-form-item>

          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="设置密码（6-20位）" 
              size="large" 
              :prefix-icon="Lock" 
              show-password
              clearable
              @input="checkPasswordStrength"
            />
            <!-- 密码强度指示器 -->
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

          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="确认密码" 
              size="large" 
              :prefix-icon="Lock" 
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item v-if="form.role === 3" prop="inviteCode">
            <el-input 
              v-model="form.inviteCode" 
              placeholder="请输入班级邀请码（选填）" 
              size="large" 
              :prefix-icon="Key"
              clearable
              maxlength="20"
            />
            <div class="text-xs text-gray-400 mt-1">注册后可输入班级邀请码加入班级，也可在登录后通过首页加入</div>
          </el-form-item>

          <el-form-item prop="phone">
            <el-input v-model="form.phone" placeholder="手机号码（选填）" size="large" :prefix-icon="Phone" clearable />
          </el-form-item>

          <el-form-item prop="agreement">
            <el-checkbox v-model="form.agreement">
              <span class="text-sm">我已阅读并同意</span>
              <el-link type="primary" :underline="false" class="!text-sm">《服务协议》</el-link>
              <span class="text-sm">和</span>
              <el-link type="primary" :underline="false" class="!text-sm">《隐私政策》</el-link>
            </el-checkbox>
          </el-form-item>

          <el-button 
            type="success" 
            size="large" 
            class="w-full !h-12 !text-base font-medium !rounded-xl" 
            :loading="loading" 
            @click="handleRegister"
          >
            注册账号
          </el-button>
        </el-form>

        <div class="mt-6 text-center">
          <span class="text-gray-500 text-sm">已有账号？</span>
          <el-link type="primary" :underline="false" @click="router.push('/login')" class="!font-medium">立即登录</el-link>
        </div>
      </div>
      
      <p class="text-center mt-6 text-sm text-gray-500/80">
        © 2026 教育智能化转型课题组
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Postcard, Phone, UserFilled, Key } from '@element-plus/icons-vue'
import { ElMessage, ElNotification } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { authApi } from '@/api'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const passwordStrength = ref(0)

const form = reactive({
  realName: '', 
  role: 3, 
  username: '', 
  password: '', 
  confirmPassword: '', 
  phone: '',
  classId: null as number | null,
  inviteCode: '',
  agreement: false
})

// 密码强度检测
const checkPasswordStrength = () => {
  const pwd = form.password
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

const validatePass = (_rule: any, value: any, callback: any) => {
  if (value === '') callback(new Error('请再次输入密码'))
  else if (value !== form.password) callback(new Error('两次密码不一致'))
  else callback()
}

const validatePhone = (_rule: any, value: any, callback: any) => {
  if (!value || /^1[3-9]\d{9}$/.test(value)) {
    callback()
  } else {
    callback(new Error('请输入正确的手机号码'))
  }
}

const rules: FormRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  username: [
    { required: true, message: '请输入学号/工号', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }, 
    { min: 6, message: '密码至少6位', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d)/, message: '密码需包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validatePass, trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  agreement: [{ type: 'enum', enum: [true], message: '请同意服务协议', trigger: 'change' }]
}

const handleRegister = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        await authApi.register({
          username: form.username,
          password: form.password,
          realName: form.realName,
          role: form.role,
          phone: form.phone,
          inviteCode: form.inviteCode
        })
        
        ElNotification({
          title: '注册成功',
          message: '欢迎加入智能作业批改系统！',
          type: 'success',
          duration: 3000
        })
        router.push('/login')
      } catch (error: any) {
        ElMessage.error('注册失败: ' + (error.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }
  })
}
</script>
