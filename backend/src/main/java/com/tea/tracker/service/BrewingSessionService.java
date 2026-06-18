package com.tea.tracker.service;

import com.tea.tracker.dto.BrewingSessionRequest;
import com.tea.tracker.dto.BrewingSessionResponse;
import com.tea.tracker.entity.BrewingParam;
import com.tea.tracker.entity.BrewingSession;
import com.tea.tracker.entity.StorageRecord;
import com.tea.tracker.entity.Tea;
import com.tea.tracker.repository.BrewingParamRepository;
import com.tea.tracker.repository.BrewingSessionRepository;
import com.tea.tracker.repository.StorageRecordRepository;
import com.tea.tracker.repository.TeaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final StorageRecordRepository storageRecordRepository;

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

        boolean shouldDeduct = Boolean.TRUE.equals(request.getDeductStock())
                && request.getStockAmount() != null
                && request.getStockAmount().compareTo(BigDecimal.ZERO) > 0;

        session.setStockDeducted(shouldDeduct);
        if (shouldDeduct) {
            session.setStockAmount(request.getStockAmount());
        } else {
            session.setStockAmount(null);
            session.setStorageRecordId(null);
        }

        BrewingSession saved = brewingSessionRepository.save(session);

        if (shouldDeduct) {
            StorageRecord storageRecord = createStorageRecordForDeduction(saved, tea, request.getStockAmount());
            saved.setStorageRecordId(storageRecord.getId());
            replayStockInternal(teaId);
            saved = brewingSessionRepository.save(saved);
            tea = teaRepository.findById(teaId).orElse(tea);
        }

        log.info("Created brewing session for tea {} (id={}), stockDeducted={}, amount={}",
                tea.getName(), saved.getId(), saved.getStockDeducted(), saved.getStockAmount());
        return toResponse(saved, tea);
    }

    @Transactional
    public BrewingSessionResponse updateBrewingSession(Long teaId, Long id, BrewingSessionRequest request) {
        BrewingSession session = brewingSessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("冲泡记录不存在: " + id));

        if (!session.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("冲泡记录不属于当前茶叶");
        }

        boolean wasDeducted = Boolean.TRUE.equals(session.getStockDeducted());
        Long oldStorageRecordId = session.getStorageRecordId();

        mapRequestToEntity(request, session);

        boolean shouldDeduct = Boolean.TRUE.equals(request.getDeductStock())
                && request.getStockAmount() != null
                && request.getStockAmount().compareTo(BigDecimal.ZERO) > 0;

        session.setStockDeducted(shouldDeduct);
        if (shouldDeduct) {
            session.setStockAmount(request.getStockAmount());
        } else {
            session.setStockAmount(null);
            session.setStorageRecordId(null);
        }

        if (wasDeducted && oldStorageRecordId != null) {
            storageRecordRepository.deleteById(oldStorageRecordId);
            session.setStorageRecordId(null);
        }

        BrewingSession updated = brewingSessionRepository.save(session);

        Tea tea = teaRepository.findById(teaId).orElseThrow();

        if (shouldDeduct) {
            StorageRecord existing = storageRecordRepository.findByBrewingSessionId(updated.getId()).orElse(null);
            StorageRecord storageRecord;
            if (existing != null) {
                existing.setStockChange(request.getStockAmount().negate());
                existing.setRecordDate(LocalDateTime.now());
                storageRecord = storageRecordRepository.save(existing);
            } else {
                storageRecord = createStorageRecordForDeduction(updated, tea, request.getStockAmount());
            }
            updated.setStorageRecordId(storageRecord.getId());
            updated = brewingSessionRepository.save(updated);
        }

        if (wasDeducted || shouldDeduct) {
            replayStockInternal(teaId);
            tea = teaRepository.findById(teaId).orElse(tea);
        }

        return toResponse(updated, tea);
    }

    @Transactional
    public void deleteBrewingSession(Long teaId, Long id) {
        BrewingSession session = brewingSessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("冲泡记录不存在: " + id));

        if (!session.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("冲泡记录不属于当前茶叶");
        }

        boolean wasDeducted = Boolean.TRUE.equals(session.getStockDeducted());

        brewingSessionRepository.delete(session);

        if (wasDeducted) {
            storageRecordRepository.deleteByBrewingSessionId(id);
            replayStockInternal(teaId);
        }

        log.info("Deleted brewing session id={} for tea {}", id, session.getTea().getName());
    }

    @Transactional(readOnly = true)
    public List<BrewingSessionResponse> getBrewingSessionsByTeaId(Long teaId) {
        Tea tea = teaRepository.findById(teaId)
                .orElseThrow(() -> new EntityNotFoundException("茶叶档案不存在: " + teaId));
        return brewingSessionRepository.findByTeaIdOrderBySessionDateDesc(teaId)
                .stream()
                .map(s -> toResponse(s, tea))
                .collect(Collectors.toList());
    }

    private StorageRecord createStorageRecordForDeduction(BrewingSession session, Tea tea, BigDecimal amount) {
        StorageRecord record = new StorageRecord();
        record.setTea(tea);
        record.setBrewingSessionId(session.getId());
        record.setStockChange(amount.negate());
        record.setCurrentStock(BigDecimal.ZERO);
        record.setRecordDate(LocalDateTime.now());
        record.setNotes("冲泡消耗扣减：" + tea.getName());
        return storageRecordRepository.save(record);
    }

    private void replayStockInternal(Long teaId) {
        Tea tea = teaRepository.findById(teaId)
                .orElseThrow(() -> new EntityNotFoundException("茶叶档案不存在: " + teaId));

        List<StorageRecord> allRecords = storageRecordRepository.findByTeaIdOrderByRecordDateAsc(teaId);

        BigDecimal runningStock = BigDecimal.ZERO;
        for (StorageRecord r : allRecords) {
            BigDecimal change = r.getStockChange() != null ? r.getStockChange() : BigDecimal.ZERO;
            runningStock = runningStock.add(change);
            if (runningStock.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        String.format("库存不足！冲泡扣减后库存将为负数。累计库存: %s%s，需要扣减但库存不够。" +
                                        "请先通过仓储记录补充库存，或调整扣减数量。",
                                runningStock, tea.getStockUnit() != null ? tea.getStockUnit() : "")
                );
            }
            r.setCurrentStock(runningStock);
            storageRecordRepository.save(r);
        }

        tea.setCurrentStock(runningStock);
        teaRepository.save(tea);
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

    private BrewingSessionResponse toResponse(BrewingSession session, Tea tea) {
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
        resp.setStockDeducted(session.getStockDeducted());
        resp.setStockAmount(session.getStockAmount());
        resp.setStorageRecordId(session.getStorageRecordId());
        resp.setStockUnit(tea.getStockUnit());
        resp.setCurrentStockAfterDeduction(tea.getCurrentStock());
        resp.setSessionDate(session.getSessionDate());
        resp.setCreatedAt(session.getCreatedAt());
        resp.setUpdatedAt(session.getUpdatedAt());
        return resp;
    }
}
