package com.hackathon.sla_service.service.impl;

import com.hackathon.sla_service.config.SlaConfigProperties;
import com.hackathon.sla_service.dto.common.SummaryMetricDto;
import com.hackathon.sla_service.dto.common.SummaryPeriodDto;
import com.hackathon.sla_service.dto.response.SlaSummaryResponse;
import com.hackathon.sla_service.repository.SlaRepository;
import com.hackathon.sla_service.service.SlaFullSummaryService;
import com.hackathon.sla_service.service.calculator.SlaMetricCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SlaFullSummaryServiceImpl implements SlaFullSummaryService {

    private final SlaRepository slaRepository;
    private final SlaMetricCalculator slaMetricCalculator;
    private final SlaConfigProperties slaConfig;

    public SlaFullSummaryServiceImpl(SlaRepository slaRepository,
                                     SlaMetricCalculator slaMetricCalculator,
                                     SlaConfigProperties slaConfig) {
        this.slaRepository = slaRepository;
        this.slaMetricCalculator = slaMetricCalculator;
        this.slaConfig = slaConfig;
    }

    @Override
    public SlaSummaryResponse getFullSummary(LocalDate dateFrom, LocalDate dateTo) {
        int fullCycleDays = slaConfig.getFullCycleDays();

        SummaryMetricDto fullCycleMetric = slaMetricCalculator.calculateDays(
                slaRepository.getFullCycleValues(dateFrom, dateTo),
                fullCycleDays
        );

        Map<String, SummaryMetricDto> metrics = new LinkedHashMap<>();
        metrics.put("full_cycle", fullCycleMetric);

        SlaSummaryResponse response = new SlaSummaryResponse();
        response.setPeriod(new SummaryPeriodDto(dateFrom, dateTo));
        response.setPipeline("full");
        response.setMetrics(metrics);

        return response;
    }
}