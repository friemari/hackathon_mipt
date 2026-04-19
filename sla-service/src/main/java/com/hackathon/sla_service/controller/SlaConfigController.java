package com.hackathon.sla_service.controller;

import com.hackathon.sla_service.dto.response.SlaConfigResponse;
import com.hackathon.sla_service.service.SlaConfigService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sla")
public class SlaConfigController {

    private final SlaConfigService slaConfigService;

    public SlaConfigController(SlaConfigService slaConfigService) {
        this.slaConfigService = slaConfigService;
    }

    @Operation(summary = "Текущие нормативы SLA из конфига")
    @GetMapping("/config")
    public ResponseEntity<SlaConfigResponse> getConfig() {
        return ResponseEntity.ok(slaConfigService.getConfig());
    }
}