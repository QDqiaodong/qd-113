package com.tea.tracker.repository;

import com.tea.tracker.entity.TastingVocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TastingVocabularyRepository extends JpaRepository<TastingVocabulary, Long> {

    List<TastingVocabulary> findByVocabularyTypeOrderBySortOrderAscIdAsc(String vocabularyType);

    List<TastingVocabulary> findByTeaCategoryOrderBySortOrderAscIdAsc(String teaCategory);

    List<TastingVocabulary> findByVocabularyTypeAndTeaCategoryOrderBySortOrderAscIdAsc(String vocabularyType, String teaCategory);

    Optional<TastingVocabulary> findByVocabularyTypeAndTeaCategoryAndWord(String vocabularyType, String teaCategory, String word);

    void deleteByVocabularyTypeAndTeaCategory(String vocabularyType, String teaCategory);
}
