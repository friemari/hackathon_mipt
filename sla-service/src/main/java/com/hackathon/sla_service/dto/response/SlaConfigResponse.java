package com.hackathon.sla_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SlaConfigResponse {

    @JsonProperty("b2c")
    private B2cConfigDto b2c;

    @JsonProperty("delivery")
    private DeliveryConfigDto delivery;

    @JsonProperty("full_cycle_days")
    private Integer fullCycleDays;

    @JsonProperty("breach_buckets")
    private BreachBucketsDto breachBuckets;

    public B2cConfigDto getB2c() {
        return b2c;
    }

    public void setB2c(B2cConfigDto b2c) {
        this.b2c = b2c;
    }

    public DeliveryConfigDto getDelivery() {
        return delivery;
    }

    public void setDelivery(DeliveryConfigDto delivery) {
        this.delivery = delivery;
    }

    public Integer getFullCycleDays() {
        return fullCycleDays;
    }

    public void setFullCycleDays(Integer fullCycleDays) {
        this.fullCycleDays = fullCycleDays;
    }

    public BreachBucketsDto getBreachBuckets() {
        return breachBuckets;
    }

    public void setBreachBuckets(BreachBucketsDto breachBuckets) {
        this.breachBuckets = breachBuckets;
    }

    public static class B2cConfigDto {
        @JsonProperty("reaction_minutes")
        private Integer reactionMinutes;

        @JsonProperty("to_assembly_hours")
        private Integer toAssemblyHours;

        @JsonProperty("assembly_to_delivery_days")
        private Integer assemblyToDeliveryDays;

        @JsonProperty("total_days")
        private Integer totalDays;

        public Integer getReactionMinutes() {
            return reactionMinutes;
        }

        public void setReactionMinutes(Integer reactionMinutes) {
            this.reactionMinutes = reactionMinutes;
        }

        public Integer getToAssemblyHours() {
            return toAssemblyHours;
        }

        public void setToAssemblyHours(Integer toAssemblyHours) {
            this.toAssemblyHours = toAssemblyHours;
        }

        public Integer getAssemblyToDeliveryDays() {
            return assemblyToDeliveryDays;
        }

        public void setAssemblyToDeliveryDays(Integer assemblyToDeliveryDays) {
            this.assemblyToDeliveryDays = assemblyToDeliveryDays;
        }

        public Integer getTotalDays() {
            return totalDays;
        }

        public void setTotalDays(Integer totalDays) {
            this.totalDays = totalDays;
        }
    }

    public static class DeliveryConfigDto {
        @JsonProperty("to_pvz_days")
        private Integer toPvzDays;

        @JsonProperty("pvz_storage_days")
        private Integer pvzStorageDays;

        @JsonProperty("total_days")
        private Integer totalDays;

        public Integer getToPvzDays() {
            return toPvzDays;
        }

        public void setToPvzDays(Integer toPvzDays) {
            this.toPvzDays = toPvzDays;
        }

        public Integer getPvzStorageDays() {
            return pvzStorageDays;
        }

        public void setPvzStorageDays(Integer pvzStorageDays) {
            this.pvzStorageDays = pvzStorageDays;
        }

        public Integer getTotalDays() {
            return totalDays;
        }

        public void setTotalDays(Integer totalDays) {
            this.totalDays = totalDays;
        }
    }

    public static class BreachBucketsDto {
        @JsonProperty("short_minutes")
        private List<Integer> shortMinutes;

        @JsonProperty("days")
        private List<Integer> days;

        public List<Integer> getShortMinutes() {
            return shortMinutes;
        }

        public void setShortMinutes(List<Integer> shortMinutes) {
            this.shortMinutes = shortMinutes;
        }

        public List<Integer> getDays() {
            return days;
        }

        public void setDays(List<Integer> days) {
            this.days = days;
        }
    }
}