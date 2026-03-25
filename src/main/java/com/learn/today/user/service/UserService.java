package com.learn.today.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.learn.today.user.entity.User;
import com.learn.today.user.entity.dto.UserLoginDTO;
import com.learn.today.user.entity.dto.UserRegisterDTO;
import com.learn.today.user.entity.vo.UserInfoVO;
import com.learn.today.user.entity.vo.UserLoginVO;

public interface UserService extends IService<User> {

    /** 用户注册 */
    void register(UserRegisterDTO dto);

    /** 用户登录，返回 token */
    UserLoginVO login(UserLoginDTO dto);

    /** 获取当前用户信息（不含 token） */
    UserInfoVO getMyInfo(String userId);
}
