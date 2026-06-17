package com.tea.tracker.repository;

import com.tea.tracker.entity.TemplateVersion;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateVersionRepository extends JpaRepository<TemplateVersion, Long> {

    Optional<TemplateVersion> findByTeaCategory(String teaCategory);

    List<TemplateVersion> findAllByOrderByTeaCategoryAsc();
}
