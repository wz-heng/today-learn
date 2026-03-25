package com.learn.today.archive.entity.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 学习统计数据
 */
@Data
@Builder
public class StatsVO {
    /** 累计学习天数 */
    private Integer totalDays;
    /** 累计完成任务数 */
    private Integer totalTasks;
    /** 当前连续打卡天数 */
    private Integer currentStreak;
    /** 历史最长连续打卡天数 */
    private Integer longestStreak;
}
