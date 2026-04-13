package com.hackathon.sla_service.dto;

public class DataLoadResponse {

    private String fileName;
    private int totalRows;
    private int insertedRows;
    private int updatedRows;
    private int skippedRows;
    private int errorRows;
    private long batchId;
    private String status;

    public DataLoadResponse() {
    }

    public DataLoadResponse(String fileName,
                            int totalRows,
                            int insertedRows,
                            int updatedRows,
                            int skippedRows,
                            int errorRows,
                            long batchId,
                            String status) {
        this.fileName = fileName;
        this.totalRows = totalRows;
        this.insertedRows = insertedRows;
        this.updatedRows = updatedRows;
        this.skippedRows = skippedRows;
        this.errorRows = errorRows;
        this.batchId = batchId;
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getInsertedRows() {
        return insertedRows;
    }

    public int getUpdatedRows() {
        return updatedRows;
    }

    public int getSkippedRows() {
        return skippedRows;
    }

    public int getErrorRows() {
        return errorRows;
    }

    public long getBatchId() {
        return batchId;
    }

    public String getStatus() {
        return status;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public void setInsertedRows(int insertedRows) {
        this.insertedRows = insertedRows;
    }

    public void setUpdatedRows(int updatedRows) {
        this.updatedRows = updatedRows;
    }

    public void setSkippedRows(int skippedRows) {
        this.skippedRows = skippedRows;
    }

    public void setErrorRows(int errorRows) {
        this.errorRows = errorRows;
    }

    public void setBatchId(long batchId) {
        this.batchId = batchId;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}