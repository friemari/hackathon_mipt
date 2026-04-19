package com.hackathon.sla_service.controller;

import com.hackathon.sla_service.dto.response.OrderTimelineResponse;
import com.hackathon.sla_service.service.OrderTimelineService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderTimelineController {

    private final OrderTimelineService orderTimelineService;

    public OrderTimelineController(OrderTimelineService orderTimelineService) {
        this.orderTimelineService = orderTimelineService;
    }

    @Operation(summary = "Временная линия конкретной сделки")
    @GetMapping("/{leadId}/timeline")
    public ResponseEntity<OrderTimelineResponse> getTimeline(@PathVariable String leadId) {
        return ResponseEntity.ok(orderTimelineService.getTimeline(leadId));
    }
}