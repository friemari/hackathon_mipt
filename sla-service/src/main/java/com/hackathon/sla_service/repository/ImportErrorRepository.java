package com.hackathon.sla_service.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ImportErrorRepository {

    private final JdbcTemplate jdbcTemplate;

    public ImportErrorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveError(long batchId,
                          int rowNumber,
                          String leadId,
                          String errorMessage) {
        String sql = """
                INSERT INTO import_errors (
                    batch_id,
                    row_number,
                    lead_id,
                    error_message
                )
                VALUES (?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                batchId,
                rowNumber,
                leadId,
                errorMessage
        );
    }
}