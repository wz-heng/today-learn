package com.learn.today.common.context;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 当前登录用户上下文（ThreadLocal）
 *
 * 使用方式：
 *   - 拦截器解析 JWT 后调用 UserContext.set(loginUser)
 *   - Controller / Service 中调用 UserContext.getUserId() 获取用户 ID
 *   - 请求结束时拦截器调用 UserContext.clear() 防止内存泄漏
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 直接获取 userId，减少样板代码 */
    public static String getUserId() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new IllegalStateException("当前请求未登录，无法获取用户 ID");
        }
        return user.getUserId();
    }

    public static void clear() {
        HOLDER.remove();
    }

    // -------------------------------------------------------
    // 登录用户信息
    // -------------------------------------------------------

    @Data
    @AllArgsConstructor
    public static class LoginUser {
        private String userId;
        private String email;
    }
}
