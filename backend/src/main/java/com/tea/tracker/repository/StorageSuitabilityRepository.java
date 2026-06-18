package com.tea.tracker.repository;

import com.tea.tracker.entity.StorageSuitability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageSuitabilityRepository extends JpaRepository<StorageSuitability, Long> {

    List<StorageSuitability> findByTeaIdOrderByRecordDateDesc(Long teaId);

    List<StorageSuitability> findByTeaIdOrderByRecordDateAsc(Long teaId);
}
