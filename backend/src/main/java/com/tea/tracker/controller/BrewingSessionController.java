package com.tea.tracker.controller;

import com.tea.tracker.dto.ApiResponse;
import com.tea.tracker.dto.BrewingSessionRequest;
import com.tea.tracker.dto.BrewingSessionResponse;
import com.tea.tracker.service.BrewingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teas/{teaId}/brewing-sessions")
@RequiredArgsConstructor
public class BrewingSessionController {

    private final BrewingSessionService brewingSessionService;

    @PostMapping
    public ApiResponse<BrewingSessionResponse> createBrewingSession(
            @PathVariable Long teaId,
            @Valid @RequestBody BrewingSessionRequest request) {
        return ApiResponse.success(brewingSessionService.createBrewingSession(teaId, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<BrewingSessionResponse> updateBrewingSession(
            @PathVariable Long teaId,
            @PathVariable Long id,
            @Valid @RequestBody BrewingSessionRequest request) {
        return ApiResponse.success(brewingSessionService.updateBrewingSession(teaId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBrewingSession(@PathVariable Long teaId, @PathVariable Long id) {
        brewingSessionService.deleteBrewingSession(teaId, id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping
    public ApiResponse<List<BrewingSessionResponse>> getBrewingSessionsByTeaId(@PathVariable Long teaId) {
        return ApiResponse.success(brewingSessionService.getBrewingSessionsByTeaId(teaId));
    }
}
