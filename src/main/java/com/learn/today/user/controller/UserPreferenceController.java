package com.learn.today.user.controller;

import com.learn.today.common.context.UserContext;
import com.learn.today.common.result.Result;
import com.learn.today.user.entity.dto.UserPreferenceDTO;
import com.learn.today.user.entity.vo.UserPreferenceVO;
import com.learn.today.user.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    /** GET /user/preferences - 获取当前用户偏好 */
    @GetMapping
    public Result<UserPreferenceVO> getPreference() {
        return Result.success(userPreferenceService.getPreference(UserContext.getUserId()));
    }

    /** PUT /user/preferences - 保存用户偏好 */
    @PutMapping
    public Result<Void> savePreference(@RequestBody UserPreferenceDTO dto) {
        userPreferenceService.savePreference(UserContext.getUserId(), dto);
        return Result.success();
    }
}
