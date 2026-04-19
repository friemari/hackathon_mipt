package com.hackathon.sla_service.service.impl;

import com.hackathon.sla_service.config.SlaConfigProperties;
import com.hackathon.sla_service.dto.common.SummaryMetricDto;
import com.hackathon.sla_service.dto.common.SummaryPeriodDto;
import com.hackathon.sla_service.dto.response.ByManagerResponse;
import com.hackathon.sla_service.dto.response.ManagerMetricsRowDto;
import com.hackathon.sla_service.dto.response.SlaSummaryResponse;
import com.hackathon.sla_service.repository.SlaRepository;
import com.hackathon.sla_service.service.SlaSummaryService;
import com.hackathon.sla_service.service.calculator.SlaMetricCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SlaSummaryServiceImpl implements SlaSummaryService {

        private final SlaRepository slaRepository;
        private final SlaMetricCalculator slaMetricCalculator;
        private final SlaConfigProperties slaConfig;

        public SlaSummaryServiceImpl(SlaRepository slaRepository,
                        SlaMetricCalculator slaMetricCalculator,
                        SlaConfigProperties slaConfig) {
                this.slaRepository = slaRepository;
                this.slaMetricCalculator = slaMetricCalculator;
                this.slaConfig = slaConfig;
        }

        @Override
        public SlaSummaryResponse getB2cSummary(LocalDate dateFrom,
                        LocalDate dateTo,
                        String managerId,
                        String qualification) {

                int reactionMinutes = slaConfig.getB2c().getReactionMinutes();
                int toAssemblyMinutes = slaConfig.getB2c().getToAssemblyHours() * 60;
                int assemblyToDeliveryMinutes = slaConfig.getB2c().getAssemblyToDeliveryDays() * 24 * 60;
                int totalMinutes = slaConfig.getB2c().getTotalDays() * 24 * 60;

                SummaryMetricDto sla1 = slaMetricCalculator.calculate(
                                slaRepository.getSla1Values(dateFrom, dateTo, managerId, qualification),
                                reactionMinutes);

                SummaryMetricDto sla2 = slaMetricCalculator.calculate(
                                slaRepository.getSla2Values(dateFrom, dateTo, managerId, qualification),
                                toAssemblyMinutes);

                SummaryMetricDto sla3 = slaMetricCalculator.calculate(
                                slaRepository.getSla3Values(dateFrom, dateTo, managerId, qualification),
                                assemblyToDeliveryMinutes);

                SummaryMetricDto b2cTotal = slaMetricCalculator.calculate(
                                slaRepository.getB2cTotalValues(dateFrom, dateTo, managerId, qualification),
                                totalMinutes);

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

        @Override
        public ByManagerResponse getB2cByManager(LocalDate dateFrom,
                        LocalDate dateTo,
                        String qualification) {

                int reactionMinutes = slaConfig.getB2c().getReactionMinutes();
                int toAssemblyMinutes = slaConfig.getB2c().getToAssemblyHours() * 60;
                int assemblyToDeliveryMinutes = slaConfig.getB2c().getAssemblyToDeliveryDays() * 24 * 60;
                int totalMinutes = slaConfig.getB2c().getTotalDays() * 24 * 60;

                List<String> managerIds = slaRepository.getDistinctManagerIds(dateFrom, dateTo, qualification);
                List<ManagerMetricsRowDto> items = new ArrayList<>();

                for (String managerId : managerIds) {
                        ManagerMetricsRowDto row = new ManagerMetricsRowDto();
                        row.setManagerId(managerId);

                        Map<String, SummaryMetricDto> metrics = new LinkedHashMap<>();

                        SummaryMetricDto sla1 = slaMetricCalculator.calculate(
                                        slaRepository.getSla1Values(dateFrom, dateTo, managerId, qualification),
                                        reactionMinutes);
                        metrics.put("sla1_reaction", sla1);

                        SummaryMetricDto sla2 = slaMetricCalculator.calculate(
                                        slaRepository.getSla2Values(dateFrom, dateTo, managerId, qualification),
                                        toAssemblyMinutes);
                        metrics.put("sla2_to_assembly", sla2);

                        SummaryMetricDto sla3 = slaMetricCalculator.calculate(
                                        slaRepository.getSla3Values(dateFrom, dateTo, managerId, qualification),
                                        assemblyToDeliveryMinutes);
                        metrics.put("sla3_assembly_to_delivery", sla3);

                        SummaryMetricDto b2cTotal = slaMetricCalculator.calculate(
                                        slaRepository.getB2cTotalValues(dateFrom, dateTo, managerId, qualification),
                                        totalMinutes);
                        metrics.put("b2c_total", b2cTotal);

                        row.setMetrics(metrics);
                        items.add(row);
                }

                ByManagerResponse response = new ByManagerResponse();
                response.setPeriod(new SummaryPeriodDto(dateFrom, dateTo));
                response.setPipeline("b2c");
                response.setItems(items);

                return response;
        }
}