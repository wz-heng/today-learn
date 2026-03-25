package com.learn.today.plan.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * plan_topics 关联表
 * 记录每个计划选择了哪些学习方向
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanTopic {
    private String planId;
    private String topicId;
    private Boolean isAutoRecommended;
}
