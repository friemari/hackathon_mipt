package com.hackathon.sla_service.service;

import com.hackathon.sla_service.dto.response.ByManagerResponse;
import java.time.LocalDate;

public interface SlaByManagerService {
    ByManagerResponse getB2cByManager(LocalDate dateFrom, LocalDate dateTo);
    ByManagerResponse getDeliveryByManager(LocalDate dateFrom, LocalDate dateTo);
}