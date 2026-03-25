<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { archiveApi } from '@/api/archive'
import type { ArchiveVO, StatsVO } from '@/types'

const stats = ref<StatsVO | null>(null)
const archives = ref<ArchiveVO[]>([])
const loading = ref(false)
const page = ref(1)
const noMore = ref(false)

async function loadStats() {
  const res = await archiveApi.getStats()
  stats.value = res.data
}

async function loadArchives(reset = false) {
  if (noMore.value && !reset) return
  loading.value = true
  try {
    if (reset) {
      page.value = 1
      archives.value = []
      noMore.value = false
    }
    const res = await archiveApi.list(page.value, 10)
    archives.value.push(...res.data)
    if (res.data.length < 10) noMore.value = true
    else page.value++
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
  loadArchives(true)
})
</script>

<template>
  <div class="archive-view">
    <h2 class="page-title">学习归档</h2>

    <!-- 统计卡片 -->
    <div class="stats-row" v-if="stats">
      <el-card class="stat-card" shadow="never">
        <div class="stat-num">{{ stats.totalDays }}</div>
        <div class="stat-label">累计学习天数</div>
      </el-card>
      <el-card class="stat-card" shadow="never">
        <div class="stat-num">{{ stats.totalTasks }}</div>
        <div class="stat-label">完成任务总数</div>
      </el-card>
      <el-card class="stat-card stat-streak" shadow="never">
        <div class="stat-num streak">{{ stats.currentStreak }} 🔥</div>
        <div class="stat-label">当前连续天数</div>
      </el-card>
      <el-card class="stat-card" shadow="never">
        <div class="stat-num">{{ stats.longestStreak }}</div>
        <div class="stat-label">历史最长连续</div>
      </el-card>
    </div>

    <!-- 归档列表 -->
    <div v-if="archives.length === 0 && !loading" class="empty">
      <el-empty description="暂无学习记录" />
    </div>

    <el-card
      v-for="item in archives"
      :key="item.planId"
      class="archive-card"
      shadow="never"
    >
      <div class="archive-header">
        <div>
          <span class="archive-date">{{ item.planDate }}</span>
          <el-tag
            v-for="name in item.topicNames"
            :key="name"
            size="small"
            class="topic-tag"
          >{{ name }}</el-tag>
        </div>
        <div class="archive-summary">
          <span>{{ item.availableMinutes }}分钟</span>
          <span class="divider">·</span>
          <span>完成 {{ item.completedTasks }}/{{ item.totalTasks }} 个任务</span>
          <el-progress
            :percentage="item.totalTasks ? Math.round(item.completedTasks / item.totalTasks * 100) : 0"
            :stroke-width="6"
            style="width: 80px; display: inline-flex; margin-left: 8px"
          />
        </div>
      </div>

      <el-collapse>
        <el-collapse-item title="查看任务详情" :name="item.planId">
          <div class="task-row" v-for="task in item.tasks" :key="task.taskId">
            <el-icon v-if="task.status === 'completed'" style="color:#67c23a"><CircleCheck /></el-icon>
            <el-icon v-else style="color:#909399"><Remove /></el-icon>
            <span class="task-title" :class="{ skipped: task.status === 'skipped' }">
              {{ task.title }}
            </span>
            <el-tag size="small" effect="plain" style="margin-left: auto">{{ task.topicName }}</el-tag>
            <el-tag size="small" :type="task.difficulty === 'hard' ? 'danger' : task.difficulty === 'medium' ? 'warning' : 'success'">
              {{ { easy:'简单', medium:'中等', hard:'困难' }[task.difficulty] ?? task.difficulty }}
            </el-tag>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <div class="load-more">
      <el-button v-if="!noMore" :loading="loading" @click="loadArchives()">加载更多</el-button>
      <span v-else-if="archives.length > 0" class="no-more">已加载全部记录</span>
    </div>
  </div>
</template>

<style scoped>
@media (max-width: 768px) {
  .archive-view { padding: 12px; }
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .archive-header { flex-direction: column; align-items: flex-start; }
}
.archive-view {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 20px;
  color: #303133;
}
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}
.stat-card {
  text-align: center;
  border-radius: 10px;
  border: 1px solid #ebeef5;
}
.stat-num {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}
.stat-num.streak { color: #e6a23c; }
.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.archive-card {
  margin-bottom: 12px;
  border-radius: 10px;
  border: 1px solid #ebeef5;
}
.archive-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.archive-date {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-right: 8px;
}
.topic-tag {
  margin-right: 4px;
}
.archive-summary {
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
}
.divider { margin: 0 6px; }
.task-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}
.task-row:last-child { border-bottom: none; }
.task-title { flex: 1; color: #303133; }
.task-title.skipped { text-decoration: line-through; color: #909399; }
.load-more {
  text-align: center;
  padding: 24px 0;
}
.no-more { color: #c0c4cc; font-size: 13px; }
.empty { padding: 60px 0; }
</style>
