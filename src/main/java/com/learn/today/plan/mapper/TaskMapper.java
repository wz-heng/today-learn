package com.learn.today.plan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learn.today.plan.entity.Task;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface TaskMapper extends BaseMapper<Task> {

    /** 查询用户已完成的知识点 ID（用于去重） */
    List<String> selectCompletedKnowledgePointIds(@Param("userId") String userId);
}
