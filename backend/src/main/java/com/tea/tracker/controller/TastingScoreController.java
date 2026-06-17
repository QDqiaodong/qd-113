package com.tea.tracker.controller;

import com.tea.tracker.dto.ApiResponse;
import com.tea.tracker.dto.ScoreByCategory;
import com.tea.tracker.dto.ScoreByYear;
import com.tea.tracker.dto.TastingScoreSummary;
import com.tea.tracker.service.TastingScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasting-scores")
@RequiredArgsConstructor
public class TastingScoreController {

    private final TastingScoreService tastingScoreService;

    @GetMapping("/summary")
    public ApiResponse<List<TastingScoreSummary>> getScoreSummaryByTea() {
        return ApiResponse.success(tastingScoreService.getScoreSummaryByTea());
    }

    @GetMapping("/summary/{teaId}")
    public ApiResponse<TastingScoreSummary> getScoreSummaryByTeaId(@PathVariable Long teaId) {
        return ApiResponse.success(tastingScoreService.getScoreSummaryByTeaId(teaId));
    }

    @GetMapping("/summary/by-category")
    public ApiResponse<List<ScoreByCategory>> getScoreSummaryByCategory() {
        return ApiResponse.success(tastingScoreService.getScoreSummaryByCategory());
    }

    @GetMapping("/summary/by-year")
    public ApiResponse<List<ScoreByYear>> getScoreSummaryByYear() {
        return ApiResponse.success(tastingScoreService.getScoreSummaryByYear());
    }
}
