package com.tea.tracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "storage_risk_action")
public class StorageRiskAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tea_id", nullable = false)
    private Tea tea;

    @Column(name = "suitability_id")
    private Long suitabilityId;

    @Column(name = "storage_record_id")
    private Long storageRecordId;

    @Column(name = "risk_type", nullable = false, length = 50)
    private String riskType;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;

    @Column(name = "risk_description", length = 500)
    private String riskDescription;

    @Column(name = "temperature_before", precision = 5, scale = 2)
    private BigDecimal temperatureBefore;

    @Column(name = "humidity_before", precision = 5, scale = 2)
    private BigDecimal humidityBefore;

    @Column(name = "seal_before", length = 50)
    private String sealBefore;

    @Column(name = "action_type", length = 100)
    private String actionType;

    @Column(name = "action_description", length = 1000)
    private String actionDescription;

    @Column(name = "action_by", length = 100)
    private String actionBy;

    @Column(name = "action_date")
    private LocalDateTime actionDate;

    @Column(name = "temperature_after", precision = 5, scale = 2)
    private BigDecimal temperatureAfter;

    @Column(name = "humidity_after", precision = 5, scale = 2)
    private BigDecimal humidityAfter;

    @Column(name = "seal_after", length = 50)
    private String sealAfter;

    @Column(name = "result_status", length = 20)
    private String resultStatus;

    @Column(name = "result_description", length = 500)
    private String resultDescription;

    @Column(name = "follow_up_date")
    private LocalDateTime followUpDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
