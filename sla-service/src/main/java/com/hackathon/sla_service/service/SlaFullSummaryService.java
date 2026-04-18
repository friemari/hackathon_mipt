package com.hackathon.sla_service.service;

import com.hackathon.sla_service.dto.response.SlaSummaryResponse;
import java.time.LocalDate;

public interface SlaFullSummaryService {
    SlaSummaryResponse getFullSummary(LocalDate dateFrom, LocalDate dateTo);
}