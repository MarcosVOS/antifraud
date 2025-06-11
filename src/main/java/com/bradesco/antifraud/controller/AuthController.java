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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "User login", description = "Performs login and sends a verification token by email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token sent to email"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User login data", required = true, content = @Content(schema = @Schema(implementation = LoginRequest.class))) @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest) {
        Customer customer = customerService.findByEmail(request.email());
        String status;
        if (customer == null || !passwordEncoder.matches(request.password(), customer.getPassword())) {
            status = "FAILURE-INVALID_CREDENTIALS";
            if (customer != null) {
                accessLogService.createLog(customer.getId(), httpRequest, "LOGIN", status);
            }
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        // String token = String.format("%06d", random.nextInt(1_000_000));
        String token = UUID.randomUUID().toString();
        tokenService.storeToken(token, customer.getId(), Duration.ofMinutes(15));

        EmailRequest emailRequest = EmailRequest.builder()
                .senderAddress(request.email())
                .subject("Login Confirmation: " + token)
                .build();

        emailService.sendEmail(emailRequest);

        status = "SUCCESS-TOKEN_SENT";
        accessLogService.createLog(customer.getId(), httpRequest, "LOGIN", status);

        return ResponseEntity.ok("Verification token sent to email");
    }

    @Operation(summary = "Verify login token", description = "Validates the token sent to the email and returns the session token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid token, session created", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content)
    })
    @PostMapping("/verify-token")
    public ResponseEntity<LoginResponse> verifyToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Verification token received by email", required = true, content = @Content(schema = @Schema(implementation = TokenValidationRequest.class))) @RequestBody TokenValidationRequest request,
            HttpServletRequest httpRequest) {
        UUID userId = tokenService.validateAndConsumeToken(request.getToken());
        if (userId == null) {
            accessLogService.createLog(null, httpRequest, "LOGIN", "FAILURE-TOKEN_INVALID");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Customer customer = customerService.findByEmail(request.getEmail());
        if (customer == null || !customer.getId().equals(userId)) {
            accessLogService.createLog(userId, httpRequest, "LOGIN", "FAILURE-EMAIL_MISMATCH");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        String sessionUuid = UUID.randomUUID().toString();
        sessionService.storeSession(sessionUuid, userId);

        accessLogService.createLog(userId, httpRequest, "LOGIN", "SUCCESS-TOKEN_VALIDATED");
        return ResponseEntity.ok(new LoginResponse(sessionUuid));
    }

    @Operation(summary = "Get user session", description = "Returns information about the authenticated user by session.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session found and valid"),
            @ApiResponse(responseCode = "404", description = "Session not found or expired", content = @Content)
    })
    @GetMapping("/session/{sessionToken}")
    public ResponseEntity<?> getSession(
            @Parameter(description = "Session token", required = true) @PathVariable String sessionToken,
            HttpServletRequest httpRequest) {
        UUID userId = sessionService.getUserIdBySession(sessionToken);
        if (userId == null) {
            accessLogService.createLog(userId, httpRequest, "LOGIN", "FAILURE-SESSION_NOT_FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found or expired");
        }

        Customer customer;
        try {
            customer = customerService.findById(userId);
        } catch (Exception e) {
            accessLogService.createLog(userId, httpRequest, "LOGIN", "FAILURE-USER_NOT_FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        accessLogService.createLog(userId, httpRequest, "LOGIN", "SUCCESS-SESSION_FOUND");
        return ResponseEntity.ok().body(customer);
    }
}
