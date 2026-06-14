package com.tea.tracker.controller;

import com.tea.tracker.dto.ApiResponse;
import com.tea.tracker.dto.TeaRequest;
import com.tea.tracker.dto.TeaResponse;
import com.tea.tracker.service.TeaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teas")
@RequiredArgsConstructor
public class TeaController {

    private final TeaService teaService;

    @PostMapping
    public ApiResponse<TeaResponse> createTea(@Valid @RequestBody TeaRequest request) {
        return ApiResponse.success(teaService.createTea(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TeaResponse> updateTea(@PathVariable Long id, @Valid @RequestBody TeaRequest request) {
        return ApiResponse.success(teaService.updateTea(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTea(@PathVariable Long id) {
        teaService.deleteTea(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/{id}")
    public ApiResponse<TeaResponse> getTeaById(@PathVariable Long id) {
        return ApiResponse.success(teaService.getTeaById(id));
    }

    @GetMapping
    public ApiResponse<List<TeaResponse>> getAllTeas(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return ApiResponse.success(teaService.searchTeas(keyword));
        }
        if (category != null && !category.isBlank()) {
            return ApiResponse.success(teaService.getTeasByCategory(category));
        }
        if (region != null && !region.isBlank()) {
            return ApiResponse.success(teaService.getTeasByRegion(region));
        }
        return ApiResponse.success(teaService.getAllTeas());
    }

    @GetMapping("/categories")
    public ApiResponse<List<String>> getAllCategories() {
        return ApiResponse.success(teaService.getAllCategories());
    }

    @GetMapping("/regions")
    public ApiResponse<List<String>> getAllRegions() {
        return ApiResponse.success(teaService.getAllRegions());
    }
}
