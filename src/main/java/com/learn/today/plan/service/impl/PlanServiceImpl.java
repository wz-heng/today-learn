package com.learn.today.plan.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.learn.today.ai.dto.AiTaskResult;
import com.learn.today.ai.service.AiService;
import com.learn.today.common.exception.BusinessException;
import com.learn.today.common.result.ResultCode;
import com.learn.today.plan.entity.DailyPlan;
import com.learn.today.plan.entity.KnowledgePoint;
import com.learn.today.plan.entity.PlanTopic;
import com.learn.today.plan.entity.Task;
import com.learn.today.plan.entity.dto.GeneratePlanDTO;
import com.learn.today.plan.entity.vo.TaskVO;
import com.learn.today.plan.entity.vo.TodayPlanVO;
import com.learn.today.plan.mapper.DailyPlanMapper;
import com.learn.today.plan.mapper.KnowledgePointMapper;
import com.learn.today.plan.mapper.PlanTopicMapper;
import com.learn.today.plan.mapper.TaskMapper;
import com.learn.today.plan.service.PlanService;
import com.learn.today.topic.entity.Topic;
import com.learn.today.topic.mapper.TopicMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final DailyPlanMapper dailyPlanMapper;
    private final TaskMapper taskMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final PlanTopicMapper planTopicMapper;
    private final TopicMapper topicMapper;
    private final AiService aiService;

    // -------------------------------------------------------
    // 生成今日计划（核心接口）
    // -------------------------------------------------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TodayPlanVO generatePlan(String userId, GeneratePlanDTO dto) {
        // 1. 今日若已有计划，先删除（允许重新生成）
        LocalDate today = LocalDate.now();
        DailyPlan existing = dailyPlanMapper.selectOne(
                Wrappers.<DailyPlan>lambdaQuery()
                        .eq(DailyPlan::getUserId, userId)
                        .eq(DailyPlan::getPlanDate, today)
        );
        if (existing != null) {
            // 级联删除 tasks、plan_topics（schema 已设置 ON DELETE CASCADE）
            dailyPlanMapper.deleteById(existing.getId());
        }

        // 2. 确定学习方向
        boolean isAutoTopic = CollectionUtils.isEmpty(dto.getTopicIds());
        List<Topic> topics = isAutoTopic
                ? autoRecommendTopics()
                : loadTopicsByIds(dto.getTopicIds());

        // 3. 查询用户已学知识点标题（传给 AI 避免重复）
        List<String> learnedTitles = getLearnedTitles(userId);

        // 4. 调用 AI 生成任务
        List<AiTaskResult> aiTasks = aiService.generateTasks(
                dto.getAvailableMinutes(), topics, learnedTitles, dto.getModel());

        // 5. 创建 daily_plans 记录
        DailyPlan plan = new DailyPlan();
        plan.setUserId(userId);
        plan.setPlanDate(today);
        plan.setAvailableMinutes(dto.getAvailableMinutes());
        plan.setIsAutoTopic(isAutoTopic);
        plan.setStatus("active");
        dailyPlanMapper.insert(plan);

        // 6. 插入 plan_topics 关联
        for (Topic topic : topics) {
            planTopicMapper.insert(new PlanTopic(plan.getId(), topic.getId(), isAutoTopic));
        }

        // 7. 知识点去重入库 + 批量创建任务
        Map<String, Topic> topicBySlug = topics.stream()
                .collect(Collectors.toMap(Topic::getSlug, t -> t));
        Topic fallbackTopic = topics.get(0);

        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < aiTasks.size(); i++) {
            AiTaskResult ai = aiTasks.get(i);
            Topic topic = topicBySlug.getOrDefault(ai.getTopicSlug(), fallbackTopic);

            KnowledgePoint kp = saveOrGetKnowledgePoint(ai, topic);

            Task task = new Task();
            task.setPlanId(plan.getId());
            task.setUserId(userId);
            task.setKnowledgePointId(kp.getId());
            task.setTopicId(topic.getId());
            task.setTitle(ai.getTitle());
            task.setContent(ai.getContent());
            task.setEstimatedMinutes(ai.getEstimatedMinutes());
            task.setDifficulty(ai.getDifficulty());
            task.setStatus("pending");
            task.setSortOrder(i);
            taskMapper.insert(task);
            tasks.add(task);
        }

        log.info("用户 {} 今日计划生成完成，共 {} 条任务", userId, tasks.size());
        return buildTodayPlanVO(plan, topics, tasks);
    }

    // -------------------------------------------------------
    // 获取今日计划
    // -------------------------------------------------------

    @Override
    public TodayPlanVO getTodayPlan(String userId) {
        DailyPlan plan = dailyPlanMapper.selectOne(
                Wrappers.<DailyPlan>lambdaQuery()
                        .eq(DailyPlan::getUserId, userId)
                        .eq(DailyPlan::getPlanDate, LocalDate.now())
        );
        if (plan == null) {
            return null;
        }
        return buildPlanVOWithDB(plan);
    }

    // -------------------------------------------------------
    // 历史计划列表
    // -------------------------------------------------------

    @Override
    public List<TodayPlanVO> listPlans(String userId, int page, int size) {
        Page<DailyPlan> pageResult = dailyPlanMapper.selectPage(
                new Page<>(page, size),
                Wrappers.<DailyPlan>lambdaQuery()
                        .eq(DailyPlan::getUserId, userId)
                        .orderByDesc(DailyPlan::getPlanDate)
        );
        return pageResult.getRecords().stream()
                .map(this::buildPlanVOWithDB)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // 更新任务状态（完成 / 跳过）
    // -------------------------------------------------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskStatus(String userId, String taskId, String status) {
        // 1. 查任务，校验归属当前用户
        Task task = taskMapper.selectById(taskId);
        if (task == null || !userId.equals(task.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "任务不存在");
        }
        if (!"pending".equals(task.getStatus())) {
            throw new BusinessException("任务已完成或已跳过，不能重复操作");
        }

        // 2. 更新状态
        task.setStatus(status);
        if ("completed".equals(status)) {
            task.setCompletedAt(LocalDateTime.now());
        }
        taskMapper.updateById(task);

        // 3. 检查该计划所有任务是否全部结束
        long pendingCount = taskMapper.selectCount(
                Wrappers.<Task>lambdaQuery()
                        .eq(Task::getPlanId, task.getPlanId())
                        .eq(Task::getStatus, "pending")
        );
        if (pendingCount == 0) {
            DailyPlan plan = dailyPlanMapper.selectById(task.getPlanId());
            plan.setStatus("completed");
            plan.setCompletedAt(LocalDateTime.now());
            dailyPlanMapper.updateById(plan);
            log.info("计划 {} 已全部完成，自动归档", plan.getId());
        }
    }

    // -------------------------------------------------------
    // 私有方法
    // -------------------------------------------------------

    /** 系统自动推荐：取排序最靠前的 2 个活跃方向 */
    private List<Topic> autoRecommendTopics() {
        List<Topic> topics = topicMapper.selectList(
                Wrappers.<Topic>lambdaQuery()
                        .eq(Topic::getIsActive, true)
                        .orderByAsc(Topic::getSortOrder)
                        .last("LIMIT 2")
        );
        if (topics.isEmpty()) {
            throw new BusinessException("暂无可用学习方向，请联系管理员");
        }
        return topics;
    }

    /** 根据用户传入的 topicIds 加载方向 */
    private List<Topic> loadTopicsByIds(List<String> topicIds) {
        List<Topic> topics = topicMapper.selectBatchIds(topicIds);
        if (topics.isEmpty()) {
            throw new BusinessException("所选学习方向不存在");
        }
        return topics;
    }

    /** 获取用户已学内容标题，用于传给 AI 去重 */
    private List<String> getLearnedTitles(String userId) {
        List<String> learnedKpIds = taskMapper.selectCompletedKnowledgePointIds(userId);
        if (learnedKpIds.isEmpty()) {
            return Collections.emptyList();
        }
        return knowledgePointMapper.selectBatchIds(learnedKpIds)
                .stream()
                .map(KnowledgePoint::getTitle)
                .collect(Collectors.toList());
    }

    /**
     * 知识点去重入库：
     * 先按 content_hash 查，已存在则复用，不存在则插入
     */
    private KnowledgePoint saveOrGetKnowledgePoint(AiTaskResult ai, Topic topic) {
        String hash = DigestUtils.md5DigestAsHex(
                (ai.getTitle().toLowerCase().trim() + "::" + topic.getId())
                        .getBytes(StandardCharsets.UTF_8)
        );

        KnowledgePoint existing = knowledgePointMapper.selectOne(
                Wrappers.<KnowledgePoint>lambdaQuery()
                        .eq(KnowledgePoint::getTopicId, topic.getId())
                        .eq(KnowledgePoint::getContentHash, hash)
        );
        if (existing != null) {
            return existing;
        }

        KnowledgePoint kp = new KnowledgePoint();
        kp.setTopicId(topic.getId());
        kp.setTitle(ai.getTitle());
        kp.setContent(ai.getContent());
        kp.setDifficulty(ai.getDifficulty());
        kp.setEstimatedMinutes(ai.getEstimatedMinutes());
        kp.setContentHash(hash);
        kp.setSource("ai_generated");
        kp.setIsActive(true);
        knowledgePointMapper.insert(kp);
        return kp;
    }

    /** 从数据库重新加载 topics/tasks，组装 VO（用于查询接口） */
    private TodayPlanVO buildPlanVOWithDB(DailyPlan plan) {
        List<Task> tasks = taskMapper.selectList(
                Wrappers.<Task>lambdaQuery()
                        .eq(Task::getPlanId, plan.getId())
                        .orderByAsc(Task::getSortOrder)
        );
        List<String> topicIds = planTopicMapper.selectTopicIdsByPlanId(plan.getId());
        List<Topic> topics = topicIds.isEmpty()
                ? Collections.emptyList()
                : topicMapper.selectBatchIds(topicIds);

        return buildTodayPlanVO(plan, topics, tasks);
    }

    /** 将 plan + topics + tasks 组装成 VO */
    private TodayPlanVO buildTodayPlanVO(DailyPlan plan, List<Topic> topics, List<Task> tasks) {
        Map<String, String> topicNameMap = topics.stream()
                .collect(Collectors.toMap(Topic::getId, Topic::getName));

        List<TaskVO> taskVOs = tasks.stream().map(task -> {
            TaskVO vo = new TaskVO();
            vo.setId(task.getId());
            vo.setTopicId(task.getTopicId());
            vo.setTopicName(topicNameMap.getOrDefault(task.getTopicId(), ""));
            vo.setTitle(task.getTitle());
            vo.setContent(task.getContent());
            vo.setEstimatedMinutes(task.getEstimatedMinutes());
            vo.setDifficulty(task.getDifficulty());
            vo.setStatus(task.getStatus());
            vo.setSortOrder(task.getSortOrder());
            return vo;
        }).collect(Collectors.toList());

        TodayPlanVO vo = new TodayPlanVO();
        vo.setPlanId(plan.getId());
        vo.setPlanDate(plan.getPlanDate());
        vo.setAvailableMinutes(plan.getAvailableMinutes());
        vo.setIsAutoTopic(plan.getIsAutoTopic());
        vo.setStatus(plan.getStatus());
        vo.setTopicNames(topics.stream().map(Topic::getName).collect(Collectors.toList()));
        vo.setTasks(taskVOs);
        return vo;
    }
}
