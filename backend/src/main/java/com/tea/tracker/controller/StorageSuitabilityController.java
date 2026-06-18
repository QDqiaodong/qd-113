package com.tea.tracker.controller;

import com.tea.tracker.dto.ApiResponse;
import com.tea.tracker.dto.StorageSuitabilityRequest;
import com.tea.tracker.dto.StorageSuitabilityResponse;
import com.tea.tracker.service.StorageSuitabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teas/{teaId}/suitability")
@RequiredArgsConstructor
public class StorageSuitabilityController {

    private final StorageSuitabilityService storageSuitabilityService;

    @PostMapping("/evaluate")
    public ApiResponse<StorageSuitabilityResponse> evaluateSuitability(
            @PathVariable Long teaId,
            @Valid @RequestBody StorageSuitabilityRequest request) {
        return ApiResponse.success(storageSuitabilityService.evaluateSuitability(teaId, request));
    }

    @GetMapping
    public ApiResponse<List<StorageSuitabilityResponse>> getSuitabilityRecords(@PathVariable Long teaId) {
        return ApiResponse.success(storageSuitabilityService.getSuitabilityRecords(teaId));
    }

    @GetMapping("/latest")
    public ApiResponse<StorageSuitabilityResponse> getLatestSuitability(@PathVariable Long teaId) {
        StorageSuitabilityResponse latest = storageSuitabilityService.getLatestSuitability(teaId);
        if (latest == null) {
            return ApiResponse.success("暂无适宜度记录", null);
        }
        return ApiResponse.success(latest);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSuitability(@PathVariable Long teaId, @PathVariable Long id) {
        storageSuitabilityService.deleteSuitability(teaId, id);
        return ApiResponse.success("删除成功", null);
    }
}
