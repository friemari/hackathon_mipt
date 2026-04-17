package com.hackathon.sla_service.controller;

import com.hackathon.sla_service.dto.response.ByManagerResponse;
import com.hackathon.sla_service.dto.response.DeliverySummaryResponse;
import com.hackathon.sla_service.service.DeliverySummaryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/sla")
public class DeliveryController {

    private final DeliverySummaryService deliverySummaryService;

    public DeliveryController(DeliverySummaryService deliverySummaryService) {
        this.deliverySummaryService = deliverySummaryService;
    }

    @Operation(summary = "Агрегаты по воронке доставки за период")
    @GetMapping("/delivery/summary")
    public ResponseEntity<DeliverySummaryResponse> getDeliverySummary(
            @RequestParam("date_from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam("date_to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "manager_id", required = false) String managerId,
            @RequestParam(value = "delivery_service", required = false) String deliveryService
    ) {
        return ResponseEntity.ok(deliverySummaryService.getDeliverySummary(dateFrom, dateTo, managerId, deliveryService));
    }

    @Operation(summary = "Агрегаты по менеджерам доставки за период")
    @GetMapping("/delivery/by-manager")
    public ResponseEntity<ByManagerResponse> getDeliveryByManager(
            @RequestParam("date_from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam("date_to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "delivery_service", required = false) String deliveryService
    ) {
        return ResponseEntity.ok(deliverySummaryService.getDeliveryByManager(dateFrom, dateTo, deliveryService));
    }
}