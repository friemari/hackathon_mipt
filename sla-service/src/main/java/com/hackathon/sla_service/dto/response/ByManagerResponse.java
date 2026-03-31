package com.hackathon.sla_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hackathon.sla_service.dto.common.SummaryPeriodDto;
import java.util.List;

public class ByManagerResponse {

    @JsonProperty("period")
    private SummaryPeriodDto period;

    @JsonProperty("pipeline")
    private String pipeline;

    @JsonProperty("items")
    private List<ManagerMetricsRowDto> items;

    public ByManagerResponse() {
    }

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

    public List<ManagerMetricsRowDto> getItems() {
        return items;
    }

    public void setItems(List<ManagerMetricsRowDto> items) {
        this.items = items;
    }
}