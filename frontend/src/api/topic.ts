import request from './request'
import type { Result, TopicVO } from '@/types'

export const topicApi = {
  listAll: () => request.get<never, Result<TopicVO[]>>('/topics'),
}
