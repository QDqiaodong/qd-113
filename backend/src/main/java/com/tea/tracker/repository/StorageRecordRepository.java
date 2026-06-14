package com.tea.tracker.repository;

import com.tea.tracker.entity.StorageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageRecordRepository extends JpaRepository<StorageRecord, Long> {

    List<StorageRecord> findByTeaIdOrderByRecordDateDesc(Long teaId);

    List<StorageRecord> findByTeaIdOrderByCreatedAtDesc(Long teaId);
}
