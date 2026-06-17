package com.tea.tracker.service;

import com.tea.tracker.dto.TastingNoteRequest;
import com.tea.tracker.dto.TastingNoteResponse;
import com.tea.tracker.entity.TastingNote;
import com.tea.tracker.entity.Tea;
import com.tea.tracker.repository.TastingNoteRepository;
import com.tea.tracker.repository.TeaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TastingNoteService {

    private final TastingNoteRepository tastingNoteRepository;
    private final TeaRepository teaRepository;

    @Transactional
    public TastingNoteResponse createTastingNote(Long teaId, TastingNoteRequest request) {
        Tea tea = teaRepository.findById(teaId)
                .orElseThrow(() -> new EntityNotFoundException("茶叶档案不存在: " + teaId));

        TastingNote note = new TastingNote();
        mapRequestToEntity(request, note);
        note.setTea(tea);
        note.setTastingDate(LocalDateTime.now());
        TastingNote saved = tastingNoteRepository.save(note);
        log.info("Created tasting note for tea {} (id={})", tea.getName(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public TastingNoteResponse updateTastingNote(Long teaId, Long id, TastingNoteRequest request) {
        TastingNote note = tastingNoteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("品饮记录不存在: " + id));

        if (!note.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("品饮记录不属于当前茶叶");
        }

        mapRequestToEntity(request, note);
        TastingNote updated = tastingNoteRepository.save(note);
        return toResponse(updated);
    }

    @Transactional
    public void deleteTastingNote(Long teaId, Long id) {
        TastingNote note = tastingNoteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("品饮记录不存在: " + id));

        if (!note.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("品饮记录不属于当前茶叶");
        }

        tastingNoteRepository.delete(note);
    }

    @Transactional(readOnly = true)
    public List<TastingNoteResponse> getTastingNotesByTeaId(Long teaId) {
        return tastingNoteRepository.findByTeaIdOrderByTastingDateDesc(teaId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void mapRequestToEntity(TastingNoteRequest request, TastingNote note) {
        note.setBrewingMethod(request.getBrewingMethod());
        note.setAromaScore(request.getAromaScore());
        note.setAromaDesc(request.getAromaDesc());
        note.setLiquorColorScore(request.getLiquorColorScore());
        note.setLiquorColorDesc(request.getLiquorColorDesc());
        note.setTasteScore(request.getTasteScore());
        note.setTasteDesc(request.getTasteDesc());
        note.setAftertasteScore(request.getAftertasteScore());
        note.setAftertasteDesc(request.getAftertasteDesc());
        note.setOverallScore(calculateOverallScore(request));
        note.setImpression(request.getImpression());
    }

    private Integer calculateOverallScore(TastingNoteRequest request) {
        if (request.getOverallScore() != null && request.getOverallScore() > 0) {
            return request.getOverallScore();
        }
        int count = 0;
        int sum = 0;
        if (request.getAromaScore() != null) {
            sum += request.getAromaScore();
            count++;
        }
        if (request.getLiquorColorScore() != null) {
            sum += request.getLiquorColorScore();
            count++;
        }
        if (request.getTasteScore() != null) {
            sum += request.getTasteScore();
            count++;
        }
        if (request.getAftertasteScore() != null) {
            sum += request.getAftertasteScore();
            count++;
        }
        if (count == 0) {
            return 0;
        }
        return Math.round((float) sum / count);
    }

    private TastingNoteResponse toResponse(TastingNote note) {
        TastingNoteResponse resp = new TastingNoteResponse();
        resp.setId(note.getId());
        resp.setTeaId(note.getTea().getId());
        resp.setTeaName(note.getTea().getName());
        resp.setBrewingMethod(note.getBrewingMethod());
        resp.setAromaScore(note.getAromaScore());
        resp.setAromaDesc(note.getAromaDesc());
        resp.setLiquorColorScore(note.getLiquorColorScore());
        resp.setLiquorColorDesc(note.getLiquorColorDesc());
        resp.setTasteScore(note.getTasteScore());
        resp.setTasteDesc(note.getTasteDesc());
        resp.setAftertasteScore(note.getAftertasteScore());
        resp.setAftertasteDesc(note.getAftertasteDesc());
        resp.setOverallScore(note.getOverallScore());
        resp.setImpression(note.getImpression());
        resp.setTastingDate(note.getTastingDate());
        resp.setCreatedAt(note.getCreatedAt());
        resp.setUpdatedAt(note.getUpdatedAt());
        return resp;
    }
}
