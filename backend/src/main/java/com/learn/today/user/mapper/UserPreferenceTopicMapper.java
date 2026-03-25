package com.learn.today.user.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserPreferenceTopicMapper {

    @Insert("INSERT OR IGNORE INTO user_preference_topics(user_id, topic_id) " +
            "VALUES(#{userId}, #{topicId})")
    void insert(@Param("userId") String userId, @Param("topicId") String topicId);

    @Delete("DELETE FROM user_preference_topics WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") String userId);

    @Select("SELECT topic_id FROM user_preference_topics WHERE user_id = #{userId}")
    List<String> selectTopicIdsByUserId(@Param("userId") String userId);
}
