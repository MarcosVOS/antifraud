package com.bradesco.antifraud.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

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

    private TransactionService service;

    public TransactionController(TransactionService service){
        this.service = service;
    }
    
    @PostMapping
    public ResponseEntity<Transaction> create(@Valid @RequestBody Transaction transaction){
        Transaction created = service.create(transaction);
        URI location = URI.create("/transactions/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Transaction>> findById(@PathVariable UUID id){
        Optional<Transaction> transaction = service.findById(id);
        return ResponseEntity.ok(transaction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> update(@PathVariable UUID id, @Valid @RequestBody Transaction transaction){
        Transaction updated = service.update(id, transaction);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable UUID id){
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ArrayList<Transaction>> findAll(){
        return ResponseEntity.ok(service.findAll());
    }

    
}
