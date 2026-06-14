package com.tea.tracker.controller;

import com.tea.tracker.dto.ApiResponse;
import com.tea.tracker.dto.TastingNoteRequest;
import com.tea.tracker.dto.TastingNoteResponse;
import com.tea.tracker.service.TastingNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teas/{teaId}/tasting-notes")
@RequiredArgsConstructor
public class TastingNoteController {

    private final TastingNoteService tastingNoteService;

    @PostMapping
    public ApiResponse<TastingNoteResponse> createTastingNote(
            @PathVariable Long teaId,
            @Valid @RequestBody TastingNoteRequest request) {
        return ApiResponse.success(tastingNoteService.createTastingNote(teaId, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TastingNoteResponse> updateTastingNote(
            @PathVariable Long teaId,
            @PathVariable Long id,
            @Valid @RequestBody TastingNoteRequest request) {
        return ApiResponse.success(tastingNoteService.updateTastingNote(teaId, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTastingNote(@PathVariable Long teaId, @PathVariable Long id) {
        tastingNoteService.deleteTastingNote(teaId, id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping
    public ApiResponse<List<TastingNoteResponse>> getTastingNotesByTeaId(@PathVariable Long teaId) {
        return ApiResponse.success(tastingNoteService.getTastingNotesByTeaId(teaId));
    }
}
