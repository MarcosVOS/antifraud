package com.bradesco.antifraud.service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.bradesco.antifraud.model.Transaction;
import com.bradesco.antifraud.repository.TransactionRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class TransactionService {
    
    private TransactionRepository repository;

    public TransactionService(TransactionRepository repository){
        this.repository = repository;
    }

    public Transaction create(Transaction transaction){
        return repository.save(transaction);
    }
    public Optional<Transaction> findById(UUID id){
        return repository.findById(id);
    }

    public Transaction update(UUID id, Transaction nova){
        if(!repository.existsById(id)){
            return null;
        }
        return repository.save(nova);
    }
    public void delete(UUID id){
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Trasação solicitada não encontrada");
        }
        repository.deleteById(id);
    }
    public ArrayList<Transaction> findAll(){
        return (ArrayList<Transaction>) repository.findAll();
    }

    private void validacao(Transaction transaction){
        boolean contaOrigem = transaction.getContaDeOrigem() != null;
        boolean contaDestino = transaction.getContaDeDestino() != null;
        Transaction.TransactionType tipo = transaction.getTipo();
    

        if (tipo == Transaction.TransactionType.DEPOSITO) {
            if (contaOrigem) {
                throw conflito("Sem conta de origem.");
            }
            if (!contaDestino) {
                throw conflito("Deve ter conta de destino.");
            }
        }
        else if (tipo == Transaction.TransactionType.SAQUE) {
            if (!contaOrigem) {
                throw conflito("Deve ter uma conta de origem.");
            }
            if (contaDestino) {
                throw conflito("Sem conta de destino.");
            }
        }    
        else if (tipo == Transaction.TransactionType.TRANSFERENCIA) {
            if (!contaOrigem && contaDestino) {
                throw conflito("Deve ter conta de origem e de destino.");
            }
        }
        else if (tipo == Transaction.TransactionType.PAGAMENTO) {
            if (!contaOrigem&&contaDestino) {
                throw conflito("Deve ter conta de origem e destino");
            }
        }         
    }
    private ResponseStatusException conflito(String msg){
        return new ResponseStatusException(HttpStatus.CONFLICT, msg);
    }
}
