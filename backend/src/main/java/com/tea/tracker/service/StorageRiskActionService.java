package com.tea.tracker.service;

import com.tea.tracker.dto.StorageRiskActionRequest;
import com.tea.tracker.dto.StorageRiskActionResponse;
import com.tea.tracker.entity.StorageRiskAction;
import com.tea.tracker.entity.Tea;
import com.tea.tracker.repository.StorageRiskActionRepository;
import com.tea.tracker.repository.TeaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageRiskActionService {

    private final StorageRiskActionRepository storageRiskActionRepository;
    private final TeaRepository teaRepository;

    @Transactional
    public StorageRiskActionResponse createRiskAction(Long teaId, StorageRiskActionRequest request) {
        Tea tea = teaRepository.findById(teaId)
                .orElseThrow(() -> new EntityNotFoundException("茶叶档案不存在: " + teaId));

        StorageRiskAction action = new StorageRiskAction();
        action.setTea(tea);
        action.setSuitabilityId(request.getSuitabilityId());
        action.setStorageRecordId(request.getStorageRecordId());
        action.setRiskType(request.getRiskType());
        action.setRiskLevel(request.getRiskLevel());
        action.setRiskDescription(request.getRiskDescription());
        action.setTemperatureBefore(request.getTemperatureBefore());
        action.setHumidityBefore(request.getHumidityBefore());
        action.setSealBefore(request.getSealBefore());
        action.setActionType(request.getActionType());
        action.setActionDescription(request.getActionDescription());
        action.setActionBy(request.getActionBy());
        action.setActionDate(request.getActionDate() != null ? request.getActionDate() : LocalDateTime.now());
        action.setTemperatureAfter(request.getTemperatureAfter());
        action.setHumidityAfter(request.getHumidityAfter());
        action.setSealAfter(request.getSealAfter());
        action.setResultStatus(request.getResultStatus() != null ? request.getResultStatus() : "处理中");
        action.setResultDescription(request.getResultDescription());
        action.setFollowUpDate(request.getFollowUpDate());

        StorageRiskAction saved = storageRiskActionRepository.save(action);
        log.info("Created storage risk action for tea {} (id={}), riskType={}, level={}",
                tea.getName(), saved.getId(), saved.getRiskType(), saved.getRiskLevel());

        return toResponse(saved);
    }

    @Transactional
    public StorageRiskActionResponse updateRiskAction(Long teaId, Long id, StorageRiskActionRequest request) {
        StorageRiskAction action = storageRiskActionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("风险处置记录不存在: " + id));

        if (!action.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("风险处置记录不属于当前茶叶");
        }

        if (request.getSuitabilityId() != null) {
            action.setSuitabilityId(request.getSuitabilityId());
        }
        if (request.getStorageRecordId() != null) {
            action.setStorageRecordId(request.getStorageRecordId());
        }
        if (request.getRiskType() != null) {
            action.setRiskType(request.getRiskType());
        }
        if (request.getRiskLevel() != null) {
            action.setRiskLevel(request.getRiskLevel());
        }
        if (request.getRiskDescription() != null) {
            action.setRiskDescription(request.getRiskDescription());
        }
        if (request.getTemperatureBefore() != null) {
            action.setTemperatureBefore(request.getTemperatureBefore());
        }
        if (request.getHumidityBefore() != null) {
            action.setHumidityBefore(request.getHumidityBefore());
        }
        if (request.getSealBefore() != null) {
            action.setSealBefore(request.getSealBefore());
        }
        if (request.getActionType() != null) {
            action.setActionType(request.getActionType());
        }
        if (request.getActionDescription() != null) {
            action.setActionDescription(request.getActionDescription());
        }
        if (request.getActionBy() != null) {
            action.setActionBy(request.getActionBy());
        }
        if (request.getActionDate() != null) {
            action.setActionDate(request.getActionDate());
        }
        if (request.getTemperatureAfter() != null) {
            action.setTemperatureAfter(request.getTemperatureAfter());
        }
        if (request.getHumidityAfter() != null) {
            action.setHumidityAfter(request.getHumidityAfter());
        }
        if (request.getSealAfter() != null) {
            action.setSealAfter(request.getSealAfter());
        }
        if (request.getResultStatus() != null) {
            action.setResultStatus(request.getResultStatus());
        }
        if (request.getResultDescription() != null) {
            action.setResultDescription(request.getResultDescription());
        }
        if (request.getFollowUpDate() != null) {
            action.setFollowUpDate(request.getFollowUpDate());
        }

        StorageRiskAction saved = storageRiskActionRepository.save(action);
        log.info("Updated storage risk action id={} for tea {}", id, action.getTea().getName());

        return toResponse(saved);
    }

    @Transactional
    public StorageRiskActionResponse markAsComplete(Long teaId, Long id, StorageRiskActionRequest request) {
        StorageRiskAction action = storageRiskActionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("风险处置记录不存在: " + id));

        if (!action.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("风险处置记录不属于当前茶叶");
        }

        action.setResultStatus("已完成");
        if (request != null) {
            if (request.getTemperatureAfter() != null) {
                action.setTemperatureAfter(request.getTemperatureAfter());
            }
            if (request.getHumidityAfter() != null) {
                action.setHumidityAfter(request.getHumidityAfter());
            }
            if (request.getSealAfter() != null) {
                action.setSealAfter(request.getSealAfter());
            }
            if (request.getResultDescription() != null && !request.getResultDescription().isBlank()) {
                action.setResultDescription(request.getResultDescription());
            }
            if (request.getFollowUpDate() != null) {
                action.setFollowUpDate(request.getFollowUpDate());
            }
        }

        StorageRiskAction saved = storageRiskActionRepository.save(action);
        log.info("Marked storage risk action id={} as completed for tea {}", id, action.getTea().getName());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StorageRiskActionResponse> getRiskActions(Long teaId) {
        return storageRiskActionRepository.findByTeaIdOrderByCreatedAtDesc(teaId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StorageRiskActionResponse> getRiskActionsByStatus(Long teaId, String resultStatus) {
        return storageRiskActionRepository.findByTeaIdAndResultStatusOrderByCreatedAtDesc(teaId, resultStatus)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StorageRiskActionResponse> getRiskActionsBySuitability(Long suitabilityId) {
        return storageRiskActionRepository.findBySuitabilityIdOrderByCreatedAtDesc(suitabilityId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StorageRiskActionResponse getRiskActionById(Long teaId, Long id) {
        StorageRiskAction action = storageRiskActionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("风险处置记录不存在: " + id));

        if (!action.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("风险处置记录不属于当前茶叶");
        }

        return toResponse(action);
    }

    @Transactional
    public void deleteRiskAction(Long teaId, Long id) {
        StorageRiskAction action = storageRiskActionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("风险处置记录不存在: " + id));

        if (!action.getTea().getId().equals(teaId)) {
            throw new IllegalArgumentException("风险处置记录不属于当前茶叶");
        }

        storageRiskActionRepository.delete(action);
        log.info("Deleted storage risk action id={} for tea {}", id, action.getTea().getName());
    }

    private StorageRiskActionResponse toResponse(StorageRiskAction action) {
        StorageRiskActionResponse resp = new StorageRiskActionResponse();
        resp.setId(action.getId());
        resp.setTeaId(action.getTea().getId());
        resp.setTeaName(action.getTea().getName());
        resp.setSuitabilityId(action.getSuitabilityId());
        resp.setStorageRecordId(action.getStorageRecordId());
        resp.setRiskType(action.getRiskType());
        resp.setRiskLevel(action.getRiskLevel());
        resp.setRiskDescription(action.getRiskDescription());
        resp.setTemperatureBefore(action.getTemperatureBefore());
        resp.setHumidityBefore(action.getHumidityBefore());
        resp.setSealBefore(action.getSealBefore());
        resp.setActionType(action.getActionType());
        resp.setActionDescription(action.getActionDescription());
        resp.setActionBy(action.getActionBy());
        resp.setActionDate(action.getActionDate());
        resp.setTemperatureAfter(action.getTemperatureAfter());
        resp.setHumidityAfter(action.getHumidityAfter());
        resp.setSealAfter(action.getSealAfter());
        resp.setResultStatus(action.getResultStatus());
        resp.setResultDescription(action.getResultDescription());
        resp.setFollowUpDate(action.getFollowUpDate());
        resp.setCreatedAt(action.getCreatedAt());
        resp.setUpdatedAt(action.getUpdatedAt());
        return resp;
    }
}
