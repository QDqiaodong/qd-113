package com.tea.tracker.repository;

import com.tea.tracker.entity.BrewingParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrewingParamRepository extends JpaRepository<BrewingParam, Long> {

    List<BrewingParam> findByTeaIdOrderByCreatedAtDesc(Long teaId);

    List<BrewingParam> findByTeaIdAndIsDefaultTrue(Long teaId);

    @Modifying
    @Query("UPDATE BrewingParam b SET b.isDefault = false WHERE b.tea.id = :teaId AND b.isDefault = true")
    int clearDefaultByTeaId(@Param("teaId") Long teaId);

    Optional<BrewingParam> findFirstByTeaIdAndIsDefaultTrueOrderByCreatedAtAsc(Long teaId);
}
