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
    public TastingNoteResponse updateTastingNote(Long id, TastingNoteRequest request) {
        TastingNote note = tastingNoteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("品饮记录不存在: " + id));
        mapRequestToEntity(request, note);
        TastingNote updated = tastingNoteRepository.save(note);
        return toResponse(updated);
    }

    @Transactional
    public void deleteTastingNote(Long id) {
        if (!tastingNoteRepository.existsById(id)) {
            throw new EntityNotFoundException("品饮记录不存在: " + id);
        }
        tastingNoteRepository.deleteById(id);
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
        note.setOverallScore(request.getOverallScore());
        note.setImpression(request.getImpression());
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
