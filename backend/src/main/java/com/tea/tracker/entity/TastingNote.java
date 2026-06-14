package com.tea.tracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tasting_note")
public class TastingNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tea_id", nullable = false)
    private Tea tea;

    @Column(name = "brewing_method", length = 100)
    private String brewingMethod;

    @Column(name = "aroma_score")
    private Integer aromaScore;

    @Column(name = "aroma_desc", length = 500)
    private String aromaDesc;

    @Column(name = "liquor_color_score")
    private Integer liquorColorScore;

    @Column(name = "liquor_color_desc", length = 500)
    private String liquorColorDesc;

    @Column(name = "taste_score")
    private Integer tasteScore;

    @Column(name = "taste_desc", length = 500)
    private String tasteDesc;

    @Column(name = "aftertaste_score")
    private Integer aftertasteScore;

    @Column(name = "aftertaste_desc", length = 500)
    private String aftertasteDesc;

    @Column(name = "overall_score")
    private Integer overallScore;

    @Column(length = 1000)
    private String impression;

    @Column(name = "tasting_date")
    private LocalDateTime tastingDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
