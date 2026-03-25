package com.learn.today.archive.service;

import com.learn.today.archive.entity.vo.ArchiveVO;
import com.learn.today.archive.entity.vo.StatsVO;

import java.util.List;

public interface ArchiveService {

    /** 分页获取归档列表 */
    List<ArchiveVO> listArchive(String userId, int page, int size);

    /** 获取学习统计数据 */
    StatsVO getStats(String userId);
}
