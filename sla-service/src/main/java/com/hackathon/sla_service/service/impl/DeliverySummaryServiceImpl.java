package com.hackathon.sla_service.service.impl;

import com.hackathon.sla_service.config.SlaConfigProperties;
import com.hackathon.sla_service.dto.response.ByManagerResponse;
import com.hackathon.sla_service.dto.response.DeliverySummaryResponse;
import com.hackathon.sla_service.dto.response.ManagerMetricsRowDto;
import com.hackathon.sla_service.dto.common.SummaryMetricDto;
import com.hackathon.sla_service.dto.common.SummaryPeriodDto;
import com.hackathon.sla_service.repository.SlaRepository;
import com.hackathon.sla_service.service.DeliverySummaryService;
import com.hackathon.sla_service.service.calculator.SlaMetricCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeliverySummaryServiceImpl implements DeliverySummaryService {

    private final SlaRepository slaRepository;
    private final SlaMetricCalculator slaMetricCalculator;
    private final SlaConfigProperties slaConfig;

    public DeliverySummaryServiceImpl(SlaRepository slaRepository,
                                      SlaMetricCalculator slaMetricCalculator,
                                      SlaConfigProperties slaConfig) {
        this.slaRepository = slaRepository;
        this.slaMetricCalculator = slaMetricCalculator;
        this.slaConfig = slaConfig;
    }

    @Override
    public DeliverySummaryResponse getDeliverySummary(LocalDate dateFrom,
                                                      LocalDate dateTo,
                                                      String managerId,
                                                      String deliveryService) {

        int toPvzDays = slaConfig.getDelivery().getToPvzDays();
        int pvzStorageDays = slaConfig.getDelivery().getPvzStorageDays();
        int deliveryTotalDays = slaConfig.getDelivery().getTotalDays();

        // SLA-4: Время до ПВЗ (в днях)
        SummaryMetricDto sla4 = slaMetricCalculator.calculateDays(
                slaRepository.getSla4Values(dateFrom, dateTo, managerId, deliveryService),
                toPvzDays
        );

        // SLA-5: Хранение на ПВЗ (в днях)
        SummaryMetricDto sla5 = slaMetricCalculator.calculateDays(
                slaRepository.getSla5Values(dateFrom, dateTo, managerId, deliveryService),
                pvzStorageDays
        );

        // DEL-total: Полный цикл доставки (в днях)
        SummaryMetricDto deliveryTotal = slaMetricCalculator.calculateDays(
                slaRepository.getDeliveryTotalValues(dateFrom, dateTo, managerId, deliveryService),
                deliveryTotalDays
        );

        Map<String, SummaryMetricDto> metrics = new LinkedHashMap<>();
        metrics.put("sla4_to_pvz", sla4);
        metrics.put("sla5_pvz_storage", sla5);
        metrics.put("delivery_total", deliveryTotal);

        DeliverySummaryResponse response = new DeliverySummaryResponse();
        response.setPeriod(new SummaryPeriodDto(dateFrom, dateTo));
        response.setPipeline("delivery");
        response.setMetrics(metrics);

        return response;
    }

    @Override
    public ByManagerResponse getDeliveryByManager(LocalDate dateFrom,
                                                  LocalDate dateTo,
                                                  String deliveryService) {

        List<String> managerIds = slaRepository.getDistinctDeliveryManagerIds(dateFrom, dateTo, deliveryService);

        List<ManagerMetricsRowDto> items = new ArrayList<>();

        int toPvzDays = slaConfig.getDelivery().getToPvzDays();
        int pvzStorageDays = slaConfig.getDelivery().getPvzStorageDays();
        int deliveryTotalDays = slaConfig.getDelivery().getTotalDays();

        for (String managerId : managerIds) {
            ManagerMetricsRowDto row = new ManagerMetricsRowDto();
            row.setManagerId(managerId);
            row.setManagerName(managerId);

            Map<String, SummaryMetricDto> metrics = new LinkedHashMap<>();

            SummaryMetricDto sla4 = slaMetricCalculator.calculateDays(
                    slaRepository.getSla4Values(dateFrom, dateTo, managerId, deliveryService),
                    toPvzDays
            );
            metrics.put("sla4_to_pvz", sla4);

            SummaryMetricDto sla5 = slaMetricCalculator.calculateDays(
                    slaRepository.getSla5Values(dateFrom, dateTo, managerId, deliveryService),
                    pvzStorageDays
            );
            metrics.put("sla5_pvz_storage", sla5);

            SummaryMetricDto deliveryTotal = slaMetricCalculator.calculateDays(
                    slaRepository.getDeliveryTotalValues(dateFrom, dateTo, managerId, deliveryService),
                    deliveryTotalDays
            );
            metrics.put("delivery_total", deliveryTotal);

            row.setMetrics(metrics);
            items.add(row);
        }

        ByManagerResponse response = new ByManagerResponse();
        response.setPeriod(new SummaryPeriodDto(dateFrom, dateTo));
        response.setPipeline("delivery");
        response.setItems(items);

        return response;
    }
}