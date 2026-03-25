package com.learn.today.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.learn.today.archive.entity.dto.ArchivePlanRow;
import com.learn.today.archive.entity.vo.ArchiveVO;
import com.learn.today.archive.entity.vo.StatsVO;
import com.learn.today.archive.mapper.ArchiveMapper;
import com.learn.today.archive.service.ArchiveService;
import com.learn.today.plan.entity.Task;
import com.learn.today.plan.mapper.PlanTopicMapper;
import com.learn.today.plan.mapper.TaskMapper;
import com.learn.today.topic.entity.Topic;
import com.learn.today.topic.mapper.TopicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArchiveServiceImpl implements ArchiveService {

    private final ArchiveMapper archiveMapper;
    private final TaskMapper taskMapper;
    private final PlanTopicMapper planTopicMapper;
    private final TopicMapper topicMapper;

    // -------------------------------------------------------
    // 归档列表
    // -------------------------------------------------------

    @Override
    public List<ArchiveVO> listArchive(String userId, int page, int size) {
        int offset = (page - 1) * size;

        // 1. 查计划行（含任务统计聚合）
        List<ArchivePlanRow> planRows = archiveMapper.selectArchivedPlanRows(userId, offset, size);
        if (planRows.isEmpty()) {
            return List.of();
        }

        List<String> planIds = planRows.stream()
                .map(ArchivePlanRow::getPlanId)
                .collect(Collectors.toList());

        // 2. 批量查每个计划的所有任务
        LambdaQueryWrapper<Task> taskQuery = new LambdaQueryWrapper<Task>()
                .in(Task::getPlanId, planIds)
                .orderByAsc(Task::getSortOrder);
        List<Task> allTasks = taskMapper.selectList(taskQuery);
        Map<String, List<Task>> tasksByPlan = allTasks.stream()
                .collect(Collectors.groupingBy(Task::getPlanId));

        // 3. 批量查每个计划的 topicId 列表，再批量查 topic 名称
        //    topicId -> topicName 映射（懒加载一次，覆盖本页所有计划用到的 topic）
        List<String> allTopicIds = allTasks.stream()
                .map(Task::getTopicId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> topicNameMap;
        if (allTopicIds.isEmpty()) {
            topicNameMap = Map.of();
        } else {
            LambdaQueryWrapper<Topic> topicQuery = new LambdaQueryWrapper<Topic>()
                    .in(Topic::getId, allTopicIds)
                    .select(Topic::getId, Topic::getName);
            topicNameMap = topicMapper.selectList(topicQuery).stream()
                    .collect(Collectors.toMap(Topic::getId, Topic::getName));
        }

        // 4. 组装 ArchiveVO
        List<ArchiveVO> result = new ArrayList<>();
        for (ArchivePlanRow row : planRows) {
            ArchiveVO vo = new ArchiveVO();
            vo.setPlanId(row.getPlanId());
            vo.setPlanDate(LocalDate.parse(row.getPlanDate()));
            vo.setAvailableMinutes(row.getAvailableMinutes());
            vo.setTotalTasks(row.getTotalTasks());
            vo.setCompletedTasks(row.getCompletedTasks());

            List<Task> tasks = tasksByPlan.getOrDefault(row.getPlanId(), List.of());

            // 去重收集 topicNames
            List<String> topicNames = tasks.stream()
                    .map(Task::getTopicId)
                    .filter(id -> id != null && topicNameMap.containsKey(id))
                    .map(topicNameMap::get)
                    .distinct()
                    .collect(Collectors.toList());
            vo.setTopicNames(topicNames);

            // 转换任务列表
            List<ArchiveVO.ArchivedTaskVO> taskVOs = tasks.stream().map(t -> {
                ArchiveVO.ArchivedTaskVO taskVO = new ArchiveVO.ArchivedTaskVO();
                taskVO.setTaskId(t.getId());
                taskVO.setTopicName(topicNameMap.getOrDefault(t.getTopicId(), ""));
                taskVO.setTitle(t.getTitle());
                taskVO.setDifficulty(t.getDifficulty());
                taskVO.setEstimatedMinutes(t.getEstimatedMinutes());
                taskVO.setStatus(t.getStatus());
                return taskVO;
            }).collect(Collectors.toList());
            vo.setTasks(taskVOs);

            result.add(vo);
        }
        return result;
    }

    // -------------------------------------------------------
    // 学习统计
    // -------------------------------------------------------

    @Override
    public StatsVO getStats(String userId) {
        // 1. 已完成计划日期列表（已按 DESC 排序）
        List<String> dates = archiveMapper.selectCompletedPlanDates(userId);
        int totalDays = dates.size();

        // 2. 已完成任务总数
        int totalTasks = archiveMapper.countCompletedTasks(userId);

        // 3. 计算连续打卡天数（需要 ASC 顺序，dates 是 DESC 所以倒序遍历）
        int currentStreak = 0;
        int longestStreak = 0;

        if (!dates.isEmpty()) {
            // 转为 LocalDate 并去重（一天可能有多个计划，理论上不会但防御一下）
            List<LocalDate> sortedDates = dates.stream()
                    .map(LocalDate::parse)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            // 计算最长连续天数
            int streak = 1;
            longestStreak = 1;
            for (int i = 1; i < sortedDates.size(); i++) {
                if (sortedDates.get(i).minusDays(1).equals(sortedDates.get(i - 1))) {
                    streak++;
                    longestStreak = Math.max(longestStreak, streak);
                } else {
                    streak = 1;
                }
            }

            // 计算当前连续天数：从最近一天往前数连续的
            LocalDate today = LocalDate.now();
            LocalDate lastDate = sortedDates.get(sortedDates.size() - 1);

            // 最近打卡日期是今天或昨天才算连续
            if (lastDate.equals(today) || lastDate.equals(today.minusDays(1))) {
                currentStreak = 1;
                for (int i = sortedDates.size() - 2; i >= 0; i--) {
                    if (sortedDates.get(i + 1).minusDays(1).equals(sortedDates.get(i))) {
                        currentStreak++;
                    } else {
                        break;
                    }
                }
            }
        }

        return StatsVO.builder()
                .totalDays(totalDays)
                .totalTasks(totalTasks)
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .build();
    }
}
