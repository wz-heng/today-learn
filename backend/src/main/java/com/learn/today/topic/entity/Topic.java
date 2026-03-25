package com.learn.today.topic.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("topics")
public class Topic {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String name;

    private String slug;

    private String parentId;

    private String description;

    private String icon;

    private Integer sortOrder;

    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
