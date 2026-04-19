package com.hackathon.sla_service.service;

import com.hackathon.sla_service.dto.response.SlaConfigResponse;

public interface SlaConfigService {
    SlaConfigResponse getConfig();
}