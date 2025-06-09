package com.bradesco.antifraud.service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.bradesco.antifraud.model.Transaction;
import com.bradesco.antifraud.repository.TransactionRepository;
import com.bradesco.antifraud.mapper.TransactionMapper; // Importa o mapper

import jakarta.persistence.EntityNotFoundException;

@Service
public class TransactionService {

    private final TransactionRepository repository; 
    private final TransactionMapper transactionMapper; 

    public TransactionService(TransactionRepository repository, TransactionMapper transactionMapper){
        this.repository = repository;
        this.transactionMapper = transactionMapper;
    }

    public Transaction create(Transaction transaction){
        validate(transaction);
        return repository.save(transaction);
    }

    public Optional<Transaction> findById(UUID id){
        return repository.findById(id);
    }

    public Transaction update(UUID id, Transaction updated){
        if(!repository.existsById(id)){
            throw new EntityNotFoundException("Transaction not found");
        }
        validate(updated);
        updated.setId(id);
        return repository.save(updated);
    }

    public void delete(UUID id){
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Requested transaction not found");
        }
        repository.deleteById(id);
    }

    public ArrayList<Transaction> findAll(){
        return (ArrayList<Transaction>) repository.findAll();
    }

    private void validate(Transaction transaction){
        boolean hasSourceAccount = transaction.getContaDeOrigem() != null;
        boolean hasDestinationAccount = transaction.getContaDeDestino() != null;
        Transaction.TransactionType type = transaction.getTipo();

        if (type == Transaction.TransactionType.DEPOSITO) {
            if (hasSourceAccount) {
                throw conflict("Deposit should not have a source account.");
            }
            if (!hasDestinationAccount) {
                throw conflict("Deposit must have a destination account.");
            }
        }
        else if (type == Transaction.TransactionType.SAQUE) {
            if (!hasSourceAccount) {
                throw conflict("Withdrawal must have a source account.");
            }
            if (hasDestinationAccount) {
                throw conflict("Withdrawal should not have a destination account.");
            }
        }
        else if (type == Transaction.TransactionType.TRANSFERENCIA) {
            if (!hasSourceAccount || !hasDestinationAccount) {
                throw conflict("Transfer must have both source and destination accounts.");
            }
        }
        else if (type == Transaction.TransactionType.PAGAMENTO) {
            if (!hasSourceAccount) {
                throw conflict("Payment must have a source account.");
            }
            if (hasDestinationAccount) {
                throw conflict("Payment should not have a destination account.");
            }
        }
    }
    private ResponseStatusException conflict(String msg){
        return new ResponseStatusException(HttpStatus.CONFLICT, msg);
    }
}
