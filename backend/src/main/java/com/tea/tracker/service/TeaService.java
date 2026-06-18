package com.tea.tracker.service;

import com.tea.tracker.dto.BrewingParamResponse;
import com.tea.tracker.dto.StockTrendPoint;
import com.tea.tracker.dto.TeaComparisonResponse;
import com.tea.tracker.dto.TeaRequest;
import com.tea.tracker.dto.TeaResponse;
import com.tea.tracker.dto.TastingScoreSummary;
import com.tea.tracker.entity.BrewingParam;
import com.tea.tracker.entity.StorageRecord;
import com.tea.tracker.entity.Tea;
import com.tea.tracker.repository.BrewingParamRepository;
import com.tea.tracker.repository.StorageRecordRepository;
import com.tea.tracker.repository.TeaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeaService {

    private static final Set<String> AGING_TEA_CATEGORIES = Set.of("普洱茶", "白茶", "黑茶");
    private static final int MIN_AGING_HARVEST_YEAR = 1900;
    private static final int MIN_REGULAR_HARVEST_YEAR = 1950;

    private final TeaRepository teaRepository;
    private final StorageRecordRepository storageRecordRepository;
    private final BrewingParamRepository brewingParamRepository;
    private final TastingScoreService tastingScoreService;

    @Transactional
    public TeaResponse createTea(TeaRequest request) {
        validateHarvestYear(request.getTeaCategory(), request.getHarvestYear());
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
        validateHarvestYear(request.getTeaCategory(), request.getHarvestYear());
        mapRequestToEntity(request, tea);
        Tea updated = teaRepository.save(tea);
        log.info("Updated tea: {} (id={})", updated.getName(), updated.getId());
        return toResponse(updated);
    }

    private void validateHarvestYear(String teaCategory, Integer harvestYear) {
        if (harvestYear == null) {
            return;
        }
        int currentYear = LocalDate.now().getYear();
        if (harvestYear > currentYear) {
            throw new IllegalArgumentException("采摘年份不能晚于当前年份: " + currentYear + "年");
        }
        int minYear = AGING_TEA_CATEGORIES.contains(teaCategory) ? MIN_AGING_HARVEST_YEAR : MIN_REGULAR_HARVEST_YEAR;
        if (harvestYear < minYear) {
            throw new IllegalArgumentException("采摘年份不能早于" + minYear + "年");
        }
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

    @Transactional(readOnly = true)
    public List<TeaComparisonResponse> getTeaComparison(List<Long> teaIds) {
        return teaIds.stream()
                .map(id -> teaRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .map(this::toComparisonResponse)
                .collect(Collectors.toList());
    }

    private TeaComparisonResponse toComparisonResponse(Tea tea) {
        TeaComparisonResponse resp = new TeaComparisonResponse();
        resp.setId(tea.getId());
        resp.setName(tea.getName());
        resp.setTeaCategory(tea.getTeaCategory());
        resp.setOriginRegion(tea.getOriginRegion());
        resp.setHarvestYear(tea.getHarvestYear());
        resp.setYearNote(tea.getYearNote());
        resp.setStorageMethod(tea.getStorageMethod());
        resp.setCurrentStock(tea.getCurrentStock());
        resp.setStockUnit(tea.getStockUnit());
        resp.setDescription(tea.getDescription());
        resp.setImageUrl(tea.getImageUrl());

        brewingParamRepository.findFirstByTeaIdAndIsDefaultTrueOrderByCreatedAtAsc(tea.getId())
                .ifPresent(param -> resp.setDefaultBrewingParam(toBrewingParamResponse(param)));

        storageRecordRepository.findTopByTeaIdOrderByRecordDateDesc(tea.getId())
                .ifPresent(record -> resp.setLatestStorageRecord(toStorageRecordResponse(record)));

        TastingScoreSummary scoreSummary = tastingScoreService.getScoreSummaryByTeaId(tea.getId());
        resp.setAvgTastingScores(scoreSummary);

        return resp;
    }

    private BrewingParamResponse toBrewingParamResponse(BrewingParam param) {
        BrewingParamResponse resp = new BrewingParamResponse();
        resp.setId(param.getId());
        resp.setTeaId(param.getTea().getId());
        resp.setTeaName(param.getTea().getName());
        resp.setParamName(param.getParamName());
        resp.setBrewingMethod(param.getBrewingMethod());
        resp.setWaterTemperature(param.getWaterTemperature());
        resp.setTeaAmount(param.getTeaAmount());
        resp.setTeaRatio(param.getTeaRatio());
        resp.setWaterAmount(param.getWaterAmount());
        resp.setFirstInfusionTime(param.getFirstInfusionTime());
        resp.setSecondInfusionTime(param.getSecondInfusionTime());
        resp.setThirdInfusionTime(param.getThirdInfusionTime());
        resp.setSubsequentInfusionTime(param.getSubsequentInfusionTime());
        resp.setTotalInfusions(param.getTotalInfusions());
        resp.setWaterQuality(param.getWaterQuality());
        resp.setNotes(param.getNotes());
        resp.setIsDefault(param.getIsDefault());
        resp.setCreatedAt(param.getCreatedAt());
        resp.setUpdatedAt(param.getUpdatedAt());
        return resp;
    }

    private com.tea.tracker.dto.StorageRecordResponse toStorageRecordResponse(StorageRecord record) {
        com.tea.tracker.dto.StorageRecordResponse resp = new com.tea.tracker.dto.StorageRecordResponse();
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

    private void mapRequestToEntity(TeaRequest request, Tea tea) {
        tea.setName(request.getName());
        tea.setTeaCategory(request.getTeaCategory());
        tea.setOriginRegion(request.getOriginRegion());
        tea.setHarvestYear(request.getHarvestYear());
        if (AGING_TEA_CATEGORIES.contains(request.getTeaCategory())) {
            tea.setYearNote(request.getYearNote());
        } else {
            tea.setYearNote(null);
        }
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
        resp.setYearNote(tea.getYearNote());
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
