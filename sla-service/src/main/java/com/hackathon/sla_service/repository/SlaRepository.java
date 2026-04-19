package com.hackathon.sla_service.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.hackathon.sla_service.repository.model.LeadTimelineRow;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;


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

    public List<String> getDistinctManagerIds(LocalDate dateFrom,
                                              LocalDate dateTo,
                                              String qualification) {
        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT b2c_manager_id
            FROM leads
            WHERE sale_date BETWEEN ? AND ?
              AND lifecycle_incomplete = FALSE
              AND b2c_manager_id IS NOT NULL
              AND b2c_manager_id != ''
        """);

        List<Object> params = new ArrayList<>();
        params.add(Date.valueOf(dateFrom));
        params.add(Date.valueOf(dateTo));

        if (qualification != null && !qualification.isBlank()) {
            sql.append(" AND qualification = ?");
            params.add(qualification);
        } else {
            sql.append(" AND qualification IN ('A', 'B', 'C')");
        }

        sql.append(" ORDER BY b2c_manager_id");

        return jdbcTemplate.queryForList(sql.toString(), String.class, params.toArray());
    }

    public List<Double> getSla4Values(LocalDate dateFrom,
                                      LocalDate dateTo,
                                      String managerId,
                                      String deliveryService) {
        return queryDeliveryMetricValues(
                "EXTRACT(EPOCH FROM (issued_or_pvz_ts - handed_to_delivery_ts)) / 60.0 / 24.0",
                dateFrom,
                dateTo,
                managerId,
                deliveryService,
                "issued_or_pvz_ts"
        );
    }

    public List<Double> getSla5Values(LocalDate dateFrom,
                                      LocalDate dateTo,
                                      String managerId,
                                      String deliveryService) {
        return queryDeliveryMetricValues(
                "EXTRACT(EPOCH FROM (COALESCE(received_ts, rejected_ts, returned_ts) - issued_or_pvz_ts)) / 60.0 / 24.0",
                dateFrom,
                dateTo,
                managerId,
                deliveryService,
                "issued_or_pvz_ts"
        );
    }

    public List<Double> getDeliveryTotalValues(LocalDate dateFrom,
                                               LocalDate dateTo,
                                               String managerId,
                                               String deliveryService) {
        return queryDeliveryMetricValues(
                "EXTRACT(EPOCH FROM (COALESCE(received_ts, rejected_ts, returned_ts) - handed_to_delivery_ts)) / 60.0 / 24.0",
                dateFrom,
                dateTo,
                managerId,
                deliveryService,
                "handed_to_delivery_ts"
        );
    }

    private List<Double> queryDeliveryMetricValues(String intervalExpression,
                                                   LocalDate dateFrom,
                                                   LocalDate dateTo,
                                                   String managerId,
                                                   String deliveryService,
                                                   String requiredField) {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT metric_value FROM ( SELECT ");
        sql.append(intervalExpression);
        sql.append(" AS metric_value FROM leads WHERE sale_date BETWEEN ? AND ? ");
        sql.append("AND lifecycle_incomplete = FALSE AND outcome_unknown = FALSE ");
        sql.append("AND ").append(requiredField).append(" IS NOT NULL ");
        sql.append("AND handed_to_delivery_ts IS NOT NULL ");

        List<Object> params = new ArrayList<>();
        params.add(Date.valueOf(dateFrom));
        params.add(Date.valueOf(dateTo));

        if (managerId != null && !managerId.isBlank()) {
            sql.append(" AND delivery_manager_id = ? ");
            params.add(managerId);
        }

        if (deliveryService != null && !deliveryService.isBlank()) {
            sql.append(" AND delivery_service = ? ");
            params.add(deliveryService);
        }

        sql.append(") t WHERE metric_value IS NOT NULL AND metric_value >= 0 ORDER BY metric_value");

        return jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> rs.getDouble("metric_value"),
                params.toArray()
        );
    }

    public List<String> getDistinctDeliveryManagerIds(LocalDate dateFrom,
                                                      LocalDate dateTo,
                                                      String deliveryService) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT delivery_manager_id FROM leads ");
        sql.append("WHERE sale_date BETWEEN ? AND ? ");
        sql.append("AND lifecycle_incomplete = FALSE AND outcome_unknown = FALSE ");
        sql.append("AND delivery_manager_id IS NOT NULL AND delivery_manager_id != '' ");

        List<Object> params = new ArrayList<>();
        params.add(Date.valueOf(dateFrom));
        params.add(Date.valueOf(dateTo));

        if (deliveryService != null && !deliveryService.isBlank()) {
            sql.append(" AND delivery_service = ? ");
            params.add(deliveryService);
        }

        sql.append(" ORDER BY delivery_manager_id");

        return jdbcTemplate.queryForList(sql.toString(), String.class, params.toArray());
    }

    public List<Double> getFullCycleValues(LocalDate dateFrom,
                                           LocalDate dateTo) {
        String sql = """
            SELECT metric_value
            FROM (
                SELECT EXTRACT(EPOCH FROM (closed_ts - created_at)) / 60.0 / 24.0 AS metric_value
                FROM leads
                WHERE sale_date BETWEEN ? AND ?
                  AND lifecycle_incomplete = FALSE
                  AND outcome_unknown = FALSE
                  AND created_at IS NOT NULL
                  AND closed_ts IS NOT NULL
            ) t
            WHERE metric_value IS NOT NULL
              AND metric_value >= 0
            ORDER BY metric_value
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getDouble("metric_value"),
                Date.valueOf(dateFrom),
                Date.valueOf(dateTo)
        );
    }

    public LeadTimelineRow getLeadTimeline(String leadId) {
        String sql = """
        SELECT
            id,
            lifecycle_incomplete,
            outcome_unknown,
            created_at,
            sale_ts,
            to_assembly_ts,
            handed_to_delivery_ts,
            issued_or_pvz_ts,
            received_ts,
            rejected_ts,
            returned_ts,
            closed_ts
        FROM leads
        WHERE id = ?
        """;

        List<LeadTimelineRow> rows = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    LeadTimelineRow row = new LeadTimelineRow();
                    row.setLeadId(rs.getString("id"));
                    row.setLifecycleIncomplete(rs.getBoolean("lifecycle_incomplete"));
                    row.setOutcomeUnknown(rs.getBoolean("outcome_unknown"));
                    row.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
                    row.setSaleTs(toLocalDateTime(rs.getTimestamp("sale_ts")));
                    row.setToAssemblyTs(toLocalDateTime(rs.getTimestamp("to_assembly_ts")));
                    row.setHandedToDeliveryTs(toLocalDateTime(rs.getTimestamp("handed_to_delivery_ts")));
                    row.setIssuedOrPvzTs(toLocalDateTime(rs.getTimestamp("issued_or_pvz_ts")));
                    row.setReceivedTs(toLocalDateTime(rs.getTimestamp("received_ts")));
                    row.setRejectedTs(toLocalDateTime(rs.getTimestamp("rejected_ts")));
                    row.setReturnedTs(toLocalDateTime(rs.getTimestamp("returned_ts")));
                    row.setClosedTs(toLocalDateTime(rs.getTimestamp("closed_ts")));
                    return row;
                },
                leadId
        );

        return rows.isEmpty() ? null : rows.get(0);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}

