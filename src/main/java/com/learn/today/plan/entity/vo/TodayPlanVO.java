package com.learn.today.plan.entity.vo;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class TodayPlanVO {
    private String planId;
    private LocalDate planDate;
    private Integer availableMinutes;
    private Boolean isAutoTopic;
    private String status;
    private List<String> topicNames;
    private List<TaskVO> tasks;
}
