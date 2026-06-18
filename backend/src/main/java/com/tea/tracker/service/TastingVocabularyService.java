package com.tea.tracker.service;

import com.tea.tracker.dto.TastingVocabularyRequest;
import com.tea.tracker.dto.TastingVocabularyResponse;
import com.tea.tracker.entity.TastingVocabulary;
import com.tea.tracker.repository.TastingVocabularyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TastingVocabularyService {

    private final TastingVocabularyRepository vocabularyRepository;

    @Transactional(readOnly = true)
    public List<TastingVocabularyResponse> getAllVocabularies() {
        return vocabularyRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TastingVocabularyResponse getVocabularyById(Long id) {
        TastingVocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("词汇不存在: " + id));
        return toResponse(vocabulary);
    }

    @Transactional(readOnly = true)
    public List<TastingVocabularyResponse> getVocabulariesByType(String vocabularyType) {
        return vocabularyRepository.findByVocabularyTypeOrderBySortOrderAscIdAsc(vocabularyType)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TastingVocabularyResponse> getVocabulariesByTeaCategory(String teaCategory) {
        return vocabularyRepository.findByTeaCategoryOrderBySortOrderAscIdAsc(teaCategory)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TastingVocabularyResponse> getVocabulariesByTypeAndTeaCategory(String vocabularyType, String teaCategory) {
        return vocabularyRepository.findByVocabularyTypeAndTeaCategoryOrderBySortOrderAscIdAsc(vocabularyType, teaCategory)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TastingVocabularyResponse createVocabulary(TastingVocabularyRequest request) {
        if (vocabularyRepository.findByVocabularyTypeAndTeaCategoryAndWord(
                request.getVocabularyType(), request.getTeaCategory(), request.getWord()).isPresent()) {
            throw new IllegalArgumentException("该词汇已存在: " + request.getWord());
        }

        TastingVocabulary vocabulary = new TastingVocabulary();
        mapRequestToEntity(request, vocabulary);
        TastingVocabulary saved = vocabularyRepository.save(vocabulary);
        log.info("Created tasting vocabulary: {} (type={}, category={})", saved.getWord(), saved.getVocabularyType(), saved.getTeaCategory());
        return toResponse(saved);
    }

    @Transactional
    public TastingVocabularyResponse updateVocabulary(Long id, TastingVocabularyRequest request) {
        TastingVocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("词汇不存在: " + id));

        if (!vocabulary.getWord().equals(request.getWord()) &&
                vocabularyRepository.findByVocabularyTypeAndTeaCategoryAndWord(
                        request.getVocabularyType(), request.getTeaCategory(), request.getWord()).isPresent()) {
            throw new IllegalArgumentException("该词汇已存在: " + request.getWord());
        }

        mapRequestToEntity(request, vocabulary);
        TastingVocabulary updated = vocabularyRepository.save(vocabulary);
        log.info("Updated tasting vocabulary: {}", updated.getId());
        return toResponse(updated);
    }

    @Transactional
    public void deleteVocabulary(Long id) {
        if (!vocabularyRepository.existsById(id)) {
            throw new EntityNotFoundException("词汇不存在: " + id);
        }
        vocabularyRepository.deleteById(id);
        log.info("Deleted tasting vocabulary: {}", id);
    }

    @Transactional
    public void batchCreateVocabularies(List<TastingVocabularyRequest> requests) {
        for (TastingVocabularyRequest request : requests) {
            if (vocabularyRepository.findByVocabularyTypeAndTeaCategoryAndWord(
                    request.getVocabularyType(), request.getTeaCategory(), request.getWord()).isEmpty()) {
                TastingVocabulary vocabulary = new TastingVocabulary();
                mapRequestToEntity(request, vocabulary);
                vocabularyRepository.save(vocabulary);
            }
        }
        log.info("Batch created/skipped {} tasting vocabularies", requests.size());
    }

    private void mapRequestToEntity(TastingVocabularyRequest request, TastingVocabulary vocabulary) {
        vocabulary.setVocabularyType(request.getVocabularyType());
        vocabulary.setTeaCategory(request.getTeaCategory());
        vocabulary.setWord(request.getWord());
        vocabulary.setDescription(request.getDescription());
        vocabulary.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
    }

    private TastingVocabularyResponse toResponse(TastingVocabulary vocabulary) {
        TastingVocabularyResponse resp = new TastingVocabularyResponse();
        resp.setId(vocabulary.getId());
        resp.setVocabularyType(vocabulary.getVocabularyType());
        resp.setTeaCategory(vocabulary.getTeaCategory());
        resp.setWord(vocabulary.getWord());
        resp.setDescription(vocabulary.getDescription());
        resp.setSortOrder(vocabulary.getSortOrder());
        resp.setCreatedAt(vocabulary.getCreatedAt());
        resp.setUpdatedAt(vocabulary.getUpdatedAt());
        return resp;
    }
}
