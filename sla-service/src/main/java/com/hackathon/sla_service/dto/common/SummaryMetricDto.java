package com.hackathon.sla_service.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SummaryMetricDto {

    @JsonProperty("threshold_minutes")
    private Integer thresholdMinutes;

    @JsonProperty("total_orders")
    private Long totalOrders;

    @JsonProperty("met_count")
    private Long metCount;

    @JsonProperty("met_percent")
    private Double metPercent;

    @JsonProperty("breach_count")
    private Long breachCount;

    @JsonProperty("breach_percent")
    private Double breachPercent;

    @JsonProperty("avg_minutes")
    private Double avgMinutes;

    @JsonProperty("median_minutes")
    private Double medianMinutes;

    @JsonProperty("p90_minutes")
    private Double p90Minutes;

    @JsonProperty("breach_distribution")
    private BreachDistributionDto breachDistribution;

    public SummaryMetricDto() {}

    public Integer getThresholdMinutes() {
        return thresholdMinutes;
    }

    public void setThresholdMinutes(Integer thresholdMinutes) {
        this.thresholdMinutes = thresholdMinutes;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getMetCount() {
        return metCount;
    }

    public void setMetCount(Long metCount) {
        this.metCount = metCount;
    }

    public Double getMetPercent() {
        return metPercent;
    }

    public void setMetPercent(Double metPercent) {
        this.metPercent = metPercent;
    }

    public Long getBreachCount() {
        return breachCount;
    }

    public void setBreachCount(Long breachCount) {
        this.breachCount = breachCount;
    }

    public Double getBreachPercent() {
        return breachPercent;
    }

    public void setBreachPercent(Double breachPercent) {
        this.breachPercent = breachPercent;
    }

    public Double getAvgMinutes() {
        return avgMinutes;
    }

    public void setAvgMinutes(Double avgMinutes) {
        this.avgMinutes = avgMinutes;
    }

    public Double getMedianMinutes() {
        return medianMinutes;
    }

    public void setMedianMinutes(Double medianMinutes) {
        this.medianMinutes = medianMinutes;
    }

    public Double getP90Minutes() {
        return p90Minutes;
    }

    public void setP90Minutes(Double p90Minutes) {
        this.p90Minutes = p90Minutes;
    }

    public BreachDistributionDto getBreachDistribution() {
        return breachDistribution;
    }

    public void setBreachDistribution(BreachDistributionDto breachDistribution) {
        this.breachDistribution = breachDistribution;
    }
}