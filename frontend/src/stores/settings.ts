import { defineStore } from 'pinia'
import { ref } from 'vue'

export const AI_MODELS = [
  { value: 'deepseek-chat',            label: 'DeepSeek Chat',         provider: 'DeepSeek', tag: '推荐' },
  { value: 'deepseek-reasoner',        label: 'DeepSeek Reasoner',     provider: 'DeepSeek', tag: '推理' },
  { value: 'claude-3-5-haiku-20241022',  label: 'Claude 3.5 Haiku',   provider: 'Claude',   tag: '快速' },
  { value: 'claude-3-5-sonnet-20241022', label: 'Claude 3.5 Sonnet',  provider: 'Claude',   tag: '强大' },
]

export const useSettingsStore = defineStore('settings', () => {
  const aiModel = ref<string>(localStorage.getItem('aiModel') ?? 'deepseek-chat')

  function setAiModel(model: string) {
    aiModel.value = model
    localStorage.setItem('aiModel', model)
  }

  return { aiModel, setAiModel }
})
