package com.bradesco.antifraud.controller;

import com.bradesco.antifraud.model.EmailRequest;
import com.bradesco.antifraud.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/sendmailTest")
class EmailSystem {
    private final EmailService emailService;

    EmailSystem(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping()
    public ResponseEntity<String> sendMail(@RequestBody EmailRequest emailRequest) {

        emailService.sendEmail(emailRequest);
        return ResponseEntity.ok("Email sent successfully");
    }
}
