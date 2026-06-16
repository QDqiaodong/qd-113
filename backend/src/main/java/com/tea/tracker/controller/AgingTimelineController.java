package com.tea.tracker.controller;

import com.tea.tracker.dto.AgingTimelineResponse;
import com.tea.tracker.dto.ApiResponse;
import com.tea.tracker.service.AgingTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teas")
@RequiredArgsConstructor
public class AgingTimelineController {

    private final AgingTimelineService agingTimelineService;

    @GetMapping("/{id}/aging-timeline")
    public ApiResponse<AgingTimelineResponse> getAgingTimeline(@PathVariable Long id) {
        return ApiResponse.success(agingTimelineService.getAgingTimeline(id));
    }

    @GetMapping("/aging-timelines")
    public ApiResponse<List<AgingTimelineResponse>> getAgingTimelines(
            @RequestParam(required = false) String category) {
        return ApiResponse.success(agingTimelineService.getAgingTimelinesByCategory(category));
    }
}
