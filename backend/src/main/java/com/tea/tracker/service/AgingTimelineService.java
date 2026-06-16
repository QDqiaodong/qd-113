package com.tea.tracker.service;

import com.tea.tracker.dto.AgingNode;
import com.tea.tracker.dto.AgingTimelineResponse;
import com.tea.tracker.entity.StorageRecord;
import com.tea.tracker.entity.Tea;
import com.tea.tracker.entity.TastingNote;
import com.tea.tracker.repository.StorageRecordRepository;
import com.tea.tracker.repository.TeaRepository;
import com.tea.tracker.repository.TastingNoteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgingTimelineService {

    private final TeaRepository teaRepository;
    private final StorageRecordRepository storageRecordRepository;
    private final TastingNoteRepository tastingNoteRepository;

    private static final Set<String> AGING_RECOMMENDED_CATEGORIES = Set.of("普洱茶", "白茶", "黑茶");

    private static final Map<String, Map<String, Object>> CATEGORY_AGING_CONFIG = new HashMap<>();

    static {
        Map<String, Object> puErConfig = new HashMap<>();
        puErConfig.put("optimalStart", 3);
        puErConfig.put("optimalEnd", 20);
        puErConfig.put("description", "越陈越香，后发酵茶，适合长期存放");
        puErConfig.put("stages", Arrays.asList(
                createStage(0, 1, "新茶期", "茶性活跃，香气高扬，苦涩明显"),
                createStage(1, 3, "转化初期", "苦涩渐消，汤色加深，香气转变"),
                createStage(3, 5, "转化中期", "苦涩均衡，陈香初显，口感醇厚"),
                createStage(5, 10, "转化后期", "陈香浓郁，口感滑糯，层次丰富"),
                createStage(10, 20, "陈茶期", "药香明显，口感醇厚饱满"),
                createStage(20, Integer.MAX_VALUE, "老茶期", "参香、樟香，珍贵稀缺")
        ));
        puErConfig.put("milestones", Arrays.asList(1, 3, 5, 10, 15, 20, 30));
        CATEGORY_AGING_CONFIG.put("普洱茶", puErConfig);

        Map<String, Object> whiteConfig = new HashMap<>();
        whiteConfig.put("optimalStart", 3);
        whiteConfig.put("optimalEnd", 15);
        whiteConfig.put("description", "一年茶，三年药，七年宝");
        whiteConfig.put("stages", Arrays.asList(
                createStage(0, 1, "新茶期", "毫香明显，清甜鲜爽"),
                createStage(1, 3, "转化期", "毫香转蜜香，汤色加深"),
                createStage(3, 7, "药香期", "荷叶香、枣香显现，药性渐显"),
                createStage(7, 15, "宝期", "陈香馥郁，醇厚顺滑，药性显著"),
                createStage(15, Integer.MAX_VALUE, "老白茶期", "参香、药香浓郁")
        ));
        whiteConfig.put("milestones", Arrays.asList(1, 3, 5, 7, 10, 15, 20));
        CATEGORY_AGING_CONFIG.put("白茶", whiteConfig);

        Map<String, Object> darkConfig = new HashMap<>();
        darkConfig.put("optimalStart", 3);
        darkConfig.put("optimalEnd", 15);
        darkConfig.put("description", "后发酵茶，越陈越醇，适合长期存放");
        darkConfig.put("stages", Arrays.asList(
                createStage(0, 2, "新茶期", "火气未退，口感粗砺"),
                createStage(2, 5, "转化初期", "火气消退，陈香初显"),
                createStage(5, 10, "转化中期", "陈香明显，醇和顺滑"),
                createStage(10, 15, "陈茶期", "药香、陈香浓郁"),
                createStage(15, Integer.MAX_VALUE, "老茶期", "醇厚饱满，参香明显")
        ));
        darkConfig.put("milestones", Arrays.asList(2, 5, 8, 10, 15, 20));
        CATEGORY_AGING_CONFIG.put("黑茶", darkConfig);

        Map<String, Object> oolongConfig = new HashMap<>();
        oolongConfig.put("optimalStart", 1);
        oolongConfig.put("optimalEnd", 5);
        oolongConfig.put("description", "半发酵茶，部分可陈化");
        oolongConfig.put("stages", Arrays.asList(
                createStage(0, 1, "清香期", "香气高扬，口感清爽"),
                createStage(1, 3, "转化期", "香气转沉，口感醇厚"),
                createStage(3, 5, "陈香期", "陈香显现，口感醇和")
        ));
        oolongConfig.put("milestones", Arrays.asList(1, 2, 3, 5));
        CATEGORY_AGING_CONFIG.put("乌龙茶", oolongConfig);

        Map<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put("optimalStart", 0);
        defaultConfig.put("optimalEnd", 2);
        defaultConfig.put("description", "建议新鲜饮用，不建议长期陈化");
        defaultConfig.put("stages", Collections.emptyList());
        defaultConfig.put("milestones", Arrays.asList(1, 2));
        CATEGORY_AGING_CONFIG.put("default", defaultConfig);
    }

    private static Map<String, Object> createStage(int start, int end, String name, String description) {
        Map<String, Object> stage = new HashMap<>();
        stage.put("startYears", start);
        stage.put("endYears", end);
        stage.put("name", name);
        stage.put("description", description);
        return stage;
    }

    @Transactional(readOnly = true)
    public AgingTimelineResponse getAgingTimeline(Long teaId) {
        Tea tea = teaRepository.findById(teaId)
                .orElseThrow(() -> new EntityNotFoundException("茶叶档案不存在: " + teaId));

        AgingTimelineResponse response = new AgingTimelineResponse();
        response.setTeaId(tea.getId());
        response.setTeaName(tea.getName());
        response.setTeaCategory(tea.getTeaCategory());
        response.setHarvestYear(tea.getHarvestYear());
        response.setStorageMethod(tea.getStorageMethod());
        response.setCalculatedAt(LocalDateTime.now());

        boolean isAgingRecommended = AGING_RECOMMENDED_CATEGORIES.contains(tea.getTeaCategory());
        response.setIsAgingRecommended(isAgingRecommended);

        Map<String, Object> config = CATEGORY_AGING_CONFIG.getOrDefault(
                tea.getTeaCategory(), CATEGORY_AGING_CONFIG.get("default"));

        response.setOptimalAgingStartYears((Integer) config.get("optimalStart"));
        response.setOptimalAgingEndYears((Integer) config.get("optimalEnd"));
        response.setAgingDescription((String) config.get("description"));

        if (tea.getHarvestYear() != null) {
            LocalDate harvestDate = LocalDate.of(tea.getHarvestYear(), 4, 1);
            LocalDate today = LocalDate.now();
            Period period = Period.between(harvestDate, today);
            int totalMonths = period.getYears() * 12 + period.getMonths();
            response.setCurrentAgingYears(period.getYears());
            response.setTotalAgingMonths(totalMonths);

            String agingStatus = calculateAgingStatus(period.getYears(), tea.getTeaCategory());
            response.setAgingStatus(agingStatus);
        } else {
            response.setCurrentAgingYears(0);
            response.setTotalAgingMonths(0);
            response.setAgingStatus("未知");
        }

        List<AgingNode> nodes = generateAgingNodes(tea, config);
        response.setNodes(nodes);

        return response;
    }

    private String calculateAgingStatus(int years, String category) {
        Map<String, Object> config = CATEGORY_AGING_CONFIG.getOrDefault(
                category, CATEGORY_AGING_CONFIG.get("default"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stages = (List<Map<String, Object>>) config.get("stages");

        for (Map<String, Object> stage : stages) {
            int start = (Integer) stage.get("startYears");
            int end = (Integer) stage.get("endYears");
            if (years >= start && years < end) {
                return (String) stage.get("name");
            }
        }

        if (!stages.isEmpty() && years >= (Integer) stages.get(stages.size() - 1).get("startYears")) {
            return (String) stages.get(stages.size() - 1).get("name");
        }

        return "适饮期";
    }

    private List<AgingNode> generateAgingNodes(Tea tea, Map<String, Object> config) {
        List<AgingNode> nodes = new ArrayList<>();
        long nodeId = 1;

        if (tea.getHarvestYear() != null) {
            AgingNode harvestNode = new AgingNode();
            harvestNode.setId(nodeId++);
            harvestNode.setNodeType("HARVEST");
            harvestNode.setNodeTitle("采摘年份");
            harvestNode.setNodeDescription(tea.getHarvestYear() + "年春茶采摘");
            harvestNode.setNodeDate(LocalDate.of(tea.getHarvestYear(), 4, 1));
            harvestNode.setYearsSinceHarvest(0);
            harvestNode.setMonthsSinceHarvest(0);
            harvestNode.setImportance("HIGH");
            harvestNode.setIsHighlighted(true);
            harvestNode.setColor("#22c55e");
            nodes.add(harvestNode);

            @SuppressWarnings("unchecked")
            List<Integer> milestones = (List<Integer>) config.get("milestones");
            int currentYear = LocalDate.now().getYear();
            for (Integer milestone : milestones) {
                int milestoneYear = tea.getHarvestYear() + milestone;
                if (milestoneYear <= currentYear + 5) {
                    AgingNode milestoneNode = createMilestoneNode(nodeId++, tea.getHarvestYear(), milestone, milestoneYear, tea.getTeaCategory());
                    nodes.add(milestoneNode);
                }
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> stages = (List<Map<String, Object>>) config.get("stages");
            for (Map<String, Object> stage : stages) {
                int startYear = tea.getHarvestYear() + (Integer) stage.get("startYears");
                if (startYear <= currentYear) {
                    AgingNode stageNode = new AgingNode();
                    stageNode.setId(nodeId++);
                    stageNode.setNodeType("STAGE");
                    stageNode.setNodeTitle("进入" + stage.get("name"));
                    stageNode.setNodeDescription((String) stage.get("description"));
                    stageNode.setNodeDate(LocalDate.of(startYear, 1, 1));
                    stageNode.setYearsSinceHarvest((Integer) stage.get("startYears"));
                    stageNode.setMonthsSinceHarvest((Integer) stage.get("startYears") * 12);
                    stageNode.setImportance("MEDIUM");
                    stageNode.setIsHighlighted(false);
                    stageNode.setColor("#3b82f6");
                    nodes.add(stageNode);
                }
            }
        }

        List<StorageRecord> storageRecords = storageRecordRepository.findByTeaIdOrderByRecordDateAsc(tea.getId());
        if (storageRecords != null) {
            for (StorageRecord record : storageRecords) {
                if (record.getRecordDate() != null) {
                    AgingNode storageNode = new AgingNode();
                    storageNode.setId(nodeId++);
                    storageNode.setNodeType("STORAGE");
                    storageNode.setNodeTitle("仓储记录");
                    storageNode.setNodeDescription(record.getNotes() != null ? record.getNotes() : "仓储环境检查");
                    storageNode.setNodeDate(record.getRecordDate().toLocalDate());
                    if (tea.getHarvestYear() != null) {
                        Period period = Period.between(
                                LocalDate.of(tea.getHarvestYear(), 4, 1),
                                record.getRecordDate().toLocalDate());
                        storageNode.setYearsSinceHarvest(period.getYears());
                        storageNode.setMonthsSinceHarvest(period.getYears() * 12 + period.getMonths());
                    }
                    storageNode.setImportance("MEDIUM");
                    storageNode.setTemperature(record.getTemperature());
                    storageNode.setHumidity(record.getHumidity());
                    storageNode.setStorageLocation(record.getStorageLocation());
                    storageNode.setSealCondition(record.getSealCondition());
                    storageNode.setRelatedRecordType("STORAGE_RECORD");
                    storageNode.setRelatedRecordId(record.getId());
                    storageNode.setIsHighlighted(false);
                    storageNode.setColor("#f59e0b");
                    nodes.add(storageNode);
                }
            }
        }

        List<TastingNote> tastingNotes = tastingNoteRepository.findByTeaIdOrderByTastingDateAsc(tea.getId());
        if (tastingNotes != null) {
            for (TastingNote note : tastingNotes) {
                if (note.getTastingDate() != null) {
                    AgingNode tastingNode = new AgingNode();
                    tastingNode.setId(nodeId++);
                    tastingNode.setNodeType("TASTING");
                    tastingNode.setNodeTitle("品鉴记录");
                    tastingNode.setNodeDescription(note.getImpression() != null ? note.getImpression() : "口感品鉴");
                    tastingNode.setNodeDate(note.getTastingDate().toLocalDate());
                    if (tea.getHarvestYear() != null) {
                        Period period = Period.between(
                                LocalDate.of(tea.getHarvestYear(), 4, 1),
                                note.getTastingDate().toLocalDate());
                        tastingNode.setYearsSinceHarvest(period.getYears());
                        tastingNode.setMonthsSinceHarvest(period.getYears() * 12 + period.getMonths());
                    }
                    tastingNode.setImportance("MEDIUM");
                    tastingNode.setRelatedRecordType("TASTING_NOTE");
                    tastingNode.setRelatedRecordId(note.getId());
                    tastingNode.setIsHighlighted(false);
                    tastingNode.setColor("#8b5cf6");
                    nodes.add(tastingNode);
                }
            }
        }

        nodes.sort(Comparator.comparing(AgingNode::getNodeDate));

        return nodes;
    }

    private AgingNode createMilestoneNode(Long id, Integer harvestYear, Integer milestone, Integer milestoneYear, String category) {
        AgingNode node = new AgingNode();
        node.setId(id);
        node.setNodeType("MILESTONE");
        node.setYearsSinceHarvest(milestone);
        node.setMonthsSinceHarvest(milestone * 12);
        node.setImportance("HIGH");
        node.setIsHighlighted(true);
        node.setColor("#ef4444");

        switch (milestone) {
            case 1:
                node.setNodeTitle("1周年陈化");
                if ("白茶".equals(category)) {
                    node.setNodeDescription("一年茶，香气转化开始显现");
                } else {
                    node.setNodeDescription("第一年转化初期，茶性趋于稳定");
                }
                break;
            case 3:
                node.setNodeTitle("3周年陈化");
                if ("白茶".equals(category)) {
                    node.setNodeDescription("三年药，药性初显，口感醇厚");
                } else if ("普洱茶".equals(category)) {
                    node.setNodeDescription("转化中期，苦涩均衡，陈香初显");
                } else {
                    node.setNodeDescription("三年陈化，口感醇厚饱满");
                }
                break;
            case 5:
                node.setNodeTitle("5周年陈化");
                if ("白茶".equals(category)) {
                    node.setNodeDescription("五年转化，药香明显");
                } else if ("普洱茶".equals(category)) {
                    node.setNodeDescription("五年陈化，茶性温和，陈香浓郁");
                } else {
                    node.setNodeDescription("五年陈化，口感醇和");
                }
                break;
            case 7:
                node.setNodeTitle("7周年陈化");
                if ("白茶".equals(category)) {
                    node.setNodeDescription("七年宝，陈香馥郁，药性显著");
                } else {
                    node.setNodeDescription("七年陈化，品质升华");
                }
                break;
            case 10:
                node.setNodeTitle("10周年陈化");
                node.setNodeDescription("十年以上老茶，陈香馥郁，口感醇厚饱满");
                break;
            case 15:
                node.setNodeTitle("15周年陈化");
                node.setNodeDescription("十五年老茶，参香明显，珍贵稀缺");
                break;
            case 20:
                node.setNodeTitle("20周年陈化");
                node.setNodeDescription("二十年老茶，古董级珍藏品");
                break;
            case 30:
                node.setNodeTitle("30周年陈化");
                node.setNodeDescription("三十年老茶，可遇不可求");
                break;
            default:
                node.setNodeTitle(milestone + "周年陈化");
                node.setNodeDescription(milestone + "年陈化纪念");
        }

        node.setNodeDate(LocalDate.of(milestoneYear, 4, 1));
        return node;
    }

    @Transactional(readOnly = true)
    public List<AgingTimelineResponse> getAgingTimelinesByCategory(String category) {
        List<Tea> teas;
        if (category != null && !category.isBlank()) {
            teas = teaRepository.findByTeaCategoryOrderByCreatedAtDesc(category);
        } else {
            teas = teaRepository.findAllByOrderByCreatedAtDesc();
        }

        List<AgingTimelineResponse> responses = new ArrayList<>();
        for (Tea tea : teas) {
            responses.add(getAgingTimeline(tea.getId()));
        }
        responses.sort(Comparator.comparingInt(AgingTimelineResponse::getCurrentAgingYears).reversed());
        return responses;
    }
}
