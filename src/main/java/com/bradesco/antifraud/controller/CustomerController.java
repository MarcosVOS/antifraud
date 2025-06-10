package com.bradesco.antifraud.controller;

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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AccessLogService accessLogService;
    private final TokenService tokenService;
    private final SessionService sessionService;

    private static final SecureRandom random = new SecureRandom();

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody @Valid Customer customer) {
        Customer created = customerService.create(customer);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable UUID id, @RequestBody @Valid Customer customer) {
        Customer updated = customerService.update(id, customer);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Customer>> list() {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        String sessionUuid = UUID.randomUUID().toString();
        sessionService.storeSession(sessionUuid, userId);

        return ResponseEntity.ok(new LoginResponse(sessionUuid));
    }
}
