package com.learn.today.archive.entity.vo;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ArchiveVO {
    private String planId;
    private LocalDate planDate;
    private Integer availableMinutes;
    private List<String> topicNames;
    private Integer totalTasks;
    private Integer completedTasks;
    private List<ArchivedTaskVO> tasks;

    @Data
    public static class ArchivedTaskVO {
        private String taskId;
        private String topicName;
        private String title;
        private String difficulty;
        private Integer estimatedMinutes;
        private String status;
    }
}
