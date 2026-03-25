package com.learn.today.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.today.common.exception.BusinessException;
import com.learn.today.common.result.ResultCode;
import com.learn.today.common.util.JwtUtil;
import com.learn.today.user.entity.User;
import com.learn.today.user.entity.dto.UserLoginDTO;
import com.learn.today.user.entity.dto.UserRegisterDTO;
import com.learn.today.user.entity.vo.UserInfoVO;
import com.learn.today.user.entity.vo.UserLoginVO;
import com.learn.today.user.mapper.UserMapper;
import com.learn.today.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // -------------------------------------------------------
    // 注册
    // -------------------------------------------------------

    @Override
    public void register(UserRegisterDTO dto) {
        // 1. 邮箱唯一性校验
        boolean emailExists = lambdaQuery()
                .eq(User::getEmail, dto.getEmail())
                .exists();
        if (emailExists) {
            throw new BusinessException("该邮箱已被注册");
        }

        // 2. 构建用户，密码 BCrypt 加密
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setDisplayName(
                dto.getDisplayName() != null ? dto.getDisplayName() : dto.getEmail()
        );
        user.setTimezone("Asia/Shanghai");

        // 3. 入库（createdAt / updatedAt 由 MetaObjectHandler 自动填充）
        save(user);
        log.info("用户注册成功: {}", dto.getEmail());
    }

    // -------------------------------------------------------
    // 登录
    // -------------------------------------------------------

    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        // 1. 根据邮箱查询用户
        User user = lambdaQuery()
                .eq(User::getEmail, dto.getEmail())
                .one();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "邮箱或密码错误");
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "邮箱或密码错误");
        }

        // 3. 生成 JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        log.info("用户登录成功: {}", dto.getEmail());

        return UserLoginVO.builder()
                .token(token)
                .userId(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .build();
    }

    // -------------------------------------------------------
    // 当前用户信息
    // -------------------------------------------------------

    @Override
    public UserInfoVO getMyInfo(String userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        return UserInfoVO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
