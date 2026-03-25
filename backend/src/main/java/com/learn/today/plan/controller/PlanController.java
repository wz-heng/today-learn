package com.learn.today.plan.controller;

import com.learn.today.common.context.UserContext;
import com.learn.today.common.result.Result;
import com.learn.today.plan.entity.dto.GeneratePlanDTO;
import com.learn.today.plan.entity.dto.TaskActionDTO;
import com.learn.today.plan.entity.vo.TodayPlanVO;
import com.learn.today.plan.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    /** POST /plans/generate - 生成今日学习计划 */
    @PostMapping("/plans/generate")
    public Result<TodayPlanVO> generate(@Valid @RequestBody GeneratePlanDTO dto) {
        return Result.success(planService.generatePlan(UserContext.getUserId(), dto));
    }

    /** GET /plans/today - 获取今日计划 */
    @GetMapping("/plans/today")
    public Result<TodayPlanVO> getToday() {
        return Result.success(planService.getTodayPlan(UserContext.getUserId()));
    }

    /** GET /plans - 历史计划列表 */
    @GetMapping("/plans")
    public Result<List<TodayPlanVO>> listPlans(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(planService.listPlans(UserContext.getUserId(), page, size));
    }

    /** PATCH /tasks/{taskId}/status - 更新任务状态（完成/跳过） */
    @PatchMapping("/tasks/{taskId}/status")
    public Result<Void> updateTaskStatus(
            @PathVariable String taskId,
            @Valid @RequestBody TaskActionDTO dto) {
        planService.updateTaskStatus(UserContext.getUserId(), taskId, dto.getStatus());
        return Result.success();
    }
}
