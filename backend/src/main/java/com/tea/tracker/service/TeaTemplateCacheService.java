package com.tea.tracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeaTemplateCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String TEMPLATE_KEY_PREFIX = "tea:template:";
    private static final String VERSION_KEY_PREFIX = "tea:template:version:";
    private static final long TEMPLATE_TTL_HOURS = 24;
    private static final long CURRENT_VERSION = 2L;

    private final ConcurrentHashMap<String, CacheHitStatus> lastHitStatus = new ConcurrentHashMap<>();

    private static final Map<String, Map<String, Object>> RANGE_CONFIGS = new LinkedHashMap<>();

    static {
        Map<String, Object> lvcha = new LinkedHashMap<>();
        lvcha.put("waterTemperatureMin", 60); lvcha.put("waterTemperatureMax", 85);
        lvcha.put("teaAmountMin", 2); lvcha.put("teaAmountMax", 5);
        lvcha.put("waterAmountMin", 100); lvcha.put("waterAmountMax", 200);
        lvcha.put("firstInfusionTimeMin", 5); lvcha.put("firstInfusionTimeMax", 30);
        lvcha.put("secondInfusionTimeMin", 5); lvcha.put("secondInfusionTimeMax", 35);
        lvcha.put("thirdInfusionTimeMin", 5); lvcha.put("thirdInfusionTimeMax", 40);
        lvcha.put("subsequentInfusionTimeMin", 5); lvcha.put("subsequentInfusionTimeMax", 45);
        RANGE_CONFIGS.put("绿茶", lvcha);

        Map<String, Object> hongcha = new LinkedHashMap<>();
        hongcha.put("waterTemperatureMin", 80); hongcha.put("waterTemperatureMax", 95);
        hongcha.put("teaAmountMin", 3); hongcha.put("teaAmountMax", 7);
        hongcha.put("waterAmountMin", 100); hongcha.put("waterAmountMax", 200);
        hongcha.put("firstInfusionTimeMin", 5); hongcha.put("firstInfusionTimeMax", 20);
        hongcha.put("secondInfusionTimeMin", 5); hongcha.put("secondInfusionTimeMax", 25);
        hongcha.put("thirdInfusionTimeMin", 5); hongcha.put("thirdInfusionTimeMax", 30);
        hongcha.put("subsequentInfusionTimeMin", 5); hongcha.put("subsequentInfusionTimeMax", 35);
        RANGE_CONFIGS.put("红茶", hongcha);

        Map<String, Object> wulongcha = new LinkedHashMap<>();
        wulongcha.put("waterTemperatureMin", 90); wulongcha.put("waterTemperatureMax", 100);
        wulongcha.put("teaAmountMin", 5); wulongcha.put("teaAmountMax", 12);
        wulongcha.put("waterAmountMin", 100); wulongcha.put("waterAmountMax", 220);
        wulongcha.put("firstInfusionTimeMin", 5); wulongcha.put("firstInfusionTimeMax", 20);
        wulongcha.put("secondInfusionTimeMin", 5); wulongcha.put("secondInfusionTimeMax", 20);
        wulongcha.put("thirdInfusionTimeMin", 5); wulongcha.put("thirdInfusionTimeMax", 25);
        wulongcha.put("subsequentInfusionTimeMin", 5); wulongcha.put("subsequentInfusionTimeMax", 30);
        RANGE_CONFIGS.put("乌龙茶", wulongcha);

        Map<String, Object> puercha = new LinkedHashMap<>();
        puercha.put("waterTemperatureMin", 95); puercha.put("waterTemperatureMax", 100);
        puercha.put("teaAmountMin", 5); puercha.put("teaAmountMax", 12);
        puercha.put("waterAmountMin", 100); puercha.put("waterAmountMax", 220);
        puercha.put("firstInfusionTimeMin", 5); puercha.put("firstInfusionTimeMax", 15);
        puercha.put("secondInfusionTimeMin", 5); puercha.put("secondInfusionTimeMax", 15);
        puercha.put("thirdInfusionTimeMin", 5); puercha.put("thirdInfusionTimeMax", 20);
        puercha.put("subsequentInfusionTimeMin", 5); puercha.put("subsequentInfusionTimeMax", 25);
        RANGE_CONFIGS.put("普洱茶", puercha);

        Map<String, Object> baicha = new LinkedHashMap<>();
        baicha.put("waterTemperatureMin", 70); baicha.put("waterTemperatureMax", 95);
        baicha.put("teaAmountMin", 3); baicha.put("teaAmountMax", 7);
        baicha.put("waterAmountMin", 100); baicha.put("waterAmountMax", 200);
        baicha.put("firstInfusionTimeMin", 10); baicha.put("firstInfusionTimeMax", 35);
        baicha.put("secondInfusionTimeMin", 10); baicha.put("secondInfusionTimeMax", 40);
        baicha.put("thirdInfusionTimeMin", 10); baicha.put("thirdInfusionTimeMax", 45);
        baicha.put("subsequentInfusionTimeMin", 10); baicha.put("subsequentInfusionTimeMax", 50);
        RANGE_CONFIGS.put("白茶", baicha);

        Map<String, Object> heicha = new LinkedHashMap<>();
        heicha.put("waterTemperatureMin", 95); heicha.put("waterTemperatureMax", 100);
        heicha.put("teaAmountMin", 5); heicha.put("teaAmountMax", 12);
        heicha.put("waterAmountMin", 100); heicha.put("waterAmountMax", 220);
        heicha.put("firstInfusionTimeMin", 5); heicha.put("firstInfusionTimeMax", 20);
        heicha.put("secondInfusionTimeMin", 5); heicha.put("secondInfusionTimeMax", 20);
        heicha.put("thirdInfusionTimeMin", 5); heicha.put("thirdInfusionTimeMax", 25);
        heicha.put("subsequentInfusionTimeMin", 5); heicha.put("subsequentInfusionTimeMax", 30);
        RANGE_CONFIGS.put("黑茶", heicha);

        Map<String, Object> huangcha = new LinkedHashMap<>();
        huangcha.put("waterTemperatureMin", 65); huangcha.put("waterTemperatureMax", 85);
        huangcha.put("teaAmountMin", 2); huangcha.put("teaAmountMax", 5);
        huangcha.put("waterAmountMin", 100); huangcha.put("waterAmountMax", 200);
        huangcha.put("firstInfusionTimeMin", 5); huangcha.put("firstInfusionTimeMax", 30);
        huangcha.put("secondInfusionTimeMin", 5); huangcha.put("secondInfusionTimeMax", 35);
        huangcha.put("thirdInfusionTimeMin", 5); huangcha.put("thirdInfusionTimeMax", 40);
        huangcha.put("subsequentInfusionTimeMin", 5); huangcha.put("subsequentInfusionTimeMax", 45);
        RANGE_CONFIGS.put("黄茶", huangcha);

        Map<String, Object> huacha = new LinkedHashMap<>();
        huacha.put("waterTemperatureMin", 75); huacha.put("waterTemperatureMax", 90);
        huacha.put("teaAmountMin", 3); huacha.put("teaAmountMax", 7);
        huacha.put("waterAmountMin", 100); huacha.put("waterAmountMax", 200);
        huacha.put("firstInfusionTimeMin", 5); huacha.put("firstInfusionTimeMax", 30);
        huacha.put("secondInfusionTimeMin", 5); huacha.put("secondInfusionTimeMax", 35);
        huacha.put("thirdInfusionTimeMin", 5); huacha.put("thirdInfusionTimeMax", 40);
        huacha.put("subsequentInfusionTimeMin", 5); huacha.put("subsequentInfusionTimeMax", 45);
        RANGE_CONFIGS.put("花茶", huacha);

        Map<String, Object> defaultRange = new LinkedHashMap<>();
        defaultRange.put("waterTemperatureMin", 60); defaultRange.put("waterTemperatureMax", 100);
        defaultRange.put("teaAmountMin", 2); defaultRange.put("teaAmountMax", 12);
        defaultRange.put("waterAmountMin", 80); defaultRange.put("waterAmountMax", 250);
        defaultRange.put("firstInfusionTimeMin", 3); defaultRange.put("firstInfusionTimeMax", 60);
        defaultRange.put("secondInfusionTimeMin", 3); defaultRange.put("secondInfusionTimeMax", 60);
        defaultRange.put("thirdInfusionTimeMin", 3); defaultRange.put("thirdInfusionTimeMax", 60);
        defaultRange.put("subsequentInfusionTimeMin", 3); defaultRange.put("subsequentInfusionTimeMax", 60);
        RANGE_CONFIGS.put("default", defaultRange);
    }

    public Map<String, Object> getParamRanges(String teaCategory) {
        return RANGE_CONFIGS.getOrDefault(teaCategory, RANGE_CONFIGS.get("default"));
    }

    public Map<String, Object> getBrewingTemplate(String teaCategory) {
        String key = TEMPLATE_KEY_PREFIX + teaCategory;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                Map<String, Object> template = objectMapper.readValue(cached, Map.class);
                Long cachedVersion = getCachedVersion(teaCategory);
                if (cachedVersion != null && cachedVersion >= CURRENT_VERSION) {
                    lastHitStatus.put(teaCategory, new CacheHitStatus(true, LocalDateTime.now(), cachedVersion, CURRENT_VERSION));
                    return template;
                }
                log.info("Template version mismatch for {} (cached={}, current={}), rebuilding", teaCategory, cachedVersion, CURRENT_VERSION);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse cached template for {}: {}", teaCategory, e.getMessage());
            }
        }
        Map<String, Object> template = buildDefaultTemplate(teaCategory);
        cacheTemplate(teaCategory, template);
        lastHitStatus.put(teaCategory, new CacheHitStatus(false, LocalDateTime.now(), null, CURRENT_VERSION));
        return template;
    }

    private Long getCachedVersion(String teaCategory) {
        String versionKey = VERSION_KEY_PREFIX + teaCategory;
        String versionStr = redisTemplate.opsForValue().get(versionKey);
        if (versionStr != null) {
            try {
                return Long.parseLong(versionStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private void cacheTemplate(String teaCategory, Map<String, Object> template) {
        String key = TEMPLATE_KEY_PREFIX + teaCategory;
        String versionKey = VERSION_KEY_PREFIX + teaCategory;
        try {
            String json = objectMapper.writeValueAsString(template);
            redisTemplate.opsForValue().set(key, json, TEMPLATE_TTL_HOURS, TimeUnit.HOURS);
            redisTemplate.opsForValue().set(versionKey, String.valueOf(CURRENT_VERSION), TEMPLATE_TTL_HOURS, TimeUnit.HOURS);
            log.info("Cached brewing template for: {} (version={})", teaCategory, CURRENT_VERSION);
        } catch (JsonProcessingException e) {
            log.warn("Failed to cache template for {}: {}", teaCategory, e.getMessage());
        }
    }

    private Map<String, Object> buildDefaultTemplate(String teaCategory) {
        Map<String, Object> template = new HashMap<>();
        template.put("teaCategory", teaCategory);

        switch (teaCategory) {
            case "绿茶":
                template.put("waterTemperature", 80);
                template.put("teaAmount", 3);
                template.put("teaRatio", "1:50");
                template.put("waterAmount", 150);
                template.put("firstInfusionTime", 15);
                template.put("secondInfusionTime", 20);
                template.put("thirdInfusionTime", 25);
                template.put("subsequentInfusionTime", 30);
                template.put("totalInfusions", 3);
                template.put("waterQuality", "纯净水/山泉水");
                template.put("tips", "绿茶不宜高温冲泡，避免苦涩");
                break;
            case "红茶":
                template.put("waterTemperature", 90);
                template.put("teaAmount", 5);
                template.put("teaRatio", "1:30");
                template.put("waterAmount", 150);
                template.put("firstInfusionTime", 10);
                template.put("secondInfusionTime", 15);
                template.put("thirdInfusionTime", 20);
                template.put("subsequentInfusionTime", 25);
                template.put("totalInfusions", 5);
                template.put("waterQuality", "纯净水/山泉水");
                template.put("tips", "红茶可用稍高温水，充分激发香气");
                break;
            case "乌龙茶":
                template.put("waterTemperature", 95);
                template.put("teaAmount", 8);
                template.put("teaRatio", "1:20");
                template.put("waterAmount", 160);
                template.put("firstInfusionTime", 10);
                template.put("secondInfusionTime", 12);
                template.put("thirdInfusionTime", 15);
                template.put("subsequentInfusionTime", 20);
                template.put("totalInfusions", 8);
                template.put("waterQuality", "山泉水/纯净水");
                template.put("tips", "乌龙茶需高温冲泡，激发花果香");
                break;
            case "普洱茶":
                template.put("waterTemperature", 100);
                template.put("teaAmount", 8);
                template.put("teaRatio", "1:20");
                template.put("waterAmount", 160);
                template.put("firstInfusionTime", 8);
                template.put("secondInfusionTime", 8);
                template.put("thirdInfusionTime", 10);
                template.put("subsequentInfusionTime", 15);
                template.put("totalInfusions", 12);
                template.put("waterQuality", "山泉水/纯净水");
                template.put("tips", "普洱茶需沸水冲泡，洗茶一道后品饮");
                break;
            case "白茶":
                template.put("waterTemperature", 85);
                template.put("teaAmount", 5);
                template.put("teaRatio", "1:30");
                template.put("waterAmount", 150);
                template.put("firstInfusionTime", 20);
                template.put("secondInfusionTime", 25);
                template.put("thirdInfusionTime", 30);
                template.put("subsequentInfusionTime", 35);
                template.put("totalInfusions", 6);
                template.put("waterQuality", "纯净水/山泉水");
                template.put("tips", "白茶温和，新茶低温老茶高温");
                break;
            case "黑茶":
                template.put("waterTemperature", 100);
                template.put("teaAmount", 8);
                template.put("teaRatio", "1:20");
                template.put("waterAmount", 160);
                template.put("firstInfusionTime", 10);
                template.put("secondInfusionTime", 10);
                template.put("thirdInfusionTime", 12);
                template.put("subsequentInfusionTime", 15);
                template.put("totalInfusions", 10);
                template.put("waterQuality", "山泉水/纯净水");
                template.put("tips", "黑茶需沸水冲泡，可多次出汤");
                break;
            case "黄茶":
                template.put("waterTemperature", 80);
                template.put("teaAmount", 3);
                template.put("teaRatio", "1:50");
                template.put("waterAmount", 150);
                template.put("firstInfusionTime", 15);
                template.put("secondInfusionTime", 20);
                template.put("thirdInfusionTime", 25);
                template.put("subsequentInfusionTime", 30);
                template.put("totalInfusions", 4);
                template.put("waterQuality", "纯净水/山泉水");
                template.put("tips", "黄茶冲泡类似绿茶，水温略低");
                break;
            case "花茶":
                template.put("waterTemperature", 85);
                template.put("teaAmount", 5);
                template.put("teaRatio", "1:30");
                template.put("waterAmount", 150);
                template.put("firstInfusionTime", 15);
                template.put("secondInfusionTime", 20);
                template.put("thirdInfusionTime", 25);
                template.put("subsequentInfusionTime", 30);
                template.put("totalInfusions", 4);
                template.put("waterQuality", "纯净水/山泉水");
                template.put("tips", "花茶中温冲泡，保留花香");
                break;
            default:
                template.put("waterTemperature", 90);
                template.put("teaAmount", 5);
                template.put("teaRatio", "1:30");
                template.put("waterAmount", 150);
                template.put("firstInfusionTime", 15);
                template.put("secondInfusionTime", 20);
                template.put("thirdInfusionTime", 25);
                template.put("subsequentInfusionTime", 30);
                template.put("totalInfusions", 5);
                template.put("waterQuality", "纯净水/山泉水");
                template.put("tips", "可根据个人口味调整参数");
                break;
        }

        Map<String, Object> ranges = getParamRanges(teaCategory);
        template.put("ranges", ranges);

        return template;
    }

    public void initAllTemplates() {
        String[] categories = {"绿茶", "红茶", "乌龙茶", "普洱茶", "白茶", "黑茶", "黄茶", "花茶"};
        for (String category : categories) {
            refreshTemplate(category);
        }
        log.info("Initialized all tea brewing templates in Redis cache (version={})", CURRENT_VERSION);
    }

    public void refreshTemplate(String teaCategory) {
        String key = TEMPLATE_KEY_PREFIX + teaCategory;
        redisTemplate.delete(key);
        String versionKey = VERSION_KEY_PREFIX + teaCategory;
        redisTemplate.delete(versionKey);
        Map<String, Object> template = buildDefaultTemplate(teaCategory);
        cacheTemplate(teaCategory, template);
        lastHitStatus.put(teaCategory, new CacheHitStatus(false, LocalDateTime.now(), null, CURRENT_VERSION));
        log.info("Refreshed brewing template for: {} (version={})", teaCategory, CURRENT_VERSION);
    }

    public void refreshAllTemplates() {
        String[] categories = {"绿茶", "红茶", "乌龙茶", "普洱茶", "白茶", "黑茶", "黄茶", "花茶"};
        for (String category : categories) {
            refreshTemplate(category);
        }
        log.info("Refreshed all tea brewing templates (version={})", CURRENT_VERSION);
    }

    public void evictTemplate(String teaCategory) {
        String key = TEMPLATE_KEY_PREFIX + teaCategory;
        redisTemplate.delete(key);
        String versionKey = VERSION_KEY_PREFIX + teaCategory;
        redisTemplate.delete(versionKey);
        lastHitStatus.remove(teaCategory);
        log.info("Evicted brewing template cache for: {}", teaCategory);
    }

    public CacheHitStatus getCacheHitStatus(String teaCategory) {
        return lastHitStatus.get(teaCategory);
    }

    public Map<String, CacheHitStatus> getAllCacheStatus() {
        return new HashMap<>(lastHitStatus);
    }

    public List<String> getSupportedCategories() {
        return Arrays.asList("绿茶", "红茶", "乌龙茶", "普洱茶", "白茶", "黑茶", "黄茶", "花茶");
    }

    public static class CacheHitStatus {
        private final boolean cacheHit;
        private final LocalDateTime hitTime;
        private final Long cachedVersion;
        private final long currentVersion;

        public CacheHitStatus(boolean cacheHit, LocalDateTime hitTime, Long cachedVersion, long currentVersion) {
            this.cacheHit = cacheHit;
            this.hitTime = hitTime;
            this.cachedVersion = cachedVersion;
            this.currentVersion = currentVersion;
        }

        public boolean isCacheHit() { return cacheHit; }
        public LocalDateTime getHitTime() { return hitTime; }
        public Long getCachedVersion() { return cachedVersion; }
        public long getCurrentVersion() { return currentVersion; }
        public boolean isVersionMatch() { return cachedVersion != null && cachedVersion == currentVersion; }
    }
}
