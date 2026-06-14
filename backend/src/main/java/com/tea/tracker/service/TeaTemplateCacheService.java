package com.tea.tracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeaTemplateCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String TEMPLATE_KEY_PREFIX = "tea:template:";
    private static final long TEMPLATE_TTL_HOURS = 24;

    public Map<String, Object> getBrewingTemplate(String teaCategory) {
        String key = TEMPLATE_KEY_PREFIX + teaCategory;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, Map.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse cached template for {}: {}", teaCategory, e.getMessage());
            }
        }
        Map<String, Object> template = buildDefaultTemplate(teaCategory);
        cacheTemplate(teaCategory, template);
        return template;
    }

    private void cacheTemplate(String teaCategory, Map<String, Object> template) {
        String key = TEMPLATE_KEY_PREFIX + teaCategory;
        try {
            String json = objectMapper.writeValueAsString(template);
            redisTemplate.opsForValue().set(key, json, TEMPLATE_TTL_HOURS, TimeUnit.HOURS);
            log.info("Cached brewing template for: {}", teaCategory);
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
        return template;
    }

    public void initAllTemplates() {
        String[] categories = {"绿茶", "红茶", "乌龙茶", "普洱茶", "白茶", "黑茶", "黄茶", "花茶"};
        for (String category : categories) {
            getBrewingTemplate(category);
        }
        log.info("Initialized all tea brewing templates in Redis cache");
    }
}
