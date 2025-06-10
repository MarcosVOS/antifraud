package com.bradesco.antifraud.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String sessionId;

    public LoginResponse(String sessionId) {
        this.sessionId = sessionId;
    }
}
