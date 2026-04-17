package com.hackathon.sla_service.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ApiErrorResponse {

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("error")
    private String error;

    @JsonProperty("message")
    private String message;

    @JsonProperty("path")
    private String path;

    @JsonProperty("details")
    private Map<String, List<String>> details;

    @JsonProperty("trace_id")
    private String traceId;

    public ApiErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Map<String, List<String>> getDetails() { return details; }
    public void setDetails(Map<String, List<String>> details) { this.details = details; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    // ИСПРАВЛЕННЫЕ МЕТОДЫ:
    public static ApiErrorResponse badRequest(String message, String path) {
        ApiErrorResponse response = new ApiErrorResponse();
        response.setStatus(400);
        response.setError("Bad Request");
        response.setMessage(message);
        response.setPath(path);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static ApiErrorResponse notFound(String message, String path) {
        ApiErrorResponse response = new ApiErrorResponse();
        response.setStatus(404);
        response.setError("Not Found");
        response.setMessage(message);
        response.setPath(path);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static ApiErrorResponse internalError(String message, String path) {
        ApiErrorResponse response = new ApiErrorResponse();
        response.setStatus(500);
        response.setError("Internal Server Error");
        response.setMessage(message);
        response.setPath(path);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}