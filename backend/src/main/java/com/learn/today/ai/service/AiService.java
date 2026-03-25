package com.learn.today.ai.service;

import com.learn.today.ai.dto.AiTaskResult;
import com.learn.today.topic.entity.Topic;

import java.util.List;

public interface AiService {

    /**
     * 调用 AI 生成今日学习任务列表
     *
     * @param availableMinutes 用户今日可用时间（分钟）
     * @param topics           本次学习方向列表
     * @param learnedTitles    用户已学内容标题（去重用）
     * @param model            指定模型，null 则使用系统默认
     */
    List<AiTaskResult> generateTasks(int availableMinutes,
                                     List<Topic> topics,
                                     List<String> learnedTitles,
                                     String model);
}
