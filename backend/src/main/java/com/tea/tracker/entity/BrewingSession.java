package com.tea.tracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "brewing_session")
public class BrewingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tea_id", nullable = false)
    private Tea tea;

    @Column(name = "brewing_param_id")
    private Long brewingParamId;

    @Column(name = "actual_water_temperature")
    private Integer actualWaterTemperature;

    @Column(name = "first_infusion_time")
    private Integer firstInfusionTime;

    @Column(name = "second_infusion_time")
    private Integer secondInfusionTime;

    @Column(name = "third_infusion_time")
    private Integer thirdInfusionTime;

    @Column(name = "subsequent_infusion_time")
    private Integer subsequentInfusionTime;

    @Column(name = "taste_impression", length = 1000)
    private String tasteImpression;

    @Column(name = "stock_deducted")
    private Boolean stockDeducted;

    @Column(name = "stock_amount", precision = 10, scale = 2)
    private BigDecimal stockAmount;

    @Column(name = "storage_record_id")
    private Long storageRecordId;

    @Column(name = "session_date")
    private LocalDateTime sessionDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
