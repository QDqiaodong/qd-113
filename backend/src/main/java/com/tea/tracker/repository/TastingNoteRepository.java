package com.tea.tracker.repository;

import com.tea.tracker.dto.ScoreByCategory;
import com.tea.tracker.dto.ScoreByYear;
import com.tea.tracker.dto.TastingScoreSummary;
import com.tea.tracker.entity.TastingNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TastingNoteRepository extends JpaRepository<TastingNote, Long> {

    List<TastingNote> findByTeaIdOrderByTastingDateDesc(Long teaId);

    List<TastingNote> findByTeaIdOrderByTastingDateAsc(Long teaId);

    List<TastingNote> findByTeaIdOrderByCreatedAtDesc(Long teaId);

    @Query("SELECT new com.tea.tracker.dto.TastingScoreSummary(" +
            "t.tea.id, t.tea.name, t.tea.teaCategory, t.tea.harvestYear, " +
            "CAST(AVG(CAST(t.aromaScore AS double)) AS bigdecimal), " +
            "CAST(AVG(CAST(t.liquorColorScore AS double)) AS bigdecimal), " +
            "CAST(AVG(CAST(t.tasteScore AS double)) AS bigdecimal), " +
            "CAST(AVG(CAST(t.aftertasteScore AS double)) AS bigdecimal), " +
            "CAST(AVG(CAST(t.overallScore AS double)) AS bigdecimal), " +
            "COUNT(t)) " +
            "FROM TastingNote t " +
            "GROUP BY t.tea.id, t.tea.name, t.tea.teaCategory, t.tea.harvestYear " +
            "ORDER BY t.tea.id")
    List<TastingScoreSummary> findScoreSummaryByTea();

    @Query("SELECT new com.tea.tracker.dto.TastingScoreSummary(" +
            "t.tea.id, t.tea.name, t.tea.teaCategory, t.tea.harvestYear, " +
            "CAST(AVG(CAST(t.aromaScore AS double)) AS bigdecimal), " +
            "CAST(AVG(CAST(t.liquorColorScore AS double)) AS bigdecimal), " +
            "CAST(AVG(CAST(t.tasteScore AS double)) AS bigdecimal), " +
            "CAST(AVG(CAST(t.aftertasteScore AS double)) AS bigdecimal), " +
            "CAST(AVG(CAST(t.overallScore AS double)) AS bigdecimal), " +
            "COUNT(t)) " +
            "FROM TastingNote t " +
            "WHERE t.tea.id = :teaId " +
            "GROUP BY t.tea.id, t.tea.name, t.tea.teaCategory, t.tea.harvestYear")
    TastingScoreSummary findScoreSummaryByTeaId(Long teaId);

    @Query("SELECT new com.tea.tracker.dto.ScoreByCategory(" +
            "t.tea.teaCategory, " +
            "CAST(AVG(CAST(t.aromaScore AS double)) AS bigdecimal), " +
            "CAST(AVG(CAST(t.liquorColorScore AS double)) AS bigdecimal), " +
            "CAST(AVG(CAST(t.tasteScore AS double)) AS bigdecimal), " +
            "CAST(AVG(CAST(t.aftertasteScore AS double)) AS bigdecimal), " +
            "CAST(AVG(CAST(t.overallScore AS double)) AS bigdecimal), " +
            "COUNT(t)) " +
            "FROM TastingNote t " +
            "GROUP BY t.tea.teaCategory " +
            "ORDER BY t.tea.teaCategory")
    List<ScoreByCategory> findScoreSummaryByCategory();

    @Query("SELECT new com.tea.tracker.dto.ScoreByYear(" +
            "t.tea.harvestYear, " +
            "CAST(AVG(CAST(t.aromaScore AS double)) AS bigdecimal), " +
            "CAST(AVG(CAST(t.liquorColorScore AS double)) AS bigdecimal), " +
            "CAST(AVG(CAST(t.tasteScore AS double)) AS bigdecimal), " +
            "CAST(AVG(CAST(t.aftertasteScore AS double)) AS bigdecimal), " +
            "CAST(AVG(CAST(t.overallScore AS double)) AS bigdecimal), " +
            "COUNT(t)) " +
            "FROM TastingNote t " +
            "GROUP BY t.tea.harvestYear " +
            "ORDER BY t.tea.harvestYear DESC")
    List<ScoreByYear> findScoreSummaryByYear();
}
