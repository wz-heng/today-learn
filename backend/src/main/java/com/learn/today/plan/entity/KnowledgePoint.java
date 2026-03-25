package com.learn.today.plan.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_points")
public class KnowledgePoint {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String topicId;

    private String title;

    private String content;

    /** easy / medium / hard */
    private String difficulty;

    private Integer estimatedMinutes;

    /** MD5(lower(trim(title)) + "::" + topicId) */
    private String contentHash;

    /** preset / ai_generated */
    private String source;

    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
