package com.bradesco.antifraud.service;

import com.bradesco.antifraud.model.AccessLog;
import com.bradesco.antifraud.model.Customer;
import com.bradesco.antifraud.repository.AccessLogRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessLogService {

    private final AccessLogRepository repository;
    private final CustomerService customerService;

    public AccessLog create(AccessLog log) {
        return repository.save(log);
    }

    public AccessLog findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Access log not found"));
    }

    public AccessLog createLog(UUID customerId, HttpServletRequest request) {
    Customer customer = customerService.findById(customerId);

    String ip = getClientIp(request);
    String userAgent = request.getHeader("User-Agent");
    String path = request.getRequestURI();
    LocalDateTime accessTime = LocalDateTime.now();

    AccessLog log = AccessLog.builder()
            .customer(customer)
            .ipAddress(ip)
            .userAgent(userAgent)
            .path(path)
            .accessTime(accessTime)
            .build();

    return repository.save(log);
}

private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
