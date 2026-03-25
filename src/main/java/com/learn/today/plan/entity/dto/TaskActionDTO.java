package com.learn.today.plan.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TaskActionDTO {

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "completed|skipped", message = "状态只能是 completed 或 skipped")
    private String status;
}
