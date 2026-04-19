package com.hackathon.sla_service.service.impl;

import com.hackathon.sla_service.config.SlaConfigProperties;
import com.hackathon.sla_service.dto.response.OrderTimelineResponse;
import com.hackathon.sla_service.repository.SlaRepository;
import com.hackathon.sla_service.repository.model.LeadTimelineRow;
import com.hackathon.sla_service.service.OrderTimelineService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class OrderTimelineServiceImpl implements OrderTimelineService {

    private final SlaRepository slaRepository;
    private final SlaConfigProperties slaConfig;

    public OrderTimelineServiceImpl(SlaRepository slaRepository,
                                    SlaConfigProperties slaConfig) {
        this.slaRepository = slaRepository;
        this.slaConfig = slaConfig;
    }

    @Override
    public OrderTimelineResponse getTimeline(String leadId) {
        LeadTimelineRow row = slaRepository.getLeadTimeline(leadId);

        if (row == null) {
            throw new IllegalArgumentException("Сделка не найдена: " + leadId);
        }

        OrderTimelineResponse response = new OrderTimelineResponse();
        response.setLeadId(row.getLeadId());
        response.setLifecycleIncomplete(row.getLifecycleIncomplete());
        response.setOutcomeUnknown(row.getOutcomeUnknown());

        OrderTimelineResponse.TimelineTimestampsDto timestamps = new OrderTimelineResponse.TimelineTimestampsDto();
        timestamps.setCreatedAt(row.getCreatedAt());
        timestamps.setSaleTs(row.getSaleTs());
        timestamps.setToAssemblyTs(row.getToAssemblyTs());
        timestamps.setHandedToDeliveryTs(row.getHandedToDeliveryTs());
        timestamps.setIssuedOrPvzTs(row.getIssuedOrPvzTs());
        timestamps.setReceivedTs(row.getReceivedTs());
        timestamps.setRejectedTs(row.getRejectedTs());
        timestamps.setReturnedTs(row.getReturnedTs());
        timestamps.setClosedTs(row.getClosedTs());
        response.setTimestamps(timestamps);

        Map<String, OrderTimelineResponse.TimelineStageDto> stages = new LinkedHashMap<>();

        stages.put("sla1_reaction", buildMinutesStage(
                row.getCreatedAt(),
                row.getSaleTs(),
                slaConfig.getB2c().getReactionMinutes()
        ));

        stages.put("sla2_to_assembly", buildMinutesStage(
                row.getSaleTs(),
                row.getToAssemblyTs(),
                slaConfig.getB2c().getToAssemblyHours() * 60
        ));

        stages.put("sla3_assembly_to_delivery", buildDaysStage(
                row.getToAssemblyTs(),
                row.getHandedToDeliveryTs(),
                slaConfig.getB2c().getAssemblyToDeliveryDays()
        ));

        stages.put("b2c_total", buildDaysStage(
                row.getCreatedAt(),
                row.getHandedToDeliveryTs(),
                slaConfig.getB2c().getTotalDays()
        ));

        stages.put("sla4_to_pvz", buildDaysStage(
                row.getHandedToDeliveryTs(),
                row.getIssuedOrPvzTs(),
                slaConfig.getDelivery().getToPvzDays()
        ));

        stages.put("sla5_pvz_storage", buildDaysStage(
                row.getIssuedOrPvzTs(),
                firstNotNull(row.getReceivedTs(), row.getRejectedTs(), row.getReturnedTs()),
                slaConfig.getDelivery().getPvzStorageDays()
        ));

        stages.put("delivery_total", buildDaysStage(
                row.getHandedToDeliveryTs(),
                firstNotNull(row.getReceivedTs(), row.getRejectedTs(), row.getReturnedTs()),
                slaConfig.getDelivery().getTotalDays()
        ));

        stages.put("full_cycle", buildDaysStage(
                row.getCreatedAt(),
                row.getClosedTs(),
                slaConfig.getFullCycleDays()
        ));

        response.setStages(stages);

        return response;
    }

    private OrderTimelineResponse.TimelineStageDto buildMinutesStage(LocalDateTime start,
                                                                     LocalDateTime end,
                                                                     int thresholdMinutes) {
        OrderTimelineResponse.TimelineStageDto dto = new OrderTimelineResponse.TimelineStageDto();

        if (start == null || end == null || end.isBefore(start)) {
            dto.setAvailable(false);
            dto.setDurationValue(null);
            dto.setDurationUnit("minutes");
            dto.setThresholdValue(thresholdMinutes);
            dto.setThresholdUnit("minutes");
            dto.setBreached(null);
            return dto;
        }

        double durationMinutes = Duration.between(start, end).toMinutes();

        dto.setAvailable(true);
        dto.setDurationValue(round(durationMinutes));
        dto.setDurationUnit("minutes");
        dto.setThresholdValue(thresholdMinutes);
        dto.setThresholdUnit("minutes");
        dto.setBreached(durationMinutes > thresholdMinutes);

        return dto;
    }

    private OrderTimelineResponse.TimelineStageDto buildDaysStage(LocalDateTime start,
                                                                  LocalDateTime end,
                                                                  int thresholdDays) {
        OrderTimelineResponse.TimelineStageDto dto = new OrderTimelineResponse.TimelineStageDto();

        if (start == null || end == null || end.isBefore(start)) {
            dto.setAvailable(false);
            dto.setDurationValue(null);
            dto.setDurationUnit("days");
            dto.setThresholdValue(thresholdDays);
            dto.setThresholdUnit("days");
            dto.setBreached(null);
            return dto;
        }

        double durationDays = Duration.between(start, end).toMinutes() / 60.0 / 24.0;

        dto.setAvailable(true);
        dto.setDurationValue(round(durationDays));
        dto.setDurationUnit("days");
        dto.setThresholdValue(thresholdDays);
        dto.setThresholdUnit("days");
        dto.setBreached(durationDays > thresholdDays);

        return dto;
    }

    @SafeVarargs
    private final <T> T firstNotNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}