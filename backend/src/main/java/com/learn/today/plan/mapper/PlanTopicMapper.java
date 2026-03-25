package com.learn.today.plan.mapper;

import com.learn.today.plan.entity.PlanTopic;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PlanTopicMapper {

    @Insert("INSERT INTO plan_topics(plan_id, topic_id, is_auto_recommended) " +
            "VALUES(#{planId}, #{topicId}, #{isAutoRecommended})")
    void insert(PlanTopic planTopic);

    @Select("SELECT topic_id FROM plan_topics WHERE plan_id = #{planId}")
    List<String> selectTopicIdsByPlanId(@Param("planId") String planId);
}
