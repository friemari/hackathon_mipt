package com.hackathon.sla_service.controller;

import com.hackathon.sla_service.dto.response.SlaSummaryResponse;
import com.hackathon.sla_service.service.SlaFullSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/sla")
public class FullSlaController {

    private final SlaFullSummaryService slaFullSummaryService;

    public FullSlaController(SlaFullSummaryService slaFullSummaryService) {
        this.slaFullSummaryService = slaFullSummaryService;
    }

    @Operation(summary = "Сквозной SLA: полный цикл сделки")
    @GetMapping("/full/summary")
    public ResponseEntity<SlaSummaryResponse> getFullSummary(
            @RequestParam("date_from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam("date_to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(slaFullSummaryService.getFullSummary(dateFrom, dateTo));
    }
}