package com.bradesco.antifraud.service;


import com.bradesco.antifraud.exception.accountExceptions.AccountAlreadyExistsException;
import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;


    public AccountService(AccountRepository accountRepository
    ) {
        this.accountRepository = accountRepository;
    }

    public Optional<Account> getAccountById(UUID id) {
       if(!accountRepository.existsById(id)) {
            throw new EntityNotFoundException("Account with ID " + id + " does not exist.");
        }
        return accountRepository.findById(id);
    }

    @Transactional
    public Account createAccount(Account newAccount) {
        // 1. Verificar se já existe uma conta com o mesmo Id
        if (accountRepository.findByAccountNumber(newAccount.getAccountNumber())) {
            throw new AccountAlreadyExistsException("Account with Id " + newAccount.getId() + " already exists.");
        }

        return accountRepository.save(newAccount);
    }

    public void deleteAccount(UUID id) {
        if (!accountRepository.existsById(id)) {
            throw new EntityNotFoundException("Account with ID " + id + " does not exist.");
        }
        accountRepository.deleteById(id);
    }

    @Transactional
    public Account updateAccount(UUID id, Account updatedAccountData) { // Pode precisar do customerId se for alterável
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account with ID " + id + " does not exist."));


        // Verificar se o accountNumber está sendo alterado para um já existente (excluindo o próprio)
        Optional<Account> accountByNewNumber = accountRepository.findById(updatedAccountData.getId());
        if (accountByNewNumber.isPresent() && !accountByNewNumber.get().getId().equals(id)) {
            throw new AccountAlreadyExistsException("Account with number " + updatedAccountData.getId() + " already exists.");
        }

        // Atualizar campos da existingAccount com updatedAccountData
        existingAccount.setAccountNumber(updatedAccountData.getAccountNumber());
        existingAccount.setAgency(updatedAccountData.getAgency());
        existingAccount.setBalance(updatedAccountData.getBalance());
        existingAccount.setAccountType(updatedAccountData.getAccountType());
        existingAccount.setAccountStatus(updatedAccountData.getAccountStatus());


        return accountRepository.save(existingAccount);
    }

    public boolean accountExists(UUID id) {
        return accountRepository.existsById(id);
    }


}
