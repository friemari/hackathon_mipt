package com.hackathon.sla_service.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SlaRepository {

    private final JdbcTemplate jdbcTemplate;

    public SlaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Double> getSla1Values(LocalDate dateFrom,
                                      LocalDate dateTo,
                                      String managerId,
                                      String qualification) {
        return queryMetricValues(
                "EXTRACT(EPOCH FROM (sale_ts - created_at)) / 60.0",
                dateFrom,
                dateTo,
                managerId,
                qualification
        );
    }

    public List<Double> getSla2Values(LocalDate dateFrom,
                                      LocalDate dateTo,
                                      String managerId,
                                      String qualification) {
        return queryMetricValues(
                "EXTRACT(EPOCH FROM (to_assembly_ts - sale_ts)) / 60.0",
                dateFrom,
                dateTo,
                managerId,
                qualification
        );
    }

    public List<Double> getSla3Values(LocalDate dateFrom,
                                      LocalDate dateTo,
                                      String managerId,
                                      String qualification) {
        return queryMetricValues(
                "EXTRACT(EPOCH FROM (handed_to_delivery_ts - to_assembly_ts)) / 60.0",
                dateFrom,
                dateTo,
                managerId,
                qualification
        );
    }

    public List<Double> getB2cTotalValues(LocalDate dateFrom,
                                          LocalDate dateTo,
                                          String managerId,
                                          String qualification) {
        return queryMetricValues(
                "EXTRACT(EPOCH FROM (handed_to_delivery_ts - created_at)) / 60.0",
                dateFrom,
                dateTo,
                managerId,
                qualification
        );
    }

    private List<Double> queryMetricValues(String intervalExpression,
                                           LocalDate dateFrom,
                                           LocalDate dateTo,
                                           String managerId,
                                           String qualification) {

        StringBuilder sql = new StringBuilder("""
            SELECT metric_value
            FROM (
                SELECT
                    """).append(intervalExpression).append("""
                    AS metric_value
                FROM leads
                WHERE sale_date BETWEEN ? AND ?
                  AND lifecycle_incomplete = FALSE
            """);

        List<Object> params = new ArrayList<>();
        params.add(Date.valueOf(dateFrom));
        params.add(Date.valueOf(dateTo));

        if (managerId != null && !managerId.isBlank()) {
            sql.append(" AND b2c_manager_id = ? ");
            params.add(managerId);
        }

        if (qualification != null && !qualification.isBlank()) {
            sql.append(" AND qualification = ? ");
            params.add(qualification);
        } else {
            sql.append(" AND qualification IN ('A', 'B', 'C') ");
        }

        sql.append("""
            ) t
            WHERE metric_value IS NOT NULL
              AND metric_value >= 0
            ORDER BY metric_value
            """);

        return jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> rs.getDouble("metric_value"),
                params.toArray()
        );
    }
}