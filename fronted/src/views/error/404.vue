<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-50 to-gray-100">
    <div class="text-center">
      <div class="text-9xl font-black text-primary-200 mb-4">404</div>
      <h1 class="text-3xl font-bold text-gray-800 mb-2">页面不存在</h1>
      <p class="text-gray-500 mb-8">抱歉，您访问的页面可能已被删除或不存在</p>
      <el-button type="primary" size="large" @click="router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>
