import { http } from './request'
import type { 
  PageResult, 
  User, 
  LoginForm, 
  RegisterForm,
  Subject,
  Class,
  Course
} from '@/types'

// 认证相关 API
export const authApi = {
  // 用户登录
  login(data: LoginForm) {
    return http.post<{ token: string; user: User }>('/auth/login', data)
  },
  
  // 用户注册
  register(data: RegisterForm) {
    return http.post<{ token: string; user: User }>('/auth/register', data)
  },
  
  // 获取当前用户信息
  getCurrentUser() {
    return http.get<User>('/auth/current')
  },
  
  // 修改密码
  changePassword(data: { oldPassword: string; newPassword: string }) {
    return http.post('/auth/change-password', data)
  },
  
  // 退出登录
  logout() {
    return http.post('/auth/logout')
  }
}

// 用户管理 API (管理员)
export const userApi = {
  // 获取用户列表
  getUserList(params: { page?: number; size?: number; role?: number; keyword?: string }) {
    return http.get<PageResult<User>>('/users', { params })
  },
  
  // 获取用户详情
  getUserById(id: number) {
    return http.get<User>(`/users/${id}`)
  },
  
  // 创建用户
  createUser(data: Partial<User>) {
    return http.post<User>('/users', data)
  },
  
  // 更新用户
  updateUser(id: number, data: Partial<User>) {
    return http.put<User>(`/users/${id}`, data)
  },
  
  // 删除用户
  deleteUser(id: number) {
    return http.delete(`/users/${id}`)
  },
  
  // 批量导入用户
  batchImportUsers(data: Partial<User>[]) {
    return http.post('/users/batch', data)
  }
}

// 学科管理 API (管理员)
export const subjectApi = {
  // 获取学科列表
  getSubjectList(params?: { keyword?: string }) {
    return http.get<Subject[]>('/subjects', { params })
  },
  
  // 获取学科详情
  getSubjectById(id: number) {
    return http.get<Subject>(`/subjects/${id}`)
  },
  
  // 创建学科
  createSubject(data: Partial<Subject>) {
    return http.post<Subject>('/subjects', data)
  },
  
  // 更新学科
  updateSubject(id: number, data: Partial<Subject>) {
    return http.put<Subject>(`/subjects/${id}`, data)
  },
  
  // 删除学科
  deleteSubject(id: number) {
    return http.delete(`/subjects/${id}`)
  }
}

// 班级管理 API (管理员)
export const classApi = {
  // 获取班级列表
  getClassList(params?: { keyword?: string; grade?: string }) {
    return http.get<Class[]>('/classes', { params })
  },
  
  // 获取班级详情
  getClassById(id: number) {
    return http.get<Class>(`/classes/${id}`)
  },
  
  // 创建班级
  createClass(data: Partial<Class>) {
    return http.post<Class>('/classes', data)
  },
  
  // 更新班级
  updateClass(id: number, data: Partial<Class>) {
    return http.put<Class>(`/classes/${id}`, data)
  },
  
  // 删除班级
  deleteClass(id: number) {
    return http.delete(`/classes/${id}`)
  }
}

// 课程管理 API (教师)
export const courseApi = {
  // 获取课程列表
  getCourseList(params?: { teacherId?: number; classId?: number; semester?: string }) {
    return http.get<Course[]>('/courses', { params })
  },
  
  // 获取课程详情
  getCourseById(id: number) {
    return http.get<Course>(`/courses/${id}`)
  },
  
  // 创建课程
  createCourse(data: Partial<Course>) {
    return http.post<Course>('/courses', data)
  },
  
  // 更新课程
  updateCourse(id: number, data: Partial<Course>) {
    return http.put<Course>(`/courses/${id}`, data)
  },
  
  // 删除课程
  deleteCourse(id: number) {
    return http.delete(`/courses/${id}`)
  }
}
