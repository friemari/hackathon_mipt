package com.hackathon.sla_service.controller;

import com.hackathon.sla_service.dto.DataLoadRequest;
import com.hackathon.sla_service.dto.DataLoadResponse;
import com.hackathon.sla_service.service.DataLoadService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;

@RestController
@RequestMapping("/api/data")
public class DataLoadController {

    private final DataLoadService dataLoadService;

    public DataLoadController(DataLoadService dataLoadService) {
        this.dataLoadService = dataLoadService;
    }

    @Operation(summary = "Загрузка CSV датасета")
    @PostMapping(value = "/load", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DataLoadResponse> load(@ModelAttribute DataLoadRequest request) {
        DataLoadResponse response = dataLoadService.loadCsv(request.getFile());
        return ResponseEntity.ok(response);
    }
}