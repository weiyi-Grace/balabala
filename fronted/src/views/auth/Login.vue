<template>
  <div class="min-h-screen bg-system flex items-center justify-center relative overflow-hidden">
    <!-- 背景装饰元素 -->
    <div class="absolute top-20 left-20 w-72 h-72 bg-primary-500/20 rounded-full blur-3xl animate-pulse"></div>
    <div class="absolute bottom-20 right-20 w-96 h-96 bg-primary-700/20 rounded-full blur-3xl animate-pulse" style="animation-delay: 1s;"></div>
    
    <div class="w-full max-w-md mx-4 z-10">
      <div class="glass-effect rounded-2xl p-8 shadow-2xl animate-slide-up">
        <div class="text-center mb-8">
          <div class="w-24 h-24 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center shadow-lg shadow-primary-500/30">
            <img src="/logo.png" alt="Logo" class="w-18 h-18 object-contain" />
          </div>
          <h1 class="text-2xl font-bold text-gray-800">智能作业批改系统</h1>
          <p class="text-sm text-gray-500 mt-1">基于 DeepSeek 大语言模型</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入学号/工号" 
              size="large" 
              :prefix-icon="User"
              clearable
            >
              <template #prepend>
                <el-select v-model="form.role" style="width: 80px">
                  <el-option label="学生" :value="3" />
                  <el-option label="教师" :value="2" />
                </el-select>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码" 
              size="large" 
              :prefix-icon="Lock" 
              show-password
              clearable
            />
          </el-form-item>

          <!-- 验证码 -->
          <el-form-item prop="captcha">
            <div class="flex gap-3">
              <el-input 
                v-model="form.captcha" 
                placeholder="请输入验证码" 
                size="large"
                class="flex-1"
                maxlength="4"
              />
              <div 
                class="w-28 h-10 bg-gray-200 rounded-lg flex items-center justify-center cursor-pointer select-none"
                @click="refreshCaptcha"
              >
                <span class="text-lg font-bold text-primary-600 tracking-widest">{{ captchaCode }}</span>
              </div>
            </div>
          </el-form-item>

          <div class="flex items-center justify-between mb-4">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false" @click="forgotPassword">忘记密码？</el-link>
          </div>

          <el-button 
            type="primary" 
            size="large" 
            class="w-full !h-12 !text-base font-medium !rounded-xl" 
            :loading="loading" 
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form>

        <div class="mt-6 text-center">
          <span class="text-gray-500 text-sm">还没有账号？</span>
          <el-link type="primary" :underline="false" @click="router.push('/register')" class="!font-medium">立即注册</el-link>
        </div>

        <p class="text-center mt-6 text-sm text-gray-500/80">
          2026 教育智能化转型课题组
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElNotification } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)
const captchaCode = ref('')

const form = reactive({ 
  username: '', 
  password: '',
  captcha: '',
  role: 3
})

// 生成验证码
const generateCaptcha = () => {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  let code = ''
  for (let i = 0; i < 4; i++) {
    code += chars[Math.floor(Math.random() * chars.length)]
  }
  captchaCode.value = code
}

const refreshCaptcha = () => {
  generateCaptcha()
  form.captcha = ''
}

onMounted(() => {
  generateCaptcha()
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入学号/工号', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为4位字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    
    // 验证码校验
    if (form.captcha.toUpperCase() !== captchaCode.value) {
      ElMessage.error('验证码错误')
      refreshCaptcha()
      return
    }
    
    loading.value = true
    try {
      const userStore = useUserStore()
      await userStore.login({
        username: form.username,
        password: form.password,
        role: form.role
      })
      
      ElNotification({
        title: '登录成功',
        message: `欢迎回来，${form.role === 2 ? '老师' : '同学'}！`,
        type: 'success',
        duration: 2000
      })
      
      // 根据角色跳转
      router.push(form.role === 2 ? '/teacher/dashboard' : '/student/dashboard')
    } catch (error: any) {
      ElMessage.error(error.message || '登录失败')
      refreshCaptcha()
    } finally {
      loading.value = false
    }
  })
}

const forgotPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}
</script>

<style scoped>
:deep(.el-input__wrapper) {
  border-radius: 10px;
}
</style>
