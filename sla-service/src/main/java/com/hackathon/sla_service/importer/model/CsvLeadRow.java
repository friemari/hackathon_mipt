package com.hackathon.sla_service.importer.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CsvLeadRow {

    private String leadId;
    private String groupId;
    private String groupName;

    private String b2cManagerId;
    private String deliveryManagerId;

    private String qualification;
    private String deliveryService;
    private String city;

    private LocalDate saleDate;

    private LocalDateTime createdAt;
    private LocalDateTime saleTs;
    private LocalDateTime toAssemblyTs;
    private LocalDateTime handedToDeliveryTs;
    private LocalDateTime handedToDeliveryAltTs;
    private LocalDateTime issuedOrPvzTs;
    private LocalDateTime receivedTs;
    private LocalDateTime rejectedTs;
    private LocalDateTime returnedTs;
    private LocalDateTime closedTs;

    private Boolean lifecycleIncomplete;
    private Boolean outcomeUnknown;
    private Boolean buyoutFlag;

    public String getLeadId() {
        return leadId;
    }

    public void setLeadId(String leadId) {
        this.leadId = leadId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getB2cManagerId() {
        return b2cManagerId;
    }

    public void setB2cManagerId(String b2cManagerId) {
        this.b2cManagerId = b2cManagerId;
    }

    public String getDeliveryManagerId() {
        return deliveryManagerId;
    }

    public void setDeliveryManagerId(String deliveryManagerId) {
        this.deliveryManagerId = deliveryManagerId;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getDeliveryService() {
        return deliveryService;
    }

    public void setDeliveryService(String deliveryService) {
        this.deliveryService = deliveryService;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
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

    public LocalDateTime getHandedToDeliveryAltTs() {
        return handedToDeliveryAltTs;
    }

    public void setHandedToDeliveryAltTs(LocalDateTime handedToDeliveryAltTs) {
        this.handedToDeliveryAltTs = handedToDeliveryAltTs;
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

    public Boolean getBuyoutFlag() {
        return buyoutFlag;
    }

    public void setBuyoutFlag(Boolean buyoutFlag) {
        this.buyoutFlag = buyoutFlag;
    }
}