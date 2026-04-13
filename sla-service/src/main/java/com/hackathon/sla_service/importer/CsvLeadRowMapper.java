package com.hackathon.sla_service.importer;

import com.hackathon.sla_service.importer.model.CsvLeadRow;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class CsvLeadRowMapper {

    public CsvLeadRow map(CSVRecord record) {
        CsvLeadRow row = new CsvLeadRow();

        row.setLeadId(text(record, "lead_id"));

        row.setGroupId(normalizeId(text(record, "lead_group_id")));
        row.setGroupName(text(record, "lead_group"));

        row.setB2cManagerId(normalizeId(text(record, "lead_responsible_user_id")));
        row.setDeliveryManagerId(normalizeId(text(record, "lead_Ответственный за доставку")));

        row.setQualification(normalizeQualification(text(record, "lead_Квалификация лида")));
        row.setDeliveryService(text(record, "lead_Служба доставки"));
        row.setCity(text(record, "contact_Город"));

        row.setSaleDate(parseDate(text(record, "sale_date")));

        row.setCreatedAt(parseUnixTs(text(record, "lead_created_at")));
        row.setSaleTs(parseUnixTs(text(record, "sale_ts")));
        row.setToAssemblyTs(parseUnixTs(text(record, "lead_Дата перехода в Сборку")));
        row.setHandedToDeliveryTs(parseUnixTs(text(record, "handed_to_delivery_ts")));
        row.setHandedToDeliveryAltTs(parseUnixTs(text(record, "lead_Дата перехода Передан в доставку")));
        row.setIssuedOrPvzTs(parseUnixTs(text(record, "issued_or_pvz_ts")));
        row.setReceivedTs(parseUnixTs(text(record, "received_ts")));
        row.setRejectedTs(parseUnixTs(text(record, "rejected_ts")));
        row.setReturnedTs(parseUnixTs(text(record, "returned_ts")));
        row.setClosedTs(parseUnixTs(text(record, "closed_ts")));

        row.setLifecycleIncomplete(parseBoolean(text(record, "lifecycle_incomplete")));
        row.setOutcomeUnknown(parseBoolean(text(record, "outcome_unknown")));
        row.setBuyoutFlag(parseBoolean(text(record, "buyout_flag")));

        return row;
    }

    private String text(CSVRecord record, String key) {
        try {
            String value = record.get(key);
            if (value == null) {
                return null;
            }
            value = value.trim();
            return value.isBlank() ? null : value;
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeId(String value) {
        if (value == null) {
            return null;
        }
        if ("0".equals(value) || "0.0".equals(value)) {
            return null;
        }
        if (value.endsWith(".0")) {
            return value.substring(0, value.length() - 2);
        }
        return value;
    }

    private String normalizeQualification(String value) {
        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }

        String v = value.trim().toUpperCase();

        if (v.startsWith("A") || v.startsWith("А")) return "A";
        if (v.startsWith("B") || v.startsWith("В")) return "B";
        if (v.startsWith("C") || v.startsWith("С")) return "C";
        if (v.startsWith("D") || v.startsWith("Д")) return "D";
        if (v.startsWith("E") || v.startsWith("Е")) return "E";

        if (v.contains("НЕ КВАЛ") || v.contains("НЕКВАЛ")) {
            return "NOT_QUAL";
        }

        return "UNKNOWN";
    }
    private LocalDate parseDate(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseUnixTs(String value) {
        if (value == null) {
            return null;
        }
        try {
            long epochSeconds = (long) Double.parseDouble(value);
            if (epochSeconds <= 0) {
                return null;
            }
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean parseBoolean(String value) {
        if (value == null) {
            return null;
        }

        if ("true".equalsIgnoreCase(value) || "1".equals(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
            return false;
        }

        return null;
    }
}