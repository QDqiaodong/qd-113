package com.tea.tracker.repository;

import com.tea.tracker.entity.TastingNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TastingNoteRepository extends JpaRepository<TastingNote, Long> {

    List<TastingNote> findByTeaIdOrderByTastingDateDesc(Long teaId);

    List<TastingNote> findByTeaIdOrderByCreatedAtDesc(Long teaId);
}
