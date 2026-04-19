package com.hackathon.sla_service.repository.model;

import java.time.LocalDateTime;

public class LeadTimelineRow {

    private String leadId;
    private Boolean lifecycleIncomplete;
    private Boolean outcomeUnknown;

    private LocalDateTime createdAt;
    private LocalDateTime saleTs;
    private LocalDateTime toAssemblyTs;
    private LocalDateTime handedToDeliveryTs;
    private LocalDateTime issuedOrPvzTs;
    private LocalDateTime receivedTs;
    private LocalDateTime rejectedTs;
    private LocalDateTime returnedTs;
    private LocalDateTime closedTs;

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