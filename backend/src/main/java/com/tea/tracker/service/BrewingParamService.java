package com.tea.tracker.service;

import com.tea.tracker.dto.BrewingParamRequest;
import com.tea.tracker.dto.BrewingParamResponse;
import com.tea.tracker.entity.BrewingParam;
import com.tea.tracker.entity.Tea;
import com.tea.tracker.repository.BrewingParamRepository;
import com.tea.tracker.repository.TeaRepository;
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
public class BrewingParamService {

    private final BrewingParamRepository brewingParamRepository;
    private final TeaRepository teaRepository;

    @Transactional
    public BrewingParamResponse createBrewingParam(Long teaId, BrewingParamRequest request) {
        Tea tea = teaRepository.findById(teaId)
                .orElseThrow(() -> new EntityNotFoundException("茶叶档案不存在: " + teaId));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            brewingParamRepository.findByTeaIdAndIsDefaultTrue(teaId)
                    .forEach(p -> {
                        p.setIsDefault(false);
                        brewingParamRepository.save(p);
                    });
        }

        BrewingParam param = new BrewingParam();
        mapRequestToEntity(request, param);
        param.setTea(tea);
        BrewingParam saved = brewingParamRepository.save(param);
        log.info("Created brewing param for tea {} (id={})", tea.getName(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public BrewingParamResponse updateBrewingParam(Long id, BrewingParamRequest request) {
        BrewingParam param = brewingParamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("冲泡参数不存在: " + id));

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(param.getIsDefault())) {
            brewingParamRepository.findByTeaIdAndIsDefaultTrue(param.getTea().getId())
                    .forEach(p -> {
                        p.setIsDefault(false);
                        brewingParamRepository.save(p);
                    });
        }

        mapRequestToEntity(request, param);
        BrewingParam updated = brewingParamRepository.save(param);
        return toResponse(updated);
    }

    @Transactional
    public void deleteBrewingParam(Long id) {
        if (!brewingParamRepository.existsById(id)) {
            throw new EntityNotFoundException("冲泡参数不存在: " + id);
        }
        brewingParamRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<BrewingParamResponse> getBrewingParamsByTeaId(Long teaId) {
        return brewingParamRepository.findByTeaIdOrderByCreatedAtDesc(teaId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BrewingParamResponse getBrewingParamById(Long id) {
        BrewingParam param = brewingParamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("冲泡参数不存在: " + id));
        return toResponse(param);
    }

    private void mapRequestToEntity(BrewingParamRequest request, BrewingParam param) {
        param.setParamName(request.getParamName());
        param.setWaterTemperature(request.getWaterTemperature());
        param.setTeaAmount(request.getTeaAmount());
        param.setTeaRatio(request.getTeaRatio());
        param.setWaterAmount(request.getWaterAmount());
        param.setFirstInfusionTime(request.getFirstInfusionTime());
        param.setSecondInfusionTime(request.getSecondInfusionTime());
        param.setThirdInfusionTime(request.getThirdInfusionTime());
        param.setSubsequentInfusionTime(request.getSubsequentInfusionTime());
        param.setTotalInfusions(request.getTotalInfusions());
        param.setWaterQuality(request.getWaterQuality());
        param.setNotes(request.getNotes());
        param.setIsDefault(request.getIsDefault());
    }

    private BrewingParamResponse toResponse(BrewingParam param) {
        BrewingParamResponse resp = new BrewingParamResponse();
        resp.setId(param.getId());
        resp.setTeaId(param.getTea().getId());
        resp.setTeaName(param.getTea().getName());
        resp.setParamName(param.getParamName());
        resp.setWaterTemperature(param.getWaterTemperature());
        resp.setTeaAmount(param.getTeaAmount());
        resp.setTeaRatio(param.getTeaRatio());
        resp.setWaterAmount(param.getWaterAmount());
        resp.setFirstInfusionTime(param.getFirstInfusionTime());
        resp.setSecondInfusionTime(param.getSecondInfusionTime());
        resp.setThirdInfusionTime(param.getThirdInfusionTime());
        resp.setSubsequentInfusionTime(param.getSubsequentInfusionTime());
        resp.setTotalInfusions(param.getTotalInfusions());
        resp.setWaterQuality(param.getWaterQuality());
        resp.setNotes(param.getNotes());
        resp.setIsDefault(param.getIsDefault());
        resp.setCreatedAt(param.getCreatedAt());
        resp.setUpdatedAt(param.getUpdatedAt());
        return resp;
    }
}
