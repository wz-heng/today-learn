package com.learn.today.archive.controller;

import com.learn.today.archive.entity.vo.ArchiveVO;
import com.learn.today.archive.entity.vo.StatsVO;
import com.learn.today.archive.service.ArchiveService;
import com.learn.today.common.context.UserContext;
import com.learn.today.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/archive")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;

    /** GET /archive - 获取归档列表 */
    @GetMapping
    public Result<List<ArchiveVO>> listArchive(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(archiveService.listArchive(UserContext.getUserId(), page, size));
    }

    /** GET /archive/stats - 获取学习统计 */
    @GetMapping("/stats")
    public Result<StatsVO> getStats() {
        return Result.success(archiveService.getStats(UserContext.getUserId()));
    }
}
