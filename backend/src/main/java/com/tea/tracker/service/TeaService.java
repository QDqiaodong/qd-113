package com.tea.tracker.service;

import com.tea.tracker.dto.StockTrendPoint;
import com.tea.tracker.dto.TeaRequest;
import com.tea.tracker.dto.TeaResponse;
import com.tea.tracker.entity.StorageRecord;
import com.tea.tracker.entity.Tea;
import com.tea.tracker.repository.StorageRecordRepository;
import com.tea.tracker.repository.TeaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeaService {

    private final TeaRepository teaRepository;
    private final StorageRecordRepository storageRecordRepository;

    @Transactional
    public TeaResponse createTea(TeaRequest request) {
        Tea tea = new Tea();
        mapRequestToEntity(request, tea);
        Tea saved = teaRepository.save(tea);
        log.info("Created tea: {} (id={})", saved.getName(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public TeaResponse updateTea(Long id, TeaRequest request) {
        Tea tea = teaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("茶叶档案不存在: " + id));
        mapRequestToEntity(request, tea);
        Tea updated = teaRepository.save(tea);
        log.info("Updated tea: {} (id={})", updated.getName(), updated.getId());
        return toResponse(updated);
    }

    @Transactional
    public void deleteTea(Long id) {
        if (!teaRepository.existsById(id)) {
            throw new EntityNotFoundException("茶叶档案不存在: " + id);
        }
        teaRepository.deleteById(id);
        log.info("Deleted tea id={}", id);
    }

    @Transactional(readOnly = true)
    public TeaResponse getTeaById(Long id) {
        Tea tea = teaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("茶叶档案不存在: " + id));
        return toResponse(tea);
    }

    @Transactional(readOnly = true)
    public List<TeaResponse> getAllTeas() {
        return teaRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeaResponse> getTeasByCategory(String category) {
        return teaRepository.findByTeaCategoryOrderByCreatedAtDesc(category)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeaResponse> getTeasByRegion(String region) {
        return teaRepository.findByOriginRegionOrderByCreatedAtDesc(region)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeaResponse> searchTeas(String keyword) {
        return teaRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return teaRepository.findAllCategories();
    }

    @Transactional(readOnly = true)
    public List<String> getAllRegions() {
        return teaRepository.findAllRegions();
    }

    private void mapRequestToEntity(TeaRequest request, Tea tea) {
        tea.setName(request.getName());
        tea.setTeaCategory(request.getTeaCategory());
        tea.setOriginRegion(request.getOriginRegion());
        tea.setHarvestYear(request.getHarvestYear());
        tea.setStorageMethod(request.getStorageMethod());
        tea.setCurrentStock(request.getCurrentStock());
        tea.setStockUnit(request.getStockUnit());
        tea.setDescription(request.getDescription());
        tea.setImageUrl(request.getImageUrl());
    }

    private TeaResponse toResponse(Tea tea) {
        TeaResponse resp = new TeaResponse();
        resp.setId(tea.getId());
        resp.setName(tea.getName());
        resp.setTeaCategory(tea.getTeaCategory());
        resp.setOriginRegion(tea.getOriginRegion());
        resp.setHarvestYear(tea.getHarvestYear());
        resp.setStorageMethod(tea.getStorageMethod());
        resp.setCurrentStock(tea.getCurrentStock());
        resp.setStockUnit(tea.getStockUnit());
        resp.setDescription(tea.getDescription());
        resp.setImageUrl(tea.getImageUrl());
        resp.setCreatedAt(tea.getCreatedAt());
        resp.setUpdatedAt(tea.getUpdatedAt());

        List<StorageRecord> records = storageRecordRepository.findByTeaIdOrderByRecordDateAsc(tea.getId());
        if (records != null && !records.isEmpty()) {
            StorageRecord lastRecord = records.get(records.size() - 1);
            resp.setLastStorageDate(lastRecord.getRecordDate());

            List<StockTrendPoint> trend = new ArrayList<>();
            int maxPoints = 10;
            int start = Math.max(0, records.size() - maxPoints);
            for (int i = start; i < records.size(); i++) {
                StorageRecord r = records.get(i);
                StockTrendPoint point = new StockTrendPoint();
                point.setRecordDate(r.getRecordDate());
                point.setStock(r.getCurrentStock());
                point.setChange(r.getStockChange());
                trend.add(point);
            }
            resp.setStockTrend(trend);
        }

        return resp;
    }
}
