package com.hackathon.sla_service.service;

import com.hackathon.sla_service.dto.OrderDto;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class OrderStorageService {
    private final Map<String, OrderDto> storage = new HashMap<>();

    public void saveAll(List<OrderDto> orders) {
        for (OrderDto order : orders) {
            storage.put(order.getLeadId(), order);
        }
    }

    public List<OrderDto> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void clear() {
        storage.clear();
    }

    public int getCount() {
        return storage.size();
    }
}