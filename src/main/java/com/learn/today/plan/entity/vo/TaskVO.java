package com.learn.today.plan.entity.vo;

import lombok.Data;

@Data
public class TaskVO {
    private String id;
    private String topicId;
    private String topicName;
    private String title;
    private String content;
    private Integer estimatedMinutes;
    private String difficulty;
    private String status;
    private Integer sortOrder;
}
