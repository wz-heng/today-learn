package com.learn.today.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * AI 生成的单条学习任务
 * 对应 Claude 返回 JSON 数组中的每个元素
 */
@Data
public class AiTaskResult {

    /** 任务标题，20字以内 */
    private String title;

    /** 具体学习内容说明，2-3句话 */
    private String content;

    /** 难度：easy / medium / hard */
    private String difficulty;

    /** 预计学习时长（分钟） */
    @JsonProperty("estimatedMinutes")
    private Integer estimatedMinutes;

    /** 对应学习方向的 slug，如 java / mysql / redis */
    @JsonProperty("topicSlug")
    private String topicSlug;
}
