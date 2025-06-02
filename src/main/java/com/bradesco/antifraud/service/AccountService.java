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
}
