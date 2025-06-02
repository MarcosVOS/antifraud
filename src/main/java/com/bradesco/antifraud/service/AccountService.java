package com.bradesco.antifraud.service;


import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Optional<Account> getAccountById(UUID id) {

         return  accountRepository.findById(id);
     }

    public Account createAccount(Account newAccount) {

        return accountRepository.save(newAccount);
    }
    public void deleteAccount(UUID id) {
        accountRepository.deleteById(id);
    }
    public Account updateAccount(UUID id, Account updatedAccount) {
        if (!accountRepository.existsById(id)) {
            throw new IllegalArgumentException("Account with ID " + id + " does not exist.");
        }
        updatedAccount.setId(id);
        return accountRepository.save(updatedAccount);
    }
    public boolean accountExists(UUID id) {
        return accountRepository.existsById(id);
    }


}
