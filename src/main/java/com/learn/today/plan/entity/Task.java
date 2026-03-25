package com.learn.today.plan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tasks")
public class Task {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String planId;

    private String userId;

    private String knowledgePointId;

    private String topicId;

    private String title;

    private String content;

    private Integer estimatedMinutes;

    /** easy / medium / hard */
    private String difficulty;

    /** pending / completed / skipped */
    private String status;

    private Integer sortOrder;

    private LocalDateTime completedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
