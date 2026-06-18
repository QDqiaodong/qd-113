package com.tea.tracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "param_sync_record")
public class ParamSyncRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_tea_id", nullable = false)
    private Long sourceTeaId;

    @Column(name = "source_tea_name", nullable = false, length = 100)
    private String sourceTeaName;

    @Column(name = "source_param_id", nullable = false)
    private Long sourceParamId;

    @Column(name = "source_param_name", length = 100)
    private String sourceParamName;

    @Column(name = "target_tea_id", nullable = false)
    private Long targetTeaId;

    @Column(name = "target_tea_name", nullable = false, length = 100)
    private String targetTeaName;

    @Column(name = "target_param_id")
    private Long targetParamId;

    @Column(name = "target_param_name", length = 100)
    private String targetParamName;

    @Column(name = "tea_category", nullable = false, length = 50)
    private String teaCategory;

    @Column(name = "field_differences", columnDefinition = "TEXT")
    private String fieldDifferences;

    @Column(name = "sync_type", nullable = false, length = 20)
    private String syncType = "DEFAULT";

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
