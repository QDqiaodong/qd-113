package com.tea.tracker.service;

import com.tea.tracker.dto.StockReplayResult;
import com.tea.tracker.dto.StorageRecordRequest;
import com.tea.tracker.dto.StorageRecordResponse;
import com.tea.tracker.entity.StorageRecord;
import com.tea.tracker.entity.Tea;
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
public class StorageRecordService {

    private final StorageRecordRepository storageRecordRepository;
    private final TeaRepository teaRepository;

    @Transactional
    public StorageRecordResponse createStorageRecord(Long teaId, StorageRecordRequest request) {
        Tea tea = teaRepository.findById(teaId)
                .orElseThrow(() -> new EntityNotFoundException("茶叶档案不存在: " + teaId));

        StorageRecord record = new StorageRecord();
        mapRequestToEntity(request, record);
        record.setTea(tea);
        record.setRecordDate(LocalDateTime.now());
        record.setCurrentStock(BigDecimal.ZERO);

        StorageRecord saved = storageRecordRepository.save(record);

        replayStockInternal(teaId);

        StorageRecord finalRecord = storageRecordRepository.findById(saved.getId())
                .orElseThrow(() -> new EntityNotFoundException("仓储记录不存在: " + saved.getId()));
        log.info("Created storage record for tea {} (id={})", tea.getName(), saved.getId());
        return toResponse(finalRecord);
    }

    @Transactional
    public StorageRecordResponse updateStorageRecord(Long teaId, Long id, StorageRecordRequest request) {
        StorageRecord record = storageRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("仓储记录不存在: " + id));

        if (!record.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("仓储记录不属于当前茶叶");
        }

        mapRequestToEntity(request, record);
        storageRecordRepository.save(record);

        replayStockInternal(teaId);

        StorageRecord finalRecord = storageRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("仓储记录不存在: " + id));
        log.info("Updated storage record id={} for tea {}", id, record.getTea().getName());
        return toResponse(finalRecord);
    }

    @Transactional
    public void deleteStorageRecord(Long teaId, Long id) {
        StorageRecord record = storageRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("仓储记录不存在: " + id));

        if (!record.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("仓储记录不属于当前茶叶");
        }

        storageRecordRepository.delete(record);

        replayStockInternal(teaId);

        log.info("Deleted storage record id={} for tea {}", id, record.getTea().getName());
    }

    @Transactional
    public StockReplayResult replayStock(Long teaId) {
        Tea tea = teaRepository.findById(teaId)
                .orElseThrow(() -> new EntityNotFoundException("茶叶档案不存在: " + teaId));

        BigDecimal previousStock = tea.getCurrentStock() != null ? tea.getCurrentStock() : BigDecimal.ZERO;

        List<StorageRecord> recalculated = replayStockInternal(teaId);

        tea = teaRepository.findById(teaId).orElseThrow();
        BigDecimal recalculatedStock = tea.getCurrentStock() != null ? tea.getCurrentStock() : BigDecimal.ZERO;

        StockReplayResult result = new StockReplayResult();
        result.setTeaId(teaId);
        result.setTeaName(tea.getName());
        result.setPreviousStock(previousStock);
        result.setRecalculatedStock(recalculatedStock);
        result.setTotalRecordsReplayed(recalculated.size());
        result.setReplayedRecords(recalculated.stream().map(this::toResponse).collect(Collectors.toList()));

        log.info("Replayed stock for tea {} (id={}): {} -> {} ({} records)",
                tea.getName(), teaId, previousStock, recalculatedStock, recalculated.size());
        return result;
    }

    private List<StorageRecord> replayStockInternal(Long teaId) {
        Tea tea = teaRepository.findById(teaId)
                .orElseThrow(() -> new EntityNotFoundException("茶叶档案不存在: " + teaId));

        List<StorageRecord> allRecords = storageRecordRepository.findByTeaIdOrderByRecordDateAsc(teaId);

        BigDecimal runningStock = BigDecimal.ZERO;
        for (StorageRecord r : allRecords) {
            BigDecimal change = r.getStockChange() != null ? r.getStockChange() : BigDecimal.ZERO;
            runningStock = runningStock.add(change);
            if (runningStock.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        String.format("库存回放发现负库存！记录id=%d, 变动: %s%s, 累计库存: %s%s",
                                r.getId(),
                                change, tea.getStockUnit() != null ? tea.getStockUnit() : "",
                                runningStock, tea.getStockUnit() != null ? tea.getStockUnit() : "")
                );
            }
            r.setCurrentStock(runningStock);
            storageRecordRepository.save(r);
        }

        tea.setCurrentStock(runningStock);
        teaRepository.save(tea);

        return allRecords;
    }

    @Transactional(readOnly = true)
    public List<StorageRecordResponse> getStorageRecordsByTeaId(Long teaId) {
        return storageRecordRepository.findByTeaIdOrderByRecordDateDesc(teaId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void mapRequestToEntity(StorageRecordRequest request, StorageRecord record) {
        record.setStorageLocation(request.getStorageLocation());
        record.setTemperature(request.getTemperature());
        record.setHumidity(request.getHumidity());
        record.setSealCondition(request.getSealCondition());
        record.setBrewingSessionId(request.getBrewingSessionId());
        record.setStockChange(request.getStockChange());
        record.setNotes(request.getNotes());
    }

    private StorageRecordResponse toResponse(StorageRecord record) {
        StorageRecordResponse resp = new StorageRecordResponse();
        resp.setId(record.getId());
        resp.setTeaId(record.getTea().getId());
        resp.setTeaName(record.getTea().getName());
        resp.setStorageLocation(record.getStorageLocation());
        resp.setTemperature(record.getTemperature());
        resp.setHumidity(record.getHumidity());
        resp.setSealCondition(record.getSealCondition());
        resp.setBrewingSessionId(record.getBrewingSessionId());
        resp.setStockChange(record.getStockChange());
        resp.setCurrentStock(record.getCurrentStock());
        resp.setRecordDate(record.getRecordDate());
        resp.setNotes(record.getNotes());
        resp.setCreatedAt(record.getCreatedAt());
        resp.setUpdatedAt(record.getUpdatedAt());
        return resp;
    }
}
