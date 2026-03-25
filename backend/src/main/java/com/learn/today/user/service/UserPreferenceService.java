package com.learn.today.user.service;

import com.learn.today.user.entity.dto.UserPreferenceDTO;
import com.learn.today.user.entity.vo.UserPreferenceVO;

public interface UserPreferenceService {

    /** 获取当前用户偏好设置 */
    UserPreferenceVO getPreference(String userId);

    /** 保存（新建或更新）用户偏好设置 */
    void savePreference(String userId, UserPreferenceDTO dto);
}
