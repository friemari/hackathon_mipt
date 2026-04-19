package com.hackathon.sla_service.service;

import com.hackathon.sla_service.dto.response.OrderTimelineResponse;

public interface OrderTimelineService {
    OrderTimelineResponse getTimeline(String leadId);
}