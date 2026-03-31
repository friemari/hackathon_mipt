package com.hackathon.sla_service.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BreachDistributionDto {

    @JsonProperty("up_to_15min")
    private Integer upTo15Min;

    @JsonProperty("15_to_60min")
    private Integer from15To60Min;

    @JsonProperty("over_60min")
    private Integer over60Min;

    public BreachDistributionDto() {}

    public Integer getUpTo15Min() {
        return upTo15Min;
    }

    public void setUpTo15Min(Integer upTo15Min) {
        this.upTo15Min = upTo15Min;
    }

    public Integer getFrom15To60Min() {
        return from15To60Min;
    }

    public void setFrom15To60Min(Integer from15To60Min) {
        this.from15To60Min = from15To60Min;
    }

    public Integer getOver60Min() {
        return over60Min;
    }

    public void setOver60Min(Integer over60Min) {
        this.over60Min = over60Min;
    }
}