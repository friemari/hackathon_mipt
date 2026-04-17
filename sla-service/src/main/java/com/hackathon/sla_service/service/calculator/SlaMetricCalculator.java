package com.hackathon.sla_service.service.calculator;

import com.hackathon.sla_service.config.SlaConfigProperties;
import com.hackathon.sla_service.dto.common.BreachDistributionDto;
import com.hackathon.sla_service.dto.common.SummaryMetricDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SlaMetricCalculator {

    private final SlaConfigProperties slaConfig;

    public SlaMetricCalculator(SlaConfigProperties slaConfig) {
        this.slaConfig = slaConfig;
    }

    public SummaryMetricDto calculate(List<Double> values, int thresholdMinutes) {
        SummaryMetricDto dto = new SummaryMetricDto();

        long total = values.size();
        long metCount = values.stream().filter(v -> v <= thresholdMinutes).count();
        long breachCount = total - metCount;

        dto.setThresholdMinutes(thresholdMinutes);
        dto.setTotalOrders(total);
        dto.setMetCount(metCount);
        dto.setMetPercent(percent(metCount, total));
        dto.setBreachCount(breachCount);
        dto.setBreachPercent(percent(breachCount, total));
        dto.setAvgMinutes(round(avg(values)));
        dto.setMedianMinutes(round(percentile(values, 0.50)));
        dto.setP90Minutes(round(percentile(values, 0.90)));
        dto.setBreachDistribution(buildDistribution(values, thresholdMinutes));

        return dto;
    }

    public SummaryMetricDto calculateDays(List<Double> values, int thresholdDays) {
        SummaryMetricDto dto = new SummaryMetricDto();

        long total = values.size();
        long metCount = values.stream().filter(v -> v <= thresholdDays).count();
        long breachCount = total - metCount;

        dto.setThresholdMinutes(thresholdDays);
        dto.setTotalOrders(total);
        dto.setMetCount(metCount);
        dto.setMetPercent(percent(metCount, total));
        dto.setBreachCount(breachCount);
        dto.setBreachPercent(percent(breachCount, total));
        dto.setAvgMinutes(round(avg(values)));
        dto.setMedianMinutes(round(percentile(values, 0.50)));
        dto.setP90Minutes(round(percentile(values, 0.90)));

        // Build distribution for days
        BreachDistributionDto distribution = new BreachDistributionDto();
        List<Integer> buckets = slaConfig.getBreachBuckets().getDays();
        int bucket1 = buckets.get(0);
        int bucket2 = buckets.get(1);

        long upToBucket1 = values.stream()
                .filter(v -> v > thresholdDays)
                .mapToDouble(v -> v - thresholdDays)
                .filter(breach -> breach > 0 && breach <= bucket1)
                .count();

        long fromBucket1ToBucket2 = values.stream()
                .filter(v -> v > thresholdDays)
                .mapToDouble(v -> v - thresholdDays)
                .filter(breach -> breach > bucket1 && breach <= bucket2)
                .count();

        long overBucket2 = values.stream()
                .filter(v -> v > thresholdDays)
                .mapToDouble(v -> v - thresholdDays)
                .filter(breach -> breach > bucket2)
                .count();

        distribution.setUpTo15Min((int) upToBucket1);
        distribution.setFrom15To60Min((int) fromBucket1ToBucket2);
        distribution.setOver60Min((int) overBucket2);

        dto.setBreachDistribution(distribution);

        return dto;
    }

    private BreachDistributionDto buildDistribution(List<Double> values, int thresholdMinutes) {
        long upTo15 = values.stream()
                .filter(v -> v > thresholdMinutes)
                .mapToDouble(v -> v - thresholdMinutes)
                .filter(breach -> breach > 0 && breach <= 15)
                .count();

        long from15To60 = values.stream()
                .filter(v -> v > thresholdMinutes)
                .mapToDouble(v -> v - thresholdMinutes)
                .filter(breach -> breach > 15 && breach <= 60)
                .count();

        long over60 = values.stream()
                .filter(v -> v > thresholdMinutes)
                .mapToDouble(v -> v - thresholdMinutes)
                .filter(breach -> breach > 60)
                .count();

        BreachDistributionDto dto = new BreachDistributionDto();
        dto.setUpTo15Min((int) upTo15);
        dto.setFrom15To60Min((int) from15To60);
        dto.setOver60Min((int) over60);

        return dto;
    }

    private double avg(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        }
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private double percentile(List<Double> sortedValues, double percentile) {
        if (sortedValues.isEmpty()) {
            return 0.0;
        }

        if (sortedValues.size() == 1) {
            return sortedValues.get(0);
        }

        double index = percentile * (sortedValues.size() - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);

        if (lower == upper) {
            return sortedValues.get(lower);
        }

        double weight = index - lower;
        return sortedValues.get(lower) * (1 - weight) + sortedValues.get(upper) * weight;
    }

    private double percent(long part, long total) {
        if (total == 0) {
            return 0.0;
        }
        return round((part * 100.0) / total);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}