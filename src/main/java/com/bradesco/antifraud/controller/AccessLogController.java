package com.bradesco.antifraud.controller;

import com.bradesco.antifraud.model.AccessLog;
import com.bradesco.antifraud.service.AccessLogService;

import lombok.RequiredArgsConstructor;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> create(@RequestParam(required = false) UUID customerId, HttpServletRequest request) {
        if (customerId == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Customer ID is required."); // 400 (Bad Request se faltar ID de customer)
        }

        try {
            AccessLog savedLog = accessLogService.createLog(customerId, request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedLog); // 201 (Created com o log salvo)
        } catch (EntityNotFoundException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage()); // 404 (Not Found se o customer não existir)
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        try {
            accessLogService.deleteById(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found
        }
    }
}