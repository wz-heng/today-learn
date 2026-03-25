<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { planApi } from '@/api/plan'
import { topicApi } from '@/api/topic'
import { useSettingsStore } from '@/stores/settings'
import type { TodayPlanVO, TopicVO } from '@/types'

const settingsStore = useSettingsStore()

const plan = ref<TodayPlanVO | null>(null)
const topics = ref<TopicVO[]>([])
const loading = ref(false)
const generating = ref(false)

// 生成计划表单
const showGenerate = ref(false)
const genForm = ref({ availableMinutes: 30, topicIds: [] as string[] })

// 所有叶子节点（二级方向）
const leafTopics = computed(() => {
  const result: TopicVO[] = []
  topics.value.forEach((t) => {
    if (t.children.length > 0) {
      result.push(...t.children)
    } else {
      result.push(t)
    }
  })
  return result
})

const completedCount = computed(
  () => plan.value?.tasks.filter((t) => t.status === 'completed').length ?? 0,
)
const totalCount = computed(() => plan.value?.tasks.length ?? 0)
const progress = computed(() =>
  totalCount.value === 0 ? 0 : Math.round((completedCount.value / totalCount.value) * 100),
)

async function loadData() {
  loading.value = true
  try {
    const [planRes, topicRes] = await Promise.all([planApi.getToday(), topicApi.listAll()])
    plan.value = planRes.data
    topics.value = topicRes.data
  } finally {
    loading.value = false
  }
}

async function generatePlan() {
  generating.value = true
  try {
    const res = await planApi.generate({
      availableMinutes: genForm.value.availableMinutes,
      topicIds: genForm.value.topicIds.length > 0 ? genForm.value.topicIds : undefined,
      model: settingsStore.aiModel,
    })
    plan.value = res.data
    showGenerate.value = false
    ElMessage.success('计划生成成功！已替换旧计划')
  } finally {
    generating.value = false
  }
}

async function updateStatus(taskId: string, status: 'completed' | 'skipped') {
  await planApi.updateTaskStatus(taskId, status)
  const task = plan.value?.tasks.find((t) => t.id === taskId)
  if (task) task.status = status
  // 检查是否全部完成
  if (plan.value && plan.value.tasks.every((t) => t.status !== 'pending')) {
    plan.value.status = 'completed'
  }
}

function difficultyTag(d: string) {
  return { easy: 'success', medium: 'warning', hard: 'danger' }[d] ?? 'info'
}
function difficultyLabel(d: string) {
  return { easy: '简单', medium: '中等', hard: '困难' }[d] ?? d
}

onMounted(loadData)
</script>

<template>
  <div class="today-view" v-loading="loading">
    <!-- 无计划状态 -->
    <div v-if="!plan" class="empty-state">
      <el-empty description="今天还没有学习计划">
        <el-button type="primary" size="large" @click="showGenerate = true">
          <el-icon><MagicStick /></el-icon>
          生成今日计划
        </el-button>
      </el-empty>
    </div>

    <!-- 有计划状态 -->
    <div v-else>
      <!-- 顶部信息栏 -->
      <div class="plan-header">
        <div class="plan-meta">
          <h2 class="plan-date">{{ plan.planDate }} 的学习计划</h2>
          <div class="plan-tags">
            <el-tag v-for="name in plan.topicNames" :key="name" size="small" class="topic-tag">
              {{ name }}
            </el-tag>
            <el-tag size="small" type="info">{{ plan.availableMinutes }} 分钟</el-tag>
            <el-tag size="small" :type="plan.status === 'completed' ? 'success' : 'primary'">
              {{ plan.status === 'completed' ? '已完成 🎉' : '进行中' }}
            </el-tag>
          </div>
        </div>
        <!-- 进度环 -->
        <el-progress
          type="circle"
          :percentage="progress"
          :width="80"
          :stroke-width="8"
          :color="plan.status === 'completed' ? '#67c23a' : '#409eff'"
        >
          <template #default="{ percentage }">
            <span class="progress-text">{{ completedCount }}/{{ totalCount }}</span>
          </template>
        </el-progress>
      </div>

      <!-- 任务列表 -->
      <div class="task-list">
        <el-card
          v-for="task in plan.tasks"
          :key="task.id"
          class="task-card"
          :class="{ done: task.status !== 'pending' }"
          shadow="never"
        >
          <div class="task-header">
            <div class="task-title-row">
              <el-icon v-if="task.status === 'completed'" class="check-icon"><CircleCheck /></el-icon>
              <el-icon v-else-if="task.status === 'skipped'" class="skip-icon"><Remove /></el-icon>
              <el-icon v-else class="pending-icon"><Clock /></el-icon>
              <span class="task-title" :class="{ 'line-through': task.status === 'skipped' }">
                {{ task.title }}
              </span>
            </div>
            <div class="task-badges">
              <el-tag size="small" :type="difficultyTag(task.difficulty)">
                {{ difficultyLabel(task.difficulty) }}
              </el-tag>
              <el-tag size="small" type="info">{{ task.estimatedMinutes }}分钟</el-tag>
              <el-tag size="small" effect="plain">{{ task.topicName }}</el-tag>
            </div>
          </div>

          <p class="task-content" style="white-space: pre-wrap;">{{ task.content }}</p>

          <div v-if="task.status === 'pending'" class="task-actions">
            <el-button type="success" size="small" @click="updateStatus(task.id, 'completed')">
              <el-icon><Check /></el-icon> 完成
            </el-button>
            <el-button size="small" @click="updateStatus(task.id, 'skipped')">
              跳过
            </el-button>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 生成计划对话框 -->
    <el-dialog v-model="showGenerate" title="生成今日学习计划" width="480px" :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="可用时间">
          <div class="slider-wrap">
            <div class="time-label">{{ genForm.availableMinutes }} 分钟</div>
            <el-slider
              v-model="genForm.availableMinutes"
              :min="15"
              :max="180"
              :step="15"
              show-stops
              :marks="{ 15: '15m', 30: '30m', 60: '1h', 120: '2h', 180: '3h' }"
            />
          </div>
        </el-form-item>
        <el-form-item label="指定方向" style="margin-top: 24px;">
          <el-select
            v-model="genForm.topicIds"
            multiple
            placeholder="不选则自动推荐"
            style="width: 100%"
            clearable
          >
            <el-option-group v-for="group in topics" :key="group.id" :label="group.name">
              <el-option
                v-for="child in group.children"
                :key="child.id"
                :label="child.name"
                :value="child.id"
              />
            </el-option-group>
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showGenerate = false">取消</el-button>
        <el-button type="primary" :loading="generating" @click="generatePlan">
          <el-icon><MagicStick /></el-icon>
          AI 生成
        </el-button>
      </template>
    </el-dialog>

    <!-- 浮动按钮（已有计划时重新生成） -->
    <el-tooltip v-if="plan" content="重新生成计划" placement="left">
      <el-button
        type="primary"
        circle
        size="large"
        class="fab"
        @click="showGenerate = true"
      >
        <el-icon><Refresh /></el-icon>
      </el-button>
    </el-tooltip>
  </div>
</template>

<style scoped>
.today-view {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  min-height: calc(100vh - 48px);
}
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60vh;
}
.plan-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 24px;
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.plan-date {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 10px;
  color: #303133;
}
.plan-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.topic-tag {
  background: #ecf5ff;
  color: #409eff;
  border-color: #d9ecff;
}
.progress-text {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}
.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.task-card {
  border-radius: 10px;
  border: 1px solid #ebeef5;
  transition: all 0.2s;
}
.task-card.done {
  opacity: 0.65;
}
.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 10px;
}
.task-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.task-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}
.line-through {
  text-decoration: line-through;
  color: #909399;
}
.check-icon { color: #67c23a; font-size: 18px; }
.skip-icon  { color: #909399; font-size: 18px; }
.pending-icon { color: #409eff; font-size: 18px; }
.task-badges {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}
.task-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.7;
  margin: 0 0 12px;
}
.task-actions {
  display: flex;
  gap: 8px;
}
.fab {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 52px !important;
  height: 52px !important;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}
.slider-wrap {
  width: 100%;
}
.time-label {
  font-size: 13px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 8px;
}
</style>
