package com.hackathon.sla_service.service.calculator;

import com.hackathon.sla_service.dto.common.BreachDistributionDto;
import com.hackathon.sla_service.dto.common.SummaryMetricDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SlaMetricCalculator {

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