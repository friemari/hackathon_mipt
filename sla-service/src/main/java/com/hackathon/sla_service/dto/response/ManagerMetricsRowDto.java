package com.hackathon.sla_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hackathon.sla_service.dto.common.SummaryMetricDto;

import java.util.Map;

public class ManagerMetricsRowDto {

    @JsonProperty("manager_id")
    private String managerId;

    @JsonProperty("metrics")
    private Map<String, SummaryMetricDto> metrics;

    public ManagerMetricsRowDto() {
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public Map<String, SummaryMetricDto> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, SummaryMetricDto> metrics) {
        this.metrics = metrics;
    }
}