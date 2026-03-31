package com.hackathon.sla_service.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class SummaryPeriodDto {
    @JsonProperty("from")
    private LocalDate from;

    @JsonProperty("to")
    private LocalDate to;

    public SummaryPeriodDto() {
    }

    public SummaryPeriodDto(LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }
}
