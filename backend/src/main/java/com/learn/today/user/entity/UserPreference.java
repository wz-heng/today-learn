package com.learn.today.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_preferences")
public class UserPreference {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    /** 是否启用自动推荐方向（0=否 1=是） */
    private Boolean enableAutoRecommend;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
