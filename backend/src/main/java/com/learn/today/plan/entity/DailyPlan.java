package com.learn.today.plan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_plans")
public class DailyPlan {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private LocalDate planDate;

    private Integer availableMinutes;

    private Boolean isAutoTopic;

    /** active / completed / skipped */
    private String status;

    private LocalDateTime completedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
