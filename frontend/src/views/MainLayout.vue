<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="aside">
      <div class="logo">📚 今天学点啥</div>

      <el-menu router :default-active="$route.path" class="menu">
        <el-menu-item index="/today">
          <el-icon><Calendar /></el-icon>
          <span>今日计划</span>
        </el-menu-item>
        <el-menu-item index="/archive">
          <el-icon><Collection /></el-icon>
          <span>学习归档</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <span>偏好设置</span>
        </el-menu-item>
      </el-menu>

      <div class="user-info">
        <el-avatar :size="32" class="avatar">
          {{ userStore.userInfo?.displayName?.[0] ?? '我' }}
        </el-avatar>
        <span class="username">{{ userStore.userInfo?.displayName ?? '用户' }}</span>
        <el-button link size="small" @click="logout">退出</el-button>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
}
.aside {
  background: #1e1e2e;
  display: flex;
  flex-direction: column;
  border-right: none;
}
.logo {
  padding: 24px 20px;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.5px;
}
.menu {
  flex: 1;
  background: transparent;
  border: none;
}
.menu :deep(.el-menu-item) {
  color: #a0a8c0;
}
.menu :deep(.el-menu-item.is-active),
.menu :deep(.el-menu-item:hover) {
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}
.avatar {
  background: #667eea;
  flex-shrink: 0;
  font-size: 14px;
}
.username {
  color: #c0c4cc;
  font-size: 13px;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.main {
  background: #f5f7fa;
  overflow-y: auto;
}
</style>
