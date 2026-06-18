package com.tea.tracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "storage_suitability")
public class StorageSuitability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tea_id", nullable = false)
    private Tea tea;

    @Column(name = "tea_category", nullable = false, length = 50)
    private String teaCategory;

    @Column(name = "storage_method", length = 100)
    private String storageMethod;

    @Column(name = "temperature", precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "humidity", precision = 5, scale = 2)
    private BigDecimal humidity;

    @Column(name = "seal_condition", length = 50)
    private String sealCondition;

    @Column(name = "total_score")
    private Integer totalScore;

    @Column(name = "temperature_score")
    private Integer temperatureScore;

    @Column(name = "humidity_score")
    private Integer humidityScore;

    @Column(name = "seal_score")
    private Integer sealScore;

    @Column(name = "storage_method_score")
    private Integer storageMethodScore;

    @Column(name = "suitability_level", length = 20)
    private String suitabilityLevel;

    @Column(name = "suggestions", length = 1000)
    private String suggestions;

    @Column(name = "record_date")
    private LocalDateTime recordDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
