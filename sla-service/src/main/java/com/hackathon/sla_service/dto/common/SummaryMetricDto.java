package com.hackathon.sla_service.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class SummaryMetricDto {

    @JsonProperty("threshold_value")
    private Integer thresholdValue;

    @JsonProperty("unit")
    private String unit;

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

    @JsonProperty("avg_value")
    private Double avgValue;

    @JsonProperty("median_value")
    private Double medianValue;

    @JsonProperty("p90_value")
    private Double p90Value;

    @JsonProperty("breach_distribution")
    private Map<String, Integer> breachDistribution;

    public SummaryMetricDto() {}

    public Integer getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(Integer thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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

    public Double getAvgValue() {
        return avgValue;
    }

    public void setAvgValue(Double avgValue) {
        this.avgValue = avgValue;
    }

    public Double getMedianValue() {
        return medianValue;
    }

    public void setMedianValue(Double medianValue) {
        this.medianValue = medianValue;
    }

    public Double getP90Value() {
        return p90Value;
    }

    public void setP90Value(Double p90Value) {
        this.p90Value = p90Value;
    }

    public Map<String, Integer> getBreachDistribution() {
        return breachDistribution;
    }

    public void setBreachDistribution(Map<String, Integer> breachDistribution) {
        this.breachDistribution = breachDistribution;
    }
}