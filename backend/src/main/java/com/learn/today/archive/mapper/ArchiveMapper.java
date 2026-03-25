package com.learn.today.archive.mapper;

import com.learn.today.archive.entity.dto.ArchivePlanRow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ArchiveMapper {

    /** 分页查询已完成的计划行（含任务统计数量） */
    List<ArchivePlanRow> selectArchivedPlanRows(
            @Param("userId") String userId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /** 查询所有已完成计划的日期列表（用于计算连续打卡天数） */
    @Select("SELECT plan_date FROM daily_plans " +
            "WHERE user_id = #{userId} AND status = 'completed' " +
            "ORDER BY plan_date DESC")
    List<String> selectCompletedPlanDates(@Param("userId") String userId);

    /** 查询用户所有已完成任务数 */
    @Select("SELECT COUNT(*) FROM tasks " +
            "WHERE user_id = #{userId} AND status = 'completed'")
    int countCompletedTasks(@Param("userId") String userId);
}
