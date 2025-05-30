package com.bradesco.antifraud.service;

import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account getAccountById(UUID id) {

        // Logic to retrieve an account by its ID
         return accountRepository.findById(id).orElse(null);
     }
}
