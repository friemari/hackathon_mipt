package com.hackathon.sla_service.repository;

import com.hackathon.sla_service.importer.model.CsvLeadRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Repository
public class LeadRepository {

    private final JdbcTemplate jdbcTemplate;

    public LeadRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsById(String leadId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM leads WHERE id = ?",
                Integer.class,
                leadId
        );
        return count != null && count > 0;
    }

    public void upsertLeadGroup(String groupId, String groupName) {
        if (groupId == null) {
            return;
        }

        String sql = """
                INSERT INTO lead_groups(id, name)
                VALUES (?, ?)
                ON CONFLICT (id) DO UPDATE
                SET name = EXCLUDED.name
                """;

        jdbcTemplate.update(sql, groupId, groupName != null ? groupName : "UNKNOWN");
    }

    public void upsertLead(CsvLeadRow row) {
        String sql = """
                INSERT INTO leads (
                    id,
                    group_id,
                    b2c_manager_id,
                    delivery_manager_id,
                    qualification,
                    delivery_service,
                    city,
                    sale_date,
                    created_at,
                    sale_ts,
                    to_assembly_ts,
                    handed_to_delivery_ts,
                    handed_to_delivery_alt_ts,
                    issued_or_pvz_ts,
                    received_ts,
                    rejected_ts,
                    returned_ts,
                    closed_ts,
                    lifecycle_incomplete,
                    outcome_unknown,
                    buyout_flag
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    group_id = EXCLUDED.group_id,
                    b2c_manager_id = EXCLUDED.b2c_manager_id,
                    delivery_manager_id = EXCLUDED.delivery_manager_id,
                    qualification = EXCLUDED.qualification,
                    delivery_service = EXCLUDED.delivery_service,
                    city = EXCLUDED.city,
                    sale_date = EXCLUDED.sale_date,
                    created_at = EXCLUDED.created_at,
                    sale_ts = EXCLUDED.sale_ts,
                    to_assembly_ts = EXCLUDED.to_assembly_ts,
                    handed_to_delivery_ts = EXCLUDED.handed_to_delivery_ts,
                    handed_to_delivery_alt_ts = EXCLUDED.handed_to_delivery_alt_ts,
                    issued_or_pvz_ts = EXCLUDED.issued_or_pvz_ts,
                    received_ts = EXCLUDED.received_ts,
                    rejected_ts = EXCLUDED.rejected_ts,
                    returned_ts = EXCLUDED.returned_ts,
                    closed_ts = EXCLUDED.closed_ts,
                    lifecycle_incomplete = EXCLUDED.lifecycle_incomplete,
                    outcome_unknown = EXCLUDED.outcome_unknown,
                    buyout_flag = EXCLUDED.buyout_flag
                """;

        jdbcTemplate.update(sql,
                row.getLeadId(),
                row.getGroupId(),
                row.getB2cManagerId(),
                row.getDeliveryManagerId(),
                row.getQualification(),
                row.getDeliveryService(),
                row.getCity(),
                row.getSaleDate(),
                toTimestamp(row.getCreatedAt()),
                toTimestamp(row.getSaleTs()),
                toTimestamp(row.getToAssemblyTs()),
                toTimestamp(row.getHandedToDeliveryTs()),
                toTimestamp(row.getHandedToDeliveryAltTs()),
                toTimestamp(row.getIssuedOrPvzTs()),
                toTimestamp(row.getReceivedTs()),
                toTimestamp(row.getRejectedTs()),
                toTimestamp(row.getReturnedTs()),
                toTimestamp(row.getClosedTs()),
                Boolean.TRUE.equals(row.getLifecycleIncomplete()),
                Boolean.TRUE.equals(row.getOutcomeUnknown()),
                Boolean.TRUE.equals(row.getBuyoutFlag())
        );
    }

    private Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }
}