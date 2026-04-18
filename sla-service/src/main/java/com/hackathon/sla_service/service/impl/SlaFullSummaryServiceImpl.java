package com.hackathon.sla_service.service.impl;

import com.hackathon.sla_service.dto.common.BreachDistributionDto;
import com.hackathon.sla_service.dto.common.SummaryMetricDto;
import com.hackathon.sla_service.dto.common.SummaryPeriodDto;
import com.hackathon.sla_service.dto.response.SlaSummaryResponse;
import com.hackathon.sla_service.service.SlaFullSummaryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SlaFullSummaryServiceImpl implements SlaFullSummaryService {

    @Value("${sla.full-cycle-days:16}")
    private int fullCycleDays;

    @Override
    public SlaSummaryResponse getFullSummary(LocalDate dateFrom, LocalDate dateTo) {
        SummaryMetricDto fullCycleMetric = buildMockFullCycleMetric();

        Map<String, SummaryMetricDto> metrics = new LinkedHashMap<>();
        metrics.put("full_cycle", fullCycleMetric);

        SlaSummaryResponse response = new SlaSummaryResponse();
        response.setPeriod(new SummaryPeriodDto(dateFrom, dateTo));
        response.setPipeline("full");
        response.setMetrics(metrics);

        return response;
    }

    private SummaryMetricDto buildMockFullCycleMetric() {
        SummaryMetricDto dto = new SummaryMetricDto();
        int thresholdMinutes = fullCycleDays * 24 * 60;

        dto.setThresholdMinutes(thresholdMinutes);
        dto.setTotalOrders(1247L);
        dto.setMetCount(892L);
        dto.setMetPercent(71.53);
        dto.setBreachCount(355L);
        dto.setBreachPercent(28.47);
        dto.setAvgMinutes(14820.5);
        dto.setMedianMinutes(12960.0);
        dto.setP90Minutes(25920.0);

        BreachDistributionDto distribution = new BreachDistributionDto();
        distribution.setUpTo15Min(0);
        distribution.setFrom15To60Min(0);
        distribution.setOver60Min(0);
        dto.setBreachDistribution(distribution);

        return dto;
    }
}