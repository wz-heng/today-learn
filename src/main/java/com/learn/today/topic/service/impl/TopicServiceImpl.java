package com.learn.today.topic.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learn.today.topic.entity.Topic;
import com.learn.today.topic.entity.vo.TopicVO;
import com.learn.today.topic.mapper.TopicMapper;
import com.learn.today.topic.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    @Override
    public List<TopicVO> listAll() {
        List<Topic> topics = lambdaQuery()
                .eq(Topic::getIsActive, true)
                .orderByAsc(Topic::getSortOrder)
                .list();

        // 平铺列表转 VO，建 id -> VO 索引
        Map<String, TopicVO> map = new LinkedHashMap<>();
        for (Topic t : topics) {
            TopicVO vo = new TopicVO();
            vo.setId(t.getId());
            vo.setName(t.getName());
            vo.setSlug(t.getSlug());
            vo.setParentId(t.getParentId());
            vo.setDescription(t.getDescription());
            vo.setIcon(t.getIcon());
            vo.setSortOrder(t.getSortOrder());
            map.put(t.getId(), vo);
        }

        // 组装树：无 parentId 的为根节点，有的挂到父节点下
        List<TopicVO> roots = new ArrayList<>();
        for (TopicVO vo : map.values()) {
            if (vo.getParentId() == null) {
                roots.add(vo);
            } else {
                TopicVO parent = map.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                }
            }
        }
        return roots;
    }
}
