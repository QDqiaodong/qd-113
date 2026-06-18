package com.tea.tracker.init;

import com.tea.tracker.dto.TastingVocabularyRequest;
import com.tea.tracker.service.TastingVocabularyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class TastingVocabularyInitializer implements CommandLineRunner {

    private final TastingVocabularyService vocabularyService;

    @Override
    public void run(String... args) {
        log.info("Initializing tasting vocabulary presets...");
        initGreenTeaVocabularies();
        initBlackTeaVocabularies();
        initOolongTeaVocabularies();
        initPuErTeaVocabularies();
        initWhiteTeaVocabularies();
        initDarkTeaVocabularies();
        initYellowTeaVocabularies();
        initFlowerTeaVocabularies();
        log.info("Tasting vocabulary presets initialization complete.");
    }

    private void initGreenTeaVocabularies() {
        String category = "绿茶";
        List<TastingVocabularyRequest> vocabularies = new ArrayList<>();

        vocabularies.addAll(buildAromaVocabularies(category, Arrays.asList(
                new WordDesc("豆香", "新鲜豆类的清香"),
                new WordDesc("栗香", "熟板栗的香气"),
                new WordDesc("清香", "清新淡雅的青草香"),
                new WordDesc("兰香", "兰花般的幽香"),
                new WordDesc("板栗香", "熟板栗的甜香"),
                new WordDesc("毫香", "茶毫特有的清香"),
                new WordDesc("嫩香", "嫩芽的清鲜香气"),
                new WordDesc("果香", "淡淡的水果香气"),
                new WordDesc("花香", "清雅的花香味"),
                new WordDesc("青草香", "新鲜青草的香气")
        )));

        vocabularies.addAll(buildLiquorColorVocabularies(category, Arrays.asList(
                new WordDesc("嫩绿", "鲜嫩的绿色"),
                new WordDesc("黄绿", "黄中带绿"),
                new WordDesc("翠绿", "翠绿明亮"),
                new WordDesc("浅绿", "淡绿色"),
                new WordDesc("杏绿", "杏黄色带绿"),
                new WordDesc("清澈", "茶汤清澈透明"),
                new WordDesc("明亮", "汤色光泽明亮"),
                new WordDesc("嫩绿明亮", "嫩绿且有光泽")
        )));

        vocabularies.addAll(buildTasteVocabularies(category, Arrays.asList(
                new WordDesc("鲜爽", "鲜醇爽口"),
                new WordDesc("甘醇", "甘甜醇厚"),
                new WordDesc("清甜", "清爽甘甜"),
                new WordDesc("醇厚", "口感饱满厚实"),
                new WordDesc("鲜醇", "鲜爽醇厚"),
                new WordDesc("回甘快", "回甘迅速明显"),
                new WordDesc("生津", "口腔分泌唾液"),
                new WordDesc("清爽", "口感清爽不腻"),
                new WordDesc("绵滑", "口感细腻顺滑"),
                new WordDesc("苦涩适中", "苦涩感恰到好处")
        )));

        vocabularies.addAll(buildAftertasteVocabularies(category, Arrays.asList(
                new WordDesc("回甘持久", "回甘持续时间长"),
                new WordDesc("生津明显", "生津效果显著"),
                new WordDesc("齿颊留香", "牙齿和两颊留香"),
                new WordDesc("余韵悠长", "余味悠长"),
                new WordDesc("清甜回味", "回味清甜"),
                new WordDesc("甘润", "回味甘甜润泽"),
                new WordDesc("留香持久", "香气留存时间长"),
                new WordDesc("喉韵明显", "喉部有明显韵味")
        )));

        vocabularyService.batchCreateVocabularies(vocabularies);
        log.info("Green tea vocabularies initialized: {} words", vocabularies.size());
    }

    private void initBlackTeaVocabularies() {
        String category = "红茶";
        List<TastingVocabularyRequest> vocabularies = new ArrayList<>();

        vocabularies.addAll(buildAromaVocabularies(category, Arrays.asList(
                new WordDesc("蜜香", "蜂蜜般的甜香"),
                new WordDesc("果香", "成熟水果的香气"),
                new WordDesc("焦糖香", "焦糖的甜香"),
                new WordDesc("玫瑰香", "玫瑰花的香气"),
                new WordDesc("薯香", "红薯的香甜气"),
                new WordDesc("桂圆香", "桂圆干的香气"),
                new WordDesc("松烟香", "松烟熏制的香气"),
                new WordDesc("花香", "馥郁的花香"),
                new WordDesc("甜香", "甜甜的香气"),
                new WordDesc("麦芽香", "麦芽糖的香气")
        )));

        vocabularies.addAll(buildLiquorColorVocabularies(category, Arrays.asList(
                new WordDesc("红艳", "红亮艳丽"),
                new WordDesc("红亮", "红而明亮"),
                new WordDesc("橙红", "橙红色"),
                new WordDesc("深红", "深红色"),
                new WordDesc("金黄", "金黄色"),
                new WordDesc("琥珀色", "琥珀般的颜色"),
                new WordDesc("明亮", "汤色有光泽"),
                new WordDesc("金圈明显", "杯沿有明显金圈")
        )));

        vocabularies.addAll(buildTasteVocabularies(category, Arrays.asList(
                new WordDesc("醇厚", "口感饱满厚实"),
                new WordDesc("鲜甜", "鲜爽甘甜"),
                new WordDesc("浓强", "浓烈强劲"),
                new WordDesc("甘润", "甘甜润泽"),
                new WordDesc("鲜爽", "鲜醇爽口"),
                new WordDesc("甜醇", "甜而醇厚"),
                new WordDesc("饱满", "口感丰富饱满"),
                new WordDesc("顺滑", "口感顺滑"),
                new WordDesc("绵甜", "绵长的甜味"),
                new WordDesc("醇厚回甘", "醇厚且有回甘")
        )));

        vocabularies.addAll(buildAftertasteVocabularies(category, Arrays.asList(
                new WordDesc("回甘持久", "回甘持续时间长"),
                new WordDesc("甜润悠长", "甜润感持久"),
                new WordDesc("齿颊留香", "牙齿和两颊留香"),
                new WordDesc("生津明显", "生津效果显著"),
                new WordDesc("余韵悠长", "余味悠长"),
                new WordDesc("蜜甜回味", "回味如蜜甜"),
                new WordDesc("喉韵甘润", "喉部甘润有韵味"),
                new WordDesc("留香持久", "香气留存时间长")
        )));

        vocabularyService.batchCreateVocabularies(vocabularies);
        log.info("Black tea vocabularies initialized: {} words", vocabularies.size());
    }

    private void initOolongTeaVocabularies() {
        String category = "乌龙茶";
        List<TastingVocabularyRequest> vocabularies = new ArrayList<>();

        vocabularies.addAll(buildAromaVocabularies(category, Arrays.asList(
                new WordDesc("花香", "馥郁的花香"),
                new WordDesc("果香", "成熟水果香"),
                new WordDesc("蜜香", "蜂蜜般的甜香"),
                new WordDesc("兰香", "兰花的幽香"),
                new WordDesc("桂花香", "桂花的香气"),
                new WordDesc("奶香", "淡淡的奶香味"),
                new WordDesc("焙火香", "烘焙后的焦香"),
                new WordDesc("岩韵", "岩石般的韵味"),
                new WordDesc("清香", "清雅的香气"),
                new WordDesc("馥郁", "香气浓郁丰富")
        )));

        vocabularies.addAll(buildLiquorColorVocabularies(category, Arrays.asList(
                new WordDesc("金黄", "金黄色"),
                new WordDesc("橙黄", "橙黄色"),
                new WordDesc("黄绿", "黄中带绿"),
                new WordDesc("橙红", "橙红色"),
                new WordDesc("琥珀色", "琥珀般的颜色"),
                new WordDesc("清澈", "茶汤清澈"),
                new WordDesc("明亮", "汤色有光泽"),
                new WordDesc("油润", "汤色油亮有光泽")
        )));

        vocabularies.addAll(buildTasteVocabularies(category, Arrays.asList(
                new WordDesc("醇厚", "口感饱满厚实"),
                new WordDesc("鲜爽", "鲜醇爽口"),
                new WordDesc("甘醇", "甘甜醇厚"),
                new WordDesc("滑顺", "口感顺滑"),
                new WordDesc("岩骨花香", "岩韵与花香并存"),
                new WordDesc("韵味足", "茶韵十足"),
                new WordDesc("饱满", "口感丰富"),
                new WordDesc("清爽", "口感清爽"),
                new WordDesc("绵柔", "口感绵软柔和"),
                new WordDesc("层次丰富", "口感层次多样")
        )));

        vocabularies.addAll(buildAftertasteVocabularies(category, Arrays.asList(
                new WordDesc("回甘持久", "回甘持续时间长"),
                new WordDesc("生津明显", "生津效果显著"),
                new WordDesc("齿颊留香", "牙齿和两颊留香"),
                new WordDesc("岩韵悠长", "岩韵持久"),
                new WordDesc("余韵悠长", "余味悠长"),
                new WordDesc("七泡有余香", "多次冲泡仍有香气"),
                new WordDesc("喉韵明显", "喉部有明显韵味"),
                new WordDesc("留香持久", "香气留存时间长")
        )));

        vocabularyService.batchCreateVocabularies(vocabularies);
        log.info("Oolong tea vocabularies initialized: {} words", vocabularies.size());
    }

    private void initPuErTeaVocabularies() {
        String category = "普洱茶";
        List<TastingVocabularyRequest> vocabularies = new ArrayList<>();

        vocabularies.addAll(buildAromaVocabularies(category, Arrays.asList(
                new WordDesc("陈香", "陈化后的香气"),
                new WordDesc("樟香", "樟树的香气"),
                new WordDesc("枣香", "红枣的甜香"),
                new WordDesc("蜜香", "蜂蜜般的甜香"),
                new WordDesc("药香", "药材的香气"),
                new WordDesc("荷香", "荷叶的清香"),
                new WordDesc("糯香", "糯米的香气"),
                new WordDesc("菌香", "菌类的香气"),
                new WordDesc("烟香", "轻微的烟熏香"),
                new WordDesc("木香", "木材的香气")
        )));

        vocabularies.addAll(buildLiquorColorVocabularies(category, Arrays.asList(
                new WordDesc("红浓", "红而浓稠"),
                new WordDesc("酒红", "红酒般的颜色"),
                new WordDesc("深红", "深红色"),
                new WordDesc("褐红", "褐红色"),
                new WordDesc("金黄", "金黄色（生普）"),
                new WordDesc("橙黄", "橙黄色（生普）"),
                new WordDesc("明亮", "汤色有光泽"),
                new WordDesc("透亮", "茶汤透澈明亮")
        )));

        vocabularies.addAll(buildTasteVocabularies(category, Arrays.asList(
                new WordDesc("醇厚", "口感饱满厚实"),
                new WordDesc("顺滑", "口感顺滑"),
                new WordDesc("甘醇", "甘甜醇厚"),
                new WordDesc("绵柔", "口感绵软柔和"),
                new WordDesc("饱满", "口感丰富饱满"),
                new WordDesc("陈醇", "陈化后的醇厚感"),
                new WordDesc("生津", "口腔分泌唾液"),
                new WordDesc("回甘", "回味甘甜"),
                new WordDesc("甜润", "甜而润泽"),
                new WordDesc("层次丰富", "口感层次多样")
        )));

        vocabularies.addAll(buildAftertasteVocabularies(category, Arrays.asList(
                new WordDesc("回甘持久", "回甘持续时间长"),
                new WordDesc("生津明显", "生津效果显著"),
                new WordDesc("陈韵悠长", "陈韵持久"),
                new WordDesc("喉韵明显", "喉部有明显韵味"),
                new WordDesc("余韵悠长", "余味悠长"),
                new WordDesc("齿颊留香", "牙齿和两颊留香"),
                new WordDesc("甜润回甘", "甜润且有回甘"),
                new WordDesc("茶气足", "茶气强劲有力")
        )));

        vocabularyService.batchCreateVocabularies(vocabularies);
        log.info("Pu'er tea vocabularies initialized: {} words", vocabularies.size());
    }

    private void initWhiteTeaVocabularies() {
        String category = "白茶";
        List<TastingVocabularyRequest> vocabularies = new ArrayList<>();

        vocabularies.addAll(buildAromaVocabularies(category, Arrays.asList(
                new WordDesc("毫香", "茶毫特有的清香"),
                new WordDesc("清香", "清新淡雅的香气"),
                new WordDesc("花香", "清雅的花香"),
                new WordDesc("果香", "淡淡的水果香"),
                new WordDesc("蜜香", "蜂蜜般的甜香"),
                new WordDesc("药香", "淡淡的药材香"),
                new WordDesc("枣香", "红枣的甜香（老白茶）"),
                new WordDesc("荷叶香", "荷叶的清香"),
                new WordDesc("嫩香", "嫩芽的清鲜香气"),
                new WordDesc("鲜甜香", "鲜甜的香气")
        )));

        vocabularies.addAll(buildLiquorColorVocabularies(category, Arrays.asList(
                new WordDesc("杏黄", "杏黄色"),
                new WordDesc("浅黄", "淡黄色"),
                new WordDesc("橙黄", "橙黄色"),
                new WordDesc("黄绿", "黄中带绿"),
                new WordDesc("金黄", "金黄色"),
                new WordDesc("清澈", "茶汤清澈"),
                new WordDesc("明亮", "汤色有光泽"),
                new WordDesc("晶亮", "茶汤晶莹透亮")
        )));

        vocabularies.addAll(buildTasteVocabularies(category, Arrays.asList(
                new WordDesc("清甜", "清爽甘甜"),
                new WordDesc("鲜爽", "鲜醇爽口"),
                new WordDesc("甘醇", "甘甜醇厚"),
                new WordDesc("醇厚", "口感饱满厚实"),
                new WordDesc("鲜醇", "鲜爽醇厚"),
                new WordDesc("清爽", "口感清爽"),
                new WordDesc("绵柔", "口感绵软柔和"),
                new WordDesc("顺滑", "口感顺滑"),
                new WordDesc("甘润", "甘甜润泽"),
                new WordDesc("清甜回甘", "清甜且有回甘")
        )));

        vocabularies.addAll(buildAftertasteVocabularies(category, Arrays.asList(
                new WordDesc("回甘持久", "回甘持续时间长"),
                new WordDesc("生津明显", "生津效果显著"),
                new WordDesc("余韵悠长", "余味悠长"),
                new WordDesc("清甜回味", "回味清甜"),
                new WordDesc("齿颊留香", "牙齿和两颊留香"),
                new WordDesc("毫香明显", "毫香余韵明显"),
                new WordDesc("喉韵清润", "喉部清润有韵味"),
                new WordDesc("留香持久", "香气留存时间长")
        )));

        vocabularyService.batchCreateVocabularies(vocabularies);
        log.info("White tea vocabularies initialized: {} words", vocabularies.size());
    }

    private void initDarkTeaVocabularies() {
        String category = "黑茶";
        List<TastingVocabularyRequest> vocabularies = new ArrayList<>();

        vocabularies.addAll(buildAromaVocabularies(category, Arrays.asList(
                new WordDesc("陈香", "陈化后的香气"),
                new WordDesc("菌花香", "金花菌的香气"),
                new WordDesc("糯香", "糯米的香气"),
                new WordDesc("松烟香", "松烟熏制的香气"),
                new WordDesc("枣香", "红枣的甜香"),
                new WordDesc("药香", "药材的香气"),
                new WordDesc("木香", "木材的香气"),
                new WordDesc("焦糖香", "焦糖的甜香"),
                new WordDesc("槟榔香", "槟榔的香气"),
                new WordDesc("醇厚香", "醇厚的香气")
        )));

        vocabularies.addAll(buildLiquorColorVocabularies(category, Arrays.asList(
                new WordDesc("橙红", "橙红色"),
                new WordDesc("深红", "深红色"),
                new WordDesc("酒红", "红酒般的颜色"),
                new WordDesc("褐红", "褐红色"),
                new WordDesc("橙黄", "橙黄色"),
                new WordDesc("明亮", "汤色有光泽"),
                new WordDesc("红浓", "红而浓稠"),
                new WordDesc("透亮", "茶汤透澈明亮")
        )));

        vocabularies.addAll(buildTasteVocabularies(category, Arrays.asList(
                new WordDesc("醇厚", "口感饱满厚实"),
                new WordDesc("甘醇", "甘甜醇厚"),
                new WordDesc("顺滑", "口感顺滑"),
                new WordDesc("陈醇", "陈化后的醇厚感"),
                new WordDesc("饱满", "口感丰富饱满"),
                new WordDesc("甜润", "甜而润泽"),
                new WordDesc("生津", "口腔分泌唾液"),
                new WordDesc("回甘", "回味甘甜"),
                new WordDesc("绵柔", "口感绵软柔和"),
                new WordDesc("醇和", "醇和不刺激")
        )));

        vocabularies.addAll(buildAftertasteVocabularies(category, Arrays.asList(
                new WordDesc("回甘持久", "回甘持续时间长"),
                new WordDesc("生津明显", "生津效果显著"),
                new WordDesc("陈韵悠长", "陈韵持久"),
                new WordDesc("余韵悠长", "余味悠长"),
                new WordDesc("齿颊留香", "牙齿和两颊留香"),
                new WordDesc("喉韵明显", "喉部有明显韵味"),
                new WordDesc("茶气足", "茶气强劲有力"),
                new WordDesc("留香持久", "香气留存时间长")
        )));

        vocabularyService.batchCreateVocabularies(vocabularies);
        log.info("Dark tea vocabularies initialized: {} words", vocabularies.size());
    }

    private void initYellowTeaVocabularies() {
        String category = "黄茶";
        List<TastingVocabularyRequest> vocabularies = new ArrayList<>();

        vocabularies.addAll(buildAromaVocabularies(category, Arrays.asList(
                new WordDesc("清香", "清新淡雅的香气"),
                new WordDesc("毫香", "茶毫特有的清香"),
                new WordDesc("嫩香", "嫩芽的清鲜香气"),
                new WordDesc("花香", "清雅的花香"),
                new WordDesc("栗香", "熟板栗的香气"),
                new WordDesc("豆香", "豆类的清香"),
                new WordDesc("果香", "淡淡的水果香"),
                new WordDesc("甜香", "甜甜的香气"),
                new WordDesc("玉米香", "玉米的甜香"),
                new WordDesc("锅巴香", "锅巴的焦香")
        )));

        vocabularies.addAll(buildLiquorColorVocabularies(category, Arrays.asList(
                new WordDesc("杏黄", "杏黄色"),
                new WordDesc("浅黄", "淡黄色"),
                new WordDesc("黄绿", "黄中带绿"),
                new WordDesc("橙黄", "橙黄色"),
                new WordDesc("金黄", "金黄色"),
                new WordDesc("清澈", "茶汤清澈"),
                new WordDesc("明亮", "汤色有光泽"),
                new WordDesc("嫩绿微黄", "嫩绿中带微黄")
        )));

        vocabularies.addAll(buildTasteVocabularies(category, Arrays.asList(
                new WordDesc("鲜醇", "鲜爽醇厚"),
                new WordDesc("甘醇", "甘甜醇厚"),
                new WordDesc("清甜", "清爽甘甜"),
                new WordDesc("醇厚", "口感饱满厚实"),
                new WordDesc("鲜爽", "鲜醇爽口"),
                new WordDesc("清爽", "口感清爽"),
                new WordDesc("甘润", "甘甜润泽"),
                new WordDesc("绵柔", "口感绵软柔和"),
                new WordDesc("甜爽", "甜而爽口"),
                new WordDesc("回甘快", "回甘迅速明显")
        )));

        vocabularies.addAll(buildAftertasteVocabularies(category, Arrays.asList(
                new WordDesc("回甘持久", "回甘持续时间长"),
                new WordDesc("生津明显", "生津效果显著"),
                new WordDesc("余韵悠长", "余味悠长"),
                new WordDesc("清甜回味", "回味清甜"),
                new WordDesc("齿颊留香", "牙齿和两颊留香"),
                new WordDesc("甘润悠长", "甘润感持久"),
                new WordDesc("喉韵清润", "喉部清润有韵味"),
                new WordDesc("留香持久", "香气留存时间长")
        )));

        vocabularyService.batchCreateVocabularies(vocabularies);
        log.info("Yellow tea vocabularies initialized: {} words", vocabularies.size());
    }

    private void initFlowerTeaVocabularies() {
        String category = "花茶";
        List<TastingVocabularyRequest> vocabularies = new ArrayList<>();

        vocabularies.addAll(buildAromaVocabularies(category, Arrays.asList(
                new WordDesc("茉莉花香", "茉莉花的香气"),
                new WordDesc("玫瑰花香", "玫瑰花的香气"),
                new WordDesc("桂花香", "桂花的香气"),
                new WordDesc("兰花香", "兰花的香气"),
                new WordDesc("菊花香", "菊花的清香"),
                new WordDesc("茉莉甜香", "茉莉花的甜香"),
                new WordDesc("花香馥郁", "花香浓郁丰富"),
                new WordDesc("鲜灵花香", "鲜活灵动的花香"),
                new WordDesc("茶香花香", "茶香与花香交融"),
                new WordDesc("清香幽雅", "清香幽雅")
        )));

        vocabularies.addAll(buildLiquorColorVocabularies(category, Arrays.asList(
                new WordDesc("黄绿", "黄中带绿"),
                new WordDesc("浅黄", "淡黄色"),
                new WordDesc("杏黄", "杏黄色"),
                new WordDesc("橙黄", "橙黄色"),
                new WordDesc("嫩绿", "鲜嫩的绿色"),
                new WordDesc("清澈", "茶汤清澈"),
                new WordDesc("明亮", "汤色有光泽"),
                new WordDesc("透亮", "茶汤透澈明亮")
        )));

        vocabularies.addAll(buildTasteVocabularies(category, Arrays.asList(
                new WordDesc("鲜爽", "鲜醇爽口"),
                new WordDesc("清甜", "清爽甘甜"),
                new WordDesc("甘醇", "甘甜醇厚"),
                new WordDesc("醇厚", "口感饱满厚实"),
                new WordDesc("鲜醇", "鲜爽醇厚"),
                new WordDesc("花香明显", "花香味明显"),
                new WordDesc("清爽", "口感清爽"),
                new WordDesc("顺滑", "口感顺滑"),
                new WordDesc("甘润", "甘甜润泽"),
                new WordDesc("甜爽", "甜而爽口")
        )));

        vocabularies.addAll(buildAftertasteVocabularies(category, Arrays.asList(
                new WordDesc("回甘持久", "回甘持续时间长"),
                new WordDesc("花香回味", "回味有花香"),
                new WordDesc("生津明显", "生津效果显著"),
                new WordDesc("余韵悠长", "余味悠长"),
                new WordDesc("齿颊留香", "牙齿和两颊留香"),
                new WordDesc("清甜回味", "回味清甜"),
                new WordDesc("喉韵清润", "喉部清润有韵味"),
                new WordDesc("花香持久", "花香留存时间长")
        )));

        vocabularyService.batchCreateVocabularies(vocabularies);
        log.info("Flower tea vocabularies initialized: {} words", vocabularies.size());
    }

    private List<TastingVocabularyRequest> buildAromaVocabularies(String teaCategory, List<WordDesc> words) {
        return buildVocabularies("aroma", teaCategory, words);
    }

    private List<TastingVocabularyRequest> buildLiquorColorVocabularies(String teaCategory, List<WordDesc> words) {
        return buildVocabularies("liquor_color", teaCategory, words);
    }

    private List<TastingVocabularyRequest> buildTasteVocabularies(String teaCategory, List<WordDesc> words) {
        return buildVocabularies("taste", teaCategory, words);
    }

    private List<TastingVocabularyRequest> buildAftertasteVocabularies(String teaCategory, List<WordDesc> words) {
        return buildVocabularies("aftertaste", teaCategory, words);
    }

    private List<TastingVocabularyRequest> buildVocabularies(String type, String teaCategory, List<WordDesc> words) {
        List<TastingVocabularyRequest> result = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            WordDesc wd = words.get(i);
            TastingVocabularyRequest req = new TastingVocabularyRequest();
            req.setVocabularyType(type);
            req.setTeaCategory(teaCategory);
            req.setWord(wd.word);
            req.setDescription(wd.description);
            req.setSortOrder(i + 1);
            result.add(req);
        }
        return result;
    }

    private static class WordDesc {
        String word;
        String description;

        WordDesc(String word, String description) {
            this.word = word;
            this.description = description;
        }
    }
}
