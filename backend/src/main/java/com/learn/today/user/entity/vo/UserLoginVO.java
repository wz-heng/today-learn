package com.learn.today.user.entity.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLoginVO {
    private String token;
    private String userId;
    private String displayName;
    private String email;
}
