package com.tea.tracker.controller;

import com.tea.tracker.dto.ApiResponse;
import com.tea.tracker.dto.TemplateVersionResponse;
import com.tea.tracker.service.TeaTemplateCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tea-templates")
@RequiredArgsConstructor
public class TeaTemplateController {

    private final TeaTemplateCacheService templateCacheService;

    @GetMapping("/categories")
    public ApiResponse<List<String>> getSupportedCategories() {
        return ApiResponse.success(templateCacheService.getSupportedCategories());
    }

    @GetMapping("/{category}")
    public ApiResponse<Map<String, Object>> getTemplate(@PathVariable String category) {
        return ApiResponse.success(templateCacheService.getBrewingTemplate(category));
    }

    @GetMapping("/{category}/ranges")
    public ApiResponse<Map<String, Object>> getParamRanges(@PathVariable String category) {
        return ApiResponse.success(templateCacheService.getParamRanges(category));
    }

    @GetMapping("/{category}/cache-status")
    public ApiResponse<Map<String, Object>> getCacheStatus(@PathVariable String category) {
        TeaTemplateCacheService.CacheHitStatus status = templateCacheService.getCacheHitStatus(category);
        Map<String, Object> result = new LinkedHashMap<>();
        if (status != null) {
            result.put("category", category);
            result.put("cacheHit", status.isCacheHit());
            result.put("cachedVersion", status.getCachedVersion());
            result.put("currentVersion", status.getCurrentVersion());
            result.put("versionMatch", status.isVersionMatch());
            result.put("hitTime", status.getHitTime());
        } else {
            result.put("category", category);
            result.put("cacheHit", false);
            result.put("message", "尚未访问过该茶类模板");
        }
        return ApiResponse.success(result);
    }

    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refreshAllTemplates() {
        templateCacheService.refreshAllTemplates();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("refreshedCategories", templateCacheService.getSupportedCategories());
        result.put("message", "所有茶类冲泡模板缓存已刷新");
        return ApiResponse.success(result);
    }

    @PostMapping("/{category}/refresh")
    public ApiResponse<Map<String, Object>> refreshTemplate(@PathVariable String category) {
        templateCacheService.refreshTemplate(category);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("category", category);
        result.put("message", "茶类冲泡模板缓存已刷新");
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{category}/cache")
    public ApiResponse<Map<String, Object>> evictTemplate(@PathVariable String category) {
        templateCacheService.evictTemplate(category);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("category", category);
        result.put("message", "茶类冲泡模板缓存已清除");
        return ApiResponse.success(result);
    }

    @GetMapping("/cache-status")
    public ApiResponse<Map<String, Map<String, Object>>> getAllCacheStatus() {
        Map<String, TeaTemplateCacheService.CacheHitStatus> allStatus = templateCacheService.getAllCacheStatus();
        Map<String, Map<String, Object>> result = new HashMap<>();
        allStatus.forEach((category, status) -> {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("cacheHit", status.isCacheHit());
            info.put("cachedVersion", status.getCachedVersion());
            info.put("currentVersion", status.getCurrentVersion());
            info.put("versionMatch", status.isVersionMatch());
            info.put("hitTime", status.getHitTime());
            result.put(category, info);
        });
        return ApiResponse.success(result);
    }

    @GetMapping("/versions")
    public ApiResponse<List<TemplateVersionResponse>> getAllTemplateVersions() {
        return ApiResponse.success(templateCacheService.getAllTemplateVersionsFromDb());
    }

    @GetMapping("/{category}/version")
    public ApiResponse<TemplateVersionResponse> getTemplateVersion(@PathVariable String category) {
        TemplateVersionResponse version = templateCacheService.getTemplateVersionFromDb(category);
        if (version == null) {
            return ApiResponse.error(404, "未找到该茶类的版本记录");
        }
        return ApiResponse.success(version);
    }
}
