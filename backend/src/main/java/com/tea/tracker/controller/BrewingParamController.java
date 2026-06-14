package com.tea.tracker.controller;

import com.tea.tracker.dto.ApiResponse;
import com.tea.tracker.dto.BrewingParamRequest;
import com.tea.tracker.dto.BrewingParamResponse;
import com.tea.tracker.service.BrewingParamService;
import com.tea.tracker.service.TeaTemplateCacheService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teas/{teaId}/brewing-params")
@RequiredArgsConstructor
public class BrewingParamController {

    private final BrewingParamService brewingParamService;
    private final TeaTemplateCacheService templateCacheService;

    @PostMapping
    public ApiResponse<BrewingParamResponse> createBrewingParam(
            @PathVariable Long teaId,
            @Valid @RequestBody BrewingParamRequest request) {
        return ApiResponse.success(brewingParamService.createBrewingParam(teaId, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<BrewingParamResponse> updateBrewingParam(
            @PathVariable Long teaId,
            @PathVariable Long id,
            @Valid @RequestBody BrewingParamRequest request) {
        return ApiResponse.success(brewingParamService.updateBrewingParam(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBrewingParam(@PathVariable Long teaId, @PathVariable Long id) {
        brewingParamService.deleteBrewingParam(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping
    public ApiResponse<List<BrewingParamResponse>> getBrewingParamsByTeaId(@PathVariable Long teaId) {
        return ApiResponse.success(brewingParamService.getBrewingParamsByTeaId(teaId));
    }

    @GetMapping("/{id}")
    public ApiResponse<BrewingParamResponse> getBrewingParamById(
            @PathVariable Long teaId, @PathVariable Long id) {
        return ApiResponse.success(brewingParamService.getBrewingParamById(id));
    }

    @GetMapping("/template")
    public ApiResponse<Map<String, Object>> getBrewingTemplate(
            @PathVariable Long teaId,
            @RequestParam String category) {
        return ApiResponse.success(templateCacheService.getBrewingTemplate(category));
    }
}
