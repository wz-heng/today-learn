package com.learn.today.user.entity.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 当前登录用户信息（不含 token）
 */
@Data
@Builder
public class UserInfoVO {
    private String userId;
    private String email;
    private String displayName;
    private String avatarUrl;
}
