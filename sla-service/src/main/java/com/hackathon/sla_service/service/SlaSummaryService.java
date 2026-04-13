package com.hackathon.sla_service.service;

import com.hackathon.sla_service.dto.response.SlaSummaryResponse;

import java.time.LocalDate;

public interface SlaSummaryService {

    SlaSummaryResponse getB2cSummary(LocalDate dateFrom,
                                     LocalDate dateTo,
                                     String managerId,
                                     String qualification);
}