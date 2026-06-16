package com.tea.tracker.service;

import com.tea.tracker.dto.BrewingParamRequest;
import com.tea.tracker.dto.BrewingParamResponse;
import com.tea.tracker.entity.BrewingParam;
import com.tea.tracker.entity.Tea;
import com.tea.tracker.repository.BrewingParamRepository;
import com.tea.tracker.repository.TeaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrewingParamService {

    private final BrewingParamRepository brewingParamRepository;
    private final TeaRepository teaRepository;
    private final TeaTemplateCacheService teaTemplateCacheService;

    private final ConcurrentHashMap<Long, ReentrantLock> teaLocks = new ConcurrentHashMap<>();

    private ReentrantLock getTeaLock(Long teaId) {
        return teaLocks.computeIfAbsent(teaId, k -> new ReentrantLock());
    }

    @Transactional
    public BrewingParamResponse createBrewingParam(Long teaId, BrewingParamRequest request) {
        ReentrantLock lock = getTeaLock(teaId);
        lock.lock();
        try {
            Tea tea = teaRepository.findById(teaId)
                    .orElseThrow(() -> new EntityNotFoundException("茶叶档案不存在: " + teaId));

            if (Boolean.TRUE.equals(request.getIsDefault())) {
                brewingParamRepository.clearDefaultByTeaId(teaId);
            }

            BrewingParam param = new BrewingParam();
            mapRequestToEntity(request, param);
            param.setTea(tea);
            BrewingParam saved = brewingParamRepository.save(param);
            log.info("Created brewing param for tea {} (id={})", tea.getName(), saved.getId());
            return toResponse(saved);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public BrewingParamResponse updateBrewingParam(Long teaId, Long id, BrewingParamRequest request) {
        ReentrantLock lock = getTeaLock(teaId);
        lock.lock();
        try {
            BrewingParam param = brewingParamRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("冲泡参数不存在: " + id));

            if (!param.getTea().getId().equals(teaId)) {
                throw new IllegalArgumentException("冲泡参数不属于当前茶叶");
            }

            if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(param.getIsDefault())) {
                brewingParamRepository.clearDefaultByTeaId(param.getTea().getId());
            }

            mapRequestToEntity(request, param);
            BrewingParam updated = brewingParamRepository.save(param);
            return toResponse(updated);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void deleteBrewingParam(Long teaId, Long id) {
        BrewingParam param = brewingParamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("冲泡参数不存在: " + id));

        if (!param.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("冲泡参数不属于当前茶叶");
        }

        brewingParamRepository.delete(param);
    }

    @Transactional(readOnly = true)
    public List<BrewingParamResponse> getBrewingParamsByTeaId(Long teaId) {
        List<BrewingParam> params = brewingParamRepository.findByTeaIdOrderByCreatedAtDesc(teaId);
        long defaultCount = params.stream().filter(p -> Boolean.TRUE.equals(p.getIsDefault())).count();
        if (defaultCount > 1) {
            log.warn("Tea {} has {} default brewing params, will fix on next write operation", teaId, defaultCount);
            BrewingParam firstDefault = params.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getIsDefault()))
                    .findFirst()
                    .orElse(null);
            if (firstDefault != null) {
                for (BrewingParam p : params) {
                    p.setIsDefault(p.getId().equals(firstDefault.getId()));
                }
            }
        }
        return params.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BrewingParamResponse getBrewingParamById(Long teaId, Long id) {
        BrewingParam param = brewingParamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("冲泡参数不存在: " + id));

        if (!param.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("冲泡参数不属于当前茶叶");
        }

        return toResponse(param);
    }

    private void mapRequestToEntity(BrewingParamRequest request, BrewingParam param) {
        param.setParamName(request.getParamName());
        param.setWaterTemperature(request.getWaterTemperature());
        param.setTeaAmount(request.getTeaAmount());
        param.setTeaRatio(request.getTeaRatio());
        param.setWaterAmount(request.getWaterAmount());
        param.setFirstInfusionTime(request.getFirstInfusionTime());
        param.setSecondInfusionTime(request.getSecondInfusionTime());
        param.setThirdInfusionTime(request.getThirdInfusionTime());
        param.setSubsequentInfusionTime(request.getSubsequentInfusionTime());
        param.setTotalInfusions(request.getTotalInfusions());
        param.setWaterQuality(request.getWaterQuality());
        param.setNotes(request.getNotes());
        param.setIsDefault(request.getIsDefault());
    }

    private BrewingParamResponse toResponse(BrewingParam param) {
        BrewingParamResponse resp = new BrewingParamResponse();
        resp.setId(param.getId());
        resp.setTeaId(param.getTea().getId());
        resp.setTeaName(param.getTea().getName());
        resp.setParamName(param.getParamName());
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

        String teaCategory = param.getTea().getTeaCategory();
        if (teaCategory != null) {
            Map<String, Object> template = teaTemplateCacheService.getBrewingTemplate(teaCategory);
            resp.setDeviations(calculateDeviations(param, template));
        }

        return resp;
    }

    private Map<String, BrewingParamResponse.ParamDeviation> calculateDeviations(BrewingParam param, Map<String, Object> template) {
        Map<String, BrewingParamResponse.ParamDeviation> deviations = new HashMap<>();

        deviations.put("waterTemperature", compareNumeric(
                param.getWaterTemperature(),
                template.get("waterTemperature"),
                5
        ));

        deviations.put("teaAmount", compareNumeric(
                param.getTeaAmount(),
                template.get("teaAmount"),
                0.5
        ));

        deviations.put("waterAmount", compareNumeric(
                param.getWaterAmount(),
                template.get("waterAmount"),
                20
        ));

        deviations.put("firstInfusionTime", compareNumeric(
                param.getFirstInfusionTime(),
                template.get("firstInfusionTime"),
                5
        ));

        deviations.put("secondInfusionTime", compareNumeric(
                param.getSecondInfusionTime(),
                template.get("secondInfusionTime"),
                5
        ));

        deviations.put("thirdInfusionTime", compareNumeric(
                param.getThirdInfusionTime(),
                template.get("thirdInfusionTime"),
                5
        ));

        deviations.put("subsequentInfusionTime", compareNumeric(
                param.getSubsequentInfusionTime(),
                template.get("subsequentInfusionTime"),
                5
        ));

        return deviations;
    }

    private BrewingParamResponse.ParamDeviation compareNumeric(Object actualValue, Object templateValueObj, double tolerance) {
        BrewingParamResponse.ParamDeviation deviation = new BrewingParamResponse.ParamDeviation();

        if (actualValue == null || templateValueObj == null) {
            deviation.setDeviates(false);
            deviation.setActualValue(actualValue);
            deviation.setTemplateValue(templateValueObj);
            return deviation;
        }

        double actual = toDouble(actualValue);
        double template = toDouble(templateValueObj);
        double diff = actual - template;

        deviation.setActualValue(actualValue);
        deviation.setTemplateValue(templateValueObj);

        if (Math.abs(diff) <= tolerance) {
            deviation.setDeviates(false);
        } else {
            deviation.setDeviates(true);
            deviation.setDirection(diff > 0 ? "higher" : "lower");
            if (template != 0) {
                deviation.setDeviationPercent(Math.round((diff / template) * 10000.0) / 100.0);
            }
        }

        return deviation;
    }

    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
