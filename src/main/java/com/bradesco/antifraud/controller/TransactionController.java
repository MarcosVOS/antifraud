package com.bradesco.antifraud.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;



import com.bradesco.antifraud.service.AccessLogService;
import com.bradesco.antifraud.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bradesco.antifraud.model.Transaction;
import com.bradesco.antifraud.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService service;
    private final AccountService accountService;
    private final AccessLogService accessLogService;


    public TransactionController(TransactionService service, AccountService accountService, AccessLogService accessLogService) {
        this.service = service;

        this.accountService = accountService;
        this.accessLogService = accessLogService;
    }

    @PostMapping
    public ResponseEntity<Transaction> create(@Valid @RequestBody Transaction transaction) {
        Transaction created = service.create(transaction);
        URI location = URI.create("/transactions/" + created.getId());

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> findById(@PathVariable UUID id) {
        Optional<Transaction> transaction = service.findById(id);
        if (transaction.isPresent()) {
            return ResponseEntity.ok(transaction.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> update(@PathVariable UUID id, @Valid @RequestBody Transaction transaction) {
        Transaction updated = service.update(id, transaction);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ArrayList<Transaction>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @SuppressWarnings("null")
	@PostMapping("/payment")
    public ResponseEntity<Transaction> payment(@Valid @RequestBody Transaction transaction, HttpServletRequest httpRequest) {
        //Montar um Body
        Transaction paymentProcessor = service.processTransaction(transaction).getBody();
        //Fazer a inserção no log de acesso
        URI location = URI.create("/transactions/" + "payment-Successful" + paymentProcessor);



        System.out.println("\n\n\n\n\n\n\n\n");
        System.out.println("Payment successful for transaction ID: " + transaction.getContaDeOrigem().getCustomer().getId());
        System.out.println("\n\n\n\n\n\n\n\n");
        try {

            assert paymentProcessor != null;
            accessLogService.createLog(
                    transaction.getContaDeOrigem().getCustomer().getId(),
                    httpRequest,
                    paymentProcessor.getTipo().toString(), // Assegure que getTipo() retorne uma String adequada
                    "SUCCESS"
            );
        } catch (Exception e) {
  
            System.err.println("Error creating access log: " + e.getMessage());


            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.created(location).body(paymentProcessor);
    }
}
