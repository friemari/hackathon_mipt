package com.hackathon.sla_service.service.calculator;

import com.hackathon.sla_service.config.SlaConfigProperties;
import com.hackathon.sla_service.dto.common.SummaryMetricDto;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        dto.setThresholdValue(thresholdMinutes);
        dto.setUnit("minutes");
        dto.setTotalOrders(total);
        dto.setMetCount(metCount);
        dto.setMetPercent(percent(metCount, total));
        dto.setBreachCount(breachCount);
        dto.setBreachPercent(percent(breachCount, total));
        dto.setAvgValue(round(avg(values)));
        dto.setMedianValue(round(percentile(values, 0.50)));
        dto.setP90Value(round(percentile(values, 0.90)));
        dto.setBreachDistribution(buildMinutesDistribution(values, thresholdMinutes));

        return dto;
    }

    public SummaryMetricDto calculateDays(List<Double> values, int thresholdDays) {
        SummaryMetricDto dto = new SummaryMetricDto();

        long total = values.size();
        long metCount = values.stream().filter(v -> v <= thresholdDays).count();
        long breachCount = total - metCount;

        dto.setThresholdValue(thresholdDays);
        dto.setUnit("days");
        dto.setTotalOrders(total);
        dto.setMetCount(metCount);
        dto.setMetPercent(percent(metCount, total));
        dto.setBreachCount(breachCount);
        dto.setBreachPercent(percent(breachCount, total));
        dto.setAvgValue(round(avg(values)));
        dto.setMedianValue(round(percentile(values, 0.50)));
        dto.setP90Value(round(percentile(values, 0.90)));
        dto.setBreachDistribution(buildDaysDistribution(values, thresholdDays));

        return dto;
    }

    private Map<String, Integer> buildMinutesDistribution(List<Double> values, int thresholdMinutes) {
        List<Integer> buckets = slaConfig.getBreachBuckets().getShortMinutes();
        int b1 = buckets.get(0);
        int b2 = buckets.get(1);

        long c1 = 0;
        long c2 = 0;
        long c3 = 0;

        for (double value : values) {
            if (value <= thresholdMinutes) {
                continue;
            }

            double breach = value - thresholdMinutes;

            if (breach > 0 && breach <= b1) {
                c1++;
            } else if (breach <= b2) {
                c2++;
            } else {
                c3++;
            }
        }

        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("0-" + b1 + "_min", (int) c1);
        result.put(b1 + "-" + b2 + "_min", (int) c2);
        result.put("over_" + b2 + "_min", (int) c3);

        return result;
    }

    private Map<String, Integer> buildDaysDistribution(List<Double> values, int thresholdDays) {
        List<Integer> buckets = slaConfig.getBreachBuckets().getDays();
        int b1 = buckets.get(0);
        int b2 = buckets.get(1);

        long c1 = 0;
        long c2 = 0;
        long c3 = 0;

        for (double value : values) {
            if (value <= thresholdDays) {
                continue;
            }

            double breach = value - thresholdDays;

            if (breach > 0 && breach <= b1) {
                c1++;
            } else if (breach <= b2) {
                c2++;
            } else {
                c3++;
            }
        }

        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("0-" + b1 + "_days", (int) c1);
        result.put(b1 + "-" + b2 + "_days", (int) c2);
        result.put("over_" + b2 + "_days", (int) c3);

        return result;
    }

    private double avg(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        }
        return values.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
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