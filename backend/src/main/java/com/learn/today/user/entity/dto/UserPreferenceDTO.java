package com.learn.today.user.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * 保存用户偏好设置的请求体
 */
@Data
public class UserPreferenceDTO {

    /** 是否启用自动推荐方向 */
    private Boolean enableAutoRecommend;

    /** 偏好的学习方向 ID 列表（可为空） */
    private List<String> topicIds;
}
