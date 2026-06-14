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

        if (request.getStockChange() != null && tea.getCurrentStock() != null) {
            tea.setCurrentStock(tea.getCurrentStock().add(request.getStockChange()));
            teaRepository.save(tea);
        }

        StorageRecord saved = storageRecordRepository.save(record);
        log.info("Created storage record for tea {} (id={})", tea.getName(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public StorageRecordResponse updateStorageRecord(Long id, StorageRecordRequest request) {
        StorageRecord record = storageRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("仓储记录不存在: " + id));
        mapRequestToEntity(request, record);
        StorageRecord updated = storageRecordRepository.save(record);
        return toResponse(updated);
    }

    @Transactional
    public void deleteStorageRecord(Long id) {
        if (!storageRecordRepository.existsById(id)) {
            throw new EntityNotFoundException("仓储记录不存在: " + id);
        }
        storageRecordRepository.deleteById(id);
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
        record.setCurrentStock(request.getCurrentStock());
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
