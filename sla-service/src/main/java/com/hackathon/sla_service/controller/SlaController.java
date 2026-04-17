package com.hackathon.sla_service.controller;

import com.hackathon.sla_service.dto.response.ByManagerResponse;
import com.hackathon.sla_service.dto.response.SlaSummaryResponse;
import com.hackathon.sla_service.service.SlaSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/sla")
public class SlaController {

    private final SlaSummaryService slaSummaryService;

    public SlaController(SlaSummaryService slaSummaryService) {
        this.slaSummaryService = slaSummaryService;
    }

    @Operation(summary = "Агрегаты по воронке B2C за период")
    @GetMapping("/b2c/summary")
    public ResponseEntity<SlaSummaryResponse> getB2cSummary(
            @RequestParam("date_from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam("date_to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "manager_id", required = false) String managerId,
            @RequestParam(value = "qualification", required = false) String qualification
    ) {
        return ResponseEntity.ok(slaSummaryService.getB2cSummary(dateFrom, dateTo, managerId, qualification));
    }

    @Operation(summary = "Агрегаты по менеджерам B2C за период")
    @GetMapping("/b2c/by-manager")
    public ResponseEntity<ByManagerResponse> getB2cByManager(
            @RequestParam("date_from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam("date_to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "qualification", required = false) String qualification
    ) {
        return ResponseEntity.ok(slaSummaryService.getB2cByManager(dateFrom, dateTo, qualification));
    }
}