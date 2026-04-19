package com.hackathon.sla_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

public class OrderTimelineResponse {

    @JsonProperty("lead_id")
    private String leadId;

    @JsonProperty("lifecycle_incomplete")
    private Boolean lifecycleIncomplete;

    @JsonProperty("outcome_unknown")
    private Boolean outcomeUnknown;

    @JsonProperty("timestamps")
    private TimelineTimestampsDto timestamps;

    @JsonProperty("stages")
    private Map<String, TimelineStageDto> stages;

    public String getLeadId() {
        return leadId;
    }

    public void setLeadId(String leadId) {
        this.leadId = leadId;
    }

    public Boolean getLifecycleIncomplete() {
        return lifecycleIncomplete;
    }

    public void setLifecycleIncomplete(Boolean lifecycleIncomplete) {
        this.lifecycleIncomplete = lifecycleIncomplete;
    }

    public Boolean getOutcomeUnknown() {
        return outcomeUnknown;
    }

    public void setOutcomeUnknown(Boolean outcomeUnknown) {
        this.outcomeUnknown = outcomeUnknown;
    }

    public TimelineTimestampsDto getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(TimelineTimestampsDto timestamps) {
        this.timestamps = timestamps;
    }

    public Map<String, TimelineStageDto> getStages() {
        return stages;
    }

    public void setStages(Map<String, TimelineStageDto> stages) {
        this.stages = stages;
    }

    public static class TimelineTimestampsDto {
        @JsonProperty("created_at")
        private LocalDateTime createdAt;

        @JsonProperty("sale_ts")
        private LocalDateTime saleTs;

        @JsonProperty("to_assembly_ts")
        private LocalDateTime toAssemblyTs;

        @JsonProperty("handed_to_delivery_ts")
        private LocalDateTime handedToDeliveryTs;

        @JsonProperty("issued_or_pvz_ts")
        private LocalDateTime issuedOrPvzTs;

        @JsonProperty("received_ts")
        private LocalDateTime receivedTs;

        @JsonProperty("rejected_ts")
        private LocalDateTime rejectedTs;

        @JsonProperty("returned_ts")
        private LocalDateTime returnedTs;

        @JsonProperty("closed_ts")
        private LocalDateTime closedTs;

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getSaleTs() {
            return saleTs;
        }

        public void setSaleTs(LocalDateTime saleTs) {
            this.saleTs = saleTs;
        }

        public LocalDateTime getToAssemblyTs() {
            return toAssemblyTs;
        }

        public void setToAssemblyTs(LocalDateTime toAssemblyTs) {
            this.toAssemblyTs = toAssemblyTs;
        }

        public LocalDateTime getHandedToDeliveryTs() {
            return handedToDeliveryTs;
        }

        public void setHandedToDeliveryTs(LocalDateTime handedToDeliveryTs) {
            this.handedToDeliveryTs = handedToDeliveryTs;
        }

        public LocalDateTime getIssuedOrPvzTs() {
            return issuedOrPvzTs;
        }

        public void setIssuedOrPvzTs(LocalDateTime issuedOrPvzTs) {
            this.issuedOrPvzTs = issuedOrPvzTs;
        }

        public LocalDateTime getReceivedTs() {
            return receivedTs;
        }

        public void setReceivedTs(LocalDateTime receivedTs) {
            this.receivedTs = receivedTs;
        }

        public LocalDateTime getRejectedTs() {
            return rejectedTs;
        }

        public void setRejectedTs(LocalDateTime rejectedTs) {
            this.rejectedTs = rejectedTs;
        }

        public LocalDateTime getReturnedTs() {
            return returnedTs;
        }

        public void setReturnedTs(LocalDateTime returnedTs) {
            this.returnedTs = returnedTs;
        }

        public LocalDateTime getClosedTs() {
            return closedTs;
        }

        public void setClosedTs(LocalDateTime closedTs) {
            this.closedTs = closedTs;
        }
    }

    public static class TimelineStageDto {
        @JsonProperty("available")
        private Boolean available;

        @JsonProperty("duration_value")
        private Double durationValue;

        @JsonProperty("duration_unit")
        private String durationUnit;

        @JsonProperty("threshold_value")
        private Integer thresholdValue;

        @JsonProperty("threshold_unit")
        private String thresholdUnit;

        @JsonProperty("breached")
        private Boolean breached;

        public Boolean getAvailable() {
            return available;
        }

        public void setAvailable(Boolean available) {
            this.available = available;
        }

        public Double getDurationValue() {
            return durationValue;
        }

        public void setDurationValue(Double durationValue) {
            this.durationValue = durationValue;
        }

        public String getDurationUnit() {
            return durationUnit;
        }

        public void setDurationUnit(String durationUnit) {
            this.durationUnit = durationUnit;
        }

        public Integer getThresholdValue() {
            return thresholdValue;
        }

        public void setThresholdValue(Integer thresholdValue) {
            this.thresholdValue = thresholdValue;
        }

        public String getThresholdUnit() {
            return thresholdUnit;
        }

        public void setThresholdUnit(String thresholdUnit) {
            this.thresholdUnit = thresholdUnit;
        }

        public Boolean getBreached() {
            return breached;
        }

        public void setBreached(Boolean breached) {
            this.breached = breached;
        }
    }
}