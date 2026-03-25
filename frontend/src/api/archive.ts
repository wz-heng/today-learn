import request from './request'
import type { Result, ArchiveVO, StatsVO } from '@/types'

export const archiveApi = {
  list: (page = 1, size = 10) =>
    request.get<never, Result<ArchiveVO[]>>('/archive', { params: { page, size } }),

  getStats: () =>
    request.get<never, Result<StatsVO>>('/archive/stats'),
}
