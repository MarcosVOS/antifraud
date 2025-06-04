package com.bradesco.antifraud.controller;

import com.bradesco.antifraud.model.AccessLog;
import com.bradesco.antifraud.service.AccessLogService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class AccessLogController {

    private final AccessLogService accessLogService;

    @GetMapping("/{id}")
    public ResponseEntity<AccessLog> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(accessLogService.findById(id));
    }

    @PostMapping
    public ResponseEntity<AccessLog> create(@RequestBody AccessLog log) {
        AccessLog savedLog = accessLogService.create(log);
        return ResponseEntity.ok(savedLog);
    }
}
