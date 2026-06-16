package com.tea.tracker.service;

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

        BigDecimal stockChange = request.getStockChange() != null ? request.getStockChange() : BigDecimal.ZERO;
        BigDecimal currentTeaStock = tea.getCurrentStock() != null ? tea.getCurrentStock() : BigDecimal.ZERO;
        BigDecimal newStock = currentTeaStock.add(stockChange);

        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    String.format("库存变动会导致负库存！当前库存: %s%s, 变动: %s%s, 变动后: %s%s",
                            currentTeaStock, tea.getStockUnit() != null ? tea.getStockUnit() : "",
                            stockChange, tea.getStockUnit() != null ? tea.getStockUnit() : "",
                            newStock, tea.getStockUnit() != null ? tea.getStockUnit() : "")
            );
        }

        StorageRecord record = new StorageRecord();
        mapRequestToEntity(request, record);
        record.setTea(tea);
        record.setRecordDate(LocalDateTime.now());
        record.setCurrentStock(newStock);

        tea.setCurrentStock(newStock);
        teaRepository.save(tea);

        StorageRecord saved = storageRecordRepository.save(record);
        log.info("Created storage record for tea {} (id={}), stock: {} -> {}",
                tea.getName(), saved.getId(), currentTeaStock, newStock);
        return toResponse(saved);
    }

    @Transactional
    public StorageRecordResponse updateStorageRecord(Long teaId, Long id, StorageRecordRequest request) {
        StorageRecord record = storageRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("仓储记录不存在: " + id));

        if (!record.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("仓储记录不属于当前茶叶");
        }

        Tea tea = record.getTea();
        BigDecimal oldChange = record.getStockChange() != null ? record.getStockChange() : BigDecimal.ZERO;
        BigDecimal newChange = request.getStockChange() != null ? request.getStockChange() : BigDecimal.ZERO;
        BigDecimal currentTeaStock = tea.getCurrentStock() != null ? tea.getCurrentStock() : BigDecimal.ZERO;

        BigDecimal effectiveStock = currentTeaStock.subtract(oldChange);
        BigDecimal newStock = effectiveStock.add(newChange);

        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    String.format("库存变动会导致负库存！原变动回退后库存: %s%s, 新变动: %s%s, 变动后: %s%s",
                            effectiveStock, tea.getStockUnit() != null ? tea.getStockUnit() : "",
                            newChange, tea.getStockUnit() != null ? tea.getStockUnit() : "",
                            newStock, tea.getStockUnit() != null ? tea.getStockUnit() : "")
            );
        }

        tea.setCurrentStock(newStock);
        teaRepository.save(tea);

        mapRequestToEntity(request, record);
        record.setCurrentStock(newStock);
        StorageRecord updated = storageRecordRepository.save(record);
        log.info("Updated storage record id={} for tea {}, stock adjusted to {}",
                id, tea.getName(), newStock);
        return toResponse(updated);
    }

    @Transactional
    public void deleteStorageRecord(Long teaId, Long id) {
        StorageRecord record = storageRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("仓储记录不存在: " + id));

        if (!record.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("仓储记录不属于当前茶叶");
        }

        Tea tea = record.getTea();
        BigDecimal oldChange = record.getStockChange() != null ? record.getStockChange() : BigDecimal.ZERO;
        BigDecimal currentTeaStock = tea.getCurrentStock() != null ? tea.getCurrentStock() : BigDecimal.ZERO;
        BigDecimal newStock = currentTeaStock.subtract(oldChange);

        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    String.format("删除该记录会导致负库存！当前库存: %s%s, 回退变动: %s%s, 回退后: %s%s",
                            currentTeaStock, tea.getStockUnit() != null ? tea.getStockUnit() : "",
                            oldChange, tea.getStockUnit() != null ? tea.getStockUnit() : "",
                            newStock, tea.getStockUnit() != null ? tea.getStockUnit() : "")
            );
        }

        tea.setCurrentStock(newStock);
        teaRepository.save(tea);

        storageRecordRepository.delete(record);
        log.info("Deleted storage record id={} for tea {}, stock adjusted to {}",
                id, tea.getName(), newStock);
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
        resp.setStockChange(record.getStockChange());
        resp.setCurrentStock(record.getCurrentStock());
        resp.setRecordDate(record.getRecordDate());
        resp.setNotes(record.getNotes());
        resp.setCreatedAt(record.getCreatedAt());
        resp.setUpdatedAt(record.getUpdatedAt());
        return resp;
    }
}
