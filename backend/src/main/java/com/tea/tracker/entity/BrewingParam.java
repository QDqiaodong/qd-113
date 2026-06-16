package com.tea.tracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "brewing_param")
public class BrewingParam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tea_id", nullable = false)
    private Tea tea;

    @Column(name = "param_name", length = 100)
    private String paramName;

    @Column(name = "brewing_method", length = 100)
    private String brewingMethod;

    @Column(name = "water_temperature", nullable = false)
    private Integer waterTemperature;

    @Column(name = "tea_amount", precision = 5, scale = 2, nullable = false)
    private BigDecimal teaAmount;

    @Column(name = "tea_ratio", length = 50)
    private String teaRatio;

    @Column(name = "water_amount", precision = 6, scale = 2)
    private BigDecimal waterAmount;

    @Column(name = "first_infusion_time", nullable = false)
    private Integer firstInfusionTime;

    @Column(name = "second_infusion_time")
    private Integer secondInfusionTime;

    @Column(name = "third_infusion_time")
    private Integer thirdInfusionTime;

    @Column(name = "subsequent_infusion_time")
    private Integer subsequentInfusionTime;

    @Column(name = "total_infusions")
    private Integer totalInfusions;

    @Column(name = "water_quality", length = 50)
    private String waterQuality;

    @Column(length = 500)
    private String notes;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
