package com.tea.tracker.repository;

import com.tea.tracker.entity.StorageRiskAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageRiskActionRepository extends JpaRepository<StorageRiskAction, Long> {

    List<StorageRiskAction> findByTeaIdOrderByCreatedAtDesc(Long teaId);

    List<StorageRiskAction> findByTeaIdAndResultStatusOrderByCreatedAtDesc(Long teaId, String resultStatus);

    List<StorageRiskAction> findBySuitabilityIdOrderByCreatedAtDesc(Long suitabilityId);

    List<StorageRiskAction> findByStorageRecordIdOrderByCreatedAtDesc(Long storageRecordId);
}
