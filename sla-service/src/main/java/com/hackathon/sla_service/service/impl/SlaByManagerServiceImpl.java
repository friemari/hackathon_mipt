package com.hackathon.sla_service.service.impl;

import com.hackathon.sla_service.dto.common.BreachDistributionDto;
import com.hackathon.sla_service.dto.common.SummaryMetricDto;
import com.hackathon.sla_service.dto.common.SummaryPeriodDto;
import com.hackathon.sla_service.dto.response.ByManagerResponse;
import com.hackathon.sla_service.dto.response.ManagerMetricsRowDto;
import com.hackathon.sla_service.service.SlaByManagerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class SlaByManagerServiceImpl implements SlaByManagerService {

    @Value("${sla.b2c.reaction-minutes:30}")
    private int reactionMinutes;

    @Value("${sla.b2c.to-assembly-hours:4}")
    private int toAssemblyHours;

    @Value("${sla.b2c.assembly-to-delivery-days:1}")
    private int assemblyToDeliveryDays;

    @Value("${sla.b2c.total-days:2}")
    private int b2cTotalDays;

    @Value("${sla.delivery.to-pvz-days:5}")
    private int toPvzDays;

    @Value("${sla.delivery.pvz-storage-days:7}")
    private int pvzStorageDays;

    @Value("${sla.delivery.total-days:14}")
    private int deliveryTotalDays;

    @Override
    public ByManagerResponse getB2cByManager(LocalDate dateFrom, LocalDate dateTo) {
        List<ManagerMetricsRowDto> items = buildMockB2cManagerMetrics();

        ByManagerResponse response = new ByManagerResponse();
        response.setPeriod(new SummaryPeriodDto(dateFrom, dateTo));
        response.setPipeline("b2c");
        response.setItems(items);

        return response;
    }

    @Override
    public ByManagerResponse getDeliveryByManager(LocalDate dateFrom, LocalDate dateTo) {
        List<ManagerMetricsRowDto> items = buildMockDeliveryManagerMetrics();

        ByManagerResponse response = new ByManagerResponse();
        response.setPeriod(new SummaryPeriodDto(dateFrom, dateTo));
        response.setPipeline("delivery");
        response.setItems(items);

        return response;
    }

    private List<ManagerMetricsRowDto> buildMockB2cManagerMetrics() {
        List<ManagerMetricsRowDto> items = new ArrayList<>();

        ManagerMetricsRowDto mgr1 = new ManagerMetricsRowDto();
        mgr1.setManagerId("MGR_0001");
        mgr1.setManagerName("Иванов Иван");
        mgr1.setMetrics(buildMockB2cMetricsForManager(342, 285, 312, 340));
        items.add(mgr1);

        ManagerMetricsRowDto mgr2 = new ManagerMetricsRowDto();
        mgr2.setManagerId("MGR_0002");
        mgr2.setManagerName("Петров Пётр");
        mgr2.setMetrics(buildMockB2cMetricsForManager(198, 145, 178, 195));
        items.add(mgr2);

        ManagerMetricsRowDto mgr3 = new ManagerMetricsRowDto();
        mgr3.setManagerId("MGR_0003");
        mgr3.setManagerName("Сидорова Мария");
        mgr3.setMetrics(buildMockB2cMetricsForManager(267, 210, 245, 260));
        items.add(mgr3);

        return items;
    }

    private List<ManagerMetricsRowDto> buildMockDeliveryManagerMetrics() {
        List<ManagerMetricsRowDto> items = new ArrayList<>();

        ManagerMetricsRowDto d1 = new ManagerMetricsRowDto();
        d1.setManagerId("DLV_0001");
        d1.setManagerName("Смирнов Алексей");
        d1.setMetrics(buildMockDeliveryMetricsForManager(520, 498, 515));
        items.add(d1);

        ManagerMetricsRowDto d2 = new ManagerMetricsRowDto();
        d2.setManagerId("DLV_0002");
        d2.setManagerName("Кузнецова Елена");
        d2.setMetrics(buildMockDeliveryMetricsForManager(387, 352, 380));
        items.add(d2);

        return items;
    }

    private Map<String, SummaryMetricDto> buildMockB2cMetricsForManager(
            long sla1Total, long sla2Total, long sla3Total, long b2cTotal) {

        Map<String, SummaryMetricDto> metrics = new LinkedHashMap<>();

        metrics.put("sla1_reaction", buildMockMetric(
                reactionMinutes, sla1Total, 0.78, 24.5, 18.0, 45.0,
                15, 28, 12
        ));

        metrics.put("sla2_to_assembly", buildMockMetric(
                toAssemblyHours * 60, sla2Total, 0.82, 185.0, 165.0, 310.0,
                0, 0, 0
        ));

        metrics.put("sla3_assembly_to_delivery", buildMockMetric(
                assemblyToDeliveryDays * 24 * 60, sla3Total, 0.75, 1320.0, 1080.0, 2100.0,
                0, 0, 0
        ));

        metrics.put("b2c_total", buildMockMetric(
                b2cTotalDays * 24 * 60, b2cTotal, 0.71, 2650.0, 2300.0, 3800.0,
                0, 0, 0
        ));

        return metrics;
    }

    private Map<String, SummaryMetricDto> buildMockDeliveryMetricsForManager(
            long sla4Total, long sla5Total, long deliveryTotal) {

        Map<String, SummaryMetricDto> metrics = new LinkedHashMap<>();

        metrics.put("sla4_to_pvz", buildMockMetric(
                toPvzDays * 24 * 60, sla4Total, 0.85, 3520.0, 2880.0, 5760.0,
                0, 0, 0
        ));

        metrics.put("sla5_pvz_storage", buildMockMetric(
                pvzStorageDays * 24 * 60, sla5Total, 0.79, 6840.0, 6480.0, 10080.0,
                0, 0, 0
        ));

        metrics.put("delivery_total", buildMockMetric(
                deliveryTotalDays * 24 * 60, deliveryTotal, 0.73, 11520.0, 10080.0, 17280.0,
                0, 0, 0
        ));

        return metrics;
    }

    private SummaryMetricDto buildMockMetric(
            int thresholdMinutes,
            long totalOrders,
            double metPercent,
            double avgMinutes,
            double medianMinutes,
            double p90Minutes,
            int upTo15Min,
            int from15To60Min,
            int over60Min) {

        SummaryMetricDto dto = new SummaryMetricDto();
        long metCount = Math.round(totalOrders * metPercent);
        long breachCount = totalOrders - metCount;

        dto.setThresholdMinutes(thresholdMinutes);
        dto.setTotalOrders(totalOrders);
        dto.setMetCount(metCount);
        dto.setMetPercent(metPercent * 100);
        dto.setBreachCount(breachCount);
        dto.setBreachPercent((1 - metPercent) * 100);
        dto.setAvgMinutes(avgMinutes);
        dto.setMedianMinutes(medianMinutes);
        dto.setP90Minutes(p90Minutes);

        BreachDistributionDto distribution = new BreachDistributionDto();
        distribution.setUpTo15Min(upTo15Min);
        distribution.setFrom15To60Min(from15To60Min);
        distribution.setOver60Min(over60Min);
        dto.setBreachDistribution(distribution);

        return dto;
    }
}