package com.learn.today.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.today.user.entity.UserPreference;
import com.learn.today.user.entity.dto.UserPreferenceDTO;
import com.learn.today.user.entity.vo.UserPreferenceVO;
import com.learn.today.user.mapper.UserPreferenceMapper;
import com.learn.today.user.mapper.UserPreferenceTopicMapper;
import com.learn.today.user.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPreferenceServiceImpl
        extends ServiceImpl<UserPreferenceMapper, UserPreference>
        implements UserPreferenceService {

    private final UserPreferenceTopicMapper preferenceTopicMapper;

    // -------------------------------------------------------
    // 查询
    // -------------------------------------------------------

    @Override
    public UserPreferenceVO getPreference(String userId) {
        UserPreference pref = lambdaQuery()
                .eq(UserPreference::getUserId, userId)
                .one();

        boolean autoRecommend = pref == null || Boolean.TRUE.equals(pref.getEnableAutoRecommend());
        List<String> topicIds = preferenceTopicMapper.selectTopicIdsByUserId(userId);

        return UserPreferenceVO.builder()
                .enableAutoRecommend(autoRecommend)
                .topicIds(topicIds)
                .build();
    }

    // -------------------------------------------------------
    // 保存（upsert）
    // -------------------------------------------------------

    @Override
    @Transactional
    public void savePreference(String userId, UserPreferenceDTO dto) {
        // 1. upsert user_preferences
        UserPreference existing = lambdaQuery()
                .eq(UserPreference::getUserId, userId)
                .one();

        if (existing == null) {
            UserPreference pref = new UserPreference();
            pref.setUserId(userId);
            pref.setEnableAutoRecommend(
                    dto.getEnableAutoRecommend() == null || dto.getEnableAutoRecommend());
            save(pref);
        } else {
            if (dto.getEnableAutoRecommend() != null) {
                existing.setEnableAutoRecommend(dto.getEnableAutoRecommend());
                updateById(existing);
            }
        }

        // 2. 全量替换偏好方向：先删后插
        preferenceTopicMapper.deleteByUserId(userId);
        List<String> topicIds = dto.getTopicIds();
        if (topicIds != null) {
            for (String topicId : topicIds) {
                preferenceTopicMapper.insert(userId, topicId);
            }
        }
    }
}
