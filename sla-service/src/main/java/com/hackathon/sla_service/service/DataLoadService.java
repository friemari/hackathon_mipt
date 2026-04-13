package com.hackathon.sla_service.service;

import com.hackathon.sla_service.dto.DataLoadResponse;
import com.hackathon.sla_service.importer.CsvImportService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DataLoadService {

    private final CsvImportService csvImportService;

    public DataLoadService(CsvImportService csvImportService) {
        this.csvImportService = csvImportService;
    }

    public DataLoadResponse loadCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл пустой");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Нужен CSV файл");
        }

        return csvImportService.importFile(file);
    }
}