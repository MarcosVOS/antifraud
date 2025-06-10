package com.bradesco.antifraud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "access_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank(message = "Action is required")
    private String action;

    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @NotNull(message = "Customer is required")
    private Customer customer;

    private String userAgent;
    private String path;
    private LocalDateTime accessTime;

    private String ipAddress;
    private String sessionId;
    private String status;
    private String httpMethod;
}
