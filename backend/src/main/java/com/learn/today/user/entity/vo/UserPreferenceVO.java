package com.learn.today.user.entity.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 用户偏好设置响应
 */
@Data
@Builder
public class UserPreferenceVO {

    private Boolean enableAutoRecommend;

    /** 已选的偏好学习方向 ID 列表 */
    private List<String> topicIds;
}
