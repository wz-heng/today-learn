package com.learn.today.plan.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class GeneratePlanDTO {

    @NotNull(message = "可用时间不能为空")
    @Min(value = 5, message = "学习时间至少5分钟")
    private Integer availableMinutes;

    /** 可选，不填则系统自动推荐 */
    private List<String> topicIds;

    /** 可选，指定 AI 模型，不填使用系统默认 */
    private String model;
}
