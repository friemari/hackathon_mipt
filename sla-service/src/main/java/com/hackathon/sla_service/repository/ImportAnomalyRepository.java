package com.hackathon.sla_service.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ImportAnomalyRepository {

    private final JdbcTemplate jdbcTemplate;

    public ImportAnomalyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveMismatch(long batchId,
                             String leadId,
                             String fieldName,
                             String expectedValue,
                             String actualValue,
                             String message) {
        String sql = """
                INSERT INTO import_anomalies(
                    batch_id, lead_id, field_name, expected_value, actual_value, message
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                batchId,
                leadId,
                fieldName,
                expectedValue,
                actualValue,
                message
        );
    }
}