package com.learn.today.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.today.common.context.UserContext;
import com.learn.today.common.result.Result;
import com.learn.today.common.result.ResultCode;
import com.learn.today.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 鉴权拦截器
 *
 * 流程：
 *   1. 从 Authorization 头提取 Bearer token
 *   2. 用 JwtUtil 验证签名和过期时间
 *   3. 解析 userId / email 写入 UserContext（ThreadLocal）
 *   4. 请求结束后在 afterCompletion 清除 ThreadLocal
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String token = extractToken(request);

        if (!StringUtils.hasText(token)) {
            writeUnauthorized(response, "请先登录");
            return false;
        }

        if (!jwtUtil.validateToken(token)) {
            writeUnauthorized(response, "登录已过期，请重新登录");
            return false;
        }

        String userId = jwtUtil.getUserIdFromToken(token);
        String email  = jwtUtil.getEmailFromToken(token);
        UserContext.set(new UserContext.LoginUser(userId, email));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // 必须清理，防止线程池复用时 ThreadLocal 数据串用
        UserContext.clear();
    }

    // -------------------------------------------------------
    // 私有方法
    // -------------------------------------------------------

    /** 从 Authorization: Bearer <token> 中提取 token */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /** 直接写 401 JSON 响应，不走 ExceptionHandler（拦截器在 DispatcherServlet 之前） */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Result<Void> result = Result.fail(ResultCode.UNAUTHORIZED);
        // 覆盖默认消息，给出更具体的提示
        result.setMessage(message);

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
