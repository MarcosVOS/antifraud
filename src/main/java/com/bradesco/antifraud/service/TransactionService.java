package com.bradesco.antifraud.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import com.bradesco.antifraud.dto.AccountDto;
import com.bradesco.antifraud.model.Account;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.bradesco.antifraud.model.Transaction;
import com.bradesco.antifraud.repository.TransactionRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class TransactionService {
    
    private final TransactionRepository repository;
    private final AccountService accountService;


    public TransactionService(TransactionRepository repository, AccountService accountService){
        this.repository = repository;
        this.accountService = accountService;
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
            if (!hasSourceAccount) {
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
            if (!hasDestinationAccount) {
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
        }
        
    }

    public ResponseEntity<Transaction> processTransaction(Transaction transaction) {

        System.out.println("\n\n\n\n\n\n\n\n");
        System.out.println("Payment : " + transaction.getContaDeOrigem()+ transaction.getTipo());
        System.out.println("\n\n\n\n\n\n\n\n");

        Transaction created = create(transaction);
        BigDecimal value = created.getValor();
        UUID accountDeDestinoId = created.getContaDeDestino().getId();
        UUID accountDeOrigemId = created.getContaDeOrigem().getId();
        BigDecimal balanceDeDestino = created.getContaDeOrigem().getBalance();
        BigDecimal balanceDeOrigem = created.getContaDeDestino().getBalance();

        //Ajustar o saldo das contas envolvidas
        //o balance de quem envia não está sendo subtraido corretamente

        if (created.getTipo() == Transaction.TransactionType.PAGAMENTO) {
            if (balanceDeDestino.compareTo(value) < 0) {
                return ResponseEntity.badRequest().build();
            }
            balanceDeDestino = balanceDeDestino.subtract(value);
            balanceDeOrigem = balanceDeOrigem.add(value);
        } else if (created.getTipo() == Transaction.TransactionType.TRANSFERENCIA) {
            if (balanceDeOrigem.compareTo(value) < 0) {
                return ResponseEntity.badRequest().build();
            }
            balanceDeOrigem = balanceDeOrigem.subtract(value);
            balanceDeDestino = balanceDeDestino.add(value);
        } else if (created.getTipo() == Transaction.TransactionType.DEPOSITO) {
            if (balanceDeDestino.compareTo(value) < 0) {
                return ResponseEntity.badRequest().build();
            }
            balanceDeDestino = balanceDeDestino  .add(value);
            balanceDeOrigem = balanceDeOrigem.subtract(value);
        } else {
            return ResponseEntity.badRequest().build();
        }
        // Atualizar o saldo das contas envolvidas
        Account accountDestinoDto = Account.builder()
                .id(accountDeDestinoId)
                .balance(balanceDeDestino)
                .build();

        Account accountOrigemDto = Account.builder()
                .id(accountDeOrigemId)
                .balance(balanceDeOrigem)
                .build();

        accountService.updateAccount(accountDeDestinoId, accountDestinoDto);
        accountService.updateAccount(accountDeOrigemId, accountOrigemDto);

        Transaction transactionResponse = Transaction.builder()
                .id(created.getId())
                .tipo(created.getTipo())
                .valor(created.getValor())
                .dataHora(created.getDataHora())
                .descricao(created.getDescricao())
                .build();

        return ResponseEntity.ok(transactionResponse);
    }

        

    private ResponseStatusException conflict(String msg){
        return new ResponseStatusException(HttpStatus.CONFLICT, msg);
    }
}
