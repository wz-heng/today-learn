<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { preferenceApi } from '@/api/auth'
import { topicApi } from '@/api/topic'
import { useSettingsStore, AI_MODELS } from '@/stores/settings'
import type { TopicVO } from '@/types'

const settingsStore = useSettingsStore()
const topics = ref<TopicVO[]>([])
const form = ref({ enableAutoRecommend: true, topicIds: [] as string[] })
const saving = ref(false)

async function loadData() {
  const [prefRes, topicRes] = await Promise.all([
    preferenceApi.get(),
    topicApi.listAll(),
  ])
  form.value = {
    enableAutoRecommend: prefRes.data.enableAutoRecommend,
    topicIds: prefRes.data.topicIds,
  }
  topics.value = topicRes.data
}

async function save() {
  saving.value = true
  try {
    await preferenceApi.save(form.value)
    ElMessage.success('设置已保存')
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div class="settings-view">
    <h2 class="page-title">偏好设置</h2>

    <!-- AI 模型设置 -->
    <el-card class="settings-card" shadow="never">
      <div class="section-title">🤖 AI 模型</div>
      <div class="model-list">
        <div
          v-for="m in AI_MODELS"
          :key="m.value"
          class="model-item"
          :class="{ active: settingsStore.aiModel === m.value }"
          @click="settingsStore.setAiModel(m.value)"
        >
          <div class="model-left">
            <el-radio :model-value="settingsStore.aiModel" :value="m.value" @change="settingsStore.setAiModel(m.value)" />
            <div>
              <div class="model-name">{{ m.label }}</div>
              <div class="model-provider">{{ m.provider }}</div>
            </div>
          </div>
          <el-tag size="small" :type="m.tag === '推荐' ? 'success' : m.tag === '推理' ? 'warning' : 'info'">
            {{ m.tag }}
          </el-tag>
        </div>
      </div>
      <p class="hint">选择的模型会在生成计划时使用，需在后端 application-dev.yml 中配置对应的 API Key</p>
    </el-card>

    <!-- 学习偏好设置 -->
    <el-card class="settings-card" shadow="never" style="margin-top: 16px">
      <div class="section-title">📚 学习偏好</div>
      <el-form label-width="140px">
        <el-form-item label="自动推荐方向">
          <el-switch v-model="form.enableAutoRecommend" />
          <span class="hint">开启后，生成计划时系统会根据你的历史自动推荐学习方向</span>
        </el-form-item>

        <el-form-item label="偏好学习方向">
          <div class="topic-groups">
            <div v-for="group in topics" :key="group.id" class="topic-group">
              <div class="group-name">{{ group.icon }} {{ group.name }}</div>
              <div class="topic-chips">
                <el-check-tag
                  v-for="child in group.children"
                  :key="child.id"
                  :checked="form.topicIds.includes(child.id)"
                  @change="(checked: boolean) => {
                    if (checked) form.topicIds.push(child.id)
                    else form.topicIds = form.topicIds.filter(id => id !== child.id)
                  }"
                  class="topic-chip"
                >
                  {{ child.name }}
                </el-check-tag>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.settings-view {
  padding: 24px;
  max-width: 700px;
  margin: 0 auto;
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 20px;
  color: #303133;
}
.settings-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}
.model-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}
.model-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
}
.model-item:hover { border-color: #c6e2ff; background: #f5f9ff; }
.model-item.active { border-color: #409eff; background: #ecf5ff; }
.model-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.model-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}
.model-provider {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
.hint {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}
p.hint { margin-left: 0; }
.topic-groups {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}
.group-name {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}
.topic-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.topic-chip { cursor: pointer; }
</style>
