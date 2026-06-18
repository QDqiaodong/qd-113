package com.tea.tracker.controller;

import com.tea.tracker.dto.ApiResponse;
import com.tea.tracker.dto.StorageRiskActionRequest;
import com.tea.tracker.dto.StorageRiskActionResponse;
import com.tea.tracker.service.StorageRiskActionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teas/{teaId}/risk-actions")
@RequiredArgsConstructor
public class StorageRiskActionController {

    private final StorageRiskActionService storageRiskActionService;

    @PostMapping
    public ApiResponse<StorageRiskActionResponse> createRiskAction(
            @PathVariable Long teaId,
            @Valid @RequestBody StorageRiskActionRequest request) {
        return ApiResponse.success(storageRiskActionService.createRiskAction(teaId, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<StorageRiskActionResponse> updateRiskAction(
            @PathVariable Long teaId,
            @PathVariable Long id,
            @Valid @RequestBody StorageRiskActionRequest request) {
        return ApiResponse.success(storageRiskActionService.updateRiskAction(teaId, id, request));
    }

    @GetMapping
    public ApiResponse<List<StorageRiskActionResponse>> getRiskActions(
            @PathVariable Long teaId,
            @RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            return ApiResponse.success(storageRiskActionService.getRiskActionsByStatus(teaId, status));
        }
        return ApiResponse.success(storageRiskActionService.getRiskActions(teaId));
    }

    @GetMapping("/{id}")
    public ApiResponse<StorageRiskActionResponse> getRiskActionById(
            @PathVariable Long teaId,
            @PathVariable Long id) {
        return ApiResponse.success(storageRiskActionService.getRiskActionById(teaId, id));
    }

    @GetMapping("/suitability/{suitabilityId}")
    public ApiResponse<List<StorageRiskActionResponse>> getRiskActionsBySuitability(
            @PathVariable Long teaId,
            @PathVariable Long suitabilityId) {
        return ApiResponse.success(storageRiskActionService.getRiskActionsBySuitability(suitabilityId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRiskAction(
            @PathVariable Long teaId,
            @PathVariable Long id) {
        storageRiskActionService.deleteRiskAction(teaId, id);
        return ApiResponse.success("删除成功", null);
    }
}
