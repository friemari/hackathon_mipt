package com.hackathon.sla_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hackathon.sla_service.dto.common.SummaryMetricDto;
import com.hackathon.sla_service.dto.common.SummaryPeriodDto;

import java.util.Map;

public class SlaSummaryResponse {

    @JsonProperty("period")
    private SummaryPeriodDto period;

    @JsonProperty("pipeline")
    private String pipeline;

    @JsonProperty("metrics")
    private Map<String, SummaryMetricDto> metrics;

    public SlaSummaryResponse() {}

    public SummaryPeriodDto getPeriod() {
        return period;
    }

    public void setPeriod(SummaryPeriodDto period) {
        this.period = period;
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public Map<String, SummaryMetricDto> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, SummaryMetricDto> metrics) {
        this.metrics = metrics;
    }
}