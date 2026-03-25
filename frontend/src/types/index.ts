// ---- 通用响应 ----
export interface Result<T = null> {
  code: number
  message: string
  data: T
}

// ---- 用户 ----
export interface UserLoginVO {
  token: string
  userId: string
  displayName: string
  email: string
}

export interface UserInfoVO {
  userId: string
  email: string
  displayName: string
  avatarUrl: string | null
}

export interface UserPreferenceVO {
  enableAutoRecommend: boolean
  topicIds: string[]
}

// ---- 学习方向 ----
export interface TopicVO {
  id: string
  name: string
  slug: string
  parentId: string | null
  description: string
  icon: string
  sortOrder: number
  children: TopicVO[]
}

// ---- 学习计划 ----
export interface TaskVO {
  id: string
  topicId: string
  topicName: string
  title: string
  content: string
  estimatedMinutes: number
  difficulty: 'easy' | 'medium' | 'hard'
  status: 'pending' | 'completed' | 'skipped'
  sortOrder: number
}

export interface TodayPlanVO {
  planId: string
  planDate: string
  availableMinutes: number
  isAutoTopic: boolean
  status: 'active' | 'completed' | 'skipped'
  topicNames: string[]
  tasks: TaskVO[]
}

// ---- 归档 ----
export interface ArchivedTaskVO {
  taskId: string
  topicName: string
  title: string
  difficulty: string
  estimatedMinutes: number
  status: string
}

export interface ArchiveVO {
  planId: string
  planDate: string
  availableMinutes: number
  topicNames: string[]
  totalTasks: number
  completedTasks: number
  tasks: ArchivedTaskVO[]
}

export interface StatsVO {
  totalDays: number
  totalTasks: number
  currentStreak: number
  longestStreak: number
}
