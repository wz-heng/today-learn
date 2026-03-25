package com.learn.today.user.controller;

import com.learn.today.common.context.UserContext;
import com.learn.today.common.result.Result;
import com.learn.today.user.entity.dto.UserLoginDTO;
import com.learn.today.user.entity.dto.UserRegisterDTO;
import com.learn.today.user.entity.vo.UserInfoVO;
import com.learn.today.user.entity.vo.UserLoginVO;
import com.learn.today.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** POST /auth/register - 用户注册 */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    /** POST /auth/login - 用户登录 */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    /** GET /auth/me - 获取当前登录用户信息 */
    @GetMapping("/me")
    public Result<UserInfoVO> getMyInfo() {
        return Result.success(userService.getMyInfo(UserContext.getUserId()));
    }
}
