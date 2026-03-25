package com.learn.today.topic.controller;

import com.learn.today.common.result.Result;
import com.learn.today.topic.entity.vo.TopicVO;
import com.learn.today.topic.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    /** GET /topics - 获取所有学习方向（树形结构） */
    @GetMapping
    public Result<List<TopicVO>> listAll() {
        return Result.success(topicService.listAll());
    }
}
