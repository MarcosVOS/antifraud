package com.bradesco.antifraud.service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.bradesco.antifraud.dto.TransactionRequest;
import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Transaction;
import com.bradesco.antifraud.repository.TransactionRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountService accountService; // Injete AccountService

    public TransactionService(TransactionRepository repository, AccountService accountService) {
        this.repository = repository;
        this.accountService = accountService;
    }

    @Transactional
    public Transaction create(TransactionRequest request) { // Recebe o DTO de requisição
        Transaction transaction = convertToEntity(request); // Converte DTO para Entidade
        validate(transaction);
        return repository.save(transaction);
    }

    public Optional<Transaction> findById(UUID id) {
        return repository.findById(id);
    }

    @Transactional
    public Transaction update(UUID id, TransactionRequest request) { // Recebe o DTO de requisição
        Transaction existingTransaction = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Transação com ID " + id + " não encontrada."));

        // Converte o DTO para uma nova entidade temporária para obter os dados atualizados
        Transaction updatedTransactionData = convertToEntity(request);

        // Transfere os dados do DTO convertido para a entidade existente
        existingTransaction.setTipo(updatedTransactionData.getTipo());
        existingTransaction.setValor(updatedTransactionData.getValor());
        existingTransaction.setDataHora(updatedTransactionData.getDataHora());
        existingTransaction.setDescricao(updatedTransactionData.getDescricao());
        existingTransaction.setContaDeOrigem(updatedTransactionData.getContaDeOrigem());
        existingTransaction.setContaDeDestino(updatedTransactionData.getContaDeDestino());

        validate(existingTransaction); // Valida a transação atualizada
        return repository.save(existingTransaction);
    }

    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Transação com ID " + id + " não encontrada.");
        }
        repository.deleteById(id);
    }

    public ArrayList<Transaction> findAll() {
        return (ArrayList<Transaction>) repository.findAll();
    }

    // Método auxiliar para converter TransactionRequest para Transaction Entity
    private Transaction convertToEntity(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setTipo(request.getTipo());
        transaction.setValor(request.getValor());
        transaction.setDataHora(request.getDataHora());
        transaction.setDescricao(request.getDescricao());

        // Busca a entidade Account para contaDeOrigem
        if (request.getContaDeOrigem() != null) {
            try {
                UUID originAccountId = UUID.fromString(request.getContaDeOrigem());
                Account originAccount = accountService.findById(originAccountId)
                    .orElseThrow(() -> new EntityNotFoundException("Conta de origem com ID " + originAccountId + " não encontrada."));
                transaction.setContaDeOrigem(originAccount);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de UUID inválido para contaDeOrigem: " + e.getMessage(), e);
            }
        }

        // Busca a entidade Account para contaDeDestino
        if (request.getContaDeDestino() != null) {
            try {
                UUID destinationAccountId = UUID.fromString(request.getContaDeDestino());
                Account destinationAccount = accountService.findById(destinationAccountId)
                    .orElseThrow(() -> new EntityNotFoundException("Conta de destino com ID " + destinationAccountId + " não encontrada."));
                transaction.setContaDeDestino(destinationAccount);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de UUID inválido para contaDeDestino: " + e.getMessage(), e);
            }
        }
        return transaction;
    }

    private void validate(Transaction transaction) {
        boolean hasSourceAccount = transaction.getContaDeOrigem() != null;
        boolean hasDestinationAccount = transaction.getContaDeDestino() != null;
        Transaction.TransactionType type = transaction.getTipo();

        if (type == Transaction.TransactionType.DEPOSITO) {
            if (hasSourceAccount) {
                throw conflict("Para depósito, não deve haver conta de origem.");
            }
            if (!hasDestinationAccount) {
                throw conflict("Para depósito, deve haver uma conta de destino.");
            }
        } else if (type == Transaction.TransactionType.SAQUE) {
            if (!hasSourceAccount) {
                throw conflict("Para saque, deve haver uma conta de origem.");
            }
            if (hasDestinationAccount) {
                throw conflict("Para saque, não deve haver conta de destino.");
            }
        } else if (type == Transaction.TransactionType.TRANSFERENCIA) {
            if (!hasSourceAccount || !hasDestinationAccount) {
                throw conflict("Para transferência, deve haver conta de origem e destino.");
            }
            if (hasSourceAccount && hasDestinationAccount && transaction.getContaDeOrigem().getId().equals(transaction.getContaDeDestino().getId())) {
                throw conflict("Conta de origem e destino não podem ser a mesma para transferência.");
            }
        } else if (type == Transaction.TransactionType.PAGAMENTO) {
            if (!hasSourceAccount) {
                throw conflict("Para pagamento, deve haver uma conta de origem.");
            }
            if (hasDestinationAccount) {
                throw conflict("Para pagamento, não deve haver conta de destino.");
            }
        }
    }

    private ResponseStatusException conflict(String msg) {
        return new ResponseStatusException(HttpStatus.CONFLICT, msg);
    }
}