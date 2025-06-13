package com.bradesco.antifraud.service;

import com.bradesco.antifraud.dto.AccountDto;
import com.bradesco.antifraud.exception.accountExceptions.AccountAlreadyExistsException;
import com.bradesco.antifraud.mapper.AccountMapper;
import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Customer;
import com.bradesco.antifraud.repository.AccountRepository;
import com.bradesco.antifraud.repository.CustomerRepository;

import jakarta.persistence.EntityNotFoundException;


import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final CustomerRepository customerRepository;


    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.customerRepository = customerRepository;
    }

    public Optional<Account> getAccountById(UUID id) {
       if(!accountRepository.existsById(id)) {
            throw new EntityNotFoundException("Account with ID " + id + " does not exist.");
        }
        return accountRepository.findById(id);
    }


    public Account createAccount(@NotNull AccountDto newAccount) {

        if (accountRepository.existsByAccountNumber((newAccount.getAccountNumber()))) {
            throw new AccountAlreadyExistsException("Account with Id " + newAccount.getId() + " already exists.");
        }

        Account account = accountMapper.toEntity(newAccount);


        Customer customer = customerRepository.findById(newAccount.getCustomerId())
                  .orElseThrow(() -> new EntityNotFoundException("Customer with ID " + newAccount.getCustomerId() + " not found."));
        account.setCustomer(customer);

        return accountRepository.save(account);
    }

    public void deleteAccount(UUID id) {
        if (!accountRepository.existsById(id)) {
            throw new EntityNotFoundException("Account with ID " + id + " does not exist.");
        }
        accountRepository.deleteById(id);
    }


    public Account updateAccount(UUID id, AccountDto updatedAccountData) { // Pode precisar do customerId se for alterável
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account with ID " + id + " does not exist."));


        // Verificar se o accountNumber está sendo alterado para um já existente (excluindo o próprio)
        Optional<Account> accountByNewNumber = accountRepository.findByAccountNumber(updatedAccountData.getAccountNumber());
        if (accountByNewNumber.isPresent() && !accountByNewNumber.get().getId().equals(id)) {
            throw new AccountAlreadyExistsException("Account with number " + updatedAccountData.getAccountNumber() + " already exists.");
        }

        if (updatedAccountData.getAgency() != null) {
        existingAccount.setAgency(updatedAccountData.getAgency());
    }
        if (updatedAccountData.getBalance() != null) {
        existingAccount.setBalance(updatedAccountData.getBalance());
    }
        if (updatedAccountData.getAccountType() != null) {
        existingAccount.setAccountType(updatedAccountData.getAccountType());
    }
        if (updatedAccountData.getAccountStatus() != null) {
        existingAccount.setAccountStatus(updatedAccountData.getAccountStatus());
    }


        return accountRepository.save(existingAccount);
    }

    public Account updateAccount(UUID id, Account updatedAccountData) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account with ID " + id + " does not exist."));


        // Verificar se o accountNumber está sendo alterado para um já existente (excluindo o próprio)
        Optional<Account> accountByNewNumber = accountRepository.findByAccountNumber(updatedAccountData.getAccountNumber());
        if (accountByNewNumber.isPresent() && !accountByNewNumber.get().getId().equals(id)) {
            throw new AccountAlreadyExistsException("Account with number " + updatedAccountData.getAccountNumber() + " already exists.");
        }



        if (updatedAccountData.getAgency() != null) {
            existingAccount.setAgency(updatedAccountData.getAgency());
        }
        if (updatedAccountData.getBalance() != null) {
            existingAccount.setBalance(updatedAccountData.getBalance());
        }
        if (updatedAccountData.getAccountType() != null) {
            existingAccount.setAccountType(updatedAccountData.getAccountType());
        }
        if (updatedAccountData.getAccountStatus() != null) {
            existingAccount.setAccountStatus(updatedAccountData.getAccountStatus());
        }


        return accountRepository.save(existingAccount);
    }


    public boolean accountExists(UUID id) {
        return accountRepository.existsById(id);
    }


}
