<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">用户管理</h2>
        <p class="text-gray-500 mt-1">管理系统教师、学生账号，支持批量导入</p>
      </div>
      <div class="flex gap-3">
        <el-button type="primary" @click="showCreateDialog">
          <el-icon class="mr-1"><Plus /></el-icon>新建用户
        </el-button>
        <el-button @click="showImportDialog">
          <el-icon class="mr-1"><Upload /></el-icon>批量导入
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部角色" clearable style="width: 120px">
            <el-option label="管理员" :value="1" />
            <el-option label="教师" :value="2" />
            <el-option label="学生" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="姓名/账号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never">
      <el-table :data="userList" v-loading="loading" stripe>
        <el-table-column type="index" width="50" />
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">{{ getRoleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="学科/班级" min-width="150">
          <template #default="{ row }">
            <span v-if="row.role === 2">{{ row.subjectName || '-' }}</span>
            <span v-else-if="row.role === 3">{{ row.className || '-' }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="flex justify-end mt-4">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新建/编辑用户对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新建用户'" width="550px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="教师用工号，学生用学号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="form.role" :disabled="isEdit">
            <el-radio-button :label="1">管理员</el-radio-button>
            <el-radio-button :label="2">教师</el-radio-button>
            <el-radio-button :label="3">学生</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="学科" v-if="form.role === 2" prop="subjectId">
          <el-select v-model="form.subjectId" placeholder="选择学科" style="width: 100%">
            <el-option v-for="item in subjects" :key="item.id" :label="item.subjectName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" v-if="form.role === 3" prop="classId">
          <el-select v-model="form.classId" placeholder="选择班级" style="width: 100%">
            <el-option v-for="item in classes" :key="item.id" :label="item.className" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="初始密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog v-model="importVisible" title="批量导入用户" width="500px">
      <div class="space-y-4">
        <el-alert type="info" :closable="false">
          <template #title>
            <div class="flex items-center gap-2">
              <el-icon><InfoFilled /></el-icon>
              <span>请按照模板格式上传 Excel 文件</span>
            </div>
          </template>
          <div class="mt-2 text-sm">
            <p>• 支持 .xlsx, .xls 格式</p>
            <p>• 第一行为表头，包含：账号、姓名、角色、学科/班级、联系电话</p>
            <p>• 每次最多导入 500 条记录</p>
          </div>
        </el-alert>
        <el-upload
          drag
          action="#"
          :auto-upload="false"
          accept=".xlsx,.xls"
          :on-change="handleFileChange"
          class="w-full"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
        </el-upload>
      </div>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImport" :loading="importLoading" :disabled="!importFile">
          开始导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { Plus, Upload, Search, InfoFilled, UploadFilled } from '@element-plus/icons-vue'
import type { User, Subject, Class } from '@/types'
import { userApi } from '@/api/system'

const loading = ref(false)
const submitLoading = ref(false)
const importLoading = ref(false)
const dialogVisible = ref(false)
const importVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const importFile = ref<UploadFile | null>(null)

const userList = ref<User[]>([])
const subjects = ref<Subject[]>([])
const classes = ref<Class[]>([])

const searchForm = reactive({
  role: undefined as number | undefined,
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive<Partial<User>>({
  username: '',
  realName: '',
  role: 3,
  subjectId: undefined,
  classId: undefined,
  phone: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  subjectId: [{ required: true, message: '请选择学科', trigger: 'change' }],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  password: [{ required: !isEdit.value, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }]
}

const getRoleType = (role: number) => {
  const map: Record<number, string> = { 1: 'danger', 2: 'warning', 3: 'success' }
  return map[role] || 'info'
}

const getRoleText = (role: number) => {
  const map: Record<number, string> = { 1: '管理员', 2: '教师', 3: '学生' }
  return map[role] || '未知'
}

const fetchUsers = async () => {
  loading.value = true
  try {
    const result = await userApi.getUserList({
      page: pagination.page,
      size: pagination.size,
      role: searchForm.role,
      keyword: searchForm.keyword
    })
    userList.value = result.records
    pagination.total = result.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchUsers()
}

const resetSearch = () => {
  searchForm.role = undefined
  searchForm.keyword = ''
  handleSearch()
}

const handleSizeChange = (val: number) => {
  pagination.size = val
  fetchUsers()
}

const handlePageChange = (val: number) => {
  pagination.page = val
  fetchUsers()
}

const showCreateDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    username: '',
    realName: '',
    role: 3,
    subjectId: undefined,
    classId: undefined,
    phone: '',
    password: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row: User) => {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      if (isEdit.value && form.id) {
        await userApi.updateUser(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await userApi.createUser(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchUsers()
    } finally {
      submitLoading.value = false
    }
  })
}

const handleToggleStatus = async (row: User) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定${actionText}该用户吗？`, '提示', { type: 'warning' })
    await userApi.updateUser(row.id, { status: newStatus })
    ElMessage.success(`${actionText}成功`)
    fetchUsers()
  } catch {
    // 取消操作
  }
}

const handleDelete = async (row: User) => {
  try {
    await ElMessageBox.confirm('确定删除该用户吗？此操作不可恢复！', '警告', { type: 'error' })
    await userApi.deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch {
    // 取消操作
  }
}

const showImportDialog = () => {
  importVisible.value = true
  importFile.value = null
}

const handleFileChange = (file: UploadFile) => {
  importFile.value = file
}

const handleImport = async () => {
  if (!importFile.value) return
  
  importLoading.value = true
  try {
    // 实际项目中这里应该调用导入 API
    await new Promise(resolve => setTimeout(resolve, 1500))
    ElMessage.success('导入成功')
    importVisible.value = false
    fetchUsers()
  } finally {
    importLoading.value = false
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.search-card :deep(.el-card__body) {
  padding-bottom: 0;
}
</style>
