package com.hackathon.sla_service.controller;

import com.hackathon.sla_service.dto.OrderDto;
import com.hackathon.sla_service.service.OrderStorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/data")
public class DataLoadController {

    private final OrderStorageService storageService;

    public DataLoadController(OrderStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/load")
    public Map<String, Object> loadData(@RequestParam("file") MultipartFile file) {
        try {
            List<OrderDto> orders = parseCsv(file);
            storageService.saveAll(orders);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("loaded", orders.size());
            response.put("total", storageService.getCount());
            response.put("message", "Данные загружены");
            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return error;
        }
    }

    @GetMapping("/orders")
    public List<OrderDto> getAllOrders() {
        return storageService.findAll();
    }

    @DeleteMapping("/clear")
    public Map<String, Object> clear() {
        storageService.clear();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Все данные удалены");
        return response;
    }

    private List<OrderDto> parseCsv(MultipartFile file) throws IOException {
        List<OrderDto> orders = new ArrayList<>();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
        );

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        // Читаем заголовок
        String header = reader.readLine();
        if (header == null) {
            throw new IOException("Файл пуст");
        }

        String line;
        int lineNumber = 1;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            String[] fields = line.split(",");

            if (fields.length < 15) {
                System.err.println("Строка " + lineNumber + ": недостаточно полей");
                continue;
            }

            try {
                OrderDto order = new OrderDto();

                // Парсинг полей по индексам (временная версия)
                order.setLeadId(getString(fields, 0));
                order.setLeadCreatedAt(getDateTime(fields, 1, formatter));
                order.setSaleTs(getDateTime(fields, 2, formatter));
                order.setAssemblyTs(getDateTime(fields, 3, formatter));
                order.setHandedToDeliveryTs(getDateTime(fields, 4, formatter));
                order.setIssuedOrPvzTs(getDateTime(fields, 5, formatter));
                order.setReceivedTs(getDateTime(fields, 6, formatter));
                order.setRejectedTs(getDateTime(fields, 7, formatter));
                order.setReturnedTs(getDateTime(fields, 8, formatter));
                order.setClosedTs(getDateTime(fields, 9, formatter));
                order.setLifecycleIncomplete(getBoolean(fields, 10));
                order.setOutcomeUnknown(getBoolean(fields, 11));
                order.setLeadResponsibleUserId(getString(fields, 12));
                order.setQualification(getString(fields, 13));
                order.setDeliveryService(getString(fields, 14));

                orders.add(order);

            } catch (Exception e) {
                System.err.println("Ошибка в строке " + lineNumber + ": " + e.getMessage());
            }
        }

        return orders;
    }

    private String getString(String[] fields, int index) {
        if (index >= fields.length) return null;
        String value = fields[index].trim();
        return value.isEmpty() || "null".equals(value) ? null : value;
    }

    private LocalDateTime getDateTime(String[] fields, int index, DateTimeFormatter formatter) {
        String value = getString(fields, index);
        if (value == null) return null;
        try {
            return LocalDateTime.parse(value, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean getBoolean(String[] fields, int index) {
        String value = getString(fields, index);
        if (value == null) return null;
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }
}