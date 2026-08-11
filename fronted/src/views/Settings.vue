<template>
  <div class="max-w-7xl mx-auto animate-fade-in">
    <!-- 顶部导航栏 -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 mb-6">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 bg-primary-100 rounded-xl flex items-center justify-center">
            <el-icon :size="24" class="text-primary-600"><Setting /></el-icon>
          </div>
          <div>
            <h2 class="text-xl font-bold text-gray-800">系统设置</h2>
            <p class="text-sm text-gray-500 mt-1">配置系统参数和全局选项</p>
          </div>
        </div>
        <el-button type="primary" @click="saveSettings" :loading="saving">
          <el-icon class="mr-1"><Check /></el-icon>
          保存设置
        </el-button>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-6">
      <!-- 左侧菜单 -->
      <div class="col-span-3">
        <div class="bg-white rounded-2xl p-4 shadow-sm border border-gray-100 sticky top-4">
          <el-menu
            :default-active="activeMenu"
            class="border-none"
            @select="handleMenuSelect"
          >
            <el-menu-item index="general">
              <el-icon><Setting /></el-icon>
              <span>通用设置</span>
            </el-menu-item>
            <el-menu-item index="ai">
              <el-icon><Cpu /></el-icon>
              <span>AI 配置</span>
            </el-menu-item>
            <el-menu-item index="notification">
              <el-icon><Bell /></el-icon>
              <span>通知设置</span>
            </el-menu-item>
            <el-menu-item index="security">
              <el-icon><Lock /></el-icon>
              <span>安全设置</span>
            </el-menu-item>
            <el-menu-item index="backup">
              <el-icon><DocumentCopy /></el-icon>
              <span>数据备份</span>
            </el-menu-item>
          </el-menu>
        </div>
      </div>

      <!-- 右侧设置内容 -->
      <div class="col-span-9">
        <!-- 通用设置 -->
        <div v-if="activeMenu === 'general'" class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h3 class="font-bold text-lg mb-6">通用设置</h3>
          
          <div class="space-y-6">
            <div class="flex items-center justify-between py-4 border-b border-gray-100">
              <div>
                <div class="font-medium">系统名称</div>
                <div class="text-sm text-gray-500">显示在页面标题和Logo处</div>
              </div>
              <el-input v-model="settings.systemName" style="width: 300px" />
            </div>

            <div class="flex items-center justify-between py-4 border-b border-gray-100">
              <div>
                <div class="font-medium">默认学科</div>
                <div class="text-sm text-gray-500">新用户默认选择的学科</div>
              </div>
              <el-select v-model="settings.defaultSubject" style="width: 300px">
                <el-option label="语文" value="chinese" />
                <el-option label="数学" value="math" />
                <el-option label="英语" value="english" />
              </el-select>
            </div>

            <div class="flex items-center justify-between py-4 border-b border-gray-100">
              <div>
                <div class="font-medium">作业截止时间默认</div>
                <div class="text-sm text-gray-500">发布作业时的默认截止时间（天）</div>
              </div>
              <el-input-number v-model="settings.defaultDeadline" :min="1" :max="30" />
            </div>

            <div class="flex items-center justify-between py-4">
              <div>
                <div class="font-medium">显示学生排名</div>
                <div class="text-sm text-gray-500">是否在学生端显示班级排名</div>
              </div>
              <el-switch v-model="settings.showRanking" />
            </div>
          </div>
        </div>

        <!-- AI 配置 -->
        <div v-if="activeMenu === 'ai'" class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h3 class="font-bold text-lg mb-6">AI 配置</h3>
          
          <div class="space-y-6">
            <div class="flex items-center justify-between py-4 border-b border-gray-100">
              <div>
                <div class="font-medium">启用 AI 智能批改</div>
                <div class="text-sm text-gray-500">是否允许使用 AI 自动批改功能</div>
              </div>
              <el-switch v-model="settings.aiEnabled" />
            </div>

            <div class="flex items-center justify-between py-4 border-b border-gray-100">
              <div>
                <div class="font-medium">AI 模型版本</div>
                <div class="text-sm text-gray-500">选择使用的 AI 模型版本</div>
              </div>
              <el-select v-model="settings.aiModel" style="width: 300px">
                <el-option label="DeepSeek-V4 (推荐)" value="deepseek-v4" />
                <el-option label="DeepSeek-V3" value="deepseek-v3" />
                <el-option label="GPT-4" value="gpt-4" />
              </el-select>
            </div>

            <div class="flex items-center justify-between py-4 border-b border-gray-100">
              <div>
                <div class="font-medium">自动批改阈值</div>
                <div class="text-sm text-gray-500">置信度高于此值时自动通过（%）</div>
              </div>
              <el-slider v-model="settings.aiThreshold" :max="100" style="width: 300px" show-input />
            </div>

            <div class="flex items-center justify-between py-4">
              <div>
                <div class="font-medium">生成评语</div>
                <div class="text-sm text-gray-500">批改后自动生成评语</div>
              </div>
              <el-switch v-model="settings.aiCommentEnabled" />
            </div>
          </div>
        </div>

        <!-- 通知设置 -->
        <div v-if="activeMenu === 'notification'" class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h3 class="font-bold text-lg mb-6">通知设置</h3>
          
          <div class="space-y-6">
            <div class="flex items-center justify-between py-4 border-b border-gray-100">
              <div>
                <div class="font-medium">邮件通知</div>
                <div class="text-sm text-gray-500">通过邮件发送重要通知</div>
              </div>
              <el-switch v-model="settings.emailNotification" />
            </div>

            <div class="flex items-center justify-between py-4 border-b border-gray-100">
              <div>
                <div class="font-medium">短信通知</div>
                <div class="text-sm text-gray-500">通过短信发送紧急通知</div>
              </div>
              <el-switch v-model="settings.smsNotification" />
            </div>

            <div class="flex items-center justify-between py-4">
              <div>
                <div class="font-medium">作业截止提醒时间</div>
                <div class="text-sm text-gray-500">截止前多长时间发送提醒</div>
              </div>
              <el-select v-model="settings.reminderTime" style="width: 300px">
                <el-option label="截止前24小时" value="24" />
                <el-option label="截止前12小时" value="12" />
                <el-option label="截止前6小时" value="6" />
                <el-option label="截止前1小时" value="1" />
              </el-select>
            </div>
          </div>
        </div>

        <!-- 安全设置 -->
        <div v-if="activeMenu === 'security'" class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h3 class="font-bold text-lg mb-6">安全设置</h3>
          
          <div class="space-y-6">
            <div class="flex items-center justify-between py-4 border-b border-gray-100">
              <div>
                <div class="font-medium">密码强度要求</div>
                <div class="text-sm text-gray-500">设置用户密码的最小强度</div>
              </div>
              <el-select v-model="settings.passwordStrength" style="width: 300px">
                <el-option label="低（6位以上）" value="low" />
                <el-option label="中（字母+数字）" value="medium" />
                <el-option label="高（大小写+数字+符号）" value="high" />
              </el-select>
            </div>

            <div class="flex items-center justify-between py-4 border-b border-gray-100">
              <div>
                <div class="font-medium">登录失败锁定</div>
                <div class="text-sm text-gray-500">连续失败多少次后锁定账户</div>
              </div>
              <el-input-number v-model="settings.loginAttempts" :min="3" :max="10" />
            </div>

            <div class="flex items-center justify-between py-4">
              <div>
                <div class="font-medium">会话超时时间</div>
                <div class="text-sm text-gray-500">无操作多少分钟后自动登出</div>
              </div>
              <el-select v-model="settings.sessionTimeout" style="width: 300px">
                <el-option label="15分钟" value="15" />
                <el-option label="30分钟" value="30" />
                <el-option label="1小时" value="60" />
                <el-option label="2小时" value="120" />
              </el-select>
            </div>
          </div>
        </div>

        <!-- 数据备份 -->
        <div v-if="activeMenu === 'backup'" class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h3 class="font-bold text-lg mb-6">数据备份</h3>
          
          <div class="space-y-6">
            <div class="flex items-center justify-between py-4 border-b border-gray-100">
              <div>
                <div class="font-medium">自动备份</div>
                <div class="text-sm text-gray-500">启用自动备份功能</div>
              </div>
              <el-switch v-model="settings.autoBackup" />
            </div>

            <div class="flex items-center justify-between py-4 border-b border-gray-100">
              <div>
                <div class="font-medium">备份频率</div>
                <div class="text-sm text-gray-500">自动备份的执行频率</div>
              </div>
              <el-select v-model="settings.backupFrequency" style="width: 300px">
                <el-option label="每天" value="daily" />
                <el-option label="每周" value="weekly" />
                <el-option label="每月" value="monthly" />
              </el-select>
            </div>

            <div class="flex items-center justify-between py-4">
              <div>
                <div class="font-medium">保留备份数</div>
                <div class="text-sm text-gray-500">最多保留多少个历史备份</div>
              </div>
              <el-input-number v-model="settings.backupRetention" :min="3" :max="30" />
            </div>

            <div class="pt-4 border-t border-gray-100">
              <el-button type="primary" @click="manualBackup" :loading="backingUp">
                <el-icon class="mr-1"><Download /></el-icon>
                立即手动备份
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { 
  Setting, 
  Cpu, 
  Bell, 
  Lock, 
  DocumentCopy, 
  Check,
  Download 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const activeMenu = ref('general')
const saving = ref(false)
const backingUp = ref(false)

// 设置数据
const settings = reactive({
  // 通用设置
  systemName: '智能作业批改系统',
  defaultSubject: 'chinese',
  defaultDeadline: 7,
  showRanking: true,
  
  // AI 配置
  aiEnabled: true,
  aiModel: 'deepseek-v4',
  aiThreshold: 80,
  aiCommentEnabled: true,
  
  // 通知设置
  emailNotification: true,
  smsNotification: false,
  reminderTime: '24',
  
  // 安全设置
  passwordStrength: 'medium',
  loginAttempts: 5,
  sessionTimeout: '30',
  
  // 数据备份
  autoBackup: true,
  backupFrequency: 'weekly',
  backupRetention: 10
})

// 菜单选择
const handleMenuSelect = (index: string) => {
  activeMenu.value = index
}

// 保存设置
const saveSettings = async () => {
  saving.value = true
  setTimeout(() => {
    saving.value = false
    ElMessage.success('系统设置保存成功！')
  }, 1500)
}

// 手动备份
const manualBackup = async () => {
  backingUp.value = true
  setTimeout(() => {
    backingUp.value = false
    ElMessage.success('数据备份完成！')
  }, 2000)
}
</script>
