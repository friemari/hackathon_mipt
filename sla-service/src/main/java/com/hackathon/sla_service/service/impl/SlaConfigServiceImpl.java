package com.hackathon.sla_service.service.impl;

import com.hackathon.sla_service.config.SlaConfigProperties;
import com.hackathon.sla_service.dto.response.SlaConfigResponse;
import com.hackathon.sla_service.service.SlaConfigService;
import org.springframework.stereotype.Service;

@Service
public class SlaConfigServiceImpl implements SlaConfigService {

    private final SlaConfigProperties slaConfig;

    public SlaConfigServiceImpl(SlaConfigProperties slaConfig) {
        this.slaConfig = slaConfig;
    }

    @Override
    public SlaConfigResponse getConfig() {
        SlaConfigResponse response = new SlaConfigResponse();

        SlaConfigResponse.B2cConfigDto b2cDto = new SlaConfigResponse.B2cConfigDto();
        b2cDto.setReactionMinutes(slaConfig.getB2c().getReactionMinutes());
        b2cDto.setToAssemblyHours(slaConfig.getB2c().getToAssemblyHours());
        b2cDto.setAssemblyToDeliveryDays(slaConfig.getB2c().getAssemblyToDeliveryDays());
        b2cDto.setTotalDays(slaConfig.getB2c().getTotalDays());

        SlaConfigResponse.DeliveryConfigDto deliveryDto = new SlaConfigResponse.DeliveryConfigDto();
        deliveryDto.setToPvzDays(slaConfig.getDelivery().getToPvzDays());
        deliveryDto.setPvzStorageDays(slaConfig.getDelivery().getPvzStorageDays());
        deliveryDto.setTotalDays(slaConfig.getDelivery().getTotalDays());

        SlaConfigResponse.BreachBucketsDto bucketsDto = new SlaConfigResponse.BreachBucketsDto();
        bucketsDto.setShortMinutes(slaConfig.getBreachBuckets().getShortMinutes());
        bucketsDto.setDays(slaConfig.getBreachBuckets().getDays());

        response.setB2c(b2cDto);
        response.setDelivery(deliveryDto);
        response.setFullCycleDays(slaConfig.getFullCycleDays());
        response.setBreachBuckets(bucketsDto);

        return response;
    }
}