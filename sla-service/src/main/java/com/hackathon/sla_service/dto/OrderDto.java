package com.hackathon.sla_service.dto;

import java.time.LocalDateTime;

public class OrderDto {
    private String leadId;
    private LocalDateTime leadCreatedAt;
    private LocalDateTime saleTs;
    private LocalDateTime assemblyTs;
    private LocalDateTime handedToDeliveryTs;
    private LocalDateTime issuedOrPvzTs;
    private LocalDateTime receivedTs;
    private LocalDateTime rejectedTs;
    private LocalDateTime returnedTs;
    private LocalDateTime closedTs;
    private Boolean lifecycleIncomplete;
    private Boolean outcomeUnknown;
    private String leadResponsibleUserId;
    private String qualification;
    private String deliveryService;

    // Геттеры и сеттеры
    public String getLeadId() { return leadId; }
    public void setLeadId(String leadId) { this.leadId = leadId; }

    public LocalDateTime getLeadCreatedAt() { return leadCreatedAt; }
    public void setLeadCreatedAt(LocalDateTime leadCreatedAt) { this.leadCreatedAt = leadCreatedAt; }

    public LocalDateTime getSaleTs() { return saleTs; }
    public void setSaleTs(LocalDateTime saleTs) { this.saleTs = saleTs; }

    public LocalDateTime getAssemblyTs() { return assemblyTs; }
    public void setAssemblyTs(LocalDateTime assemblyTs) { this.assemblyTs = assemblyTs; }

    public LocalDateTime getHandedToDeliveryTs() { return handedToDeliveryTs; }
    public void setHandedToDeliveryTs(LocalDateTime handedToDeliveryTs) { this.handedToDeliveryTs = handedToDeliveryTs; }

    public LocalDateTime getIssuedOrPvzTs() { return issuedOrPvzTs; }
    public void setIssuedOrPvzTs(LocalDateTime issuedOrPvzTs) { this.issuedOrPvzTs = issuedOrPvzTs; }

    public LocalDateTime getReceivedTs() { return receivedTs; }
    public void setReceivedTs(LocalDateTime receivedTs) { this.receivedTs = receivedTs; }

    public LocalDateTime getRejectedTs() { return rejectedTs; }
    public void setRejectedTs(LocalDateTime rejectedTs) { this.rejectedTs = rejectedTs; }

    public LocalDateTime getReturnedTs() { return returnedTs; }
    public void setReturnedTs(LocalDateTime returnedTs) { this.returnedTs = returnedTs; }

    public LocalDateTime getClosedTs() { return closedTs; }
    public void setClosedTs(LocalDateTime closedTs) { this.closedTs = closedTs; }

    public Boolean getLifecycleIncomplete() { return lifecycleIncomplete; }
    public void setLifecycleIncomplete(Boolean lifecycleIncomplete) { this.lifecycleIncomplete = lifecycleIncomplete; }

    public Boolean getOutcomeUnknown() { return outcomeUnknown; }
    public void setOutcomeUnknown(Boolean outcomeUnknown) { this.outcomeUnknown = outcomeUnknown; }

    public String getLeadResponsibleUserId() { return leadResponsibleUserId; }
    public void setLeadResponsibleUserId(String leadResponsibleUserId) { this.leadResponsibleUserId = leadResponsibleUserId; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getDeliveryService() { return deliveryService; }
    public void setDeliveryService(String deliveryService) { this.deliveryService = deliveryService; }
}