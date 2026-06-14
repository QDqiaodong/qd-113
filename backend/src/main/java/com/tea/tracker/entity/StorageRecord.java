package com.tea.tracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "storage_record")
public class StorageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tea_id", nullable = false)
    private Tea tea;

    @Column(name = "storage_location", length = 100)
    private String storageLocation;

    @Column(name = "temperature")
    private BigDecimal temperature;

    @Column(name = "humidity")
    private BigDecimal humidity;

    @Column(name = "seal_condition", length = 50)
    private String sealCondition;

    @Column(name = "stock_change", precision = 10, scale = 2)
    private BigDecimal stockChange;

    @Column(name = "current_stock", precision = 10, scale = 2)
    private BigDecimal currentStock;

    @Column(name = "record_date")
    private LocalDateTime recordDate;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
