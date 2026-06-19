package com.tea.tracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tea.tracker.dto.BrewingParamRequest;
import com.tea.tracker.dto.BrewingParamResponse;
import com.tea.tracker.dto.BrewingParamSyncRequest;
import com.tea.tracker.dto.FieldValidationError;
import com.tea.tracker.dto.ParamSyncRecordResponse;
import com.tea.tracker.entity.BrewingParam;
import com.tea.tracker.entity.ParamSyncRecord;
import com.tea.tracker.entity.Tea;
import com.tea.tracker.exception.BrewingParamValidationException;
import com.tea.tracker.repository.BrewingParamRepository;
import com.tea.tracker.repository.ParamSyncRecordRepository;
import com.tea.tracker.repository.TeaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
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
    private final ParamSyncRecordRepository paramSyncRecordRepository;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<Long, ReentrantLock> teaLocks = new ConcurrentHashMap<>();

    private static final Map<String, String> FIELD_LABEL_MAP = new LinkedHashMap<>();

    static {
        FIELD_LABEL_MAP.put("paramName", "参数名称");
        FIELD_LABEL_MAP.put("brewingMethod", "冲泡方式");
        FIELD_LABEL_MAP.put("waterTemperature", "水温");
        FIELD_LABEL_MAP.put("teaAmount", "投茶量");
        FIELD_LABEL_MAP.put("teaRatio", "茶水比");
        FIELD_LABEL_MAP.put("waterAmount", "注水量");
        FIELD_LABEL_MAP.put("firstInfusionTime", "首泡时长");
        FIELD_LABEL_MAP.put("secondInfusionTime", "二泡时长");
        FIELD_LABEL_MAP.put("thirdInfusionTime", "三泡时长");
        FIELD_LABEL_MAP.put("subsequentInfusionTime", "后续泡时长");
        FIELD_LABEL_MAP.put("totalInfusions", "总泡数");
        FIELD_LABEL_MAP.put("waterQuality", "水质");
        FIELD_LABEL_MAP.put("notes", "备注");
    }

    private ReentrantLock getTeaLock(Long teaId) {
        return teaLocks.computeIfAbsent(teaId, k -> new ReentrantLock());
    }

    private void validateBrewingParams(String teaCategory, BrewingParamRequest request) {
        Map<String, Object> ranges = teaTemplateCacheService.getParamRanges(teaCategory);
        List<FieldValidationError> errors = new ArrayList<>();

        if (request.getWaterTemperature() != null) {
            int min = toInt(ranges.get("waterTemperatureMin"));
            int max = toInt(ranges.get("waterTemperatureMax"));
            if (request.getWaterTemperature() < min || request.getWaterTemperature() > max) {
                errors.add(new FieldValidationError(
                        "waterTemperature", request.getWaterTemperature(), min, max,
                        String.format("水温应在 %d~%d°C 之间，当前值: %d°C", min, max, request.getWaterTemperature())
                ));
            }
        }

        if (request.getTeaAmount() != null) {
            double min = toDouble(ranges.get("teaAmountMin"));
            double max = toDouble(ranges.get("teaAmountMax"));
            double val = request.getTeaAmount().doubleValue();
            if (val < min || val > max) {
                errors.add(new FieldValidationError(
                        "teaAmount", request.getTeaAmount(), min, max,
                        String.format("投茶量应在 %.1f~%.1fg 之间，当前值: %.1fg", min, max, val)
                ));
            }
        }

        if (request.getWaterAmount() != null) {
            double min = toDouble(ranges.get("waterAmountMin"));
            double max = toDouble(ranges.get("waterAmountMax"));
            double val = request.getWaterAmount().doubleValue();
            if (val < min || val > max) {
                errors.add(new FieldValidationError(
                        "waterAmount", request.getWaterAmount(), min, max,
                        String.format("注水量应在 %.0f~%.0fml 之间，当前值: %.0fml", min, max, val)
                ));
            }
        }

        validateInfusionTime(request.getFirstInfusionTime(), "firstInfusionTime", "首泡时长", ranges, errors);
        validateInfusionTime(request.getSecondInfusionTime(), "secondInfusionTime", "二泡时长", ranges, errors);
        validateInfusionTime(request.getThirdInfusionTime(), "thirdInfusionTime", "三泡时长", ranges, errors);
        validateInfusionTime(request.getSubsequentInfusionTime(), "subsequentInfusionTime", "后续泡次时长", ranges, errors);

        if (!errors.isEmpty()) {
            throw new BrewingParamValidationException(
                    String.format("冲泡参数校验失败（茶类: %s），共 %d 项异常", teaCategory, errors.size()),
                    errors
            );
        }
    }

    private void validateInfusionTime(Integer value, String fieldKey, String displayName,
                                      Map<String, Object> ranges, List<FieldValidationError> errors) {
        if (value == null) return;
        int min = toInt(ranges.get(fieldKey + "Min"));
        int max = toInt(ranges.get(fieldKey + "Max"));
        if (value < min || value > max) {
            errors.add(new FieldValidationError(
                    fieldKey, value, min, max,
                    String.format("%s应在 %d~%d秒 之间，当前值: %d秒", displayName, min, max, value)
            ));
        }
    }

    @Transactional
    public BrewingParamResponse createBrewingParam(Long teaId, BrewingParamRequest request) {
        ReentrantLock lock = getTeaLock(teaId);
        lock.lock();
        try {
            Tea tea = teaRepository.findById(teaId)
                    .orElseThrow(() -> new EntityNotFoundException("茶叶档案不存在: " + teaId));

            validateBrewingParams(tea.getTeaCategory(), request);

            if (Boolean.TRUE.equals(request.getIsDefault())) {
                if (request.getBrewingMethod() != null && !request.getBrewingMethod().isEmpty()) {
                    brewingParamRepository.clearDefaultByTeaIdAndBrewingMethod(teaId, request.getBrewingMethod());
                } else {
                    brewingParamRepository.clearDefaultByTeaId(teaId);
                }
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

            validateBrewingParams(param.getTea().getTeaCategory(), request);

            String oldBrewingMethod = param.getBrewingMethod();
            String newBrewingMethod = request.getBrewingMethod();
            boolean methodChanged = (oldBrewingMethod == null && newBrewingMethod != null)
                    || (oldBrewingMethod != null && !oldBrewingMethod.equals(newBrewingMethod));

            boolean becomingDefault = Boolean.TRUE.equals(request.getIsDefault())
                    && !Boolean.TRUE.equals(param.getIsDefault());

            if (becomingDefault || (methodChanged && Boolean.TRUE.equals(request.getIsDefault()))) {
                if (newBrewingMethod != null && !newBrewingMethod.isEmpty()) {
                    brewingParamRepository.clearDefaultByTeaIdAndBrewingMethod(teaId, newBrewingMethod);
                } else {
                    brewingParamRepository.clearDefaultByTeaId(teaId);
                }
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

    @Transactional
    public List<BrewingParamResponse> getBrewingParamsByTeaId(Long teaId) {
        List<BrewingParam> params = brewingParamRepository.findByTeaIdOrderByCreatedAtDesc(teaId);

        Map<String, List<BrewingParam>> paramsByMethod = params.stream()
                .collect(Collectors.groupingBy(p -> p.getBrewingMethod() != null ? p.getBrewingMethod() : ""));

        List<BrewingParam> paramsToSave = new ArrayList<>();

        for (Map.Entry<String, List<BrewingParam>> entry : paramsByMethod.entrySet()) {
            List<BrewingParam> methodParams = entry.getValue();
            long defaultCount = methodParams.stream().filter(p -> Boolean.TRUE.equals(p.getIsDefault())).count();
            if (defaultCount > 1) {
                String methodLabel = entry.getKey().isEmpty() ? "(未指定)" : entry.getKey();
                log.warn("Tea {} has {} default brewing params for method '{}', fixing now",
                        teaId, defaultCount, methodLabel);
                BrewingParam firstDefault = methodParams.stream()
                        .filter(p -> Boolean.TRUE.equals(p.getIsDefault()))
                        .findFirst()
                        .orElse(null);
                if (firstDefault != null) {
                    for (BrewingParam p : methodParams) {
                        boolean newIsDefault = p.getId().equals(firstDefault.getId());
                        if (Boolean.TRUE.equals(p.getIsDefault()) != newIsDefault) {
                            p.setIsDefault(newIsDefault);
                            paramsToSave.add(p);
                        }
                    }
                }
            }
        }

        if (!paramsToSave.isEmpty()) {
            brewingParamRepository.saveAll(paramsToSave);
            log.info("Fixed {} brewing params with incorrect default status for tea {}", paramsToSave.size(), teaId);
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
        param.setBrewingMethod(request.getBrewingMethod());
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

        String teaCategory = param.getTea().getTeaCategory();
        if (teaCategory != null) {
            Map<String, Object> template = teaTemplateCacheService.getBrewingTemplate(teaCategory);
            resp.setDeviations(calculateDeviations(param, template));

            TeaTemplateCacheService.CacheHitStatus hitStatus = teaTemplateCacheService.getCacheHitStatus(teaCategory);
            if (hitStatus != null) {
                BrewingParamResponse.TemplateHitStatus status = new BrewingParamResponse.TemplateHitStatus();
                status.setHit(hitStatus.isCacheHit());
                status.setCachedVersion(hitStatus.getCachedVersion());
                status.setCurrentVersion(hitStatus.getCurrentVersion());
                status.setVersionMatch(hitStatus.isVersionMatch());
                resp.setTemplateHitStatus(status);
            }
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

    private int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public List<ParamSyncRecordResponse.FieldDifference> previewSync(Long sourceTeaId, Long sourceParamId, Long targetTeaId) {
        BrewingParam sourceParam = brewingParamRepository.findById(sourceParamId)
                .orElseThrow(() -> new EntityNotFoundException("来源冲泡参数不存在: " + sourceParamId));

        Tea sourceTea = sourceParam.getTea();
        if (!sourceTea.getId().equals(sourceTeaId)) {
            throw new IllegalArgumentException("来源冲泡参数不属于指定的来源茶叶");
        }

        Tea targetTea = teaRepository.findById(targetTeaId)
                .orElseThrow(() -> new EntityNotFoundException("目标茶叶不存在: " + targetTeaId));

        if (!sourceTea.getTeaCategory().equals(targetTea.getTeaCategory())) {
            throw new IllegalArgumentException("只能同步同茶类的冲泡参数（来源：" 
                    + sourceTea.getTeaCategory() + "，目标：" + targetTea.getTeaCategory() + "）");
        }

        List<BrewingParam> targetParams = brewingParamRepository.findByTeaIdOrderByCreatedAtDesc(targetTeaId);
        BrewingParam targetDefault = targetParams.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsDefault()))
                .findFirst()
                .orElse(null);

        return calculateFieldDifferences(sourceParam, targetDefault);
    }

    @Transactional
    public ParamSyncRecordResponse syncBrewingParam(Long sourceTeaId, BrewingParamSyncRequest request) {
        BrewingParam sourceParam = brewingParamRepository.findById(request.getSourceParamId())
                .orElseThrow(() -> new EntityNotFoundException("来源冲泡参数不存在: " + request.getSourceParamId()));

        Tea sourceTea = sourceParam.getTea();
        if (!sourceTea.getId().equals(sourceTeaId)) {
            throw new IllegalArgumentException("来源冲泡参数不属于指定的来源茶叶");
        }

        Tea targetTea = teaRepository.findById(request.getTargetTeaId())
                .orElseThrow(() -> new EntityNotFoundException("目标茶叶不存在: " + request.getTargetTeaId()));

        if (!sourceTea.getTeaCategory().equals(targetTea.getTeaCategory())) {
            throw new IllegalArgumentException("只能同步同茶类的冲泡参数");
        }

        ReentrantLock lock = getTeaLock(request.getTargetTeaId());
        lock.lock();
        try {
            List<BrewingParam> targetParams = brewingParamRepository.findByTeaIdOrderByCreatedAtDesc(request.getTargetTeaId());
            BrewingParam targetDefault = targetParams.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getIsDefault()))
                    .findFirst()
                    .orElse(null);

            List<ParamSyncRecordResponse.FieldDifference> differences = 
                    calculateFieldDifferences(sourceParam, targetDefault);

            BrewingParam newParam = new BrewingParam();
            newParam.setTea(targetTea);
            copyParamFields(sourceParam, newParam, request.getFieldsToSync());

            String paramName = request.getTargetParamName();
            if (paramName == null || paramName.isBlank()) {
                paramName = sourceParam.getParamName() != null ? sourceParam.getParamName() + " (同步)" : "同步参数";
            }
            newParam.setParamName(paramName);

            if (Boolean.TRUE.equals(request.getSetAsDefault())) {
                brewingParamRepository.clearDefaultByTeaId(request.getTargetTeaId());
                newParam.setIsDefault(true);
            } else {
                newParam.setIsDefault(false);
            }

            validateBrewingParams(targetTea.getTeaCategory(), convertToRequest(newParam));

            BrewingParam saved = brewingParamRepository.save(newParam);
            log.info("Synced brewing param from tea {} (paramId={}) to tea {} (newParamId={})",
                    sourceTea.getName(), sourceParam.getId(), targetTea.getName(), saved.getId());

            ParamSyncRecord syncRecord = new ParamSyncRecord();
            syncRecord.setSourceTeaId(sourceTea.getId());
            syncRecord.setSourceTeaName(sourceTea.getName());
            syncRecord.setSourceParamId(sourceParam.getId());
            syncRecord.setSourceParamName(sourceParam.getParamName());
            syncRecord.setTargetTeaId(targetTea.getId());
            syncRecord.setTargetTeaName(targetTea.getName());
            syncRecord.setTargetParamId(saved.getId());
            syncRecord.setTargetParamName(saved.getParamName());
            syncRecord.setTeaCategory(sourceTea.getTeaCategory());
            syncRecord.setSyncType("DEFAULT");
            syncRecord.setSyncedAt(java.time.LocalDateTime.now());
            syncRecord.setFieldDifferences(serializeFieldDifferences(differences));

            paramSyncRecordRepository.save(syncRecord);

            return toSyncRecordResponse(syncRecord);
        } finally {
            lock.unlock();
        }
    }

    @Transactional(readOnly = true)
    public List<ParamSyncRecordResponse> getSyncRecordsByTargetTeaId(Long targetTeaId) {
        return paramSyncRecordRepository.findByTargetTeaIdOrderBySyncedAtDesc(targetTeaId)
                .stream()
                .map(this::toSyncRecordResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ParamSyncRecordResponse> getSyncRecordsBySourceTeaId(Long sourceTeaId) {
        return paramSyncRecordRepository.findBySourceTeaIdOrderBySyncedAtDesc(sourceTeaId)
                .stream()
                .map(this::toSyncRecordResponse)
                .collect(Collectors.toList());
    }

    private List<ParamSyncRecordResponse.FieldDifference> calculateFieldDifferences(
            BrewingParam source, BrewingParam target) {
        List<ParamSyncRecordResponse.FieldDifference> differences = new ArrayList<>();

        for (Map.Entry<String, String> entry : FIELD_LABEL_MAP.entrySet()) {
            String fieldName = entry.getKey();
            String fieldLabel = entry.getValue();

            Object sourceValue = getFieldValue(source, fieldName);
            Object targetValue = target != null ? getFieldValue(target, fieldName) : null;

            boolean differs = (sourceValue == null && targetValue != null)
                    || (sourceValue != null && !sourceValue.equals(targetValue));

            if (differs) {
                ParamSyncRecordResponse.FieldDifference diff = new ParamSyncRecordResponse.FieldDifference();
                diff.setFieldName(fieldName);
                diff.setFieldLabel(fieldLabel);
                diff.setSourceValue(sourceValue);
                diff.setTargetValue(targetValue);
                differences.add(diff);
            }
        }

        return differences;
    }

    private Object getFieldValue(BrewingParam param, String fieldName) {
        return switch (fieldName) {
            case "paramName" -> param.getParamName();
            case "brewingMethod" -> param.getBrewingMethod();
            case "waterTemperature" -> param.getWaterTemperature();
            case "teaAmount" -> param.getTeaAmount();
            case "teaRatio" -> param.getTeaRatio();
            case "waterAmount" -> param.getWaterAmount();
            case "firstInfusionTime" -> param.getFirstInfusionTime();
            case "secondInfusionTime" -> param.getSecondInfusionTime();
            case "thirdInfusionTime" -> param.getThirdInfusionTime();
            case "subsequentInfusionTime" -> param.getSubsequentInfusionTime();
            case "totalInfusions" -> param.getTotalInfusions();
            case "waterQuality" -> param.getWaterQuality();
            case "notes" -> param.getNotes();
            default -> null;
        };
    }

    private void copyParamFields(BrewingParam source, BrewingParam target, List<String> fieldsToSync) {
        if (fieldsToSync == null || fieldsToSync.isEmpty()) {
            target.setBrewingMethod(source.getBrewingMethod());
            target.setWaterTemperature(source.getWaterTemperature());
            target.setTeaAmount(source.getTeaAmount());
            target.setTeaRatio(source.getTeaRatio());
            target.setWaterAmount(source.getWaterAmount());
            target.setFirstInfusionTime(source.getFirstInfusionTime());
            target.setSecondInfusionTime(source.getSecondInfusionTime());
            target.setThirdInfusionTime(source.getThirdInfusionTime());
            target.setSubsequentInfusionTime(source.getSubsequentInfusionTime());
            target.setTotalInfusions(source.getTotalInfusions());
            target.setWaterQuality(source.getWaterQuality());
            target.setNotes(source.getNotes());
        } else {
            for (String field : fieldsToSync) {
                switch (field) {
                    case "brewingMethod" -> target.setBrewingMethod(source.getBrewingMethod());
                    case "waterTemperature" -> target.setWaterTemperature(source.getWaterTemperature());
                    case "teaAmount" -> target.setTeaAmount(source.getTeaAmount());
                    case "teaRatio" -> target.setTeaRatio(source.getTeaRatio());
                    case "waterAmount" -> target.setWaterAmount(source.getWaterAmount());
                    case "firstInfusionTime" -> target.setFirstInfusionTime(source.getFirstInfusionTime());
                    case "secondInfusionTime" -> target.setSecondInfusionTime(source.getSecondInfusionTime());
                    case "thirdInfusionTime" -> target.setThirdInfusionTime(source.getThirdInfusionTime());
                    case "subsequentInfusionTime" -> target.setSubsequentInfusionTime(source.getSubsequentInfusionTime());
                    case "totalInfusions" -> target.setTotalInfusions(source.getTotalInfusions());
                    case "waterQuality" -> target.setWaterQuality(source.getWaterQuality());
                    case "notes" -> target.setNotes(source.getNotes());
                }
            }
        }
    }

    private BrewingParamRequest convertToRequest(BrewingParam param) {
        BrewingParamRequest req = new BrewingParamRequest();
        req.setParamName(param.getParamName());
        req.setBrewingMethod(param.getBrewingMethod());
        req.setWaterTemperature(param.getWaterTemperature());
        req.setTeaAmount(param.getTeaAmount());
        req.setTeaRatio(param.getTeaRatio());
        req.setWaterAmount(param.getWaterAmount());
        req.setFirstInfusionTime(param.getFirstInfusionTime());
        req.setSecondInfusionTime(param.getSecondInfusionTime());
        req.setThirdInfusionTime(param.getThirdInfusionTime());
        req.setSubsequentInfusionTime(param.getSubsequentInfusionTime());
        req.setTotalInfusions(param.getTotalInfusions());
        req.setWaterQuality(param.getWaterQuality());
        req.setNotes(param.getNotes());
        req.setIsDefault(param.getIsDefault());
        return req;
    }

    private String serializeFieldDifferences(List<ParamSyncRecordResponse.FieldDifference> differences) {
        try {
            return objectMapper.writeValueAsString(differences);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize field differences", e);
            return "[]";
        }
    }

    private List<ParamSyncRecordResponse.FieldDifference> deserializeFieldDifferences(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<ParamSyncRecordResponse.FieldDifference>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize field differences", e);
            return new ArrayList<>();
        }
    }

    private ParamSyncRecordResponse toSyncRecordResponse(ParamSyncRecord record) {
        ParamSyncRecordResponse resp = new ParamSyncRecordResponse();
        resp.setId(record.getId());
        resp.setSourceTeaId(record.getSourceTeaId());
        resp.setSourceTeaName(record.getSourceTeaName());
        resp.setSourceParamId(record.getSourceParamId());
        resp.setSourceParamName(record.getSourceParamName());
        resp.setTargetTeaId(record.getTargetTeaId());
        resp.setTargetTeaName(record.getTargetTeaName());
        resp.setTargetParamId(record.getTargetParamId());
        resp.setTargetParamName(record.getTargetParamName());
        resp.setTeaCategory(record.getTeaCategory());
        resp.setSyncType(record.getSyncType());
        resp.setSyncedAt(record.getSyncedAt());
        resp.setCreatedAt(record.getCreatedAt());
        resp.setFieldDifferences(deserializeFieldDifferences(record.getFieldDifferences()));
        return resp;
    }
}
