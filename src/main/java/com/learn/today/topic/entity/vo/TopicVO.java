package com.learn.today.topic.entity.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 学习方向 VO，含子方向列表，用于返回树形结构
 */
@Data
public class TopicVO {
    private String id;
    private String name;
    private String slug;
    private String parentId;
    private String description;
    private String icon;
    private Integer sortOrder;
    private List<TopicVO> children = new ArrayList<>();
}
