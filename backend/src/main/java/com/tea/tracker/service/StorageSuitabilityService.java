package com.tea.tracker.service;

import com.tea.tracker.dto.StorageSuitabilityRequest;
import com.tea.tracker.dto.StorageSuitabilityResponse;
import com.tea.tracker.entity.StorageSuitability;
import com.tea.tracker.entity.Tea;
import com.tea.tracker.repository.StorageSuitabilityRepository;
import com.tea.tracker.repository.TeaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageSuitabilityService {

    private final StorageSuitabilityRepository storageSuitabilityRepository;
    private final TeaRepository teaRepository;

    private static final Map<String, int[]> TEA_TEMP_RANGE = new HashMap<>();
    private static final Map<String, int[]> TEA_HUMIDITY_RANGE = new HashMap<>();
    private static final Map<String, List<String>> TEA_RECOMMENDED_STORAGE = new HashMap<>();
    private static final Map<String, Integer> SEAL_SCORE_MAP = new HashMap<>();

    static {
        TEA_TEMP_RANGE.put("绿茶", new int[]{0, 5});
        TEA_TEMP_RANGE.put("红茶", new int[]{20, 25});
        TEA_TEMP_RANGE.put("乌龙茶", new int[]{15, 25});
        TEA_TEMP_RANGE.put("普洱茶", new int[]{20, 30});
        TEA_TEMP_RANGE.put("白茶", new int[]{10, 25});
        TEA_TEMP_RANGE.put("黑茶", new int[]{20, 30});
        TEA_TEMP_RANGE.put("黄茶", new int[]{0, 10});
        TEA_TEMP_RANGE.put("花茶", new int[]{15, 25});
        TEA_TEMP_RANGE.put("默认", new int[]{15, 25});

        TEA_HUMIDITY_RANGE.put("绿茶", new int[]{50, 60});
        TEA_HUMIDITY_RANGE.put("红茶", new int[]{50, 65});
        TEA_HUMIDITY_RANGE.put("乌龙茶", new int[]{50, 65});
        TEA_HUMIDITY_RANGE.put("普洱茶", new int[]{60, 80});
        TEA_HUMIDITY_RANGE.put("白茶", new int[]{40, 60});
        TEA_HUMIDITY_RANGE.put("黑茶", new int[]{60, 80});
        TEA_HUMIDITY_RANGE.put("黄茶", new int[]{50, 60});
        TEA_HUMIDITY_RANGE.put("花茶", new int[]{50, 65});
        TEA_HUMIDITY_RANGE.put("默认", new int[]{50, 70});

        TEA_RECOMMENDED_STORAGE.put("绿茶", Arrays.asList("冷藏保存", "锡罐储存", "真空包装"));
        TEA_RECOMMENDED_STORAGE.put("红茶", Arrays.asList("常温密封", "锡罐储存", "陶罐储存"));
        TEA_RECOMMENDED_STORAGE.put("乌龙茶", Arrays.asList("常温密封", "真空包装", "锡罐储存"));
        TEA_RECOMMENDED_STORAGE.put("普洱茶", Arrays.asList("陶罐储存", "干燥避光", "常温密封"));
        TEA_RECOMMENDED_STORAGE.put("白茶", Arrays.asList("陶罐储存", "干燥避光", "常温密封"));
        TEA_RECOMMENDED_STORAGE.put("黑茶", Arrays.asList("陶罐储存", "干燥避光", "常温密封"));
        TEA_RECOMMENDED_STORAGE.put("黄茶", Arrays.asList("冷藏保存", "锡罐储存", "真空包装"));
        TEA_RECOMMENDED_STORAGE.put("花茶", Arrays.asList("常温密封", "锡罐储存", "干燥避光"));
        TEA_RECOMMENDED_STORAGE.put("默认", Arrays.asList("常温密封", "干燥避光"));

        SEAL_SCORE_MAP.put("完全密封", 20);
        SEAL_SCORE_MAP.put("重新密封", 18);
        SEAL_SCORE_MAP.put("半密封", 12);
        SEAL_SCORE_MAP.put("已开封", 5);
        SEAL_SCORE_MAP.put("密封不良", 3);
    }

    @Transactional
    public StorageSuitabilityResponse evaluateSuitability(Long teaId, StorageSuitabilityRequest request) {
        Tea tea = teaRepository.findById(teaId)
                .orElseThrow(() -> new EntityNotFoundException("茶叶档案不存在: " + teaId));

        String teaCategory = tea.getTeaCategory();
        BigDecimal temperature = request.getTemperature();
        BigDecimal humidity = request.getHumidity();
        String sealCondition = request.getSealCondition();
        String storageMethod = request.getStorageMethod();

        int tempScore = calculateTemperatureScore(teaCategory, temperature);
        int humidityScore = calculateHumidityScore(teaCategory, humidity);
        int sealScore = calculateSealScore(sealCondition);
        int storageMethodScore = calculateStorageMethodScore(teaCategory, storageMethod);

        int totalScore = tempScore + humidityScore + sealScore + storageMethodScore;
        String suitabilityLevel = determineSuitabilityLevel(totalScore);
        String suggestions = generateSuggestions(teaCategory, temperature, humidity, sealCondition,
                storageMethod, tempScore, humidityScore, sealScore, storageMethodScore);

        StorageSuitability record = new StorageSuitability();
        record.setTea(tea);
        record.setTeaCategory(teaCategory);
        record.setStorageMethod(storageMethod);
        record.setTemperature(temperature);
        record.setHumidity(humidity);
        record.setSealCondition(sealCondition);
        record.setTemperatureScore(tempScore);
        record.setHumidityScore(humidityScore);
        record.setSealScore(sealScore);
        record.setStorageMethodScore(storageMethodScore);
        record.setTotalScore(totalScore);
        record.setSuitabilityLevel(suitabilityLevel);
        record.setSuggestions(suggestions);
        record.setRecordDate(LocalDateTime.now());

        StorageSuitability saved = storageSuitabilityRepository.save(record);
        log.info("Created storage suitability record for tea {} (id={}), score={}, level={}",
                tea.getName(), saved.getId(), totalScore, suitabilityLevel);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StorageSuitabilityResponse> getSuitabilityRecords(Long teaId) {
        return storageSuitabilityRepository.findByTeaIdOrderByRecordDateDesc(teaId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StorageSuitabilityResponse getLatestSuitability(Long teaId) {
        List<StorageSuitability> records = storageSuitabilityRepository.findByTeaIdOrderByRecordDateDesc(teaId);
        if (records.isEmpty()) {
            return null;
        }
        return toResponse(records.get(0));
    }

    @Transactional
    public void deleteSuitability(Long teaId, Long id) {
        StorageSuitability record = storageSuitabilityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("适宜度记录不存在: " + id));

        if (!record.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("适宜度记录不属于当前茶叶");
        }

        storageSuitabilityRepository.delete(record);
        log.info("Deleted storage suitability record id={} for tea {}", id, record.getTea().getName());
    }

    private int calculateTemperatureScore(String category, BigDecimal temperature) {
        if (temperature == null) {
            return 0;
        }
        int[] range = TEA_TEMP_RANGE.getOrDefault(category, TEA_TEMP_RANGE.get("默认"));
        double temp = temperature.doubleValue();
        int min = range[0];
        int max = range[1];

        if (temp >= min && temp <= max) {
            return 30;
        }

        double deviation = temp < min ? min - temp : temp - max;
        if (deviation <= 3) {
            return 20;
        } else if (deviation <= 5) {
            return 10;
        } else {
            return 0;
        }
    }

    private int calculateHumidityScore(String category, BigDecimal humidity) {
        if (humidity == null) {
            return 0;
        }
        int[] range = TEA_HUMIDITY_RANGE.getOrDefault(category, TEA_HUMIDITY_RANGE.get("默认"));
        double hum = humidity.doubleValue();
        int min = range[0];
        int max = range[1];

        if (hum >= min && hum <= max) {
            return 30;
        }

        double deviation = hum < min ? min - hum : hum - max;
        if (deviation <= 5) {
            return 20;
        } else if (deviation <= 10) {
            return 10;
        } else {
            return 0;
        }
    }

    private int calculateSealScore(String sealCondition) {
        if (sealCondition == null || sealCondition.isEmpty()) {
            return 0;
        }
        return SEAL_SCORE_MAP.getOrDefault(sealCondition, 5);
    }

    private int calculateStorageMethodScore(String category, String storageMethod) {
        if (storageMethod == null || storageMethod.isEmpty()) {
            return 0;
        }
        List<String> recommended = TEA_RECOMMENDED_STORAGE.getOrDefault(category, TEA_RECOMMENDED_STORAGE.get("默认"));
        if (recommended.isEmpty()) {
            return 10;
        }
        if (recommended.get(0).equals(storageMethod)) {
            return 20;
        } else if (recommended.contains(storageMethod)) {
            return 15;
        } else {
            return 8;
        }
    }

    private String determineSuitabilityLevel(int totalScore) {
        if (totalScore >= 90) {
            return "优秀";
        } else if (totalScore >= 75) {
            return "良好";
        } else if (totalScore >= 60) {
            return "一般";
        } else if (totalScore >= 40) {
            return "较差";
        } else {
            return "不适宜";
        }
    }

    private String generateSuggestions(String category, BigDecimal temperature, BigDecimal humidity,
                                       String sealCondition, String storageMethod,
                                       int tempScore, int humidityScore, int sealScore, int storageMethodScore) {
        List<String> suggestions = new ArrayList<>();
        int[] tempRange = TEA_TEMP_RANGE.getOrDefault(category, TEA_TEMP_RANGE.get("默认"));
        int[] humidityRange = TEA_HUMIDITY_RANGE.getOrDefault(category, TEA_HUMIDITY_RANGE.get("默认"));
        List<String> recommendedStorage = TEA_RECOMMENDED_STORAGE.getOrDefault(category, TEA_RECOMMENDED_STORAGE.get("默认"));

        if (tempScore < 30 && temperature != null) {
            double temp = temperature.doubleValue();
            if (temp < tempRange[0]) {
                suggestions.add(String.format("当前温度%.1f℃偏低，建议将温度提升至%d-%d℃", temp, tempRange[0], tempRange[1]));
            } else {
                suggestions.add(String.format("当前温度%.1f℃偏高，建议将温度降低至%d-%d℃", temp, tempRange[0], tempRange[1]));
            }
        }

        if (humidityScore < 30 && humidity != null) {
            double hum = humidity.doubleValue();
            if (hum < humidityRange[0]) {
                suggestions.add(String.format("当前湿度%.1f%%偏低，建议将湿度提升至%d-%d%%", hum, humidityRange[0], humidityRange[1]));
            } else {
                suggestions.add(String.format("当前湿度%.1f%%偏高，建议将湿度降低至%d-%d%%", hum, humidityRange[0], humidityRange[1]));
            }
        }

        if (sealScore < 20 && sealCondition != null && !sealCondition.isEmpty()) {
            suggestions.add("建议检查密封状况，保持完全密封状态以防止茶叶受潮和香气散失");
        }

        if (storageMethodScore < 20 && storageMethod != null && !storageMethod.isEmpty()) {
            if (!recommendedStorage.isEmpty()) {
                suggestions.add(String.format("对于%s，推荐使用%s储存方式", category, String.join("、", recommendedStorage)));
            }
        }

        if (suggestions.isEmpty()) {
            suggestions.add("当前仓储环境良好，请继续保持");
        }

        return String.join("；", suggestions);
    }

    private StorageSuitabilityResponse toResponse(StorageSuitability record) {
        StorageSuitabilityResponse resp = new StorageSuitabilityResponse();
        resp.setId(record.getId());
        resp.setTeaId(record.getTea().getId());
        resp.setTeaName(record.getTea().getName());
        resp.setTeaCategory(record.getTeaCategory());
        resp.setStorageMethod(record.getStorageMethod());
        resp.setTemperature(record.getTemperature());
        resp.setHumidity(record.getHumidity());
        resp.setSealCondition(record.getSealCondition());
        resp.setTemperatureScore(record.getTemperatureScore());
        resp.setHumidityScore(record.getHumidityScore());
        resp.setSealScore(record.getSealScore());
        resp.setStorageMethodScore(record.getStorageMethodScore());
        resp.setTotalScore(record.getTotalScore());
        resp.setSuitabilityLevel(record.getSuitabilityLevel());
        resp.setSuggestions(record.getSuggestions());
        resp.setRecordDate(record.getRecordDate());
        resp.setCreatedAt(record.getCreatedAt());
        resp.setUpdatedAt(record.getUpdatedAt());
        return resp;
    }
}
