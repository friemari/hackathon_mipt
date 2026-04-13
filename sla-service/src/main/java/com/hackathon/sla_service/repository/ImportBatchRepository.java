package com.hackathon.sla_service.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ImportBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    public ImportBatchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long createBatch(String fileName) {
        String sql = """
                INSERT INTO import_batches(file_name, status)
                VALUES (?, 'RUNNING')
                RETURNING id
                """;

        Long id = jdbcTemplate.queryForObject(sql, Long.class, fileName);

        if (id == null) {
            throw new IllegalStateException("Не удалось создать import batch");
        }

        return id;
    }

    public void finishBatch(long batchId,
                            int totalRows,
                            int insertedRows,
                            int updatedRows,
                            int skippedRows,
                            int errorRows,
                            String status) {
        String sql = """
                UPDATE import_batches
                SET finished_at = NOW(),
                    total_rows = ?,
                    inserted_rows = ?,
                    updated_rows = ?,
                    skipped_rows = ?,
                    error_rows = ?,
                    status = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql,
                totalRows,
                insertedRows,
                updatedRows,
                skippedRows,
                errorRows,
                status,
                batchId
        );
    }
}