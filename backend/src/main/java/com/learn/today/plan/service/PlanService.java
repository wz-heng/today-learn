package com.learn.today.plan.service;

import com.learn.today.plan.entity.dto.GeneratePlanDTO;
import com.learn.today.plan.entity.vo.TodayPlanVO;
import java.util.List;

public interface PlanService {

    /** 生成今日学习计划（核心接口） */
    TodayPlanVO generatePlan(String userId, GeneratePlanDTO dto);

    /** 获取今日计划 */
    TodayPlanVO getTodayPlan(String userId);

    /** 获取历史计划列表 */
    List<TodayPlanVO> listPlans(String userId, int page, int size);

    /** 标记任务状态（completed / skipped） */
    void updateTaskStatus(String userId, String taskId, String status);
}
