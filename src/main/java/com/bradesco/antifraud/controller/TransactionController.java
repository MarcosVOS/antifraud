package com.bradesco.antifraud.controller;

import com.bradesco.antifraud.dto.TransactionDTO;
import com.bradesco.antifraud.mapper.TransactionMapper;
import com.bradesco.antifraud.model.Transaction;
import com.bradesco.antifraud.service.TransactionService;
import jakarta.validation.Valid; // Importação correta para @Valid
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.server.ResponseStatusException; 
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors; 

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper; 

    public TransactionController(TransactionService transactionService, TransactionMapper transactionMapper) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> create(@Valid @RequestBody TransactionDTO transactionDTO) {
        Transaction transactionToCreate = transactionMapper.toEntity(transactionDTO);
        
        Transaction createdTransaction = transactionService.create(transactionToCreate);
        
        TransactionDTO createdTransactionDTO = transactionMapper.toDTO(createdTransaction);

        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                                                  .path("/{id}")
                                                  .buildAndExpand(createdTransactionDTO.getId())
                                                  .toUri();
        return ResponseEntity.created(location).body(createdTransactionDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> findById(@PathVariable UUID id) {
        Optional<Transaction> transactionOptional = transactionService.findById(id);

        if (transactionOptional.isPresent()) {
            TransactionDTO transactionDTO = transactionMapper.toDTO(transactionOptional.get());
            return ResponseEntity.ok(transactionDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> update(@PathVariable UUID id, @Valid @RequestBody TransactionDTO transactionDTO) {
        transactionDTO.setId(id); 
        Transaction transactionToUpdate = transactionMapper.toEntity(transactionDTO);
        Transaction updatedTransaction = transactionService.update(id, transactionToUpdate);
        TransactionDTO updatedTransactionDTO = transactionMapper.toDTO(updatedTransaction);
        return ResponseEntity.ok(updatedTransactionDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build(); 
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> findAll() {
        List<Transaction> transactions = transactionService.findAll();
        List<TransactionDTO> transactionDTOs = transactions.stream()
            .map(transactionMapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(transactionDTOs);
    }
}
