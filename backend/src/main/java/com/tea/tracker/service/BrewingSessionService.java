package com.tea.tracker.service;

import com.tea.tracker.dto.BrewingSessionRequest;
import com.tea.tracker.dto.BrewingSessionResponse;
import com.tea.tracker.entity.BrewingParam;
import com.tea.tracker.entity.BrewingSession;
import com.tea.tracker.entity.Tea;
import com.tea.tracker.repository.BrewingParamRepository;
import com.tea.tracker.repository.BrewingSessionRepository;
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
public class BrewingSessionService {

    private final BrewingSessionRepository brewingSessionRepository;
    private final TeaRepository teaRepository;
    private final BrewingParamRepository brewingParamRepository;

    @Transactional
    public BrewingSessionResponse createBrewingSession(Long teaId, BrewingSessionRequest request) {
        Tea tea = teaRepository.findById(teaId)
                .orElseThrow(() -> new EntityNotFoundException("茶叶档案不存在: " + teaId));

        BrewingSession session = new BrewingSession();
        mapRequestToEntity(request, session);
        session.setTea(tea);
        session.setSessionDate(LocalDateTime.now());

        if (request.getBrewingParamId() != null) {
            brewingParamRepository.findById(request.getBrewingParamId())
                    .ifPresent(param -> session.setBrewingParamId(param.getId()));
        }

        BrewingSession saved = brewingSessionRepository.save(session);
        log.info("Created brewing session for tea {} (id={})", tea.getName(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public BrewingSessionResponse updateBrewingSession(Long teaId, Long id, BrewingSessionRequest request) {
        BrewingSession session = brewingSessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("冲泡记录不存在: " + id));

        if (!session.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("冲泡记录不属于当前茶叶");
        }

        mapRequestToEntity(request, session);
        BrewingSession updated = brewingSessionRepository.save(session);
        return toResponse(updated);
    }

    @Transactional
    public void deleteBrewingSession(Long teaId, Long id) {
        BrewingSession session = brewingSessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("冲泡记录不存在: " + id));

        if (!session.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("冲泡记录不属于当前茶叶");
        }

        brewingSessionRepository.delete(session);
    }

    @Transactional(readOnly = true)
    public List<BrewingSessionResponse> getBrewingSessionsByTeaId(Long teaId) {
        return brewingSessionRepository.findByTeaIdOrderBySessionDateDesc(teaId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void mapRequestToEntity(BrewingSessionRequest request, BrewingSession session) {
        session.setBrewingParamId(request.getBrewingParamId());
        session.setActualWaterTemperature(request.getActualWaterTemperature());
        session.setFirstInfusionTime(request.getFirstInfusionTime());
        session.setSecondInfusionTime(request.getSecondInfusionTime());
        session.setThirdInfusionTime(request.getThirdInfusionTime());
        session.setSubsequentInfusionTime(request.getSubsequentInfusionTime());
        session.setTasteImpression(request.getTasteImpression());
    }

    private BrewingSessionResponse toResponse(BrewingSession session) {
        BrewingSessionResponse resp = new BrewingSessionResponse();
        resp.setId(session.getId());
        resp.setTeaId(session.getTea().getId());
        resp.setTeaName(session.getTea().getName());
        resp.setBrewingParamId(session.getBrewingParamId());

        if (session.getBrewingParamId() != null) {
            brewingParamRepository.findById(session.getBrewingParamId())
                    .ifPresent(param -> resp.setBrewingParamName(param.getParamName()));
        }

        resp.setActualWaterTemperature(session.getActualWaterTemperature());
        resp.setFirstInfusionTime(session.getFirstInfusionTime());
        resp.setSecondInfusionTime(session.getSecondInfusionTime());
        resp.setThirdInfusionTime(session.getThirdInfusionTime());
        resp.setSubsequentInfusionTime(session.getSubsequentInfusionTime());
        resp.setTasteImpression(session.getTasteImpression());
        resp.setSessionDate(session.getSessionDate());
        resp.setCreatedAt(session.getCreatedAt());
        resp.setUpdatedAt(session.getUpdatedAt());
        return resp;
    }
}
