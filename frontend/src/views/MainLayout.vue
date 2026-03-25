<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="layout">
    <!-- PC 侧边栏 -->
    <aside class="sidebar">
      <div class="logo">📚 今天学点啥</div>

      <el-menu router :default-active="route.path" class="menu">
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
    </aside>

    <!-- 主内容区 -->
    <main class="main">
      <!-- 移动端顶部栏 -->
      <div class="mobile-header">
        <span class="mobile-title">📚 今天学点啥</span>
        <el-button link @click="logout" style="color:#fff; font-size:13px">退出</el-button>
      </div>

      <div class="page-content">
        <router-view />
      </div>
    </main>

    <!-- 移动端底部导航 -->
    <nav class="bottom-nav">
      <router-link to="/today" class="nav-item" :class="{ active: route.path === '/today' }">
        <el-icon><Calendar /></el-icon>
        <span>今日</span>
      </router-link>
      <router-link to="/archive" class="nav-item" :class="{ active: route.path === '/archive' }">
        <el-icon><Collection /></el-icon>
        <span>归档</span>
      </router-link>
      <router-link to="/settings" class="nav-item" :class="{ active: route.path === '/settings' }">
        <el-icon><Setting /></el-icon>
        <span>设置</span>
      </router-link>
    </nav>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  height: 100dvh;
}

/* ---- PC 侧边栏 ---- */
.sidebar {
  width: 220px;
  background: #1e1e2e;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.logo {
  padding: 24px 20px;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
}
.menu {
  flex: 1;
  background: transparent;
  border: none;
}
.menu :deep(.el-menu-item) { color: #a0a8c0; }
.menu :deep(.el-menu-item.is-active),
.menu :deep(.el-menu-item:hover) {
  color: #fff;
  background: rgba(255,255,255,0.08);
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-top: 1px solid rgba(255,255,255,0.08);
}
.avatar { background: #667eea; flex-shrink: 0; font-size: 14px; }
.username {
  color: #c0c4cc;
  font-size: 13px;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ---- 主内容 ---- */
.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  overflow: hidden;
}
.mobile-header {
  display: none;
  background: #667eea;
  padding: 0 16px;
  height: 52px;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}
.mobile-title {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
}
.page-content {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

/* ---- 移动端底部导航 ---- */
.bottom-nav { display: none; }

/* ---- 移动端适配 ---- */
@media (max-width: 768px) {
  .sidebar { display: none; }
  .mobile-header { display: flex; }
  .bottom-nav {
    display: flex;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    height: 60px;
    background: #fff;
    border-top: 1px solid #ebeef5;
    z-index: 100;
    padding-bottom: env(safe-area-inset-bottom);
  }
  .nav-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 3px;
    color: #909399;
    text-decoration: none;
    font-size: 11px;
    transition: color 0.15s;
  }
  .nav-item .el-icon { font-size: 22px; }
  .nav-item.active { color: #667eea; }
  .page-content { padding-bottom: 68px; }
}
</style>
