package com.bradesco.antifraud.repository;

import com.bradesco.antifraud.model.AccessLog;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, UUID> {
}
