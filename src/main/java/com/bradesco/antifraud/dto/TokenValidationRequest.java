package com.bradesco.antifraud.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class TokenValidationRequest {
    private UUID token;
}
