package com.bradesco.antifraud.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @Email(message = "Email inválido") String email,
    @NotBlank(message = "Senha obrigatória") String password
) {}