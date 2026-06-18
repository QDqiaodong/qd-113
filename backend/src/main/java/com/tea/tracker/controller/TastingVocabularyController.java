package com.tea.tracker.controller;

import com.tea.tracker.dto.ApiResponse;
import com.tea.tracker.dto.TastingVocabularyRequest;
import com.tea.tracker.dto.TastingVocabularyResponse;
import com.tea.tracker.service.TastingVocabularyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasting-vocabularies")
@RequiredArgsConstructor
public class TastingVocabularyController {

    private final TastingVocabularyService vocabularyService;

    @GetMapping
    public ApiResponse<List<TastingVocabularyResponse>> getAllVocabularies(
            @RequestParam(required = false) String vocabularyType,
            @RequestParam(required = false) String teaCategory) {
        List<TastingVocabularyResponse> result;
        if (vocabularyType != null && teaCategory != null) {
            result = vocabularyService.getVocabulariesByTypeAndTeaCategory(vocabularyType, teaCategory);
        } else if (vocabularyType != null) {
            result = vocabularyService.getVocabulariesByType(vocabularyType);
        } else if (teaCategory != null) {
            result = vocabularyService.getVocabulariesByTeaCategory(teaCategory);
        } else {
            result = vocabularyService.getAllVocabularies();
        }
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<TastingVocabularyResponse> getVocabularyById(@PathVariable Long id) {
        return ApiResponse.success(vocabularyService.getVocabularyById(id));
    }

    @PostMapping
    public ApiResponse<TastingVocabularyResponse> createVocabulary(
            @Valid @RequestBody TastingVocabularyRequest request) {
        return ApiResponse.success(vocabularyService.createVocabulary(request));
    }

    @PostMapping("/batch")
    public ApiResponse<Void> batchCreateVocabularies(
            @Valid @RequestBody List<TastingVocabularyRequest> requests) {
        vocabularyService.batchCreateVocabularies(requests);
        return ApiResponse.success("批量创建成功", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<TastingVocabularyResponse> updateVocabulary(
            @PathVariable Long id,
            @Valid @RequestBody TastingVocabularyRequest request) {
        return ApiResponse.success(vocabularyService.updateVocabulary(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVocabulary(@PathVariable Long id) {
        vocabularyService.deleteVocabulary(id);
        return ApiResponse.success("删除成功", null);
    }
}
