package com.tea.tracker.repository;

import com.tea.tracker.entity.Tea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeaRepository extends JpaRepository<Tea, Long> {

    List<Tea> findByTeaCategoryOrderByCreatedAtDesc(String teaCategory);

    List<Tea> findByOriginRegionOrderByCreatedAtDesc(String originRegion);

    List<Tea> findAllByOrderByCreatedAtDesc();

    @Query("SELECT DISTINCT t.teaCategory FROM Tea t ORDER BY t.teaCategory")
    List<String> findAllCategories();

    @Query("SELECT DISTINCT t.originRegion FROM Tea t ORDER BY t.originRegion")
    List<String> findAllRegions();

    List<Tea> findByNameContainingIgnoreCase(String keyword);
}
