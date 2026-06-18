package com.tea.tracker.repository;

import com.tea.tracker.entity.StorageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StorageRecordRepository extends JpaRepository<StorageRecord, Long> {

    List<StorageRecord> findByTeaIdOrderByRecordDateDesc(Long teaId);

    List<StorageRecord> findByTeaIdOrderByCreatedAtDesc(Long teaId);

    List<StorageRecord> findByTeaIdOrderByRecordDateAsc(Long teaId);

    Optional<StorageRecord> findTopByTeaIdOrderByRecordDateDesc(Long teaId);

    Optional<StorageRecord> findByBrewingSessionId(Long brewingSessionId);

    void deleteByBrewingSessionId(Long brewingSessionId);
}
