package com.hackathon.sla_service.service;

import com.hackathon.sla_service.dto.response.ByManagerResponse;
import com.hackathon.sla_service.dto.response.DeliverySummaryResponse;

import java.time.LocalDate;

public interface DeliverySummaryService {

    DeliverySummaryResponse getDeliverySummary(LocalDate dateFrom,
                                               LocalDate dateTo,
                                               String managerId,
                                               String deliveryService);

    ByManagerResponse getDeliveryByManager(LocalDate dateFrom,
                                           LocalDate dateTo,
                                           String deliveryService);
}