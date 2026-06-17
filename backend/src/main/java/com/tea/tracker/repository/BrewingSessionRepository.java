package com.tea.tracker.repository;

import com.tea.tracker.entity.BrewingSession;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrewingSessionRepository extends JpaRepository<BrewingSession, Long> {

    List<BrewingSession> findByTeaIdOrderBySessionDateDesc(Long teaId);

    List<BrewingSession> findByTeaIdOrderByCreatedAtDesc(Long teaId);
}
