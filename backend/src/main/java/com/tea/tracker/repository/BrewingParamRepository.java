package com.tea.tracker.repository;

import com.tea.tracker.entity.BrewingParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrewingParamRepository extends JpaRepository<BrewingParam, Long> {

    List<BrewingParam> findByTeaIdOrderByCreatedAtDesc(Long teaId);

    List<BrewingParam> findByTeaIdAndIsDefaultTrue(Long teaId);
}
