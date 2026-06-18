package com.tea.tracker.repository;

import com.tea.tracker.entity.ParamSyncRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParamSyncRecordRepository extends JpaRepository<ParamSyncRecord, Long> {

    List<ParamSyncRecord> findByTargetTeaIdOrderBySyncedAtDesc(Long targetTeaId);

    List<ParamSyncRecord> findBySourceTeaIdOrderBySyncedAtDesc(Long sourceTeaId);

    List<ParamSyncRecord> findByTeaCategoryOrderBySyncedAtDesc(String teaCategory);
}
