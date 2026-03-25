package com.learn.today.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learn.today.user.entity.User;

public interface UserMapper extends BaseMapper<User> {
    // 自定义 SQL 写在 UserMapper.xml
}
