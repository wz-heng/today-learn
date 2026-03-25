import request from './request'
import type { Result, TodayPlanVO } from '@/types'

export const planApi = {
  generate: (data: { availableMinutes: number; topicIds?: string[]; model?: string }) =>
    request.post<never, Result<TodayPlanVO>>('/plans/generate', data, { timeout: 120000 }),

  getToday: () =>
    request.get<never, Result<TodayPlanVO | null>>('/plans/today'),

  updateTaskStatus: (taskId: string, status: 'completed' | 'skipped') =>
    request.patch<never, Result>(`/tasks/${taskId}/status`, { status }),
}
