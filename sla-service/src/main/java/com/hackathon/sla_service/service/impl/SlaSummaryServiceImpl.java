package com.hackathon.sla_service.service.impl;

import com.hackathon.sla_service.dto.common.SummaryMetricDto;
import com.hackathon.sla_service.dto.common.SummaryPeriodDto;
import com.hackathon.sla_service.dto.response.SlaSummaryResponse;
import com.hackathon.sla_service.repository.SlaRepository;
import com.hackathon.sla_service.service.SlaSummaryService;
import com.hackathon.sla_service.service.calculator.SlaMetricCalculator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SlaSummaryServiceImpl implements SlaSummaryService {

    private final SlaRepository slaRepository;
    private final SlaMetricCalculator slaMetricCalculator;

    @Value("${sla.b2c.reaction-minutes}")
    private int reactionMinutes;

    @Value("${sla.b2c.to-assembly-hours}")
    private int toAssemblyHours;

    @Value("${sla.b2c.assembly-to-delivery-days}")
    private int assemblyToDeliveryDays;

    @Value("${sla.b2c.total-days}")
    private int totalDays;

    public SlaSummaryServiceImpl(SlaRepository slaRepository,
                                 SlaMetricCalculator slaMetricCalculator) {
        this.slaRepository = slaRepository;
        this.slaMetricCalculator = slaMetricCalculator;
    }

    @Override
    public SlaSummaryResponse getB2cSummary(LocalDate dateFrom,
                                            LocalDate dateTo,
                                            String managerId,
                                            String qualification) {

        SummaryMetricDto sla1 = slaMetricCalculator.calculate(
                slaRepository.getSla1Values(dateFrom, dateTo, managerId, qualification),
                reactionMinutes
        );

        SummaryMetricDto sla2 = slaMetricCalculator.calculate(
                slaRepository.getSla2Values(dateFrom, dateTo, managerId, qualification),
                toAssemblyHours * 60
        );

        SummaryMetricDto sla3 = slaMetricCalculator.calculate(
                slaRepository.getSla3Values(dateFrom, dateTo, managerId, qualification),
                assemblyToDeliveryDays * 24 * 60
        );

        SummaryMetricDto b2cTotal = slaMetricCalculator.calculate(
                slaRepository.getB2cTotalValues(dateFrom, dateTo, managerId, qualification),
                totalDays * 24 * 60
        );

        Map<String, SummaryMetricDto> metrics = new LinkedHashMap<>();
        metrics.put("sla1_reaction", sla1);
        metrics.put("sla2_to_assembly", sla2);
        metrics.put("sla3_assembly_to_delivery", sla3);
        metrics.put("b2c_total", b2cTotal);

        SlaSummaryResponse response = new SlaSummaryResponse();
        response.setPeriod(new SummaryPeriodDto(dateFrom, dateTo));
        response.setPipeline("b2c");
        response.setMetrics(metrics);

        return response;
    }
}