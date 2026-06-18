package com.tea.tracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tasting_vocabulary")
public class TastingVocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vocabulary_type", nullable = false, length = 20)
    private String vocabularyType;

    @Column(name = "tea_category", nullable = false, length = 50)
    private String teaCategory;

    @Column(nullable = false, length = 50)
    private String word;

    @Column(length = 200)
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
