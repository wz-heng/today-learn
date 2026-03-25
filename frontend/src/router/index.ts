import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { guest: true },
    },
    {
      path: '/register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: { guest: true },
    },
    {
      path: '/',
      component: () => import('@/views/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/today' },
        { path: 'today', component: () => import('@/views/plan/TodayView.vue') },
        { path: 'archive', component: () => import('@/views/archive/ArchiveView.vue') },
        { path: 'settings', component: () => import('@/views/settings/SettingsView.vue') },
      ],
    },
  ],
})

// 路由守卫：未登录跳登录页
router.beforeEach((to) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn()) {
    return '/login'
  }
  if (to.meta.guest && userStore.isLoggedIn()) {
    return '/today'
  }
})

export default router
