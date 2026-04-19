package com.hackathon.sla_service.importer;

import com.hackathon.sla_service.dto.DataLoadResponse;
import com.hackathon.sla_service.importer.model.CsvLeadRow;
import com.hackathon.sla_service.repository.ImportAnomalyRepository;
import com.hackathon.sla_service.repository.ImportBatchRepository;
import com.hackathon.sla_service.repository.ImportErrorRepository;
import com.hackathon.sla_service.repository.LeadRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class CsvImportService {

    private final CsvLeadRowMapper rowMapper;
    private final LeadRepository leadRepository;
    private final ImportBatchRepository importBatchRepository;
    private final ImportAnomalyRepository importAnomalyRepository;
    private final ImportErrorRepository importErrorRepository;

    public CsvImportService(CsvLeadRowMapper rowMapper,
                            LeadRepository leadRepository,
                            ImportBatchRepository importBatchRepository,
                            ImportAnomalyRepository importAnomalyRepository,
                            ImportErrorRepository importErrorRepository
                            ) {
        this.rowMapper = rowMapper;
        this.leadRepository = leadRepository;
        this.importBatchRepository = importBatchRepository;
        this.importAnomalyRepository = importAnomalyRepository;
        this.importErrorRepository = importErrorRepository;
    }

    public DataLoadResponse importFile(MultipartFile file) {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown.csv";
        long batchId = importBatchRepository.createBatch(fileName);

        int totalRows = 0;
        int insertedRows = 0;
        int updatedRows = 0;
        int skippedRows = 0;
        int errorRows = 0;

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
                );
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreEmptyLines(true)
                        .setTrim(true)
                        .build()
                        .parse(reader)
        ) {
            for (CSVRecord record : parser) {
                totalRows++;

                try {
                    CsvLeadRow row = rowMapper.map(record);

                    if (row.getLeadId() == null || row.getSaleDate() == null) {
                        skippedRows++;
                        continue;
                    }

                    boolean existedBefore = leadRepository.existsById(row.getLeadId());

                    leadRepository.upsertLeadGroup(row.getGroupId(), row.getGroupName());

                    if (row.getHandedToDeliveryTs() != null
                            && row.getHandedToDeliveryAltTs() != null
                            && !row.getHandedToDeliveryTs().equals(row.getHandedToDeliveryAltTs())) {
                        importAnomalyRepository.saveMismatch(
                                batchId,
                                row.getLeadId(),
                                "handed_to_delivery_ts",
                                String.valueOf(row.getHandedToDeliveryTs()),
                                String.valueOf(row.getHandedToDeliveryAltTs()),
                                "Расхождение между handed_to_delivery_ts и lead_Дата перехода Передан в доставку"
                        );
                    }

                    leadRepository.upsertLead(row);

                    if (existedBefore) {
                        updatedRows++;
                    } else {
                        insertedRows++;
                    }

                } catch (Exception e) {
                    errorRows++;

                    String leadId = safeGet(record, "lead_id");
                    String errorMessage = buildErrorMessage(e);

                    importErrorRepository.saveError(
                            batchId,
                            totalRows,
                            leadId,
                            errorMessage
                    );

                    System.err.println("Ошибка импорта строки #" + totalRows);
                    System.err.println("lead_id = " + record.get("lead_id"));
                    System.err.println("Причина: " + e.getMessage());
                    e.printStackTrace();
                }
            }


            importBatchRepository.finishBatch(
                    batchId,
                    totalRows,
                    insertedRows,
                    updatedRows,
                    skippedRows,
                    errorRows,
                    "DONE"
            );

            return new DataLoadResponse(
                    fileName,
                    totalRows,
                    insertedRows,
                    updatedRows,
                    skippedRows,
                    errorRows,
                    batchId,
                    "DONE"
            );

        } catch (
                Exception e) {
            importBatchRepository.finishBatch(
                    batchId,
                    totalRows,
                    insertedRows,
                    updatedRows,
                    skippedRows,
                    errorRows + 1,
                    "FAILED"
            );

            throw new RuntimeException("Ошибка загрузки CSV: " + e.getMessage(), e);
        }
    }
    private String safeGet(CSVRecord record, String fieldName) {
        try {
            String value = record.get(fieldName);
            return value == null || value.isBlank() ? null : value;
        } catch (Exception e) {
            return null;
        }
    }

    private String buildErrorMessage(Exception e) {
        if (e.getMessage() == null || e.getMessage().isBlank()) {
            return e.getClass().getSimpleName();
        }
        return e.getClass().getSimpleName() + ": " + e.getMessage();
    }

}