import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, Subject, Class } from '@/types'
import { authApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<User | null>(null)
  const subjects = ref<Subject[]>([])
  const classes = ref<Class[]>([])
  
  // Getters
  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 1)
  const isTeacher = computed(() => user.value?.role === 2)
  const isStudent = computed(() => user.value?.role === 3)
  const currentUser = computed(() => user.value)
  
  // Actions
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  const setUser = (newUser: User) => {
    user.value = newUser
    localStorage.setItem('user', JSON.stringify(newUser))
  }
  
  const clearAuth = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }
  
  // 登录 - 真实API
  const login = async (loginData: { username: string; password: string; role?: number }) => {
    const result = await authApi.login(loginData)
    setToken(result.token)
    setUser(result.user)
    return result.user
  }
  
  // 注册 - 真实API
  const register = async (registerData: {
    username: string
    password: string
    confirmPassword: string
    realName: string
    role: number
    subjectId?: number
    classId?: number
    phone?: string
  }) => {
    const result = await authApi.register(registerData)
    setToken(result.token)
    setUser(result.user)
    return result.user
  }
  
  // 获取当前用户信息
  const fetchCurrentUser = async () => {
    if (!token.value || !user.value) return null
    try {
      const userData = await authApi.login({
        username: user.value.username,
        password: '',
        role: user.value.role
      })
      setUser(userData.user)
      return userData.user
    } catch {
      clearAuth()
      return null
    }
  }
  
  // 退出登录
  const logout = async () => {
    clearAuth()
  }
  
  // 从 localStorage 恢复用户状态
  const restoreUser = () => {
    const savedToken = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')
    
    if (savedToken && savedUser) {
      token.value = savedToken
      try {
        user.value = JSON.parse(savedUser)
      } catch {
        clearAuth()
      }
    }
  }
  
  return {
    token,
    user,
    subjects,
    classes,
    isLoggedIn,
    isAdmin,
    isTeacher,
    isStudent,
    currentUser,
    setToken,
    setUser,
    clearAuth,
    login,
    register,
    fetchCurrentUser,
    logout,
    restoreUser
  }
})
