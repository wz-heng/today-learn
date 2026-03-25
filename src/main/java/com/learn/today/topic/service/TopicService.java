package com.learn.today.topic.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.learn.today.topic.entity.Topic;
import com.learn.today.topic.entity.vo.TopicVO;
import java.util.List;

public interface TopicService extends IService<Topic> {

    /** 获取所有启用的学习方向（树形结构） */
    List<TopicVO> listAll();
}
