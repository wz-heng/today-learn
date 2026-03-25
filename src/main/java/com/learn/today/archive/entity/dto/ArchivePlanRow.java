package com.learn.today.archive.entity.dto;

import lombok.Data;

/**
 * ArchiveMapper SQL 查询的中间结果，一行对应一个已完成的计划
 */
@Data
public class ArchivePlanRow {
    private String planId;
    private String planDate;        // SQLite 存 TEXT，格式 YYYY-MM-DD
    private Integer availableMinutes;
    private Integer totalTasks;
    private Integer completedTasks;
}
