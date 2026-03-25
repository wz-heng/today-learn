import request from './request'
import type { Result, UserLoginVO, UserInfoVO, UserPreferenceVO } from '@/types'

export const authApi = {
  register: (data: { email: string; password: string; displayName?: string }) =>
    request.post<never, Result>('/auth/register', data),

  login: (data: { email: string; password: string }) =>
    request.post<never, Result<UserLoginVO>>('/auth/login', data),

  getMyInfo: () =>
    request.get<never, Result<UserInfoVO>>('/auth/me'),
}

export const preferenceApi = {
  get: () =>
    request.get<never, Result<UserPreferenceVO>>('/user/preferences'),

  save: (data: { enableAutoRecommend: boolean; topicIds: string[] }) =>
    request.put<never, Result>('/user/preferences', data),
}
