package com.tea.tracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "tea")
public class Tea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "tea_category", nullable = false, length = 50)
    private String teaCategory;

    @Column(name = "origin_region", nullable = false, length = 100)
    private String originRegion;

    @Column(name = "harvest_year")
    private Integer harvestYear;

    @Column(name = "storage_method", length = 100)
    private String storageMethod;

    @Column(name = "current_stock", precision = 10, scale = 2)
    private BigDecimal currentStock;

    @Column(name = "stock_unit", length = 20)
    private String stockUnit;

    @Column(length = 500)
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "tea", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BrewingParam> brewingParams = new ArrayList<>();

    @OneToMany(mappedBy = "tea", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StorageRecord> storageRecords = new ArrayList<>();

    @OneToMany(mappedBy = "tea", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TastingNote> tastingNotes = new ArrayList<>();

    @OneToMany(mappedBy = "tea", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BrewingSession> brewingSessions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
