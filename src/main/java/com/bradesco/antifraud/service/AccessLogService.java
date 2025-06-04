package com.bradesco.antifraud.service;

import com.bradesco.antifraud.model.AccessLog;
import com.bradesco.antifraud.repository.AccessLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessLogService {

    private final AccessLogRepository repository;

    public AccessLog create(AccessLog log) {
        return repository.save(log);
    }

    public AccessLog findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Access log not found"));
    }
}
