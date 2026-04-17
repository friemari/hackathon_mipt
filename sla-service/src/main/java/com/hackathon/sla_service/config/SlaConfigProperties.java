package com.hackathon.sla_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "sla")
public class SlaConfigProperties {

    private B2cConfig b2c = new B2cConfig();
    private DeliveryConfig delivery = new DeliveryConfig();
    private Integer fullCycleDays = 16;
    private BreachBucketsConfig breachBuckets = new BreachBucketsConfig();

    // Getters and Setters
    public B2cConfig getB2c() { return b2c; }
    public void setB2c(B2cConfig b2c) { this.b2c = b2c; }

    public DeliveryConfig getDelivery() { return delivery; }
    public void setDelivery(DeliveryConfig delivery) { this.delivery = delivery; }

    public Integer getFullCycleDays() { return fullCycleDays; }
    public void setFullCycleDays(Integer fullCycleDays) { this.fullCycleDays = fullCycleDays; }

    public BreachBucketsConfig getBreachBuckets() { return breachBuckets; }
    public void setBreachBuckets(BreachBucketsConfig breachBuckets) { this.breachBuckets = breachBuckets; }

    public static class B2cConfig {
        private Integer reactionMinutes = 30;
        private Integer toAssemblyHours = 4;
        private Integer assemblyToDeliveryDays = 1;
        private Integer totalDays = 2;

        public Integer getReactionMinutes() { return reactionMinutes; }
        public void setReactionMinutes(Integer reactionMinutes) { this.reactionMinutes = reactionMinutes; }
        public Integer getToAssemblyHours() { return toAssemblyHours; }
        public void setToAssemblyHours(Integer toAssemblyHours) { this.toAssemblyHours = toAssemblyHours; }
        public Integer getAssemblyToDeliveryDays() { return assemblyToDeliveryDays; }
        public void setAssemblyToDeliveryDays(Integer assemblyToDeliveryDays) { this.assemblyToDeliveryDays = assemblyToDeliveryDays; }
        public Integer getTotalDays() { return totalDays; }
        public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }
    }

    public static class DeliveryConfig {
        private Integer toPvzDays = 5;
        private Integer pvzStorageDays = 7;
        private Integer totalDays = 14;

        public Integer getToPvzDays() { return toPvzDays; }
        public void setToPvzDays(Integer toPvzDays) { this.toPvzDays = toPvzDays; }
        public Integer getPvzStorageDays() { return pvzStorageDays; }
        public void setPvzStorageDays(Integer pvzStorageDays) { this.pvzStorageDays = pvzStorageDays; }
        public Integer getTotalDays() { return totalDays; }
        public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }
    }

    public static class BreachBucketsConfig {
        private List<Integer> shortMinutes = List.of(15, 60);
        private List<Integer> days = List.of(1, 3);

        public List<Integer> getShortMinutes() { return shortMinutes; }
        public void setShortMinutes(List<Integer> shortMinutes) { this.shortMinutes = shortMinutes; }
        public List<Integer> getDays() { return days; }
        public void setDays(List<Integer> days) { this.days = days; }
    }
}