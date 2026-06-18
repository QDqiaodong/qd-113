package com.tea.tracker.controller;

import com.tea.tracker.dto.ApiResponse;
import com.tea.tracker.dto.StockReplayResult;
import com.tea.tracker.dto.StorageRecordRequest;
import com.tea.tracker.dto.StorageRecordResponse;
import com.tea.tracker.service.StorageRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teas/{teaId}/storage-records")
@RequiredArgsConstructor
public class StorageRecordController {

    private final StorageRecordService storageRecordService;

    @PostMapping
    public ApiResponse<StorageRecordResponse> createStorageRecord(
            @PathVariable Long teaId,
            @Valid @RequestBody StorageRecordRequest request) {
        return ApiResponse.success(storageRecordService.createStorageRecord(teaId, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<StorageRecordResponse> updateStorageRecord(
            @PathVariable Long teaId,
            @PathVariable Long id,
            @Valid @RequestBody StorageRecordRequest request) {
        return ApiResponse.success(storageRecordService.updateStorageRecord(teaId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStorageRecord(@PathVariable Long teaId, @PathVariable Long id) {
        storageRecordService.deleteStorageRecord(teaId, id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping
    public ApiResponse<List<StorageRecordResponse>> getStorageRecordsByTeaId(@PathVariable Long teaId) {
        return ApiResponse.success(storageRecordService.getStorageRecordsByTeaId(teaId));
    }

    @PostMapping("/replay")
    public ApiResponse<StockReplayResult> replayStock(@PathVariable Long teaId) {
        return ApiResponse.success(storageRecordService.replayStock(teaId));
    }
}
