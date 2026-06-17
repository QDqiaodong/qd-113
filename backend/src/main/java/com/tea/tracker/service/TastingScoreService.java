package com.tea.tracker.service;

import com.tea.tracker.dto.ScoreByCategory;
import com.tea.tracker.dto.ScoreByYear;
import com.tea.tracker.dto.TastingScoreSummary;
import com.tea.tracker.repository.TastingNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TastingScoreService {

    private final TastingNoteRepository tastingNoteRepository;

    @Transactional(readOnly = true)
    public List<TastingScoreSummary> getScoreSummaryByTea() {
        List<TastingScoreSummary> summaries = tastingNoteRepository.findScoreSummaryByTea();
        summaries.forEach(this::roundScores);
        log.info("Fetched tasting score summary for {} teas", summaries.size());
        return summaries;
    }

    @Transactional(readOnly = true)
    public TastingScoreSummary getScoreSummaryByTeaId(Long teaId) {
        TastingScoreSummary summary = tastingNoteRepository.findScoreSummaryByTeaId(teaId);
        if (summary != null) {
            roundScores(summary);
        }
        return summary;
    }

    @Transactional(readOnly = true)
    public List<ScoreByCategory> getScoreSummaryByCategory() {
        List<ScoreByCategory> summaries = tastingNoteRepository.findScoreSummaryByCategory();
        summaries.forEach(this::roundCategoryScores);
        log.info("Fetched tasting score summary for {} categories", summaries.size());
        return summaries;
    }

    @Transactional(readOnly = true)
    public List<ScoreByYear> getScoreSummaryByYear() {
        List<ScoreByYear> summaries = tastingNoteRepository.findScoreSummaryByYear();
        summaries.forEach(this::roundYearScores);
        log.info("Fetched tasting score summary for {} years", summaries.size());
        return summaries;
    }

    private void roundScores(TastingScoreSummary s) {
        s.setAvgAromaScore(round(s.getAvgAromaScore()));
        s.setAvgLiquorColorScore(round(s.getAvgLiquorColorScore()));
        s.setAvgTasteScore(round(s.getAvgTasteScore()));
        s.setAvgAftertasteScore(round(s.getAvgAftertasteScore()));
        s.setAvgOverallScore(round(s.getAvgOverallScore()));
    }

    private void roundCategoryScores(ScoreByCategory s) {
        s.setAvgAromaScore(round(s.getAvgAromaScore()));
        s.setAvgLiquorColorScore(round(s.getAvgLiquorColorScore()));
        s.setAvgTasteScore(round(s.getAvgTasteScore()));
        s.setAvgAftertasteScore(round(s.getAvgAftertasteScore()));
        s.setAvgOverallScore(round(s.getAvgOverallScore()));
    }

    private void roundYearScores(ScoreByYear s) {
        s.setAvgAromaScore(round(s.getAvgAromaScore()));
        s.setAvgLiquorColorScore(round(s.getAvgLiquorColorScore()));
        s.setAvgTasteScore(round(s.getAvgTasteScore()));
        s.setAvgAftertasteScore(round(s.getAvgAftertasteScore()));
        s.setAvgOverallScore(round(s.getAvgOverallScore()));
    }

    private BigDecimal round(BigDecimal value) {
        return value != null ? value.setScale(1, RoundingMode.HALF_UP) : null;
    }
}
