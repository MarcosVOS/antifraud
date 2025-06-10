package com.bradesco.antifraud.controller;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bradesco.antifraud.dto.LoginRequest;
import com.bradesco.antifraud.dto.LoginResponse;
import com.bradesco.antifraud.dto.TokenValidationRequest;
import com.bradesco.antifraud.model.Customer;
import com.bradesco.antifraud.model.EmailRequest;
import com.bradesco.antifraud.service.AccessLogService;
import com.bradesco.antifraud.service.CustomerService;
import com.bradesco.antifraud.service.EmailService;
import com.bradesco.antifraud.service.SessionService;
import com.bradesco.antifraud.service.TokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AccessLogService accessLogService;
    private final TokenService tokenService;
    private final SessionService sessionService;
    private static final SecureRandom random = new SecureRandom();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {
        Customer customer = customerService.findByEmail(request.email());
        String status;
        if (customer == null || !passwordEncoder.matches(request.password(), customer.getPassword())) {
            status = "FAILURE-INVALID_CREDENTIALS";
            if (customer != null) {
                accessLogService.createLog(customer.getId(), httpRequest, "LOGIN", status);
            }
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        //String token = String.format("%06d", random.nextInt(1_000_000));
        String token = UUID.randomUUID().toString();
        tokenService.storeToken(token, customer.getId(), Duration.ofMinutes(15));

        EmailRequest emailRequest = EmailRequest.builder()
                .senderAddress(request.email())
                .subject("Confirmação de Login: " + token)
                .build();

        emailService.sendEmail(emailRequest);

        status = "SUCCESS-TOKEN_SENT";
        accessLogService.createLog(customer.getId(), httpRequest, "LOGIN", status);

        return ResponseEntity.ok("Verification token sent to email");
    }

    @PostMapping("/verify-token")
    public ResponseEntity<LoginResponse> verifyToken(@RequestBody TokenValidationRequest request) {
        UUID userId = tokenService.validateAndConsumeToken(request.getToken());
        if (userId == null) {
            accessLogService.createLog(null, null, "LOGIN", "FAILURE-TOKEN_INVALID");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        String sessionUuid = UUID.randomUUID().toString();
        sessionService.storeSession(sessionUuid, userId);

        accessLogService.createLog(userId, null, "LOGIN", "SUCCESS-TOKEN_VALIDATED");
        return ResponseEntity.ok(new LoginResponse(sessionUuid));
    }

    @GetMapping("/session/{sessionToken}")
    public ResponseEntity<?> getSession(@PathVariable String sessionToken, HttpServletRequest httpRequest) {
        UUID userId = sessionService.getUserIdBySession(sessionToken);
        if (userId == null) {
            accessLogService.createLog(userId, httpRequest, "LOGIN", "FAILURE-SESSION_NOT_FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sessão não encontrada ou expirada");
        }

        accessLogService.createLog(userId, httpRequest, "LOGIN", "SUCCESS-SESSION_FOUND");
        return ResponseEntity.ok().body(customerService.findById(userId));
    }
}
